package com.nr.lna;

import static java.lang.Math.max;

import org.netlib.util.intW;

/**
 * compression by arithmetic coding
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Arithcode {
    int nch, nrad, ncum;
    long jdif;
    int nc, minint;
    int[] ilob, iupb;
    int[] ncumfq;
    static final int NWK = 20;

    public Arithcode(final int[] nfreq, final int nnch, final int nnrad) {
	nch = nnch;
	nrad = nnrad;
	ilob = new int[NWK];
	iupb = new int[NWK];
	ncumfq = new int[nch + 2];

	int j;
	if (nrad > 256)
	    throw new IllegalArgumentException(
		    "output radix must be <= 256 in Arithcode");
	minint = (int) (0xFFFFFFFFL / nrad);
	ncumfq[0] = 0;
	for (j = 1; j <= nch; j++)
	    ncumfq[j] = ncumfq[j - 1] + max(nfreq[j - 1], 1);
	ncum = ncumfq[nch + 1] = ncumfq[nch] + 1;
    }

    public void messageinit() {
	int j;
	jdif = nrad - 1;
	for (j = NWK - 1; j >= 0; j--) {
	    iupb[j] = nrad - 1;
	    ilob[j] = 0;
	    nc = j;
	    if (jdif > minint)
		return;
	    jdif = (jdif + 1) * nrad - 1;
	}
	throw new IllegalArgumentException("NWK too small in arcode.");
    }

    public void codeone(final int ich, final byte[] code, final intW lcd) {
	if (ich > nch)
	    throw new IllegalArgumentException("bad ich in Arithcode");
	advance(ich, code, lcd, 1);
    }

    public int decodeone(final byte[] code, final intW lcd) {
	int ich;
	int j, ihi, ja, m;
	ja = code[lcd.val] - ilob[nc];
	for (j = nc + 1; j < NWK; j++) {
	    ja *= nrad;
	    ja += (code[lcd.val + j - nc]) - ilob[j];
	}
	ihi = nch + 1;
	ich = 0;
	while (ihi - ich > 1) {
	    m = (ich + ihi) >>> 1;
	    if (ja >= multdiv(jdif, ncumfq[m], ncum))
		ich = m;
	    else
		ihi = m;
	}
	if (ich != nch)
	    advance(ich, code, lcd, -1);
	return ich;
    }

    public void advance(final int ich, final byte[] code, final intW lcd,
	    final int isign) {
	int j, k, jh, jl;
	jh = multdiv(jdif, ncumfq[ich + 1], ncum);
	jl = multdiv(jdif, ncumfq[ich], ncum);
	jdif = jh - jl;
	arrsum(ilob, iupb, jh, NWK, nrad, nc);
	arrsum(ilob, ilob, jl, NWK, nrad, nc);
	for (j = nc; j < NWK; j++) {
	    if (ich != nch && iupb[j] != ilob[j])
		break;
	    if (isign > 0)
		code[lcd.val] = (byte) (ilob[j]);
	    lcd.val++;
	}
	if (j + 1 > NWK)
	    return;
	nc = j;
	for (j = 0; jdif < minint; j++)
	    jdif *= nrad;
	if (j > nc)
	    throw new IllegalArgumentException("NWK too small in arcode.");
	if (j != 0) {
	    for (k = nc; k < NWK; k++) {
		iupb[k - j] = iupb[k];
		ilob[k - j] = ilob[k];
	    }
	}
	nc -= j;
	for (k = NWK - j; k < NWK; k++)
	    iupb[k] = ilob[k] = 0;
	return;
    }

    public int multdiv(final long j, final int k, final int m) {
	return (int) (j * k / m);
    }

    public void arrsum(final int[] iin, final int[] iout, final int ja0,
	    final int nwk, final int nrad, final int nc) {
	int karry = 0, j, jtmp;
	int ja = ja0;
	for (j = nwk - 1; j > nc; j--) {
	    jtmp = ja;
	    ja /= nrad;
	    iout[j] = iin[j] + (jtmp - ja * nrad) + karry;
	    if (iout[j] >= nrad) {
		iout[j] -= nrad;
		karry = 1;
	    } else
		karry = 0;
	}
	iout[nc] = iin[nc] + ja + karry;
    }
}
