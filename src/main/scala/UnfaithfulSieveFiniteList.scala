import scala.collection.immutable
import scala.concurrent.duration.Duration
// The Unfaithful Eratosthenes algorithm in 'The Genuine Sieve of Eratosthenes'
// but converted by me to work with finite lists
// :{
// sieve [] = []
// sieve (p : xs) = p : sieve [x | x <- xs, x `mod` p > 0]
// primes n = sieve [2..n]
// :}

//def sieve : List[Int] => List[Int] =
//  case Nil => Nil
//  case p :: xs => p :: sieve(for x <- xs if x % p > 0 yield x)
//
//def primes(n: Int): List[Int] =
//  sieve(List.range(2,n+1))

// same but printing numbers being processed
//def sieve : List[Int] => List[Int] =
//  case Nil => Nil
//  case p :: xs => println(s"new prime is $p"); println(s"number to filter=${xs.size}"); p :: sieve(for x <- xs; _ = print(s"$x,"); if x % p > 0 yield x)
//
//def primes(n: Int): List[Int] =
//  sieve(List.range(2,n+1))

//// My stream-based version
//def sieve : LazyList[Int] => LazyList[Int] =
//  case p #:: xs => p #:: sieve(for x <- xs if x % p > 0 yield x)
//
//def primes = sieve(LazyList.from(2))

// Another version based on the black tiger book
//primes = [x | x <- [2..], divisors x == [x]]
//divisors x = [d | d <- [2..x], x `mod` d == 0]
// Can't work in Scala because the for comprehensions go on forever !!!
//val divisors: Int => LazyList[Int] = x =>
//  for
//    d <- LazyList.from(2);
//    if x % d == 0
//  yield d
//
//def primes =
//  for
//    x <- LazyList.from(2)
//    if divisors(x) == LazyList(x)
//  yield x

//  -----------------------------------------------------------------------------------
//  -- Solution, by Richard Bird, in the Epilogue of 'The Genuine Sieve of Eratosthenes
//  -- e.g. take 10 primes ==> [2,3,5,7,11,13,17,19,23,29]

// https://scastie.scala-lang.org/philipschwarz/Br1EOmXnTCmCC0ZyTQGrPg/17

//def primes: LazyList[Int] =
//  def composites = union { for p <- primes yield multiples(p) }
//  2 #:: minus(LazyList.from(3), composites)
//
//def multiples(n: Int) =
//  LazyList.from(n) map (n * _)
//
//val minus: (LazyList[Int], LazyList[Int]) => LazyList[Int] =
//  case (x #:: xs, y #:: ys) =>
//    if x<y then x #:: minus(xs,y#::ys)
//    else if x==y then minus(xs,ys)
//    else minus(x#::xs,ys)
//
//import cats.{Foldable,Eval}
////import cats.syntax._
//import cats.implicits._
//
//def union(xss: LazyList[LazyList[Int]]): LazyList[Int] =
//  def merge: (LazyList[Int], LazyList[Int]) => LazyList[Int] =
//    case (x #:: xs, y #:: ys) =>
//      if x<y then x #:: merge(xs,y#::ys)
//      else if x==y then x #:: merge(xs,ys)
//      else y #:: merge(x#::xs,ys)
//  // original
////  val xmerge: (LazyList[Int], LazyList[Int]) => LazyList[Int] =
////    case (x #:: xs, ys) => x #:: merge(xs,ys)
//  val xmerge: (LazyList[Int], Eval[LazyList[Int]]) => Eval[LazyList[Int]] =
//    //case (x #:: xs, ys) => Eval.later(x #:: merge(xs,ys.value))
//    case (x #:: xs, ysEval) => Eval.now(x #:: merge(xs,ysEval.value))
//    // doesn't work:
//    //case (x #:: xs, ysEval) => ysEval.map{ x #:: merge(xs,_) }
//  // Here we get StackOverflowError on the call to foldRight
//  //xss.foldRight(LazyList.empty[Int])(xmerge)
//  // so we switch to cats Foldable's foldRight, which is lazy
//  Foldable[LazyList].foldRight(xss,Eval.now(LazyList.empty[Int]))(xmerge).value
//  // multiplesof2 xmerge multiplesof3 xmerge multiplesof5 xmerge ...
//  //xss.foldr(Eval.now(LazyList.empty[Int]))(xmerge).value
//  // I think the next doesn't work because it is not only xss that is infinite,
//  // but also each xs!!!
////  Foldable[LazyList].foldRight(xss,Eval.now(LazyList.empty[Int])){ (xs,ysEval) =>
////    ysEval.map(xmerge(xs,_))
////  }.value

//def eval[A](expression: => A): (A, Duration) =
//  def getTime = System.currentTimeMillis()
//  val startTime = getTime
//  val result = expression
//  val endTime = getTime
//  val duration = endTime - startTime
//  (result, Duration(duration,"ms"))

//@main def main: Unit =
  //  println(s"primes(100)=${primes(100)}")
  //  assert(
  //    primes(100)
  //    ==
  //    List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
  //         43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97))

//  println(s"primes.take(100)=${primes.take(100).toList}")
//  assert(
//    primes.take(100).toList
//      ==
//      List(2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,
//           53,59,61,67,71,73,79,83,89,97,101,103,107,
//           109,113,127,131,137,139,149,151,157,163,
//           167,173,179,181,191,193,197,199,211,223,
//           227,229,233,239,241,251,257,263,269,271,
//           277,281,283,293,307,311,313,317,331,337,
//           347,349,353,359,367,373,379,383,389,397,
//           401,409,419,421,431,433,439,443,449,457,
//           461,463,467,479,487,491,499,503,509,521,
//           523,541))

//   println(s"eval(primes.take(100).toList took ${eval(primes.take(100).toList)(1)}")
//   println(s"eval(primes.take(1,000).toList took ${eval(primes.take(1_000).toList)(1)}")
//   println(s"eval(primes.take(2,000).toList took ${eval(primes.take(2_000).toList)(1)}")
//   println(s"eval(primes.take(5,000).toList took ${eval(primes.take(5_000).toList)(1)}")
//   println(s"eval(primes.take(7,000).toList took ${eval(primes.take(7_000).toList)(1)}")
//   println(s"eval(primes.take(10,000).toList took ${eval(primes.take(10_000).toList)(1)}")
//   println(s"eval(primes.take(100,000).toList took ${eval(primes.take(100_000).toList)(1)}")
   // println(s"eval(primes.take(10000).toList took ${eval(primes.take(10000).toList)(1)}")
   // encounters stack overflow error
   //println(primes.take(10000).toList)
   // primes(3,000)=27457

//   println(s"eval(primes(100))=${eval(primes(100))}")
//   println(s"eval(primes(1,000))=${eval(primes(1_000))}")
//   println(s"eval(primes(3,000))=${eval(primes(3_000))}")
//   println(s"eval(primes(5,000))=${eval(primes(5_000))}")
//   println(s"eval(primes(10,000))=${eval(primes(10_000))}")
//   println(s"eval(primes(20,000))=${eval(primes(20_000))}")
//   println(s"eval(primes(50,000))=${eval(primes(50_000))}")
//   println(s"eval(primes(100,000))=${eval(primes(100_000))}")

//   assert(
//     primes.take(100).toList
//     ==
//     List(  2,   3,   5,   7,  11,  13,  17,  19,  23,  29,
//           31,  37,  41,  43,  47,  53,  59,  61,  67,  71,
//           73,  79,  83,  89,  97, 101, 103, 107, 109, 113,
//          127, 131, 137, 139, 149, 151, 157, 163, 167, 173,
//          179, 181, 191, 193, 197, 199, 211, 223, 227, 229,
//          233, 239, 241, 251, 257, 263, 269, 271, 277, 281,
//          283, 293, 307, 311, 313, 317, 331, 337, 347, 349,
//          353, 359, 367, 373, 379, 383, 389, 397, 401, 409,
//          419, 421, 431, 433, 439, 443, 449, 457, 461, 463,
//          467, 479, 487, 491, 499, 503, 509, 521, 523, 541)
//   )
//
//   println(s"First 100 primes: ${primes.take(100).toList}")   
//
//   List((1_000,7_919), (10_000,104_729), (50_000,611_953), (100_000, 1_299_709), (1_000_000,15_485_863)).foreach { (n,ep) =>
//     val (p,t) = eval(primes(n-1))
//     assert(p == ep)
//     println(s"n=$n; prime=$p time=$t" )
//   }

  //  List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541)
  //  1,000 =>     (7,919,          65 milliseconds)
  //  10,000 =>    (104,729,       406 milliseconds)
  //  50,000 =>    (611,953,      7782 milliseconds)
  //  100,000 =>   (1,299,709,   13880 milliseconds)
  //  1,000,000 => (15,485,863, 384015 milliseconds)

  // this works - prints false
  //  println(Foldable[LazyList].foldRight(true#::true#::LazyList.continually(false),Eval.now(false)){ (x,acc) =>
  //    Eval.now(x && acc.value)
  //  }.value)
//   println((true#::true#::LazyList.continually(false)).foldr(Eval.now(false)){ (x,acc) =>
//     Eval.now(x && acc.value)
//   }.value)
//   println((false#::false#::LazyList.continually(true)).foldr(Eval.now(false)){ (x,acc) =>
//     Eval.now(x || acc.value)
//   }.value)