package rkhs

import breeze.linalg._
import scala.util.{ Try, Success, Failure }

import various.Error
import various.TypeDef._

/**
 * Parsing for instantiation of kernels. As long as the number of combination of data and kernels is low, it is possible to maintain this list of combinations by hand.
 * 
 * TODO: A more automated solution would have to be implemented later. This next solution could use the same interface with nameStr, paramStr and the data, though.
 * TODO: This implementation could use dictionaries for example.
 */
object KerEvalGenerator {
  /**
   * Generate a single var KerEval from a combination of parameter string and data.
   */
  def generateKernelFromParamData(kernelNameStr: String, paramStr: String, dataRow: KerEval.DataRoot, dataCol: KerEval.DataRoot): Try[(Index, Index) => Real] = (dataRow, dataCol) match {
    case (KerEval.DenseVectorReal(dataRow), KerEval.DenseVectorReal(dataCol)) if kernelNameStr == "Linear" => {
      Success(KerEval.generateKerEvalFunc(
        dataRow,
        dataCol,
        Kernel.InnerProduct.linear(
          _: Real,
          _: Real,
          Algebra.R.InnerProductSpace)))
    }

    case (KerEval.DenseVectorReal(dataRow), KerEval.DenseVectorReal(dataCol)) if kernelNameStr == "Gaussian" => {
      Try(paramStr.toReal)
        .flatMap(sd => Error.validate(sd, 0.0 < sd, s"A $kernelNameStr model has a sd parameter value $paramStr. sd should be striclty superior to 0."))
        .map(sd => {
          KerEval.generateKerEvalFunc(
            dataRow,
            dataCol,
            Kernel.InnerProduct.gaussian(
              _: Real,
              _: Real,
              Algebra.R.InnerProductSpace,
              sd))
        })
    }

    case (KerEval.DenseVectorDenseVectorReal(dataRow), KerEval.DenseVectorDenseVectorReal(dataCol)) if kernelNameStr == "Linear" => {
      Success(KerEval.generateKerEvalFunc(
        dataRow,
        dataCol,
        Kernel.InnerProduct.linear(
          _: DenseVector[Real],
          _: DenseVector[Real],
          Algebra.DenseVectorReal.InnerProductSpace)))
    }

    case (KerEval.DenseVectorDenseVectorReal(dataRow), KerEval.DenseVectorDenseVectorReal(dataCol)) if kernelNameStr == "Gaussian" => {
      Try(paramStr.toReal)
        .flatMap(sd => Error.validate(sd, 0.0 < sd, s"A $kernelNameStr model has a sd parameter value $paramStr. sd should be be striclty superior to 0."))
        .map(sd => {
          KerEval.generateKerEvalFunc(
            dataRow,
            dataCol,
            Kernel.Metric.gaussian(
              _: DenseVector[Real],
              _: DenseVector[Real],
              Algebra.DenseVectorReal.MetricSpace,
              sd))
        })
    }

    case (KerEval.DenseVectorDenseMatrixReal(dataRow), KerEval.DenseVectorDenseMatrixReal(dataCol)) if kernelNameStr == "Gaussian" => {
      Try(paramStr.toReal)
        .flatMap(sd => Error.validate(sd, 0.0 < sd, s"A $kernelNameStr model has a sd parameter value $paramStr. sd should be be striclty superior to 0."))
        .map(sd => {
          KerEval.generateKerEvalFunc(
            dataRow,
            dataCol,
            Kernel.Metric.gaussian(
              _: DenseMatrix[Real],
              _: DenseMatrix[Real],
              Algebra.DenseMatrixReal.MetricSpace,
              sd))
        })
    }

    case _ => Failure(new Exception(s"$kernelNameStr kernel is not available for (${dataRow.typeName}, ${dataCol.typeName})"))
  }
}