/* example taken from the first proposal at: http://stackoverflow.com/questions/23031310/lazy-val-to-implement-lazy-lists-in-scala?rq=1
*/

package org.lazyexp
 
object LazyList {

  def empty[A] : LazyList[A] = new LazyList[A] {
    lazy val uncons = None
  }

  def cons[A](h : => A, t : => LazyList[A]) : LazyList[A] = new LazyList[A] {
    println("[cons]: h = " + h + " t = " + t)
    lazy val uncons = Some( (h,t) )
  }

  def from(s : Int) : LazyList[Int] = new LazyList[Int] {
    lazy val uncons = Some( (s, from(s + 1)) )
  }
}

trait LazyList[A] {

  import LazyList._

  def uncons : Option[(A,LazyList[A])]

  def fmap[B](f : A => B) : LazyList[B] = uncons match {
    case None          => empty
    case Some( (h,t) ) => cons(f(h),t.fmap(f))
  }

  def take(i : Int) : LazyList[A] = {
    println("[take]: i = " + i + " uncons = " + uncons)
    uncons match {
    case None          => empty
    case Some( (h,t) ) => if (i <= 0) empty else cons(h,t.take(i - 1))
    }
  }

  override def toString : String = uncons match {
    case None          => "[]"
    case Some( (h,t) ) => "[" ++ h.toString ++ ",..]"
  }

  def toList : List[A] = uncons match {
    case None          => List[A]()
    case Some( (h,t) ) => h :: t.toList
  }
}

/* the code corr. to the answer */

object LazyList2 {

  def empty[A] : LazyList2[A] = new LazyList2[A] {
    lazy val uncons = None
  }

  def cons[A](h : => A, t : => LazyList2[A]) : LazyList2[A] = new LazyList2[A] {
    println("[cons2]: h = " + h + " t = " + t)
    lazy val uncons = Some( (h, () => t) )
  }

  def from(s : Int) : LazyList2[Int] = new LazyList2[Int] {
    lazy val uncons = Some( (s, () => from(s + 1)) )
  }
}

trait LazyList2[A] {

  import LazyList2._

  // main chg wrt LazyList: call by name !
  def uncons : Option[(A, () => LazyList2[A])]

  def fmap[B](f : A => B) : LazyList2[B] = uncons match {
    case None          => empty
    case Some( (h,t) ) => cons(f(h),t().fmap(f))
  }

  def take(i : Int) : LazyList2[A] = {
    println("[take2]: i = " + i + " uncons = " + uncons)
    uncons match {
    case None          => empty
    case Some( (h,t) ) => if (i <= 0) empty else cons(h,t().take(i - 1))
    }
  }

  override def toString : String = uncons match {
    case None          => "[]"
    case Some( (h,t) ) => "[" ++ h.toString ++ ",..]"
  }

  def toList : List[A] = uncons match {
    case None          => List[A]()
    case Some( (h,t) ) => h :: t().toList
  }
}

object TestLazyF extends App {
  val lazyl = LazyList from 8 take(3)
  //weird behaviour: take is printed more times than needed, cons is printed too little times, and the return is wrong 
  println(lazyl)
  val l = lazyl.toList
  println("l = " + l)

  val lazyl2 = (LazyList2 from 8).take(3)
  println(lazyl2)
  val l2 = lazyl2.toList
  println("l2 = " + l2)

/* overflow :) fmap never stops! 
  val lazylM = LazyList2 from 8 fmap(x => 2*x) take(3)
  println(lazylM)
  val lM = lazylM.toList
  println("lM = " + lM)
*/

  //first make it finite and then map
  val lazylM = LazyList2 from 8 take(3) fmap(x => 2*x)
  println(lazylM)
  val lM = lazylM.toList
  println("lM = " + lM)

/*
  val lazylM2 = (LazyList2 from 8).fmap(x => 2*x).take(3)
  println(lazylM2)
  val lM2 = lazylM2.toList
  println("lM2 = " + lM2)
*/
}

