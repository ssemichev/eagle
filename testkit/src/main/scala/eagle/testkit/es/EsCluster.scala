package eagle.testkit.es

import java.io.File
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.IndexAndType
import com.sksamuel.elastic4s.testkit.ElasticMatchers
import com.sksamuel.elastic4s.testkit.ElasticSugar
import org.apache.commons.io.FileUtils
import scala.util.Try

trait EsCluster extends ElasticMatchers with ElasticSugar {

  val esIndex = getClass.getSimpleName.toLowerCase
  val esType = "test_type"
  def indexAndType: IndexAndType = IndexAndType(esIndex, esType)

  def initCluster(): Unit = {}

  def shutdownCluster(dataDir: String)(implicit client: ElasticClient): Unit = {
    client.close()

    // As a temporary fix for https://github.com/sksamuel/elastic4s/issues/604
    Try(FileUtils.deleteDirectory(new File(dataDir)))
  }
}
