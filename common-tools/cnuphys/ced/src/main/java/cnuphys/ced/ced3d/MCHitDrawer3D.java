package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Support3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.ECAL;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.PCAL;

import com.jogamp.opengl.GLAutoDrawable;

import item3D.Item3D;

public class MCHitDrawer3D extends Item3D {

	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	private static final float POINTSIZE = 3f;
	private CedPanel3D _cedPanel3D;

	public MCHitDrawer3D(CedPanel3D panel3D) {
		super(panel3D);
		_cedPanel3D = panel3D;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {

		if (!_cedPanel3D.showMCTruth()) {
			return;
		}

		if (_panel3D instanceof ForwardPanel3D) { // forward detectors

			// if (showDC()) {
			// }

			if (_cedPanel3D.showFTOF()) {
				showGemcXYZHits(drawable, FTOF.getInstance().avgX(FTOF.PANEL_1A),
						FTOF.getInstance().avgY(FTOF.PANEL_1A), FTOF.getInstance().avgZ(FTOF.PANEL_1A), null, 0);
				showGemcXYZHits(drawable, FTOF.getInstance().avgX(FTOF.PANEL_1B),
						FTOF.getInstance().avgY(FTOF.PANEL_1B), FTOF.getInstance().avgZ(FTOF.PANEL_1B), null, 1);
				showGemcXYZHits(drawable, FTOF.getInstance().avgX(FTOF.PANEL_2), FTOF.getInstance().avgY(FTOF.PANEL_2),
						FTOF.getInstance().avgZ(FTOF.PANEL_2), null, 2);
			}

			if (_cedPanel3D.showECAL()) {
				showGemcXYZHits(drawable, ECAL.avgX(), ECAL.avgY(), ECAL.avgZ(), ECAL.pid(), 0);
			}

			if (_cedPanel3D.showPCAL()) {
				showGemcXYZHits(drawable, PCAL.avgX(), PCAL.avgY(), PCAL.avgZ(), PCAL.pid(), 0);
			}

		}
		else if (_panel3D instanceof CentralPanel3D) { // central detectors

		}

	}

	// draw all the MC hits at once
	private void showGemcXYZHits(GLAutoDrawable drawable, double x[], double y[], double z[], int pid[], int option) {

		if ((x == null) || (y == null) || (z == null) || (x.length < 1)) {
			return;
		}

		// should not be necessary but be safe
		int len = x.length;
		len = Math.min(len, y.length);
		len = Math.min(len, z.length);

		if (len < 1) {
			return;
		}

		for (int hitIndex = 0; hitIndex < len; hitIndex++) {
			Color truthColor = DetectorItem3D.truthColor(pid, hitIndex);
			float xcm = (float) (x[hitIndex] / 10); // convert mm to cm
			float ycm = (float) (y[hitIndex] / 10); // convert mm to cm
			float zcm = (float) (z[hitIndex] / 10); // convert mm to cm
			Support3D.drawPoint(drawable, xcm, ycm, zcm, Color.black, POINTSIZE + 2, true);
			Support3D.drawPoint(drawable, xcm, ycm, zcm, truthColor, POINTSIZE, true);

		}

	}

}
