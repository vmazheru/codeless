package cl.core.function

import scala.language.implicitConversions

import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Supplier

import scala.collection.JavaConverters._
import scala.collection.SortedSet

/**
 * Implicit conversions between Scala function types and Java 8 functional interfaces.
 */
object ScalaToJava {
  
  // converting collections
  implicit def toJavaList[A](seq: Seq[A]): java.util.List[A] = seq.asJava
  implicit def toScalaList[A](javaList: java.util.List[A]) = javaList.asScala.toList
  implicit def toJavaSet[A](set: Set[A]): java.util.Set[A] = set.asJava
  implicit def toJavaSortedSet[A](set: SortedSet[A]): java.util.Set[A] = set.asJava


  // converting functions
  implicit def toRunnable(f: () => Unit) = new Runnable() {
    override def run() = f.apply()
  }
  
  implicit def toSupplier[R](f: () => R) = new Supplier[R]() {
    override def get() = f.apply()
  }
  
  implicit def toSupplierWithException[R](f: () => R) = new SupplierWithException[R]() {
    override def get() = f.apply()
  }  

  implicit def toConsumer[T](f: T => Unit) = new Consumer[T]() {
    override def accept(t: T) = f.apply(t)
  }
  
  implicit def fromConsumer[T](c: Consumer[T]): T => Unit = t => c.accept(t)
  
  implicit def toIterConsumer[T](f: Iterable[T] => Unit) = new Consumer[java.lang.Iterable[T]]() {
    override def accept(t: java.lang.Iterable[T]) = f.apply(t.asScala)
  }
  
  implicit def toBiConsumer[T,U](f: (T,U) => Unit) = new BiConsumer[T,U]() {
    override def accept(t: T, u: U) = f.apply(t, u)
  }
  
  implicit def toIterBiConsumer[T,U](f: (Iterable[T], U) => Unit) = new BiConsumer[java.lang.Iterable[T], U]() {
    override def accept(t: java.lang.Iterable[T], u: U) = f.apply(t.asScala, u)
  }  
  
  implicit def toFunction[T,R](f: T => R) = new java.util.function.Function[T,R]() {
    override def apply(t: T) = f.apply(t)
  }
  
  implicit def toFunctionWithException[T,R](f: T => R) = new FunctionWithException[T,R]() {
    override def apply(t: T) = f.apply(t)
  }  
  
  implicit def toIterToListFunction[T,R](f: Iterable[T] => List[R]) =
    new java.util.function.Function[java.lang.Iterable[T], java.util.List[R]]() {
      override def apply(t: java.lang.Iterable[T]): java.util.List[R] = f.apply(t.asScala).asJava
  }
  
  implicit def toBiFunction[T,U,R](f: (T,U) => R) = new BiFunction[T,U,R]() {
    override def apply(t: T, u: U) = f.apply(t, u)
  }
  
  implicit def toIterToListBiFunction[T,U,R](f: (Iterable[T], U) => List[R]) =
    new BiFunction[java.lang.Iterable[T], U, java.util.List[R]]() {
      override def apply(t: java.lang.Iterable[T], u: U): java.util.List[R] = f.apply(t.asScala, u).asJava
  }
  
}