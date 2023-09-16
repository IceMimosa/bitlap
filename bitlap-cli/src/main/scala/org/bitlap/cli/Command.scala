/**
 * Copyright (C) 2023 bitlap.org .
 */
package org.bitlap.cli

/** Bitmap command line supported commands.
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/4/23
 */
sealed trait Command extends Product with Serializable

object Command {

  final case class Sql(
    server: String,
    user: String,
    password: String,
    args: List[String])
      extends Command

  final case class Server(operate: String) extends Command

}
