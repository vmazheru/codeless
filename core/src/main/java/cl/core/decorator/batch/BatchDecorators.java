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
 * Contains static decorator methods which aim to process lists in batches.
 */
public interface BatchDecorators {
    
    ////////////////////////decorators ///////////////////////////

    static <T,C extends Comparable<C>,I extends Iterable<T>>
    Consumer<I> batched(int batchSize, Consumer<I> f) {
        return new BatchDecorator<T,Object,Object,C,I>(batchSize).decorate(f);
    }
    
    static <T,C extends Comparable<C>,I extends Iterable<T>>
    Consumer<I> batched(int batchSize, boolean inputSorted, Function<T,C> groupFunction, Consumer<I> f) {
        return new BatchDecorator<T,Object,Object,C,I>(batchSize, inputSorted, groupFunction).decorate(f);
        //return null;
    }    
    
    static <T,U,C extends Comparable<C>,I extends Iterable<T>>
    BiConsumer<I, U> batched(int batchSize, BiConsumer<I,U> f) {
        return new BatchDecorator<T,U,Object,C,I>(batchSize).decorate(f);
    }
    
    static <T,U,C extends Comparable<C>,I extends Iterable<T>>
    BiConsumer<I, U> batched(int batchSize, boolean inputSorted, Function<T,C> groupFunction, BiConsumer<I,U> f) {
        return new BatchDecorator<T,U,Object,C,I>(batchSize, inputSorted, groupFunction).decorate(f);
    }    
    
    static <T,R,C extends Comparable<C>,I extends Iterable<T>>
    Function<I, List<R>> batched(int batchSize, Function<I, List<R>> f) {
        return new BatchDecorator<T,Object,R,C,I>(batchSize).decorate(f);
    }
    
    static <T,R,C extends Comparable<C>,I extends Iterable<T>>
    Function<I, List<R>> batched(int batchSize, boolean inputSorted, Function<T,C> groupFunction, Function<I, List<R>> f) {
        return new BatchDecorator<T,Object,R,C,I>(batchSize, inputSorted, groupFunction).decorate(f);
    }    
    
    static <T,U,R,C extends Comparable<C>,I extends Iterable<T>>
    BiFunction<I,U,List<R>> batched(int batchSize, BiFunction<I,U,List<R>> f) {
        return new BatchDecorator<T,U,R,C,I>(batchSize).decorate(f);
    }
    
    static <T,U,R,C extends Comparable<C>,I extends Iterable<T>>
    BiFunction<I,U,List<R>> batched(int batchSize, boolean inputSorted, Function<T,C> groupFunction, BiFunction<I,U,List<R>> f) {
        return new BatchDecorator<T,U,R,C,I>(batchSize, inputSorted, groupFunction).decorate(f);
    }
    
    
    ///////////////////////// decorator applications ////////////////
    
    static <T> void batch(
            int batchSize,
            Iterable<T> col,
            Consumer<Iterable<T>> f) {
        batched(batchSize, f).accept(col);
    }
    
    static <T, C extends Comparable<C>> void batch(
            int batchSize,
            Iterable<T> col,
            boolean inputSorted,
            Function<T,C> groupFunction,
            Consumer<Iterable<T>> f) {
        batched(batchSize, inputSorted, groupFunction, f).accept(col);
    }
    
    static <T,U> void batch(
            int batchSize,
            Iterable<T> col,
            U param,
            BiConsumer<Iterable<T>,U> f) {
        batched(batchSize, f).accept(col, param);
    }
    
    static <T,U,C extends Comparable<C>> void batch(
            int batchSize,
            Iterable<T> col,
            U param,
            boolean inputSorted,
            Function<T,C> groupFunction,
            BiConsumer<Iterable<T>,U> f) {
        batched(batchSize, inputSorted, groupFunction, f).accept(col, param);
    }
    
    static <T,R> List<R> batch(
            int batchSize,
            Iterable<T> col,
            Function<Iterable<T>,List<R>> f) {
        return batched(batchSize, f).apply(col);
    }
    
    static <T,R,C extends Comparable<C>> List<R> batch(
            int batchSize,
            Iterable<T> col,
            boolean inputSorted,
            Function<T,C> groupFunction,
            Function<Iterable<T>,List<R>> f) {
        return batched(batchSize, inputSorted, groupFunction, f).apply(col);
    }

    static <T,U,R> List<R> batch(
            int batchSize,
            Iterable<T> col,
            U param,
            BiFunction<Iterable<T>,U,List<R>> f) {
        return batched(batchSize, f).apply(col, param);
    }
    
    static <T,U,R,C extends Comparable<C>> List<R> batch(
            int batchSize,
            Iterable<T> col,
            U param,
            boolean inputSorted,
            Function<T,C> groupFunction,
            BiFunction<Iterable<T>,U,List<R>> f) {
        return batched(batchSize, inputSorted, groupFunction, f).apply(col, param);
    }
}

class BatchDecorator<T,U,R,C extends Comparable<C>,I extends Iterable<T>> implements Decorator<I, U, List<R>> {
    
    private final int batchSize;
    private final boolean inputSorted;
    private final Function<T,C> groupFunction;
    
    BatchDecorator(int batchSize, boolean inputSorted, Function<T,C> groupFunction) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("invalid batch size");
        }
        
        this.batchSize = batchSize;
        this.inputSorted = inputSorted;
        this.groupFunction = groupFunction;
    }
    
    BatchDecorator(int batchSize) {
        this(batchSize, false, null);
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
            
            if (groupFunction == null) {
                if (iterable instanceof List) {
                    return processListSimply((List<T>)iterable, u, f);
                }
                return processIterableSimply(iterable, u, f);
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
            if (identifier.compareTo(groupIdentifier) != 0) { // this is start of a new group
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
        checkGroupSize(group.size());
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
    
    @SuppressWarnings("unchecked")
    private List<R> processIterableSimply(I input, U u, BiFunction<I, U, List<R>> f) {
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
    private List<R> processListSimply(List<T> list, U u, BiFunction<I, U, List<R>> f) {
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
   
    private List<T> sort(Iterable<T> input) {
        List<T> l = new ArrayList<>();
        for (T t : input) l.add(t);
        Comparator<T> c = (t1, t2) -> groupFunction.apply(t1).compareTo(groupFunction.apply(t2));
        Collections.sort(l, c);
        return l;
    }
    
    private static <T> void addAllSafely(List<T> dest, List<T> src) {
        if (src != null) {
            dest.addAll(src);
        }
    }
    
    private void checkGroupSize(int groupSize) {
        if (groupSize > batchSize) {
            throw new RuntimeException(
                    "Cannot fit a group of items of size " + groupSize + 
                    " into a batch of size " + batchSize + ". Batch size is too small");
        }        
    }
}