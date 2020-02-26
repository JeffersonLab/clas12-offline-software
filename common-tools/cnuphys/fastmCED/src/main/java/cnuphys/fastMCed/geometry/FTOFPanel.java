package cnuphys.fastMCed.geometry;


import java.awt.geom.Point2D;

import org.jlab.geom.prim.Plane3D;

public class FTOFPanel {
	
	// ftof constants
	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;
	public static final String panelNames[] = { "Panel 1A", "Panel 1B",
			"Panel 2" };
	private static final String briefPNames[] = { "1A", "1B",
	"2" };


	private int _panelType; // 0,1,2 1A, 1B 2

	private int _numPaddle;

	public FTOFPanel(String pname, int numPaddle) {
		if (pname.contains("1a") || pname.contains("1A")) {
			_panelType = PANEL_1A; // 0
		} else if (pname.contains("1b") || pname.contains("1B")) {
			_panelType = PANEL_1B; // 1
		} else {
			_panelType = PANEL_2; // 2
		}
		_numPaddle = numPaddle;
	}

	/**
	 * Obtain the shell for the paddle.
	 * 
	 * @index the zero based index of the paddle
	 * @param projectionPlane
	 *            the projection plane
	 * @param wp holds the shell for the paddle in 2D world coordinates
	 * @return <code>true</code> if the paddle fully intersects the projection plane
	 */
	public boolean getPaddle(int index, Plane3D projectionPlane, Point2D.Double wp[]) {
		return FTOFGeometry.getIntersections(_panelType, index, projectionPlane, wp);
	}
	
	/**
	 * Does the paddle fully intersect the projection plane
	 * @index the zero based index of the paddle
	 * @param projectionPlane
	 *            the projection plane
	 * @return
	 */
	public boolean paddleFullyIntersects(int index, Plane3D projectionPlane) {
		return FTOFGeometry.doesProjectedPolyFullyIntersect(_panelType, index, projectionPlane);
	}

	/**
	 * Obtain the shell for the whole panel.
	 * 
	 * @param projectionPlane
	 *            the projection plane
	 * @return the shell for the whole panel.
	 */
	public Point2D.Double[] getShell(Plane3D projectionPlane) {
		Point2D.Double wp[] = GeometryManager.allocate(4);


		// get first and last panel
		Point2D.Double lastPP[] = GeometryManager.allocate(4);
		Point2D.Double firstPP[] = GeometryManager.allocate(4);
		
		int lastIndex = _numPaddle - 1;
		int firstIndex = 0;

		while (!getPaddle(lastIndex, projectionPlane, lastPP)) {
			lastIndex--;
		}
		
		while (!getPaddle(firstIndex, projectionPlane, firstPP)) {
			firstIndex++;
		}
		
		wp[0] = lastPP[1];
		wp[1] = firstPP[0];
		wp[2] = firstPP[3];
		wp[3] = lastPP[2];

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