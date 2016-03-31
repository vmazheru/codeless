package cl.ugly.ds.bloomfilter.hashfunctions;

/**
 * Adopted from Orestes Bloom Filter library: 
 * http://divinetraube.github.io/Orestes-Bloomfilter/
 *
 * Generates a hash value using the Adler32 Checksum algorithm. Adler32 is
 * comaprable to CRC32 but is faster at the cost of a less uniform
 * distribution of hash values.
 */
public class Adler32 extends Checksum {
	
	public Adler32() {
		super(new java.util.zip.Adler32());
	}

}
