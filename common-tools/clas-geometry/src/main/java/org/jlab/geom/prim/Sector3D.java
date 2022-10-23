package org.jlab.geom.prim;

import java.util.List;

/**
 * A sector represented by an {@link org.jlab.geom.prim.Arc3D arc} and a radial
 * thickness. Sectors are regions of a plane bounded by two angles and two
 * radii relative to some reference point.
 *
 * @author jnhankins
 */
public final class Sector3D implements Face3D {
    private final Arc3D outerArc = new Arc3D();
    private double dR; // the thickness of the sector: r1 = r0 + dR
    
    /**
     * Constructs a new {@code Sector3D} such that the sector is bounded by two
     * arcs such that the origin points of the inner and outer arcs begin at
     * begin at (1, 0, 0) and (2, 0, 0) and end at (0, 1, 0) and (0, 2, 0)
     * respectively.
     * <p>
     * This sector is coplanar with the xy-plane. The inner and outer radius of
     * this sector will differ by one unit. This sector has a coverage of 90
     * degrees.
     */
    public Sector3D() {
        outerArc.setRadius(2);
        dR = 1;
    }
    
    /**
     * Constructs a new {@code Sector3D} from the given outer arc and the given
     * radial thickness.
     * @param outerArc the outer arc
     * @param dR the radial thickness
     */
    public Sector3D(Arc3D outerArc, double dR) {
        set(outerArc, dR);
    }
    
    /**
     * Constructs a new {@code Sector3D} that is identical to the given sector.
     * @param sector the sector to copy
     */
    public Sector3D(Sector3D sector) {
        copy(sector);
    }
    
    /**
     * Copies the parameters of the given sector so that this sector is
     * identical to the given sector.
     * @param sector the sector to copy
     */
    public void copy(Sector3D sector) {
        set(sector.outerArc, sector.dR);
    }
    
    /**
     * Sets the outer arc of this sector and the radial thickness.
     * @param outerArc the outer arc
     * @param dR the radial thickness
     */
    public void set(Arc3D outerArc, double dR) {
        setInnerArc(outerArc);
        setRadialThickness(dR);
    }
    
    /**
     * Sets the outer arc of this sector.
     * @param outerArc the outer arc
     */
    public void setInnerArc(Arc3D outerArc) {
        this.outerArc.copy(outerArc);
    }
    
    /**
     * Sets the radial thickness of this sector, that is the positive 
     * difference between the inner and outer radii.
     * @param dR the radial thickness
     */
    public void setRadialThickness(double dR) {
        if (outerArc.radius() < dR) {
            System.err.println("Sector3D: setRadialThickness(double dR): outerArc.radius() < dR ");
            return;
        }
        this.dR = dR;
    }
    
    /**
     * Returns the outer arc of this sector.
     * @return the outer arc
     */
    public Arc3D outerArc() {
        return outerArc;
    }
    
    /**
     * Constructs the inner arc of this sector.
     * @return the inner arc.
     */
    public Arc3D innerArc() {
        Arc3D innerArc = new Arc3D(outerArc);
        innerArc.setRadius(outerArc.radius()-dR);
        return innerArc;
    }
    
    /**
     * Returns the radial thickness of this sector, that is the positive 
     * difference between the inner and outer radii.
     * @return the radial thickness
     */
    public double radialThickness() {
        return dR;
    }
    
    /**
     * Returns the inner radius of this sector.
     * @return the inner radius
     */
    public double innerRadius() {
        return outerRadius() - dR;
    }
    
    /**
     * Returns the outer radius of this sector.
     * @return the outer radius
     */
    public double outerRadius() {
        return outerArc.radius();
    }
    
    /**
     * Test whether the given point is inside the area of this {@code Sector3D}.
     * @param p the given point
     * @return true if inside or false if outside
     */
    public boolean isInside(Point3D p) {

        Arc3D  arc = this.innerArc();
        double   r = p.distance(arc.center());
        if(r > this.innerRadius() && r < this.outerRadius()) {
            Vector3D vecP = arc.center().vectorTo(p);
            Vector3D vecO = arc.center().vectorTo(arc.origin());
            double  angle = vecP.angle(vecO);
            if(angle<0) angle += 2*Math.PI;
            if(angle<arc.theta())
                return true;
        }
        return false;
    }

    /**
     * Computes the minimum distance of the given point from the edges of 
     * this {@code Sector3D}.
     * @param p the given point
     * @return the signed distance to the {@code Sector3D} edges, positive if inside or negative if outside
     */    
    public double distanceFromEdge(Point3D p) {
        double r = p.distance(this.innerArc().center());
        double distance = Math.min(r-this.innerRadius(), this.outerRadius()-r);
        if(this.outerArc.theta()<2*Math.PI) {
            for(int i=0; i<2; i++) {
                double disti = new Line3D(this.point(i), this.point(i+2)).distanceSegment(p).length();
                if(disti < distance) distance = disti;
            }
        }
        if(!this.isInside(p)) 
            distance *= -1;
        return distance;
    }      

    @Override
    public Point3D point(int index) {
        switch (index) {
            case 0:
                return innerArc().origin();
            case 1:
                return innerArc().end();
            case 2:
                return outerArc().origin();
            case 3:
                return outerArc().end();
        }
        System.err.println("Warning: Sector3D: point(int index): invalid index=" + index);
        return null;
    }

    public Vector3D normal() {
        return outerArc.normal();
    }

    public Plane3D plane() {
        return new Plane3D(outerArc.center(), outerArc.normal());
    }
    
    @Override
    public int intersection(Line3D line, List<Point3D> intersections) {
        Point3D  center = outerArc.center();
        Vector3D normal = outerArc.normal();
        
        // Check for plane intersection
        Plane3D plane = new Plane3D(center, normal);
        Point3D intersect = new Point3D();
        if (plane.intersection(line, intersect) != 1)
            return 0;
        
        // Create a vector from the center to the intersection point
        Vector3D v = intersect.vectorFrom(center);
        
        // Check the radius
        double r = v.mag();
        if (r < innerRadius() || outerRadius() < r)
            return 0;
        
        // Check the theta
        double theta = outerArc.theta();
        double t = normal.angle(outerArc.originVector(), v);
        if (t < 0 || theta < t) {
            return 0;
        } else {
            intersections.add(intersect);
            return 1;
        }
    }
    
    @Override
    public int intersectionRay(Line3D line, List<Point3D> intersections) {
        Point3D  center = outerArc.center();
        Vector3D normal = outerArc.normal();
        
        // Check for plane intersection
        Plane3D plane = new Plane3D(center, normal);
        Point3D intersect = new Point3D();
        if (plane.intersectionRay(line, intersect) != 1)
            return 0;
        
        // Create a vector from the center to the intersection point
        Vector3D v = intersect.vectorFrom(center);
        
        // Check the radius
        double r = v.mag();
        if (r < innerRadius() || outerRadius() < r)
            return 0;
        
        // Check the theta
        double theta = outerArc.theta();
        double t = normal.angle(outerArc.originVector(), v);
        if (t < 0 || theta < t) {
            return 0;
        } else {
            intersections.add(intersect);
            return 1;
        }
    }
    
    @Override
    public int intersectionSegment(Line3D line, List<Point3D> intersections) {
        Point3D  center = outerArc.center();
        Vector3D normal = outerArc.normal();
        
        // Check for plane intersection
        Plane3D plane = new Plane3D(center, normal);
        Point3D intersect = new Point3D();
        if (plane.intersectionSegment(line, intersect) != 1)
            return 0;
        
        // Create a vector from the center to the intersection point
        Vector3D v = intersect.vectorFrom(center);
        
        // Check the radius
        double r = v.mag();
        if (r < innerRadius() || outerRadius() < r)
            return 0;
        
        // Check the theta
        double theta = outerArc.theta();
        double t = normal.angle(outerArc.originVector(), v);
        if (t < 0 && theta < t) {
            return 0;
        } else {
            intersections.add(intersect);
            return 1;
        }
    }
    
    @Override
    public void translateXYZ(double dx, double dy, double dz) {
        outerArc.translateXYZ(dx, dy, dz);
    }

    @Override
    public void rotateX(double angle) {
        outerArc.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        outerArc.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        outerArc.rotateZ(angle);
    }
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    @Override
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append("Sector3D:\n\t")
                .append(outerArc)
                .append("\n\tdR:\t")
                .append(dR)
                .toString();
    }
}
