/* Copyright (c) 2022 bitlap.org */
package org.bitlap.jdbc.client

import io.grpc.ManagedChannelBuilder
import org.bitlap.network.driver.proto.BCloseSession.BCloseSessionReq
import org.bitlap.network.driver.proto.BExecuteStatement.BExecuteStatementReq
import org.bitlap.network.driver.proto.BOpenSession.BOpenSessionReq
import org.bitlap.network.driver.service.ZioService.DriverServiceClient
import org.bitlap.network.handles.{ OperationHandle, SessionHandle }
import org.bitlap.network.rpc.{ exception, RpcN }
import org.bitlap.network.{ handles, models, RpcStatus }
import scalapb.zio_grpc.ZManagedChannel
import zio.{ Layer, ZIO }

/**
 * This class mainly wraps the RPC call procedure used inside JDBC.
 *
 * @author 梦境迷离
 * @since 2021/11/21
 * @version 1.0
 */
private[jdbc] class BitlapZioClient(uri: String, port: Int) extends RpcN[ZIO] with RpcStatus {

  private val clientLayer: Layer[Throwable, DriverServiceClient] = DriverServiceClient.live(
    ZManagedChannel(ManagedChannelBuilder.forAddress(uri, port))
  )

  override def openSession(
    username: String,
    password: String,
    configuration: Map[String, String]
  ): ZIO[Any, Throwable, SessionHandle] =
    DriverServiceClient
      .openSession(BOpenSessionReq(username, password, configuration))
      .map(r => new SessionHandle(r.getSessionHandle)) // 因为server和client使用一套API。必须转换以下
      .mapError(f => new Throwable(f.asException()))
      .provideLayer(clientLayer)

  override def closeSession(sessionHandle: handles.SessionHandle): ZIO[Any, Throwable, Unit] =
    DriverServiceClient
      .closeSession(BCloseSessionReq(sessionHandle = Some(sessionHandle.toBSessionHandle())))
      .map(_ => ())
      .mapError(st => new Throwable(exception(st)))
      .provideLayer(clientLayer)

  override def executeStatement(
    sessionHandle: handles.SessionHandle,
    statement: String,
    queryTimeout: Long,
    confOverlay: Map[String, String]
  ): ZIO[Any, Throwable, OperationHandle] =
    DriverServiceClient
      .executeStatement(
        BExecuteStatementReq(statement, Some(sessionHandle.toBSessionHandle()), confOverlay, queryTimeout)
      )
      .map(r => new OperationHandle(r.getOperationHandle))
      .mapError(st => new Throwable(exception(st)))
      .provideLayer(clientLayer)

  override def executeStatement(
    sessionHandle: SessionHandle,
    statement: String,
    confOverlay: Map[String, String]
  ): ZIO[Any, Throwable, OperationHandle] = ???

  // TODO return status and hasMoreRows
  override def fetchResults(opHandle: OperationHandle): ZIO[Any, Throwable, models.RowSet] = ???

  override def getResultSetMetadata(opHandle: OperationHandle): ZIO[Any, Throwable, models.TableSchema] = ???

  override def getColumns(
    sessionHandle: SessionHandle,
    tableName: String,
    schemaName: String,
    columnName: String
  ): ZIO[Any, Throwable, OperationHandle] = ???

  override def getDatabases(pattern: String): ZIO[Any, Throwable, List[String]] = ???

  override def getTables(database: String, pattern: String): ZIO[Any, Throwable, List[String]] = ???
}
