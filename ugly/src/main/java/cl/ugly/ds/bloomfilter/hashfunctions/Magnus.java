package cl.ugly.ds.bloomfilter.hashfunctions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cl.ugly.ds.bloomfilter.HashFunction;

public class Magnus implements HashFunction {
	
	private MessageDigest hashFunction;
	
	public Magnus(String hashFunctionName) {
		try {
			hashFunction = MessageDigest.getInstance(hashFunctionName);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unknown hash function provided. Use  MD2, MD5, SHA-1, SHA-256, SHA-384 or SHA-512.");
		}		
	}	

	/**
	 * Adopted from Orestes Bloom Filter library: 
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 */
	@Override
    public int[] hash(byte[] bytes, int M, int K) {
		int hashes = K;
		int[] result = new int[hashes];

		int k = 0;
		byte salt = 0;
		while (k < hashes) {
			byte[] digest;
			hashFunction.update(salt);
			salt++;
			digest = hashFunction.digest(bytes);

			for (int i = 0; i < digest.length / 4 && k < hashes; i++) {
				int h = 0;
				for (int j = (i * 4); j < (i * 4) + 4; j++) {
					h <<= 8;
					h |= digest[j] & 0xFF;
				}
				result[k] = Math.abs(h % M);
				k++;
			}
		}
		return result;
	}

}
