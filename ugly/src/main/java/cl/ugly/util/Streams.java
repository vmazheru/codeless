package cl.ugly.util;

import static cl.core.decorator.exception.ExceptionDecorators.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cl.core.ds.Pair;

public interface Streams {

    static <T,U> Stream<Pair<T,U>> zip(Stream<T> s1, Stream<U> s2) {

        class ZipSpliterator implements Spliterator<Pair<T,U>>{
            
            private final Spliterator<T> s1Spliterator;
            private final Spliterator<U> s2Spliterator;
            
            private T nextT;
            private U nextU;
            
            ZipSpliterator(Stream<T> s1, Stream<U> s2) {
                s1Spliterator = s1.spliterator();
                s2Spliterator = s2.spliterator();
            }

            @Override
            public boolean tryAdvance(Consumer<? super Pair<T, U>> action) {
                boolean t1Advanced = s1Spliterator.tryAdvance(t -> { nextT = t; });
                boolean t2Advanced = s2Spliterator.tryAdvance(u -> { nextU = u; });
                if (t1Advanced && t2Advanced) {
                    action.accept(new Pair<>(nextT, nextU));
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<Pair<T, U>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return 0;
            }

            
        }
    
        return StreamSupport.stream(new ZipSpliterator(s1, s2), false).onClose(() -> {
            uncheck(() -> s1.close());
            uncheck(() -> s2.close());
        });
    
    }
    
}

class StreamUtils {
    static Runnable uncheckedCloser(Closeable closeable) {
        return () -> {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
