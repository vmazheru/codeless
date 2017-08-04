package cl.core.decorator.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import cl.core.decorator.Decorator;

/**
 * Contains static decorator methods which aim to process collections in batches of smaller size.
 * 
 * <p>Methods in this interface fall into two groups.
 * 
 * <p>Firstly, there are overloaded {@code batched()} methods which accept different functions 
 * operating on collections and return functions which do the same things as original functions but
 * in batches (aka "decorated" functions). An example is shown below:
 * 
 * <pre>{@code
        List<Integer> integers = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16);
        batch(5, integers, batch -> {
            System.out.println(batch);
        });
  
        // Prints:
        //        [1, 2, 3, 4, 5]
        //        [6, 7, 8, 9, 10]
        //        [11, 12, 13, 14, 15]
        //        [16]
 * }</pre>
 * 
 *  <p>Secondly, there are methods which accept functions along with collections and apply these
 *  functions with batching. An example is shown below:
 *  
 *  <pre>{@code
        Consumer<Iterable<Integer>> consumer = batched(5, batch -> {
            System.out.println(batch);
        });
        
        // this consumer, when called, prints a collection of integers
        consumer.accept(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16));
        
        // Prints:
        //        [1, 2, 3, 4, 5]
        //        [6, 7, 8, 9, 10]
        //        [11, 12, 13, 14, 15]
        //        [16]
 *  }</pre>
 *  
 *  <p>
 *  Also, methods in this interface may be divided into:
 *  
 *  <ol>
 *      <li>
 *          Methods which execute collections in batches of exact given size. These methods assume that 
 *          the collection elements may be processed independently of each other.
 *      </li>
 *      <li>
 *          Methods which keep "related" elements together (that is do not split them into different batches).
 *          These methods accept an additional parameter, a group function, which specifies how the
 *          elements are grouped.  
 *          
 *          <p>The group function takes a collection element as an input and returns a {@code Comparable}
 *          object as an output.  These comparable objects will be used to compare and, optionally, sort
 *          the collection elements.
 *          
 *          <p>These methods throw exceptions whenever they encounter a group which size is
 *          larger than the batch size. 
 *          
 *          <p>In order for these methods to work correctly, the input collections
 *          must be sorted so that "related" elements are kept together.  Yet another additional
 *          parameter to these methods denotes whether the input collections are sorted or not.
 *          If a given collection is not sorted it will be sorted according to the given group function, 
 *          and the original order of elements in the collection will be lost. 
 *          
 *          <p>We will refer to this type of batching as "batching with groups".
 *      </li>
 *  </ol>
 */
public interface BatchDecorators {
    
    ////////////////////////decorators ///////////////////////////

    /**
     * Decorate a consumer of a collection with batching.
     * 
     * @param batchSize  batch size
     * @param f          a consumer which accepts an {@code Iterable} object
     * @return           a consumer which applies batching to the code of the original consumer
     */
    static <T, I extends Iterable<T>> Consumer<I> batched(int batchSize, Consumer<I> f) {
        return new BatchDecorator<T, Object, Object, I>(batchSize).decorate(f);
    }
    
    /**
     * Apply batching with groups to a consumer of a collection.
     * 
     * @param batchSize      batch size
     * @param inputSorted    indicates whether the input is sorted or not
     * @param groupFunction  a group function
     * @param f              a consumer which accepts an {@code Iterable} object
     * @return               a consumer which applies batching with groups to the code of the original consumer
     */
    static <T, C extends Comparable<C>, I extends Iterable<T>> Consumer<I> batched(
            int batchSize, boolean inputSorted, Function<T, C> groupFunction, Consumer<I> f) {
        return new GroupedBatchDecorator<T, Object, Object, C, I>(batchSize, inputSorted, groupFunction).decorate(f);
    }    
    
    /**
     * Decorate a {@code BiConumser} which accepts a collection with batching.
     * 
     * @param batchSize  batch size
     * @param f          a consumer which accepts an {@code Iterable} object and an additional parameter
     * @return           a consumer which apples batching to the code of the original consumer
     */
    static <T, U, I extends Iterable<T>> BiConsumer<I, U> batched(int batchSize, BiConsumer<I, U> f) {
        return new BatchDecorator<T, U, Object, I>(batchSize).decorate(f);
    }
    
    /**
     * Apply batching with groups to a {@code BiConsumer} of a collection.
     * 
     * @param batchSize      batch size
     * @param inputSorted    indicates whether the input is sorted or not
     * @param groupFunction  a group function
     * @param f              a consumer which accepts an {@code Iterable} object and another additional parameter
     * @return               a consumer which applies batching with groups to the code of the original consumer
     */
    static <T, U, C extends Comparable<C>, I extends Iterable<T>> BiConsumer<I, U> batched(
            int batchSize, boolean inputSorted, Function<T, C> groupFunction, BiConsumer<I, U> f) {
        return new GroupedBatchDecorator<T, U, Object, C, I>(batchSize, inputSorted, groupFunction).decorate(f);
    }    
    
    /**
     * Decorate with batching a {@code Function} which maps a collection of some type to a list of a different type.
     * 
     * @param batchSize  batch size
     * @param f          a function which accepts an {@code Iterable} object and returns a list of some other type
     * @return           a function which apples batching to the code of the original function
     */
    static <T, R, I extends Iterable<T>> Function<I, List<R>> batched(int batchSize, Function<I, List<R>> f) {
        return new BatchDecorator<T, Object, R, I>(batchSize).decorate(f);
    }
    
    /**
     * Apply batching with groups to a function which accepts a collection and returns a list.
     * 
     * @param batchSize      batch size
     * @param inputSorted    indicates whether the input is sorted or not
     * @param groupFunction  a group function
     * @param f              a function which accepts an {@code Iterable} object and returns a list
     * @return               a consumer which applies batching with groups to the code of the original function
     */
    static <T, R, C extends Comparable<C>, I extends Iterable<T>> Function<I, List<R>> batched(
            int batchSize, boolean inputSorted, Function<T, C> groupFunction, Function<I, List<R>> f) {
        return new GroupedBatchDecorator<T, Object, R, C, I>(batchSize, inputSorted, groupFunction).decorate(f);
    }    
    
    /**
     * Decorate with batching a {@code BiFunction} which maps a collection of some type to a list of a different type.
     * 
     * @param batchSize  batch size
     * @param f          a function which accepts an {@code Iterable} object, additional parameter, and returns a list of some other type
     * @return           a function which apples batching to the code of the original function
     */
    static <T, U, R, I extends Iterable<T>> BiFunction<I, U, List<R>> batched(int batchSize, BiFunction<I, U, List<R>> f) {
        return new BatchDecorator<T, U, R, I>(batchSize).decorate(f);
    }
    
    /**
     * Apply batching with groups to a {@code BiFunction} which accepts a collection and additional parameter
     * and returns a list.
     * 
     * @param batchSize      batch size
     * @param inputSorted    indicates whether the input is sorted or not
     * @param groupFunction  a group function
     * @param f              a function which accepts an {@code Iterable} object and an additional parameter and returns a list
     * @return               a function which applies batching with groups to the code of the original function
     */
    static <T, U, R, C extends Comparable<C>, I extends Iterable<T>> BiFunction<I, U, List<R>> batched(
            int batchSize, boolean inputSorted, Function<T, C> groupFunction, BiFunction<I, U, List<R>> f) {
        return new GroupedBatchDecorator<T, U, R, C, I>(batchSize, inputSorted, groupFunction).decorate(f);
    }
    
    ///////////////////////// decorator applications ////////////////
    
    /**
     * Execute a collection consumer in batches. 
     * 
     * @param batchSize  batch size
     * @param col        a collection of objects which will be passed to the consumer 
     * @param f          a consumer which will be applied to each batch
     */
    static <T, I extends Iterable<T>> void batch(int batchSize, I col, Consumer<I> f) {
        batched(batchSize, f).accept(col);
    }
    
    /**
     * Apply batching with groups to a consumer of a collection.
     * 
     * @param batchSize       batch size
     * @param col             a collection of objects which will be passed to the consumer
     * @param inputSorted     indicates whether the input collection is sorted or not
     * @param groupFunction   a group function
     * @param f               a consumer which will be applied to each batch
     */
    static <T, C extends Comparable<C>, I extends Iterable<T>> void batch(
            int batchSize, I col, boolean inputSorted, Function<T, C> groupFunction, Consumer<I> f) {
        batched(batchSize, inputSorted, groupFunction, f).accept(col);
    }
    
    /**
     * Execute a collection consumer in batches. 
     * 
     * @param batchSize  batch size
     * @param col        a collection of objects which will be passed to the consumer
     * @param param      the second argument to the consumer 
     * @param f          a consumer which will be applied to each batch
     */
    static <T, U, I extends Iterable<T>> void batch(int batchSize, I col, U param, BiConsumer<I, U> f) {
        batched(batchSize, f).accept(col, param);
    }
    
    /**
     * Apply batching with groups to a consumer of a collection.
     * 
     * @param batchSize       batch size
     * @param col             a collection of objects which will be passed to the consumer
     * @param param           the second parameter to the consumer
     * @param inputSorted     indicates whether the input collection is sorted or not
     * @param groupFunction   a group function
     * @param f               a consumer which will be applied to each batch
     */
    static <T, U, C extends Comparable<C>, I extends Iterable<T>> void batch(
            int batchSize, I col, U param, boolean inputSorted, Function<T, C> groupFunction, BiConsumer<I, U> f) {
        batched(batchSize, inputSorted, groupFunction, f).accept(col, param);
    }
    
    /**
     * Execute a function which maps a collection to a list in batches. 
     * 
     * @param batchSize  batch size
     * @param col        a collection of objects which will be passed to the function 
     * @param f          a function which will be applied to each batch
     */
    static <T, R, I extends Iterable<T>> List<R> batch(int batchSize, I col, Function<I, List<R>> f) {
        return batched(batchSize, f).apply(col);
    }
    
    /**
     * Apply batching with groups to a function which maps a collection to a list.
     * 
     * @param batchSize       batch size
     * @param col             a collection of objects which will be passed to the function
     * @param inputSorted     indicates whether the input collection is sorted or not
     * @param groupFunction   a group function
     * @param f               a function which will be applied to each batch
     */
    static <T, R, C extends Comparable<C>, I extends Iterable<T>> List<R> batch(
            int batchSize, I col, boolean inputSorted, Function<T, C> groupFunction, Function<I, List<R>> f) {
        return batched(batchSize, inputSorted, groupFunction, f).apply(col);
    }

    /**
     * Execute a function which maps a collection to a list in batches. 
     * 
     * @param batchSize  batch size
     * @param col        a collection of objects which will be passed to the function
     * @param param      the second parameter to the function 
     * @param f          a function which will be applied to each batch
     */
    static <T, U, R, I extends Iterable<T>> List<R> batch(int batchSize, I col, U param, BiFunction<I, U, List<R>> f) {
        return batched(batchSize, f).apply(col, param);
    }
    
    /**
     * Apply batching with groups to a function which maps a collection to a list.
     * 
     * @param batchSize       batch size
     * @param col             a collection of objects which will be passed to the function
     * @param param           the second parameter to the function
     * @param inputSorted     indicates whether the input collection is sorted or not
     * @param groupFunction   a group function
     * @param f               a function which will be applied to each batch
     */
    static <T, U, R, C extends Comparable<C>, I extends Iterable<T>> List<R> batch(
            int batchSize, I col, U param, boolean inputSorted, Function<T, C> groupFunction, BiFunction<I, U, List<R>> f) {
        return batched(batchSize, inputSorted, groupFunction, f).apply(col, param);
    }
}

/**
 * Implementation of {@link Decorator} interface which processes collections in fixed size batches.
 *
 * @param <T>  type of objects which the input collection contains
 * @param <U>  type of the second parameter passed to the decorator (beside the collection object itself)
 * @param <R>  type of the objects which the resulting list will have
 * @param <I>  type of the collection which must extend {@code java.lang.Iterable}
 */
class BatchDecorator<T, U, R, I extends Iterable<T>> implements Decorator<I, U, List<R>> {
    
    protected final int batchSize;
    
    BatchDecorator(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public BiFunction<I, U, List<R>> decorate(BiFunction<I, U, List<R>> f) {
        return (iterable, u) -> {
            if (iterable == null) {
                throw new IllegalArgumentException("null list passed to a function");
            }
            if (!iterable.iterator().hasNext()) {
                return Collections.emptyList();
            }
            
            if (iterable instanceof List) {
                return processList((List<T>)iterable, u, f);
            }
            return processIterable(iterable, u, f);
        };    
    }
    
    @SuppressWarnings("unchecked")
    private List<R> processIterable(I input, U u, BiFunction<I, U, List<R>> f) {
        List<R> result = new ArrayList<>();

        int i = 0;
        List<T> group = new ArrayList<>(batchSize);
        for (T t : input) {
            if (i >= batchSize) {
                addAllSafely(result, f.apply((I)group, u));
                group = new ArrayList<>(batchSize);
                i = 0;
            }
            group.add(t);
            i++;
        }

        if (!group.isEmpty()) {
            addAllSafely(result, f.apply((I)group, u));
        }
        
        return result.isEmpty() ? null : result;
    }    
    
    @SuppressWarnings("unchecked")
    private List<R> processList(List<T> list, U u, BiFunction<I, U, List<R>> f) {
        final List<R> result = new ArrayList<>();
        final int size = list.size();
        int start = 0;
        int end = Math.min(batchSize, size);
        
        while (start < size) {
            addAllSafely(result, f.apply((I)list.subList(start, end), u));
            start = end;
            end = Math.min(end + batchSize, size);
        }
        return result.isEmpty() ? null : result;
    }

    protected static <T> void addAllSafely(List<T> dest, List<T> src) {
        if (src != null) {
            dest.addAll(src);
        }
    }   
}

/**
 * Implementation of {@link Decorator} interface which processes collections in fixed size batches with groups.
 * <p>
 * The decorator will try to break the given collection into batches of the given size, but keep 
 * related objects together in the same batch.  The given "group" function defines which objects are
 * considered "related".
 * <p>
 * If any of the groups as defined by the group function has size greater than the given batch size,
 * the decorator will throw an exception.  If multiple groups, on the other hand, can fit in one batch
 * they will be processed together.
 *
 * @param <T>  type of objects which the input collection contains
 * @param <U>  type of the second parameter passed to the decorator (beside the collection object itself)
 * @param <R>  type of the objects which the resulting list will have
 * @param <C>  type of the output of the group function
 * @param <I>  type of the collection which must extend {@code java.lang.Iterable}
 */
class GroupedBatchDecorator<T, U, R, C extends Comparable<C>, I extends Iterable<T>> extends BatchDecorator<T, U, R, I> {
    
    private final boolean inputSorted;
    private final Function<T,C> groupFunction;
    
    GroupedBatchDecorator(int batchSize, boolean inputSorted, Function<T,C> groupFunction) {
        super(batchSize);
        this.inputSorted = inputSorted;
        this.groupFunction = groupFunction;
    }

    @Override
    public BiFunction<I, U, List<R>> decorate(BiFunction<I, U, List<R>> f) {
        return (iterable, u) -> {
            if (iterable == null) {
                throw new IllegalArgumentException("null list passed to a function");
            }
            if (!iterable.iterator().hasNext()) {
                return Collections.emptyList();
            }
            return processWithGroups(iterable, u, f);
        };
    }
    
    @SuppressWarnings("unchecked")
    private List<R> processWithGroups(I iterable, U u, BiFunction<I, U, List<R>> f) {
        Iterable<T> input = inputSorted ? iterable : sort(iterable);
        List<R> result = new ArrayList<>();
        List<T> part   = new ArrayList<>(batchSize);
        List<T> group  = new ArrayList<>(batchSize);
        C groupIdentifier = groupFunction.apply(input.iterator().next());
        
        for (T t : input) {
            C identifier = groupFunction.apply(t);
            if (identifier.compareTo(groupIdentifier) != 0) { // this is the start of a new group
                int groupSize = group.size();
                checkGroupSize(groupSize);
                if (part.size() + groupSize > batchSize) {
                    addAllSafely(result, f.apply((I)part, u));
                    part.clear();
                }
                part.addAll(group);
                group.clear();
                groupIdentifier = identifier;
            }
            group.add(t);
        }

        int groupSize = group.size();
        checkGroupSize(groupSize);
        int partSize = part.size();
        if (partSize + groupSize > batchSize) {
            if (partSize  != 0) addAllSafely(result, f.apply((I)part, u));
            if (groupSize != 0) addAllSafely(result, f.apply((I)group, u));
        } else {
            if (groupSize != 0) part.addAll(group);
            if (!part.isEmpty()) addAllSafely(result, f.apply((I)part, u));
        }

        return result.isEmpty() ? null : result;
    }
    
    private List<T> sort(Iterable<T> input) {
        List<T> l = new ArrayList<>();
        for (T t : input) l.add(t);
        Comparator<T> c = (t1, t2) -> groupFunction.apply(t1).compareTo(groupFunction.apply(t2));
        Collections.sort(l, c);
        return l;
    }
    
    private void checkGroupSize(int groupSize) {
        if (groupSize > batchSize) {
            throw new RuntimeException(
                    "Cannot fit a group of items of size " + groupSize + 
                    " into a batch of size " + batchSize + ". Batch size is too small");
        }        
    }
}