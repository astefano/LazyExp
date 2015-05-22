package org.lazyexp

import org.lazyexp._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class LazyCollTests extends FunSuite with ShouldMatchers {  
    import LazyCollImplicit._ 
    def initFrom(n : Int) : LazyColl[Int] = n compose initFrom(n+1)
    val ml = initFrom(3) map (x => x*2)

    test("test that el of 6, 8, 10,... is 6") { 
      ml.el should equal (6)      
    }

    test("test that el of body of 6, 8, 10,... is 8") { 
      ml.body.el should equal (8)      
    }

    val ml2 = 3 compose (1 compose (2 compose LazyEmpty))
    val ml3 = ml2.map (x => x + 5)

    test("test that el of the collection 3, 1, 2 is 3") {
      ml2.el should equal (3)
    }

    test("test that el of the collection 8, 6, 7 is 8") {
      ml3.el should equal (8)
    }
        
    var ml4 = ml.take(20).filter(x => x < 10)

    test("test that the number of elements in the first 20 even numbers bigger than 4 and smaller than 10 is 2") {
      ml4.count should equal (2)
    }
}
