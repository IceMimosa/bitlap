/* Copyright (c) 2022 bitlap.org */
package org.bitlap.network.rpc

import org.bitlap.network.handles.{ OperationHandle, SessionHandle }
import org.bitlap.network.models.{ FetchResults, TableSchema }

/**
 * @author 梦境迷离
 * @version 1.0,2022/4/21
 */
trait RpcF[F[_]] {

  def openSession(
    username: String,
    password: String,
    configuration: Map[String, String]
  ): F[SessionHandle]

  def closeSession(sessionHandle: SessionHandle): F[Unit]

  def executeStatement(
    sessionHandle: SessionHandle,
    statement: String,
    queryTimeout: Long,
    confOverlay: Map[String, String]
  ): F[OperationHandle]

  def fetchResults(opHandle: OperationHandle): F[FetchResults]

  def getResultSetMetadata(opHandle: OperationHandle): F[TableSchema]

  def getColumns(
    sessionHandle: SessionHandle,
    schemaName: String = null,
    tableName: String = null,
    columnName: String = null
  ): F[OperationHandle]

  /**
   * get databases or schemas from catalog
   *
   * @see `show databases`
   */
  def getDatabases(pattern: String): F[OperationHandle]

  /**
   * get tables from catalog with database name
   *
   * @see `show tables in [db_name]`
   */
  def getTables(database: String, pattern: String): F[OperationHandle]

  /**
   * get schemas from catalog
   *
   * @see `show tables in [db_name]`
   */
  def getSchemas(sessionHandle: SessionHandle, catalogName: String, schemaName: String): F[OperationHandle]
}
