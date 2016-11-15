package eagle.common.traits

import java.lang.annotation.Annotation

trait ApplicationGlobalBase {
  def getInstance[T: Manifest]: T
  def getInstance[T: Manifest](ann: Annotation): T
}