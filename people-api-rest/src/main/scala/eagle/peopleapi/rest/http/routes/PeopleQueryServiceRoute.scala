package eagle.peopleapi.rest.http.routes

import javax.inject.Inject

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import eagle.common.traits.Translator
import eagle.peopleapi.rest.common.BaseServiceRoute
import eagle.peopleapi.rest.datacontracts.DatatrustMatchRequest
import eagle.peopleapi.rest.datacontracts.DatatrustMatchResponse
import eagle.peopleapi.rest.http.formats.PeopleQueryFormat
import eagle.peopleapi.service.datatrust.PeopleQuery
import eagle.peopleapi.service.datatrust.models.Person
import eagle.peopleapi.service.datatrust.models.PersonCriteria

import scala.concurrent.ExecutionContext

class PeopleQueryServiceRoute @Inject() (
    personQueryService: PeopleQuery,
    requestTranslator:  Translator[DatatrustMatchRequest, PersonCriteria],
    responseTranslator: Translator[Person, DatatrustMatchResponse]
)(implicit ec: ExecutionContext) extends BaseServiceRoute with PeopleQueryFormat {

  val route =

    pathPrefix("people" / "datatrust") {
      pathPrefix("search") {
        post {
          entity(as[DatatrustMatchRequest]) { request ⇒
            doSearch(request)
          }
        }
      } ~
        get {
          parameters('first_name.?, 'last_name, 'zip.?) { (firstName, lastName, zip) ⇒
            val request = DatatrustMatchRequest(firstName, Some(lastName), zip)
            doSearch(request)
          }
        }
    }

  private[this] def doSearch(request: DatatrustMatchRequest): Route = {
    onSuccess(personQueryService.findBy(requestTranslator.translate(request))) {
      case persons if persons.nonEmpty ⇒ complete {
        responseTranslator.translate(persons.head)
      }
      case _ ⇒ complete(StatusCodes.NoContent)
    }
  }
}