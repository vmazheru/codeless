package cl.ugly.ds.bloomfilter.hashfunctions;

import cl.ugly.ds.bloomfilter.HashFunction;

public class LCG implements HashFunction {
	
	private static final long multiplier = 0x5DEECE66DL;
	private static final long addend = 0xBL;
	private static final long mask = (1L << 48) - 1;
	
	/**
	 * Adopted from Orestes Bloom Filter library: 
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 * 
	 * Uses the very simple LCG (Linear Congruential Generator) scheme.
	 * This method is intended to be employed if the bloom filter has to be used
	 * in a language which doesn't support any of the other hash functions. This
	 * hash function can then easily be implemented.
	 */
	@Override
    public int[] hash(byte[] bytes, int m, int k) {
		int reduced = Math.abs(HashUtils.hashBytes(bytes));
		// Make number positive
		// Handle the special case: smallest negative number is itself as the
		// absolute value
		if (reduced == Integer.MIN_VALUE) {
			reduced = 42;
		}

		// Calculate k numbers iterativeley
		int[] positions = new int[k];
		long seed = reduced;
		for (int i = 0; i < k; i++) {
			// LCG formula: x_i+1 = (multiplier * x_i + addend) mod mask
			seed = (seed * multiplier + addend) & mask;
			positions[i] = (int) (seed >>> (48 - 30)) % m;
		}
		return positions;
	}

}
