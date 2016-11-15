package eagle.peopleapi.rest.datacontracts

import eagle.common.traits.Contract

final case class DatatrustMatchResponse(
  identities: Option[Identity] = None,
  name:       Option[Name]     = None,
  dob:        Option[Dob]      = None,
  county:     Option[String]   = None,
  zip:        Option[String]   = None,
  details:    Option[Details]  = None
) extends Contract

final case class Identity(
  tvId:                Option[String] = None,
  dtRegId:             Option[String] = None,
  stateVoterId:        Option[String] = None,
  jurisdictionVoterId: Option[String] = None,
  affidavitId:         Option[String] = None,
  legacyId:            Option[String] = None,
  sourceId:            Option[String] = None
)

final case class Name(
  firstName:  Option[String] = None,
  middleName: Option[String] = None,
  lastName:   Option[String] = None,
  prefix:     Option[String] = None,
  suffix:     Option[String] = None
)

final case class Dob(
  birthYear:   Option[String] = None,
  birthMonth:  Option[String] = None,
  birthDay:    Option[String] = None,
  dateOfBirth: Option[String] = None
)

final case class Details(
  registeredParty:       Option[String] = None,
  juriscode:             Option[String] = None,
  jurisname:             Option[String] = None,
  countyFips:            Option[String] = None,
  countyNumber:          Option[String] = None,
  town:                  Option[String] = None,
  precinctNumber:        Option[String] = None,
  precinctName:          Option[String] = None,
  congressionalDistrict: Option[String] = None,
  dma:                   Option[String] = None,
  dmaNumber:             Option[String] = None,
  republicanBallotScore: Option[String] = None,
  landLineAreaCode:      Option[String] = None,
  landLineNumber:        Option[String] = None,
  generalTurnoutScore:   Option[String] = None
)