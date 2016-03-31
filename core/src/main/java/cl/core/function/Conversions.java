package cl.core.function;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Conversions between different functions and the most "general" BiFunction.
 */
public interface Conversions {

    // Runnable
    static <R>     Supplier  <R>     toS (Runnable r) { return ()    -> { r.run(); return null; }; }
    static <T>     Consumer  <T>     toC (Runnable r) { return (t)   -> { r.run(); }; }
    static <T,U>   BiConsumer<T,U>   toBC(Runnable r) { return (t,u) -> { r.run(); }; }
    static <T,R>   Function  <T,R>   toF (Runnable r) { return (t)   -> { r.run(); return null; }; }
    static <T,U,R> BiFunction<T,U,R> toBF(Runnable r) { return (t,u) -> { r.run(); return null; }; }
    
    static <R>     SupplierWithException  <R>     toS (RunnableWithException r) { return ()    -> { r.run(); return null; }; }
    static <T>     ConsumerWithException  <T>     toC (RunnableWithException r) { return (t)   -> { r.run(); }; }
    static <T,U>   BiConsumerWithException<T,U>   toBC(RunnableWithException r) { return (t,u) -> { r.run(); }; }
    static <T,R>   FunctionWithException  <T,R>   toF (RunnableWithException r) { return (t)   -> { r.run(); return null; }; }
    static <T,U,R> BiFunctionWithException<T,U,R> toBF(RunnableWithException r) { return (t,u) -> { r.run(); return null; }; }
    
    // Supplier
    static         Runnable          toR (Supplier<?> s) { return ()    -> s.get(); }
    static <T>     Consumer  <T>     toC (Supplier<?> s) { return (t)   -> s.get(); }
    static <T,U>   BiConsumer<T,U>   toBC(Supplier<?> s) { return (t,u) -> s.get(); }
    static <T,R>   Function  <T,R>   toF (Supplier<R> s) { return (t)   -> s.get(); }
    static <T,U,R> BiFunction<T,U,R> toBF(Supplier<R> s) { return (t,u) -> s.get(); }
    
    static         RunnableWithException          toR (SupplierWithException<?> s) { return ()    -> s.get(); }
    static <T>     ConsumerWithException  <T>     toC (SupplierWithException<?> s) { return (t)   -> s.get(); }
    static <T,U>   BiConsumerWithException<T,U>   toBC(SupplierWithException<?> s) { return (t,u) -> s.get(); }
    static <T,R>   FunctionWithException  <T,R>   toF (SupplierWithException<R> s) { return (t)   -> s.get(); }
    static <T,U,R> BiFunctionWithException<T,U,R> toBF(SupplierWithException<R> s) { return (t,u) -> s.get(); }
    
    // Consumer
    static         Runnable          toR (Consumer<?> c) { return ()    -> c.accept(null); }
    static <R>     Supplier<R>       toS (Consumer<?> c) { return ()    -> { c.accept(null); return null; }; }
    static <T,U>   BiConsumer<T,U>   toBC(Consumer<T> c) { return (t,u) -> c.accept(t); }
    static <T,R>   Function<T,R>     toF (Consumer<T> c) { return (t)   -> { c.accept(t); return null; }; }
    static <T,U,R> BiFunction<T,U,R> toBF(Consumer<T> c) { return (t,u) -> { c.accept(t); return null; }; }
    
    static         RunnableWithException          toR (ConsumerWithException<?> c) { return ()    -> c.accept(null); }
    static <R>     SupplierWithException<R>       toS (ConsumerWithException<?> c) { return ()    -> { c.accept(null); return null; }; }
    static <T,U>   BiConsumerWithException<T,U>   toBC(ConsumerWithException<T> c) { return (t,u) -> c.accept(t); }
    static <T,R>   FunctionWithException<T,R>     toF (ConsumerWithException<T> c) { return (t)   -> { c.accept(t); return null; }; }
    static <T,U,R> BiFunctionWithException<T,U,R> toBF(ConsumerWithException<T> c) { return (t,u) -> { c.accept(t); return null; }; }
    
    // BiConsumer
    static         Runnable          toR (BiConsumer<?,?> c) { return ()    -> c.accept(null, null); }
    static <R>     Supplier<R>       toS (BiConsumer<?,?> c) { return ()    -> { c.accept(null, null); return null; }; }
    static <T>     Consumer<T>       toC (BiConsumer<T,?> c) { return (t)   -> c.accept(t, null); }
    static <T,R>   Function<T,R>     toF (BiConsumer<T,?> c) { return (t)   -> { c.accept(t,null); return null; }; }
    static <T,U,R> BiFunction<T,U,R> toBF(BiConsumer<T,U> c) { return (t,u) -> { c.accept(t,u); return null; }; }
    
    static         RunnableWithException          toR (BiConsumerWithException<?,?> c) { return ()    -> c.accept(null, null); }
    static <R>     SupplierWithException<R>       toS (BiConsumerWithException<?,?> c) { return ()    -> { c.accept(null, null); return null; }; }
    static <T>     ConsumerWithException<T>       toC (BiConsumerWithException<T,?> c) { return (t)   -> c.accept(t, null); }
    static <T,R>   FunctionWithException<T,R>     toF (BiConsumerWithException<T,?> c) { return (t)   -> { c.accept(t,null); return null; }; }
    static <T,U,R> BiFunctionWithException<T,U,R> toBF(BiConsumerWithException<T,U> c) { return (t,u) -> { c.accept(t,u); return null; }; }
    
    // Function
    static         Runnable          toR (Function<?,?> f) { return ()    -> f.apply(null); }
    static <R>     Supplier<R>       toS (Function<?,R> f) { return ()    -> f.apply(null); }
    static <T>     Consumer<T>       toC (Function<T,?> f) { return (t)   -> f.apply(t); }
    static <T,U>   BiConsumer<T,U>   toBC(Function<T,?> f) { return (t,u) -> f.apply(t); }
    static <T,U,R> BiFunction<T,U,R> toBF(Function<T,R> f) { return (t,u) -> f.apply(t); }
    
    static         RunnableWithException          toR (FunctionWithException<?,?> f) { return ()    -> f.apply(null); }
    static <R>     SupplierWithException<R>       toS (FunctionWithException<?,R> f) { return ()    -> f.apply(null); }
    static <T>     ConsumerWithException<T>       toC (FunctionWithException<T,?> f) { return (t)   -> f.apply(t); }
    static <T,U>   BiConsumerWithException<T,U>   toBC(FunctionWithException<T,?> f) { return (t,u) -> f.apply(t); }
    static <T,U,R> BiFunctionWithException<T,U,R> toBF(FunctionWithException<T,R> f) { return (t,u) -> f.apply(t); }
    
    // BiFunction
    static       Runnable        toR (BiFunction<?,?,?> f) { return ()    -> f.apply(null, null); }
    static <R>   Supplier<R>     toS (BiFunction<?,?,R> f) { return ()    -> f.apply(null, null); }
    static <T>   Consumer<T>     toC (BiFunction<T,?,?> f) { return (t)   -> f.apply(t, null); }
    static <T,U> BiConsumer<T,U> toBC(BiFunction<T,U,?> f) { return (t,u) -> f.apply(t, u); }
    static <T,R> Function<T,R>   toF (BiFunction<T,?,R> f) { return (t)   -> f.apply(t, null); }
    
    static       RunnableWithException        toR (BiFunctionWithException<?,?,?> f) { return ()    -> f.apply(null, null); }
    static <R>   SupplierWithException<R>     toS (BiFunctionWithException<?,?,R> f) { return ()    -> f.apply(null, null); }
    static <T>   ConsumerWithException<T>     toC (BiFunctionWithException<T,?,?> f) { return (t)   -> f.apply(t, null); }
    static <T,U> BiConsumerWithException<T,U> toBC(BiFunctionWithException<T,U,?> f) { return (t,u) -> f.apply(t, u); }
    static <T,R> FunctionWithException<T,R>   toF (BiFunctionWithException<T,?,R> f) { return (t)   -> f.apply(t, null); }
    
}
