package eagle.peopleapi.rest.common.authentication

import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.Provided
import akka.http.scaladsl.server.directives.SecurityDirectives
import com.google.inject.Inject
import eagle.peopleapi.rest.AppConfig

class ConfigUserPassAuthenticator @Inject() (val config: AppConfig) extends UserPassAuthenticator[String] {

  override def authenticate: SecurityDirectives.Authenticator[String] = {
    case provided @ Credentials.Provided(userName) if isValid(provided, userName) ⇒ Some(userName)
    case _ ⇒
      // If wrong credentials were given, then this route is not completed before 1 second has passed.
      // This makes timing attacks harder.
      val delay = 1000L
      Thread.sleep(delay)
      None
  }

  private[this] def isValid(provided: Provided, id: String): Boolean = {
    provided.verify(config.httpConfig.searchUserPassword) && id == config.httpConfig.searchUserName
  }
}