package eagle.peopleapi.rest.translators

import eagle.peopleapi.service.common.EsModel
import eagle.peopleapi.service.datatrust.models.Identities
import eagle.peopleapi.service.datatrust.models.Name
import eagle.peopleapi.service.datatrust.models.Person
import eagle.testkit.UnitTestSpec

class PersonToPersonDatatrustResponseSpec extends UnitTestSpec {

  behavior of "Person To PersonDatatrustResponse translator"

  trait Context {
    val name = Name(
      firstName  = Some("firstName"),
      middleName = Some("middleName"),
      lastName   = Some("lastName"),
      prefix     = Some("prefix"),
      suffix     = Some("suffix")
    )

    val person = Person(
      id         = Some("id"),
      identities = Some(Identities(Some("dtRegId"))),
      name       = Some(name),
      dob        = Some("dob"),
      county     = Some("county"),
      mailZip    = Some("mailZip"),
      regZip     = Some("regZip"),
      es         = Some(EsModel(Some("es_id")))
    )
  }

  it should "translate Person to PersonDatatrustResponse" in new Context {
    val personDatatrustResponse = new PersonToPersonDatatrustResponse().translate(person)
    personDatatrustResponse.name.value.firstName shouldBe name.firstName
    personDatatrustResponse.name.value.middleName shouldBe name.middleName
    personDatatrustResponse.name.value.lastName shouldBe name.lastName
    personDatatrustResponse.name.value.prefix shouldBe name.prefix
    personDatatrustResponse.name.value.suffix shouldBe name.suffix
    personDatatrustResponse.zip shouldBe person.regZip
    personDatatrustResponse.identities.value.dtRegId shouldBe person.identities.value.dtRegId
    personDatatrustResponse.identities.value.tvId shouldBe person.es.value.id
  }
}
