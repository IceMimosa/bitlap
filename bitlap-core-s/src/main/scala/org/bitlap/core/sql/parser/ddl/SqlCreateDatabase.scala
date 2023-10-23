/**
 * Copyright (C) 2023 bitlap.org .
 */
package org.bitlap.core.sql.parser.ddl

import org.apache.calcite.DataContext
import org.apache.calcite.sql.SqlIdentifier
import org.apache.calcite.sql.SqlKind
import org.apache.calcite.sql.SqlLiteral
import org.apache.calcite.sql.SqlSpecialOperator
import org.apache.calcite.sql.SqlWriter
import org.apache.calcite.sql.parser.SqlParserPos
import org.apache.calcite.sql.`type`.SqlTypeName
import org.bitlap.core.sql.parser.BitlapSqlDdlCreateNode

/**
 * Desc:
 *   Parse tree for `CREATE (DATABASE | SCHEMA) IF NOT EXISTS database_name` statement.
 *
 * see [org.apache.calcite.sql.ddl.SqlCreateSchema], you can also use [org.apache.calcite.sql.ddl.SqlDdlNodes]
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/8/23
 */
class SqlCreateDatabase(
    override val _pos: SqlParserPos,
    val name: SqlIdentifier,
    override val _ifNotExists: Boolean,
    override val _replace: Boolean = false
) extends BitlapSqlDdlCreateNode(
    _pos, SqlCreateDatabase.OPERATOR,
    List(SqlLiteral.createBoolean(_replace, _pos), SqlLiteral.createBoolean(_ifNotExists, _pos), name),
    _replace, _ifNotExists
) {

    override def unparse(writer: SqlWriter, leftPrec: Int, rightPrec: Int) = {
        writer.keyword("CREATE DATABASE")
        if (ifNotExists) {
            writer.keyword("IF NOT EXISTS")
        }
        name.unparse(writer, leftPrec, rightPrec)
    }

    override val resultTypes: List[(String, SqlTypeName)]
        = List(
            "result" -> SqlTypeName.BOOLEAN
        )

    override def operator(context: DataContext): List[Array[Any]] = {
        return List(
            Array(catalog.createDatabase(name.getSimple, ifNotExists))
        )
    }
}

object SqlCreateDatabase {
  val OPERATOR = SqlSpecialOperator("CREATE DATABASE", SqlKind.CREATE_SCHEMA)
}
