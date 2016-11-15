package eagle.testkit.es

import com.sksamuel.elastic4s.ElasticDsl._
import eagle.common.traits.JsonSupport
import eagle.testkit.resourceAsLines

trait DatatrustEsCluster extends EsCluster with JsonSupport {

  override val esType = "person"
  val data = resourceAsLines("/es/data/datatrust.json")

  override def initCluster(): Unit = {
    blockUntilGreen()

    val mapping = resourceAsLines("/es/mappings/person-datatrust-dev.json").mkString

    client.execute {
      create index esIndex source mapping
    }
    blockUntilIndexExists(esIndex)

    val requests = data.map {
      doc ⇒
        val docId = fieldByName[String](doc, "id")
        index into indexAndType source doc id docId
    }

    client.execute {
      bulk(requests: _*)
    }.await

    refreshIndex(esIndex)

    blockUntilCount(data.size.toLong, esIndex)
  }
}
