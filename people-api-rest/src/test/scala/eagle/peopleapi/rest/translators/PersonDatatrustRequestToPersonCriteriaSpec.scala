package eagle.peopleapi.rest.translators

import eagle.peopleapi.rest.datacontracts.DatatrustMatchRequest
import eagle.testkit.UnitTestSpec

class PersonDatatrustRequestToPersonCriteriaSpec extends UnitTestSpec {

  behavior of "PersonDatatrustRequest To PersonCriteria translator"

  trait Context {
    val personDatatrustRequest = DatatrustMatchRequest(
      firstName  = Some("firstName"),
      lastName   = Some("lastName"),
      postalCode = Some("11111")
    )
  }

  it should "translate PersonDatatrustRequest to PersonCriteria" in new Context {
    val personCriteria = new DatatrustMatchRequestToPersonCriteria().translate(personDatatrustRequest)
    personCriteria.firstName shouldBe personDatatrustRequest.firstName
    personCriteria.lastName shouldBe personDatatrustRequest.lastName
    personCriteria.regZip shouldBe personDatatrustRequest.postalCode
    personCriteria.mailZip shouldBe personDatatrustRequest.postalCode
    personCriteria.limit shouldBe 1
  }

  it should "translate PersonDatatrustRequest to Try of PersonCriteria" in new Context {
    val personCriteria = new DatatrustMatchRequestToPersonCriteria {}.tryToTranslate(personDatatrustRequest)
    personCriteria.isSuccess shouldBe true
  }

  it should "exception" in new Context {
    the[IllegalArgumentException] thrownBy {
      new DatatrustMatchRequestToPersonCriteria().translate(DatatrustMatchRequest(None, None, None))
    } should have message "requirement failed: last_name must not be empty"
  }

  it should "clean up zip during translation" in new Context {
    val criteria = new DatatrustMatchRequestToPersonCriteria()
    criteria.translate(DatatrustMatchRequest(lastName = Some("l"), postalCode = Some("11111aa"))).mailZip.value shouldBe "11111"
    criteria.translate(DatatrustMatchRequest(lastName = Some("l"), postalCode = Some("1111"))).mailZip.value shouldBe "1111"
    criteria.translate(DatatrustMatchRequest(lastName = Some("l"), postalCode = Some("11111222"))).mailZip.value shouldBe "11111"
    criteria.translate(DatatrustMatchRequest(lastName = Some("l"), postalCode = Some("11111"))).mailZip.value shouldBe "11111"
  }
}
