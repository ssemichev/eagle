package eagle.peopleapi.rest.http

import akka.event.NoLogging
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.model.headers.`WWW-Authenticate`
import akka.http.scaladsl.server.Route
import eagle.peopleapi.rest.PeopleQueryEndpoint.authenticateBasic
import eagle.peopleapi.rest.common.HttpServiceBaseSpec
import eagle.peopleapi.rest.common.authentication.UserPassAuthenticator
import eagle.peopleapi.rest.http.formats.PeopleQueryFormat
import eagle.peopleapi.rest.http.routes.PeopleQueryServiceRoute
import eagle.peopleapi.rest.translators.DatatrustMatchRequestToPersonCriteria
import eagle.peopleapi.rest.translators.PersonToPersonDatatrustResponse
import eagle.peopleapi.service.datatrust.PeopleQuery

import scala.concurrent.Future

class HttpServiceAuthenticationSpec extends HttpServiceBaseSpec with PeopleQueryFormat {

  behavior of "HttpService Authentication"

  trait Context {
    implicit val logger = NoLogging

    val personQueryMock = mock[PeopleQuery]

    val peopleQueryServiceRoute = new PeopleQueryServiceRoute(
      personQueryMock,
      new DatatrustMatchRequestToPersonCriteria(),
      new PersonToPersonDatatrustResponse()
    )

    val peopleQueryHttpService = new PeopleQueryHttpService(peopleQueryServiceRoute)

    val authenticatorMock = mock[UserPassAuthenticator[String]]

    val credentials = BasicHttpCredentials("search-user", "search-user-password")

    def routes: Route = authenticateBasic(realm = "Secure search endpoint", authenticatorMock.authenticate) {
      userName ⇒ peopleQueryHttpService.routes
    }

    val peopleEndpoint = "/people/datatrust"
    val peopleSearchEndpoint = s"$peopleEndpoint/search"

    val requestEntity = HttpEntity(ContentTypes.`application/json`, s"""{"last_name": "LastName"}""")
  }

  it should "pass requests with valid credentials for POST requests" in new Context {
    (personQueryMock.findBy _).expects(*).returns(Future.successful(Array.empty))
    (authenticatorMock.authenticate _).expects().returns(credentials ⇒ Some("search-user"))

    Post(peopleSearchEndpoint, requestEntity) ~> routes ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }

  it should "pass requests with valid credentials for GET requests" in new Context {
    (personQueryMock.findBy _).expects(*).returns(Future.successful(Array.empty))
    (authenticatorMock.authenticate _).expects().returns(credentials ⇒ Some("search-user"))

    Get(s"$peopleEndpoint/?last_name=Last+NAME") ~> routes ~> check {
      status shouldBe StatusCodes.NoContent
    }
  }

  it should "reject requests with invalid credentials for POST requests" in new Context {
    (personQueryMock.findBy _).expects(*).never
    (authenticatorMock.authenticate _).expects().returns(credentials ⇒ None)

    Post(peopleSearchEndpoint, requestEntity) ~> addCredentials(credentials) ~> Route.seal(routes) ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual "The supplied authentication is invalid"
      header[`WWW-Authenticate`].get.challenges.headOption.value shouldEqual HttpChallenge("Basic", Some("Secure search endpoint"))
    }
  }

  it should "reject requests with invalid credentials for GET requests" in new Context {
    (personQueryMock.findBy _).expects(*).never
    (authenticatorMock.authenticate _).expects().returns(credentials ⇒ None)

    Get(s"$peopleEndpoint/?last_name=Last+NAME") ~> addCredentials(credentials) ~> Route.seal(routes) ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual "The supplied authentication is invalid"
      header[`WWW-Authenticate`].get.challenges.headOption.value shouldEqual HttpChallenge("Basic", Some("Secure search endpoint"))
    }
  }

  it should "reject requests without credentials" in new Context {
    (personQueryMock.findBy _).expects(*).never
    (authenticatorMock.authenticate _).expects().returns(credentials ⇒ None).twice()

    Post(peopleSearchEndpoint, requestEntity) ~> Route.seal(routes) ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual "The resource requires authentication, which was not supplied with the request"
      header[`WWW-Authenticate`].get.challenges.headOption.value shouldEqual HttpChallenge("Basic", Some("Secure search endpoint"))
    }

    Get(s"$peopleEndpoint/?last_name=Last+NAME") ~> Route.seal(routes) ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual "The resource requires authentication, which was not supplied with the request"
      header[`WWW-Authenticate`].get.challenges.headOption.value shouldEqual HttpChallenge("Basic", Some("Secure search endpoint"))
    }
  }
}
