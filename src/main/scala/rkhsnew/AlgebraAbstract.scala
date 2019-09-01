package rkhsnew

import rkhs.Algebra
import various.TypeDef._

/**
 * Abstract algebraic structures defined as traits, which must be implemented in AlgebraImplemented as objects for each
 * type of data on which they can be implemented. The kernels are included in each algebraic structure.
 * corresponding
 */
object AlgebraAbstract {
  object Traits {
    trait InnerProductSpace[T] {
      def minus(x: T, y: T): T
      def ip(x: T, y: T): Real

      var kernel = Map[String, (String, T, T) => Real](
        "linear" -> { case (paramStr, x, y) => ip(x, y) },
 //       "polynomial" -> (paramStr: String, x: T, y: T) => {val c = 12., val d = 12, math.pow(ip(x, y) + c, d)}
      )

      def gaussian(x: T, y: T, sd: Real): Real = {
        val diff = minus(x, y)
        math.exp(-ip(diff, diff) / (2.0 * math.pow(sd, 2.0)))
      }
    }

    trait NormedSpace[T] extends InnerProductSpace[T] {
      def norm(x: T): Real
    }

    trait MetricSpace[T] {
      def distance(x: T, y: T): Real

      def gaussian(x: T, y: T, sd: Real): Real = math.exp(-math.pow(distance(x, y), 2.0) / (2.0 * math.pow(sd, 2.0)))
      def laplacian(x: T, y: T, alpha: Real): Real = math.exp(-alpha * distance(x, y))
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
  }
}