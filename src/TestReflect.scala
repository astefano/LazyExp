package org.lazyexp

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
import scala.reflect.runtime.universe._
import scala.collection.mutable.Stack

object TestReflect {

  val tb = currentMirror.mkToolBox()
  
  //dummy fct
  def f(x: Int) = x*x

  /** @return true if the `t` corresponds to the application of a map or of a filter
    */ 
  def isTransf(st: String) = st.contains("map") || st.contains("filter")  

  var concreteList = (1 to 10).toList
  
  /**@param: ops is a list of ASTs corr. to operations like map, filter, reduce
    * uses a closure (i.e., runFromTrees calls concreteList)
    * operations are either transformations or actions;
    * transformations are stored in a stack till an action appears at which time
    * the transformations are applied on concreteList and then the action;
    * pitfall 1: for simplicity, the result of the action is assumed to be Int, and wrapped in a new list to be assigned to concreteList s.t. the computation continues.
    * pitfall 2: concreteList is hard-wired
    * @return: the unchanged list if ops doesn't contain an action; owise, a list as resulting after applying all operations in ops. 
    */ 
  def runFromTrees[A](ops: List[Tree]) = {
    val appLazyStack = Stack[Tree]()
    ops foreach {
      op => 
	//if op is a transformation store it in the stack for later evaluation
	if (isTransf(showRaw(op)))
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
    println(s"[runFromTrees]:$concreteList")
  }

  //same as run but no trees and no closure
  //pitfall wrt runFromTrees: it doesn't work for map(x => f(x)), exception: "not found: value f"
  //advantage wrt runFromTrees: no hard-wired concreteList, the object to be transformed is passed as a param `l`
  def runFromStrs(ops: List[String], l: List[Int]) = {
    val appLazyStack = Stack[String]()
    var outl = l
    ops foreach {
      op => 
	if (isTransf(op))
	  appLazyStack.push(op)
	else {
	  //we found an action; app all transformations in appLazyStack and then the action itself
	  while (! appLazyStack.isEmpty) {
	    val transf = appLazyStack.pop()	    
	    //toEval is the AST the transf on l
	    //e.g., if l is List(1, 2) and transf is "map(x => x*2)" toEval is the AST corr. to "List(1,2).map(x => x*2)"
	    val toEval = tb.parse(outl.toString + "." + transf)
	    //evaluate the AST corr. to toEval
	    val res = tb.eval(toEval)
	    res match {
	      case x: List[Int] => 
		//ugly for the moment, don't know how i could avoid asInstanceOf
		outl = res.asInstanceOf[List[Int]]
	      case _ => println("[runFromStrs]: Err: the result of $transf isn't a List[Int] as expected.")
	    }
	  }
	  val res = tb.eval(tb.parse(outl.toString + "." + op))
	  if (res.isInstanceOf[Int])
	    outl = List(res.asInstanceOf[Int])
	}
    }
    println(s"[runFromStrs]:$outl")
  }
  

  def main(args: Array[String]) {
    //short warm-up for meta ops
    //go up one level, build the AST of f
    val ft = reify(f(4)).tree
    println(ft)
    //go down, eval the tree
    val ftv = tb.eval(ft)
    println("eval:" + ftv)

    //i tried to use q"""...""" instead but then eval is unsuccessful
    val metaops = List(reify(concreteList.map(x => f(x))).tree, reify(concreteList.filter(x => x % 2 == 0)).tree, reify(concreteList.reduce(_+_)).tree)
    runFromTrees(metaops)    

    val strops = List("map(x => 2*x)", "filter(x => x % 3 == 0)", "map(x => x - 1)", "reduce(_+_)")
    runFromStrs(strops, (1 to 20).toList)

  }
}
