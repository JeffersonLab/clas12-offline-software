package cnuphys.ced.ced3d;

import java.awt.Color;
import java.awt.Font;

import cnuphys.ced.ced3d.view.CedView3D;
import cnuphys.ced.geometry.FTCALGeometry;
import cnuphys.lund.X11Colors;
import item3D.Axes3D;

public class FTCalPanel3D extends CedPanel3D {

	// dimension of this panel are in cm
	// private final float xymax = 20f;
	// private final float zmax = 215f;
	// private final float zmin = 0f;
	// dimension of this panel are in cm
	private final float xymax = 50f;
	private final float zmax = 50f;
	private final float zmin = -50f;

	// labels for the check box
	private static final String _cbaLabels[] = { SHOW_VOLUMES, SHOW_TRUTH};

	public FTCalPanel3D(CedView3D view, float angleX, float angleY, float angleZ, float xDist,
			float yDist, float zDist) {
		super(view, angleX, angleY, angleZ, xDist, yDist, zDist, _cbaLabels);
	}

	@Override
	public void createInitialItems() {
		// coordinate axes
		Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax, zmin,
				zmax, null, FTCALGeometry.FTCAL_Z0, Color.darkGray, 1f, 6, 6, 6,
				Color.black, X11Colors.getX11Color("Dark Green"), new Font(
						"SansSerif", Font.PLAIN, 12), 0);
		addItem(axes);

		// trajectory drawer
		TrajectoryDrawer3D trajDrawer = new TrajectoryDrawer3D(this);
		addItem(trajDrawer);

		for (int id : FTCALGeometry.getGoodIds()) {
			FTCalPaddle3D paddle = new FTCalPaddle3D(this, id);
			addItem(paddle);
		}

	}
	

	/**
	 * This gets the z step used by the mouse and key adapters, to see how fast
	 * we move in or in in response to mouse wheel or up/down arrows. It should
	 * be overridden to give something sensible. like the scale/100;
	 * 
	 * @return the z step (changes to zDist) for moving in and out
	 */
	@Override
	public float getZStep() {
		return (zmax - zmin) / 50f;
	}

}
