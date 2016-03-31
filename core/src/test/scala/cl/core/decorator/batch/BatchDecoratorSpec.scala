package cl.core.decorator.batch

import scala.language.postfixOps

import scala.collection.SortedSet
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import cl.core.ds.Counter
import cl.core.function.ScalaToJava._
import cl.core.decorator.batch.BatchDecorators._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer


/**
 * Batch decorators specification
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class BatchDecoratorSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "batch decorator"
  
  Given ("different functions operating on limited size lists")
  object Functions {
    val SIZE_LIMIT = 5
    val counter = new Counter()
    
    val consumer = (list: Iterable[Int]) => {
      checkListSize(list)
      counter.add(list.size)
    }
    
    val biConsumer = (list: Iterable[Int], param: Int) => {
      checkListSize(list)
      counter.add(list.size)
      counter.add(param)
    }
    
    val function = (list: Iterable[Int]) => {
      checkListSize(list)
      list map { i => i + 1 } toList
    }
    
    val biFunction = (list: Iterable[Int], param: Int) => {
      checkListSize(list)
      list map { i => i + param } toList
    }
    
    private def checkListSize[A](i: Iterable[A]) = 
      if (i.size > SIZE_LIMIT) throw new RuntimeException("bad list size")  
  }
 
  it should "process bigger lists in batches of required size" in {
    import Functions._
    import scala.collection.JavaConverters._
    
    val list         = List(1,2,3,4,5,6,7)
    val set          = Set(1,2,3,4,5,6,7)
    val sortedSet    = SortedSet(1,2,3,4,5,6,7)
    val addOneResult = List(2,3,4,5,6,7,8).asJava
    val addTenResult = List(11,12,13,14,15,16,17).asJava
    
    // test with Lists
    batch(SIZE_LIMIT, list, consumer)
    counter.getValueAndReset should be (list.size)
    
    batch(SIZE_LIMIT, list, 1, biConsumer)
    counter.getValueAndReset should be (list.size + 2) //param added twice because of the batching
    
    batch(SIZE_LIMIT, list, function)       should be (addOneResult)
    batch(SIZE_LIMIT, list, 10, biFunction) should be (addTenResult)

    // test with sets
    batch(SIZE_LIMIT, set, consumer)
    counter.getValueAndReset should be (list.size)
    
    batch(SIZE_LIMIT, set, 1, biConsumer)
    counter.getValueAndReset should be (list.size + 2)
    
    batch(SIZE_LIMIT, sortedSet, function)       should be (addOneResult)
    batch(SIZE_LIMIT, sortedSet, 10, biFunction) should be (addTenResult)
  }

  it should "process lists of grouped (related) items in batches of smaller than or equal to the required size" in {
    import Functions._
    import scala.collection.JavaConverters._
    
    val list = List(1,2,2,3,3,3,4,4,4,4,5,5,5,5,5,6,7)
    val grouped = List(List(1,2,2), List(3,3,3), List(4,4,4,4), List(5,5,5,5,5), List(6,7))
    val addOneResult = (list map {i => i +  1} toList).asJava
    val addTenResult = (list map {i => i + 10} toList).asJava
    val groupFunction = (i: Int) => new Integer(i)
    
    batch(SIZE_LIMIT, list, true, groupFunction, consumer)
    counter.getValueAndReset should be (list.size)
    
    batch(SIZE_LIMIT, list, 1, true, groupFunction, biConsumer)
    counter.getValueAndReset should be (list.size + grouped.size) //5 groups less than size SIZE_LIMIT expected
    
    batch(SIZE_LIMIT, list, true, groupFunction, function)       should be (addOneResult)
    batch(SIZE_LIMIT, list, 10, true, groupFunction, biFunction) should be (addTenResult)
  }
  
  Given ("a 'chop' function which uses batch decorator to collect batches in a list")
  def chop(list: List[Int], isSorted: Boolean): List[List[Int]] = {
    val b = new ListBuffer[List[Int]]
    val consumer: (Iterable[Int] => Unit) = (list: Iterable[Int]) => b += list.toList
    batch(Functions.SIZE_LIMIT, list, isSorted, (i: Int) => new Integer(i), consumer) 
    b.toList
  }
  val list = List(1,2,2,3,3,3,4,4,4,4,5,5,5,5,5,6,7)
  val grouped = List(List(1,2,2), List(3,3,3), List(4,4,4,4), List(5,5,5,5,5), List(6,7))
  
  it should "process all items in the list and split them correctly" in {
    chop(list, true) should be (grouped)
  }
  
  it should "sort the input list if not sorted before processing" in {
    chop(scala.util.Random.shuffle(list), false) should be (grouped)
  }
  
  it should "throw an exception if there is a group which can't fit in a batch" in {
    intercept[RuntimeException] {
      chop(list ++ List(8,8,8,8,8,8) ++ List(9), true)
    }
  }
  
  Given ("bad input")
  it should "handle it graceflly" in {
    intercept[IllegalArgumentException] { batch(-1, list, (list: Iterable[Int]) => {}) }
    intercept[IllegalArgumentException] { batch(10, null, (list: Iterable[Int]) => {}) }
    val f: (Iterable[Int] => List[Int]) = list => list.toList
    val r1: List[Any] = batch(10, List(), f) // force conversion to Scala list
    r1 should be (List())
    
    val groupFunction = (i: Int) => new Integer(i)
    intercept[IllegalArgumentException] { batch(-1, list, true, groupFunction, (list: Iterable[Int]) => {}) }
    intercept[IllegalArgumentException] { batch(10, null, true, groupFunction, (list: Iterable[Int]) => {}) }
    val r2: List[Any] = batch(10, List(), true, groupFunction, f) // force conversion to Scala list
    r2 should be (List())    
  }
  
  
}