package cl.ugly.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cl.ugly.file.Serializers.Serializer.Type;

public final class Serializers {
	
	
	/**
	 * The idea of this interfaces is to have classes, which implement
	 * serialization/deserialization of objects in serailization-technology-agnostic fashion.
	 * 
	 * Serializers should be used when there is a need to process multiple objects, but keeping them in 
	 * memory is not an option. 
	 *  
	 * The typical use of a serializer object is shown in the following code (read/modify/write):
	 * <pre>{@code
	 * 	Serializer<MyClass> ser = Serializers.getReadWriteSerializer(JAVA, myInputFile, myOutputFile, 500, MyClass.class);
	 *  try {
	 *    while(!ser.done()) {
	 *      List<MyClass> batch = ser.read();
	 *      //  process, modify your objects
	 *      ser.write(batch);
	 *    }
	 *  } finally {
	 *    ser.close();
	 *  }
	 *  }</pre>
	 *  
	 *  Serializers may be read/write-only.  In that case the appropriate methods should throw UnsupportedOperationException
	 *  
	 *  Here is the typical use of read-only serializer:
	 *  
	 * <pre>{@code
	 * 	Serializer<MyClass> ser = Serializers.getReadSerializer(JSON, myInputFile, 500, MyClass.class);
	 *  try {
	 *    while(!ser.done()) {
	 *      List<MyClass> batch = ser.read();
	 *      //process objects, or save them in some other than serialized store
	 *    }
	 *  } finally {
	 *    ser.close();
	 *  }
	 * }</pre>
	 * 
	 * Write-only serializers needs to be used, for example, when creating serialized store in the first place:
	 * 
	 * <pre>{@code
	 *   Serializer<MyClass> ser = Serializers.getWriteSerializer(JAVA, myOutputFile, 500);
	 *   BufferedReader in = null;
	 *   try {
	 *   	in = getMyReader();
	 *      List<MyClass> myObjects = readBatchFromInputStream(in, 500);
	 *      ser.write(myObjects);
	 *   } finally {
	 *     if(in != null) in.close();
	 *     ser.close();
	 *   }
	 * }</pre>
	 * 
	 * Note, that method done() is not supported (since it does not make sense here) for write-only serializers 
	 */
	public interface Serializer<T> extends Closeable {
	    
        /**
         * Enumeration of supported serializer types.  Here STRING serialization simply means reading/writing
         * String objects with readLine()/println()
         */
        public enum Type { STRING, JAVA, JSON }     
	    
		
		/**
		 * Use this interface, when you need to discard curtain objecs when reading them from serialization file.
		 */
		public interface ReadFilter<T> {
			boolean accept(T object);
		}

		/**
		 * Deserialize list of objects from serialization store.  The list size will be that of a batch size parameter
		 * given at serializer construction time;
		 */
		List<T> read() throws Exception;
		
		/**
		 * Serialize objects 
		 */
		void write(List<T> objects) throws Exception;
		
		/**
		 * Return true if there are not more objects after last call to read() to deserialize.
		 * Not supported for write-only serializers. 
		 */
		boolean done();
		
		/**
		 * Only read objects for which the readFilter.accept() is true.
		 * Not supported for write-only serializers 
		 */
		void setReadFilter(ReadFilter<T> readFilter);
		
		/**
		 * Convert a read serializer to a stream.  For write and read/write serializers throw
		 * UnsupportedOperationException
		 */
		Stream<T> toStream();
		
	}
	
	/**
	 * Construct read/write serializer
	 * @param type              Serializer type
	 * @param input             Input file (must contain objects, which serializer under construction understands)
	 * @param output            Output file (will be overwritten if exists)
	 * @param batchSize         Number of objects to read/write in one shot
	 * @param klass             Object class (JSON serializer needs this in order to read objects)
	 */
	public static <T> Serializer<T> getReadWriteSerializer(
			Type type, File input, File output, int batchSize, Class<T> klass) throws IOException {
		return getSerializer(type, input, output, batchSize, klass);
	}
	
	/**
	 * Construct read-only serializer.  The list of parameters is the same as for read/write serializer, 
	 * except for there is no need for the output file. 
	 */
	public static <T> Serializer<T> getReadSerializer(
			Type type, File input, int batchSize, Class<T> klass) throws IOException {
		return getSerializer(type, input, null/*null output file - read only*/, batchSize, klass);
	}

	/**
	 * Construct write-only serializer.  This serializer does not need output file parameter, as well as
	 * object class, since class information is available to the serializer at run-time.  
	 */
	public static <T> Serializer<T> getWriteSerializer(Type type, File output) throws IOException {
		return getSerializer(type, null/*null input file - write only*/, output, -1, 
				null /*null class - class is not needed for serialization*/);
	}	
	
	private static <T> Serializer<T> getSerializer(
			Type type, File input, File output, int batchSize, Class<T> klass) throws IOException {
		switch (type) {
		case STRING : return new StringSerializer<>(input, output, batchSize);
		case JAVA   : return new JavaSerializer  <>(input, output, batchSize);
		case JSON   : return new JsonSerializer  <>(input, output, batchSize, klass);
		default     : return new StringSerializer<>(input, output, batchSize);
		}
	}
	
	private Serializers(){}
	
	/*
	 * This class implements most of the serializer's logic, except for actual read/write operations
	 * which are different for different serializer types.
	 */
	private static abstract class SerializerSupport<T> implements Serializer<T> {
		
		private int batchSize;
		private boolean canRead;
		private boolean canWrite;
		private boolean openForRead;
		private boolean done;
		private ReadFilter<T> readFilter;
		
		protected BufferedReader reader;
		protected PrintWriter writer;
		protected ObjectInputStream in;
		protected ObjectOutputStream out;

		/*
		 * Instantiate serializer.
		 * If both input/output files are given, create in/out streams, and set canWrite/canRead to true.
		 * If only input file is given, instantiate "in" streams only and set canWrite to false.
		 * If only output file is given, instantiate "out" streams only and set canRead to false.
		 * If binary parameter is true (for JAVA serializer), instantiate binary object streams, else
		 * (for STRING/JSON serializers), instantiate text streams.
		 */
		SerializerSupport(File input, File output, int batchSize, boolean binary) throws IOException {
			this.batchSize = batchSize;
			canRead  = (input != null);
			canWrite = (output != null);
			if(binary) {
				if(canRead)  in  = new ObjectInputStream(new BufferedInputStream(new FileInputStream(input)));
				if(canWrite) out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
			} else {
				if(canRead)  reader = new BufferedReader(new FileReader(input));
				if(canWrite) writer = new PrintWriter(output);
			}
		}
		
		// serializers of different types must implement these methods in their protocol-specific manner
		// JSON/String serializers will use protected reader/writer objects and JAVA serializer will use
		// protected ObjectStreams objects
		protected abstract T readNext() throws Exception;
		protected abstract void writeNext(T object) throws Exception;

		@Override
		public void setReadFilter(ReadFilter<T> readFilter) {
			if(!canRead) throw new UnsupportedOperationException("Write-only serializer");
			this.readFilter = readFilter;
		}

		@Override
		public List<T> read() throws Exception {
			if(!canRead) throw new UnsupportedOperationException("Write-only serializer");
			openForRead = true;
			try {
				List<T> result = new ArrayList<>(batchSize);
				int i = 0;
				T object = null;
				while(i++ < batchSize && !(done = !((object = readNext()) != null))) {
					if(readFilter == null || readFilter.accept(object)) {
						result.add(object);
					}
				}
				return result;
			} catch (Exception e) {
				close();
				throw e;
			}
		}
		
		@Override
		public void write(List<T> objects) throws Exception {
			if(!canWrite) throw new UnsupportedOperationException("Read-only serializer");
			try {
				for(T object : objects) {
					if(object != null) {
						writeNext(object);
					}
				}
			} catch (Exception e) {
				close();
				throw e;
			}
		}
		
		@Override
		public boolean done() {
			if(!canRead) throw new UnsupportedOperationException("Write-only serialized can't report done()");
			return done;
		}
		
		@Override
		public void close() {
			try { closeOut(); } catch(Exception ex) { handleException(ex); }
			try { closeIn();  } catch(Exception ex) { handleException(ex); }
		}
		
		@Override
		public Stream<T> toStream() {
		    if (canWrite) {
		        throw new UnsupportedOperationException("cannot convert 'write' serializer to a stream");
		    }
		    if (openForRead) {
		        throw new IllegalStateException("cannot make a stream out of serializer which has been used for reading");
		    }
		    return StreamSupport.stream(new ReadSerializerSpliterator(), false);
		}
		
		private void closeIn() throws IOException {
			if(in != null) {
				in.close();
				in = null;
			} 
			if(reader != null) {
				reader.close();
				reader = null;
			}
		}
		
		private void closeOut() throws IOException {
			if(out != null) {
				out.close();
				out = null;
			}
			if(writer != null) {
				writer.close();
				writer = null;
			}
		}
		
		private static void handleException(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
        private class ReadSerializerSpliterator implements Spliterator<T> {
            private Iterator<T> currentList;
            
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (currentList == null || !currentList.hasNext()) {
                    if (done) return false;
                    try {
                        currentList = read().iterator();
                        if (!currentList.hasNext()) return false;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                action.accept(currentList.next());
                return true;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL;
            }
        }
		
	}
	
	/*
	 * Implements serialization logic by reading/writing plain strings
	 */
	private static class StringSerializer<T> extends SerializerSupport<T> {
		StringSerializer(File input, File output, int batchSize) throws IOException {
			super(input, output, batchSize, false);
		}

		@Override
		@SuppressWarnings("unchecked")
		protected T readNext() throws IOException {
			return (T)reader.readLine();
		}

		@Override
		protected void writeNext(T object) {
			writer.println(object);
		}
	}
	
	/*
	 * Implements Java serialization
	 */
	private static class JavaSerializer<T> extends SerializerSupport<T> {
		
		private int count;
		
		JavaSerializer(File input, File output, int batchSize) throws IOException {
			super(input, output, batchSize, true);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		protected T readNext() throws Exception {
			try {
				return (T)in.readObject();
			} catch (EOFException e) {
				return null;
			}
		}

		@Override
		protected void writeNext(T object) throws Exception {
			out.writeObject(object);
			// when writing multiple objects to a single file using one ObjectOutputStream object,
			// OutOfMemeoryError may occur, since ObjectOutputStream holds references to all objects written
			// in memory till it is closed or reset() is called
			if(count++ > 10000) {
				count = 0;
				out.reset();
			}
		}
	}	
	
	/*
	 * Implements JSON serialization
	 */
	private static class JsonSerializer<T> extends SerializerSupport<T> {
	    
	    @SuppressWarnings("unused")
        private Class<T> klass;
	    
	    JsonSerializer(File input, File output, int batchSize, Class<T> klass) throws IOException {
	        super(input, output, batchSize, false);
	        this.klass = klass;
	    }

        @Override
        protected T readNext() throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected void writeNext(T object) throws Exception {
            // TODO Auto-generated method stub
            
        }
		
	    /*
	    
		private static ObjectMapper objectMapper;
		private Class<T> klass;
		
		// the object mapper is set to use fields rather then accessor methods to behave similarly to the JAVA 
		// serializer.
		// whenever an object fields is marked as transient for JAVA serializer, it should be also marked
		// @JsonIgnore for JSON serializer
		private static ObjectMapper getObjectMapper() {
			if(objectMapper == null) {
		        objectMapper = new ObjectMapper();
		        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
		                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
		                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
		                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
		                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
		                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));		        
			}
			return objectMapper;
		}
		
		JsonSerializer(File input, File output, int batchSize, Class<T> klass) throws IOException {
			super(input, output, batchSize, false);
			this.klass = klass;
		}

		@Override
		protected T readNext() throws Exception {
			String s = reader.readLine();
			if(s != null) {
				return getObjectMapper().readValue(s, klass);
			}
			return null;
		}

		@Override
		protected void writeNext(T object) throws Exception {
			String s = getObjectMapper().writeValueAsString(object);
			writer.println(s);
		}
		
		*/
	}	
}
