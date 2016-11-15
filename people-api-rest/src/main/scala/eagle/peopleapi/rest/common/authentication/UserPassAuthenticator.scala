package eagle.peopleapi.rest.common.authentication

import akka.http.scaladsl.server.directives.SecurityDirectives.Authenticator

trait UserPassAuthenticator[T] {

  def authenticate: Authenticator[T]
}
