package eagle.peopleapi.es.raw

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.analyzers.CustomAnalyzer
import eagle.testkit.es.EsUnitTestSpec
import eagle.testkit.es.DatatrustEsCluster
import org.elasticsearch.common.unit.Fuzziness

class DatatrustMatchQuerySpec extends EsUnitTestSpec with DatatrustEsCluster {

  behavior of "index matchers"

  val expectedMatches = Array("1", "14", "12", "11")

  it should "support index document count" in {
    search in indexAndType should haveTotalHits(data.size)
  }

  it should "find the best matches using raw JSON query" in {
    client.execute {
      search in indexAndType rawQuery {
        """
          |{
          |    "bool": {
          |      "must": [
          |        {
          |          "match": {
          |            "name.first_name.hash": {
          |              "query": "carolyn",
          |              "fuzziness": 2,
          |              "prefix_length": 1
          |            }
          |          }
          |        },
          |        {
          |          "bool": {
          |            "should": [
          |              {
          |                "match": {
          |                  "name.last_name.hash": {
          |                    "query": "shaw jn",
          |                    "fuzziness": 2,
          |                    "prefix_length": 1,
          |                    "boost": 3
          |                  }
          |                }
          |              },
          |              {
          |                "match": {
          |                  "name.last_name_with_suffix": {
          |                    "query": "shaw jn",
          |                    "fuzziness": 2,
          |                    "prefix_length": 1,
          |                    "boost": 4
          |                  }
          |                }
          |              }
          |            ]
          |          }
          |        },
          |        {
          |          "bool": {
          |            "should": [
          |              {
          |                "term": {
          |                  "reg_zip": "10453",
          |                  "boost": 7
          |                }
          |              },
          |              {
          |                "term": {
          |                  "mail_zip": "10453",
          |                  "boost": 5
          |                }
          |              }
          |            ]
          |          }
          |        }
          |      ]
          |    }
          |  }
        """.stripMargin
      }
    }.await.hits.map(_.id) shouldBe expectedMatches
  }

  it should "find the best matches" in {

    client.execute {
      search in indexAndType query {
        bool (
          must (
            {
              matchQuery("name.first_name.hash", "carolyn").fuzziness(2).prefixLength(1)
            },
            {
              bool (
                should (
                  {
                    matchQuery("name.last_name.hash", "shaw jn").fuzziness(2).prefixLength(1).boost(3.0)
                  },
                  {
                    matchQuery("name.last_name_with_suffix", "SHAW jn").fuzziness(2).prefixLength(1).boost(4.0)
                  }
                )
              )
            },
            {
              bool (
                should (
                  {
                    termQuery("reg_zip", "10453").boost(7.0)
                  },
                  {
                    termQuery("mail_zip", "10453").boost(5.0)
                  }
                )
              )
            }
          )
        )
      }
    }.await.hits.map(_.id) shouldBe expectedMatches
  }
}
