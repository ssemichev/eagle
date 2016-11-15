package eagle.peopleapi.rest

import eagle.common.traits.Contract
import eagle.peopleapi.rest.common.BaseConfig
import eagle.peopleapi.rest.common.elastic4s.Elastic4sConfigException

import scala.util.Try

class AppConfig extends BaseConfig with Contract {

  override def appRootSectionName: String = "eagle-service"

  lazy val elastic4s = Try(rootConfig.getConfig("elastic4s"))
    .getOrElse(throw new Elastic4sConfigException("You should provide Elastic4s configuration when loading module"))
}
