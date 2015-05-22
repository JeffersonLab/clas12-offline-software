package com.nr.ran;

import org.netlib.util.intW;

public class HashAll {
    private HashAll() {
    }

    /**
     * Pseudo-DES hashing of the 64-bit word (lword,rword). Both 32-bit
     * arguments are returned hashed on all bits.
     * 
     * @param lword
     * @param rword
     */
    public static void psdes(final intW lword, final intW rword) {
	final int NITER = 2;
	final int[] c1 = { 0xbaa96887, 0x1e17d32c, 0x03bcdc3c, 0x0f33d1b2 };
	final int[] c2 = { 0x4b0f3b58, 0xe874f0c3, 0x6955c5a6, 0x55a7ca46 };
	int i, ia, ib, iswap, itmph = 0, itmpl = 0;
	for (i = 0; i < NITER; i++) {
	    ia = (iswap = rword.val) ^ c1[i];
	    itmpl = ia & 0xffff;
	    itmph = ia >>> 16;
	    ib = itmpl * itmpl + ~(itmph * itmph);
	    rword.val = lword.val
		    ^ (((ia = (ib >>> 16) | ((ib & 0xffff) << 16)) ^ c2[i]) + itmpl
			    * itmph);
	    lword.val = iswap;
	}
    }

    /**
     * Replace the array arr by a same-sized hash, all of whose bits depend on
     * all of the bits in arr. Uses psdes for the mutual hash of two 32-bit
     * words.
     * 
     * @param arr
     */
    public static void hashall(final int[] arr) {
	int m = arr.length, n = m - 1;
	n |= n >> 1;
	n |= n >> 2;
	n |= n >> 4;
	n |= n >> 8;
	n |= n >> 16;
	n++;
	int nb = n, nb2 = n >> 1, j, jb;
	if (n < 2)
	    throw new IllegalArgumentException("size must be > 1");
	while (nb > 1) {
	    for (jb = 0; jb < n - nb + 1; jb += nb)
		for (j = 0; j < nb2; j++)
		    if (jb + j + nb2 < m) {
			intW lword = new intW(arr[jb + j]);
			intW rword = new intW(arr[jb + j + nb2]);
			psdes(lword, rword);
			arr[jb + j] = lword.val;
			arr[jb + j + nb2] = rword.val;
		    }
	    nb = nb2;
	    nb2 >>= 1;
	}
	nb2 = n >> 1;
	if (m != n)
	    for (j = nb2; j < m; j++) {
		intW lword = new intW(arr[j]);
		intW rword = new intW(arr[j - nb2]);
		psdes(lword, rword);
		arr[j] = lword.val;
		arr[j - nb2] = rword.val;
	    }
    }
}
