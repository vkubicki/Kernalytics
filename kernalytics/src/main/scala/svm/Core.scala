package svm

import breeze.linalg._

import various.TypeDef._

/**
 * Core implementation of the algorithm. Note that it is not (for now) written in the functional style, as it is a direct implementation of the paper:
 * Sequential Minimal Optimization, John C. Platt, 1998
 *
 * TODO: for debug, export the cost value at each iteration, to ensure that it decreases properly
 *
 * Algorithm ends when no Lagrange multipliers have been modified.
 */
object Core {
  val tol: Real = 1.0e-3

  def optimize(nObs: Index, kerEval: (Index, Index) => Real, y: DenseVector[Real], C: Real): DenseVector[Real] = {
    val alpha = DenseVector.zeros[Real](nObs) // DenseVector are mutable, no need for a val
    var b: Real = 0.0 // threshold
    var cached = updateCache(alpha, b, y, nObs, kerEval) // cached reference is modified by updateCache, hence the var

    var numChanged: Index = 0
    var examineAll = true

    while (numChanged > 0 || examineAll) { // as long as some coefficients where changed in the last iteration, or that a complete examination is required
      numChanged = 0
      if (examineAll) {
        for (i <- 0 to nObs - 1) {
          numChanged += examineExample(i, alpha, y, C, cached, kerEval)
        }
      } else { // Only examine the bound observations (observation for which a constraint is active)
        for (i <- 0 to nObs - 1) {
          if (tol < alpha(i) || alpha(i) < C - tol) {
            numChanged += examineExample(i, alpha, y, C, cached, kerEval)
          }
        }
      }

      if (examineAll == true) {
        examineAll = false
      } else if (numChanged == 0) { // if none were changed, that could mean that the heuristic could not find good candidates anymore, hence it is necessary to perform an exhaustive examination
        examineAll = true
      }
    }

    return alpha
  }

  /**
   * Update the error cache which contains u_i - y_i. This occurs any time a component of \alpha is modified.
   * u_i = w * x_i - b in the linear case
   * w = \sum_{i = 1}^N y_i \alpha_i x_i in the kernel case
   * u = \sum_{j = 1}^N y_j \alpha_j K(x_j, x)
   */
  def updateCache(alpha: DenseVector[Real], b: Real, y: DenseVector[Real], nObs: Index, kerEval: (Index, Index) => Real): DenseVector[Real] = {
    var e = DenseVector.zeros[Real](nObs)
    for (i <- 0 to nObs - 1) {
      var sum = 0.0
      for (j <- 0 to nObs - 1) {
        sum += y(j) * alpha(j) * kerEval(j, i)
      }
      e(i) = sum - b
    }

    return e
  }

  /**
   * Use the KKT violation as a criterium for modification.
   *
   * @param i index of the Lagrange multiplier to optimize
   * @return 1 if at least one Lagrange multiplier has been modified
   */
  def examineExample(i2: Index, alpha: DenseVector[Real], y: DenseVector[Real], C: Real, cached: DenseVector[Real], kerEval: (Index, Index) => Real): Index = {
    val nObs = alpha.length

    val y2 = y(i2)
    val alph2 = alpha(i2)
    val E2 = cached(i2)
    val r2 = E2 * y2

    if ((r2 < -tol && alph2 < C) || (r2 > tol && alph2 > 0)) { // KKT
      val randomIndices = randomNonBoundIndices(alpha, C)

      if (randomIndices._1.length > 1) {
        val i1 = secondChoiceHeuristic(E2, cached)
        return takeStep(i1, i2, alpha, y, C, cached, kerEval)
      }

      for (i1 <- 0 to randomIndices._1.length - 1) { // loop over all non-zero and non-C alpha, starting at random point
        return takeStep(i1, i2, alpha, y, C, cached, kerEval)
      }

      for (i1 <- 0 to nObs - 1) { // loop over all non-zero and non-C alpha, starting at random point TODO: there is no need to loop over the previously discarded non bound cases
        return takeStep(i1, i2, alpha, y, C, cached, kerEval)
      }
    }

    return 0 // no second Lagrange multiplier was enough candidate for the heuristic
  }

  /**
   * Step size used in take step is approximated by |E 1 - E2|. The heuristic selects the second candidate for which the step size is maximal.
   */
  def secondChoiceHeuristic(E2: Real, cached: DenseVector[Real]): Index = {
    val nObs = cached.length
    val dis = cached.map(E1 => math.abs(E2 - E1))

    return argmax(dis)
  }

  /**
   * Get the indices of non-bound Lagrange multiplier, in a random order to avoid bias in the algorithm.
   */
  def randomNonBoundIndices(alpha: DenseVector[Real], C: Real): (IndexedSeq[Index], IndexedSeq[Index]) = {
    val nObs = alpha.length
    val all = util.Random.shuffle(0 to nObs - 1)
    return (all.filter(i => tol > alpha(i) || alpha(i) < C - tol), all)
  }

  def takeStep(i1: Index, i2: Index, alpha: DenseVector[Real], y: DenseVector[Real], C: Real, cached: DenseVector[Real], kerEval: (Index, Index) => Real): Index = {
    if (i1 == i2) return 0
    val alph1 = alpha(i1)
    val alph2 = alpha(i2)
    val y1 = y(i1)
    val y2 = y(i2)

    val E1 = cached(i1)
    val E2 = cached(i2)

    val s = y1 * y2

    val (l, h) = if (y1 != y2) { // uppercases for l and h forbidden by Scala syntax: https://stackoverflow.com/questions/12636972/how-to-pattern-match-into-an-uppercase-variable
      (max(0.0, alph2 - alph1), min(C, C + alph2 - alph1))
    } else {
      (max(0.0, alph2 + alph1 - C), min(C, alph2 + alph1))
    }

    if (math.abs(l - h) < tol) return 0

    val k11 = kerEval(i1, i1)
    val k12 = kerEval(i1, i2)
    val k22 = kerEval(i2, i2)

    val eta = k11 + k22 - 2 * k12

    var a2: Real = 0.0 // use of a var to have a syntax similar to the article

    if (eta > 0.0) {
      a2 = alph2 + y2 * (E1 - E2) / eta
      if (a2 < l) {
        a2 = l
      } else if (a2 > h) {
        a2 = h
      }
    } else {
      //     val f1 = y1*(E1 + b) -
    }

    ???
  }
}