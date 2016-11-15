package eagle.peopleapi.rest.translators

import eagle.common.traits.Translator
import eagle.peopleapi.rest.datacontracts.DatatrustMatchRequest
import eagle.peopleapi.service.datatrust.models.PersonCriteria
import DatatrustMatchRequestToPersonCriteria._

class DatatrustMatchRequestToPersonCriteria extends Translator[DatatrustMatchRequest, PersonCriteria] {

  override def translate(from: DatatrustMatchRequest): PersonCriteria = {
    val zip = cleanZip(from.postalCode)

    PersonCriteria(
      firstName = from.firstName,
      lastName  = from.lastName,
      regZip    = zip,
      mailZip   = zip,
      limit     = 1
    )
  }
}

object DatatrustMatchRequestToPersonCriteria {
  lazy val nonDigitsRegex = """[^0-9]""".r

  private def cleanZip(zip: Option[String]) = {
    val zip5Length = 5

    zip.map(z ⇒ nonDigitsRegex.replaceAllIn(z, "")).map {
      z ⇒ if (z.length > zip5Length) z.substring(0, zip5Length) else z
    }
  }
}
