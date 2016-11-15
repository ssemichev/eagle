package eagle.common.traits

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TranslatorsSpec extends FlatSpec with Matchers with Translator[TestContract, TestContract] {

  behavior of "Json Support"

  it should "translate From entity to Option of To entity" in {
    tryToTranslate(new TestContract()).isSuccess shouldBe true
  }

  override def translate(from: TestContract): TestContract = from
}

class TestContract extends Contract
