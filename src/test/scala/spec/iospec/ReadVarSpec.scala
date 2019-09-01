package spec.iospec

import io.ReadVar
import org.scalatest.TryValues._
import org.scalatest._

/**
 * Test IO, using data present on file.
 */
class ReadVarSpec extends FlatSpec with Matchers {
  "mergeFilesContents" should "properly merge data" in {
    val dataA = Array(
      Array("v1", "Real", "1.0", "2.0", "3.0"),
      Array("v2", "Index", "4", "5", "6"))

    val dataB = Array(
      Array("v2", "Index", "7", "8", "9"),
      Array("v1", "Real", "10.0", "11.0", "12.0"))

    val expected = Array(
      Array("v1", "Real", "1.0", "2.0", "3.0", "10.0", "11.0", "12.0"),
      Array("v2", "Index", "4", "5", "6", "7", "8", "9"))
      
    val res = ReadVar.mergeFilesContents(dataA, dataB)

    res.success.value shouldBe expected
  }
}