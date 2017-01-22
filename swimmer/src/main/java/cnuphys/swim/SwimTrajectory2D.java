package cnuphys.swim;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.List;

import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.lund.LundId;

/**
 * A 2D version of the 3D SwimTrajectory where all points have been projected
 * onto a 2D plane.
 * 
 * @author heddle
 * 
 */
public class SwimTrajectory2D {

    private static final String fbColor = "$cornsilk$";

    public static final String SMALL_THETA = "\u03B8";
    public static final String SMALL_PHI = "\u03C6";
    public static final String SUPER2 = "\u00B2";

    // the 3D trajectory
    private SwimTrajectory _trajectory3D;

    // the 2D path
    private Point.Double[] _path;

    /**
     * Create a 2D trajectory from the 3D trajectory
     * 
     * @param trajectory
     *            the 3D trajectory from a swim
     * @param projector
     *            projects 3D to 2D
     */
    public SwimTrajectory2D(SwimTrajectory trajectory, IProjector projector) {
	_trajectory3D = trajectory;
	int size = (trajectory == null) ? 0 : trajectory.size();

	if (size > 1) {
	    _path = new Point.Double[size];

	    int index = 0;
	    for (double v3d[] : trajectory) {
		_path[index] = new Point.Double();
		// for sector this is just the usual worldFromLabXYZ
		projector.project(v3d, _path[index]);
		index++;
	    }
	}
    }

    /**
     * Get the 2D path. This is comprised of all the 3D points in the trajectory
     * that came from a swim that have been projected on to 2D.
     * 
     * @return the 2D path
     */
    public Point.Double[] getPath() {
	return _path;
    }

    /**
     * Add to the feedback strings
     * 
     * @param feedbackStrings
     */
    public void addToFeedback(List<String> feedbackStrings) {
	GeneratedParticleRecord genPart = _trajectory3D
		.getGeneratedParticleRecord();

	LundId lid = _trajectory3D.getLundId();

	String s1 = null;

	if (lid != null) {

	    s1 = fbColor
		    + String.format("Swam  %-4s %-6.2f MeV/c" + SUPER2,
			    lid.getName(), lid.getMass() * 1000.0);
	} else {
	    s1 = fbColor + "[No PID]";
	}

	s1 += String.format(" P %-6.2f Mev/c", genPart.getMomentum() * 1000.0);

	String s2 = fbColor
		+ String.format(
			"Vtx (%-3.1f, %-3.1f, %-3.1f) cm %-1s: %-6.2f  %-1s: %-6.2f",
			genPart.getVertexX(), genPart.getVertexY(),
			genPart.getVertexZ(), SMALL_THETA, genPart.getTheta(),
			SMALL_PHI, genPart.getPhi());

	feedbackStrings.add(s1);
	feedbackStrings.add(s2);
    }

    public String summaryString() {

	GeneratedParticleRecord genPart = _trajectory3D
		.getGeneratedParticleRecord();

	LundId lid = _trajectory3D.getLundId();

	StringBuffer sb = new StringBuffer(255);
	if (lid != null) {
	    sb.append("[" + lid.getName() + "]");
	} else {
	    sb.append("[No PID]");
	}

	sb.append(String.format(" P %-4.1f GeV/c", genPart.getMomentum()));

	sb.append(String.format(" Vtx (%.1f, %.1f, %.1f) cm",
		genPart.getVertexX(), genPart.getVertexY(),
		genPart.getVertexZ()));

	sb.append(String.format("  %-1s %-6.2f  %-1s %-6.2f", SMALL_THETA,
		genPart.getTheta(), SMALL_PHI, genPart.getPhi()));

	return sb.toString();
    }

	/**
	 * Get the minimum distance to the trajectory.
	 * 
	 * @param wp
	 *            the point in question.
	 * @return the minimum distance from the point to the trajectory.
	 */
	public double closestDistance(Point.Double wp) {
		if ((_path == null) || (_path.length == 0) || (wp == null)) {
			return Double.NaN;
		}
		if (_path.length == 1) {
			return wp.distance(_path[0]);
		}
		
		int len = _path.length;
		Point.Double wpi = new Point.Double();
		double minDist = Double.POSITIVE_INFINITY;
		for (int i = 1; i < len; i++) {
			Point.Double wp0 = _path[i-1];
			Point.Double wp1 = _path[i];
			double dist = Line2D.ptSegDist(wp0.x, wp0.y, wp1.x, wp1.y, wp.x, wp.y);
	//		double dist = perpendicularDistance(wp0, wp1, wp, wpi);
			if (dist < minDist) {
				minDist = dist;
			}
			wp0 = wp1;
		}
		return minDist;
	}

    /**
     * @return the trajectory3D
     */
    public SwimTrajectory getTrajectory3D() {
	return _trajectory3D;
    }

    /**
     * Given two points p0 and p1, imagine a line from p0 to p1. Take the line
     * to be parameterized by parameter t so that at t = 0 we are at p0 and t =
     * 1 we are at p1.
     * 
     * @param p0
     *            start point of main line
     * @param p1
     *            end point of main line
     * @param wp
     *            the point from which we drop a perpendicular to p0 -> p1
     * @param pintersect
     *            the intersection point of the perpendicular and the line
     *            containing p0-p1. It may or may not actually be between p0 and
     *            p1, as specified by the value of t.
     * @return the perpendicular distance to the line. If t is between 0 and 1
     *         the intersection is on the line. If t < 0 the intersection is on
     *         the "infinite line" but not on p0->p1, it is on the p0 side; this
     *         returns the distance to p0. If t > 1 the intersection is on the
     *         p1 side; this returns the distance to p1.
     */
    public static double perpendicularDistance(Point.Double p0,
	    Point.Double p1, Point.Double wp, Point.Double pintersect) {
	double delx = p1.x - p0.x;
	double dely = p1.y - p0.y;

	double numerator = delx * (wp.x - p0.x) + dely * (wp.y - p0.y);
	double denominator = delx * delx + dely * dely;
	double t = numerator / denominator;
	pintersect.x = p0.x + t * delx;
	pintersect.y = p0.y + t * dely;

	if (t < 0.0) { // intersection not on line, on p0 side
	    return p0.distance(wp);
	} else if (t > 1.0) {// intersection not on line, on p1 side
	    return p1.distance(wp);
	}
	// intersection on line
	return pintersect.distance(wp);
    }

}