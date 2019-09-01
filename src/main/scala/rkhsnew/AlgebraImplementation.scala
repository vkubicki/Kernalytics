package rkhsnew

import breeze.linalg.{DenseVector, sum}
import rkhsnew.AlgebraAbstract.Traits
import various.Error
import various.TypeDef._

import scala.util.{Failure, Success, Try}

/**
 * This is the last point in any algorithms where the data types have to be known. Beyond that, only the kernel is evaluated via KerEval.
 *
 * Bibliography:
 * - https://en.wikipedia.org/wiki/Reproducing_kernel_Hilbert_space#Examples
 * - https://en.wikipedia.org/wiki/Positive-definite_kernel , for the notations used in the kernels
 */
object AlgebraImplementation {

  object R {

    object InnerProductSpace extends Traits.InnerProductSpace[Real] {
      def minus(x: Real, y: Real): Real = x - y

      def ip(x: Real, y: Real): Real = x * y
    }

    val NormedSpace: Traits.NormedSpace[Real] = Traits.NormedSpaceFromInnerProductSpace(InnerProductSpace)

    val MetricSpace: Traits.MetricSpace[Real] = Traits.MetricSpaceFromInnerProductSpace(InnerProductSpace)

    def getKernel(inStr: String): Try[(Real, Real) => Real] = {
      val inArray: Array[String] = inStr.split(".")
      val structureName: String = inArray(0)

      for {
        _ <- Error.validate(inArray, inArray.length != 2, "For real numbers, the kernel name must follow the structure sutructure.kernel, for example InnerProductSpace.linear().")
        (kernelStr, paramStr) <- io.ReadParam.parseParam(structureName)
        res <- {
          structureName match {
            case "InnerProductSpace" => InnerProductSpace(kernelStr, paramStr)
            case _ => Failure(new Exception(s"For real numbers, the structure / kernel ${inArray(0)} has not been implemented yet."))
          }
        }
      } yield {
        res
      }
    }
  }
}