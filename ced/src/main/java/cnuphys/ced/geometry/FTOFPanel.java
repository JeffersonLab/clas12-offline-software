package cnuphys.ced.geometry;

import java.awt.geom.Point2D;

import org.jlab.geom.prim.Transformation3D;

import cnuphys.ced.event.data.FTOF;

public class FTOFPanel {

	private int _panelType; // 0,1,2 1A, 1B 2

	private int _numPaddle;

	public FTOFPanel(String pname, int numPaddle) {
		if (pname.contains("1a") || pname.contains("1A")) {
			_panelType = FTOF.PANEL_1A; // 0
		} else if (pname.contains("1b") || pname.contains("1B")) {
			_panelType = FTOF.PANEL_1B; // 1
		} else {
			_panelType = FTOF.PANEL_2; // 2
		}
		_numPaddle = numPaddle;
	}

	/**
	 * Get the edge that connects P3 to P0
	 * 
	 * @param index
	 *            zero-based paddle index
	 * @param t3d
	 *            the transformation to constant phi plane
	 * @param wp
	 *            should be able to hold two already instantiated points
	 * @return <code>false</code> if there is no intersection
	 */
	public boolean getP0P3Edge(int index, Transformation3D t3d,
			Point2D.Double wp[]) {

		Point2D.Double p[] = FTOFGeometry.getIntersections(_panelType, index,
				t3d);
		if (p == null) {
			return false;
		}
		wp[0] = p[0];
		wp[1] = p[3];
		return true;
	}

	/**
	 * Obtain the shell for the paddle.
	 * 
	 * @index the zero based index of the paddle
	 * @param t3d
	 *            the transformation to constant phi plane
	 * @return the shell for the paddle, or null if no intersection.
	 */
	public Point2D.Double[] getPaddle(int index, Transformation3D t3d) {
		return FTOFGeometry.getIntersections(_panelType, index, t3d);
	}

	/**
	 * Obtain the shell for the whole panel.
	 * 
	 * @param t3d
	 *            the transformation to constant phi plane
	 * @return the shell for the whole panel.
	 */
	public Point2D.Double[] getShell(Transformation3D t3d) {
		Point2D.Double wp[] = new Point2D.Double[4];
		for (int i = 0; i < 4; i++) {
			wp[i] = new Point2D.Double();
		}

		int lastIndex = _numPaddle - 1;

		// get last visible (intersecting) panel
		Point2D.Double lastPP[] = null;
		while ((lastPP == null) && (lastIndex >= 0)) {
			lastPP = getPaddle(lastIndex, t3d);
			if (lastPP == null) {
				lastIndex--;
			}
		}
		if (lastPP == null) {
			return null;
		}

		// get the first visible (intersecting) paddle
		// if we are here, we'll find one, even if it is
		// the same as the last
		Point2D.Double firstPP[] = null;
		int firstIndex = 0;
		while (firstPP == null) {
			firstPP = getPaddle(firstIndex, t3d);
			if (firstPP == null) {
				firstIndex++;
			}
		}

		wp[0] = lastPP[0];
		wp[1] = firstPP[1];
		wp[2] = firstPP[2];
		wp[3] = lastPP[3];

		return wp;
	}

	/**
	 * Get the number of paddles.
	 * 
	 * @return the number of paddles.
	 */
	public int getCount() {
		return _numPaddle;
	}

	/**
	 * Get the panel type
	 * 
	 * @return the panel type, one of the constants in FTOFDataContainer
	 */
	public int getPanelType() {
		return _panelType;
	}

}
