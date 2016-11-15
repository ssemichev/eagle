package eagle.peopleapi.rest.common.elastic4s

import com.typesafe.config.ConfigFactory
import eagle.testkit.UnitTestSpec

class ClusterSetupLoaderSpec extends UnitTestSpec {

  behavior of "Cluster Setup Loader"

  trait Context {
    val config = ConfigFactory.parseString(
      """
        type: "transport"
        cluster.name: "test-cluster"
        uri: "127.0.0.1:9300"
        client.transport.sniff: true
      """
    )
  }

  it should "build settings with discovering other hosts by default" in {
    val config = ConfigFactory.parseString("")
    ClusterSetupLoader.settings(config).get("client.transport.sniff") shouldBe "true"
  }

  it should "build settings with overridden client.transport.sniff settings" in {
    val config = ConfigFactory.parseString("client.transport.sniff: false")
    ClusterSetupLoader.settings(config).get("client.transport.sniff") shouldBe "false"
  }

  it should "build settings" in new Context {
    val settings = ClusterSetupLoader.settings(config)
    settings.get("type") shouldBe "transport"
    settings.get("cluster.name") shouldBe "test-cluster"
    settings.get("uri") shouldBe "127.0.0.1:9300"
    settings.get("client.transport.sniff") shouldBe "true"
  }

  it should "create RemoteClusterSetup" in new Context {
    ClusterSetupLoader.setup(config) shouldBe a[RemoteClusterSetup]
  }

  it should "create LocalNodeSetup" in new Context {
    val localClusterConfig = ConfigFactory.parseString(
      """
        type: "node"
        uri: "127.0.0.1:9300"
      """
    )
    ClusterSetupLoader.setup(localClusterConfig) shouldBe a[LocalNodeSetup]
  }
}
