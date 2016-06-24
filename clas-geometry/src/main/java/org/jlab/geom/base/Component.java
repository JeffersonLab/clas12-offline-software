package org.jlab.geom.base;

import java.util.List;
import org.jlab.geom.Showable;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformable;
import org.jlab.geom.prim.Transformation3D;


/**
 * One sensing component of a detector.
 * <p>
 * An object implementing this interface represents single sensing component
 * within a detector and provides access to essential geometry information
 * about that component.
 * <p>
 * Factory: {@link org.jlab.geom.base.Factory Factory}<br>
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.base.Detector Detector} → 
 * {@link org.jlab.geom.base.Sector Sector} → 
 * {@link org.jlab.geom.base.Superlayer Superlayer} → 
 * {@link org.jlab.geom.base.Layer Layer} → 
 * <b>{@link org.jlab.geom.base.Component Component}</b>
 * </code>
 * <p>
 * {@code Component} contains a {@code Point3D} called {@link #getMidpoint() 
 * midpoint} which generally represents the geometric center of the
 * {@code Component} but may be redefined as needed by specific kinds of
 * {@code Component}s.
 * <p>
 * {@code Component} also contains a {@code double} value called 
 * {@link #getLength() length} that represents the length of the component.
 * What this value measures is dependent on the type of component.
 * <p>
 * {@code Componet}s are also enclosed in volume that are defined points, edges, 
 * and faces.
 * 
 * @author jnhankins
 */
public interface Component extends Transformable, Showable {
    /**
     * Returns the component's id number.
     * @return the id
     */
    int getComponentId();
    
    /**
     * Returns the number of points that define the 
     * {@link #getVolumeShape() volume}.
     * @return the number of volume points
     */
    int getNumVolumePoints();
    
    /**
     * Returns the point on the {@link #getVolumeShape() volume} with the 
     * specified index.
     * @param p the index of the point
     * @return the point on the volume
     */
    Point3D getVolumePoint(int p);
    
    /**
     * Returns the number of edges between the points that define the 
     * {@link #getVolumeShape() volume}.
     * @return the number of volume edges
     */
    int getNumVolumeEdges();
    
    /**
     * Returns the edge on the {@link #getVolumeShape() volume} with the
     * specified index.
     * <p>
     * Though edges are returned as {@link org.jlab.geom.prim.Line3D} objects
     * and a majority components have edges which are all straight line
     * segments, edges are not required to be straight lines. The edge may, in
     * reality, be curved, in which case the returned
     * {@link org.jlab.geom.prim.Line3D} is merely storing the two points
     * bounding a curved path segment. Thus, special care must be taken to
     * ensure that the edge is in fact a straight line segment if one intends to
     * use it as such.
     * @param e the index of the edge
     * @return the edge on the volume
     */
    Line3D getVolumeEdge(int e);
    
    /**
     * Returns the volume of the component.
     * <p>
     * A volume is represented by a {@code Shape3D} object which contains a 
     * list of triangular {@code Face3D} objects. This these triangles bound
     * a closed closed volume.
     * @return the volume
     */
    Shape3D getVolumeShape();
    
    /**
     * Returns the cross section of a plane through the the component's
     * {@link #getVolumeShape() volume} as a list of lines in the xy-plane.
     * <p>
     * This method takes a Transformation3D object as an argument, then
     * constructs a new {@code Plane3D} object in the xy-plane and applies the
     * transformation to the plane. The intersection of the volume's shape with
     * the transformed plane is then calculated and stored in a list of lines.
     * The inverse of the transformation is then applied to each line in the
     * list ensuring that each line in the returned list is in the xy-plane.
     *
     * @param transformation the transformation to apply to the plane
     * @return a list of lines in the xy-plane representing the cross section
     */
    List<Line3D> getVolumeCrossSection(Transformation3D transformation);
    
    /**
     * Returns true if the given line segment intersect the 
     * {@link #getVolumeShape() volume-shape} twice (one incoming intersection 
     * and one out going) and stores the incoming and outgoing intersection 
     * points are stored in the first and second Point3D objects given as 
     * arguments respectively.
     * <p>
     * The incoming intersection is defined such that the incoming intersection
     * point is closer to the origin of the line than the outgoing intersection 
     * point.
     * 
     * @param line the line segment
     * @param inIntersect  the incoming intersection
     * @param outIntersect the outgoing intersection
     * @return true if two intersections are found
     */
    boolean getVolumeIntersection(Line3D line, Point3D inIntersect, Point3D outIntersect);
 
    /**
     * Returns the midpoint of this component. By convention, the midpoint of a
     * component is normally the geometric center of the component's volume but
     * the definition of midpoint may vary by component type.
     * @return the midpoint
     */
    Point3D getMidpoint();
    
    /**
     * Returns the length of this component.
     * @return the length
     */
    double getLength();
    
    /**
     * Returns a string that identifies the specific subtype of this component.
     * @return a string naming this component's type
     */
    String getType();
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    void show();
}
