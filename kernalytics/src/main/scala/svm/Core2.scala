package svm

import breeze.linalg._

import rkhs.KerEval
import various.TypeDef._

/**
 * Second implementation of Core, more modular, for easier unit testing.
 */
object Core2 {
  /**
   * Equivalent to takeStep in the article.
   * Optimize the problem for two given values of alpha.
   *
   * @return If optimization takes place, return (alpha1, alpha2, b), otherwise return None
   */

  val eps: Real = 1e-8

  def binaryOptimization(i1: Index, i2: Index, alpha: DenseVector[Real], b: Real, y: DenseVector[Real], cache: DenseVector[Real], kerEval: KerEval, C: Real): Option[(Real, Real, Real)] = {
    if (i1 == i2) {
      println("i1 == i2")
      return None
    }

    val alph1 = alpha(i1)
    val alph2 = alpha(i2)
    val y1 = y(i1)
    val y2 = y(i2)
    val e1 = cache(i1)
    val e2 = cache(i2)

    val s = y1 * y2

    val (l, h) = // compute L, H, via equations (13) and (14)
      if (s < 0.0) { // y1 != y2
        (max(0.0, alph2 - alph1), min(C, C + alph2 - alph1))
      } else { // y1 == y2
        (max(0.0, alph2 + alph1 - C), min(C, alph2 + alph1))
      }

    if (l == h) {
      println("l == h")
      return None // TODO: use epsilon for floating point number comparison
    }

    val k11 = kerEval.k(i1, i1)
    val k12 = kerEval.k(i1, i2)
    val k22 = kerEval.k(i2, i2)

    val eta = k11 + k22 - 2.0 * k12

    val a2: Real =
      if (0.0 < eta) { // TODO: use epsilon for floating point number comparison
        val ua2 = alph2 + y2 * (e1 - e2) / eta // unbounded a2

        if (ua2 < l) l
        else if (ua2 > h) h
        else ua2
      } else {
        val f1 = y1 * (e1 + b) - alph1 * k11 - s * alph2 * k12
        val f2 = y2 * (e2 + b) - s * alph1 * k12 - alph2 * k22
        val l1 = alph1 + s * (alph2 - l)
        val h1 = alph1 + s * (alph2 - h)
        val lObj = l1 * f1 + l * f2 + 0.5 * l1 * l1 * k11 + 0.5 * l * l * k22 + s * l * l1 * k12
        val hObj = h1 * f1 + h * f2 + 0.5 * h1 * h1 * k11 + 0.5 * h * h * k22 + s * h * h1 * k12

        if (lObj < hObj - eps) l
        else if (hObj + eps < lObj) h
        else alph2
      }

    if (math.abs(a2 - alph2) < eps * (a2 + alph2 + eps)) {
      println("a2 == alph2")
      return None
    }

    val a1 = alph1 + s * (alph2 - a2)

    val b1 = e1 + y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12 + b // update threshold to reflect change in Lagrange multiplier
    val b2 = e2 + y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22 + b

    val ub = // updated b
      if (0.0 < a1 && a1 < C) b1
      else if (0.0 < a2 && a2 < C) b2
      else (b1 + b2) / 2.0

    return Some((a1, a2, ub))
  }

  /**
   * Update the error cache which contains u_i - y_i. This occurs any time a component of \alpha is modified.
   * u_i = w * x_i - b in the linear case
   * w = \sum_{i = 1}^N y_i \alpha_i x_i in the kernel case
   * u = \sum_{j = 1}^N y_j \alpha_j K(x_j, x)
   */
  def computeCache(alpha: DenseVector[Real], b: Real, y: DenseVector[Real], kerEval: KerEval): DenseVector[Real] = {
    val nObs = alpha.length
    val u = DenseVector.zeros[Real](nObs)

    for (i <- 0 to nObs - 1) {
      u(i) = -b
      for (j <- 0 to nObs - 1) {
        u(i) = u(i) + y(j) * alpha(j) * kerEval.k(j, i)
      }
    }

    return u - y
  }
}