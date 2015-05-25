package org.lazyexp

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
import scala.reflect.runtime.universe._
import scala.collection.mutable.Stack
import scala.collection.parallel.immutable._

case class ToyRDD[A](obj: List[A]) {
  //make obj parallel
  var objpar = obj.par

  val tb = currentMirror.mkToolBox()

  //toylineage is a stack storing the transformations to be applied to obj
  val toylineage = Stack[Tree]()

  def map[B](f: A => B) = {
    //update lineage
    toylineage.push(reify(objpar.map(f)).tree)
  }

  def filter(p: A => Boolean) = {
    //update lineage
    toylineage.push(reify(objpar.filter(p)).tree)
  }

  //apply all transformation stored in the lineage
  def appLineage = {    
    while (! toylineage.isEmpty) {
      val transf = toylineage.pop()
      //ugly for the moment, don't know how i could avoid asInstanceOf...
      objpar = tb.eval(transf).asInstanceOf[ParSeq[A]]
    } 
  }
  
  def count = {
    appLineage
    objpar.length
  }

  def collect = {
    appLineage
    objpar
  }

  def reduce[A1 >: A](f: (A1, A1)  => A1) = {
    appLineage
    objpar.reduce(f)
  }
}


object TestToyRDD {

  val tb = currentMirror.mkToolBox()
  
  //dummy fct
  def f(x: Int) = x*2

  def main(args: Array[String]) {
    
    val myrdd = ToyRDD((1 to 1000000).toList)
    myrdd.filter(x => x % 3000 == 0)
    myrdd.map(x => f(x))
    println("myrdd.count = " + myrdd.reduce(_+_))    
  }
}
