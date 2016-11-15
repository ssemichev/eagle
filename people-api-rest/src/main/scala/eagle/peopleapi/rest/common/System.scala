package eagle.peopleapi.rest.common

import akka.actor.ActorSystem
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import eagle.peopleapi.rest.AppConfig
import eagle.peopleapi.rest.IoC.Global

import scala.concurrent.ExecutionContextExecutor

trait System extends BaseComponent {
  private[this] val serviceConfig = Global.getInstance[AppConfig].serviceConfig

  //TODO Move to AkkaModule
  //https://github.com/rocketraman/activator-akka-scala-guice
  implicit val system: ActorSystem = ActorSystem(serviceConfig.name)
  implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val logger: LoggingAdapter = Logging(system, serviceConfig.name)
}