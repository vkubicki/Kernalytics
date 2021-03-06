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
  
  def parseIndividualParam(v: Array[String]): Try[ParsedParam] =
    for {
      _ <- checkSize(v)
      w <- Try(v(1).toReal)
    } yield new ParsedParam(v(0), w, v(2))
  
  def checkSize(v: Array[String]): Try[Array[String]] =
    if (v.size != 3)
      Failure(new Exception("In descriptor file, all variables must have three lines: name, weight and kernel."))
    else
      Success(v)
  
  /**
   * Parse the parameter string to extract both the kernel name and the parameters
   */
  def parseParam(str: String): Try[(String, String)] = {
    val paramPattern = raw"([a-zA-Z0-9]+)\((.*)\)".r
    val t = Try({ val t = paramPattern.findAllIn(str); (t.group(1), t.group(2)) })

    t match {
      case Success(_) => t
      case Failure(_) => Failure(new Exception(str + " is not a valid parameter String")) // default exception for pattern matching is not expressive enough
    }
  }

  def parseKernel(str: String): Try[(String, String, String)] = {
    val paramPattern = raw"(\w+)\.(\w+)\(([+-a-zA-Z0-9.]*)\)".r

    str match {
      case paramPattern(structure, kernel, param) => Success((structure, kernel, param))
      case _ => Failure(new Exception(s"$str is not a valid parameter string. The general pattern is Structure.kernel(param)."))
    }
  }

  def parseTwoIntegers(str: String): Try[(Integer, Integer)] = {
    val paramPattern = raw" *([0-9]+) *, *([0-9]+)".r
    val t = Try({ val t = paramPattern.findAllIn(str); (t.group(1).toInteger, t.group(2).toInteger) })

    t match {
      case Success(_) => t
      case Failure(_) => Failure(new Exception(str + " should consist of two integers separated by a comma")) // default exception for pattern matching is not expressive enough
    }
  }

  def parseRealInteger(str: String): Try[(Real, Integer)] = {
    val paramPattern = raw" *([0-9.]+) *, *([0-9]+)".r
    val t = Try({ val t = paramPattern.findAllIn(str); (t.group(1).toReal, t.group(2).toInteger) })

    t match {
      case Success(_) => t
      case Failure(_) => Failure(new Exception(str + " should consist of two integers separated by a comma")) // default exception for pattern matching is not expressive enough
    }
  }
}