package eagle.testkit.es

import eagle.testkit.UnitTestSpec

trait EsUnitTestSpec extends UnitTestSpec with EsCluster {

  override def beforeAll(): Unit = {
    initCluster()
  }

  override def afterAll(): Unit = {
    shutdownCluster(testNodeHomePath.toString)(client)
  }
}
