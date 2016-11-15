package eagle.peopleapi.rest.common

import akka.event.NoLogging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import eagle.peopleapi.rest.AppConfig
import eagle.peopleapi.rest.IoC.Global
import eagle.testkit.UnitTestSpec

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait HttpServiceBaseSpec extends UnitTestSpec with ScalatestRouteTest {

  val logger = NoLogging

  val config = new AppConfig()

  override protected def afterAll() = {
    Await.ready(system.terminate(), Duration.Inf)
    super.afterAll()
  }
}
