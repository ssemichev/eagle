package eagle.peopleapi.rest.common

import java.lang.management.ManagementFactory

import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Route
import eagle.common.BuildInfo
import eagle.peopleapi.rest.common.json.format.EndpointFormat
import eagle.peopleapi.rest.common.models.About
import eagle.peopleapi.rest.common.models.Status

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success

trait Endpoint extends BaseComponent with BaseServiceRoute with EndpointFormat with CustomLogging {

  protected def config: BaseConfig

  private[this] lazy val serviceName = config.serviceConfig.name
  private[this] lazy val serviceVersion = config.serviceConfig.version
  private[this] lazy val httpConfig = config.httpConfig

  protected val defaultRoutes: Route = {

    pathPrefix("status") {
      pathEndOrSingleSlash {
        get {
          val uptime = Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString()
          complete {
            Status(serviceName, uptime)
          }
        }
      }
    } ~
      pathPrefix("about") {
        pathEndOrSingleSlash {
          get {
            complete {
              About(serviceName, DateTime.now, BuildInfo.version, BuildInfo.builtAtString)
            }
          }
        }
      } ~
      pathPrefix("health") {
        pathEndOrSingleSlash {
          get {
            complete {
              HttpEntity(ContentTypes.`application/json`, "{\"ok\":true}")
            }
          }
        }
      }
  }

  protected def routes: Route = pathPrefix(serviceVersion) {
    logRequestResult(serviceName, Logging.DebugLevel) { defaultRoutes } ~
      logRequestResult(serviceName, Logging.InfoLevel) { serviceRoutes }
  } ~ path("")(getFromResource("public/index.html"))

  def publish(): Unit = {

    val (host, port) = (httpConfig.interface, httpConfig.port)

    val loggedRoute = requestMethodAndResponseStatusAsInfo(Logging.InfoLevel, routes)
    val serverBinding = Http().bindAndHandle(loggedRoute, host, port)

    scala.sys.addShutdownHook {
      serverBinding.flatMap(_.unbind())
      system.terminate()
      logger.info(s"${system.name} has been terminated")
      Await.result(system.whenTerminated, 1 minute)
    }

    serverBinding.onComplete {
      case Success(_)  ⇒ logger.info("Has been running on {}:{}...", host, port)
      case Failure(ex) ⇒ logger.error(ex, "Failed to bind to {}:{}!", host, port)
    }
  }

  protected def serviceRoutes: Route
}

