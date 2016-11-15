package eagle.peopleapi.rest.common

import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import eagle.common._

import akka.http.scaladsl.Http
import akka.http.scaladsl.HttpsConnectionContext

// scalastyle:off null
trait HttpsContext extends BaseComponent {

  //TODO Move to AkkaSSLConfig
  val password = "abcdef".toCharArray

  val ks = KeyStore.getInstance("jks")
  ks.load(resourceStream("keys/ssl-test-keystore.jks"), password)

  val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
  keyManagerFactory.init(ks, password)

  val context = SSLContext.getInstance("TLS")
  context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)

  Http().setDefaultServerHttpContext(new HttpsConnectionContext(context))

  private[this] def resourceStream(resourceName: String): InputStream = {
    val is = getClass.getClassLoader.getResourceAsStream(resourceName)
    require(is.isDefined, s"Resource $resourceName not found")
    is
  }
}
