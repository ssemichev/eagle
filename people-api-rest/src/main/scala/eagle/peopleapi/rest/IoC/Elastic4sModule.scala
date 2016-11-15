package eagle.peopleapi.rest.IoC

import javax.inject.Named

import com.google.inject.PrivateModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.IndexAndType
import eagle.peopleapi.rest.AppConfig
import eagle.peopleapi.rest.common.elastic4s.ElasticFactory
import eagle.peopleapi.rest.common.elastic4s._
import eagle.peopleapi.service.common.Constants._
import net.codingwell.scalaguice.ScalaPrivateModule

import scala.util.Try

private[eagle] class Elastic4sModule extends PrivateModule with ScalaPrivateModule {

  final val idxTypesKey = "index-and-types"

  override def configure(): Unit = {
    setupDependencies()

    expose[IndexAndType].annotatedWithName(peopleIndexAndType)
    expose[ElasticClient].annotatedWithName(voterDataCluster)
  }

  private[this] def setupDependencies(): Unit = {
    bind[ElasticFactory].to[ElasticFactoryImpl].in[Singleton]
    bind[ElasticClient].annotatedWithName(voterDataCluster).toProvider[ElasticClientProvider].asEagerSingleton()
  }

  @Provides
  @Named(peopleIndexAndType)
  @Singleton
  def providePeopleIndexAndType(config: AppConfig): IndexAndType = {
    buildIndexAndType(peopleIndexAndType, config)
  }

  private[this] def buildIndexAndType(name: String, config: AppConfig) = {
    val configuration = Try(config.elastic4s.getConfig(s"$idxTypesKey.$name"))
      .getOrElse(throw new Elastic4sConfigException(s"Missing $name index and type configuration"))

    IndexAndTypeConfigLoader.indexAndType(configuration)
  }
}