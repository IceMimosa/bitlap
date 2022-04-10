/* Copyright (c) 2022 bitlap.org */
package org.bitlap.jdbc

import org.apache.commons.lang.StringUtils
import java.io.PrintWriter
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Logger
import javax.sql.DataSource

/**
 *
 * @author 梦境迷离
 * @since 2021/6/12
 * @version 1.0
 */
class BitlapDataSource extends DataSource {

    override def unwrap[T](iface: Class[T]): T = ???

    override def isWrapperFor(iface: Class[_]): Boolean = ???

    override def getLogWriter: PrintWriter = ???

    override def setLogWriter(out: PrintWriter): Unit = ???

    override def setLoginTimeout(seconds: Int): Unit = ???

    override def getLoginTimeout: Int = ???

    override def getParentLogger: Logger = ???

    override def getConnection(): Connection = {
        return getConnection(StringUtils.EMPTY, StringUtils.EMPTY)
    }

    override def getConnection(username: String, password: String): Connection = {
        try {
            new BitlapConnection(StringUtils.EMPTY)
        } catch {
            case ex: java.lang.Exception => throw new SQLException("Error in getting BitlapConnection", ex)
        }
    }
}
