package cl.core.decorator;

import static cl.core.decorator.batch.BatchDecorators.*;
import static cl.core.decorator.exception.ExceptionDecorators.*;
import static cl.core.decorator.retry.RetryDecorators.*;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import cl.core.decorator.DecoratorsExample.SomeAPI.Req;
import cl.core.decorator.DecoratorsExample.SomeAPI.Res;
import cl.core.decorator.DecoratorsExample.SomeAPI.SomeAPIException;

/**
 * This test shows the use of multiple decorators applied to the same function.
 */
public class DecoratorsExample {

    @Test
    public void showDecorators() {
        List<Req> request = getRequest();
        SomeAPI api = new SomeAPI();
        
        // bare request throws checked API exception
        // and try/catch is necessary
        try {
            api.process(request);
            fail("SomeAPIException is supposed to be thrown");
        } catch (SomeAPIException e) {
            //ok
        }
        
        // use exception decorator to get rid of checked exception
        // here try/catch is no longer required, but we still use it for our test purposes
        try {
            uncheck(() -> api.process(request));
            fail("RuntimeException is supposed to be thrown caused by SomeAPIException");
        } catch (RuntimeException e) {
            assertEquals(SomeAPIException.class, e.getCause().getClass());
        }

        // now, wrap our unchecked call into a retry, it still throws because we use
        // batches which exceed the size limit
        try {
            retried(3, 100, unchecked(() -> api.process(request))).get();
            fail("RuntimeException is supposed to be thrown caused by SomeAPIException");
        } catch (RuntimeException e) {
            assertEquals(SomeAPIException.class, e.getCause().getClass());
        }
        
        // now, we wrap our unchecked and retried function into batch decorator, and the call 
        // finally succeeds
        List<Res> result =
            batched(SomeAPI.MAX_REQ_SIZE,
                retried(3, 100,
                        unchecked(
                                (List<Req> list) -> api.process(list)))).apply(request);

        assertEquals(request.size(), result.size());
    }
    
    private static List<Req> getRequest() {
        Req[] request = new Req[111];
        Arrays.fill(request, new Req());
        return Arrays.asList(request);
    }
    
    /*
     * There is some API which accepts a list of objects to process, and the request size
     * is limited by 10 items.
     */
    static class SomeAPI {
        final static int MAX_REQ_SIZE = 10;
        
        List<Res> process(List<Req> request) throws SomeAPIException {
            if (request.size() > MAX_REQ_SIZE) {
                throw new SomeAPIException("request size too large");
            }
            return request.stream().map(req -> new Res()).collect(toList());
        }
        
        static class Req {}
        static class Res {}

        @SuppressWarnings("serial")
        static class SomeAPIException extends Exception {
            SomeAPIException(String msg) { super(msg); }
        }        
        
    }
    

}
