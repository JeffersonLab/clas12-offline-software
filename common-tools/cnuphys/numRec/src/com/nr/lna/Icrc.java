package com.nr.lna;

/**
 * cyclic redundancy checksum Copyright (C) Numerical Recipes Software 1986-2007
 * Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Icrc {
    int jcrc, jfill, poly;
    static int[] icrctb = new int[256];

    public Icrc(final int jpoly) {
	this(jpoly, true);
    }

    public Icrc(final int jpoly, final boolean fill) {
	jfill = fill ? 255 : 0;

	int j;
	int[] okpolys = { 0x755B, 0xA7D3, 0x8005, 0x1021, 0x5935, 0x90D9,
		0x5B93, 0x2D17 };
	poly = okpolys[jpoly & 7];
	for (j = 0; j < 256; j++) {
	    icrctb[j] = icrc1(j << 8, (byte) 0);
	}
	jcrc = (jfill | (jfill << 8));
    }

    public int crc(final byte[] bufptr) {
	jcrc = (jfill | (jfill << 8));
	return concat(bufptr);
    }

    public int concat(final byte[] bufptr) {
	int j, cword = jcrc, len = bufptr.length;
	for (j = 0; j < len; j++) {
	    cword = icrctb[(bufptr[j] & 0xFF) ^ hibyte(cword)]
		    ^ (lobyte(cword) << 8);
	}
	return jcrc = cword;
    }

    public int icrc1(final int jcrc, final byte onech) {
	int i;
	int ans = (jcrc ^ onech << 8);
	for (i = 0; i < 8; i++) {
	    if ((ans & 0x8000) != 0)
		ans = (ans <<= 1) ^ poly;
	    else
		ans <<= 1;
	    ans &= 0xffff;
	}
	return ans;
    }

    public int lobyte(final int x) {
	return (x & 0xff);
    }

    public int hibyte(final int x) {
	return (x >>> 8) & 0xff;
    }

    public static boolean decchk(final byte[] str, final byte[] ch, int nn) {
	byte c;
	int j, k = 0, m = 0, n = str.length;
	int[][] ip = { { 0, 1, 5, 8, 9, 4, 2, 7 }, { 1, 5, 8, 9, 4, 2, 7, 0 },
		{ 2, 7, 0, 1, 5, 8, 9, 4 }, { 3, 6, 3, 6, 3, 6, 3, 6 },
		{ 4, 2, 7, 0, 1, 5, 8, 9 }, { 5, 8, 9, 4, 2, 7, 0, 1 },
		{ 6, 3, 6, 3, 6, 3, 6, 3 }, { 7, 0, 1, 5, 8, 9, 4, 2 },
		{ 8, 9, 4, 2, 7, 0, 1, 5 }, { 9, 4, 2, 7, 0, 1, 5, 8 } };

	int[][] ij = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
		{ 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 },
		{ 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 },
		{ 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 },
		{ 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 },
		{ 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 },
		{ 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 },
		{ 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 },
		{ 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 },
		{ 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

	for (j = 0; j < n; j++) {
	    c = str[j];
	    if (c >= 48 && c <= 57)
		k = ij[k][ip[(c + 2) % 10][7 & m++]];
	}
	for (j = 0; j < 10; j++)
	    if (ij[k][ip[j][m & 7]] == 0)
		break;
	ch[nn] = (byte) (j + 48);
	return k == 0;
    }

}
