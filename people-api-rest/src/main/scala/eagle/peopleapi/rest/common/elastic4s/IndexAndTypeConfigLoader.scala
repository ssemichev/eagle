package eagle.peopleapi.rest.common.elastic4s

import com.sksamuel.elastic4s.IndexAndType
import com.typesafe.config.Config

import scala.util.Try

object IndexAndTypeConfigLoader {

  def indexAndType(idxTypeConfig: Config): IndexAndType = {
    val index = Try(idxTypeConfig.getString("index")).toOption
      .getOrElse(throw new Elastic4sConfigException("Configuration field index is required"))
    val typ = Try(idxTypeConfig.getString("type")).toOption
      .getOrElse(throw new Elastic4sConfigException("Configuration field type is required"))
    IndexAndType(index, typ)
  }
}
