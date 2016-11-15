package eagle.peopleapi.rest.http

import akka.event.NoLogging
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.Route
import eagle.peopleapi.rest.common.HttpServiceBaseSpec
import eagle.peopleapi.rest.datacontracts.DatatrustMatchResponse
import eagle.peopleapi.rest.http.formats.PeopleQueryFormat
import eagle.peopleapi.rest.http.routes.PeopleQueryServiceRoute
import eagle.peopleapi.rest.translators.DatatrustMatchRequestToPersonCriteria
import eagle.peopleapi.rest.translators.PersonToPersonDatatrustResponse
import eagle.peopleapi.service.datatrust.PeopleQuery
import eagle.peopleapi.service.datatrust.models.Name
import eagle.peopleapi.service.datatrust.models.Person

import scala.concurrent.Future

class PeopleQueryHttpServiceSpec extends HttpServiceBaseSpec with PeopleQueryFormat {

  behavior of "HttpService People endpoint"

  trait Context {
    implicit val logger = NoLogging

    val personQueryMock = mock[PeopleQuery]
    val peopleQueryServiceRoute = new PeopleQueryServiceRoute(
      personQueryMock,
      new DatatrustMatchRequestToPersonCriteria(),
      new PersonToPersonDatatrustResponse()
    )
    val peopleQueryHttpService = new PeopleQueryHttpService(peopleQueryServiceRoute)
    val routes = peopleQueryHttpService.routes
    val peopleEndpoint = "/people/datatrust"
    val peopleSearchEndpoint = s"$peopleEndpoint/search"

    val lastName = "LastName"
    val persons = Array(Person(name = Some(Name(lastName = Some(lastName)))))
  }

  it should "return No Content response if there isn't any person found" in new Context {

    (personQueryMock.findBy _).expects(*).returns(Future.successful(Array.empty))

    val requestEntity = HttpEntity(ContentTypes.`application/json`, s"""{"last_name": "LastName"}""")

    Post(peopleSearchEndpoint, requestEntity) ~> routes ~> check {
      status shouldBe StatusCodes.NoContent
      contentType shouldBe ContentTypes.NoContentType
    }
  }

  it should "find the best match person by search criteria" in new Context {

    (personQueryMock.findBy _).expects(*).returns(Future.successful(persons))

    val requestEntity = HttpEntity(ContentTypes.`application/json`, s"""{"last_name": "$lastName"}""")

    Post(peopleSearchEndpoint, requestEntity) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      responseAs[DatatrustMatchResponse].name.value.lastName should not be empty
      responseAs[DatatrustMatchResponse].name.value.lastName.value shouldBe lastName
    }
  }

  it should "return a BadRequest error if last_name is empty" in new Context {
    (personQueryMock.findBy _).expects(*).never()

    val requestEntity = HttpEntity(ContentTypes.`application/json`, s"""{"lastName": "LastName"}""")

    Post(peopleSearchEndpoint, requestEntity) ~> Route.seal(routes) ~> check {
      status shouldBe StatusCodes.BadRequest
      responseAs[String] shouldEqual "requirement failed: last_name must not be empty"
    }
  }

  it should "find the best match person by search criteria in a query string" in new Context {

    (personQueryMock.findBy _).expects(*).returns(Future.successful(persons))

    Get(s"$peopleEndpoint/?last_name=Last+NAME&zip=10153") ~> routes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      responseAs[DatatrustMatchResponse].name.value.lastName should not be empty
      responseAs[DatatrustMatchResponse].name.value.lastName.value shouldBe lastName
    }
  }

  it should "return a BadRequest error if last_name is empty in a query string" in new Context {
    (personQueryMock.findBy _).expects(*).never()

    val requestEntity = HttpEntity(ContentTypes.`application/json`, s"""{"lastName": "LastName"}""")

    Get(s"$peopleEndpoint/?first_name=Last+NAME&zip=10153") ~> Route.seal(routes) ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual "Request is missing required query parameter 'last_name'"
    }
  }

  it should "return a MethodNotAllowed error for Put requests to the /people path" in new Context {
    (personQueryMock.findBy _).expects(*).never()

    Put(peopleEndpoint) ~> Route.seal(routes) ~> check {
      status shouldBe StatusCodes.MethodNotAllowed
      responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
    }
  }
}
