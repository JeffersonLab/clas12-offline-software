/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.alert.AHDC;

import org.jlab.geom.abs.AbstractComponent;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sergeyeva
 */
public abstract class ConcaveComponent extends AbstractComponent {
	private final int      npoints;
	private final Vector3D direction;
	private final Line3D   line;

	protected ConcaveComponent(int componentId, List<Point3D> firstFace, List<Point3D> secondFace) {
		super(componentId);

		npoints = firstFace.size();
		if (firstFace.size() != secondFace.size()) throw new IllegalArgumentException("the number of points in the faces differ: componentId=" + componentId);
		if (npoints < 3) throw new IllegalArgumentException("there are less than three points in the faces: componentId=" + componentId);
		// if (!isCoplanarConvex(firstFace)) System.out.println("Convexity check inside ConcaveComponent constructor. First face is not convex and coplanar: componentId=" + componentId);
		// if (!isCoplanarConvex(secondFace)) System.out.println("Convexity check inside ConcaveComponent constructor. Second face is not convex and coplanar: componentId=" + componentId);

		// Find the center of the bottom
		//Point3D botMidpoint = Point3D.average(firstFace);
		Point3D botMidpoint = firstFace.get(0).midpoint(firstFace.get(3));

		// Find the center of the top
		//Point3D topMidpoint = Point3D.average(secondFace);
		Point3D topMidpoint = secondFace.get(0).midpoint(secondFace.get(3));

		// Create the line from the center of the bottom to the center of the top
		line = new Line3D(botMidpoint, topMidpoint);

		// Create the direction pointing from the bottom to the top
		direction = botMidpoint.vectorTo(topMidpoint);
		direction.unit();

		// Create the midpoint at the middle of the line
		getMidpoint().copy(line.midpoint());

		// Many 3D rendering engines can increase performance by only drawing
		// triangles whose points appear in counter-clockwise from the
		// perspective of the camera. This is known as back face culling.
		// Determine the current ordering of the points for the top and bottom.
		Vector3D u, v, n;
		u = firstFace.get(1).vectorFrom(firstFace.get(0));
		v = firstFace.get(2).vectorFrom(firstFace.get(0));
		n = u.cross(v);
		if (n.dot(direction) > 0.001) {
			// System.err.println("PrismaticComponent: #" + componentId + " " + n.dot(direction) + " > 0");
			// for (Point3D point : firstFace) System.err.println("\t" + point);
			throw new IllegalArgumentException("the first face is not counter-clockwise: componentId=" + componentId);
		}
		u = secondFace.get(1).vectorFrom(secondFace.get(0));
		v = secondFace.get(2).vectorFrom(secondFace.get(0));
		n = u.cross(v);
		if (n.dot(direction) > 0.001) {
			// System.err.println("PrismaticComponent: #" + componentId + " " + n.dot(direction) + " > 0");
			// for (Point3D point : secondFace) System.err.println("\t" + point);
			throw new IllegalArgumentException("the second face is not clockwise: componentId=" + componentId);
		}

		// Add the bottom points
		for (int i = 0; i < npoints; i++)
		     addVolumePoint(firstFace.get(i));
		// Add the top points
		for (int i = 0; i < npoints; i++)
		     addVolumePoint(secondFace.get(i));
		// Add the bottom edges
		for (int i = 1; i < npoints; i++)
		     addVolumeEdge(i - 1, i);
		// Add the top edges
		for (int i = npoints + 1; i < 2 * npoints; i++)
		     addVolumeEdge(i - 1, i);
		// Add edges from the bottom to the top
		for (int i = 0; i < npoints; i++)
		     addVolumeEdge(i, i + npoints);

		// Add faces on the sides. Add the side faces first since they're more
		// likely to be hit them first since their more likely to be
		// hit than the top or bottom and the hit finding algorithm will likely
		// iterate through the faces linearly
		for (int i = 1; i <= npoints; i++) {
			addVolumeTriangleFace(i - 1, i + npoints - 1, (i % npoints));
			addVolumeTriangleFace((i % npoints) + npoints, (i % npoints), i + npoints - 1);
		}
		// Add the bottom faces
		for (int i = 2; i < npoints; i++) {
			addVolumeTriangleFace(0, i - 1, i);
		}
		// Add the top faces
		for (int i = 2; i < npoints; i++) {
			addVolumeTriangleFace(npoints, npoints + i, npoints + i - 1);
		}
	}

	/**
	 * Returns true if the points are coplanar and convex.
	 * <p>
	 * Developer's note: The function isCoplanar(Point3D... points) could be
	 * moved into Point3D so that the routine could be used elsewhere.
	 *
	 * @param points the points forming a polygon
	 * @return true if the points form a coplanar convex polygon
	 */
	private static boolean isCoplanarConvex(List<Point3D> points) {
		// The points in the list are presumed to form a polygon such that
		// adjacent points in the list share and edge and the first and last
		// points in the list also share an edge.
		//
		// Let the "normal" vector of a point be the vector formed by the cross
		// product of the vector from the previous point to the current point
		// and from the current point to the next point.
		//
		// If the polygon is convex and coplanar then the "normal" vectors of
		// each point of the polygon will be parallel.
		Point3D  point0 = points.get(0);
		Point3D  point1 = points.get(1);
		Point3D  point2 = points.get(2);
		Vector3D u, v, w, n;
		u = point0.vectorTo(point1);
		v = point1.vectorTo(point2);
		n = u.cross(v);
		n.unit();
		for (int p = 1; p < points.size(); p++) {
			point1 = point2;
			point2 = points.get((p + 2) % points.size());
			u      = v;
			v      = point1.vectorTo(point2);
			w      = u.cross(v);
			w.unit();
			if (n.dot(w) < (1 - Math.ulp(1.0) * 10)) {
				// System.err.println("PrismaticComponent: isCoplanarConvex(List<Point3D> points): " + n.dot(w) + " < " + (1 - Math.ulp(1.0) * 10));
				// for (Point3D point : points) System.err.println("\t" + point);
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a vector pointing from the {@link #getMidpoint() midpoint} of
	 * this component towards the readout end of the component (e.g. towards the
	 * PMT of a scintillator).
	 *
	 * @return the direction of the readout end
	 */
	public Vector3D getDirection() {
		return direction;
	}

	/**
	 * Returns a line running down the middle of the length of the component
	 * such that the two end points of the line segment are at the geometric
	 * centers of the two polygons defining this component's volume. By
	 * convention, the origin point of the line lies at the geometric center of
	 * the first face and the end point of the line lies at the
	 * geometric center of the second face.
	 * <p>
	 * Note that {@code getLength()=getLine().length()}.
	 *
	 * @return a line running down the middle of the length the component
	 * @see #getLength()
	 */
	public Line3D getLine() {
		return line;
	}

	@Override
	public double getLength() {
		return line.length();
	}

	@Override
	protected void onTranslateXYZ(double dx, double dy, double dz) {
		line.translateXYZ(dx, dy, dz);
	}

	@Override
	protected void onRotateX(double angle) {
		direction.rotateX(angle);
		line.rotateX(angle);
	}

	@Override
	protected void onRotateY(double angle) {
		direction.rotateY(angle);
		line.rotateY(angle);
	}

	@Override
	protected void onRotateZ(double angle) {
		direction.rotateZ(angle);
		line.rotateZ(angle);
	}

	/**
	 * Returns true if the given line segment intersect the volume-shape twice
	 * (one incoming intersection and one out going) and stores the incoming and
	 * outgoing intersection points are stored in the first and second Point3D
	 * objects given as arguments respectively.
	 * <p>
	 * The incoming intersection is defined such that the incoming intersection
	 * point is closer to the origin of the line than the outgoing intersection
	 * point.
	 * <p>
	 * Developer's Note: For optimization, this algorithm assumes that
	 * <ol>
	 * <li>the volume contains N*4-4 triangular faces</li>
	 * <li>the faces form a closed bounding volume</li>
	 * <li>neither end point of the line segment are inside the volume</li>
	 * <li>pairs of faces from index 0 through N*2-1 (i.e. 1 and 2, 2 and 3, 3
	 * and 4, etc.) are coplanar (the side trapezoids)</li>
	 * <li>the faces from N*2 through N*3-3 are coplanar (the first base)</li>
	 * <li>the faces from N*3-3 through N*4-5 are coplanar (the second
	 * base)</li>
	 * </ol>
	 * where N is the number of points in either polygonal base.<br>
	 *
	 * @param line         the line segment
	 * @param inIntersect  the incoming intersection
	 * @param outIntersect the outgoing intersection
	 * @return true if two intersections are found
	 */
	@Override
	public boolean getVolumeIntersection(Line3D line, Point3D inIntersect, Point3D outIntersect) {
		List<Point3D> intersects  = new ArrayList<>();
		Shape3D       volumeShape = getVolumeShape();
		int           i           = 0;
		for (; i < npoints * 4 - 4; i++) {
			// If the line has not intersected the side or bottom faces...
			if (i == npoints * 3 - 2) {
				// Then it isn't going to
				return false;
			}
			// If the line intersects the current face...
			if (volumeShape.face(i).intersectionSegment(line, intersects) > 0) {
				// If the intersection is on a side...
				if (i < npoints * 2) {
					// Increment the index
					i++;
					// Each side has two faces, but it's not possible to hit the
					// same side twice so skip the face on the same side if it
					// hasn't been checked yet.
					// Note: if i is even then ((~i)&1) = 1, otherwise 0
					i += ((~i) & 1);
				}
				// If the intersection is on the bottom...
				else {
					// It is not possible to hit the bottom face twice and the
					// side faces have already been checked, so skip the top.
					i = npoints * 3 - 2;
				}
				break;
			}
		}
		for (; i < volumeShape.size(); i++) {
			if (volumeShape.face(i).intersectionSegment(line, intersects) > 0) {
				// We've found both intersections!
				// Make sure the incoming intersection is closer to the line's
				// origin point...
				if (line.origin().distance(intersects.get(0)) < line.origin().distance(intersects.get(1))) {
					inIntersect.copy(intersects.get(0));
					outIntersect.copy(intersects.get(1));
				} else {
					inIntersect.copy(intersects.get(1));
					outIntersect.copy(intersects.get(0));
				}
				// Return true
				return true;
			}
		}
		return false;
	}

}
