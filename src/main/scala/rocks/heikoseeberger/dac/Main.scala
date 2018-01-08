/*
 * Copyright 2018 Heiko Seeberger
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

package rocks.heikoseeberger.dac

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
import org.apache.logging.log4j.scala.Logging

object Main extends Logging {

  def main(args: Array[String]): Unit = {
    sys.props += "log4j2.contextSelector" -> classOf[AsyncLoggerContextSelector].getName

    val system  = ActorSystem("dac")
    val cluster = Cluster(system)

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    cluster.registerOnMemberUp(system.spawn[Nothing](Main(), "main"))
    logger.info("System started and trying to join cluster")
  }

  def apply(): Behavior[Nothing] =
    Actor.deferred[Nothing] { context =>
      logger.info("System up and running")
      Actor.empty[Nothing]
    }
}
