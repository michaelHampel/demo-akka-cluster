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

import akka.Done
import akka.actor.{ ActorSystem, Address, CoordinatedShutdown }
import akka.actor.typed.Behavior
import akka.actor.CoordinatedShutdown.PhaseServiceUnbind
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.Materializer
import org.apache.logging.log4j.scala.Logging

import scala.util.{ Failure, Success }

object Api extends Logging {

  sealed trait Command
  private final case object HandleBindFailure                  extends Command
  private final case class HandleBound(binding: ServerBinding) extends Command
  private final object Stop                                    extends Command

  final val Name = "api"

  def apply(address: String, port: Int, selfAddress: Address)(
      implicit mat: Materializer
  ): Behavior[Command] =
    Behaviors.setup { context =>
      import akka.actor.typed.scaladsl.adapter._

      implicit val system: ActorSystem = context.system.toUntyped
      implicit val executionContext    = context.executionContext
      val self                         = context.self

      Http()
        .bindAndHandle(route(selfAddress), address, port)
        .onComplete {
          case Failure(_)       => self ! HandleBindFailure
          case Success(binding) => self ! HandleBound(binding)
        }

      Behaviors.receivePartial {
        case (_, HandleBindFailure) =>
          logger.error(s"Stopping, because cannot bind to $address:$port!")
          Behaviors.stopped

        case (_, HandleBound(binding)) =>
          logger.info(s"Bound to ${binding.localAddress}")
          CoordinatedShutdown(system).addTask(PhaseServiceUnbind, "api-unbind") { () =>
            binding.unbind().map { _ =>
              self ! Stop
              Done
            }
          }
          Behaviors.receivePartial {
            case (_, Stop) => Behaviors.stopped
          }
      }
    }

  def route(selfAddress: Address): Route = {
    import Directives._
    path("self-address") {
      get {
        complete(selfAddress.toString)
      }
    } ~
    path("hello") {
      get {
        complete("Hello, from DAC Service...")
      }
    }
  }
}
