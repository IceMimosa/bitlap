/* Copyright (c) 2022 bitlap.org */
package org.bitlap.core.sql

import org.apache.calcite.config.CalciteConnectionConfig
import org.apache.calcite.prepare.CalciteCatalogReader
import org.apache.calcite.rel.type.RelDataType
import org.apache.calcite.sql.SqlNode
import org.apache.calcite.sql.SqlOperatorTable
import org.apache.calcite.sql.SqlSelect
import org.apache.calcite.sql.validate.SqlValidator
import org.apache.calcite.sql.validate.SqlValidatorImpl
import org.bitlap.core.sql.udf.FunctionRegistry

/**
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/9/10
 */
class BitlapSqlValidator(
    val opTab: SqlOperatorTable,
    val catalogReader: CalciteCatalogReader,
    val config: SqlValidator.Config,
    val connConfig: CalciteConnectionConfig,
) : SqlValidatorImpl(opTab, catalogReader, catalogReader.typeFactory, config) {

    private val queryContext = QueryContext.get()

    override fun validate(topNode: SqlNode): SqlNode {
        return super.validate(topNode)
    }

    override fun validateSelect(select: SqlSelect, targetRowType: RelDataType) {
        queryContext.currentSelectNode = select
        FunctionRegistry.sqlValidatorFunctions().invoke().map { it.validate(select, targetRowType) }
        super.validateSelect(select, targetRowType)
    }
}
