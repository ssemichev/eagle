package eagle.peopleapi.rest.IoC

import javax.inject.Inject

import com.google.inject.Provider
import com.sksamuel.elastic4s.ElasticClient
import eagle.peopleapi.rest.AppConfig
import eagle.peopleapi.rest.common.elastic4s.ClusterSetupLoader
import eagle.peopleapi.rest.common.elastic4s.Elastic4sConfigException
import eagle.peopleapi.rest.common.elastic4s.ElasticFactory
import eagle.peopleapi.service.common.Constants.voterDataCluster
import org.slf4j.LoggerFactory

import scala.util.Try

class ElasticClientProvider @Inject() (configuration: AppConfig, elasticFactory: ElasticFactory)
    extends Provider[ElasticClient] {

  final val clustersKey = "clusters"
  final val idxTypesKey = "index-and-types"

  override def get(): ElasticClient = {
    buildElasticClient(voterDataCluster, configuration, elasticFactory)
  }

  private[this] def buildElasticClient(clusterName: String, config: AppConfig, elasticFactory: ElasticFactory) = {
    val clusterConfiguration = Try(config.elastic4s.getConfig(s"$clustersKey.$clusterName"))
      .getOrElse(throw new Elastic4sConfigException(s"Missing $clusterName cluster configuration"))

    val clusterSetup = ClusterSetupLoader.setup(clusterConfiguration)

    LoggerFactory.getLogger("ElasticClientProvider").info(s"Building $clusterName ElasticClient...")

    elasticFactory(clusterSetup)
  }
}