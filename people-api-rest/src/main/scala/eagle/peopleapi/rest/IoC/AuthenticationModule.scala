package eagle.peopleapi.rest.IoC

import com.google.inject.PrivateModule
import eagle.peopleapi.rest.common.authentication.ConfigUserPassAuthenticator
import eagle.peopleapi.rest.common.authentication.UserPassAuthenticator
import net.codingwell.scalaguice.ScalaPrivateModule

private[eagle] class AuthenticationModule extends PrivateModule with ScalaPrivateModule {

  override def configure(): Unit = {
    setupDependencies()

    expose[UserPassAuthenticator[String]].annotatedWithName("searchEndpoint")
  }

  private[this] def setupDependencies(): Unit = {
    bind[UserPassAuthenticator[String]].annotatedWithName("searchEndpoint").
      to[ConfigUserPassAuthenticator].asEagerSingleton()
  }
}