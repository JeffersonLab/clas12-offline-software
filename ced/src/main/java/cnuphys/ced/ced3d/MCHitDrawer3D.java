package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.ADataContainer;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.event.data.ECDataContainer;
import com.jogamp.opengl.GLAutoDrawable;

import item3D.Item3D;

public class MCHitDrawer3D extends Item3D {

	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	private static final float POINTSIZE = 3f;

	public MCHitDrawer3D(Panel3D panel3d) {
		super(panel3d);
	}

	@Override
	public void draw(GLAutoDrawable drawable) {

		if (!showMCTruth()) {
			return;
		}

		if (_panel3D instanceof ForwardPanel3D) { // forward detectors
		// DCDataContainer dcData = _eventManager.getDCData();
		// FTOFDataContainer ftofData = _eventManager.getFTOFData();

			// has EC and PCAL data
			ECDataContainer ecData = _eventManager.getECData();

			// if (showDC()) {
			// showGemcXYZHits(drawable, dcData, dcData.dc_true_avgX,
			// dcData.dc_true_avgY, dcData.dc_true_avgZ,
			// dcData.dc_true_pid, 0);
			// }

			// if (showFTOF()) {
			// showGemcXYZHits(drawable, ftofData, ftofData.ftof1a_true_avgX,
			// ftofData.ftof1a_true_avgY, ftofData.ftof1a_true_avgZ,
			// ftofData.ftof1a_true_pid, 0);
			// showGemcXYZHits(drawable, ftofData, ftofData.ftof1b_true_avgX,
			// ftofData.ftof1b_true_avgY, ftofData.ftof1b_true_avgZ,
			// ftofData.ftof1b_true_pid, 0);
			// showGemcXYZHits(drawable, ftofData, ftofData.ftof2b_true_avgX,
			// ftofData.ftof2b_true_avgY, ftofData.ftof2b_true_avgZ,
			// ftofData.ftof2b_true_pid, 0);
			// }

			if (showEC()) {
				showGemcXYZHits(drawable, ecData, ecData.ec_true_avgX,
						ecData.ec_true_avgY, ecData.ec_true_avgZ,
						ecData.ec_true_pid, 0);
			}

			if (showPCAL()) {
				showGemcXYZHits(drawable, ecData, ecData.pcal_true_avgX,
						ecData.pcal_true_avgY, ecData.pcal_true_avgZ,
						ecData.pcal_true_pid, 0);
			}

		} else if (_panel3D instanceof CentralPanel3D) { // central detectors

			if (showSVT()) {
				BSTDataContainer bstData = _eventManager.getBSTData();
				showGemcXYZHits(drawable, bstData, bstData.bst_true_avgX,
						bstData.bst_true_avgY, bstData.bst_true_avgZ,
						bstData.bst_true_pid, 0);
			}

		}

	}

	// show DC
	private boolean showDC() {
		if (_panel3D instanceof ForwardPanel3D) {
			return ((ForwardPanel3D) _panel3D).show(CedPanel3D.SHOW_DC);
		}
		return false;
	}

	private boolean showEC() {
		if (_panel3D instanceof ForwardPanel3D) {
			return ((ForwardPanel3D) _panel3D).show(CedPanel3D.SHOW_EC);
		}
		return false;
	}

	private boolean showPCAL() {
		if (_panel3D instanceof ForwardPanel3D) {
			return ((ForwardPanel3D) _panel3D).show(CedPanel3D.SHOW_PCAL);
		}
		return false;
	}

	private boolean showFTOF() {
		if (_panel3D instanceof ForwardPanel3D) {
			return ((ForwardPanel3D) _panel3D).show(CedPanel3D.SHOW_FTOF);
		}
		return false;
	}

	private boolean showSVT() {
		if (_panel3D instanceof CentralPanel3D) {
			return ((CentralPanel3D) _panel3D).show(CedPanel3D.SHOW_FTOF);
		}
		return false;
	}

	// show MC Truth?
	protected boolean showMCTruth() {
		return ((CedPanel3D) _panel3D).show(CedPanel3D.SHOW_TRUTH);
	}

	// draw all the MC hits at once
	private void showGemcXYZHits(GLAutoDrawable drawable, ADataContainer data,
			double x[], double y[], double z[], int pid[], int option) {

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
			Support3D.drawPoint(drawable, xcm, ycm, zcm, Color.black,
					POINTSIZE + 2);
			Support3D.drawPoint(drawable, xcm, ycm, zcm, truthColor, POINTSIZE);

		}

	}

}
