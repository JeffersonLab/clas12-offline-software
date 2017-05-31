package com.nr.lna;

import static com.nr.NRUtil.*;
import org.netlib.util.intW;

/**
 * Huffman Coding and Compression of Data
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Huffcode {
    int nch, nodemax, mq;
    int ilong, nlong;
    int[] ncod, left, right;
    int[] icod;
    int[] setbit = new int[32];

    /**
     * Given the frequency of occurrence table nfreq[0..nnch-1] for nnch charac-
     * ters, constructs the Huffman code. Also sets ilong and nlong as the
     * character number that produced the longest code symbol, and the length of
     * that symbol.
     * 
     * @param nnch
     * @param nfreq
     */
    public Huffcode(final int nnch, final int[] nfreq) {
	nch = nnch;
	mq = 2 * nch - 1;
	icod = new int[mq];
	ncod = new int[mq];
	left = new int[mq];
	right = new int[mq];

	int ibit, j, node, k, n, nused;
	int[] index = new int[mq];
	int[] nprob = new int[mq];
	int[] up = new int[mq];
	for (j = 0; j < 32; j++)
	    setbit[j] = 1 << j;
	for (nused = 0, j = 0; j < nch; j++) {
	    nprob[j] = nfreq[j];
	    icod[j] = ncod[j] = 0;
	    if (nfreq[j] != 0)
		index[nused++] = j;
	}
	for (j = nused - 1; j >= 0; j--)
	    heep(index, nprob, nused, j);
	k = nch;
	while (nused > 1) {
	    node = index[0];
	    index[0] = index[(nused--) - 1];
	    heep(index, nprob, nused, 0);
	    nprob[k] = nprob[index[0]] + nprob[node];
	    left[k] = node;
	    right[k++] = index[0];
	    up[index[0]] = -k;
	    index[0] = k - 1;
	    up[node] = k;
	    heep(index, nprob, nused, 0);
	}
	up[(nodemax = k) - 1] = 0;
	for (j = 0; j < nch; j++) {
	    if (nprob[j] != 0) {
		for (n = 0, ibit = 0, node = up[j]; node != 0; node = up[node - 1], ibit++) {
		    if (node < 0) {
			n |= setbit[ibit];
			node = -node;
		    }
		}
		icod[j] = n;
		ncod[j] = ibit;
	    }
	}
	nlong = 0;
	for (j = 0; j < nch; j++) {
	    if (ncod[j] > nlong) {
		nlong = ncod[j];
		ilong = j;
	    }
	}
	if (nlong > INT_DIGITS) // numeric_limits<Uint>::digits
	    throw new IllegalArgumentException(
		    "Code too long in Huffcode.  See text.");
    }

    /**
     * Huffman encode the single character ich (in the range 0..nch-1), write
     * the result to the byte array code starting at bit nb (whose smallest
     * valid value is zero), and increment nb to the first unused bit. This
     * routine is called repeatedly to encode consecutive characters in a
     * message. The user is responsible for monitoring that the value of nb does
     * not overrun the length of code.
     * 
     * @param ich
     * @param code
     * @param nb
     */
    public void codeone(final int ich, final byte[] code, final intW nb) {
	int m, n, nc;
	if (ich >= nch)
	    throw new IllegalArgumentException(
		    "bad ich (out of range) in Huffcode");
	if (ncod[ich] == 0)
	    throw new IllegalArgumentException(
		    "bad ich (zero prob) in Huffcode");
	for (n = ncod[ich] - 1; n >= 0; n--, ++nb.val) {
	    nc = nb.val >> 3;
	    m = nb.val & 7;
	    if (m == 0)
		code[nc] = 0;
	    if ((icod[ich] & setbit[n]) != 0)
		code[nc] |= setbit[m];
	}
    }

    /**
     * Starting at bit number nb in the byte array code, decode a single
     * character (returned as ich in the range 0..nch-1) and increment nb
     * appropriately. Repeated calls, starting with nb D 0, will return
     * successive characters in a compressed message. The user is responsible
     * for detecting EOM from the message content.
     * 
     * @param code
     * @param nb
     * @return
     */
    public int decodeone(final byte[] code, final intW nb) {
	int nc;
	int node = nodemax - 1;
	for (;;) {
	    nc = nb.val >> 3;
	    node = ((code[nc] & setbit[7 & nb.val++]) != 0 ? right[node]
		    : left[node]);
	    if (node < nch)
		return node;
	}
    }

    /**
     * Used by the constructor to maintain a heap structure in the array
     * index[0..m-1].
     * 
     * @param index
     * @param nprob
     * @param n
     * @param m
     */
    public void heep(final int[] index, final int[] nprob, final int n,
	    final int m) {
	int i = m, j, k;
	k = index[i];
	while (i < (n >> 1)) {
	    if ((j = 2 * i + 1) < n - 1
		    && nprob[index[j]] > nprob[index[j + 1]])
		j++;
	    if (nprob[k] <= nprob[index[j]])
		break;
	    index[i] = index[j];
	    i = j;
	}
	index[i] = k;
    }
}
