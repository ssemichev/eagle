package eagle.peopleapi.rest.common

import akka.http.scaladsl.server.Route

trait HttpService {

  def routes: Route
}
