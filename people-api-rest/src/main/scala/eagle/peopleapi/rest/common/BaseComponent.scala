package eagle.peopleapi.rest.common

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

trait BaseComponent {
  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def executor: ExecutionContextExecutor
  implicit def logger: LoggingAdapter
}
