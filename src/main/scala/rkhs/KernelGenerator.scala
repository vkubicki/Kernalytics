package rkhs

import various.TypeDef._

import scala.util.{Failure, Try}

/**
 * Parsing for instantiation of kernels. As long as the number of combination of data and kernels is low, it is possible to maintain this list of combinations by hand.
 * 
 * TODO: A more automated solution would have to be implemented later. This next solution could use the same interface with nameStr, paramStr and the data, though.
 * TODO: This implementation could use dictionaries for example.
 */
object KernelGenerator {
  /**
   * Generate a single var KerEval from a combination of parameter string and data.
   */
  def generateKernelFromParamData(kernelStr: String, data: DataRoot): Try[(Index, Index) => Real] = data match {
    case DataRoot.RealVal(data) => AlgebraImplementation.R.getKernel(kernelStr).map(ker => KerEval.generateKerEvalFunc(data, ker))
    case DataRoot.VectorReal(data) => AlgebraImplementation.VectorR.getKernel(kernelStr).map(ker => KerEval.generateKerEvalFunc(data, ker))
    case DataRoot.MatrixReal(data) => AlgebraImplementation.MatrixR.getKernel(kernelStr).map(ker => KerEval.generateKerEvalFunc(data, ker))
  }
}