package eagle.peopleapi.rest.datacontracts

import eagle.common.traits.Contract

final case class DatatrustMatchRequest(
    firstName:  Option[String] = None,
    lastName:   Option[String] = None,
    postalCode: Option[String] = None
) extends Contract {
  require(lastName.nonEmpty, "last_name must not be empty")
}