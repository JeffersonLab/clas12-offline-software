package org.jlab.geom.prim;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jlab.geom.Showable;

/**
 * A collection of {@link org.jlab.geom.prim.Face3D Face3D} objects.
 * <p>
 * Faces can be added and retrieved via 
 * {@link #addFace(org.jlab.geom.prim.Face3D)} and {@link #face(int)}.
 * <p>
 * The center of the shape can be calculated via {@link #center()}, which 
 * returns the geometric mean of all of the points of all the faces contained
 * in this shape.
 * <p>
 * The {@link #moveTo(org.jlab.geom.prim.Point3D)} method translates this
 * shape so that its center is at the given point.
 * @author gavalian
 */
public class Shape3D implements Transformable, Showable {
    private final ArrayList<Face3D> faces = new ArrayList(); // the faces
    
    /**
     * Constructs a new empty {@code Shape3D}.
     */
    public Shape3D() {
        // nothing to do
    }
    
    /**
     * Constructs a new {@code Shape3D} from the given faces. If any of the
     * face are subsequently modified, then this shape will also be modified.
     * @param faces the faces of of this shape
     */
    public Shape3D(Face3D... faces) {
        for (Face3D face: faces) {
            this.faces.add(face);
        }
    }
    
    /**
     * Adds the given face to this shape. If the face is subsequently modified,
     * this shape will also be modified.
     * @param face the face to add
     */
    public void addFace(Face3D face) {
        faces.add(face);
    }
    
    /**
     * Returns the face corresponding to the given index.
     * @param index the index of the face to get
     * @return the face at the corresponding index or null if there is no such 
     * face
     */
    public Face3D face(int index) {
        if (index<0 || faces.size()<=index) {
            System.err.println("Warning: Face3D: faces(int index): invalid index="+index);
            return null;
        }
        return faces.get(index);
    }
    
    /**
     * Returns the number of faces in this shape.
     * @return the number of faces in this shape
     */
    public int size() {
        return faces.size();
    }
    
    /**
     * Constructs a point center of the shape. This point is at the geometric
     * mean of every point in every face of the shape.
     * @return a point at the geometric center of the shape, or a point at the
     * origin if the shape has no faces
     */
    public Point3D center() {
        int numPoints = size()*3;
        if (numPoints == 0) {
            System.err.println("Warning: Shape3D: center(): the shape has no faces");
            return new Point3D();
        }
        double cX = 0;
        double cY = 0;
        double cZ = 0;
        for (Face3D face : faces) {
            for (int p = 0; p < 3; p++) {
                cX += face.point(p).x();
                cY += face.point(p).y();
                cZ += face.point(p).z();
            }
        }
        return new Point3D(cX/numPoints, cY/numPoints, cZ/numPoints);
    }
    
    @Override
    public void translateXYZ(double x, double y, double z) {
        for (Face3D face : faces) {
            face.translateXYZ(x, y, z);
        }
    }
    @Override
    public void rotateX(double angle) {
        for (Face3D face : faces) {
            face.rotateX(angle);
        }
    }
    @Override
    public void rotateY(double angle) {
        for (Face3D face : faces) {
            face.rotateY(angle);
        }
    }
    @Override
    public void rotateZ(double angle) {
        for (Face3D face : faces) {
            face.rotateZ(angle);
        }
    }
    
    /**
     * Translates this shape so that it's geometric center coincides with the 
     * given point.
     * @param point the new position of the shape's center
     */
    public void moveTo(Point3D point) {
        moveTo(point.x(), point.y(), point.z());
    }
    
    /**
     * Translates this shape so that it's geometric center coincides with the 
     * given point at the given coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public void moveTo(double x, double y, double z) {
        Point3D cent = center();
        translateXYZ(x-cent.x(), y-cent.y(), z-cent.z());
    }
    
    /**
     * Finds intersections of the given infinite line with this shape. 
     * Intersection points will be appended to the given list.
     *
     * @param line the infinite line
     * @param intersections the list of intersections
     * @return the number of intersections that were found
     */
    public int intersection(final Line3D line, List<Point3D> intersections) {
        int count = 0;
        for (Face3D face : faces)
            count += face.intersection(line, intersections);
        return count;
    }

    /**
     * Finds intersections of the given infinite line with this shape. 
     * Intersection points will be appended to the given list.
     *
     * @param line the infinite line
     * @param intersections the list of intersections
     * @return the number of intersections that were found
     */
    public int intersection_with_faces(final Line3D line, List<Point3D> intersections, List<Integer> ifaces) {
        int count = 0;
        int ifa = 0;
        int nint = 0;
        for (Face3D face : faces){
            nint = face.intersection(line, intersections);
            count += nint;
            for(int ii=0; ii<nint; ii++)ifaces.add(ifa);
            ifa++;
        }
        return count;
    }

    /**
     * Finds intersections of the given ray with this shape. 
     * Intersection points will be appended to the given list.
     *
     * @param line the ray
     * @param intersections the list of intersections
     * @return the number of intersections that were found
     */
    public int intersectionRay(final Line3D line, List<Point3D> intersections) {
        int count = 0;
        for (Face3D face : faces)
            count += face.intersectionRay(line, intersections);
        return count;
    }
    
    /**
     * Finds intersections of the given line segment with this shape. 
     * Intersection points will be appended to the given list.
     * 
     * @param line the line segment
     * @param intersections the list of intersections
     * @return the number of intersections that were found
     */
    public int intersectionSegment(final Line3D line, List<Point3D> intersections) {
        int count = 0;
        for (Face3D face : faces)
            count += face.intersectionSegment(line, intersections);
        return count;
    }
    
    /**
     * Returns true if the infinite line intersects this shape.
     * @param line the infinite line
     * @return true if the line intersects the shape
     */
    public boolean hasIntersection(Line3D line) {
        List<Point3D> list = new ArrayList();
        for (Face3D face : faces)
            if (face.intersection(line, list) > 0)
                return true;
        return false;
    }
    
    /**
     * Returns true if the ray intersects this shape.
     * @param line the ray
     * @return true if the line intersects the shape
     */
    public boolean hasIntersectionRay(Line3D line) {
        List<Point3D> list = new ArrayList();
        for (Face3D face : faces)
            if (face.intersectionRay(line, list) > 0)
                return true;
        return false;
    }
    
    /**
     * Returns true if the line segment intersects this shape.
     * @param line the line segment
     * @return true if the line intersects the shape
     */
    public boolean hasIntersectionSegment(Line3D line) {
        List<Point3D> list = new ArrayList();
        for (Face3D face : faces)
            if (face.intersectionSegment(line, list) > 0)
                return true;
        return false;
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
        StringBuilder str = new StringBuilder();
        str.append("Shape3D:\n");
        for (Face3D face : faces) {
            str.append("\t");
            str.append(face);
            str.append("\n");
        }
        str.deleteCharAt(str.length()-1);
        return str.toString();
    }
    
    public static Shape3D  box(double xd, double yd, double zd){
        
        Shape3D  shapeBox = new Shape3D();
        shapeBox.addFace(new Triangle3D(
                 xd/2.0,-yd/2.0,-zd/2.0,
                 xd/2.0, yd/2.0,-zd/2.0,
                -xd/2.0, yd/2.0,-zd/2.0)
                );
        shapeBox.addFace(new Triangle3D(
                -xd/2.0, yd/2.0,-zd/2.0,
                -xd/2.0,-yd/2.0,-zd/2.0,
                xd/2.0,-yd/2.0,-zd/2.0)
                );
        
        return   shapeBox;
    }
    
    public String getMeshFXML(){
        StringBuilder str = new StringBuilder();
        str.append("\t<mesh>\n");
        str.append("\t\t<TriangleMesh>\n");
        str.append("\t\t\t<points>");
        for(int loop = 0; loop < this.faces.size();loop++){
            for(int f = 0; f < 3; f++){
                str.append(String.format("%8.3f %8.3f %8.3f  ", 
                        this.face(loop).point(f).x(),
                        this.face(loop).point(f).y(),
                        this.face(loop).point(f).z()
                        ));
            }
        }
        str.append("</points>\n");
        str.append("\t\t\t<faces>");
        for(int loop = 0; loop < this.faces.size();loop++){
            str.append(String.format("%d %d %d ", 0 + 3*loop, 1 + 3*loop, 2 + 3*loop));
        }
        str.append("</faces>\n");
        
        str.append("\t\t</TriangleMesh>\n");
        str.append("\t</mesh>\n");
        return str.toString();
    }
}
