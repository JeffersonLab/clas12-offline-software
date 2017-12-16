package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Support3D;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.event.data.AllEC;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.PCALGeometry;

import com.jogamp.opengl.GLAutoDrawable;

public class PCALViewPlane3D extends DetectorItem3D {

	// sector is 1..6
	private final int _sector;

	// [1, 2, 3] for [u, v, w] like geometry "layer+1"
	private final int _view;

	// the triangle coordinates
	private float _coords[];

	public PCALViewPlane3D(CedPanel3D panel3D, int sector, int view) {
		super(panel3D);
		_sector = sector;
		_view = view;
		_coords = new float[9];
		PCALGeometry.getViewTriangle(sector, view, _coords);
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {

		Color outlineColor = new Color(32, 200, 64, getVolumeAlpha());
		Support3D.drawTriangles(drawable, _coords, outlineColor, 1f, true);

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
						if (hit.layer < 4) {

							int view = hit.layer; // 123 for uvwuvw

							if ((_sector == hit.sector) && (_view == view)) {
								int strip = hit.component;

								Color color = hits.adcColor(hit, AllEC.getInstance().getMaxPCALAdc());

								PCALGeometry.getStrip(_sector, _view, strip, coords);
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


//		int hitCount = PCAL.hitCount();
//		
//		if (hitCount > 0) {
//			int sector[] = PCAL.sector();
//			int view[] = PCAL.view();
//			int strip[] = PCAL.strip();
//			int pid[] = PCAL.pid();
//			double avgX[] = PCAL.avgX();
//			double avgY[] = PCAL.avgY();
//			double avgZ[] = PCAL.avgZ();
//			
//			float coords[] = new float[24];
//			for (int i = 0; i < hitCount; i++) {
//				if ((_sector == sector[i]) && (_view == view[i])) {
//					PCALGeometry.getStrip(_sector, _view, strip[i],
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
	}

	// show PCALs?
	@Override
	protected boolean show() {
		boolean showpcal = _cedPanel3D.showPCAL();
		return showpcal && _cedPanel3D.showSector(_sector);
	}

}
