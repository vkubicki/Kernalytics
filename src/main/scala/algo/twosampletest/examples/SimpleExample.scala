package algo.twosampletest.examples

import breeze.linalg._
import breeze.stats.distributions._
import rkhs.{ AlgebraImplementation, Gram}
import algo.twosampletest.Base

object SimpleExample {
  def main {
    val nA = 20
    val nB = 30
    val kernelSd = 1.0
    val nSample = 1000
    val alpha = 0.05

    val data = generateData(nA, nB) // generation of data
    val part = new Base.Partition((0 to nA - 1).toArray, (nA to nA + nB - 1).toArray)

    val kernel = AlgebraImplementation.R.getKernel(s"InnerProductSpace.gaussian($kernelSd)").get
      
    val gram = Gram.generate(data, kernel)

    val k = Base.permutationTestCriticalValue(nSample, nA, nB, alpha, gram) // get critical value for the hypothesis testing
    val p = Base.mmdUnbiasedEstimator(gram, part)

    println(s"k: $k, p: $p")
  }

  /**
   * Generate data as a contiguous vector.
   * http://www.shogun-toolbox.org/notebook/latest/mmd_two_sample_testing.html#Running-Example-Data.-Gaussian-vs.-Laplace
   *
   * @param nA first elements generated using the first law
   * @param nB last elements generated using the second law
   */
  def generateData(nA: Int, nB: Int): DenseVector[Double] = {
    val distA = new Gaussian(0.0, 1.0)
    val distB = new Gaussian(1.0, 1.0)

    DenseVector.tabulate[Double](nA + nB)(i => if (i < nA) distA.sample else distB.sample)
  }
}