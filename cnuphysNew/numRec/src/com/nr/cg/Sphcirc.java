package com.nr.cg;

import static com.nr.cg.Point.*;

public class Sphcirc {
    public Point center;
    public double radius;

    public Sphcirc(final int dim) {
	center = new Point(dim);
    }

    public Sphcirc(final Point mycenter, final double myradius) {
	center = mycenter;
	radius = myradius;
    }

    public int dim() {
	return center.dim();
    }

    @Override
    public Sphcirc clone() {
	return new Sphcirc(center.clone(), radius);
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj instanceof Sphcirc) {
	    Sphcirc s = (Sphcirc) obj;
	    return (radius == s.radius && center.equals(s.center));
	} else
	    return false;
    }

    /**
     * 
     * Is the circle/sphere inside a box?
     * 
     * @param box
     * @return
     */
    public int isinbox(final Box box) {
	if (center.dim() != box.dim())
	    throw new IllegalArgumentException("Need same dim!");
	int DIM = center.dim();
	for (int i = 0; i < DIM; i++) {
	    if ((center.x[i] - radius < box.lo.x[i])
		    || (center.x[i] + radius > box.hi.x[i]))
		return 0;
	}
	return 1;
    }

    /**
     * Is a given point inside the circle/sphere?
     * 
     * @param point
     * @return
     */
    public int contains(final Point point) {
	if (center.dim() != point.dim())
	    throw new IllegalArgumentException("Need same dim!");
	if (dist(point, center) > radius)
	    return 0;
	else
	    return 1;
    }

    /**
     * Does it collide with another circle/sphere?
     * 
     * @param circ
     * @return
     */
    public int collides(final Sphcirc circ) {
	if (circ.dim() != this.dim())
	    throw new IllegalArgumentException("Need same dim!");

	if (dist(circ.center, center) > circ.radius + radius)
	    return 0;
	else
	    return 1;
    }
}
