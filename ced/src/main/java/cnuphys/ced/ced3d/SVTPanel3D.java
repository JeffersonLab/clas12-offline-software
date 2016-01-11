package cnuphys.ced.ced3d;

import java.awt.Color;
import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.geometry.BSTGeometry;
import cnuphys.lund.X11Colors;

import com.jogamp.opengl.GLAutoDrawable;

public class SVTPanel3D extends DetectorItem3D {

	protected static final Color outlineHitColor = new Color(0, 255, 64, 24);

	protected static final float CROSS_LEN = 3f; // in cm
	protected static final Color crossColor = X11Colors
			.getX11Color("dark orange");

	// the 1=based sect
	private int _sector;

	// the 1-based "biglayer" [1..8] used by the data
	private int _layer;

	public SVTPanel3D(Panel3D panel3d, int sector, int layer) {
		super(panel3d);
		_sector = sector;
		_layer = layer;
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		float coords[] = new float[36];

		BSTGeometry.getLayerQuads(_sector, _layer, coords);

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

				BSTGeometry.getStrip(sector, layer, strip, coords6);

				if (showMCTruth() && (pid != null)) {
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
			BSTGeometry.getLayerQuads(_sector, _layer, coords36);
			Support3D.drawQuads(drawable, coords36, outlineHitColor, 1f, true);
		}

		// cosmics?
		int ids[] = BST.cosmicID();
		if (showCosmics() && (ids != null)) {
			double yx_interc[] = BST.cosmicYxInterc();
			double yx_slope[] = BST.cosmicYxSlope();
			double yz_interc[] = BST.cosmicYzInterc();
			double yz_slope[] = BST.cosmicYzSlope();

			for (int i = 0; i < ids.length; i++) {
				double y1 = 2000;
				double y2 = -2000;
				// note conversion mm to cm
				double yx_int = yx_interc[i] / 10;
				double yz_int = yz_interc[i] / 10;
				double x1 = yx_slope[i] * y1 + yx_int;
				double x2 = yx_slope[i] * y2 + yx_int;
				double z1 = yz_slope[i] * y1 + yz_int;
				double z2 = yz_slope[i] * y2 + yz_int;
				Support3D.drawLine(drawable, (float) x1, (float) y1,
						(float) z1, (float) x2, (float) y2, (float) z2,
						cosmicColor, 1.5f);

			}
		}

		// reconstructed crosses?
		double labx[] = BST.crossX();
		if (showCrosses() && (labx != null)) {
			// these arrays are in mm
			double laby[] = BST.crossY();
			double labz[] = BST.crossZ();
			double ux[] = BST.crossUx();
			double uy[] = BST.crossUy();
			double uz[] = BST.crossUz();

			int len = (labx == null) ? 0 : labx.length;

			for (int i = 0; i < len; i++) {
				// convert to cm
				float x1 = (float) labx[i] / 10;
				float y1 = (float) laby[i] / 10;
				float z1 = (float) labz[i] / 10;

				Support3D.drawLine(drawable, x1, y1, z1, (float) (ux[i]),
						(float) (uy[i]), (float) (uz[i]), CROSS_LEN,
						crossColor, 3f);
				Support3D.drawLine(drawable, x1, y1, z1, (float) (ux[i]),
						(float) (uy[i]), (float) (uz[i]),
						(float) (1.1 * CROSS_LEN), Color.black, 1f);

				drawCrossPoint(drawable, x1, y1, z1, crossColor);

				// float x2 = (float) (x1 + CROSS_LEN * ux[i]);
				// float y2 = (float) (y1 + CROSS_LEN * uy[i]);
				// float z2 = (float) (z1 + CROSS_LEN * uz[i]);
				//
				// // System.err.println("BST crosses at (" + x1 + ", " + y1 +
				// ", " + z1 + ")");
				//
				// Support3D.drawCone(drawable, x1, y1, z1, x2, y2, z2, 1.0f,
				// coneColor);
				//
				// x2 = (float) (x1 + 1.1*CROSS_LEN * ux[i]);
				// y2 = (float) (y1 + 1.1*CROSS_LEN * uy[i]);
				// z2 = (float) (z1 + 1.1*CROSS_LEN * uz[i]);
				//
				// Support3D.drawLine(drawable, x1, y1, z1,
				// x2, y2, z2, coneLineColor, 2f);
			}

		}

	}

	// show SVT?
	@Override
	protected boolean show() {
		boolean showme = ((CentralPanel3D) _panel3D).show(CedPanel3D.SHOW_SVT);

		if (showme) {
			switch (_layer) {
			case 1:
				showme = ((CentralPanel3D) _panel3D)
						.show(CedPanel3D.SHOW_SVT_LAYER_1);
				break;
			case 2:
				showme = ((CentralPanel3D) _panel3D)
						.show(CedPanel3D.SHOW_SVT_LAYER_2);
				break;
			case 3:
				showme = ((CentralPanel3D) _panel3D)
						.show(CedPanel3D.SHOW_SVT_LAYER_3);
				break;
			case 4:
				showme = ((CentralPanel3D) _panel3D)
						.show(CedPanel3D.SHOW_SVT_LAYER_4);
				break;
			case 5:
				showme = ((CentralPanel3D) _panel3D)
						.show(CedPanel3D.SHOW_SVT_LAYER_5);
				break;
			case 6:
				showme = ((CentralPanel3D) _panel3D)
						.show(CedPanel3D.SHOW_SVT_LAYER_6);
				break;
			case 7:
				showme = ((CentralPanel3D) _panel3D)
						.show(CedPanel3D.SHOW_SVT_LAYER_7);
				break;
			case 8:
				showme = ((CentralPanel3D) _panel3D)
						.show(CedPanel3D.SHOW_SVT_LAYER_8);
				break;
			}
		}

		return showme;
	}

	// show strip hits?
	protected boolean showHits() {
		return ((CentralPanel3D) _panel3D).show(CedPanel3D.SHOW_SVT_HITS);
	}

}
