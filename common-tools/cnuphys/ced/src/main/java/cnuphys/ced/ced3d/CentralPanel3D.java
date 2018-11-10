package cnuphys.ced.ced3d;

import item3D.Axes3D;

import java.awt.Color;
import java.awt.Font;

import cnuphys.ced.ced3d.view.CedView3D;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.lund.X11Colors;

public class CentralPanel3D extends CedPanel3D {

	// dimension of this panel are in cm
	private final float xymax = 50f;
	private final float zmax = 50f;
	private final float zmin = -50f;

	// labels for the check box
	private static final String _cbaLabels[] = { SHOW_VOLUMES, SHOW_TRUTH, SHOW_BST, SHOW_BST_LAYER_1, SHOW_BST_LAYER_2,
			SHOW_BST_LAYER_3, SHOW_BST_LAYER_4, SHOW_BST_LAYER_5, SHOW_BST_LAYER_6, SHOW_BST_LAYER_7, SHOW_BST_LAYER_8,
			SHOW_BST_HITS, SHOW_BMT, SHOW_BMT_LAYER_1, SHOW_BMT_LAYER_2, SHOW_BMT_LAYER_3, SHOW_BMT_LAYER_4,
			SHOW_BMT_LAYER_5, SHOW_BMT_LAYER_6, SHOW_BMT_HITS, SHOW_CTOF, SHOW_CND, SHOW_CND_LAYER_1, SHOW_CND_LAYER_2,
			SHOW_CND_LAYER_3, SHOW_RECON_CROSSES, SHOW_TB_TRACK, SHOW_HB_TRACK, SHOW_CVT_TRACK, SHOW_COSMIC };

	/**
	 * 
	 * @param view
	 * @param angleX
	 * @param angleY
	 * @param angleZ
	 * @param xDist
	 * @param yDist
	 * @param zDist
	 */
	public CentralPanel3D(CedView3D view, float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist) {
		super(view, angleX, angleY, angleZ, xDist, yDist, zDist, _cbaLabels);
	}

	@Override
	public void createInitialItems() {
		// coordinate axes
		Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax, zmin, zmax, null, Color.darkGray, 1f, 6, 6, 6,
				Color.black, X11Colors.getX11Color("Dark Green"), new Font("SansSerif", Font.PLAIN, 12), 0);
		addItem(axes);

		// trajectory drawer
		TrajectoryDrawer3D trajDrawer = new TrajectoryDrawer3D(this);
		addItem(trajDrawer);

		// mc hit drawer
		// MCHitDrawer3D mchd = new MCHitDrawer3D(this);
		// addItem(mchd);

		// svt panels
		for (int layer = 1; layer <= 8; layer++) {
			// geom service uses 0-based superlayer [0,1,2,3] and layer [0,1]
			int supl = ((layer - 1) / 2); // 0, 1, 2, 3
			for (int sector = 1; sector <= BSTGeometry.sectorsPerSuperlayer[supl]; sector++) {
				BSTPanel3D svt = new BSTPanel3D(this, sector, layer);
				addItem(svt);
			}
		}

		// BMT
		for (int sector = 1; sector <= 3; sector++) {
			for (int layer = 1; layer <= 6; layer++) {
				BMTLayer3D bmt = new BMTLayer3D(this, sector, layer);
				addItem(bmt);
			}
		}

		// cnd
		addItem(new CND3D(this));

		// ctof
		addItem(new CTOF3D(this));

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
