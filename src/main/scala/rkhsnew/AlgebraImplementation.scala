package rkhsnew

import breeze.linalg.{DenseMatrix, DenseVector, sum}
import rkhsnew.AlgebraAbstract.Traits
import various.Error
import various.Math
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
            case "InnerProductSpace" => InnerProductSpace.getKernel(kernelStr, paramStr)
            case "MetricSpace" => MetricSpace.getKernel(kernelStr, paramStr)
            case _ => Failure(new Exception(s"For real numbers, the structure $structureName has not been implemented yet. Try InnerProductSpace or MetricSpace."))
          }
        }
      } yield {
        res
      }
    }
  }

  object VectorR {
    object InnerProductSpace extends Traits.InnerProductSpace[DenseVector[Real]] {
      def minus(x: DenseVector[Real], y: DenseVector[Real]): DenseVector[Real] = x - y
      def ip(x: DenseVector[Real], y: DenseVector[Real]): Real = x.dot(y)
    }

    val NormedSpace: Traits.NormedSpace[DenseVector[Real]] = Traits.NormedSpaceFromInnerProductSpace(InnerProductSpace)

    val MetricSpace: Traits.MetricSpace[DenseVector[Real]] = Traits.MetricSpaceFromInnerProductSpace(InnerProductSpace)

    def getKernel(inStr: String): Try[(DenseVector[Real], DenseVector[Real]) => Real] = {
      val inArray: Array[String] = inStr.split(".")
      val structureName: String = inArray(0)

      for {
        _ <- Error.validate(inArray, inArray.length != 2, "For vectors of real numbers, the kernel name must follow the structure structure.kernel, for example InnerProductSpace.linear().")
        (kernelStr, paramStr) <- io.ReadParam.parseParam(structureName)
        res <- {
          structureName match {
            case "InnerProductSpace" => InnerProductSpace.getKernel(kernelStr, paramStr)
            case "MetricSpace" => MetricSpace.getKernel(kernelStr, paramStr)
            case _ => Failure(new Exception(s"For vectors of real numbers, the structure $structureName has not been implemented yet. Try InnerProductSpace or MetricSpace."))
          }
        }
      } yield {
        res
      }
    }
  }

  object MatrixR {
    object InnerProductSpace extends Traits.InnerProductSpace[DenseMatrix[Real]] {
      def minus(x: DenseMatrix[Real], y: DenseMatrix[Real]): DenseMatrix[Real] = x - y

      def ip(x: DenseMatrix[Real], y: DenseMatrix[Real]): Real = Math.frobeniusInnerProduct(x, y) // because trace(x.t * y) computes a lot of useless coefficients (every non-diagonal terms)
    }

    val NormedSpace: Traits.NormedSpace[DenseMatrix[Real]] = Traits.NormedSpaceFromInnerProductSpace(InnerProductSpace)

    val MetricSpace: Traits.MetricSpace[DenseMatrix[Real]] = Traits.MetricSpaceFromInnerProductSpace(InnerProductSpace)

    def getKernel(inStr: String): Try[(DenseMatrix[Real], DenseMatrix[Real]) => Real] = {
      val inArray: Array[String] = inStr.split(".")
      val structureName: String = inArray(0)

      for {
        _ <- Error.validate(inArray, inArray.length != 2, "For matrices of real numbers, the kernel name must follow the structure structure.kernel, for example InnerProductSpace.linear().")
        (kernelStr, paramStr) <- io.ReadParam.parseParam(structureName)
        res <- {
          structureName match {
            case "InnerProductSpace" => InnerProductSpace.getKernel(kernelStr, paramStr)
            case "MetricSpace" => MetricSpace.getKernel(kernelStr, paramStr)
            case _ => Failure(new Exception(s"For matrices of real numbers, the structure $structureName has not been implemented yet. Try InnerProductSpace or MetricSpace."))
          }
        }
      } yield {
        res
      }
    }
  }
}