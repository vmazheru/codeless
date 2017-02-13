package cl.core.decorator.batch;

import static cl.core.decorator.batch.BatchDecorators.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

public class BatchDecoratorExamples {
    
    @Test
    public void simpleBatchExample() {
        List<Integer> integers = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16);
        batch(5, integers, (batch) -> {System.out.println(batch);});
        /* Prints:
                
                [1, 2, 3, 4, 5]
                [6, 7, 8, 9, 10]
                [11, 12, 13, 14, 15]
                [16]
         */
    }
    
    @Test
    public void simpleBatchedExample() {
        Consumer<Iterable<Integer>> consumer = batched(5, batch -> {System.out.println(batch);});
        // consumer, when called, will print a collection of integers
        consumer.accept(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16));
        
        // Prints:
                
        //        [1, 2, 3, 4, 5]
        //        [6, 7, 8, 9, 10]
        //        [11, 12, 13, 14, 15]
        //        [16]
    }
    
    

    @Test
    public void processListSimple() {
        List<String> applesAndOranges = Arrays.asList(
                "Apple 1", "Apple 2", "Avocado 1",
                "Orange 1", "Orange 2", "Orange 3", "Orange 4", "Orange 5");
        
        // Process apples and oranges in batches of 5.  Don't mix apples and oranges in one batch.
        // Notice, that "avocado" will stick with apples since the group size allows for it
        batch(5, applesAndOranges, true, s -> s.split(" ")[0], batch -> {
            System.out.println(batch);
        });
        
        // the same thing as above but with the unsorted list of apples and oranges
        Collections.shuffle(applesAndOranges);
        batch(5, applesAndOranges, false, s -> s.split(" ")[0], batch -> {
            System.out.println(batch);
        });
    }
}
