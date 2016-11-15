package eagle.peopleapi.rest.common

import java.net.InetAddress

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import eagle.common.config.ConfigFactoryExt

import scala.util.Try

trait BaseConfig {

  def config: Option[Config] = Some(ConfigFactory.load)

  def appRootSectionName: String

  val localAddress = InetAddress.getLocalHost.getHostAddress

  val rootConfig = {
    ConfigFactoryExt.enableEnvOverride()
    config match {
      case Some(c) ⇒ c.withFallback(ConfigFactory.load())
      case _       ⇒ ConfigFactory.load
    }
  }

  protected val application = rootConfig.getConfig(appRootSectionName)

  val AppName = application.getString("name")
  val AppVersion = application.getString("version")

  val httpConfig: HttpConfig = {
    HttpConfig(
      rootConfig.getString("http.interface"),
      rootConfig.getInt("http.port"),
      rootConfig.getString("http.search-user-name"),
      rootConfig.getString("http.search-user-password")
    )
  }
  val serviceConfig: ServiceConfig = {
    ServiceConfig(AppName, AppVersion)
  }

  /** Attempts to acquire from environment, then java system properties. */
  def withFallback[T](env: Try[T]): Option[T] = env match {
    case value if Option(value).isEmpty ⇒ None
    case _                              ⇒ env.toOption
  }

  protected case class HttpConfig(interface: String, port: Int, searchUserName: String, searchUserPassword: String) {
    require(!searchUserName.isEmpty, "http.search-user-name")
    require(!searchUserPassword.isEmpty, "http.search-user-password")
  }

  protected case class ServiceConfig(name: String, version: String)

}
