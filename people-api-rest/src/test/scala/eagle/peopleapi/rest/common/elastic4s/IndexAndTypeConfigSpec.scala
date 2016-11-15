package eagle.peopleapi.rest.common.elastic4s

import com.typesafe.config.ConfigFactory
import eagle.testkit.UnitTestSpec

class IndexAndTypeConfigSpec extends UnitTestSpec {

  behavior of "Index And Type Config"

  trait Context {
    val config = ConfigFactory.parseString(
      """
         index: "people"
         type: "person"
      """.stripMargin
    )
  }

  it should "build IndexAndType" in new Context {
    IndexAndTypeConfigLoader.indexAndType(config).index shouldBe "people"
    IndexAndTypeConfigLoader.indexAndType(config).`type` shouldBe "person"
  }

  it should "verify that field index is required" in {
    val config = ConfigFactory.parseString("type: \"person\"")

    the[Elastic4sConfigException] thrownBy {
      IndexAndTypeConfigLoader.indexAndType(config)
    } should have message "Configuration field index is required"
  }

  it should "verify that field type is required" in {
    val config = ConfigFactory.parseString("index: \"people\"")

    the[Elastic4sConfigException] thrownBy {
      IndexAndTypeConfigLoader.indexAndType(config)
    } should have message "Configuration field type is required"
  }
}
