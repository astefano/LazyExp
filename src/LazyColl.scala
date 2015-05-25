//adopted & adapted from http://matt.might.net/articles/implementation-of-lazy-list-streams-in-scala/

package org.lazyexp

abstract class LazyColl[+A] {
  def el : A
  def body : LazyColl[A]
  def map[B](f: A => B) : LazyColl[B]
  def filter(p: A => Boolean) : LazyColl[A]
  def take(n: Int) : LazyColl[A]
  def count : Int = 0
}

case object LazyEmpty extends LazyColl[Nothing] {
  def el : Nothing = throw new Exception("LazyEmpty has no elem")
  def body : LazyColl[Nothing] = throw new Exception("LazyEmpty has no body")
  //map, filter, take lazyEmpty returns lazyEmpty
  def map[B](f: Nothing => B) = LazyEmpty
  def filter(p: Nothing => Boolean) = LazyEmpty
  def take(n: Int) = LazyEmpty
}

class LazyCompose[A] (val el : A, _body : => LazyColl[A]) extends LazyColl[A] {
  lazy val body = _body

  def map[B](f: A => B) : LazyColl[B] = new LazyCompose(f(el), body.map(f))

  def filter(p: A => Boolean) : LazyColl[A] = if (p(el)) new LazyCompose(el, body.filter(p)) else body.filter(p)

  def take(n : Int) = if (n > 0) new LazyCompose(el, body.take(n-1)) else LazyEmpty

  override def count = {
    var r = 1
    var copy_body = _body
    while (copy_body != LazyEmpty) {
      r += 1
      copy_body = copy_body.body
    }
    r
  }
}

trait LazyComposable[A] {
  def compose (body : => LazyColl[A]) : LazyColl[A]
}

object LazyCollImplicit {
  implicit def lazyComposable[A] (el : A) : LazyComposable[A] = new LazyComposable[A] {
    def compose (body : => LazyColl[A]) = new LazyCompose(el, body)
  }  
}


object TestLazyCompo {

  import LazyCollImplicit._ 
  
  def main(args : Array[String]) {
    //dummy fct
    def f(x: Int) = x*2
    
    def initFrom(n : Int) : LazyColl[Int] = n compose initFrom(n+1)

    def initBelow(n : Int) : LazyColl[Int] = if (n > 0) n compose initBelow(n-1) else n compose LazyEmpty

    //although initFrom generates an infinite collection, applying map is fine (compared to the map in TestLazyF.scala where we have an overflow)
    val ml = initFrom(3) map (x => x*2)
    println("el of 6, 8, 10,...: " + ml.el)
    println("el of the body of 6, 8, 10, ...: " + ml.body.el)

    val ml2 = 3 compose (1 compose (2 compose LazyEmpty))
    val ml3 = ml2.map (x => x + 5)

    println("el of the collection 3, 1, 2: " + ml2.el)
    println("el of the collection 8, 6, 7: " + ml3.el)
    
    var ml4 = ml.take(20).filter(x => x < 10)
    println("the number of elements in the first 20 even numbers bigger than 4 and smaller than 10  = " + ml4.count)

    val start = System.currentTimeMillis
    val myrdd = initBelow(1000000)
    val r = myrdd.filter(x => x % 3000 == 0).map(x => f(x)).el
    //val res = myrdd.reduce(_+_)
    println("myrdd.count = " + r)    
    println("Time in millis for lazy: " + (System.currentTimeMillis - start))

    //test nonlazy   
    val start2 = System.currentTimeMillis
    val l = (1 to 1000000).toList
    val r2 = l.filter(x => x % 3000 == 0).map(x => f(x)).head
    //val res = myrdd.reduce(_+_)
    println("l.count = " + r2)    
    println("Time in millis for nonlazy: " + (System.currentTimeMillis - start))

    
    
  }
}
