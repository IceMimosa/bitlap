/**
 * Copyright (C) 2023 bitlap.org .
 */
package org.bitlap.core.sql.table

import org.apache.calcite.DataContext
import org.apache.calcite.linq4j.Enumerable
import org.apache.calcite.linq4j.Linq4j
import org.apache.calcite.rel.`type`.RelDataType
import org.apache.calcite.rel.`type`.RelDataTypeFactory
import org.apache.calcite.schema.ScannableTable
import org.apache.calcite.schema.impl.AbstractTable
import org.apache.calcite.sql.`type`.SqlTypeName

import scala.jdk.CollectionConverters._

/**
 * Desc: table for ddl operation
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/9/4
 */
class BitlapSqlDdlTable(
    val rowTypes: List[(String, SqlTypeName)],
    val operator: (DataContext) => List[Array[Any]]
) extends AbstractTable() with ScannableTable {

    override def getRowType(typeFactory: RelDataTypeFactory): RelDataType = {
      val builder = typeFactory.builder()
      this.rowTypes.foreach { rowType => builder.add(rowType._1, rowType._2) }
      builder.build()
    }

    override def scan(root: DataContext): Enumerable[Array[Any]] = {
        return Linq4j.asEnumerable(this.operator(root).asJava)
    }
}
