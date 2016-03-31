package cl.ugly.ds.bloomfilter.hashfunctions;


final class HashUtils {
	
	private static final long FNV_PRIME = 16777619;
	private static final long FNV_OFFSET_BASIS = 2166136261l;
	
	private HashUtils(){}

	/**
	 * Adopted from Orestes Bloom Filter library: 
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 * 
	 * Uses the Fowler-Noll-Vo (FNV) hash function to generate a hash value from
	 * a byte array. It is superior to the standard implementation in
	 * and can be easily implemented in most languages.
	 */
	static int hashBytes(byte a[]) {
		// 32 bit FNV constants. Using longs as Java does not support unsigned datatypes.
		if (a == null) return 0;

		long result = FNV_OFFSET_BASIS;
		for (byte element : a) {
			result = (result * FNV_PRIME) & 0xFFFFFFFF;
			result ^= element;
		}

		// a Java standard alternative to this function would be just
		// return Arrays.hashCode(a);
		return (int) result;
	}
	
	/**
	 * Adopted from Orestes Bloom Filter library: 
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 * 
	 * Performs rejection sampling on a random 32bit Java int
	 * (sampled from Integer.MIN_VALUE to Integer.MAX_VALUE).
	 * 
	 * @param r random int
	 * @param m interval size
	 * @return the number downsampled to interval [0, m]. Or -1 if it has to be rejected.
	 */
	static int rejectionSample(int r, int m) {
		int random = Math.abs(r);
		if (random > (2147483647 - 2147483647 % m) || random == Integer.MIN_VALUE)
			return -1;
		return random % m;
	}	

}
