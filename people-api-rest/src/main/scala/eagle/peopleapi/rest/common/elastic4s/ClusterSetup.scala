package eagle.peopleapi.rest.common.elastic4s

import com.sksamuel.elastic4s.ElasticsearchClientUri
import org.elasticsearch.common.settings.Settings

sealed trait ClusterSetup {
  val settings: Settings
}

case class RemoteClusterSetup(
  uri:      ElasticsearchClientUri,
  settings: Settings               = Settings.builder.build
)
    extends ClusterSetup

case class LocalNodeSetup(settings: Settings = Settings.builder.build) extends ClusterSetup
