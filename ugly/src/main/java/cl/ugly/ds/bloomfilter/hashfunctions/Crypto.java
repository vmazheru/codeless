package cl.ugly.ds.bloomfilter.hashfunctions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

import cl.ugly.ds.bloomfilter.HashFunction;

/**
 * Cryptographic hash function
 */
public class Crypto implements HashFunction {
	
	private MessageDigest hashFunction;
	
	/**
	 * Construct a cryptographic hash function 
	 * @param hashFunctionName Hash method.  For example, MD2, MD5, SHA-1, SHA-256, SHA-384 or SHA-512
	 */
	public Crypto(String hashFunctionName) {
		try {
			hashFunction = MessageDigest.getInstance(hashFunctionName);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Unknown hash function provided. Use  MD2, MD5, SHA-1, SHA-256, SHA-384 or SHA-512.");
		}		
	}

	/**
	 * Adopted from Orestes bloom filter library: 
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 *
	 */
	@Override
	public int[] hash(byte[] value, int m, int k) {
		int[] positions = new int[k];

		int computedHashes = 0;
		byte[] digest = new byte[0];
		while (computedHashes < k) {
			hashFunction.update(digest);
			digest = hashFunction.digest(value);
			BitSet hashed = BitSet.valueOf(digest);

			// Convert the hash to numbers in the range [0,m)
			// Size of the Bloomfilter rounded to the next power of two
			int filterSize = 32 - Integer.numberOfLeadingZeros(m);
			// Computed hash bits
			int hashBits = digest.length * 8;
			// Split the hash value according to the size of the Bloomfilter
			for (int split = 0; split < (hashBits / filterSize) && computedHashes < k; split++) {
				int from = split * filterSize;
				int to = (split + 1) * filterSize;
				BitSet hashSlice = hashed.get(from, to);
				// Bitset to Int
				long[] longHash = hashSlice.toLongArray();
				int intHash = longHash.length > 0 ? (int) longHash[0] : 0;
				// Only use the position if it's in [0,m); 
				//  Called rejection sampling
				if (intHash < m) {
					positions[computedHashes] = intHash;
					computedHashes++;
				}
			}
		}

		return positions;
	}
}
