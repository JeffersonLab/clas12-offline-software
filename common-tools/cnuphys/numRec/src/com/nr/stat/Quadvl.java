package com.nr.stat;

import org.netlib.util.doubleW;
import static java.lang.Math.*;

public class Quadvl implements QuadvlInf {
    @Override
    public void quadvl(final double x, final double y, doubleW fa, doubleW fb,
	    doubleW fc, doubleW fd) {
	double qa, qb, qc, qd;
	qa = min(2.0, max(0.0, 1.0 - x));
	qb = min(2.0, max(0.0, 1.0 - y));
	qc = min(2.0, max(0.0, x + 1.0));
	qd = min(2.0, max(0.0, y + 1.0));
	fa.val = 0.25 * qa * qb;
	fb.val = 0.25 * qb * qc;
	fc.val = 0.25 * qc * qd;
	fd.val = 0.25 * qd * qa;
    }

}
