package cl.ugly.ds.bloomfilter.hashfunctions;

/**
 * Adopted from Orestes Bloom Filter library: 
 * http://divinetraube.github.io/Orestes-Bloomfilter/
 * 
 * Generates a hash value using a Cyclic Redundancy Check (CRC32). CRC is
 * designed as a checksum for data integrity not as hash function but
 * exhibits very good uniformity and is relatively fast.
 */
public class CRC32 extends Checksum {
	
	public CRC32() {
		super(new java.util.zip.CRC32());
	}

}
