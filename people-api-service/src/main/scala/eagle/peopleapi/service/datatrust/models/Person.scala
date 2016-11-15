package eagle.peopleapi.service.datatrust.models

import eagle.common.traits.Contract
import eagle.peopleapi.service.common.EsModel

final case class Person(
  id:           Option[String]            = None,
  sources:      Option[List[String]]      = None,
  capturedAt:   Option[String]            = None,
  identities:   Option[Identities]        = None,
  name:         Option[Name]              = None,
  dob:          Option[String]            = None,
  county:       Option[String]            = None,
  mailZip:      Option[String]            = None,
  regZip:       Option[String]            = None,
  valuesString: Option[List[ValueString]] = None,
  es:           Option[EsModel]           = None
) extends Contract

final case class Identities(
  dtRegId: Option[String] = None
)

case class Name(
  firstName:  Option[String] = None,
  middleName: Option[String] = None,
  lastName:   Option[String] = None,
  suffix:     Option[String] = None,
  prefix:     Option[String] = None
)

case class ValueString(
  id:           Option[String]       = None,
  instances:    Option[Int]          = None,
  source:       Option[String]       = None,
  capturedAt:   Option[String]       = None,
  clients:      Option[List[String]] = None,
  sources:      Option[List[String]] = None,
  owners:       Option[List[String]] = None,
  rentalLists:  Option[List[String]] = None,
  value:        Option[String]       = None,
  hashValue:    Option[String]       = None,
  name:         Option[String]       = None,
  originalName: Option[String]       = None,
  valueS:       Option[String]       = None
)