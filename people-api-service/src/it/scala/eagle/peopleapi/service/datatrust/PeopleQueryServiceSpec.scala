package eagle.peopleapi.service.datatrust

import eagle.peopleapi.service.datatrust.models.PersonCriteria
import eagle.testkit.es.EsUnitTestSpec
import eagle.testkit.es.DatatrustEsCluster
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class PeopleQueryServiceSpec extends EsUnitTestSpec with DatatrustEsCluster with ScalaFutures {

  behavior of "PeopleQuery Service"

  override implicit val patienceConfig = PatienceConfig(timeout = 10 seconds)

  trait Context {
    val service = new PeopleQueryService(client, indexAndType)
  }

  it should "get a Person document by ID" in new Context {
    val id = "1"

    whenReady(service.getById(id)) { result =>
      result should not be empty
      result.value.id shouldBe Some(id)

      result.value.es should not be empty
      val es = result.value.es.value
      es.id shouldBe Some(id)
      es.esType shouldBe Some(indexAndType.`type`)
      es.index shouldBe Some(indexAndType.index)
      es.version shouldBe Some(1)
    }
  }

  it should "get a Person document by ID and it should containg valuesString" in new Context {
    val id = "ad82c0a0c93659928330fc1acd2984f3"

    whenReady(service.getById(id)) { result =>
      result should not be empty
      result.value.id shouldBe Some(id)

      result.value.valuesString should not be empty
      result.value.valuesString.value.size should be > 0

      result.value.es should not be empty
      val es = result.value.es.value
      es.id shouldBe Some(id)
      es.esType shouldBe Some(indexAndType.`type`)
      es.index shouldBe Some(indexAndType.index)
      es.version shouldBe Some(1)
    }
  }

  it should "returns None if there is no Person document with the given ID" in new Context {
    whenReady(service.getById("non-existent")) { result => result shouldBe None }
  }

  it should "returns None if there is no Person document matches a search criteria" in new Context {
    val criteria = PersonCriteria(lastName = Some("non-existent"))

    whenReady(service.findBy(criteria)) { result => result shouldBe empty }
  }

  it should "find Person documents by a search criteria" in new Context {
    whenReady(service.findBy(PersonCriteria(lastName = Some("ferry i")))) { result =>
      result should not be empty
      result should have length 2
    }

    whenReady(service.findBy(PersonCriteria(lastName = Some("SHAW sn")))) { result =>
      result should not be empty
      result should have length 6
    }
  }

  it should "find Person documents by a search criteria and sort them by the best match" in new Context {
    val criteria = PersonCriteria(
      firstName = Some("carOLYn "),
      lastName = Some("shaw jn"),
      regZip = Some("10453"),
      mailZip = Some("10453")
    )

    whenReady(service.findBy(criteria)) { result =>
      result should not be empty
      result should have length 4
      result flatMap { h => h.es.map(_.id.value) } shouldBe Array("1", "14", "12", "11")
    }
  }
}
