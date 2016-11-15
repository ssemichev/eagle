package eagle.peopleapi.rest.common.elastic4s

import javax.inject.Inject

import com.sksamuel.elastic4s.ElasticClient

import scala.concurrent.Future

sealed trait ElasticFactory extends (ClusterSetup ⇒ ElasticClient)

class ElasticFactoryImpl @Inject() () extends ElasticFactory {
  private[this] val clients = Map.empty[ClusterSetup, ElasticClient]

  def apply(cs: ClusterSetup): ElasticClient = clients.getOrElse(cs, withStopHook(cs match {
    case RemoteClusterSetup(uri, settings) ⇒ ElasticClient.transport(settings, uri)
    case LocalNodeSetup(settings)          ⇒ ElasticClient.local(settings)
    case _                                 ⇒ throw new Error("Wrong ClusterSetup type")
  }))

  private[this] def withStopHook(client: ElasticClient) = {
    scala.sys.addShutdownHook {
      Future.successful {
        client.close()
      }
    }
    client
  }
}