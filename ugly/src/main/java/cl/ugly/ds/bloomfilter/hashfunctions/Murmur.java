package cl.ugly.ds.bloomfilter.hashfunctions;

import cl.ugly.ds.bloomfilter.HashFunction;

public class Murmur implements HashFunction {
	
	private final static int SEED_32 = 89478583;

	/**
	 * Adopted from Orestes Bloom Filter library: 
	 * http://divinetraube.github.io/Orestes-Bloomfilter/
	 */
	@SuppressWarnings("incomplete-switch")
    @Override
    public int[] hash(byte[] bytes, int notUsed, int K) {
		int[] positions = new int[K];

		int hashes = 0;
		int lastHash = 0;
		byte[] data = bytes.clone();
		while (hashes < K) {
			// Code taken from: http://dmy999.com/article/50/murmurhash-2-java-port by Derekt

			for (int i = 0; i < bytes.length; i++) {
				if (data[i] == 127) {
					data[i] = 0;
					continue;
				}
				data[i]++;
				break;
			}

			// 'm' and 'r' are mixing constants generated offline.
			// They're not really 'magic', they just happen to work well.
			int m = 0x5bd1e995;
			int r = 24;

			// Initialize the hash to a 'random' value
			int len = data.length;
			int h = SEED_32 ^ len;

			int i = 0;
			while (len >= 4) {
				int k = data[i + 0] & 0xFF;
				k |= (data[i + 1] & 0xFF) << 8;
				k |= (data[i + 2] & 0xFF) << 16;
				k |= (data[i + 3] & 0xFF) << 24;

				k *= m;
				k ^= k >>> r;
				k *= m;

				h *= m;
				h ^= k;

				i += 4;
				len -= 4;
			}

			switch (len) {
			case 3:
				h ^= (data[i + 2] & 0xFF) << 16;
			case 2:
				h ^= (data[i + 1] & 0xFF) << 8;
			case 1:
				h ^= (data[i + 0] & 0xFF);
				h *= m;
			}

			h ^= h >>> 13;
			h *= m;
			h ^= h >>> 15;

			lastHash = HashUtils.rejectionSample(h, m);
			if (lastHash != -1) {
				positions[hashes++] = lastHash;
			}
		}
		return positions;
	}

}
