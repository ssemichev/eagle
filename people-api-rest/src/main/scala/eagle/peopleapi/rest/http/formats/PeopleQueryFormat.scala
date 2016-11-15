package eagle.peopleapi.rest.http.formats

import eagle.peopleapi.rest.common.Protocol
import eagle.peopleapi.rest.datacontracts._

trait PeopleQueryFormat extends Protocol {
  implicit val personDatatrustRequestFormat = jsonFormat3(DatatrustMatchRequest)

  implicit val identityFormat = jsonFormat7(Identity)
  implicit val nameFormat = jsonFormat5(Name)
  implicit val dobFormat = jsonFormat4(Dob)
  implicit val detailsFormat = jsonFormat15(Details)
  implicit val personDatatrustResponseFormat = jsonFormat6(DatatrustMatchResponse)
}