package rkhs

import breeze.linalg.{DenseVector, sum}
import various.Error
import various.TypeDef._

import scala.util.{Failure, Success, Try}

/**
 * Abstract algebraic structures defined as traits, which must be implemented in AlgebraImplemented as objects for each
 * type of data on which they can be implemented. The kernels are included in each algebraic structure.
 * corresponding
 *
 * Bibliography:
 * - https://en.wikipedia.org/wiki/Reproducing_kernel_Hilbert_space#Common_Examples
 * - https://en.wikipedia.org/wiki/Positive-definite_kernel , for the notations used in the kernels
 */
object AlgebraAbstract {
  trait InnerProductSpace[T] {
    def minus(x: T, y: T): T
    def ip(x: T, y: T): Real

    def getKernel(kernelNameStr: String, paramStr: String): Try[(T, T) => Real] = kernelNameStr match {
      case "linear" => Success(ip)

      case "polynomial" => for {
        (c, d) <- io.ReadParam.parseRealInteger(paramStr)
        _ <- Error.validate(d, 0 <= d, s"A $kernelNameStr model has a d parameter value $paramStr. d should be superior or equal to 0.")
      } yield (x: T, y: T) => math.pow(ip(x, y) + c, d)

      case "gaussian" => for {
        sd <- Try(paramStr.toReal)
        _ <- Error.validate(sd, 0.0 < sd, s"A $kernelNameStr model has a sd parameter value $paramStr. sd should be strictly superior to 0.")
      } yield (x: T, y: T) => {val diff = minus(x, y); math.exp(-ip(diff, diff) / (2.0 * math.pow(sd, 2.0)))}

      case _ => Failure(new Exception(s"$kernelNameStr is not a kernel of InnerProductSpace, try linear, polynomial or gaussian."))
    }
  }

  trait NormedSpace[T] extends InnerProductSpace[T] {
    def norm(x: T): Real
  }

  trait MetricSpace[T] {
    def distance(x: T, y: T): Real

    def getKernel(kernelNameStr: String, paramStr: String): Try[(T, T) => Real] = kernelNameStr match {
      case "gaussian" => for {
        sd <- Try(paramStr.toReal)
        _ <- Error.validate(sd, 0.0 < sd, s"A $kernelNameStr model has a sd parameter value $paramStr. sd should be strictly superior to 0.")
      } yield (x: T, y: T) => math.exp(-math.pow(distance(x, y), 2.0) / (2.0 * math.pow(sd, 2.0)))

      case "laplacian" => for {
        alpha <- Try(paramStr.toReal)
        _ <- Error.validate(alpha, 0.0 < alpha, s"A $kernelNameStr model has a alpha parameter value $paramStr. alpha should be strictly superior to 0.")
      } yield (x: T, y: T) => math.exp(-alpha * distance(x, y))
    }
  }

  def NormedSpaceFromInnerProductSpace[T](v: InnerProductSpace[T]): NormedSpace[T] = {
    object NormedSpace extends NormedSpace[T] {
      def minus(x: T, y: T): T = v.minus(x, y)
      def ip(x: T, y: T): Real = v.ip(x, y)
      def norm(x: T): Real = math.sqrt(v.ip(x, x))
    }

    NormedSpace
  }

  def MetricSpaceFromNormedSpace[T](n: NormedSpace[T]): MetricSpace[T] = {
    object MetricSpace extends MetricSpace[T] {
      def distance(x: T, y: T): Real = n.norm(n.minus(x, y))
    }

    MetricSpace
  }

  def MetricSpaceFromInnerProductSpace[T](v: InnerProductSpace[T]): MetricSpace[T] =
    MetricSpaceFromNormedSpace(NormedSpaceFromInnerProductSpace(v))

  object Distribution {
    /**
     * Must be used as a metric for the laplacian kernel to get the ChiSquared kernel.
     * https://en.wikipedia.org/wiki/Positive-definite_kernel
     *
     * TODO: implement other squared norm based kernels, like Jensen divergence or Total Variation.
     */
    def chisquared(x: DenseVector[Real], y: DenseVector[Real]): Real = {
      val elements = DenseVector.tabulate(x.size)(i => {
        math.pow(x(i) - y(i), 2.0) / (x(i) + y(i))
      })

      sum(elements)
    }

    def getKernel(kernelNameStr: String, paramStr: String): Try[(DenseVector[Real], DenseVector[Real]) => Real] = kernelNameStr match {
      case "chisquared()" => Success(chisquared)
      case _ => Failure(new Exception("Only chisquared is implemented in Distribution at the moment."))
    }
  }
}