package cl.ugly.ds.bloomfilter.hashfunctions;

import java.security.SecureRandom;

import cl.ugly.ds.bloomfilter.HashFunction;

public class SecureRNG implements HashFunction {

	/**
	 * Adopted from Orestes Bloom Filter Library:
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 * 
	 * Generates a hash value using the Secure Java Random Number Generator
	 * It is more random than the normal RNG but more CPU intensive.
	 * The RNG is intialized using the value to be hashed.
	 * 
	 */
	@Override
	public int[] hash(byte[] bytes, int m, int k) {
		int[] positions = new int[k];
		SecureRandom r = new SecureRandom(bytes);
		for (int i = 0; i < k; i++) {
			positions[i] = r.nextInt(m);
		}
		return positions;
	}

}
