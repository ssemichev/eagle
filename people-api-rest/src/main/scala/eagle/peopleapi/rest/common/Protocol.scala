package eagle.peopleapi.rest.common

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import eagle.peopleapi.rest.common.json.SnakifiedSprayJsonSupport
import eagle.peopleapi.rest.common.json.format.DateTimeFormat
import spray.json.CompactPrinter

trait Protocol extends SprayJsonSupport with SnakifiedSprayJsonSupport with DateTimeFormat {
  implicit val printer = CompactPrinter
}
