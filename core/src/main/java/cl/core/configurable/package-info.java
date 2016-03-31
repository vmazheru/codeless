/**
 * This package contains classes which help creating configurable object in a general way.
 * 
 * <p>The idea is to make an object expose enumeration of strongly-typed keys, which may be set
 * in a builder-like fashion, for example:
 * 
 * <pre>{@code
 * 
 * class HTTPClient extends ConfigurableObject<HTTPClient> {
 *   public static Key<Integer> socketTimeout = new Key<>(){}
 *   public static Key<Boolean> logging       = new Key<>(){}
 *   
 *   ...
 * }
 * 
 *  HTTPClient c = new HTTPClient()
 *                  .with(socketTimeout, 60_000)
 *                  .with(logging, true)
 *                  .locked();
 * 
 * }</pre>
 * 
 * <p> The responsibility of an implementation class is usually to define configuration
 * keys as shown in the example above, while {@code ConfigurableObject} will take care of
 * storing and retrieving the configuration values.
 * 
 * <p>Ideally, a configurable object should provide default values for its keys or behave
 * somewhat sensibly when certain properties are not set.
 */
package cl.core.configurable;
