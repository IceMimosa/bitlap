/*
 * Copyright 2020-2023 IceMimosa, jxnu-liguobin and the Bitlap Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitlap.testkit.server

import org.bitlap.server.*
import org.bitlap.server.config.*
import org.bitlap.server.service.DriverGrpcService

import zio.*
import zio.ZIOAppArgs.getArgs

/** Bitlap embedded services include HTTP, GRPC, and Raft
 */

object EmbedBitlapServer extends zio.ZIOAppDefault {

  override def run =
    (for {
      args <- getArgs
      t1   <- RaftServerEndpoint.service(args.toList).fork
      t2   <- GrpcServerEndpoint.service(args.toList).fork
      _ <- Console.printLine("""
                        |    __    _ __  __
                        |   / /_  (_) /_/ /___ _____
                        |  / __ \/ / __/ / __ `/ __ \
                        | / /_/ / / /_/ / /_/ / /_/ /
                        |/_.___/_/\__/_/\__,_/ .___/
                        |                   /_/
                        |""".stripMargin)
      _ <- ZIO.collectAll(Seq(t1.join, t2.join))
    } yield ())
      .provide(
        RaftServerEndpoint.live,
        GrpcServerEndpoint.live,
        Scope.default,
        MockDriverIO.live,
        ZIOAppArgs.empty,
        DriverGrpcService.live,
        BitlapServerConfiguration.testLive
      )
      .fold(
        e => ZIO.fail(e).exitCode,
        _ => ZIO.succeed(ExitCode.success)
      )
}
