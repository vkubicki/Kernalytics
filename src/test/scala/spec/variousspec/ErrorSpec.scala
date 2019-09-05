package spec.variousspec

import breeze.linalg._
import scala.util.{ Try, Success, Failure }
import org.scalactic._
import org.scalatest._

import various.TypeDef._
import rkhs.{ DataRoot, KernelGenerator, KerEval }

// http://www.scalatest.org/user_guide/using_matchers

class ErrorSpec extends FlatSpec with Matchers {
  "generateKernel" should "detect an error in parameters provided" in {
    val kernelNameStr = "InnerProductSpace.gaussian(-12.0)"
    val data = DataRoot.RealVal(DenseVector[Real](0.0, 12.0, -5.6))
    
    val res = KernelGenerator.generateKernelFromParamData(kernelNameStr, data)
    
    val mes = res match {
      case Success(_) => "No problem in parameters."
      case Failure(m) => m.toString
    }
    
    mes should === ("java.lang.Exception: A Gaussian model has a sd parameter value -12.0. sd should be strictly superior to 0.")
  }
}