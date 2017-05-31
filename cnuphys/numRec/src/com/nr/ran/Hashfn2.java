package com.nr.ran;

public class Hashfn2 {
    static long[] hashfn_tab = new long[256];

    private long h;

    // int n; final int nn

    public Hashfn2() { //
	// n = nn;
	// if (n == 1) n = 0;
	h = 0x544B2FBACAAF1684L;
	for (int j = 0; j < 256; j++) {
	    for (int i = 0; i < 31; i++) {
		h = (h >>> 7) ^ h;
		h = (h << 11) ^ h;
		h = (h >>> 10) ^ h;
	    }
	    hashfn_tab[j] = h;
	}
    }

    public long fn(final byte[] key) {
	h = 0xBB40E64DA205B064L;
	int j = 0;
	while (j < key.length) {
	    h = (h * 7664345821815920749L) ^ hashfn_tab[key[j] & 0xFF];
	    j++;
	}
	return h & 0x7FFFFFFFFFFFFFFFL;
    }

}
