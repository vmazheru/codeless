package cl.ugly.ds.bloomfilter;

import java.util.BitSet;
import java.util.Collection;

import cl.ugly.ds.bloomfilter.hashfunctions.Magnus;

/**
 * Bloom filter implementation.
 * Instances of this class are thread safe 
 *
 * Adopted and refactored from Orestes Bloom Filter library: 
 * http://divinetraube.github.io/Orestes-Bloomfilter/
 * 
 * In short, bloom filter works as follows:
 * 	1) Calculate one or more hash values for a string (by using a given hash function, like MD5)
 *  2) Convert hash values into array of bit positions
 *  3) Set (for add) or check (for contains) bits in the bit set 
 */
public class BloomFilter {
	
	private final BitSet bits;
	private final HashFunction hashFunction;
	private final int m;                       // number of bits
	private final int k;                       // number of hashFunctions
	
	private int size;
	private int expected;
	
	public BloomFilter(int expected, double tolerableFalsePositiveRate) {
		this(expected, tolerableFalsePositiveRate, new Magnus("SHA-512"));
	}
	
	public BloomFilter(int expected, double tolerableFalsePositiveRate, HashFunction hashFunction) {
		this(optimalM(expected, tolerableFalsePositiveRate), 
			 optimalK(expected, optimalM(expected, tolerableFalsePositiveRate)), hashFunction, expected);
	}
	
	private BloomFilter(int m, int k, HashFunction hashFunction, int expected) {
		this.m = m;
		this.k = k;
		this.hashFunction = hashFunction;
		this.expected = expected;
		bits = new BitSet(m);
	}
	
	public synchronized void add(String s) {
		doAdd(s);
	}

	public synchronized void addAll(Collection<String> values) {
		for (String s : values) doAdd(s);
	}
	
	public synchronized boolean contains(String s) {
		return contains(s.getBytes());
	}

	public int getM() {
		return m;
	}
	
	public int getK() {
		return k;
	}
	
	public int size() {
		return size;
	}
	
	public int getExpected() {
		return expected;
	}
	
	public HashFunction getHashFunction() {
		return hashFunction;
	}
	
	/**
	 * Returns the probability of a false positive after given number of add operations (inserts) as:
	 * (1 - e^(-k * insertedElements / m)) ^ k
	 */
	public double getFalsePositiveProbability(int inserted) {
		return Math.pow((1 - Math.exp(-k * (double) inserted / m)), k);
	}

	@Override
	public String toString() {
		return "[m=" + m + ", k=" + k + "]";
	}
	
	/*
	 * Hash an array of bytes, and set a bit for each position in the hash.
	 */
	private void doAdd(String s) {
		byte[] value = s.getBytes();
		for (int position : hashFunction.hash(value, m, k)) {
			bits.set(position);
		}
		size++;
	}
	
	/*
	 * Hash an array of bytes, and check if every position in the hash is set
	 */
	private boolean contains(byte[] value) {
		for (int position : hashFunction.hash(value, m, k)) {
			if(!bits.get(position)) return false;
		}
		return true;
	}
	
	/*
	 * Calculate optimal filter size (number of bits) 
	 * for expected number of elements and tolerable false positive rate
	 */
	private static int optimalM(int expectedNumElements, double tolerableFalsePositiveRate) {
		return (int) Math.ceil(-1 * expectedNumElements * Math.log(tolerableFalsePositiveRate) / 
				     Math.pow(Math.log(2), 2));
	}
	
	/*
	 * Calculate optimal number of hash functions for expected number of elements and filter size
	 */
	private static int optimalK(int expectedNumElements, int m) {
		return (int) Math.ceil(Math.log(2) * m / expectedNumElements);
	}	

}
