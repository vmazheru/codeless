package cl.serializers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import cl.core.configurable.Configurable;
import cl.core.util.FileUtils;
import cl.serializers.iterators.DelimitedStringIterator;
import cl.serializers.iterators.JavaIterator;
import cl.serializers.iterators.JsonIterator;
import cl.serializers.iterators.ObjectIterator;
import cl.serializers.iterators.StringIterator;
import cl.serializers.writers.DelimitedStringWriter;
import cl.serializers.writers.JavaWriter;
import cl.serializers.writers.JsonWriter;
import cl.serializers.writers.ObjectWriter;
import cl.serializers.writers.StringWriter;

/**
 * Implementation of {@link Serializer} interface.
 */
final class SerializerImpl<T,R> implements Serializer<T,R> {
    
    private final ObjectIterator<T> iterator;
    private final ObjectWriter<R> writer;
    
    private SerializerImpl(ObjectIterator<T> iterator, ObjectWriter<R> writer) {
        this.iterator = iterator;
        this.writer = writer;
    }
    
    @Override
    public ObjectIterator<T> getIterator() {
        return iterator;
    }
    
    @Override
    public ObjectWriter<R> getWriter() {
        return writer;
    }
    
    @Override
    public void close() throws IOException {
        FileUtils.close(iterator, writer);
    }
    
    /**
     * Serializer object builder 
     */
    static final class SerializerBuilder<T,R> {
     
        private File inputFile;
        private InputStream inputStream;
        private File outputFile;
        private OutputStream outputStream;
        private Optional<Class<T>> iteratorClass;
        private Optional<Configurable<?>> configuration;
        private SerializationType inputSerializationType;
        private SerializationType outputSerializationType;
        
        SerializerBuilder<T,R> withInputFile(File inputFile) {
            this.inputFile = inputFile;
            return this;
        }
        
        SerializerBuilder<T,R> withOutputFile(File outputFile) {
            this.outputFile = outputFile;
            return this;
        }
        
        SerializerBuilder<T,R> withInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }
        
        SerializerBuilder<T,R> withOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }
        
        SerializerBuilder<T,R> withIteratorClass(Optional<Class<T>> iteratorClass) {
            this.iteratorClass = iteratorClass;
            return this;
        }
        
        SerializerBuilder<T,R> withConfiguration(Optional<Configurable<?>> configuration) {
            this.configuration = configuration;
            return this;
        }
        
        SerializerBuilder<T,R> withInputSerializationType(SerializationType inputSerializationType) {
            this.inputSerializationType = inputSerializationType;
            return this;
        }
        
        SerializerBuilder<T,R> withOutputSerializationType(SerializationType outputSerializationType) {
            this.outputSerializationType = outputSerializationType;
            return this;
        }
        
        SerializerBuilder<T,R> withSerializationType(SerializationType serializationType) {
            this.inputSerializationType = serializationType;
            this.outputSerializationType = serializationType;
            return this;
        }
        
        SerializerImpl<T,R> build() {
            
            // check if input / output is set correctly
            boolean inputOutputGood = 
                    inputFile   != null && outputFile   != null && outputStream == null ||
                    inputStream != null && outputFile   != null && outputStream == null ||
                    inputFile   != null && outputStream != null && outputFile   == null ||
                    inputStream != null && outputStream != null && outputFile   == null;

            if (!inputOutputGood) {
                throw new SerializerBuildException("Serializer may have only "
                        + "one input (file or input stream) and one output (file or output stream)");
            }
            
            if (inputSerializationType == null || outputSerializationType == null) {
                throw new SerializerBuildException("Eigher input or output serialization type is not set");
            }
            
            if (inputSerializationType == SerializationType.JSON && !iteratorClass.isPresent()) {
                throw new SerializerBuildException(
                        "Object iterator class must be set when serialization type is " + inputSerializationType);
            }
            
            // create object iterator and object writer for the given input and output serialization types 
            
            ObjectIterator<T> objectIterator = getIterator();
            ObjectWriter<R> objectWriter = getWriter();
            
            // copy configuration from serializer to its object iterator and writer, and lock configurations
            
            configuration.ifPresent(config -> {
                objectIterator.withConfigurationFrom(config);
                objectWriter.withConfigurationFrom(config);
            });
            
            EnumSet<SerializationType> typesWithHeader = EnumSet.of(SerializationType.DELIMITED, SerializationType.STRING);
            if (typesWithHeader.contains(inputSerializationType) && typesWithHeader.contains(outputSerializationType)) {
                List<String> headerLines = new ArrayList<>(objectIterator.get(SerializerConfiguration.numHeaderLines));
                BiConsumer<Integer, String> existingOnHeader = objectIterator.get(SerializerConfiguration.onHeader);
                BiConsumer<Integer, String> newOnHeader = (i, s) -> {
                  existingOnHeader.accept(i,s);
                  headerLines.add(s);
                };
                objectIterator.with(SerializerConfiguration.onHeader, newOnHeader);
                objectWriter.with(SerializerConfiguration.headerLines, headerLines);
            }

            objectIterator.locked();
            objectWriter.locked();
            
            return new SerializerImpl<>(objectIterator, objectWriter);
        }
        
        static <T,R> SerializerBuilder<T,R> basicBuilder(
                SerializationType inputSerializationType,
                SerializationType outputSerializationType,
                Optional<Class<T>> iteratorClass,
                Optional<Configurable<?>> configuration) {
            return new SerializerBuilder<T,R>()
                .withInputSerializationType(inputSerializationType)
                .withOutputSerializationType(outputSerializationType)
                .withIteratorClass(iteratorClass)
                .withConfiguration(configuration);
        }
        
        /*
         * Create an object iterator based on the serializer's serialization type.
         */
        private ObjectIterator<T> getIterator() {
            switch (inputSerializationType) {
                case STRING: {
                    @SuppressWarnings("unchecked")
                    ObjectIterator<T> iter = inputFile != null ?
                        (ObjectIterator<T>)StringIterator.fromFile(inputFile, false) :
                        (ObjectIterator<T>)StringIterator.fromInputStream(inputStream, false);
                    return iter;
                }
                case JSON: return inputFile != null ?
                        JsonIterator.fromFile(inputFile, iteratorClass.get(), false) :
                        JsonIterator.fromInputStream(inputStream, iteratorClass.get(), false);
                case DELIMITED: return inputFile != null ?
                        (ObjectIterator<T>)DelimitedStringIterator.fromFile(inputFile, iteratorClass.get(), false) :
                        (ObjectIterator<T>)DelimitedStringIterator.fromInputStream(inputStream, iteratorClass.get(), false);
                case JAVA: 
                default:  {
                    @SuppressWarnings("unchecked")
                    ObjectIterator<T> iter = inputFile != null ?
                            (ObjectIterator<T>)JavaIterator.fromFile(inputFile, false) :
                            (ObjectIterator<T>)JavaIterator.fromInputStream(inputStream, false);
                    return iter;
                }
            }
        }
        
        /*
         * Create an object writer based on the serializer's serialization type.
         */
        private ObjectWriter<R> getWriter() {
            switch (outputSerializationType) {
                case STRING: {
                    @SuppressWarnings("unchecked")
                    ObjectWriter<R> iter = outputFile != null ?
                        (ObjectWriter<R>)StringWriter.toFile(outputFile, false) :
                        (ObjectWriter<R>)StringWriter.toOutputStream(outputStream, false);
                        return iter;
                }
                case JSON: return outputFile != null ?
                        JsonWriter.toFile(outputFile, false) :
                        JsonWriter.toOutputStream(outputStream, false);
                case DELIMITED: {
                    @SuppressWarnings("unchecked")
                    ObjectWriter<R> writer = outputFile != null ? 
                        (ObjectWriter<R>)DelimitedStringWriter.toFile(outputFile, iteratorClass.get(), false) :
                        (ObjectWriter<R>)DelimitedStringWriter.toOutputStream(outputStream, iteratorClass.get(), false);
                    return writer;
                }
                case JAVA:
                default: {
                    @SuppressWarnings("unchecked")
                    ObjectWriter<R> writer = outputFile != null ?
                            (ObjectWriter<R>)JavaWriter.toFile(outputFile, false) :
                            (ObjectWriter<R>)JavaWriter.toOutputStream(outputStream, false);
                    return writer;
                }
                        
            }
        }
    }
    
}
