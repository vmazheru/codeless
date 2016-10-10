/**
 * This package contains {@code Serializer} interface and support classes.
 * 
 * <p>Serializers support work flows which include reading objects from some source, processing
 * these objects, and writing these objects (possibly modified) to some destination.  The source here
 * may be a file or an input stream, while the destination may be another file or an output stream.
 * 
 * <p>The following example shows how to use {@code Serializer} to read objects of some 
 * type {@code Person} from their Java 8 serialized state in a file, and save them in a text file 
 * by mapping them to their string representations.  Note the use of java 'try-with-resource' construct
 * which automatically closes the serializer's object iterator and writer.
 * 
 * <pre>{@code
        File input  = getMyJavaInputFile();
        File output = getMyStringOutputFile();
        
        try (Serializer<Person, String> serializer = serializer(
                input,
                output,
                SerializationType.JAVA,
                SerializationType.STRING)) {
            serializer.map(Objects::toString);
        }
 * }</pre>
 * 
 * @see cl.files.serializers.Serialzer
 */
package cl.files.serializers;
