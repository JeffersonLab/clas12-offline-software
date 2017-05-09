package cnuphys.ced.ced3d;

import java.awt.Color;
import bCNU3D.Support3D;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.event.data.Cosmic;
import cnuphys.ced.event.data.CosmicList;
import cnuphys.ced.event.data.Cosmics;
import cnuphys.ced.event.data.Cross2;
import cnuphys.ced.event.data.CrossList2;
import cnuphys.ced.event.data.SVTCrosses;
import cnuphys.ced.geometry.SVTGeometry;
import cnuphys.lund.X11Colors;

import com.jogamp.opengl.GLAutoDrawable;

public class BSTPanel3D extends DetectorItem3D {

	protected static final Color outlineHitColor = new Color(0, 255, 64, 24);

	protected static final float CROSS_LEN = 3f; // in cm
	protected static final Color crossColor = X11Colors
			.getX11Color("dark orange");

	// the 1=based sect
	private int _sector;

	// the 1-based "biglayer" [1..8] used by the data
	private int _layer;

	public BSTPanel3D(CedPanel3D panel3D, int sector, int layer) {
		super(panel3D);
		_sector = sector;
		_layer = layer;
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		float coords[] = new float[36];

		SVTGeometry.getLayerQuads(_sector, _layer, coords);

		Color color = ((_layer % 2) == 0) ? X11Colors.getX11Color("coral", getVolumeAlpha())
			: X11Colors.getX11Color("Powder Blue", getVolumeAlpha());
		Support3D.drawQuads(drawable, coords, color, 1f, true);
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {

		int hitCount = BST.hitCount();
		float coords6[] = new float[6];
		float coords36[] = new float[36];
		int bstsector[] = BST.sector();
		int bstlayer[] = BST.layer();
		int bststrip[] = BST.strip();
        int pid[] = BST.pid();
		double avgX[] = BST.avgX();
		double avgY[] = BST.avgY();
		double avgZ[] = BST.avgZ();

		
		boolean drawOutline = false;
		// now strips
		for (int i = 0; i < hitCount; i++) {
			// 1 based sector
			int sector = bstsector[i];
			// "big layer" ..8
			int layer = bstlayer[i];

			if ((_sector == sector) && (_layer == layer)) {
				drawOutline = true;
				// strip 1..256
				int strip = bststrip[i];

				SVTGeometry.getStrip(sector, layer, strip, coords6);

				if (_cedPanel3D.showMCTruth() && (pid != null)) {
					Color color = truthColor(pid, i);

					if (showHits()) {
						Support3D.drawLine(drawable, coords6, color, 2f);
					}
					double xcm = avgX[i] / 10;
					double ycm = avgY[i] / 10;
					double zcm = avgZ[i] / 10;

					drawMCPoint(drawable, xcm, ycm, zcm, color);

				} // mc truth
				else {
					if (showHits()) {
						Support3D.drawLine(drawable, coords6, dgtzColor, 2f);
					}
				}
			}
		} // hitcount

		if (drawOutline) { // if any hits, draw it once
			SVTGeometry.getLayerQuads(_sector, _layer, coords36);
			Support3D.drawQuads(drawable, coords36, outlineHitColor, 1f, true);
		}
		

		// reconstructed crosses?
		if (_cedPanel3D.showReconCrosses()) {
			CrossList2 crosses = SVTCrosses.getInstance().getCrosses();
			int len = (crosses == null) ? 0 : crosses.size();
			for (int i = 0; i < len; i++) {
				Cross2 cross = crosses.elementAt(i);
				if (cross != null) {
					//convert to cm
					float x1 = cross.x / 10;
					float y1 = cross.y / 10;
					float z1 = cross.z / 10;

					Support3D.drawLine(drawable, x1, y1, z1, cross.ux,
							cross.uy, cross.uz, CROSS_LEN,
							crossColor, 3f);
					Support3D.drawLine(drawable, x1, y1, z1, cross.ux,
							cross.uy, cross.uz,
							(float) (1.1 * CROSS_LEN), Color.black, 1f);

					drawCrossPoint(drawable, x1, y1, z1, crossColor);
				}
			}
		}
		
		//cosmics?
		if (_cedPanel3D.showCosmics()) {
			CosmicList cosmics;
			cosmics = Cosmics.getInstance().getCosmics();
			
			if ((cosmics != null) && !cosmics.isEmpty()) {
				for (Cosmic cosmic : cosmics) {
					float y1 = 1000;
					float y2 = -1000;
					float x1 = cosmic.trkline_yx_slope * y1 + cosmic.trkline_yx_interc;
					float x2 = cosmic.trkline_yx_slope * y2 + cosmic.trkline_yx_interc;
					float z1 = cosmic.trkline_yz_slope * y1 + cosmic.trkline_yz_interc;
					float z2 = cosmic.trkline_yz_slope * y2 + cosmic.trkline_yz_interc;
					
					x1 /= 10;
					x2 /= 10;
					y1 /= 10;
					y2 /= 10;
					z1 /= 10;
					z2 /= 10;
					
					Support3D.drawLine(drawable, x1, y1, z1, x2, y2, z2, Color.red, 1f);

				}
			}

		}
		
//		
//		
//		
//		double labx[] = BST.crossX();
//		if (_cedPanel3D.showReconCrosses() && (labx != null)) {
//			// these arrays are in mm
//			double laby[] = BST.crossY();
//			double labz[] = BST.crossZ();
//			double ux[] = BST.crossUx();
//			double uy[] = BST.crossUy();
//			double uz[] = BST.crossUz();
//
//			int len = (labx == null) ? 0 : labx.length;
//
//			for (int i = 0; i < len; i++) {
//				// convert to cm
//				float x1 = (float) labx[i] / 10;
//				float y1 = (float) laby[i] / 10;
//				float z1 = (float) labz[i] / 10;
//
//				Support3D.drawLine(drawable, x1, y1, z1, (float) (ux[i]),
//						(float) (uy[i]), (float) (uz[i]), CROSS_LEN,
//						crossColor, 3f);
//				Support3D.drawLine(drawable, x1, y1, z1, (float) (ux[i]),
//						(float) (uy[i]), (float) (uz[i]),
//						(float) (1.1 * CROSS_LEN), Color.black, 1f);
//
//				drawCrossPoint(drawable, x1, y1, z1, crossColor);
//
//				// float x2 = (float) (x1 + CROSS_LEN * ux[i]);
//				// float y2 = (float) (y1 + CROSS_LEN * uy[i]);
//				// float z2 = (float) (z1 + CROSS_LEN * uz[i]);
//				//
//				// // System.err.println("BST crosses at (" + x1 + ", " + y1 +
//				// ", " + z1 + ")");
//				//
//				// Support3D.drawCone(drawable, x1, y1, z1, x2, y2, z2, 1.0f,
//				// coneColor);
//				//
//				// x2 = (float) (x1 + 1.1*CROSS_LEN * ux[i]);
//				// y2 = (float) (y1 + 1.1*CROSS_LEN * uy[i]);
//				// z2 = (float) (z1 + 1.1*CROSS_LEN * uz[i]);
//				//
//				// Support3D.drawLine(drawable, x1, y1, z1,
//				// x2, y2, z2, coneLineColor, 2f);
//			}

//		}
		
		
		
		//cosmics

	}

	// show SVT?
	@Override
	protected boolean show() {
		switch (_layer) {
		case 1:
			return _cedPanel3D.showBSTLayer1();
		case 2:
			return _cedPanel3D.showBSTLayer2();
		case 3:
			return _cedPanel3D.showBSTLayer3();
		case 4:
			return _cedPanel3D.showBSTLayer4();
		case 5:
			return _cedPanel3D.showBSTLayer5();
		case 6:
			return _cedPanel3D.showBSTLayer6();
		case 7:
			return _cedPanel3D.showBSTLayer7();
		case 8:
			return _cedPanel3D.showBSTLayer8();
		}
		return false;
	}

	// show strip hits?
	protected boolean showHits() {
		return show() && _cedPanel3D.showBSTHits();
	}

}
