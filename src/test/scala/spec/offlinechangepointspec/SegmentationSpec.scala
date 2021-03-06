package spec.offlinechangepointspec

import breeze.linalg._
import breeze.stats.distributions._
import org.scalactic._
import org.scalatest._
import rkhs.{ DataRoot, KernelGenerator, KerEval }
import various.TypeDef._
import algo.offlinechangepoint.Segmentation
import org.scalactic.source.Position.apply

/**
 * This class contains functional tests. This means that those tests check entire segmentations.
 */
class SegmentationSpec extends FlatSpec with Matchers {
  "normal" should "compute segmentation for a univariate float signal" in {
    val dMax = 8
    val nPoints = 1000
    val segPoints = Array(0, 250, 500, 750)

    val sampleLawDeterministic = Array[() => Real]( // TODO: use stochastic laws, once the setting of random seed is possible
      () => -10.0,
      () => 10.0,
      () => -10.0,
      () => 10.0)

    val sampleLawsStochastic = {
      val lawA = breeze.stats.distributions.Gaussian(10.0, 0.1)
      val lawB = breeze.stats.distributions.Gaussian(10.0, 1.0)

      Array[() => Real](
        () => lawA.sample,
        () => lawB.sample,
        () => lawA.sample,
        () => lawB.sample)
    }

    val data = Segmentation.generateData(sampleLawDeterministic, nPoints, segPoints)
    val kerEval = KernelGenerator.generateKernelFromParamData("InnerProductSpace.gaussian(0.5)", DataRoot.RealVal(data)).get
    val seg = Segmentation.segment(kerEval, dMax, nPoints, None)

    (segPoints) should ===(seg)
  }

  "matrix" should "compute segmentation for a univariate float signal" in {
    val dMax = 8
    val nPoints = 1000
    val segPoints = Array(0, 250, 500, 750)

    val unitMat = DenseMatrix.ones[Real](3, 3)

    val sampleLawDeterministic = Array[() => DenseMatrix[Real]]( // TODO: use stochastic laws, once the setting of random seed is possible
      () => -10.0 *:* unitMat, // *:* is the element-wise product
      () => 10.0 *:* unitMat,
      () => -10.0 *:* unitMat,
      () => 10.0 *:* unitMat)

    val sampleLawStochastic = {
      val muLaw = breeze.stats.distributions.Uniform(-10.0, 10.0)
      val sdLaw = breeze.stats.distributions.Uniform(0.1, 0.1)

      def matLaw = DenseMatrix.tabulate[breeze.stats.distributions.Gaussian](3, 3)((i, j) => breeze.stats.distributions.Gaussian(muLaw.sample, sdLaw.sample))

      Array
        .fill(4)(matLaw)
        .map(s => () => s.map(_.sample)) // for each segment, generate the function that sample every element of the corresponding matLaw
    }

    val data = Segmentation.generateData(sampleLawDeterministic, nPoints, segPoints)
    val kerEval = KernelGenerator.generateKernelFromParamData("InnerProductSpace.gaussian(0.5)", DataRoot.MatrixReal(data)).get
    val seg = Segmentation.segment(kerEval, dMax, nPoints, None)

    (segPoints) should ===(seg)
  }

  "multiKernel" should "perform a segmentation" in { // TODO: rewrite using Test.segment, like the other test
    val dMax = 8
    val nPoints = 1000
    val segPoints = Array(0, 250, 500, 750)

    val sampleLawDeterministic = Array[() => Real]( // TODO: use stochastic laws, once the setting of random seed is possible
      () => -10.0,
      () => 10.0,
      () => -10.0,
      () => 10.0)

    val sampleLawsStochastic = {
      val lawA = breeze.stats.distributions.Gaussian(10.0, 0.1)
      val lawB = breeze.stats.distributions.Gaussian(10.0, 1.0)

      Array[() => Real](
        () => lawA.sample,
        () => lawB.sample,
        () => lawA.sample,
        () => lawB.sample)
    }

    val data = Segmentation.generateData(sampleLawDeterministic, nPoints, segPoints)
    val kerEval0 = KernelGenerator.generateKernelFromParamData("InnerProductSpace.gaussian(0.5)", DataRoot.RealVal(data)).get
    val kerEval1 = KernelGenerator.generateKernelFromParamData("InnerProductSpace.linear()", DataRoot.RealVal(data)).get
    val kerEval = KerEval.linearCombKerEvalFunc(Array(kerEval0, kerEval1), DenseVector[Real](0.5, 0.5))
    val seg = Segmentation.segment(kerEval, dMax, nPoints, None)

    (segPoints) should ===(seg)
  }

  /**
   * Similar to previous utest, except that here variable descriptions are used, which are similar to what would be used with real data.
   */
  "multiVariable" should "perform a segmentation" in { // TODO: rewrite using Test.segment, like the other test
    val dMax = 8
    val nPoints = 1000
    val segPoints = Array(0, 250, 500, 750)

    val sampleLawDeterministic = Array[() => Real]( // TODO: use stochastic laws, once the setting of random seed is possible
      () => -10.0,
      () => 10.0,
      () => -10.0,
      () => 10.0)

    val sampleLawsStochastic = {
      val lawA = breeze.stats.distributions.Gaussian(10.0, 0.1)
      val lawB = breeze.stats.distributions.Gaussian(10.0, 1.0)

      Array[() => Real](
        () => lawA.sample,
        () => lawB.sample,
        () => lawA.sample,
        () => lawB.sample)
    }

    val data0 = Segmentation.generateData(sampleLawDeterministic, nPoints, segPoints)
    val data1 = Segmentation.generateData(sampleLawDeterministic, nPoints, segPoints)

    val varDescription =
      List(
        new KerEval.KerEvalFuncDescription(0.5, DataRoot.RealVal(data0), "InnerProductSpace.gaussian(0.5)"),
        new KerEval.KerEvalFuncDescription(0.5, DataRoot.RealVal(data1), "InnerProductSpace.linear()"))
    val kerEval = KerEval.multivariateKerEval(varDescription)
    val seg = Segmentation.segment(kerEval.get, dMax, nPoints, None)

    (segPoints) should ===(seg)
  }
}