/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.alert.AHDC;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

import java.util.List;

/**
 * @author sergeyeva
 */
public class AlertDCWire extends ConcaveComponent {

	/**
	 * Initializes a {@code ALERTDCWire} from the given points.
	 * The line will be from the middle of the bottom face to the middle of the
	 * top face.
	 *
	 * @param componentId the id of the component
	 * @param firstFace   first face of N points
	 * @param secondFace  second face of N points
	 * @param line        Middle line that represents the signal wire
	 */

	public AlertDCWire(int componentId, Line3D line, List<Point3D> firstFace, List<Point3D> secondFace) {
		super(componentId, firstFace, secondFace);
		getLine().copy(line);
		// if (isConcave(firstFace)) System.out.println("First face is concave: componentId=" + componentId);
		// if (!isConcave(secondFace)) System.out.println("Second face is concave: componentId=" + componentId);
	}

	/**
	 * Returns "ALERTDCWire Component".
	 *
	 * @return "ALERTDCWire Component"
	 */
	@Override
	public String getType() {
		return "ALERTDCWire Component";
	}

	private static boolean isConcave(List<Point3D> points) {
		// The points in the list are presumed to form a polygon such that
		// adjacent points in the list share and edge and the first and last
		// points in the list also share an edge.
		//
		// Let the "normal" vector of a point be the vector formed by the cross
		// product of the vector from the previous point to the current point
		// and from the current point to the next point.
		//
		// If the polygon is convex and coplannar then the "normal" vectors of
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
				// for (Point3D point : points) System.out.println("\t" + point);
				return true;
			}
		}
		return false;
	}

}
