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

// My stream-based version
//def sieve : LazyList[Int] => LazyList[Int] =
//  case p #:: xs => p #:: sieve(for x <- xs if x % p > 0 yield x)
//
//def primes = sieve(LazyList.from(2))

//  -----------------------------------------------------------------------------------
//  -- Solution, by Richard Bird, in the Epilogue of 'The Genuine Sieve of Eratosthenes
//  -- e.g. take 10 primes ==> [2,3,5,7,11,13,17,19,23,29]

// https://scastie.scala-lang.org/philipschwarz/Br1EOmXnTCmCC0ZyTQGrPg/17

def primes: LazyList[Int] =
  2 #:: minus(LazyList.from(3), composites)

def composites =
  union(for p <- primes yield multiples(p))

def multiples(n: Int) =
  LazyList.from(n).map(n * _)

def minus(left: LazyList[Int], right: LazyList[Int]): LazyList[Int] =
  (left,right) match
    case (x #:: xs, y #:: ys) if x<y => x #:: minus(xs,y#::ys)
    case (x #:: xs, y #:: ys) if x==y => minus(xs,ys)
    case (x #:: xs, y #:: ys) if x>y => minus(x#::xs,ys)

import cats._
import cats.implicits._

def union(xss: LazyList[LazyList[Int]]): LazyList[Int] =
  // Here we get StackOverflowError on the call to foldRight
  //xss.foldRight(LazyList.empty[Int])(merge)
  // so we switch to cats Foldable's foldRight, which is lazy
  Foldable[LazyList].foldRight(xss,Eval.now(LazyList.empty[Int]))(merge).value

// original
//val merge: (LazyList[Int], LazyList[Int]) => LazyList[Int] =
//  case (x #:: xs, ys) => x #:: mergex(xs,ys)

val merge: (LazyList[Int], Eval[LazyList[Int]]) => Eval[LazyList[Int]] =
  case (x #:: xs, ys) => Eval.later(x #:: mergex(xs,ys.value))

def mergex(left: LazyList[Int], right: LazyList[Int]): LazyList[Int] =
  (left,right) match
    case (x #:: xs, y #:: ys) if x<y => x #:: mergex(xs,y#::ys)
    case (x #:: xs, y #:: ys) if x==y => x #:: mergex(xs,ys)
    case (x #:: xs, y #:: ys) if x>y => y #:: mergex(x#::xs,ys)

def eval[A](expression: => A): (A, Duration) =
  def getTime = System.currentTimeMillis()
  val startTime = getTime
  val result = expression
  val endTime = getTime
  val duration = endTime - startTime
  (result, Duration(duration,"ms"))

@main def main: Unit =
  //  println(s"primes(100)=${primes(100)}")
  //  assert(
  //    primes(100)
  //    ==
  //    List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
  //         43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97))

  println(s"primes.take(100)=${primes.take(100).toList}")
  assert(
    primes.take(100).toList
      ==
      List(2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,
           53,59,61,67,71,73,79,83,89,97,101,103,107,
           109,113,127,131,137,139,149,151,157,163,
           167,173,179,181,191,193,197,199,211,223,
           227,229,233,239,241,251,257,263,269,271,
           277,281,283,293,307,311,313,317,331,337,
           347,349,353,359,367,373,379,383,389,397,
           401,409,419,421,431,433,439,443,449,457,
           461,463,467,479,487,491,499,503,509,521,
           523,541))

   println(s"eval(primes.take(100).toList took ${eval(primes.take(100).toList)(1)}")
   println(s"eval(primes.take(1,000).toList took ${eval(primes.take(1_000).toList)(1)}")
   println(s"eval(primes.take(10,000).toList took ${eval(primes.take(10_000).toList)(1)}")
   println(s"eval(primes.take(100,000).toList took ${eval(primes.take(100_000).toList)(1)}")
   // println(s"eval(primes.take(10000).toList took ${eval(primes.take(10000).toList)(1)}")
   // encounters stack overflow error
   //println(primes.take(10000).toList)
   // primes(3,000)=27457
   println(s"primes(3,000)=${primes(3_000)}")
   //primes(4,000)=37831
   println(s"primes(4,000)=${primes(4_000)}")
   // encounters stack overflow error
   //primes(5,000)=48619
   println(s"primes(5,000)=${primes(5_000)}")
   // primes(100,000)=1299721
   println(s"primes(100,000)=${primes(100_000)}")