/* Copyright (c) 2023 bitlap.org */
package org.bitlap.client

import org.bitlap.network.enumeration.GetInfoType
import org.bitlap.network.handles._
import org.bitlap.network.models._

/** 供JDBC使用的同步客户端，本身无逻辑，全部都委托给异步客户端。但可以为其添加JDBC专属逻辑。
 *
 *  @author
 *    梦境迷离
 *  @since 2021/11/21
 *  @version 1.0
 */
final class BitlapClient(serverPeers: Array[String], props: Map[String, String]) {

  private lazy val syncClient: SyncClient = new SyncClient(serverPeers, props)

  def openSession(
    username: String = "",
    password: String = "",
    config: Map[String, String] = Map.empty
  ): SessionHandle =
    syncClient
      .openSession(username, password, config)

  def closeSession(sessionHandle: SessionHandle): Unit =
    syncClient.closeSession(sessionHandle)

  def executeStatement(
    sessionHandle: SessionHandle,
    statement: String,
    queryTimeout: Long,
    config: Map[String, String] = Map.empty
  ): OperationHandle =
    syncClient
      .executeStatement(
        statement = statement,
        sessionHandle = sessionHandle,
        queryTimeout = queryTimeout,
        confOverlay = config
      )

  def fetchResults(operationHandle: OperationHandle, maxRows: Int, fetchType: Int): RowSet =
    syncClient.fetchResults(operationHandle, maxRows, fetchType).results

  def getTables(
    sessionHandle: SessionHandle,
    database: String,
    pattern: String
  ): OperationHandle = syncClient
    .getTables(sessionHandle, database, pattern)

  def getDatabases(
    sessionHandle: SessionHandle,
    pattern: String
  ): OperationHandle = syncClient.getDatabases(sessionHandle, pattern)

  def getResultSetMetadata(operationHandle: OperationHandle): TableSchema =
    syncClient.getResultSetMetadata(operationHandle)

  def cancelOperation(opHandle: OperationHandle): Unit =
    syncClient.cancelOperation(opHandle)

  def closeOperation(opHandle: OperationHandle): Unit =
    syncClient.closeOperation(opHandle)

  def getOperationStatus(opHandle: OperationHandle): OperationStatus =
    syncClient.getOperationStatus(opHandle)

  def getInfo(sessionHandle: SessionHandle, getInfoType: GetInfoType): GetInfoValue =
    syncClient.getInfo(sessionHandle, getInfoType)
}
