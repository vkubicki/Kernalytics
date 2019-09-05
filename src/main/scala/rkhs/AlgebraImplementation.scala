package rkhs

import breeze.linalg.{DenseMatrix, DenseVector, sum}
import io.ReadParam
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
    object InnerProductSpace extends AlgebraAbstract.InnerProductSpace[Real] {
      def minus(x: Real, y: Real): Real = x - y

      def ip(x: Real, y: Real): Real = x * y
    }

    val NormedSpace: AlgebraAbstract.NormedSpace[Real] = AlgebraAbstract.NormedSpaceFromInnerProductSpace(InnerProductSpace)

    val MetricSpace: AlgebraAbstract.MetricSpace[Real] = AlgebraAbstract.MetricSpaceFromInnerProductSpace(InnerProductSpace)

/*    /**
     * Must be used as a metric for the laplacian kernel to get the ChiSquared kernel.
     * https://en.wikipedia.org/wiki/Positive-definite_kernel
     *
     * TODO: implement other squared norm based kernels, like Jensen divergence or Total Variation.
     */
    def ChiSquared(x: DenseVector[Real], y: DenseVector[Real]): Real = {
      val elements = DenseVector.tabulate(x.size)(i => {
        math.pow(x(i) - y(i), 2.0) / (x(i) + y(i))
      })

      sum(elements)
    }*/

    def getKernel(inStr: String): Try[(Real, Real) => Real] = {
      for {
        (structure, kernel, param) <- ReadParam.parseKernel(inStr)
        res <- {
          structure match {
            case "InnerProductSpace" => InnerProductSpace.getKernel(kernel, param)
            case "MetricSpace" => MetricSpace.getKernel(kernel, param)
            case _ => Failure(new Exception(s"For real numbers, the structure $structure has not been implemented yet. Try InnerProductSpace or MetricSpace."))
          }
        }
      } yield {
        res
      }
    }
  }

  object VectorR {
    object InnerProductSpace extends AlgebraAbstract.InnerProductSpace[DenseVector[Real]] {
      def minus(x: DenseVector[Real], y: DenseVector[Real]): DenseVector[Real] = x - y
      def ip(x: DenseVector[Real], y: DenseVector[Real]): Real = x.dot(y)
    }

    val NormedSpace: AlgebraAbstract.NormedSpace[DenseVector[Real]] = AlgebraAbstract.NormedSpaceFromInnerProductSpace(InnerProductSpace)

    val MetricSpace: AlgebraAbstract.MetricSpace[DenseVector[Real]] = AlgebraAbstract.MetricSpaceFromInnerProductSpace(InnerProductSpace)

    def getKernel(inStr: String): Try[(DenseVector[Real], DenseVector[Real]) => Real] = {
      for {
        (structure, kernel, param) <- ReadParam.parseKernel(inStr)
        res <- {
          structure match {
            case "InnerProductSpace" => InnerProductSpace.getKernel(kernel, param)
            case "MetricSpace" => MetricSpace.getKernel(kernel, param)
            case _ => Failure(new Exception(s"For vectors of real numbers, the structure $structure has not been implemented yet. Try InnerProductSpace or MetricSpace."))
          }
        }
      } yield {
        res
      }
    }
  }

  object MatrixR {
    object InnerProductSpace extends AlgebraAbstract.InnerProductSpace[DenseMatrix[Real]] {
      def minus(x: DenseMatrix[Real], y: DenseMatrix[Real]): DenseMatrix[Real] = x - y

      def ip(x: DenseMatrix[Real], y: DenseMatrix[Real]): Real = Math.frobeniusInnerProduct(x, y) // because trace(x.t * y) computes a lot of useless coefficients (every non-diagonal terms)
    }

    val NormedSpace: AlgebraAbstract.NormedSpace[DenseMatrix[Real]] = AlgebraAbstract.NormedSpaceFromInnerProductSpace(InnerProductSpace)

    val MetricSpace: AlgebraAbstract.MetricSpace[DenseMatrix[Real]] = AlgebraAbstract.MetricSpaceFromInnerProductSpace(InnerProductSpace)

    def getKernel(inStr: String): Try[(DenseMatrix[Real], DenseMatrix[Real]) => Real] = {
      for {
        (structure, kernel, param) <- ReadParam.parseKernel(inStr)
        res <- {
          structure match {
            case "InnerProductSpace" => InnerProductSpace.getKernel(kernel, param)
            case "MetricSpace" => MetricSpace.getKernel(kernel, param)
            case _ => Failure(new Exception(s"For matrices of real numbers, the structure $structure has not been implemented yet. Try InnerProductSpace or MetricSpace."))
          }
        }
      } yield {
        res
      }
    }
  }
}