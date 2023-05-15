package org.bitlap.spark

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.types._
import org.bitlap.common.data.Event

import scala.jdk.CollectionConverters.ListHasAsScala

/**
 * spark utils
 */
object SparkUtils {

  /**
   * get spark valid input schema
   */
  def validSchema: StructType = {
    val fields = Event.getSchema.asScala.map { p =>
      val typ = p.getSecond match {
        case "int" | "integer" => IntegerType
        case "long" | "bigint" => LongType
        case "double"          => DoubleType
        case "string"          => StringType
        case _                 => throw new IllegalArgumentException("Unknown type: " + p.getSecond)
      }
      StructField(p.getFirst, typ)
    }
    StructType(fields.toList)
  }


  /**
   * get hive table
   */
  def getHiveTableIdentifier(tableName: String): TableIdentifier = {
    tableName.split("\\.") match {
      case Array(name) => new TableIdentifier(name, None)
      case Array(db, name) => new TableIdentifier(name, Some(db))
      case _ => throw new IllegalArgumentException(s"Illegal targetTableName=[$tableName]")
    }
  }


  /**
   * make dataframe as a sql view to execute with `func`
   */
  def withTempView[T](dfs: (DataFrame, String)*)(func: => T): T = {
    try {
      dfs.foreach {
        case (df, viewName) =>
          df.createOrReplaceTempView(viewName)
      }
      func
    } finally {
      dfs.foreach {
        case (df, viewName) =>
          df.sparkSession.catalog.dropTempView(viewName)
      }
    }
  }
}
