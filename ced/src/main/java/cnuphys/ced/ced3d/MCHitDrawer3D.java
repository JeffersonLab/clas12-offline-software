package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.EC;
import cnuphys.ced.event.data.PCAL;

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


			// if (showDC()) {
			// }

			if (showFTOF()) {
				showGemcXYZHits(drawable, 
						ColumnData.getDoubleArray("FTOF1A::true.avgX"),
						ColumnData.getDoubleArray("FTOF1A::true.avgY"), 
						ColumnData.getDoubleArray("FTOF1A::true.avgZ"),
						ColumnData.getIntArray("FTOF1A::true.pid"), 
						0);
				showGemcXYZHits(drawable, 
						ColumnData.getDoubleArray("FTOF1B::true.avgX"),
						ColumnData.getDoubleArray("FTOF1B::true.avgY"), 
						ColumnData.getDoubleArray("FTOF1B::true.avgZ"),
						ColumnData.getIntArray("FTOF1B::true.pid"), 
						0);
				showGemcXYZHits(drawable, 
						ColumnData.getDoubleArray("FTOF2B::true.avgX"),
						ColumnData.getDoubleArray("FTOF2B::true.avgY"), 
						ColumnData.getDoubleArray("FTOF2B::true.avgZ"),
						ColumnData.getIntArray("FTOF2B::true.pid"), 
						0);
			}

			if (showEC()) {
				showGemcXYZHits(drawable, EC.avgX(),
						EC.avgY(), EC.avgZ(),
						EC.pid(), 0);
			}

			if (showPCAL()) {
				showGemcXYZHits(drawable, PCAL.avgX(),
						PCAL.avgY(), PCAL.avgZ(),
						PCAL.pid(), 0);
			}

		} else if (_panel3D instanceof CentralPanel3D) { // central detectors

			if (showSVT()) {
				showGemcXYZHits(drawable, 
						BST.avgX(),
						BST.avgY(),
						BST.avgZ(),
						BST.pid(), 0);
			}

		}

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
	private void showGemcXYZHits(GLAutoDrawable drawable,
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
					POINTSIZE + 2, true);
			Support3D.drawPoint(drawable, xcm, ycm, zcm, truthColor, POINTSIZE, true);

		}

	}

}
