package cl.core.decorator.batch

import scala.collection.immutable.SortedSet
import scala.language.postfixOps
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import cl.core.ds.Counter
import cl.core.function.ScalaToJava._
import cl.core.decorator.batch.BatchDecorators._
import scala.collection.JavaConverters._
import java.util.stream.StreamSupport
import java.util.stream.Collectors
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
    
    val consumer = (list: java.lang.Iterable[Int]) => {
      counter.add(checkListSize(list))
    }
    
    val biConsumer = (list: java.lang.Iterable[Int], param: Int) => {
      counter.add(checkListSize(list))
      counter.add(param)
    }
    
    val function = (list: java.lang.Iterable[Int]) => {
      checkListSize(list)
      //add one to each element and return java.util.List[Int]
      StreamSupport.stream(list.spliterator(), false).map((i: Int) => i + 1).collect(Collectors.toList()).asInstanceOf[java.util.List[Int]]
    }
    
    val biFunction = (list: java.lang.Iterable[Int], param: Int) => {
      checkListSize(list)
      //add param to each element and return java.util.List[Int]
      StreamSupport.stream(list.spliterator(), false).map((i: Int) => i + param).collect(Collectors.toList()).asInstanceOf[java.util.List[Int]]
    }
    
    private def checkListSize[A](i: java.lang.Iterable[A]) = {
      val size = StreamSupport.stream(i.spliterator(), false).count().intValue()
      if (size > SIZE_LIMIT) throw new RuntimeException("bad list size")
      size
    }
  }
 
  it should "process bigger lists in batches of required size" in {
    import Functions._
    
    val list         = List(1,2,3,4,5,6,7)
    val set          = Set(1,2,3,4,5,6,7)
    val sortedSet    = SortedSet(1,2,3,4,5,6,7).asJava
    val addOneResult = List(2,3,4,5,6,7,8).asJava
    val addTenResult = List(11,12,13,14,15,16,17).asJava
    
    // test with Lists
    batch[Int, java.util.List[Int]](SIZE_LIMIT, list, consumer)
    counter.getValueAndReset should be (list.size)
    
    batch[Int, Int, java.util.List[Int]](SIZE_LIMIT, list, 1, biConsumer)
    counter.getValueAndReset should be (list.size + 2) //param added twice because of the batching
    
    batch[Int, Int, java.util.List[Int]](SIZE_LIMIT, list, function) should be (addOneResult)
    batch[Int, Int, Int, java.util.List[Int]](SIZE_LIMIT, list, 10, biFunction) should be (addTenResult)

    // test with sets
    batch[Int, java.util.Set[Int]](SIZE_LIMIT, set, consumer)
    counter.getValueAndReset should be (list.size)

    batch[Int, Int, java.util.Set[Int]](SIZE_LIMIT, set, 1, biConsumer)
    counter.getValueAndReset should be (list.size + 2)

    batch[Int, Int, java.util.Set[Int]](SIZE_LIMIT, sortedSet, function) should be (addOneResult)
    batch[Int, Int, Int, java.util.Set[Int]](SIZE_LIMIT, sortedSet, 10, biFunction) should be (addTenResult)
  }

  
  it should "process lists of grouped (related) items in batches of smaller than or equal to the batch size" in {
    import Functions._
    
    val list = List(1,2,2,3,3,3,4,4,4,4,5,5,5,5,5,6,7)
    val grouped = List(List(1,2,2), List(3,3,3), List(4,4,4,4), List(5,5,5,5,5), List(6,7))
    val addOneResult = (list map {i => i +  1} toList).asJava
    val addTenResult = (list map {i => i + 10} toList).asJava
    val groupFunction = (i: Int) => new Integer(i)
    
    batch[Int, Integer, java.util.List[Int]](SIZE_LIMIT, list, true, groupFunction, consumer)
    counter.getValueAndReset should be (list.size)
    
    batch[Int, Int, Integer, java.util.List[Int]](SIZE_LIMIT, list, 1, true, groupFunction, biConsumer)
    counter.getValueAndReset should be (list.size + grouped.size) //5 groups less than size SIZE_LIMIT expected
    
    batch[Int, Int, Integer, java.util.List[Int]](SIZE_LIMIT, list, true, groupFunction, function) should be (addOneResult)
    batch[Int, Int, Int, Integer, java.util.List[Int]](SIZE_LIMIT, list, 10, true, groupFunction, biFunction) should be (addTenResult)
  }
  
  Given ("a 'chop' function which uses batch decorator to collect batches in a list")
  def chop(list: List[Int], isSorted: Boolean): List[List[Int]] = {
    val b = new ListBuffer[List[Int]]
    val consumer: (java.lang.Iterable[Int] => Unit) = (list: java.lang.Iterable[Int]) => {
      val l = new ListBuffer[Int]
      val iter = list.iterator()
      while (iter.hasNext()) l += iter.next()
      b += l.toList
    }
    batch[Int, Integer, java.util.List[Int]](Functions.SIZE_LIMIT, list, isSorted, (i: Int) => new Integer(i), consumer)
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
  it should "handle it gracefully" in {
    intercept[IllegalArgumentException] { batch[Int, java.util.List[Int]](-1, List(1,2,3), (list: java.lang.Iterable[Int]) => {}) }
    intercept[IllegalArgumentException] { batch[Int, java.util.List[Int]](10, null, (list: java.lang.Iterable[Int]) => {}) }
    
    val groupFunction = (i: Int) => new Integer(i)
    intercept[IllegalArgumentException] { batch[Int, Integer, java.util.List[Int]](-1, List(1,2,3), true, groupFunction, (list: java.lang.Iterable[Int]) => {}) }
    intercept[IllegalArgumentException] { batch(10, null, true, groupFunction, (list: java.lang.Iterable[Int]) => {}) }
  }
  
  
}