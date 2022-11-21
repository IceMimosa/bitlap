/* Copyright (c) 2022 bitlap.org */
package org.bitlap.server.rpc

import io.grpc._
import org.bitlap.network._
import org.bitlap.network.NetworkException._
import org.bitlap.network.driver.proto._
import org.bitlap.network.driver.proto.BCancelOperation._
import org.bitlap.network.driver.proto.BCloseOperation._
import org.bitlap.network.driver.proto.BCloseSession._
import org.bitlap.network.driver.proto.BExecuteStatement._
import org.bitlap.network.driver.proto.BFetchResults._
import org.bitlap.network.driver.proto.BGetDatabases._
import org.bitlap.network.driver.proto.BGetOperationStatus._
import org.bitlap.network.driver.proto.BGetRaftMetadata._
import org.bitlap.network.driver.proto.BGetResultSetMetadata._
import org.bitlap.network.driver.proto.BGetTables._
import org.bitlap.network.driver.proto.BOpenSession._
import org.bitlap.network.driver.service.ZioService._
import org.bitlap.network.handles._
import org.bitlap.server._
import org.bitlap.tools._
import zio._

/** RPC的服务端API实现，基于 zio-grpc,zio 1.0
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/4/21
 */
@apply
final class GrpcServiceLive(private val asyncRpcBackend: AsyncRpc) extends ZDriverService[Any, Any] {

  // 直接使用zio-grpc的Status表示错误 避免处理多重错误
  override def openSession(request: BOpenSessionReq): ZIO[Any, Status, BOpenSessionResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.openSession(request.username, request.password, request.configuration)
      )
      .mapBoth(
        errorApplyFunc,
        shd =>
          BOpenSessionResp(
            configuration = request.configuration,
            sessionHandle = Some(shd.toBSessionHandle())
          )
      )

  override def closeSession(request: BCloseSessionReq): ZIO[Any, Status, BCloseSessionResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.closeSession(new SessionHandle(request.getSessionHandle))
      )
      .mapBoth(errorApplyFunc, _ => BCloseSessionResp())

  override def executeStatement(request: BExecuteStatementReq): ZIO[Any, Status, BExecuteStatementResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.executeStatement(
          new SessionHandle(request.getSessionHandle),
          request.statement,
          request.queryTimeout,
          request.confOverlay
        )
      )
      .mapBoth(errorApplyFunc, hd => BExecuteStatementResp(Some(hd.toBOperationHandle())))

  override def fetchResults(request: BFetchResultsReq): ZIO[Any, Status, BFetchResultsResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.fetchResults(new OperationHandle(request.getOperationHandle), request.maxRows.toInt, request.fetchType)
      )
      .mapBoth(errorApplyFunc, _.toBFetchResultsResp)

  override def getResultSetMetadata(request: BGetResultSetMetadataReq): ZIO[Any, Status, BGetResultSetMetadataResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.getResultSetMetadata(new OperationHandle(request.getOperationHandle))
      )
      .mapBoth(errorApplyFunc, t => BGetResultSetMetadataResp(Some(t.toBTableSchema)))

  override def getDatabases(
    request: BGetDatabases.BGetDatabasesReq
  ): ZIO[Any, Status, BGetDatabases.BGetDatabasesResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.getDatabases(new SessionHandle(request.getSessionHandle), request.pattern)
      )
      .mapBoth(errorApplyFunc, t => BGetDatabasesResp(Option(t.toBOperationHandle())))

  override def getTables(request: BGetTablesReq): ZIO[Any, Status, BGetTablesResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.getTables(new SessionHandle(request.getSessionHandle), request.database, request.pattern)
      )
      .mapBoth(errorApplyFunc, t => BGetTablesResp(Option(t.toBOperationHandle())))

  override def getLeader(request: BGetLeaderReq): ZIO[Any, Status, BGetLeaderResp] = {
    val leaderAddress = BitlapServerContext.getLeaderAddress()
    leaderAddress.flatMap { ld =>
      if (ld == null || ld.port <= 0 || ld.ip == null || ld.ip.isEmpty) {
        Task.fail(LeaderNotFoundException(s"requestId: ${request.requestId}"))
      } else {
        Task.succeed(ld)
      }
    }.mapBoth(
      errorApplyFunc,
      t => BGetLeaderResp(Option(t.ip), t.port)
    )
  }

  override def cancelOperation(
    request: BCancelOperationReq
  ): ZIO[Any, Status, BCancelOperationResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.cancelOperation(new OperationHandle(request.getOperationHandle))
      )
      .mapBoth(errorApplyFunc, _ => BCancelOperationResp())

  override def getOperationStatus(request: BGetOperationStatusReq): ZIO[Any, Status, BGetOperationStatusResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.getOperationStatus(new OperationHandle(request.getOperationHandle))
      )
      .mapBoth(errorApplyFunc, t => t.toBGetOperationStatusResp)

  override def closeOperation(request: BCloseOperation.BCloseOperationReq): ZIO[Any, Status, BCloseOperationResp] =
    asyncRpcBackend
      .when(
        BitlapServerContext.isLeader,
        OperationMustOnLeaderException(),
        _.closeOperation(new OperationHandle(request.getOperationHandle))
      )
      .mapBoth(errorApplyFunc, _ => BCloseOperationResp())
}
