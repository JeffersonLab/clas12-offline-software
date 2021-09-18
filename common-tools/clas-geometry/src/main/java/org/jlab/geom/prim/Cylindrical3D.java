package org.jlab.geom.prim;

import java.util.List;


/**
 * A horizontal cylindrical segment represented by an 
 * {@link org.jlab.geom.prim.Arc3D arc} and a height.  Cylindrical segments
 * are portions of a cylinder (constant radius from some line) with finite
 * height that are bounded by an arc.
 * 
 * @author jnhankins
 */
public final class Cylindrical3D implements Face3D {
    private final Arc3D baseArc = new Arc3D();
    private double height;
    /**
     * Constructs a new {@code Cylindrical3D} such that the arc of the surface
     * is centered around the z axis, the radius is one, the arc begins at x=1
     * y=0 and ends at x=0, y=1, and the arc has unit height such that it
     * extends from z=0 to z=1.
     */
    public Cylindrical3D() {
        height = 1;
    }
    
    /**
     * Constructs a new {@code Cylindrical3D} from the given arc and height.
     * @param arc    the base of the cylindrical segment
     * @param height the height
     */
    public Cylindrical3D(Arc3D arc, double height) {
        set(arc, height);
    }
    
    /**
     * Constructs a new {@code Cylindrical3D} that is identical to the given
     * cylindrical segment.
     * @param cylindricalSeg the cylindrical segment to copy
     */
    public Cylindrical3D(Cylindrical3D cylindricalSeg) {
        copy(cylindricalSeg);
    }
    
    /**
     * Sets the core parameters of this cylindrical segment such that this
     * surface becomes identical to the given surface.
     * @param cylindricalSeg the cylindrical segment to copy
     */
    public void copy(Cylindrical3D cylindricalSeg) {
        set(cylindricalSeg.baseArc, cylindricalSeg.height);
    }
    
    /**
     * Sets the parameters of this cylindrical segment to equal the given
     * parameters.
     * @param arc the base of the cylindrical segment
     * @param height the height
     */
    public void set(Arc3D arc, double height) {
        setArc(arc);
        setHeight(height);
    }
    
    /**
     * Sets the base of this cylindrical segment.
     * @param arc the base of the cylindrical segment
     */
    public void setArc(Arc3D arc) {
        this.baseArc.copy(arc);
    }
    
    /**
     * Sets the axis of this cylinder.
     * @param axis the base of the cylindrical segment
     */
    public void setAxis(Line3D axis) {
        this.baseArc.setCenter(axis.origin());
        this.baseArc.setNormal(axis.originDir());
        this.height = axis.length();
    }
    
    /**
     * returns the axis of this cylinder.
     * @return the axis of the cylindrical segment
     */
    public Line3D getAxis() {
        Point3D origin = new Point3D(baseArc.center());
        Vector3D   dir = new Vector3D(baseArc.normal());
        dir.setMag(height);
        Line3D axis = new Line3D(origin,dir);
        return axis;
    }

    /**
     * Sets the height of this cylindrical segment.
     * @param height the height
     */
    public void setHeight(double height) {
        this.height = height;
    }
    
    /**
     * Returns the base of this cylindrical segment.
     * @return the base of this cylindrical segment
     */
    public Arc3D baseArc() {
        return baseArc;
    }
    
    /**
     * Constructs the arc at the top of this cylindrical segment.
     * @return the arc at the top of this cylindrical segment
     */
    public Arc3D highArc() {
        Arc3D highArc = new Arc3D(baseArc);
        Vector3D N = new Vector3D(baseArc.normal());
        N.setMag(height);
        highArc.translateXYZ(N.x(), N.y(), N.z());
        return highArc;
    }
    
    /**
     * Returns the height of this cylindrical segment.
     * @return the height of this cylindrical segment.
     */
    public double height() {
        return height;
    }
    
    @Override
    public int intersection(Line3D line, List<Point3D> intersections) {
        final double SMALL = 0.00001;
        
        Point3D  pA = baseArc.center();
        Vector3D vA = baseArc.normal();
        double R = baseArc.radius();
        
        Point3D  pB = line.origin();
        Vector3D vB = line.toVector();
        vB.unit();
        
        // pBA = pB - pA
        Vector3D pBA = pB.vectorFrom(pA);
        
        if (Math.abs(vA.dot(vB)) > 1-SMALL) { // parallel
            return 0; // disjoint
        }
        
        // M = pBA - (vA dot pBA)vA
        Vector3D M = new Vector3D(vA); // = vA
        M.scale(-vA.dot(pBA));         // = -(vA dot pBA)vA
        M.add(pBA);                    // = pBA - (vA dot pBA)vA
        
        // N = vB - (vA dot vB)vA
        Vector3D N = new Vector3D(vA); // = vA
        N.scale(-vA.dot(vB));          // = -(vA dot vB)vA
        N.add(vB);                     // = vB - (vA dot vB)vA
        
        // Beginning with a general equation for a point on a cylinder, and an
        // equation that gives a point on a line given the parametric value t,
        // it is possible to find intersections by combining and reducing the
        // equations yeilding a quadratic formula such that the real solutions
        // for t give the position of the points on the line that intersect the
        // cylindar.
        
        // Quadratic: a*t^2 + b*t + c = 0
        double a = N.dot(N);        // = N^2
        double b = 2*M.dot(N);      // = 2(M dot N)
        double c = M.dot(M) - R*R;  // = M^2 - R^2
        
        // Discriminant
        double d = b*b - 4*a*c;     // = b^2 - 4ac
        
        if (d > 0) { // two real roots
            d = Math.sqrt(d);
            double t0 = (-b + d)/(2*a); // first real root
            double t1 = (-b - d)/(2*a); // second real root
            
            int count = 0;
            
            // p0 = pB + vB*t0
            vB.setMag(t0);                            // = vB*t0
            Point3D intersect0 = new Point3D(pB, vB); // = pB + vB*t0
            if (isOnSurface(intersect0)) {
                intersections.add(intersect0);
                count++;
            }
            
            // p1 = pB + vB*t1
            vB.setMag(t1);                            // = vB*t1
            Point3D intersect1 = new Point3D(pB, vB); // = pB + vB*t1
            if (isOnSurface(intersect1)) {
                intersections.add(intersect1);
                count++;
            }
            
            return count;
        } 
        
        if (d == 0) { // one real root
            double t = -b/(2*a); // root
            
            // p0 = pB + vB*t
            vB.setMag(t);                             // = vB*t
            Point3D intersect0 = new Point3D(pB, vB); // = pB + vB*t
            if (isOnSurface(intersect0)) {
                intersections.add(intersect0);
                return 1; // one intersection
            } else {
                return 0;
            }
        }
        
        return 0; // no intersections, disjoint
    }
    
    @Override
    public int intersectionRay(Line3D line, List<Point3D> intersections) {
        final double SMALL = 0.00001;
        
        Point3D  pA = baseArc.center();
        Vector3D vA = baseArc.normal();
        double R = baseArc.radius();
        
        Point3D  pB = line.origin();
        Vector3D vB = line.toVector();
        vB.unit();
        
        // pBA = pB - pA
        Vector3D pBA = pB.vectorFrom(pA);
        
        if (Math.abs(vA.dot(vB)) > 1-SMALL) {
            return 0; // disjoint
        }
        
        // M = pBA - (vA dot pBA)vA
        Vector3D M = new Vector3D(vA); // = vA
        M.scale(-vA.dot(pBA));         // = -(vA dot pBA)vA
        M.add(pBA);                    // = pBA - (vA dot pBA)vA
        
        // N = vB - (vA dot vB)vA
        Vector3D N = new Vector3D(vA); // = vA
        N.scale(-vA.dot(vB));          // = -(vA dot vB)vA
        N.add(vB);                     // = vB - (vA dot vB)vA
        
        // Beginning with a general equation for a point on a cylinder, and an
        // equation that gives a point on a line given the parametric value t,
        // it is possible to find intersections by combining and reducing the
        // equations yeilding a quadratic formula such that the real solutions
        // for t give the position of the points on the line that intersect the
        // cylindar.
        
        // Quadratic: a*t^2 + b*t + c = 0
        double a = N.dot(N);        // = N^2
        double b = 2*M.dot(N);      // = 2(M dot N)
        double c = M.dot(M) - R*R;  // = M^2 - R^2
        
        // Discriminant
        double d = b*b - 4*a*c;     // = b^2 - 4ac
        
        if (d > 0) { // two real roots
            d = Math.sqrt(d);
            double t0 = (-b + d)/(2*a); // first real root
            double t1 = (-b - d)/(2*a); // second real root

            int count = 0;
            
            // does the point land on the ray?
            if (0<=t0) {
                // p0 = pB + vB*t0
                vB.setMag(t0);                            // = vB*t0
                Point3D intersect0 = new Point3D(pB, vB); // = pB + vB*t0
                if (isOnSurface(intersect0)) {
                    intersections.add(intersect0);
                    count++;
                }
            }
            
            // does the point land on the ray?
            if (0<=t1) {
                // p1 = pB + vB*t1
                vB.setMag(t1);                            // = vB*t1
                Point3D intersect1 = new Point3D(pB, vB); // = pB + vB*t1
                if (isOnSurface(intersect1)) {
                    intersections.add(intersect1);
                    count++;
                }
            }
            
            return count;
        } 
        
        if (d == 0) { // one real root
            double t = -b/(2*a); // root
            
            // p0 = pB + vB*t
            vB.setMag(t);           // = vB*t
            Point3D intersect0 = new Point3D(pB, vB); // = pB + vB*t
            
            if (isOnSurface(intersect0)) {
                intersections.add(intersect0);
                return 1; // one intersection
            } else {
                return 0;
            }
        }
        
        return 0; // no intersections, disjoint
    }
    
    @Override
    public int intersectionSegment(Line3D line, List<Point3D> intersections) {
        final double SMALL = 0.00001;
        
        Point3D  pA = baseArc.center();
        Vector3D vA = baseArc.normal();
        double R = baseArc.radius();
        
        Point3D  pB = line.origin();
        Vector3D vB = line.toVector();
        vB.unit();
        
        // pBA = pB - pA
        Vector3D pBA = pB.vectorFrom(pA);
        
        if (Math.abs(vA.dot(vB)) > 1-SMALL) { // parallel
            return 0; // disjoint
        }
        
        // M = pBA - (vA dot pBA)vA
        Vector3D M = new Vector3D(vA); // = vA
        M.scale(-vA.dot(pBA));         // = -(vA dot pBA)vA
        M.add(pBA);                    // = pBA - (vA dot pBA)vA
        
        // N = vB - (vA dot vB)vA
        Vector3D N = new Vector3D(vA); // = vA
        N.scale(-vA.dot(vB));          // = -(vA dot vB)vA
        N.add(vB);                     // = vB - (vA dot vB)vA
        
        // Beginning with a general equation for a point on a cylinder, and an
        // equation that gives a point on a line given the parametric value t,
        // it is possible to find intersections by combining and reducing the
        // equations yeilding a quadratic formula such that the real solutions
        // for t give the position of the points on the line that intersect the
        // cylindar.
        
        // Quadratic: a*t^2 + b*t + c = 0
        double a = N.dot(N);        // = N^2
        double b = 2*M.dot(N);      // = 2(M dot N)
        double c = M.dot(M) - R*R;  // = M^2 - R^2
        
        // Discriminant
        double d = b*b - 4*a*c;     // = b^2 - 4ac
        
        if (d > 0) { // two real roots
            d = Math.sqrt(d);
            double t0 = (-b + d)/(2*a); // first real root
            double t1 = (-b - d)/(2*a); // second real root
            
            int count = 0;
            
            // does the point land on the segment?
            if (0<=t0 && t0<=line.length()) {
                // p0 = pB + vB*t0
                vB.setMag(t0);                            // = vB*t0
                Point3D intersect0 = new Point3D(pB, vB); // = pB + vB*t0
                if (isOnSurface(intersect0)) {
                    intersections.add(intersect0);
                    count++;
                }
            }
            
            // does the point land on the segment?
            if (0<=t1 && t1<=line.length()) {
                // p1 = pB + vB*t1
                vB.setMag(t1);                            // = vB*t1
                Point3D intersect1 = new Point3D(pB, vB); // = pB + vB*t1
                if (isOnSurface(intersect1)) {
                    intersections.add(intersect1);
                    count++;
                }
            }
            
            return count;
        } 
        
        if (d == 0) { // one real root
            double t = -b/(2*a); // root
            
            // p0 = pB + vB*t
            vB.setMag(t);                             // = vB*t
            Point3D intersect0 = new Point3D(pB, vB); // = pB + vB*t
            
            if (isOnSurface(intersect0)) {
                intersections.add(intersect0);
                return 1; // one intersection
            } else {
                return 0;
            }
        }
        
        return 0; // no intersections, disjoint
    }
    
    /**
     * Returns true if the given point is on the surface of this cylindrical 
     * segment.
     * @param point the point
     * @return true if the point is on the surface
     */
    public boolean isOnSurface(Point3D point) {
        return checkHeight(point) && checkTheta(point);
    }
    
    private boolean checkHeight(Point3D point) {
        Vector3D vA = baseArc.normal();
        vA.unit();
        Vector3D pBA = baseArc.center().vectorTo(point);
        double h = pBA.dot(vA);
        return (0 <= h && h <= height);
    }
    
    private boolean checkTheta(Point3D point) {
        Vector3D pBA = baseArc.center().vectorTo(point);
        double t = baseArc.normal().angle(baseArc.originVector(), pBA);
        return (0 <= t && t <= baseArc.theta());
    }
    
    @Override
    public void translateXYZ(double dx, double dy, double dz) {
        baseArc.translateXYZ(dx, dy, dz);
    }

    @Override
    public void rotateX(double angle) {
        baseArc.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        baseArc.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        baseArc.rotateZ(angle);
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
                .append("CylindricSegment3D:\n\t")
                .append(baseArc)
                .append("\n\tHeight:\t")
                .append(height)
                .toString();
    }

    @Override
    public Point3D point(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
