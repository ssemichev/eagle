package eagle.peopleapi.es.raw

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl
import eagle.testkit.UnitTestSpec
import eagle.testkit.es.EsCluster
import org.elasticsearch.common.settings.Settings

class TransportClientRawSpec extends UnitTestSpec with ElasticDsl with EsCluster {

  behavior of "ES transport client"

  it should "check if the index exist" in {

    val clusterName = getClass.getName
    val dataPath = s"/tmp/${getClass.getSimpleName}"

    val settings = Settings.settingsBuilder()
        .put("http.enabled", false)
        .put("path.home", dataPath)
        .put("cluster.name", clusterName)
    val client = ElasticClient.local(settings.build)

    client.execute { create index esIndex }
    client.admin.cluster().prepareHealth(esIndex).setWaitForActiveShards(1).execute.actionGet()

    client.execute { index into indexAndType fields "name"->"John Smith" }.await

    (index exists esIndex).indexes should contain (esIndex)

    (types exist esType in esIndex).types should contain (esType)

    client.execute { index exists esIndex }.await.isExists shouldBe true

    client.execute { delete index esIndex }.await

    client.execute { index exists esIndex }.await.isExists shouldBe false

    shutdownCluster(dataPath)(client)
  }
}
