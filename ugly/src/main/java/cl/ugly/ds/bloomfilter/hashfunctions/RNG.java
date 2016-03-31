package cl.ugly.ds.bloomfilter.hashfunctions;

import java.util.Random;

import cl.ugly.ds.bloomfilter.HashFunction;

public class RNG implements HashFunction {

	/**
	 * Adopted from Orestes Bloom Filter Library:
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 *
	 * Generates a hash value using the Java Random Number Generator (RNG) which
	 * is a Linear Congruential Generator (LCG), implementing the following
	 * formula: 
	 * <br/>
	 * <code>number_i+1 = (a * number_i + c) mod m</code><br/>
	 * <br/>
	 * The RNG is intialized using the value to be hashed.
	 */
	@Override
    public int[] hash(byte[] bytes, int m, int k) {
		int[] positions = new int[k];
		Random r = new Random(HashUtils.hashBytes(bytes));
		for (int i = 0; i < k; i++) {
			positions[i] = r.nextInt(m);
		}
		return positions;    
	}

}
