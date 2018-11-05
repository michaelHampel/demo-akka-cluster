/*
 * Copyright 2018 mh
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

import akka.actor.CoordinatedShutdown.Reason
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.cluster.Cluster
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.actor.typed.{ ActorRef, Behavior, Terminated }
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import akka.stream.{ ActorMaterializer, Materializer }
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
import org.apache.logging.log4j.scala.Logging
import pureconfig.generic.auto._
import pureconfig.loadConfigOrThrow

object Main extends Logging {

  private case class TopLevelActorTerminated(actor: ActorRef[Nothing]) extends Reason

  def main(args: Array[String]): Unit = {
    sys.props += "log4j2.contextSelector" -> classOf[AsyncLoggerContextSelector].getName

    val config = loadConfigOrThrow[Config]("dac")

    val system  = ActorSystem("dac")
    val cluster = Cluster(system)

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    cluster.registerOnMemberUp(system.spawn[Nothing](Main(config, cluster), "main"))
    logger.info("System started and trying to join cluster")
  }

  def apply(config: Config, cluster: Cluster): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      import akka.actor.typed.scaladsl.adapter._

      implicit val mat: Materializer = ActorMaterializer()(context.system.toUntyped)

      val api = {
        import config.api._
        context.spawn(Api(address, port, cluster.selfAddress), Api.Name)
      }

      context.watch(api)
      logger.info("System up and running")

      Behaviors.receiveSignal[Nothing] {
        case (_, Terminated(actor)) =>
          logger.error(s"Shutting down, because actor ${actor.path} terminated!")
          CoordinatedShutdown(context.system.toUntyped).run(TopLevelActorTerminated(actor))
          Behaviors.empty
      }
    }
}
