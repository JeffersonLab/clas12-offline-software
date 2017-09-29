package cnuphys.ced.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class BSTxyPanel implements Comparable<BSTxyPanel> {

	private double z0, z1, z2, z3, z4, z5;

	// the layer [1..8]
	private int layer;

	// the "sector" 1 .. max, max depends on layer,
	// max = {10, 14, 18, 24} for layers {(1,2), (3,4), (5,6), (7,8)}
	// respectively
	private int _sector;

	// used in z view for ordering
	private double perp;

	// used by z view to mark hits along z
	// so three entries
	public boolean hit[] = new boolean[3];

	private Line2D.Double line;

	/**
	 * Create an BSTxyPanel. The panel is really just a thick line
	 * 
	 * @param sect
	 *            1-based
	 * @param lay
	 *            1..8
	 * @param vals
	 *            should be an array of 10 numbers x y x y z0..z5
	 */
	public BSTxyPanel(int sect, int lay, double vals[]) {
		_sector = sect;
		layer = lay;
		double x1 = vals[0];
		double y1 = vals[1];
		double x2 = vals[2];
		double y2 = vals[3];

		line = new Line2D.Double(x1, y1, x2, y2);

		z0 = vals[4];
		z1 = vals[5];
		z2 = vals[6];
		z3 = vals[7];
		z4 = vals[8];
		z5 = vals[9];
	}

	/**
	 * Get the z index, 0..2 for the three sensitive areas
	 * 
	 * @param z
	 *            the z value
	 * @return the z index or -1 for out of range
	 */
	public int getZIndex(double z) {
		if ((z < z0) || (z > z5)) {
			return -1;
		}
		if (z < z1) {
			return 0;
		}
		if (z < z3) {
			return 1;
		}
		return 2;
	}

	/**
	 * Get the perpendicular distance to the panel line segment
	 * 
	 * @param wp
	 *            the point in question
	 * @return the perpendicular distance
	 */
	public double pointToLineDistance(Point2D.Double wp) {
		return line.ptSegDist(wp);
	}

	/**
	 * Get the layer, [1..8]
	 * 
	 * @return the layer
	 */
	public int getLayer() {
		return layer;
	}

	/**
	 * the "sector" 1..max, max depends on layer, max = {10, 14, 18, 24} for
	 * layers {(1,2), (3,4), (5,6), (7,8)} respectively
	 * 
	 * @return the sector
	 */
	public int getSector() {
		return _sector;
	}

	/**
	 * Get the x1 value
	 * 
	 * @return the x1 value
	 */
	public double getX1() {
		return line.x1;
	}

	/**
	 * Get the y1 value
	 * 
	 * @return the y1 value
	 */
	public double getY1() {
		return line.y1;
	}

	/**
	 * Get the x2 value
	 * 
	 * @return the x2 value
	 */
	public double getX2() {
		return line.x2;
	}

	/**
	 * Get the y2 value
	 * 
	 * @return the y2 value
	 */
	public double getY2() {
		return line.y2;
	}

	/**
	 * Get the z0 value
	 * 
	 * @return the z0 value
	 */
	public double getZ0() {
		return z0;
	}

	/**
	 * Get the z1 value
	 * 
	 * @return the z1 value
	 */
	public double getZ1() {
		return z1;
	}

	/**
	 * Get the z2 value
	 * 
	 * @return the z2 value
	 */
	public double getZ2() {
		return z2;
	}

	/**
	 * Get the z3 value
	 * 
	 * @return the z3
	 */
	public double getZ3() {
		return z3;
	}

	/**
	 * Get the z3 value
	 * 
	 * @return the z4 value
	 */
	public double getZ4() {
		return z4;
	}

	/**
	 * Get the z5 value
	 * 
	 * @return the z5 value
	 */
	public double getZ5() {
		return z5;
	}

	/**
	 * Set the perp value used by the z view for ordering
	 * 
	 * @param perpVal
	 *            the new perp value
	 */
	public void setPerp(double perpVal) {
		perp = perpVal;
	}

	/**
	 * Get the perp value used by the z view for ordering
	 * 
	 * @return the perp value
	 */
	public double getPerp() {
		return perp;
	}

	/**
	 * Get the average lab xy of this panel
	 * 
	 * @return the average lab xy of this panel
	 */
	public Point2D.Double getXyAverage() {
		double x1 = getX1();
		double x2 = getX2();
		double y1 = getY1();
		double y2 = getY2();
		return new Point2D.Double((x1 + x2) / 2, (y1 + y2) / 2);
	}

	@Override
	public int compareTo(BSTxyPanel otherPanel) {
		return Double.compare(perp, otherPanel.perp);
	}
}
