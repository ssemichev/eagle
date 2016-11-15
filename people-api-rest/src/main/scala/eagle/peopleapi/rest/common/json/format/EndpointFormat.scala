package eagle.peopleapi.rest.common.json.format

import eagle.peopleapi.rest.common.Protocol
import eagle.peopleapi.rest.common.models.About
import eagle.peopleapi.rest.common.models.Status

trait EndpointFormat extends Protocol {
  implicit val statusFormat = jsonFormat2(Status)
  implicit val aboutFormat = jsonFormat4(About)
}
