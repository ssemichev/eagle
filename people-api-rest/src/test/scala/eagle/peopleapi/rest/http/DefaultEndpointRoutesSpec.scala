package eagle.peopleapi.rest.http

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import eagle.peopleapi.rest.common.Endpoint
import eagle.peopleapi.rest.common.HttpServiceBaseSpec
import eagle.peopleapi.rest.common.models.About
import eagle.peopleapi.rest.common.models.Status

class DefaultEndpointRoutesSpec extends HttpServiceBaseSpec with Endpoint {

  override def serviceRoutes: Route = defaultRoutes

  behavior of "Default routes"

  it should "generate default and service routes" in {
    serviceRoutes shouldBe defaultRoutes
    routes should not be defaultRoutes
    routes should not be serviceRoutes
    defaultRoutes should not be routes
  }

  it should "publish HTTP endpoint" in {
    noException should be thrownBy publish()
  }

  it should "respond to Health query" in {
    Get("/health") ~> defaultRoutes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      responseAs[String] shouldBe "{\"ok\":true}"
    }
  }

  it should "respond to About query" in {
    Get("/about") ~> defaultRoutes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      responseAs[About].serviceName shouldBe config.serviceConfig.name
    }
  }

  it should "respond to Status query" in {
    Get("/status") ~> defaultRoutes ~> check {
      status shouldBe StatusCodes.OK
      contentType shouldBe ContentTypes.`application/json`
      responseAs[Status].serviceName shouldBe config.serviceConfig.name
      responseAs[Status].uptime should include("milliseconds")
    }
  }

  it should "return a NotFound error for PUT requests to a bad url" in {
    Get("/bad-url") ~> Route.seal(defaultRoutes) ~> check {
      status shouldBe StatusCodes.NotFound
      responseAs[String] shouldEqual "The requested resource could not be found."
    }
  }

  it should "return a NotFound error for PUT requests to the root path" in {
    Put("/bad-url") ~> Route.seal(defaultRoutes) ~> check {
      status shouldBe StatusCodes.NotFound
      responseAs[String] shouldEqual "The requested resource could not be found."
    }
  }

  it should "make About query to other paths handled" in {
    Get("/about") ~> defaultRoutes ~> check {
      handled shouldBe true
    }
  }
}
