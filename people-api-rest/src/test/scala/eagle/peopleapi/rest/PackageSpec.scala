package eagle.peopleapi.rest

import akka.event.NoLogging
import eagle.testkit.UnitTestSpec

class PackageSpec extends UnitTestSpec {

  behavior of "PeopleApi package"

  it should "count an action elapsed time" in {
    time(
      "test", "testAction"
    )(NoLogging) shouldBe "test"
  }

}
