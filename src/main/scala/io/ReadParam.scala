package io

import breeze.linalg._
import scala.io.Source
import scala.util.{Try, Success, Failure}

import rkhs.KerEval
import various.Def
import various.TypeDef._

/**
 * Descriptor column format:
 * - name
 * - weight
 * - kernel (including parameters)
 */
object ReadParam {
  class ParsedParam(val name: String, val weight: Real, val kernel: String)
  
  def readAndParseParam(fileName: String): Try[Array[ParsedParam]] =
    readParams(fileName)
    .flatMap(a => a.foldLeft[Try[List[ParsedParam]]](Success[List[ParsedParam]](Nil))((acc, e) => acc.flatMap(l => parseIndividualParam(e).map(r => r :: l)))) // first error in parsing ends the parsing, because the flaMap passes the error on.
    .map(l => l.reverse.toArray) // map to reverse the list and transform it to an Array, iff all the parsing were correct
    
  /**
   * Format the params var by var, without parsing the individual values.
   */
  def readParams(fileName: String): Try[Array[Array[String]]] =
    Try(
        Source
        .fromFile(fileName)
        .getLines
        .toArray
        .map(_.split(Def.csvSep))
        .transpose)
  
  def parseIndividualParam(v: Array[String]): Try[ParsedParam] = {
    if (v.size != 3)
      return Failure(new Exception("In descriptor file, all variables must have three lines: name, weight and kernel."))
       
    val varName = v(0)
    val weightStr = v(1)
    val paramStr = v(2)
    
    return Try(weightStr.toReal).map(w => new ParsedParam(v(0), w, paramStr))
  }
}