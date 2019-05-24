package org.jlab.geom.prim;

import java.util.List;
import org.jlab.geom.Showable;

/**
 * A 3D triangle represented by three points.
 * <p>
 * Since any three points in 3D space that are not collinear define a plane,
 * a face can be converted into a plane via {link #plane()}.
 * <p>
 * The normal of the surface of a face is oriented such that when looking
 * antiparallel to the normal towards the face the face's points wound
 * counterclockwise. Conversely, when looking in a direction parallel to the
 * normal, the points are wound in a clockwise fashion.
 * <p>
 * The intersection of a line with a face can be calculated using the
 * intersection methods:
 * {@link #intersection(org.jlab.geom.prim.Line3D, List) intersection(...)}, 
 * {@link #intersectionRay(org.jlab.geom.prim.Line3D, List) intersectionRay(...)}, 
 * {@link #intersectionSegment(org.jlab.geom.prim.Line3D, List) intersectionSegment(...)}.
 *
 * @author gavalian
 */
public interface Face3D extends Transformable, Showable {
    /**
     * Returns the point from this {@code Face3D} with corresponding index. If
     * an invalid index is given, then null is returned.
     *
     * @param index index of the point
     * @return the point at the corresponding index, otherwise null
     */
    Point3D point(int index);
    
    /**
     * Finds the intersections of the given infinite line with this
     * {@code Face3D}. If intersections are found they will be appended to the
     * given list. The return value will indicate the number of intersections
     * that were found.
     * @param line the infinite line
     * @param intersections the list to store the intersections in
     * @return the number of intersections found
     */
    int intersection(Line3D line, List<Point3D> intersections);

    /**
     * Finds the intersections of the given ray with this
     * {@code Face3D}. If intersections are found they will be appended to the
     * given list. The return value will indicate the number of intersections
     * that were found.
     * @param line the ray
     * @param intersections the list to store the intersections in
     * @return the number of intersections found
     */
    int intersectionRay(Line3D line, List<Point3D> intersections);
    
    /**
     * Finds the intersections of the given line segment with this
     * {@code Face3D}. If intersections are found they will be appended to the
     * given list. The return value will indicate the number of intersections
     * that were found.
     *
     * @param line the line segment
     * @param intersections the list to store the intersections in
     * @return the number of intersections found
     */
    int intersectionSegment(Line3D line, List<Point3D> intersections);
}
