package cl.ugly.ds.bloomfilter.hashfunctions;

import cl.ugly.ds.bloomfilter.HashFunction;

abstract class Checksum implements HashFunction {
	
	private final static int SEED_32 = 89478583;
	
	private java.util.zip.Checksum cs;
	
	Checksum(java.util.zip.Checksum cs) {
		this.cs = cs;
	}

	@Override
    public int[] hash(byte[] bytes, int m, int k) {
		int[] positions = new int[k];
		int hashes = 0;
		int salt = 0;
		while (hashes < k) {
			cs.reset();
			cs.update(bytes, 0, bytes.length);
			// Modify the data to be checksummed by adding the number of already
			// calculated hashes, the loop counter and a static seed
			cs.update(hashes + salt++ + SEED_32);
			int hash = HashUtils.rejectionSample((int) cs.getValue(), m);
			if (hash != -1) {
				positions[hashes++] = hash;
			}
		}
		return positions;
	}

}
