package eagle.peopleapi.service.datatrust.models

import eagle.common.traits.Contract
import eagle.peopleapi.service.common.Constants
import eagle.common._

case class PersonCriteria(
    firstName: Option[String] = None,
    lastName:  Option[String],
    regZip:    Option[String] = None,
    mailZip:   Option[String] = None,
    limit:     Int            = Constants.esHitsSize
) extends Contract {
  require(lastName.toStr.nonEmpty, "last_name must not be empty")
}