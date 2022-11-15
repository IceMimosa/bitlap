/* Copyright (c) 2022 bitlap.org */
package org.bitlap.server.session

import com.typesafe.scalalogging.LazyLogging
import org.bitlap.network.NetworkException.InternalException
import org.bitlap.network.handles.SessionHandle

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean
import scala.jdk.CollectionConverters.ConcurrentMapHasAsScala
import org.bitlap.jdbc.BitlapSQLException
import org.bitlap.network.OperationState
import org.bitlap.network.handles.OperationHandle
import org.bitlap.tools.apply

import scala.collection._

/** bitlap 会话管理器
 *  @author
 *    梦境迷离
 *  @since 2021/11/20
 *  @version 1.0
 */
@apply
final class SessionManager extends LazyLogging {

  val start = new AtomicBoolean(false)
  val operationStore: mutable.HashMap[OperationHandle, Operation] =
    mutable.HashMap[OperationHandle, Operation]()

  private lazy val sessionStore: ConcurrentHashMap[SessionHandle, Session] =
    new ConcurrentHashMap[SessionHandle, Session]()

  private lazy val sessionThread: Thread = new Thread { // register center
    override def run(): Unit =
      while (!Thread.currentThread().isInterrupted) {
        logger.info(s"[${sessionStore.size}] sessions exists")
        try {
          sessionStore.asScala.foreach { case (sessionHandle, session) =>
            if (!session.sessionState.get()) {
              sessionStore.remove(sessionHandle)
              logger.info(
                s"Session state is false, remove session [$sessionHandle]"
              )
            }

            val now = System.currentTimeMillis()
            if (session.lastAccessTime + 20 * 60 * 1000 < now) {
              sessionStore.remove(sessionHandle)
              logger.info(
                s"Session has not been visited for 20 minutes, remove session [$sessionHandle]"
              )
            } else {
              logger.info(s"SessionId [${sessionHandle.handleId}]")
            }
          }

          TimeUnit.SECONDS.sleep(3)
        } catch {
          case e: Exception =>
            logger.error(
              s"Failed to listen for session, error: ${e.getLocalizedMessage}",
              e
            )
        }
      }
  }

  def startListener(): Unit =
    if (start.compareAndSet(false, true)) {
      sessionThread.setDaemon(true)
      sessionThread.start()
    }

  def openSession(
    username: String,
    password: String,
    sessionConf: Map[String, String]
  ): Session =
    SessionManager.sessionAddLock.synchronized {
      val session = new MemorySession(
        username,
        password,
        sessionConf,
        this
      )
      session.open()
      sessionStore.put(session.sessionHandle, session)
      logger.info(s"Create session [${session.sessionHandle}]")
      return session
    }

  def closeSession(sessionHandle: SessionHandle): Unit = {
    SessionManager.sessionAddLock.synchronized {
      sessionStore.remove(sessionHandle)
    }
    logger.info(
      s"Close session [$sessionHandle], [${getOpenSessionCount()}] sessions exists"
    )
  }

  private def getOpenSessionCount(): Int =
    sessionStore.size

  def getSession(sessionHandle: SessionHandle): Session = {
    val session: Session = SessionManager.sessionAddLock.synchronized {
      sessionStore.get(sessionHandle)
    }
    if (session == null) {
      throw InternalException(s"Invalid SessionHandle: $sessionHandle")
    }
    session
  }

  def refreshSession(sessionHandle: SessionHandle, session: Session): Session =
    SessionManager.sessionAddLock.synchronized {
      session.lastAccessTime = System.currentTimeMillis()
      if (sessionStore.containsKey(sessionHandle)) {
        sessionStore.put(sessionHandle, session)
      } else {
        throw InternalException(s"Invalid SessionHandle: $sessionHandle")
      }
    }

  def addOperation(operation: Operation) {
    this.synchronized {
      operationStore.put(operation.opHandle, operation)
    }
  }

  def getOperation(operationHandle: OperationHandle): Operation =
    this.synchronized {
      val op = operationStore.getOrElse(operationHandle, null)
      if (op == null) {
        throw BitlapSQLException(s"Invalid OperationHandle: $operationHandle")
      } else {
        op.getState match {
          case OperationState.FinishedState => op
          case _ =>
            throw BitlapSQLException(s"Invalid OperationState: ${op.getState}")
        }
      }
    }
}
object SessionManager {
  private val sessionAddLock: Object = new Object
}
