package eagle.peopleapi.rest.common.json

import java.util.Locale

import spray.json.DefaultJsonProtocol

import scala.reflect.ClassTag

trait SnakifiedSprayJsonSupport extends DefaultJsonProtocol {

  override protected def extractFieldNames(classTag: ClassTag[_]) = {

    def snakify(name: String) = pass2.replaceAllIn(pass1.replaceAllIn(name, replacement), replacement).toLowerCase(Locale.US)

    super.extractFieldNames(classTag).map { snakify }
  }

  private[this] val pass1 = """([A-Z]+)([A-Z][a-z])""".r
  private[this] val pass2 = """([a-z\d])([A-Z])""".r
  private[this] val replacement = "$1_$2"
}