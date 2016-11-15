package eagle.peopleapi

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.sksamuel.elastic4s.RichGetResponse
import com.sksamuel.elastic4s.RichSearchHit
import com.sksamuel.elastic4s.RichSearchResponse
import com.sksamuel.elastic4s.jackson.JacksonJson.mapper
import eagle.peopleapi.service.common.EsModel

package object service {

  mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)
  mapper.registerModule(new DefaultScalaModule)

  implicit class RichGetResponseHelper(val response: RichGetResponse) extends AnyVal {
    def as[T: Manifest]: Option[T] = {
      Some(response.original).filter(_.isExists).map { r ⇒
        val node = mapper.readTree(response.sourceAsString).asInstanceOf[ObjectNode]
        val id = Option(response.id)
        val esType = Option(response.`type`)
        val index = Option(response.index)
        val version = Option(response.version)
        node.putPOJO("es", EsModel(id, esType, index, version))
        mapper.readValue[T](mapper.writeValueAsBytes(node))
      }
    }
  }

  implicit class RichSearchResponseHelper(val response: RichSearchResponse) extends AnyVal {

    def asArray[T: Manifest]: Array[T] = response.hits.map(hit ⇒ as[T](hit))

    private[this] def as[T: Manifest](hit: RichSearchHit): T = {
      val node = mapper.readTree(hit.sourceAsString).asInstanceOf[ObjectNode]
      val id = Option(hit.id)
      val esType = Option(hit.`type`)
      val index = Option(hit.index)
      val version = Option(hit.version)
      node.putPOJO("es", EsModel(id, esType, index, version))
      mapper.readValue[T](mapper.writeValueAsBytes(node))
    }
  }

}
