# LazyExp
small experiments (some reusing existing code on the web) on implementing lazy transformations in Scala 

Convention (adopted from the first paper on RDDs): 
- operations are of two types: transformers (like map, filter) and actions (like count, reduce)
- *P* stands for "the problem i considered"

First idea, TestReflect.scala:
- *P*: i have a list of operations and an object; iterate through the list of ops and if the current op is a transformation, add it to a stack; else, apply the transformations in the stack and after the current action. 
- to effectively implement this, my first thought was to use reflection (i had a similar but considerably smaller problem during my phd; at that time, i used the meta-level in Maude to solve it).


Experiment 2, LazyCollF.scala, adapted from http://matt.might.net/articles/implementation-of-lazy-list-streams-in-scala/:
- *P*: see collections as being constructed from element and body (for instance, for lists, el is head and body is tail); implement transformations on collections as lazy by using "lazy" for body and call by name 


Experiment 3, ViewColl.scala:
- *P*: implement transformations as lazy by using "view"
- drawback: cannot chain, e.g., lazymap(f).filter(p) because each lazy op returns a SeqView which isn't a ViewColl... 


TestLazyF.scala contains the code from http://stackoverflow.com/questions/23031310/lazy-val-to-implement-lazy-lists-in-scala: 
- there, lazy lists are understood by means of cons, uncons; transformations are implemented as lazy by using call by name for tail

