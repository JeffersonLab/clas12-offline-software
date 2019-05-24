package cnuphys.bCNU.geometry;

import java.util.ArrayList;

/**
 * A poly tube is a collection of infinite lines that are added in order.
 * Consider a hexagonal tube. That would be the six edges added either CW or CCW
 * as you look down the tube.
 * 
 * @author heddle
 *
 */

public class Polytube extends ArrayList<Line> {

	/**
	 * Get a polyline for the start or end face. This treats all the lines as
	 * directed segments since the enf (face) has no meaning for infinite lines.
	 * 
	 * @param end one of the Constants START or END
	 * @return a polyline for the given end
	 */
	public Polyline getFace(int end) {

		Polyline pline = null;

		if ((end == Constants.START) || (end == Constants.END)) {
			pline = new Polyline();
			for (Line line : this) {
				pline.add(line.getEndpoint(end));
			}
		}
		return pline;
	}

	/**
	 * Get the polyline representing the intersection of this polytube with a plane
	 * 
	 * @param plane    the plane to intersect
	 * @param lineType either Constants.INFINITE or Constants.SEGMENT
	 * @return the polyline or <code>null</code> if no intersection is found
	 */
	public Polyline planeIntersection(Plane plane, int lineType) {

		Polyline pline = null;

		if ((lineType == Constants.INFINITE) || (lineType == Constants.SEGMENT)) {
			pline = new Polyline();
			for (Line line : this) {
				Point inter = new Point();

				double t = plane.lineIntersection(line, inter, lineType);
				if (!Double.isNaN(t)) {
					pline.add(inter);
				}
			}
		}

		// don't return an empty pline (i.e. no lines interesect)
		if ((pline == null) || (pline.isEmpty())) {
			return null;
		}

		return pline;
	}

	/**
	 * Get a String representation
	 * 
	 * @return a String representation of the PolyTube
	 */
	@Override
	public String toString() {
		if (size() == 0) {
			return "Empty PolyTube";
		}
		StringBuffer sb = new StringBuffer(60 * size());

		sb.append("PolyTube has " + size() + " lines.\n");
		for (int i = 0; i < size(); i++) {
			Line line = get(i);
			sb.append("[" + (i + 1) + "] " + line + "\n");
		}

		return sb.toString();
	}

	// for testing
	public static void main(String arg[]) {
		Point p = new Point(1, 1, 1);
		Vector norm = new Vector(1, 1, 1);

		Plane plane = new Plane(norm, p);

		Polytube tube = new Polytube();
		for (int i = 0; i < 6; i++) {
			double phi = i * Math.PI / 3;
			double x = Math.cos(phi);
			double y = Math.sin(phi);
			double z1 = -10;
			double z2 = 3.5;

			tube.add(new Line(new Point(x, y, z1), new Point(x, y, z2)));
		}

		System.out.println(tube.toString());

		// get the faces
		Polyline startFace = tube.getFace(Constants.START);
		System.out.println("Start Face:\n" + startFace);

		Polyline endFace = tube.getFace(Constants.END);
		System.out.println("End Face:\n" + endFace);

		// intersections
		Polyline infInter = tube.planeIntersection(plane, Constants.INFINITE);
		System.out.println("Infinite intersection:\n" + infInter);

		Polyline segInter = tube.planeIntersection(plane, Constants.SEGMENT);
		System.out.println("Segment intersection:\n" + segInter);

	}

}
