package eagle.peopleapi.rest.translators

import eagle.common.traits.Translator
import eagle.peopleapi.rest.datacontracts._
import eagle.peopleapi.service.datatrust.models.Person

class PersonToPersonDatatrustResponse extends Translator[Person, DatatrustMatchResponse] {

  override def translate(from: Person): DatatrustMatchResponse = {
    implicit val person = from

    DatatrustMatchResponse(
      identities = identities(),
      name       = name(),
      dob        = dob(),
      county     = from.county,
      zip        = from.regZip map { zip ⇒ zip } orElse from.mailZip,
      details    = details()
    )
  }

  private[this] def identities()(implicit person: Person) = {
    Some(Identity(
      tvId                = person.es.flatMap(_.id),
      dtRegId             = person.identities.flatMap(_.dtRegId),
      stateVoterId        = getPropertyByName("stateVoterId"),
      jurisdictionVoterId = getPropertyByName("jurisdictionVoterId"),
      affidavitId         = getPropertyByName("affidavitId"),
      legacyId            = getPropertyByName("legacyId"),
      sourceId            = getPropertyByName("sourceId")
    ))
  }

  private[this] def getPropertyByName(name: String)(implicit person: Person): Option[String] = {
    for {
      valuesString ← person.valuesString
      valueString ← valuesString.find(value ⇒ value.name.contains(name.toLowerCase()))
      value ← valueString.valueS
    } yield value
  }

  private[this] def name()(implicit person: Person) = {
    Some(Name(
      firstName  = person.name.flatMap(_.firstName),
      middleName = person.name.flatMap(_.middleName),
      lastName   = person.name.flatMap(_.lastName),
      prefix     = person.name.flatMap(_.prefix),
      suffix     = person.name.flatMap(_.suffix)
    ))
  }

  private[this] def dob()(implicit person: Person) = {
    Some(Dob(
      birthYear   = getPropertyByName("birthYear"),
      birthMonth  = getPropertyByName("birthMonth"),
      birthDay    = getPropertyByName("birthDay"),
      dateOfBirth = person.dob
    ))
  }

  private[this] def details()(implicit person: Person) = {
    Some(Details(
      registeredParty       = getPropertyByName("registeredParty"),
      juriscode             = getPropertyByName("juriscode"),
      jurisname             = getPropertyByName("juriscode"),
      countyFips            = getPropertyByName("countyFips"),
      countyNumber          = getPropertyByName("countyNumber"),
      town                  = getPropertyByName("town"),
      precinctNumber        = getPropertyByName("precinctNumber"),
      precinctName          = getPropertyByName("precinctName"),
      congressionalDistrict = getPropertyByName("congressionalDistrict"),
      dma                   = getPropertyByName("dma"),
      dmaNumber             = getPropertyByName("dmaNumber"),
      republicanBallotScore = getPropertyByName("republicanBallotScore"),
      landLineAreaCode      = getPropertyByName("landLineAreaCode"),
      landLineNumber        = getPropertyByName("landLineNumber"),
      generalTurnoutScore   = getPropertyByName("generalTurnoutScore")
    ))
  }
}