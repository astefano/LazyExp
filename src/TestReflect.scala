package org.lazyexp

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
import scala.reflect.runtime.universe._
import scala.collection.mutable.Stack

object TestReflect {

  val tb = currentMirror.mkToolBox()
  
  //dummy fct
  def f(x: Int) = x*x

  /** @return true if `t` corresponds to the application of a map or of a filter
    */ 
  def isTransf(t: Tree) = {
    val st = showRaw(t)
    st.contains("map") || st.contains("filter")
  }

  var concreteList = (1 to 10).toList

  //using a closure
  def run(ops: List[Tree]) = {
    val appLazyStack = Stack[Tree]()
    ops foreach {
      op => 
	if (isTransf(op))
	  appLazyStack.push(op)
	else {
	  //we found an action; app all transformations in appLazyStack and then the action itself
	  while (! appLazyStack.isEmpty) {
	    val transf = appLazyStack.pop()
	    //ugly for the moment (should use match instead)
	    concreteList = tb.eval(transf).asInstanceOf[List[Int]]
	  }
	  concreteList = List(tb.eval(op).asInstanceOf[Int])
	}
    }
    println(s"out:$concreteList")
  }
  

  def main(args: Array[String]) {
    //short warm-up
    //go up one level, build the AST of f
    val ft = reify(f(4)).tree
    println(ft)
    //go down, eval the tree
    val ftv = tb.eval(ft)
    println("eval:" + ftv)

    val metaops = List(reify(concreteList.map(x => f(x))).tree, reify(concreteList.filter(x => x % 2 == 0)).tree, reify(concreteList.reduce(_+_)).tree)
    run(metaops)    
  }
}
