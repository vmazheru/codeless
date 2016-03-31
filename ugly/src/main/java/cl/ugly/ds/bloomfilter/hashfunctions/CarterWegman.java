package cl.ugly.ds.bloomfilter.hashfunctions;

import java.math.BigInteger;
import java.util.Random;

import cl.ugly.ds.bloomfilter.HashFunction;

public class CarterWegman implements HashFunction {

	/**
	 * Adopted from Orestes Bloom Filter Library:
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 * 
	 * Generates hash values using the Carter Wegman function 
	 * ({@link http://en.wikipedia.org/wiki/Universal_hashing}),
	 * which is a universal hashing function.
	 * 
	 * It thus has optimal guarantees for the uniformity of
	 * generated hash values. On the downside, the performance is not optimal,
	 * as arithmetic operations on large numbers have to be performed.
	 */
	@Override
    public int[] hash(byte[] bytes, int m, int k) {
		int[] positions = new int[k];
		//BigInteger prime32 = BigInteger.valueOf(4294967279l);
		BigInteger prime64 = BigInteger.valueOf(53200200938189l);
		//BigInteger prime128 = new BigInteger("21213943449988109084994671");
		Random r = getRandom();

		BigInteger v = BigInteger.valueOf(HashUtils.hashBytes(bytes));

		for (int i = 0; i < k; i++) {
			BigInteger a = BigInteger.valueOf(r.nextLong());
			BigInteger b = BigInteger.valueOf(r.nextLong());
			positions[i] = a.multiply(v).add(b).mod(prime64).mod(BigInteger.valueOf(m)).intValue();
		}
		return positions;
	}
	
	private static Random getRandom() {
		return new Random(160598551545387l);
	}

}
