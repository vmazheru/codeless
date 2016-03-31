package cl.ugly.ds.bloomfilter;

/**
 * Instances of this interface provide hashing functionality to
 * bloom filter. 
 */
public interface HashFunction {
	
	int[] hash(byte[] bytes, int m, int k);

}
