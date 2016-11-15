package eagle.peopleapi.service.common

import eagle.testkit.UnitTestSpec

class ConstantsSpec extends UnitTestSpec {

  behavior of "Constants"

  it should "test Constants values" in {
    Constants.esHitsSize shouldBe 10
    Constants.voterDataCluster shouldBe "voter-data"
    Constants.peopleIndexAndType shouldBe "people"
  }

}