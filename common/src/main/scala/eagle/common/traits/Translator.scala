package eagle.common.traits

import scala.util.Try

trait Translator[From <: Contract, To <: Contract] {

  def translate(from: From): To

  def tryToTranslate(from: From): Try[To] = Try(translate(from))
}
