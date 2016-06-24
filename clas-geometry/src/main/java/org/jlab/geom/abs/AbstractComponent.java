package org.jlab.geom.abs;

import org.jlab.geom.base.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jlab.geom.prim.Face3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Triangle3D;

/**
 * This class provides a skeletal implementation of the
 * {@link org.jlab.geom.base.Component} interface to minimize the effort
 * required to implement a {@code Component}.
 * <p>
 * To implement a {@code Component}, the programmer needs only to extend this
 * class and provide implementations for the
 * {@link org.jlab.geom.base.Component#getType()} and
 * {@link org.jlab.geom.base.Component#getLength()} methods.
 * <p>
 * Initially, the {@code Component} will contain no volume points, lines, or
 * faces and the {@code Shape3D} will be empty, so volume points, lines, and
 * faces must be added to the {@code Component} after
 * {@code AbstractComponent}'s constructor has been invoked via the  {@link #addVolumePoint(org.jlab.geom.prim.Point3D)},
 * {@link #addVolumeEdge(int, int)}, and {@link #addVolumeTriangleFace(int, int, int)}
 * methods.
 * <p>
 * To set the midpoint, use {@link #getMidpoint()} to retrieve the point and
 * modify it accordingly.<p>
 * If the subtyped {@code Component} contains additional geometry data, then, to
 * ensure that the additional geometry is properly rotated and translated with
 * the {@code Component}, the programmer must also override {@link #onTranslateXYZ(double, double, double)},
 * {@link #onRotateX(double)}, {@link #onRotateY(double)}, and
 * {@link #onRotateZ(double)}, which are invoked after the
 * {@code AbstractComponent} implementations of the
 * {@link org.jlab.geom.prim.Transformable} methods are invoked.
 *
 * @author jhankins
 */
public abstract class AbstractComponent implements Component {

    private final int componentId;
    
    private final List<Point3D> volumePoints;
    private final List<Integer> volumeEdges;
    private final Shape3D       volumeShape;
    private final Point3D       midpoint;
    
    /**
     * Initializes an empty AbstractComponent with the given id.
     * @param componentId the id of this component
     */
    protected AbstractComponent(int componentId) {
        this.componentId = componentId;
        volumePoints = new ArrayList();
        volumeEdges = new ArrayList();
        volumeShape = new Shape3D();
        midpoint = new Point3D();
    }
    
    @Override
    public final int getComponentId() {
        return componentId;
    }
    
    @Override
    public final int getNumVolumePoints() {
        return volumePoints.size();
    }
    
    @Override
    public final Point3D getVolumePoint(int pointIndex) {
        if (pointIndex<0 || pointIndex>=getNumVolumePoints()) {
            System.err.println("AbstractComponent: getVolumePoint(int pointIndex): pointIndex="+pointIndex+" is not in range [0,"+(getNumVolumePoints()-1)+")");
            return new Point3D(0, 0, -100000);
        }
        return volumePoints.get(pointIndex);
    }
    
    /**
     * Adds points to the component's volume.
     * <p>
     * Intended for use by constructors of classes extending AbstractComponent.
     * @param point the point to add
     * @see #getVolumePoint(int) 
     */
    protected final void addVolumePoint(Point3D point) {
        if (point == null)
            throw new IllegalArgumentException("point is null");
        
        volumePoints.add(new Point3D(point));
    }
    
    @Override
    public final int getNumVolumeEdges() {
        return volumeEdges.size()/2;
    }
    
    @Override
    public final Line3D getVolumeEdge(int edgeIndex) {
        if (edgeIndex<0 || edgeIndex>=getNumVolumeEdges()) {
            System.err.println("AbstractComponent: getVolumeEdge(int edgeIndex): edgeIndex="+edgeIndex+" is not in range [0,"+(getNumVolumeEdges()-1)+"]");
            return new Line3D(0, 0, -100000, 0, 0, -100000);
        }
        
        return new Line3D(getVolumePoint(volumeEdges.get(2*edgeIndex)), 
                          getVolumePoint(volumeEdges.get(2*edgeIndex+1)));
    }
    
    /**
     * Adds an edge from the point at the first index to the point at the 
     * second.
     * <p>
     * Intended for use by constructors of classes extending AbstractComponent.
     * @param pointIndex0 the first point index
     * @param pointIndex1 the second point index
     * @see #getVolumeEdge(int) 
     */
    protected final void addVolumeEdge(int pointIndex0, int pointIndex1) {
        if (pointIndex0<0 || volumePoints.size()<=pointIndex0)
            throw new IllegalArgumentException("pointIndex0="+pointIndex0+" is not in range [0,"+(volumePoints.size()-1)+"]");
        if (pointIndex1<0 || volumePoints.size()<=pointIndex1)
            throw new IllegalArgumentException("pointIndex1="+pointIndex1+" is not in range [0,"+(volumePoints.size()-1)+"]");
        
        volumeEdges.add(pointIndex0);
        volumeEdges.add(pointIndex1);
    }
    
    @Override
    public final Shape3D getVolumeShape() {
        return volumeShape;
    }
    
    /**
     * Adds a {@link org.jlab.geom.prim.Triangle3D} face to the volume shape 
     * such that the shape is composed of the points corresponding to the three
     * given point indexes.
     * <p>
     * Intended for use by constructors of classes extending AbstractComponent.
     * <p>
     * Though not required, by convention the points defining a face should be 
     * given in counterclockwise order from th perspective of an observer
     * outside the volume. This allows 3D-rendering applications to use 
     * back-face culling to increase performance.
     * @param pointIndex0 the first point index
     * @param pointIndex1 the second point index
     * @param pointIndex2 the third point index
     * @see #getVolumeShape() 
     */
    protected final void addVolumeTriangleFace(int pointIndex0, int pointIndex1, int pointIndex2) {
        if (pointIndex0<0 || volumePoints.size()<=pointIndex0)
            throw new IllegalArgumentException("pointIndex0="+pointIndex0+" is not in range [0,"+(volumePoints.size()-1)+"]");
        if (pointIndex1<0 || volumePoints.size()<=pointIndex1)
            throw new IllegalArgumentException("pointIndex1="+pointIndex1+" is not in range [0,"+(volumePoints.size()-1)+"]");
        if (pointIndex2<0 || volumePoints.size()<=pointIndex2)
            throw new IllegalArgumentException("pointIndex2="+pointIndex2+" is not in range [0,"+(volumePoints.size()-1)+"]");
        
        Face3D face = new Triangle3D(
                getVolumePoint(pointIndex0),
                getVolumePoint(pointIndex1),
                getVolumePoint(pointIndex2));
        volumeShape.addFace(face);
    }
    
    @Override
    public List<Line3D> getVolumeCrossSection(Transformation3D transformation) {
        if (transformation == null)
            throw new IllegalArgumentException("transformation is null");
        
        // Construct the cross sectional plane in the xy plane
        Plane3D crossPlane = new Plane3D(0, 0, 0, 0, 0, 1);
        // Transform the the xy plane into position
        transformation.apply(crossPlane);
        
        // Create an inverse for the transform so we can transform points from
        // 3D space into the cross sectional plane's coordinate system
        Transformation3D inv = transformation.inverse();
        
        // Find the points where the edges of the paddle intesect the plane,
        // then transform the position of each point into the cross sectional
        // plane's coordiante sytem. Also, begin summing the point's cooridnates
        // so we can find the barycenter of the transformed points easily for
        // the next step.
        double sx = 0;
        double sy = 0;
        List<Point3D> points = new ArrayList();
        for (int e=0; e<getNumVolumeEdges(); e++) {
            Point3D pt = new Point3D();
            if (crossPlane.intersectionSegment(getVolumeEdge(e), pt) == 1) {
            //if (crossPlane.intersection(getVolumeEdge(e), pt) == 1) {
                inv.apply(pt);
                points.add(pt);
                sx += pt.x();
                sy += pt.y();
            }
        }
        
        // Found no intersecting points with the cross sectional plane, return
        // an empty path
        if (points.isEmpty())
            return new ArrayList();
        
        // Find the barycenter of the points using equal weighting.
        final double bx = sx/points.size();
        final double by = sy/points.size();
        
        // Store the arctangent of each point relative to (bx, by) in each
        // point's z (since the z should be zero at this point anyways)
        for (Point3D point : points)
            point.set(point.x(), point.y(), Math.atan2(point.y()-by, point.x()-bx));
        
        // Sort the points based on their artangent
        Collections.sort(points, new Comparator<Point3D>() {
            @Override
            public int compare(Point3D a, Point3D b) {
                return Double.compare(a.z(), b.z());
            }
        });
        
        // Construct the lines from the set of points
        List<Line3D> lines = new ArrayList();
        for (int p=0; p<points.size(); p++) {
            Point3D p0 = points.get(p);
            Point3D p1 = points.get((p+1)%points.size());
            lines.add(new Line3D(p0.x(), p0.y(), 0, p1.x(), p1.y(), 0));
        }
        
        // Return the lines
        return lines;
    }
    
    @Override
    public final Point3D getMidpoint() {
        return midpoint;
    }
    
    @Override
    public final void translateXYZ(double dx, double dy, double dz) {
        for (Point3D pt : volumePoints)
            pt.translateXYZ(dx, dy, dz);
        volumeShape.translateXYZ(dx, dy, dz);
        midpoint.translateXYZ(dx, dy, dz);
        onTranslateXYZ(dx, dy, dz);
    }
    
    @Override
    public final void rotateX(double angle) {
        for (Point3D pt : volumePoints)
            pt.rotateX(angle);
        volumeShape.rotateX(angle);
        midpoint.rotateX(angle);
        onRotateX(angle);
    }
    
    @Override
    public final void rotateY(double angle) {
        for (Point3D pt : volumePoints)
            pt.rotateY(angle);
        volumeShape.rotateY(angle);
        midpoint.rotateY(angle);
        onRotateY(angle);
    }
    
    @Override
    public final void rotateZ(double angle) {
        for (Point3D pt : volumePoints)
            pt.rotateZ(angle);
        volumeShape.rotateZ(angle);
        midpoint.rotateZ(angle);
        onRotateZ(angle);
    }
    
    /**
     * Classes extending AbstractComponent should implement onTranslateXYZ so
     * that any additional geometric data they contain is translated
     * appropriately when AbstractComponent's translateXYZ method is invoked.
     * This method is called at the end of AbstractComponent's translateXYZ
     * method.
     * @param dx amount to translate along the x axis
     * @param dy amount to translate along the y axis
     * @param dz amount to translate along the z axis
     * @see org.jlab.geom.prim.Transformable#translateXYZ(double, double, double) 
     */
    protected void onTranslateXYZ(double dx, double dy, double dz) {}
    
    /**
     * Classes extending AbstractComponent should implement onRotateX so
     * that any additional geometric data they contain is rotated
     * appropriately when AbstractComponent's rotateX method is invoke.
     * This method is called at the end of AbstractComponent's rotateX method.
     * @param angle rotation angle in radians
     * @see org.jlab.geom.prim.Transformable#rotateX(double) 
     */
    protected void onRotateX(double angle) {}
    
    /**
     * Classes extending AbstractComponent should implement onRotateY so
     * that any additional geometric data they contain is rotated
     * appropriately when AbstractComponent's rotateY method is invoke.
     * This method is called at the end of AbstractComponent's rotateY method.
     * @param angle rotation angle in radians
     * @see org.jlab.geom.prim.Transformable#rotateY(double) 
     */
    protected void onRotateY(double angle) {}
    
    /**
     * Classes extending AbstractComponent should implement onRotateZ so
     * that any additional geometric data they contain is rotated
     * appropriately when AbstractComponent's rotateZ method is invoke.
     * This method is called at the end of AbstractComponent's rotateZ method.
     * @param angle rotation angle in radians
     * @see org.jlab.geom.prim.Transformable#rotateZ(double) 
     */
    protected void onRotateZ(double angle) {}
    
    @Override
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.format("%-15s\n", getType()));
        str.append(String.format("%-15s : %d\n", "Component",      componentId));
        str.deleteCharAt(str.length()-1);
        return str.toString();
    }
}
