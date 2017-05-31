package com.nr.util;

import static java.lang.Math.*;

import org.netlib.util.doubleW;
import org.netlib.util.intW;

public class Calendar {
    private Calendar() {
    }

    public static int julday(final int mm, final int id, final int iyyy) {
	final int IGREG = 15 + 31 * (10 + 12 * 1582);
	int ja, jul, jy = iyyy, jm;
	if (jy == 0)
	    throw new IllegalArgumentException("julday: there is no year zero.");
	if (jy < 0)
	    ++jy;
	if (mm > 2) {
	    jm = mm + 1;
	} else {
	    --jy;
	    jm = mm + 13;
	}
	jul = (int) (floor(365.25 * jy) + floor(30.6001 * jm) + id + 1720995);
	if (id + 31 * (mm + 12 * iyyy) >= IGREG) {
	    ja = (int) (0.01 * jy);
	    jul += 2 - ja + (int) (0.25 * ja);
	}
	return jul;
    }

    public static void caldat(final int julian, final intW mm, final intW id,
	    final intW iyyy) {
	final int IGREG = 2299161;
	int ja, jalpha, jb, jc, jd, je;

	if (julian >= IGREG) {
	    jalpha = (int) ((julian - 1867216 - 0.25) / 36524.25);
	    ja = julian + 1 + jalpha - (int) (0.25 * jalpha);
	} else if (julian < 0) {
	    ja = julian + 36525 * (1 - julian / 36525);
	} else
	    ja = julian;
	jb = ja + 1524;
	jc = (int) (6680.0 + (jb - 2439870 - 122.1) / 365.25);
	jd = (int) (365 * jc + (0.25 * jc));
	je = (int) ((jb - jd) / 30.6001);
	id.val = jb - jd - (int) (30.6001 * je);
	mm.val = je - 1;
	if (mm.val > 12)
	    mm.val -= 12;
	iyyy.val = jc - 4715;
	if (mm.val > 2)
	    --iyyy.val;
	if (iyyy.val <= 0)
	    --iyyy.val;
	if (julian < 0)
	    iyyy.val -= 100 * (1 - julian / 36525);
    }

    public static void flmoon(final int n, final int nph, final intW jd,
	    final doubleW frac) {
	final double RAD = 3.141592653589793238 / 180.0;
	int i;
	double am, as, c, t, t2, xtra;
	c = n + nph / 4.0;
	t = c / 1236.85;
	t2 = t * t;
	as = 359.2242 + 29.105356 * c;
	am = 306.0253 + 385.816918 * c + 0.010730 * t2;
	jd.val = 2415020 + 28 * n + 7 * nph;
	xtra = 0.75933 + 1.53058868 * c + ((1.178e-4) - (1.55e-7) * t) * t2;
	if (nph == 0 || nph == 2)
	    xtra += (0.1734 - 3.93e-4 * t) * sin(RAD * as) - 0.4068
		    * sin(RAD * am);
	else if (nph == 1 || nph == 3)
	    xtra += (0.1721 - 4.0e-4 * t) * sin(RAD * as) - 0.6280
		    * sin(RAD * am);
	else
	    throw new IllegalArgumentException("nph is unknown in flmoon");
	i = (int) (xtra >= 0.0 ? floor(xtra) : ceil(xtra - 1.0));
	jd.val += i;
	frac.val = xtra - i;
    }

}
