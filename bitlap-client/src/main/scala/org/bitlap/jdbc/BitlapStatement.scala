/* Copyright (c) 2022 bitlap.org */
package org.bitlap.jdbc

import org.bitlap.jdbc.client.BitlapClient
import org.bitlap.network.driver.proto.{ BOperationHandle, BSessionHandle }

import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLWarning
import java.sql.Statement

/**
 * Bitlap Statement
 *
 * @author 梦境迷离
 * @since 2021/6/6
 * @version 1.0
 */
class BitlapStatement(
  private val sessHandle: BSessionHandle,
  private val client: BitlapClient
) extends Statement {

  private var stmtHandle: BOperationHandle = _
  private val fetchSize = 50

  /**
   * We need to keep a reference to the result set to support the following:
   *
   * statement.execute(String sql);
   * statement.getResultSet();
   */
  private var resultSet: ResultSet = _

  /**
   * The maximum number of rows this statement should return (0 => all rows)
   */
  private var maxRows = 0

  /**
   * Add SQLWarnings to the warningChain if needed
   */
  private var warningChain: SQLWarning = _

  /**
   * Keep state so we can fail certain calls made after close();
   */
  private var closed = false

  override def unwrap[T](iface: Class[T]): T = ???

  override def isWrapperFor(iface: Class[_]): Boolean = ???

  override def close() {
    // TODO: how to properly shut down the client?
    resultSet = null
    closed = true
  }

  override def executeQuery(sql: String): ResultSet = {
    if (!execute(sql)) {
      throw new SQLException("The query did not generate a result set!")
    }
    resultSet
  }

  override def executeUpdate(sql: String): Int = ???

  override def getMaxFieldSize: Int = ???

  override def setMaxFieldSize(max: Int): Unit = ???

  override def getMaxRows: Int = ???

  override def setMaxRows(max: Int): Unit = ???

  override def setEscapeProcessing(enable: Boolean): Unit = ???

  override def getQueryTimeout: Int = ???

  override def setQueryTimeout(seconds: Int): Unit = ???

  override def cancel(): Unit = ???

  override def getWarnings: SQLWarning = warningChain

  override def executeUpdate(sql: String, autoGeneratedKeys: Int): Int = ???

  override def executeUpdate(sql: String, columnIndexes: Array[Int]): Int = ???

  override def executeUpdate(sql: String, columnNames: Array[String]): Int = ???

  override def clearWarnings() = warningChain = null

  override def setCursorName(name: String) = ???

  override def execute(sql: String): Boolean = {
    if (closed) throw new SQLException("Can't execute after statement has been closed")
    try {
      resultSet = null
      stmtHandle = client.executeStatement(sessHandle, sql)
      if (stmtHandle == null || !stmtHandle.hasResultSet) {
        return false
      }
    } catch {
      case ex: Exception => throw new SQLException(ex.toString, ex)
    }
    resultSet = BitlapQueryResultSet
      .builder()
      .setClient(client)
      .setSessionHandle(sessHandle)
      .setStmtHandle(stmtHandle)
      .setMaxRows(maxRows)
      .setFetchSize(fetchSize)
      .build()
    true
  }

  override def execute(sql: String, autoGeneratedKeys: Int): Boolean = ???

  override def execute(sql: String, columnIndexes: Array[Int]): Boolean = ???

  override def execute(sql: String, columnNames: Array[String]): Boolean = ???

  override def getResultSet(): ResultSet = resultSet

  override def getUpdateCount(): Int = ???

  override def getMoreResults(): Boolean = ???

  override def getMoreResults(current: Int): Boolean = ???

  override def setFetchDirection(direction: Int): Unit = ???

  override def getFetchDirection(): Int = ???

  override def setFetchSize(rows: Int): Unit = ???

  override def getFetchSize(): Int = fetchSize

  override def getResultSetConcurrency(): Int = ???

  override def getResultSetType(): Int = ???

  override def addBatch(sql: String): Unit = ???

  override def clearBatch(): Unit = ???

  override def executeBatch(): Array[Int] = ???

  override def getConnection(): Connection = ???

  override def getGeneratedKeys(): ResultSet = ???

  override def getResultSetHoldability(): Int = ???

  override def isClosed(): Boolean = closed

  override def setPoolable(poolable: Boolean): Unit = ???

  override def isPoolable(): Boolean = ???

  override def closeOnCompletion(): Unit = ???

  override def isCloseOnCompletion(): Boolean = ???
}
