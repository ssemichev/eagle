package eagle.peopleapi.rest.http

import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import eagle.peopleapi.rest.common.HttpService
import eagle.peopleapi.rest.http.routes.PeopleQueryServiceRoute

class PeopleQueryHttpService @Inject() (val peopleRouter: PeopleQueryServiceRoute) extends HttpService {

  val routes: Route = peopleRouter.route
}