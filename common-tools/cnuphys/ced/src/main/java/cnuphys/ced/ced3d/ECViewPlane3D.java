package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Support3D;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.ECGeometry;
import cnuphys.lund.X11Colors;

import com.jogamp.opengl.GLAutoDrawable;

public class ECViewPlane3D extends DetectorItem3D {

	// sector is 1..6
	private final int _sector;

	// [1,2] for inner/outer like geometry "superlayer"
	private final int _stack;

	// [1, 2, 3] for [u, v, w] like geometry "layer+1"
	private final int _view;

	// the triangle coordinates
	private float _coords[];

	public ECViewPlane3D(CedPanel3D panel3D, int sector, int stack, int view) {
		super(panel3D);
		_sector = sector;
		_stack = stack;
		_view = view;
		_coords = new float[9];
		ECGeometry.getViewTriangle(sector, stack, view, _coords);
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {

		Color color = null;
		if (_stack == 1) {
			color = X11Colors.getX11Color("tan", getVolumeAlpha());
		} else {
			color = X11Colors.getX11Color("Light Green", getVolumeAlpha());
		}

		Support3D.drawTriangles(drawable, _coords, color, 1f, true);

		// float coords[] = new float[24];
		// for (int strip = 1; strip <= 36; strip++) {
		// ECGeometry.getStrip(_sector, _stack, _view, strip, coords);
		// drawStrip(drawable, outlineColor, coords);
		// }
	}

	// draw a single strip
	private void drawStrip(GLAutoDrawable drawable, Color color, float coords[]) {

		boolean frame = true;
		Support3D.drawQuad(drawable, coords, 0, 1, 2, 3, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 3, 7, 6, 2, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 0, 4, 7, 3, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 0, 4, 5, 1, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 1, 5, 6, 2, color, 1f, frame);
		Support3D.drawQuad(drawable, coords, 4, 5, 6, 7, color, 1f, frame);
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		
		TdcAdcHitList hits = AllEC.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			
			float coords[] = new float[24];

			for (TdcAdcHit hit : hits) {
				if (hit != null) {
					try {
						if (hit.layer > 3) {
							int layer = hit.layer - 4; // 0..5

							int stack = 1 + layer / 3; // 111/222 for inner outer
							int view = 1 + (layer % 3); // 123123 for uvwuvw

							if ((_sector == hit.sector) && (_view == view) && (_stack == stack)) {
								int strip = hit.component;

								Color color = hits.adcColor(hit, AllEC.getInstance().getMaxECALAdc());

								ECGeometry.getStrip(_sector, _stack, _view, strip, coords);
								drawStrip(drawable, color, coords);
							}
						}
					}
					catch (Exception e) {
						Log.getInstance().exception(e);
					}
				} // hit not null
			} //hit loop
		} // have hits
		

		
		
//
//		int hitCount = ECAL.hitCount();
//		
//		if (hitCount > 0) {
//			int sector[] = ECAL.sector();
//			int stack[] = ECAL.stack();
//			int view[] = ECAL.view();
//			int strip[] = ECAL.strip();
//			int pid[] = ECAL.pid();
//			double avgX[] = ECAL.avgX();
//			double avgY[] = ECAL.avgY();
//			double avgZ[] = ECAL.avgZ();
//			
//			float coords[] = new float[24];
//			for (int i = 0; i < hitCount; i++) {
//				if ((_sector == sector[i]) && (_stack == stack[i])
//						&& (_view == view[i])) {
//					ECGeometry.getStrip(_sector, _stack, _view, strip[i],
//							coords);
//					if (_cedPanel3D.showMCTruth() && (pid != null)) {
//						Color color = truthColor(pid, i);
//						drawStrip(drawable, color, coords);
//						double xcm = avgX[i] / 10;
//						double ycm = avgY[i] / 10;
//						double zcm = avgZ[i] / 10;
//						drawMCPoint(drawable, xcm, ycm, zcm, color);
//
//					} else {
//						drawStrip(drawable, dgtzColor, coords);
//					}
//				}
//			} //end for loop
//
//		} //hitCount > 0
//

	}

	// show ECs?
	@Override
	protected boolean show() {
		boolean showec = _cedPanel3D.showECAL();
		return showec && _cedPanel3D.showSector(_sector);
	}

}
