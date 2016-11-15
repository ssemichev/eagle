package eagle.common.traits

import org.json4s.JsonAST.JValue
import org.scalatest.FlatSpec
import org.scalatest.Inside
import org.scalatest.Matchers
import org.scalatest.OptionValues

class JsonSupportSpec extends FlatSpec with Matchers with OptionValues with Inside with JsonSupport {

  behavior of "Json Support"

  it should "parse string to Map" in {
    val map = toMap(json)

    map should not be empty
    map should have size 3

    map.get(nameFieldName).value shouldBe childName
    map.get("abcde") shouldBe None
  }

  it should "parse string to JValue" in {
    val jValue = toJValue(json)
    jValue shouldBe a[JValue]
  }

  it should "get a field by name" in {
    val field = fieldByName[String](json, nameFieldName)
    field shouldBe childName
  }

  it should "render Json from a class" in {
    Child(childName, 1, Some("2004-09-04T18:06:22Z")).toJson shouldBe jsonChild
  }

  it should "extract a class from JSON" in {
    validateChild(fromJson[Child](jsonChild))
  }

  it should "extract Option of a class from JSON" in {
    validateChild(fromJsonOpt[Child](jsonChild).value)
  }

  it should "extract None from wrong JSON" in {
    fromJsonOpt[Child](jsonChildWrong) shouldBe None
  }

  it should "extract Either Right of a class from JSON" in {
    val child = fromJsonEither[Child](jsonChild)
    child.isRight shouldBe true
    validateChild(child.right.get)
  }

  it should "extract Either Left from wrong JSON" in {
    fromJsonEither[Child](jsonChildWrong).isLeft shouldBe true
  }

  val childName = "Joe"
  val nameFieldName = "name"
  val jsonChild = s"""{"$nameFieldName":"$childName","age":1,"birth_date":"2004-09-04T18:06:22Z"}"""
  val jsonChildWrong = s"""{"$nameFieldName":"$childName","age1":1,"birth_sate":"2004-09-04T18:06:22Z"}"""

  val json =
    s"""
         { "$nameFieldName": "$childName",
           "address": {
             "street": "Bulevard",
             "city": "Helsinki"
           },
           "children": [
             {
               "name": "Mary",
               "age": 5,
               "birth_date": "2004-09-04T18:06:22Z"
             },
             {
               "name": "Mazy",
               "age": 3
             }
           ]
         }
    """

  case class Child(name: String, age: Int, birthDate: Option[String]) extends JsonSupport

  private[this] def validateChild(child: Child): Unit = {
    inside(child) {
      case Child(name, age, birthDate) ⇒
        name shouldBe childName
        age shouldBe 1
        birthDate.value shouldBe "2004-09-04T18:06:22Z"
      case _ ⇒ throw new Error("Failed")
    }
  }
}