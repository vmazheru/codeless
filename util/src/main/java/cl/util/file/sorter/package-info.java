/**
 * This package contains {@link FileSorter} interface and its implementations.
 * 
 * <p>In its essence, a file sorter exposes just one method, {@code sort()}, but constructing an
 * object of {@link FileSorter} interface may be somewhat involving, since it requires an input and an output
 * file, both of which might be in one of the supported serialization formats. Also, since {@link FileSorter}
 * interface extends {@link cl.core.configurable.Configurable}, constructing a file sorter also may require setting certain 
 * configuration values.
 * 
 * <p>Since sorting a file involves reading a source file and writing a destination file, file sorters
 * use {@code Serializer} objects behind the scenes to do that. Depending on serializer's framework
 * make file sorters able to not only sort stings in text files, but also objects stored as JSON
 * or using Java serialization mechanism. The requirements is though that these objects implement 
 * {@code Comparable} interface, or a separate {@code Comparator} object is passed.
 * 
 * <p>There are two principally different implementations of {@link FileSorter} interface. They are:
 * <ul>
 *   <li>{@link InMemoryFileSorter}. This file sorter loads the entire file into memory, sorts it, 
 *        and writes to a destination file.
 *   </li>
 *   <li>{@link ExternalMergeFileSorter}. This file sorter splits the source file into smaller
 *       files each of which is sorted, and then merges them into a destination file.
 *   </li>
 * </ul>
 * 
 * <p>There is also a non-public {@code DefaultFileSorter} class, which switches between the two implementations
 * based on the input file size. This is the default file sorter implementation.
 *  
 *  <p>Using a file sorter may be as simple as
 *  <pre>{@code
 *     FileSorter.sort(myFile); //sort a text file
 *  }</pre>
 *  <p>or more complex like
 *  <pre>{@code
        FileSorter<Person> sorter = FileSorter.getFileSorter(
                inputFile, outputFile, SerializationType.JSON, Person.class, false)
                    .with(SerializerConfiguration.jsonMapper, myBetterJsonMapper)
                    .with(FileSorter.comparator, myPersonComparator)
                    .with(FileSorter.inMemorySizeThreshold, 10 * 1024 * 1024L).locked();
        sorter.sort();
 *  }</pre>
 *  
 *  <p>It is possible to instantiate a specific file sorter implementation classes instead of the
 *  default file sorter, but it might be easier to set {@link FileSorter#inMemorySizeThreshold}
 *  configuration value to do just that. Setting it to zero will force using of {@link ExternalMergeFileSorter}
 *  while setting it to {@code Long.MAX_VALUE} will force using of {@link InMemoryFileSorter}.
 */
package cl.util.file.sorter;
