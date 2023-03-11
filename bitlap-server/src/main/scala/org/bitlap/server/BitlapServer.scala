/* Copyright (c) 2023 bitlap.org */
package org.bitlap.server

import org.bitlap.server.config.{ BitlapGrpcConfig, BitlapHttpConfig, BitlapRaftConfig }
import org.bitlap.server.endpoint._
import zhttp.service.EventLoopGroup
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.console.putStrLn
import zio.magic._

/** bitlap 聚合服务
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/10/19
 */
object BitlapServer extends zio.App {

  // 在java 9以上运行时，需要JVM参数：--add-exports java.base/jdk.internal.ref=ALL-UNNAMED
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (for {
      t1 <- RaftServerEndpoint.service(args).fork
      t2 <- GrpcServerEndpoint.service(args).fork
      t3 <- HttpServerEndpoint.service(args).fork
      _ <- putStrLn("""
                      |    __    _ __  __
                      |   / /_  (_) /_/ /___ _____
                      |  / __ \/ / __/ / __ `/ __ \
                      | / /_/ / / /_/ / /_/ / /_/ /
                      |/_.___/_/\__/_/\__,_/ .___/
                      |                   /_/
                      |""".stripMargin)
      _ <- t1.join *> t2.join *> t3.join
    } yield ())
      .inject(
        RaftServerEndpoint.live,
        GrpcServerEndpoint.live,
        HttpServerEndpoint.live,
        zio.ZEnv.live,
        ServerChannelFactory.auto,
        EventLoopGroup.auto(16),
        BitlapGrpcConfig.live,
        BitlapHttpConfig.live,
        BitlapRaftConfig.live
      )
      .foldM(
        e => ZIO.fail(e).exitCode,
        _ => ZIO.effectTotal(ExitCode.success)
      )
}
