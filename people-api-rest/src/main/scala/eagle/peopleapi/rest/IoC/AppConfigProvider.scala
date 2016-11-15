package eagle.peopleapi.rest.IoC

import javax.inject.Inject

import com.google.inject.Provider
import com.typesafe.config.Config
import eagle.peopleapi.rest.AppConfig

class AppConfigProvider @Inject() (configuration: Config) extends Provider[AppConfig] {

  override def get(): AppConfig = {
    new AppConfig {
      override def config: Option[Config] = Some(configuration)
    }
  }
}
