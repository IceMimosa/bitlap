/* Copyright (c) 2023 bitlap.org */
package org.bitlap.cli.interactive

import org.bitlap.common.utils.StringEx
import sqlline._
import scala.collection.mutable

final class BitlapSqlPromptHandler(val line: SqlLine, val prompt: String) extends PromptHandler(line) {

  override def getDefaultPrompt(
    connectionIndex: Int,
    url: String,
    defaultPrompt: String
  ): String = {
    val sb     = new mutable.StringBuilder(this.prompt)
    val schema = sqlLine.getConnectionMetadata.getCurrentSchema
    if (!StringEx.nullOrBlank(schema)) {
      sb.append(s" ($schema)")
    }
    sb.append("> ").toString
  }
}
