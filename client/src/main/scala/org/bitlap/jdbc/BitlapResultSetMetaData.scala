/* Copyright (c) 2022 bitlap.org */
package org.bitlap.jdbc

import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Types

/**
 *
 * @author 梦境迷离
 * @since 2021/6/12
 * @version 1.0
 */
class BitlapResultSetMetaData(
    private val columnNames: List[String],
    private val columnTypes: List[String]
) extends ResultSetMetaData {

    override def getColumnCount(): Int = {
        return columnNames.size
    }

    override def isAutoIncrement(column: Int): Boolean = {
        return false
    }


    override def isCurrency(column: Int): Boolean = {
        return false
    }

    override def isNullable(column: Int): Int = {
        return ResultSetMetaData.columnNullable
    }


    override def getColumnDisplaySize(column: Int): Int = {
        // taking a stab at appropriate values
        return getColumnType(column) match {
            case Types.VARCHAR | Types.BIGINT => 32
            case Types.TINYINT => 2
            case Types.BOOLEAN => 8
            case Types.DOUBLE | Types.INTEGER => 16
            case _ => 32
        }
    }

    override def getColumnLabel(column: Int): String = {
        return columnNames(column - 1)
    }

    override def getColumnName(column: Int): String = {
        return columnNames(column - 1)
    }

    override def getSchemaName(column: Int): String = ???

    override def getPrecision(column: Int): Int = {
        return if (Types.DOUBLE == getColumnType(column)) -1 else 0 // Do we have a precision limit?
    }

    override def getScale(column: Int): Int = {
        return if (Types.DOUBLE == getColumnType(column)) -1 else 0 // Do we have a scale limit?
    }

    override def getTableName(column: Int): String = ???

    override def getCatalogName(column: Int): String = ???

    override def getColumnType(column: Int): Int = {
        if (columnTypes.isEmpty) throw new SQLException("Could not determine column type name for ResultSet")
        if (column < 1 || column > columnTypes.size) throw new SQLException("Invalid column value: $column")
        val typ = columnTypes(column - 1)
        return typ match {
            case "string" => Types.VARCHAR
            case "bool" => Types.BOOLEAN
            case "double" => Types.DOUBLE
            case "byte" => Types.TINYINT
            case "i32" => Types.INTEGER
            case "i64" => Types.BIGINT
            case _ => throw new SQLException("Inrecognized column type: $type")
        }
    }

    override def isCaseSensitive(column: Int): Boolean = ???

    override def isSearchable(column: Int): Boolean = ???

    override def isSigned(column: Int): Boolean = ???

    override def getColumnTypeName(column: Int): String = ???

    override def isReadOnly(column: Int): Boolean = ???

    override def isWritable(column: Int): Boolean = ???

    override def isDefinitelyWritable(column: Int): Boolean = ???

    override def getColumnClassName(column: Int): String = ???

    override def unwrap[T](iface: Class[T]): T = ???

    override def isWrapperFor(iface: Class[_]): Boolean = ???
}
