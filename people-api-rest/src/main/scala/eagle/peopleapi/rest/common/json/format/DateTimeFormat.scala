package eagle.peopleapi.rest.common.json.format

import akka.http.scaladsl.model.DateTime
import spray.json._

trait DateTimeFormat {

  implicit object DateJsonFormat extends RootJsonFormat[DateTime] {

    override def write(obj: DateTime): JsString = JsString(obj.toIsoDateTimeString())

    override def read(json: JsValue): DateTime = json match {
      case JsString(value) ⇒
        DateTime.fromIsoDateTimeString(value) match {
          case Some(date) ⇒ date
          case _          ⇒ throw new DeserializationException("Failed to parse date time [" + value + "].")
        }
      case _ ⇒ throw new DeserializationException("Failed to parse json string [" + json + "].")
    }
  }
}