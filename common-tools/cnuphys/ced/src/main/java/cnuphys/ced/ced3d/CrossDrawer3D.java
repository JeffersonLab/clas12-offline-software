package cnuphys.ced.ced3d;

import java.awt.Color;
import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.Cross;
import cnuphys.ced.event.data.CrossList;
import cnuphys.ced.event.data.HBCrosses;
import cnuphys.ced.event.data.TBCrosses;
import cnuphys.ced.frame.CedColors;
import item3D.Item3D;

public class CrossDrawer3D extends Item3D {

	protected static final float CROSS_LEN = 30f; // in cm
	protected static final double COS_TILT = Math.cos(Math.toRadians(25.));
	protected static final double SIN_TILT = Math.sin(Math.toRadians(25.));

	private CedPanel3D _cedPanel3D;

	public CrossDrawer3D(CedPanel3D panel3D) {
		super(panel3D);
		_cedPanel3D = panel3D;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (_cedPanel3D.showHBCross()) {
			CrossList list = HBCrosses.getInstance().getCrosses();
			drawCrossList(drawable, list, CedColors.HB_COLOR);
		}
		if (_cedPanel3D.showTBCross()) {
			CrossList list = TBCrosses.getInstance().getCrosses();
			drawCrossList(drawable, list, CedColors.TB_COLOR);
		}
		if (_cedPanel3D.showAIHBCross()) {
			CrossList list = HBCrosses.getInstance().getCrosses();
			drawCrossList(drawable, list, CedColors.AIHB_COLOR);
		}
		if (_cedPanel3D.showAITBCross()) {
			CrossList list = TBCrosses.getInstance().getCrosses();
			drawCrossList(drawable, list, CedColors.AITB_COLOR);
		}

	}

	// draw a cross list
	private void drawCrossList(GLAutoDrawable drawable, CrossList list, Color color) {
		if ((list == null) || list.isEmpty()) {
			return;
		}

		for (Cross cross : list) {
			if (cross != null) {
				drawCross(drawable, cross, color);
			}
		}
	}

	private void drawCross(GLAutoDrawable drawable, Cross cross, Color color) {

		float[] p3d0 = new float[3];
		float[] p3d1 = new float[3];

		tiltedToSector(cross.x, cross.y, cross.z, p3d0);
		float x = p3d0[0];
		float y = p3d0[1];
		float z = p3d0[2];

		float tx = cross.x + CROSS_LEN * cross.ux;
		float ty = cross.y + CROSS_LEN * cross.uy;
		float tz = cross.z + CROSS_LEN * cross.uz;
		tiltedToSector(tx, ty, tz, p3d1);

		Support3D.drawLine(drawable, x, y, z, p3d1[0], p3d1[1], p3d1[2], Color.black, 3f);
		Support3D.drawLine(drawable, x, y, z, p3d1[0], p3d1[1], p3d1[2], Color.gray, 1f);

		Support3D.drawPoint(drawable, x, y, z, Color.black, 13, true);
		Support3D.drawPoint(drawable, x, y, z, color, 11, true);

	}

	/**
	 * Convert tilted sector coordinates to sector coordinates. The two vectors can
	 * be the same in which case it is overwritten.
	 * 
	 * @param tiltedXYZ will hold the tilted coordinates
	 * @param sectorXYZ the sector coordinates
	 */
	public void tiltedToSector(float tiltx, float tilty, float tiltz, float[] sectorXYZ) {

		double sectx = tiltx * COS_TILT + tiltz * SIN_TILT;
		double secty = tilty;
		double sectz = -tiltx * SIN_TILT + tiltz * COS_TILT;

		sectorXYZ[0] = (float) sectx;
		sectorXYZ[1] = (float) secty;
		sectorXYZ[2] = (float) sectz;
	}

}
