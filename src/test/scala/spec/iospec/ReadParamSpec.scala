package spec.iospec

import io.ReadParam
import org.scalatest.TryValues._
import org.scalatest._
import various.Def

import scala.util.Success

/**
 * Test IO, using data present on file.
 */
class ReadParamSpec extends FlatSpec with Matchers {
  "parseRealInteger" should "properly parse a pair of data" in {
    val validStr = "5.9, 12"

    val res = ReadParam.parseRealInteger(validStr).map(t => (math.abs(t._1 - 5.9) < Def.epsilon, t._2))

    res should ===(Success((true, 12)))
  }
}