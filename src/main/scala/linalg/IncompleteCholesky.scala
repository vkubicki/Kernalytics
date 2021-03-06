package linalg

import breeze.linalg._
import breeze.numerics._
import breeze.stats.distributions._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.SortedSet
import scala.util.{ Try, Success, Failure }

import various.TypeDef._

object IncompleteCholesky {
  def icd(kMat: DenseMatrix[Real], m: Index): Try[DenseMatrix[Real]] = {
    val nrow = kMat.rows
    val ncol = kMat.cols

    val g = DenseMatrix.zeros[Real](nrow, m)
    val d = diag(kMat).copy
    val jkSet: SortedSet[Integer] = SortedSet(0 until ncol:_*) // mutable SortedSet
    var jkBuffer = jkSet.toIndexedSeq // a buffer is an indexedSeq with constant access, that could be used for slicing

    for (k <- 0 to m - 1) {
      val ik =
        jkBuffer
          .map(j => (j, d(j)))
          .reduceLeft((p1, p2) => if (p1._2 < p2._2) p2 else p1)
          ._1

      jkSet -= ik //update JK
      jkBuffer = jkSet.toIndexedSeq // update IndexedSet version of jkSet (used for slicing)

      if (d(ik) < 0.0) return Failure(new Exception(s"Low Rank: rank $m is too high and positiveness can not be enforced. Try either a m inferior or equal to ${k + 1}, or use Direct() to avoid low rank approximation completely."))
        
      g(ik, k) = math.sqrt(d(ik))

      val sumTerm = DenseVector.zeros[Real](jkBuffer.size)
      for (j <- 0 to k - 1) {
        sumTerm += g(jkBuffer, j) *:* g(ik, j)
      }

      val scale = 1.0 / g(ik, k)
      g(jkBuffer, k) := scale *:* (kMat(jkBuffer, ik) - sumTerm)

      for (j <- jkBuffer) {
        d(j) = d(j) - g(j, k) * g(j, k)
      }
    }

    return Success(g)
  }

  def icd(nObs: Index, kerEvalFunc: (Index, Index) => Real, m: Index): Try[DenseMatrix[Real]] = {
    val g = DenseMatrix.zeros[Real](nObs, m)
    val d = DenseVector.tabulate[Real](nObs)(i => kerEvalFunc(i, i))
    val jkSet: SortedSet[Integer] = SortedSet(0 until nObs:_*) // mutable SortedSet
    var jkBuffer = jkSet.toIndexedSeq // a buffer is an indexedSeq with constant access, that could be used for slicing

    for (k <- 0 to m - 1) {
      val ik =
        jkBuffer
          .map(j => (j, d(j)))
          .reduceLeft((p1, p2) => if (p1._2 < p2._2) p2 else p1)
          ._1

      jkSet -= ik //update JK
      jkBuffer = jkSet.toIndexedSeq // update IndexedSet version of jkSet (used for slicing)

      if (d(ik) < 0.0) return Failure(new Exception(s"Low Rank: rank $m is too high and positiveness can not be enforced. Try either a m inferior or equal to ${k + 1}, or use Direct() to avoid low rank approximation completely."))
      
      g(ik, k) = math.sqrt(d(ik))

      val sumTerm = DenseVector.zeros[Real](jkBuffer.size)
      for (j <- 0 to k - 1) {
        sumTerm += g(jkBuffer, j) *:* g(ik, j)
      }

      val scale = 1.0 / g(ik, k)

      for (j <- 0 to jkBuffer.size - 1) {
        val absJ = jkBuffer(j)
        g(absJ, k) = scale * (kerEvalFunc(absJ, ik) - sumTerm(j))
      }

      for (j <- jkBuffer) {
        d(j) = d(j) - g(j, k) * g(j, k)
      }
    }

    return Success(g)
  }
}