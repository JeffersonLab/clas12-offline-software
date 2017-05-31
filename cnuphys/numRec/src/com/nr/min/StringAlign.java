package com.nr.min;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class StringAlign {
    private StringAlign() {
    }

    /**
     * Given null terminated input strings ain and bin, and given penalties
     * mispen, gappen, and skwpen, respectively, for mismatches, interior gaps,
     * and gaps before/after either string, set null terminated output strings
     * aout, bout, and summary as the aligned versions of the input strings, and
     * a summary string. User must supply storage for the output strings of size
     * equal to the sum of the two input strings.
     * 
     * @param ain
     * @param bin
     * @param mispen
     * @param gappen
     * @param skwpen
     * @param aout
     * @param bout
     * @param summary
     */
    public static void stringalign(final char[] ain, final char[] bin,
	    final double mispen, final double gappen, final double skwpen,
	    final char[] aout, final char[] bout, final char[] summary) {
	int i, j, k;
	double dn, rt, dg;
	int ia = ain.length, ib = bin.length;
	double[][] cost = new double[ia + 1][ib + 1];
	cost[0][0] = 0.;
	for (i = 1; i <= ia; i++)
	    cost[i][0] = cost[i - 1][0] + skwpen;
	for (i = 1; i <= ib; i++)
	    cost[0][i] = cost[0][i - 1] + skwpen;
	for (i = 1; i <= ia; i++)
	    for (j = 1; j <= ib; j++) {
		dn = cost[i - 1][j] + ((j == ib) ? skwpen : gappen);
		rt = cost[i][j - 1] + ((i == ia) ? skwpen : gappen);
		dg = cost[i - 1][j - 1]
			+ ((ain[i - 1] == bin[j - 1]) ? -1. : mispen);
		cost[i][j] = min(min(dn, rt), dg);
	    }
	i = ia;
	j = ib;
	k = 0;
	while (i > 0 || j > 0) {
	    dn = rt = dg = 9.99e99;
	    if (i > 0)
		dn = cost[i - 1][j] + ((j == ib) ? skwpen : gappen);
	    if (j > 0)
		rt = cost[i][j - 1] + ((i == ia) ? skwpen : gappen);
	    if (i > 0 && j > 0)
		dg = cost[i - 1][j - 1]
			+ ((ain[i - 1] == bin[j - 1]) ? -1. : mispen);
	    if (dg <= min(dn, rt)) {
		aout[k] = ain[i - 1];
		bout[k] = bin[j - 1];
		summary[k++] = ((ain[i - 1] == bin[j - 1]) ? '=' : '!');
		i--;
		j--;
	    } else if (dn < rt) {
		aout[k] = ain[i - 1];
		bout[k] = ' ';
		summary[k++] = ' ';
		i--;
	    } else {
		aout[k] = ' ';
		bout[k] = bin[j - 1];
		summary[k++] = ' ';
		j--;
	    }
	}
	for (i = 0; i < k / 2; i++) {
	    swap(aout, i, k - 1 - i);
	    swap(bout, i, k - 1 - i);
	    swap(summary, i, k - 1 - i);
	}
	aout[k] = bout[k] = summary[k] = 0;
    }
}
