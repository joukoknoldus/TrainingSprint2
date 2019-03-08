object Problems {

  def main(args: Array[String]): Unit = {
    val l: List[Int]=List(1, 1, 2, 3, 5, 8)

    val unflattened1: List[Any]=List(List(1, 1), 2, List(3, List(5, 8)))
    val unflattened2: List[Any]=List(List(1, 1), 2)
    val unflattened3: List[Any]=List(List(1, 1))
    val unflattened4: List[Any]=List(List(1))
    val unflattened5: List[Any]=List(2, List(3, List(5, 8)))
    val unflattened6: List[Any]=List(List(List(1)))
    println(last(l))
    println(penultimate(l))
    println()
    println(flatten(unflattened1))
    println()
    println(flatten(unflattened2))
    println()
    println(flatten(unflattened3))
    println()
    println(flatten(unflattened4))
    println()
    println(unflattened5)
    println(flatten(unflattened5))
    println()
    println(unflattened6)
    println(flatten(unflattened6))

    val dups=List('a', 'a', 'a', 'a', 'b', 'c', 'c', 'a', 'a', 'd', 'e', 'e', 'e', 'e')
    println(compress(dups))
    println(pack(dups))
    println(encodeModified(dups))
    println(decode(encodeModified(dups)))
    println(encodeDirect(dups))

    val li=List('a', 'b', 'c', 'c', 'd')
    println(duplicate(li))

    println(duplicateN(3, li))

    val toK=List('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k')
    println(drop(3, toK))

    println(split(3, toK))

    println(slice(3, 7, toK))

    println(rotate(3, toK))
    println(rotate(-2, toK))

    val abcd=List('a', 'b', 'c', 'd')
    println(removeAt(1, abcd))
  }

  def last[A](l: List[A]): A = {
    l match {
      case x :: Nil => x
      case x :: tail => last(tail)
      case Nil => throw new NoSuchElementException
    }
  }

  def penultimate[A](l: List[A]): A = {
      if (l.isEmpty) { throw new NoSuchElementException }
      else if (l.tail.isEmpty ) { throw new NoSuchElementException }
      else if (l.tail.tail.isEmpty) { l.head }
      else { penultimate(l.tail) }
  }

  def nth[A](n: Int, l: List[A]): A = {
    if (l.isEmpty) { throw new NoSuchElementException }
    else if (n==0) { l.head }
    else { nth(n-1, l.tail ) }
  }

  def length[A](l: List[A]): Int = {
    if (l.isEmpty) { 0 }
    else { 1 + length(l.tail) }
  }

  def reverse[A](l: List[A]): List[A] = {
    l.last::reverse(l.tail)
  }

  def isPalindrome[A](l: List[A]): Boolean = {
    l==reverse(l)
  }

  def flatten(l: Any): List[Any] = {
    l match {
      case List(x::y) =>  flatten(x::y)
      case List(x::y, z) => flatten(x::y):::List(z)
      case List(z, x::y) => List(z):::flatten(x::y)
      case x::Nil => List(x)
      case x::y => flatten(x):::flatten(y)
      case y => List(y)
    }
  }

  def compress[A](l: List[A]): List[A] = {
    if (l.isEmpty) { l }
    else if (l.tail.isEmpty) { l }
    else if ( l.head==l.tail.head ) { compress(l.tail) }
    else { l.head::compress(l.tail) }
  }

  def combineFirstTwo[A](l: List[List[A]]): List[List[A]] = {
    if (l.length>1) {
      (l.head:::l.tail.head)::l.tail.tail
    }
    else l
  }

  def pack[A](l: List[A]): List[List[A]] = {
    if (l.isEmpty) { List(l) }
    else if (l.tail.isEmpty) { List(l) }
    else if ( l.head==l.tail.head ) { val temp=List(l.head)::pack(l.tail) ; combineFirstTwo(temp) }
    else { List(l.head)::pack(l.tail) }
  }

  def encodeModified[A](l: List[A]): List[(Int, A)] = {
    val packed=pack(l)
    packed.map( x => (x.length, x.head) )
  }

  def decodeOne[A](x: A, n: Int): List[A] = {
    if (n==0) { List() }
    else { x::decodeOne(x, n-1) }
  }

  def decode[A](l: List[(Int, A)]): List[A] = {
    if (l.isEmpty) { List() }
    else { decodeOne(l.head._2, l.head._1):::decode(l.tail) }
  }

  def combineFirstTwoDirect[A](l: List[(Int, A)]): List[(Int, A)] = {
    if (l.length>1) {
      (l.tail.head._1+1, l.tail.head._2)::l.tail.tail
    }
    else l
  }

  def encodeDirect[A](l: List[A]): List[(Int, A)] = {
    if (l.isEmpty) { List() }
    else if (l.tail.isEmpty) { List((1, l.head)) }
    else if ( l.head==l.tail.head ) { val temp=(1, l.head)::encodeDirect(l.tail) ; combineFirstTwoDirect(temp) }
    else { (1, l.head)::encodeDirect(l.tail) }
  }

  def duplicate[A](l: List[A]): List[A] = {
    if (l.isEmpty) { l }
    else { l.head::l.head::duplicate(l.tail) }
  }

  def duplicateN[A](n: Int, l: List[A]): List[A] = {
    if (l.isEmpty) { l }
    else { decodeOne(l.head, n):::duplicateN(n, l.tail) }
  }

  def dropModN[A](n: Int, m: Int, l: List[A]): List[A] = {
    if (l.isEmpty) { l }
    else if (m%n==0) { dropModN(n, m+1, l.tail) }
    else { l.head::dropModN(n, m+1, l.tail) }
  }

  def drop[A](n: Int, l: List[A]): List[A] = {
    dropModN(n, 1, l)
  }

  def split[A](n: Int, l: List[A]): (List[A], List[A]) = {
    if (n==0) { (List(), l) }
    else if (l.isEmpty) { (List(), List()) }
    else { val temp=split(n-1, l.tail); (l.head::temp._1, temp._2) }
  }

  def slice[A](start: Int, stop: Int, l: List[A]): List[A] = {
    split(stop-start, split(start, l)._2)._1
  }

  def rotate[A](n: Int, l: List[A]): List[A] = {
    if (n>=0) {
      val sliced = split(n, l)
      sliced._2:::sliced._1
    }
    else {
      val sliced=split(l.length+n, l)
      sliced._2:::sliced._1
    }
  }

  def removeAt[A](n: Int, l: List[A]): (List[A], A) = {
    if (n==0) { (l.tail, l.head) }
    else { val removed=removeAt(n-1, l.tail); (l.head::removed._1, removed._2) }
  }
}
