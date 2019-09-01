package rkhsnew

import breeze.linalg.{DenseVector, sum}
import rkhsnew.AlgebraAbstract.Traits
import various.TypeDef._

import scala.util.{Failure, Success, Try}

/**
 * This is the last point in any algorithms where the data types have to be known. Beyond that, only the kernel is evaluated via KerEval.
 *
 * Bibliography:
 * - https://en.wikipedia.org/wiki/Reproducing_kernel_Hilbert_space#Examples
 * - https://en.wikipedia.org/wiki/Positive-definite_kernel , for the notations used in the kernels
 */
object Kernel {
  object R {
      object InnerProductSpace extends Traits.InnerProductSpace[Real] {
        def minus(x: Real, y: Real): Real = x - y
        def ip(x: Real, y: Real): Real = x * y
      }

      val NormedSpace: Traits.NormedSpace[Real] = Traits.NormedSpaceFromInnerProductSpace(InnerProductSpace)

      val MetricSpace: Traits.MetricSpace[Real] = Traits.MetricSpaceFromInnerProductSpace(InnerProductSpace)

    val kerlist = Map[String, Try[(Real, Real) => Real]]()
  }



/*  InnerProduct[Data](str: String, i: AlgebraAbstract.Traits.InnerProductSpace[Data]): Try[(Data, Data) => Real] = str match {
    case "linear" => Success((x: Data, y: Data) => i.ip(x, y))
    case _ => Failure(new Exception(s"$str not found in InnerProduct"))
  }*/
}