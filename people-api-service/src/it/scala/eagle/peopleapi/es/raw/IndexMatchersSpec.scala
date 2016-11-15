package eagle.peopleapi.es.raw

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.testkit.ElasticMatchers
import com.sksamuel.elastic4s.testkit.ElasticSugar
import eagle.testkit.UnitTestSpec
import eagle.testkit.es.EsCluster

class IndexMatchersSpec extends UnitTestSpec with ElasticMatchers with ElasticSugar with EsCluster {
  //scalastyle:off magic.number

  val esEmptyIndex = "empty_index"

  behavior of "index matchers"

  client.execute {
    bulk(
      index into esIndex / esType fields("name" -> "south kensington", "line" -> "district"),
      index into esIndex / esType fields("name" -> "earls court", "line" -> "district", "zone" -> 2),
      index into esIndex / esType fields("name" -> "cockfosters", "line" -> "picadilly") id 3,
      index into esIndex / esType fields("name" -> "bank", "line" -> "northern")
    )
  }.await

  client.execute {
    create index esEmptyIndex
  }.await

  blockUntilCount(4, esIndex)

  it should "support index document count" in {
    esIndex should haveCount(4)
    esIndex should not(haveCount(11))
  }

  it should "support doc exists" in {
    esIndex should containDoc(3)
    esIndex should not(containDoc(44))
  }

  it should "support index exists" in {
    esIndex should beCreated
    "qweqwe" should not(beCreated)
  }

  it should "support isEmpty" in {
    esIndex should not(beEmpty)
    esEmptyIndex should beEmpty
  }

  override def beforeAll(): Unit = {
    client.execute { index exists esIndex }
  }
}
