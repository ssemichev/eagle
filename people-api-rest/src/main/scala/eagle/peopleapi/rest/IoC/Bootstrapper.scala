package eagle.peopleapi.rest.IoC

import com.google.inject.AbstractModule
import eagle.common.config.ConfigModule
import eagle.peopleapi.rest.AppConfig
import net.codingwell.scalaguice.ScalaModule

private[eagle] class Bootstrapper extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bindConfiguration()

    install(new ConfigModule)
    install(new AuthenticationModule)
    install(new ServicesModule)
  }

  /**
   * Binds the application configuration to the [[AppConfig]] interface.
   *
   * The config is bound as an eager singleton so that errors in the config are detected
   * as early as possible.
   */
  private[this] def bindConfiguration() = {
    bind[AppConfig].toProvider[AppConfigProvider].asEagerSingleton()
  }
}