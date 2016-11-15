package eagle.peopleapi.rest

import akka.http.scaladsl.server.Route
import com.google.inject.name.Names
import eagle.peopleapi.rest.IoC.Global
import eagle.peopleapi.rest.common.Endpoint
import eagle.peopleapi.rest.common.System
import eagle.peopleapi.rest.common.authentication.UserPassAuthenticator
import eagle.peopleapi.rest.http.PeopleQueryHttpService

object PeopleQueryEndpoint extends App with Endpoint with System {

  override protected val config: AppConfig = Global.getInstance[AppConfig]

  private[this] val authenticator = Global.getInstance[UserPassAuthenticator[String]](Names.named("searchEndpoint"))

  override protected val serviceRoutes: Route = {
    val peopleQueryHttpService = Global.getInstance[PeopleQueryHttpService]

    authenticateBasic(realm = "Secure search endpoint", authenticator.authenticate) { userName ⇒
      peopleQueryHttpService.routes
    }
  }

  publish()
}