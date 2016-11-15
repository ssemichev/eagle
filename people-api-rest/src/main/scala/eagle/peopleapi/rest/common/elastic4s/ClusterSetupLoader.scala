package eagle.peopleapi.rest.common.elastic4s

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.typesafe.config.Config
import com.typesafe.config.ConfigRenderOptions
import org.elasticsearch.common.settings.loader.JsonSettingsLoader
import org.elasticsearch.common.settings.Settings

import scala.util.Try

object ClusterSetupLoader {

  val UriKey = "uri"
  val TypeKey = "type"
  lazy val loader = new JsonSettingsLoader()

  def isTransport(config: Config): Boolean = Try(config.getString(TypeKey)).toOption match {
    case Some("transport") ⇒ true
    case Some("node")      ⇒ false
    case _ ⇒ throw new Elastic4sConfigException(
      "Configuration field type is required for cluster setup; pass either \"node\" or \"transport\""
    )
  }

  def uri(config: Config): ElasticsearchClientUri = Try(config.getString(UriKey)).toOption
    .map(ElasticsearchClientUri(_))
    .getOrElse(throw new Elastic4sConfigException("Configuration field uri is mandatory"))

  def settings(config: Config): Settings = {
    Settings.settingsBuilder()
      .put("client.transport.sniff", true) // Will discover other hosts by default
      .put(loader.load(config.root().render(ConfigRenderOptions.concise())))
      .build()
  }

  def setup(config: Config): ClusterSetup = {
    if (isTransport(config)) RemoteClusterSetup(uri(config), settings(config)) else LocalNodeSetup(settings(config))
  }
}
