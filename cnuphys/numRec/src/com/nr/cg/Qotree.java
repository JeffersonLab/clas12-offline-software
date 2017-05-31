package com.nr.cg;

import com.nr.ran.Hash;
import com.nr.ran.Hashfn1;
import com.nr.ran.Mhash;

public class Qotree {
    final int DIM;
    final int PMAX; // = 32/DIM;
    final int QO; // = (1 << DIM);
    final int QL; // = (QO - 2);
    public int maxd;
    public double[] blo;
    public double[] bscale;
    Mhash1 elhash;
    Hash1 pophash;

    class Mhash1 extends Mhash<Integer, Sphcirc> {
	Hashfn1 hfn = new Hashfn1(4);

	public Mhash1(final int nh, final int nm) {
	    super(nh, nm);
	}

	@Override
	public long fn(final Integer k) {
	    return hfn.fn(k);
	}
    }

    class Hash1 extends Hash<Integer, Integer> {
	Hashfn1 hfn = new Hashfn1(4);

	public Hash1(final int nh, final int nm) {
	    super(nh, nm);
	}

	@Override
	public long fn(final Integer k) {
	    return hfn.fn(k);
	}
    }

    /**
     * Constructor for a quad- (DIM=2) or oc- (DIM=3) tree that can store a max
     * of nv elements of type elT, using hash tables of length nh (typically
     * nv). maxdep is the number of levels to be represented.
     * 
     * @param dim
     * @param nh
     * @param nv
     * @param maxdep
     */
    public Qotree(final int dim, final int nh, final int nv, final int maxdep) {
	DIM = dim;
	PMAX = 32 / DIM;
	QO = (1 << DIM);
	QL = (QO - 2);

	blo = new double[DIM];
	bscale = new double[DIM];
	elhash = new Mhash1(nh, nv);
	maxd = maxdep;
	pophash = new Hash1(maxd * nh, maxd * nv);

	if (maxd > PMAX)
	    throw new IllegalArgumentException("maxdep too large in Qotree");
	setouterbox(new Point(DIM, new double[] { 0.0, 0.0, 0.0 }), new Point(
		DIM, new double[] { 1.0, 1.0, 1.0 }));
    }

    public int dim() {
	return DIM;
    }

    public void setouterbox(final Point lo, final Point hi) {
	if (lo.dim() != DIM || hi.dim() != DIM)
	    throw new IllegalArgumentException("Need same dim!");
	for (int j = 0; j < DIM; j++) {
	    blo[j] = lo.x[j];
	    bscale[j] = hi.x[j] - lo.x[j];
	}
    }

    /**
     * Returns the box indexed by k.
     * 
     * @param kk
     * @return
     */
    public Box qobox(final int kk) {
	int j, kb;
	int k = kk;
	Point plo = new Point(DIM), phi = new Point(DIM);
	double[] offset = new double[DIM];
	double del = 1.0;
	for (j = 0; j < DIM; j++)
	    offset[j] = 0.0;
	while (k > 1) {
	    kb = (k + QL) % QO;
	    for (j = 0; j < DIM; j++) {
		if ((kb & (1 << j)) != 0)
		    offset[j] += del;
	    }
	    k = (k + QL) >> DIM;
	    del *= 2.0;
	}
	for (j = 0; j < DIM; j++) {
	    plo.x[j] = blo[j] + bscale[j] * offset[j] / del;
	    phi.x[j] = blo[j] + bscale[j] * (offset[j] + 1.0) / del;
	}
	return new Box(plo, phi);
    }

    /**
     * Return the box number of the smallest box that can contain an element
     * tobj, without regard to whether tobj is already stored in the tree.
     * 
     * @param tobj
     * @return
     */
    public int qowhichbox(final Sphcirc tobj) {
	int p, k, kl, kr, ks = 1;
	for (p = 2; p <= maxd; p++) {
	    kl = QO * ks - QL;
	    kr = kl + QO - 1;
	    for (k = kl; k <= kr; k++) {
		if (tobj.isinbox(qobox(k)) != 0) {
		    ks = k;
		    break;
		}
	    }
	    if (k > kr)
		break;
	}
	return ks;
    }

    /**
     * Store the element tobj in the Qotree, and return the box number into
     * which it was stored.
     * 
     * @param tobj
     * @return
     */
    public int qostore(final Sphcirc obj) {
	// XXX clone object for storing.
	Sphcirc tobj = obj.clone();
	int k, ks, kks, km;
	ks = kks = qowhichbox(tobj);
	elhash.store(ks, tobj);
	Integer ppop = pophash.get(ks);
	if (ppop == null)
	    ppop = 0;
	pophash.set(ks, ppop | 1);
	while (ks > 1) {
	    km = (ks + QL) >> DIM;
	    k = ks - (QO * km - QL);
	    ks = km;
	    ppop = pophash.get(ks);
	    if (ppop == null)
		ppop = 0;
	    pophash.set(ks, ppop | (1 << (k + 1)));
	}
	return kks;
    }

    /**
     * Erase the element tobj, returning the box number into which it was stored
     * or 0 if the element was not found in the Qotree. Note logic very similar
     * to qostore.
     * 
     * @param tobj
     * @return
     */
    public int qoerase(final Sphcirc tobj) {
	int k, ks, kks, km;
	Integer ppop;
	ks = kks = qowhichbox(tobj);
	if (elhash.erase(ks, tobj) == 0)
	    return 0;
	if (elhash.count(ks) != 0)
	    return kks;
	ppop = pophash.get(ks);
	if (ppop == null)
	    ppop = 0;
	pophash.set(ks, ppop & ~(1));
	while (ks > 1) {
	    if (pophash.get(ks) != 0)
		break;
	    pophash.erase(ks);
	    km = (ks + QL) >> DIM;
	    k = ks - (QO * km - QL);
	    ks = km;
	    ppop = pophash.get(ks);
	    if (ppop == null)
		ppop = 0;
	    pophash.set(ks, ppop & (~(1 << (k + 1))));
	    // *ppop &= ~((Uint)(1 << (k+1)));
	}
	return kks;
    }

    /**
     * Retrieve all (or up to nmax if it is smaller) elements that are stored in
     * box k of the Qotree. The elements are copied into list[0..nlist-1] and
     * the value nlist (<= nmax) is returned.
     * 
     * @param k
     * @param list
     * @param nmax
     * @return
     */
    public int qoget(final int k, final Sphcirc[] list, final int nmax) {
	int ks, pop, nlist;
	ks = k;
	nlist = 0;
	Integer[] pop_w = new Integer[1];
	pophash.get(ks, pop_w, 0);
	pop = pop_w[0];
	if ((pop & 1) != 0 && elhash.getinit(ks) != 0) {
	    while (nlist < nmax && elhash.getnext(list, nlist) != 0) {
		nlist++;
	    }
	}
	return nlist;
    }

    /**
     * 
     * @param klist
     * @param list
     * @param nmax
     * @return
     */
    public int qodump(final int[] klist, final Sphcirc[] list, final int nmax) {
	int nlist, ntask, ks, pop, k;
	int[] tasklist = new int[200];
	nlist = 0;
	ntask = 1;
	tasklist[1] = 1;
	while (ntask != 0) {
	    ks = tasklist[ntask--];
	    Integer[] pop_w = new Integer[1];
	    if (pophash.get(ks, pop_w, 0) == 0)
		continue;
	    pop = pop_w[0];
	    if ((pop & 1) != 0 && elhash.getinit(ks) != 0) {
		while (nlist < nmax && elhash.getnext(list, nlist) != 0) {
		    klist[nlist] = ks;
		    nlist++;
		}
	    }
	    if (nlist == nmax)
		break;
	    k = QO * ks - QL;
	    while ((pop >>= 1) != 0) {
		if ((pop & 1) != 0)
		    tasklist[++ntask] = k;
		k++;
	    }
	}
	return nlist;
    }

    /**
     * Retrieve all (or up to nmax if it is smaller) elements in Qotree that
     * contain the point pt. The elements are copied into list[0..nlist-1] and
     * the value nlist (<= nmax) is returned.
     * 
     * @param pt
     * @param list
     * @param nmax
     * @return
     */
    public int qocontainspt(final Point pt, final Sphcirc[] list, final int nmax) {
	int j, k, ks, pop, nlist;
	double[] bblo = new double[DIM], bbscale = new double[DIM];
	for (j = 0; j < DIM; j++) {
	    bblo[j] = blo[j];
	    bbscale[j] = bscale[j];
	}
	nlist = 0;
	ks = 1;
	Integer[] pop_w = new Integer[1];
	while (pophash.get(ks, pop_w, 0) != 0) {
	    pop = pop_w[0];
	    if ((pop & 1) != 0) {
		elhash.getinit(ks);

		while (nlist < nmax && elhash.getnext(list, nlist) != 0) {

		    if (list[nlist].contains(pt) != 0) {
			nlist++;
		    }
		}
	    }
	    if ((pop >>= 1) == 0)
		break;
	    for (k = 0, j = 0; j < DIM; j++) {
		bbscale[j] *= 0.5;
		if (pt.x[j] > bblo[j] + bbscale[j]) {
		    k += (1 << j);
		    bblo[j] += bbscale[j];
		}
	    }
	    if (((pop >> k) & 1) == 0)
		break;
	    ks = QO * ks - QL + k;
	}
	return nlist;
    }

    /**
     * Retrieve all (or up to nmax if it is smaller) elements in Qotree that
     * collide with an element qt (which needn't be in the tree itself). The
     * elements are copied into list[0..nlist-1] and the value nlist (Ä nmax) is
     * returned.
     * 
     * @param qt
     * @param list
     * @param nmax
     * @return
     */
    public int qocollides(final Sphcirc qt, final Sphcirc[] list, final int nmax) {
	int k, ks, kks, pop, nlist, ntask;
	int[] tasklist = new int[200];
	nlist = 0;
	kks = ks = qowhichbox(qt);
	ntask = 0;
	while (ks > 0) {
	    tasklist[++ntask] = ks;
	    ks = (ks + QL) >> DIM;
	}
	while (ntask != 0) {
	    ks = tasklist[ntask--];
	    Integer[] pop_w = new Integer[1];
	    if (pophash.get(ks, pop_w, 0) == 0)
		continue;
	    pop = pop_w[0];
	    if ((pop & 1) != 0) {
		elhash.getinit(ks);
		while (nlist < nmax && elhash.getnext(list, nlist) != 0) {
		    if (list[nlist].collides(qt) != 0) {
			nlist++;
		    }
		}
	    }
	    if (ks >= kks) {
		k = QO * ks - QL;
		while ((pop >>= 1) != 0) {
		    if ((pop & 1) != 0)
			tasklist[++ntask] = k;
		    k++;
		}
	    }
	}
	return nlist;
    }
}
