# LazyExp
Summary: small experiments (some reusing existing code on the web) on implementing lazy transformations in Scala. Starting problem: mimic RDDs...

Conventions:
- (adopted from the '12 paper on RDDs https://www.cs.berkeley.edu/~matei/papers/2012/nsdi_spark.pdf) operations are of two types: transformers (like map, filter) and actions (like count, reduce)
- *P* stands for "the problem i considered"


Experiment 0, ToyRDD.scala:
- i considered that a ToyRDD is constructed from a list "obj";
- inside ToyRDD[A], i store a variable "objpar" which is the parallel list corresponding to "obj";
- i have mainly used reflection: all transformations i store as ASTs in a stack until an action is applied at which point first the transformations in the stack are applied and these changes are stored in "objpar".


Experiment 1, TestReflect.scala:
- *P*: i have a list of operations and an object; iterate through the list of ops and if the current op is a transformation, add it to a stack; else, apply the transformations in the stack and after the current action. Use reflection.
- drawback: maybe a bit far-fetched to take ops as a param of run??


Experiment 2, LazyColl.scala, adapted from http://matt.might.net/articles/implementation-of-lazy-list-streams-in-scala/:
- *P*: see collections as being constructed from element and body (for instance, for lists, el is head and body is tail); implement transformations on collections as lazy by using "lazy" for body and call-by-name
- advantage: to get the head the transformations aren't applied on the whole collection
- obs: compared the performance of "taking the head" after a filter and a map for an obj of type LazyColl and the corr. list; as expected, laziness is fully justified :)


Experiment 3, TestViews.scala:
- *P*: implement transformations as lazy by using "view"
- drawback: cannot chain, e.g., lazymap(f).filter(p) because each lazy op returns a SeqView which isn't a ViewColl...


Experiment 4, TestLazy.scala:
- in the same idea as in Experiment 3, lazy list
- small tests on the code from http://stackoverflow.com/questions/23031310/lazy-val-to-implement-lazy-lists-in-scala:
- there, lazy lists are understood by means of cons, uncons; transformations are implemented as lazy by using call- by-name for tail
