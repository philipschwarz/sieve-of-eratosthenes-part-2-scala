
import cats.{Eval, Foldable}, cats.implicits.*

import scala.concurrent.duration.Duration

def primes: LazyList[Int] =

  def composites = union { for p <- primes yield multiples(p) }

  2 #:: minus(LazyList.from(3), composites)

def multiples(n: Int): LazyList[Int] =
  LazyList.from(n) map (n * _)

val minus: (LazyList[Int], LazyList[Int]) => LazyList[Int] =
  case (x #:: xs, y #:: ys) =>
    if x<y then x #:: minus(xs,y#::ys)
    else if x==y then minus(xs,ys)
    else minus(x#::xs,ys)

def union(xss: LazyList[LazyList[Int]]): LazyList[Int] =

  def merge: (LazyList[Int], LazyList[Int]) => LazyList[Int] =
    case (x #:: xs, y #:: ys) =>
      if x<y then x #:: merge(xs,y#::ys)
      else if x==y then x #:: merge(xs,ys)
      else y #:: merge(x#::xs,ys)

  val xmerge: (LazyList[Int], Eval[LazyList[Int]]) => Eval[LazyList[Int]] =
    case (x #:: xs, ysEval) => Eval.now(x #:: merge(xs,ysEval.value))

  xss.foldr(Eval.now(LazyList.empty[Int]))(xmerge).value

def eval[A](expression: => A): (A, Duration) =
  def getTime = System.currentTimeMillis()
  val startTime = getTime
  val result = expression
  val endTime = getTime
  val duration = endTime - startTime
  (result, Duration(duration,"ms"))

@main def main(): Unit =

  assert(
    primes.take(100).toList
    ==
    List(   2,   3,   5,   7,  11,  13,  17,  19,  23,  29,
           31,  37,  41,  43,  47,  53,  59,  61,  67,  71,
           73,  79,  83,  89,  97, 101, 103, 107, 109, 113,
          127, 131, 137, 139, 149, 151, 157, 163, 167, 173,
          179, 181, 191, 193, 197, 199, 211, 223, 227, 229,
          233, 239, 241, 251, 257, 263, 269, 271, 277, 281,
          283, 293, 307, 311, 313, 317, 331, 337, 347, 349,
          353, 359, 367, 373, 379, 383, 389, 397, 401, 409,
          419, 421, 431, 433, 439, 443, 449, 457, 461, 463,
          467, 479, 487, 491, 499, 503, 509, 521, 523, 541)
  )

  println(s"First 100 primes: ${primes.take(100).toList}")

  List((1_000, 7_919), (10_000, 104_729), (50_000, 611_953), (100_000, 1_299_709), (1_000_000, 15_485_863)).foreach { (n,expectedPrime) =>
    val (prime,time) = eval(primes(n-1))
    assert(prime == expectedPrime)
    println(s"n=$n; prime=$prime; time=$time" )
  }