package eagle.peopleapi.service.common

final case class EsModel(
  id:      Option[String] = None,
  esType:  Option[String] = None,
  index:   Option[String] = None,
  version: Option[Long]   = None
)
