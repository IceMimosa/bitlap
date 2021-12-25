package org.bitlap.core.sql.udf

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Boolean
import org.apache.calcite.schema.impl.AggregateFunctionImpl
import org.apache.calcite.schema.impl.ScalarFunctionImpl
import org.apache.calcite.sql.SqlFunction
import org.apache.calcite.sql.SqlIdentifier
import org.apache.calcite.sql.SqlKind
import org.apache.calcite.sql.parser.SqlParserPos
import org.apache.calcite.sql.type.InferTypes
import org.apache.calcite.sql.type.OperandTypes
import org.apache.calcite.sql.type.ReturnTypes
import org.apache.calcite.sql.type.SqlTypeFamily
import org.apache.calcite.sql.type.SqlTypeName
import org.apache.calcite.sql.validate.SqlUserDefinedAggFunction
import org.apache.calcite.sql.validate.SqlUserDefinedFunction
import org.apache.calcite.util.Optionality
import org.bitlap.common.utils.PreConditions
import java.util.concurrent.ConcurrentHashMap

/**
 * manage functions.
 */
object FunctionRegistry {

    private val functions: ConcurrentHashMap<String, SqlFunction> = ConcurrentHashMap()
    init {
        register(
            UdafBMSum(),
            UdafBMCountDistinct(),
        )
        register("if", "condition")
    }

    fun register(vararg func: UDAF<*, *, *>): FunctionRegistry {
        func.forEach { register(it) }
        return this
    }
    fun register(func: UDAF<*, *, *>): FunctionRegistry {
        val name = PreConditions.checkNotBlank(func.name).trim()
        if (functions.containsKey(name)) {
            throw IllegalArgumentException("$name function already exists.")
        }
        functions[name] = SqlUserDefinedAggFunction(
            SqlIdentifier(func.name, SqlParserPos.ZERO),
            SqlKind.OTHER_FUNCTION,
            ReturnTypes.explicit(func.resultType),
            InferTypes.FIRST_KNOWN,
            OperandTypes.operandMetadata(
                listOf(SqlTypeFamily.ANY),
                { t -> func.inputTypes.map { t.createSqlType(it) } },
                { i -> "$i" },
                { true }
            ),
            AggregateFunctionImpl.create(func::class.java),
            false, false, Optionality.FORBIDDEN
        )
        return this
    }

    fun register(name: String, func: SqlFunction): FunctionRegistry {
        val cleanName = PreConditions.checkNotBlank(func.name).trim()
        if (functions.containsKey(cleanName)) {
            throw IllegalArgumentException("$cleanName function already exists.")
        }
        functions[cleanName] = func
        return this
    }

    private fun register(name: String, methodName: String = name): FunctionRegistry {
        val func = ScalarFunctionImpl.create(UDFs::class.java, methodName)
        if (func != null) {
            functions[name] = SqlUserDefinedFunction(
                SqlIdentifier(name, SqlParserPos.ZERO),
                SqlKind.OTHER_FUNCTION,
                ReturnTypes.ARG1_NULLABLE,
                InferTypes.FIRST_KNOWN,
                OperandTypes.operandMetadata(
                    listOf(SqlTypeFamily.BOOLEAN, SqlTypeFamily.ANY, SqlTypeFamily.ANY),
                    { t -> listOf(t.createSqlType(SqlTypeName.BOOLEAN), t.createSqlType(SqlTypeName.ANY), t.createSqlType(SqlTypeName.ANY)) },
                    { i -> "$i" },
                    { true }
                ),
                func
            )
        }
        return this
    }

    fun sqlFunctions() = this.functions.values
    fun getFunction(name: String) = this.functions[name]
    fun contains(name: String) = this.functions.containsKey(name)
}
