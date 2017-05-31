package com.nr.sf;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import org.netlib.util.intW;

public class Bessjy {
    private static final double xj00 = 5.783185962946785;
    private static final double xj10 = 3.047126234366209e1;
    private static final double xj01 = 1.468197064212389e1;
    private static final double xj11 = 4.921845632169460e1;
    private static final double twoopi = 0.6366197723675813;
    private static final double pio4 = 0.7853981633974483;
    private static final double j0r[] = { 1.682397144220462e-4,
	    2.058861258868952e-5, 5.288947320067750e-7, 5.557173907680151e-9,
	    2.865540042042604e-11, 7.398972674152181e-14, 7.925088479679688e-17 };
    private static final double j0s[] = { 1.0, 1.019685405805929e-2,
	    5.130296867064666e-5, 1.659702063950243e-7, 3.728997574317067e-10,
	    5.709292619977798e-13, 4.932979170744996e-16 };
    private static final double j0pn[] = { 9.999999999999999e-1,
	    1.039698629715637, 2.576910172633398e-1, 1.504152485749669e-2,
	    1.052598413585270e-4 };
    private static final double j0pd[] = { 1.0, 1.040797262528109,
	    2.588070904043728e-1, 1.529954477721284e-2, 1.168931211650012e-4 };
    private static final double j0qn[] = { -1.562499999999992e-2,
	    -1.920039317065641e-2, -5.827951791963418e-3,
	    -4.372674978482726e-4, -3.895839560412374e-6 };
    private static final double j0qd[] = { 1.0, 1.237980436358390,
	    3.838793938147116e-1, 3.100323481550864e-2, 4.165515825072393e-4 };
    private static final double j1r[] = { 7.309637831891357e-5,
	    3.551248884746503e-6, 5.820673901730427e-8, 4.500650342170622e-10,
	    1.831596352149641e-12, 3.891583573305035e-15, 3.524978592527982e-18 };
    private static final double j1s[] = { 1.0, 9.398354768446072e-3,
	    4.328946737100230e-5, 1.271526296341915e-7, 2.566305357932989e-10,
	    3.477378203574266e-13, 2.593535427519985e-16 };
    private static final double j1pn[] = { 1.0, 1.014039111045313,
	    2.426762348629863e-1, 1.350308200342000e-2, 9.516522033988099e-5 };
    private static final double j1pd[] = { 1.0, 1.012208056357845,
	    2.408580305488938e-1, 1.309511056184273e-2, 7.746422941504713e-5 };
    private static final double j1qn[] = { 4.687499999999991e-2,
	    5.652407388406023e-2, 1.676531273460512e-2, 1.231216817715814e-3,
	    1.178364381441801e-5 };
    private static final double j1qd[] = { 1.0, 1.210119370463693,
	    3.626494789275638e-1, 2.761695824829316e-2, 3.240517192670181e-4 };
    private static final double y0r[] = { -7.653778457189104e-3,
	    -5.854760129990403e-2, 3.720671300654721e-4, 3.313722284628089e-5,
	    4.247761237036536e-8, -4.134562661019613e-9,
	    -3.382190331837473e-11, -1.017764126587862e-13,
	    -1.107646382675456e-16 };
    private static final double y0s[] = { 1.0, 1.125494540257841e-2,
	    6.427210537081400e-5, 2.462520624294959e-7, 7.029372432344291e-10,
	    1.560784108184928e-12, 2.702374957564761e-15,
	    3.468496737915257e-18, 2.716600180811817e-21 };
    private static final double y0pn[] = { 9.999999999999999e-1,
	    1.039698629715637, 2.576910172633398e-1, 1.504152485749669e-2,
	    1.052598413585270e-4 };
    private static final double y0pd[] = { 1.0, 1.040797262528109,
	    2.588070904043728e-1, 1.529954477721284e-2, 1.168931211650012e-4 };
    private static final double y0qn[] = { -1.562499999999992e-2,
	    -1.920039317065641e-2, -5.827951791963418e-3,
	    -4.372674978482726e-4, -3.895839560412374e-6 };
    private static final double y0qd[] = { 1.0, 1.237980436358390,
	    3.838793938147116e-1, 3.100323481550864e-2, 4.165515825072393e-4 };
    private static final double y1r[] = { -1.041835425863234e-1,
	    -1.135093963908952e-5, 2.212118520638132e-4, 1.270981874287763e-6,
	    -3.982892100836748e-8, -4.820712110115943e-10,
	    -1.929392690596969e-12, -2.725259514545605e-15 };
    private static final double y1s[] = { 1.0, 1.186694184425838e-2,
	    7.121205411175519e-5, 2.847142454085055e-7, 8.364240962784899e-10,
	    1.858128283833724e-12, 3.018846060781846e-15, 3.015798735815980e-18 };
    private static final double y1pn[] = { 1.0, 1.014039111045313,
	    2.426762348629863e-1, 1.350308200342000e-2, 9.516522033988099e-5 };
    private static final double y1pd[] = { 1.0, 1.012208056357845,
	    2.408580305488938e-1, 1.309511056184273e-2, 7.746422941504713e-5 };
    private static final double y1qn[] = { 4.687499999999991e-2,
	    5.652407388406023e-2, 1.676531273460512e-2, 1.231216817715814e-3,
	    1.178364381441801e-5 };
    private static final double y1qd[] = { 1.0, 1.210119370463693,
	    3.626494789275638e-1, 2.761695824829316e-2, 3.240517192670181e-4 };
    private double nump, denp, numq, denq, y, z, ax, xx;

    public Bessjy() {
    }

    public double j0(final double x) {
	if ((ax = abs(x)) < 8.0) {
	    rat(x, j0r, j0s, 6);
	    return nump * (y - xj00) * (y - xj10) / denp;
	} else {
	    asp(j0pn, j0pd, j0qn, j0qd, 1.);
	    return sqrt(twoopi / ax)
		    * (cos(xx) * nump / denp - z * sin(xx) * numq / denq);
	}
    }

    public double j1(final double x) {
	if ((ax = abs(x)) < 8.0) {
	    rat(x, j1r, j1s, 6);
	    return x * nump * (y - xj01) * (y - xj11) / denp;
	} else {
	    asp(j1pn, j1pd, j1qn, j1qd, 3.);
	    double ans = sqrt(twoopi / ax)
		    * (cos(xx) * nump / denp - z * sin(xx) * numq / denq);
	    return x > 0.0 ? ans : -ans;
	}
    }

    public double y0(final double x) {
	if (x < 8.0) {
	    double j0x = j0(x);
	    rat(x, y0r, y0s, 8);
	    return nump / denp + twoopi * j0x * log(x);
	} else {
	    ax = x;
	    asp(y0pn, y0pd, y0qn, y0qd, 1.);
	    return sqrt(twoopi / x)
		    * (sin(xx) * nump / denp + z * cos(xx) * numq / denq);
	}
    }

    public double y1(final double x) {
	if (x < 8.0) {
	    double j1x = j1(x);
	    rat(x, y1r, y1s, 7);
	    return x * nump / denp + twoopi * (j1x * log(x) - 1.0 / x);
	} else {
	    ax = x;
	    asp(y1pn, y1pd, y1qn, y1qd, 3.);
	    return sqrt(twoopi / x)
		    * (sin(xx) * nump / denp + z * cos(xx) * numq / denq);
	}
    }

    public void rat(final double x, final double[] r, final double[] s,
	    final int n) {
	y = x * x;
	z = 64.0 - y;
	nump = r[n];
	denp = s[n];
	for (int i = n - 1; i >= 0; i--) {
	    nump = nump * z + r[i];
	    denp = denp * y + s[i];
	}
    }

    public void asp(final double[] pn, final double[] pd, final double[] qn,
	    final double[] qd, final double fac) {
	z = 8.0 / ax;
	y = z * z;
	xx = ax - fac * pio4;
	nump = pn[4];
	denp = pd[4];
	numq = qn[4];
	denq = qd[4];
	for (int i = 3; i >= 0; i--) {
	    nump = nump * y + pn[i];
	    denp = denp * y + pd[i];
	    numq = numq * y + qn[i];
	    denq = denq * y + qd[i];
	}
    }

    public double jn(final int n, final double x) {
	final double ACC = 160.0;
	final int IEXP = Double.MAX_EXPONENT / 2; // numeric_limits<double>::max_exponent/2;
	boolean jsum;
	int j, m;
	intW k = new intW(0);
	double ax, bj, bjm, bjp, dum, sum, tox, ans;
	if (n == 0)
	    return j0(x);
	if (n == 1)
	    return j1(x);
	ax = abs(x);
	if (ax * ax <= 8.0 * Double.MIN_NORMAL)
	    return 0.0;
	else if (ax > n) {
	    tox = 2.0 / ax;
	    bjm = j0(ax);
	    bj = j1(ax);
	    for (j = 1; j < n; j++) {
		bjp = j * tox * bj - bjm;
		bjm = bj;
		bj = bjp;
	    }
	    ans = bj;
	} else {
	    tox = 2.0 / ax;
	    m = 2 * ((n + (int) (sqrt(ACC * n))) / 2);
	    jsum = false;
	    bjp = ans = sum = 0.0;
	    bj = 1.0;
	    for (j = m; j > 0; j--) {
		bjm = j * tox * bj - bjp;
		bjp = bj;
		bj = bjm;
		dum = frexp(bj, k);
		dum = dum + 0;
		if (k.val > IEXP) {

		    bj = ldexp(bj, -IEXP);
		    bjp = ldexp(bjp, -IEXP);
		    ans = ldexp(ans, -IEXP);
		    sum = ldexp(sum, -IEXP);
		}
		if (jsum)
		    sum += bj;
		jsum = !jsum;
		if (j == n)
		    ans = bjp;
	    }
	    sum = 2.0 * sum - bj;
	    ans /= sum;
	}
	return (x < 0.0 && (n & 1) != 0) ? -ans : ans;
    }

    public double yn(final int n, final double x) {
	int j;
	double by, bym, byp, tox;
	if (n == 0)
	    return y0(x);
	if (n == 1)
	    return y1(x);
	tox = 2.0 / x;
	by = y1(x);
	bym = y0(x);
	for (j = 1; j < n; j++) {
	    byp = j * tox * by - bym;
	    bym = by;
	    by = byp;
	}
	return by;
    }
}
