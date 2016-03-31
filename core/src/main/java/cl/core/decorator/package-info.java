/**
 * This package contains an interface for all decorator types.
 * 
 * Decorators "decorate" functions with additional behavior.
 * Examples would be logging, exception handling, retrying execution etc.
 * 
 * <p>Decorators have overloaded versions of {@code decorate()} method which takes a function and
 * returns the same type of function, which behavior is now "enhanced".
 * 
 * <p>To create a decorator, one need to implement the most general {@code decorate()} method version, 
 * that is the one which accepts and returns a {@code BiFunction} object.  The other {@code decorate()}
 * methods will delegate to it.
 * 
 * <p>Here is an example of a simple decorator:
 * 
 * <pre>{@code
 *    
   public static class DelayDecorator implements Decorator {
        @Override
        public <T, U, R> BiFunction<T, U, R> decorate(BiFunction<T, U, R> f) {
            return (t,u) -> {
              try { Thread.sleep(1000); } catch (InterruptedException e) { }
              return f.apply(t, u);
            };
        }
    
        public static void withDelay(Runnable r) {
            new DelayDecorator().decorate(r).run();
        }
        
        public static void main(String[] args) {
            withDelay(() -> System.out.prinltn("Hello, world!"));
        }
   }
   
   }</pre>
 * 
 */
package cl.core.decorator;
