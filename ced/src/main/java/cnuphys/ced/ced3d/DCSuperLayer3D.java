package cnuphys.ced.ced3d;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import org.jlab.geom.DetectorHit;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCTdcHit;
import cnuphys.ced.event.data.DCTdcHitList;
import cnuphys.ced.fastmc.FastMCManager;
import cnuphys.ced.fastmc.ParticleHits;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.lund.X11Colors;

import com.jogamp.opengl.GLAutoDrawable;

public class DCSuperLayer3D extends DetectorItem3D {

	protected static final Color docaColor = new Color(255, 0, 0, 64);

	private static final boolean frame = true;

	// one based sector [1..6]
	private final int _sector;

	// one based superlayer [1..6]
	private final int _superLayer;

	// the vertices
	private float[] coords = new float[18];

	/**
	 * The owner panel
	 * 
	 * @param panel3d
	 * @param sector
	 *            one based sector [1..6]
	 * @param superLayer
	 *            one based superlayer [1..6]
	 */
	public DCSuperLayer3D(Panel3D panel3d, int sector, int superLayer) {
		super(panel3d);
		_sector = sector;
		_superLayer = superLayer;
		DCGeometry.superLayerVertices(_sector, _superLayer, coords);
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {

		Color outlineColor = X11Colors.getX11Color("wheat", getVolumeAlpha());

		Support3D.drawTriangle(drawable, coords, 0, 1, 2, outlineColor, 1f,
				frame);
		Support3D.drawQuad(drawable, coords, 1, 4, 3, 0, outlineColor, 1f,
				frame);
		Support3D.drawQuad(drawable, coords, 0, 3, 5, 2, outlineColor, 1f,
				frame);
		Support3D.drawQuad(drawable, coords, 1, 4, 5, 2, outlineColor, 1f,
				frame);
		Support3D.drawTriangle(drawable, coords, 3, 4, 5, outlineColor, 1f,
				frame);

	}
	
	private void fastMCDraw(GLAutoDrawable drawable) {
		if (FastMCManager.getInstance().isStreaming()) {
			return;
		}

		Vector<ParticleHits> phits = FastMCManager.getInstance().getFastMCHits();
		if ((phits == null) || phits.isEmpty()) {
			return;
		}
		
		float coords[] = new float[6];

		for (ParticleHits hits : phits) {
			List<DetectorHit> dchits = hits.getDCHits();
			if (dchits != null) {
				for (DetectorHit hit : dchits) {
					int sect1 = hit.getSectorId() + 1;
					int supl1 = hit.getSuperlayerId() + 1;
					if ((sect1 == _sector) && (supl1 == _superLayer)) {
						int lay1 = hit.getLayerId() + 1;
						int wire1 = hit.getComponentId() + 1;
						getWire(lay1, wire1, coords);
						int pid = hits.lundId();
						Color color = truthColor(pid);
						Support3D.drawLine(drawable, coords, color, 2f);
					}
				}
			}
		}
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {

		//reroute for fast MC
		if (_eventManager.isSourceFastMC()) {
			fastMCDraw(drawable);
			return;
		}

		
		float coords[] = new float[6];
		
		DCTdcHitList hits = DC.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			for (DCTdcHit hit : hits) {
				if ((hit.sector == _sector) && (hit.superlayer == _superLayer)) {
					getWire(hit.layer6, hit.wire, coords);
					Support3D.drawLine(drawable, coords, dgtzColor, 1f);
				}
			}
		}
		
//		int hitCount = DC.hitCount();
//		
//		if (hitCount > 0) {
//			byte sector[] = DC.sector();
//			byte superlayer[] = DC.superlayer();
//			byte layer[] = DC.layer();
//			short wire[] = DC.wire();
//			int pid[] = DC.pid();
//			double avgX[] = DC.avgX();
//			double avgY[] = DC.avgY();
//			double avgZ[] = DC.avgZ();
//			float docas[] = DC.doca();
//
//			
//			for (int i = 0; i < hitCount; i++) {
//				try {
//					int sect1 = sector[i]; // 1 based
//					if (sect1 == _sector) {
//						int supl1 = superlayer[i]; // 1 based
//						if (supl1 == _superLayer) {
//							int lay1 = layer[i];
//							int wire1 = wire[i];
//							getWire(lay1, wire1, coords);
//
//							if (showMCTruth() && (pid != null) && (avgX != null)) {
//								
//
//								
//								Color color = truthColor(pid, i);
//								Support3D.drawLine(drawable, coords, color, 2f);
//								// convert mm to cm
//								double xcm = avgX[i] / 10;
//								double ycm = avgY[i] / 10;
//								double zcm = avgZ[i] / 10;
//								drawMCPoint(drawable, xcm, ycm, zcm, color);
//							} else {
//								Support3D.drawLine(drawable, coords, dgtzColor, 1f);
//							}
//							// doca: mm to cm
//							double doca = docas[i]/10;
//
//							if (showGemcDOCA() && !Double.isNaN(doca) && (doca > .01)) {
//								Support3D.drawTube(drawable, coords[0], coords[1],
//										coords[2], coords[3], coords[4], coords[5],
//										(float) doca, docaColor);
//							}
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			} //for
//
//		} //hitcount > 0
		drawTBData(drawable);	
	}
	
	private void drawTBData(GLAutoDrawable drawable) {
//		int hitCount = DC.timeBasedTrkgHitCount();
//
//		if (hitCount > 0) {
//			int sector[] = DC.timeBasedTrkgSector();
//			int superlayer[] = DC.timeBasedTrkgSuperlayer();
//			int layer[] = DC.timeBasedTrkgLayer();
//			int wire[] = DC.timeBasedTrkgWire();
//			double doca[] = DC.timeBasedTrkgDoca();
//			
//			for (int i = 0; i < hitCount; i++) {
//				try {
//					int sect1 = sector[i]; // 1 based
//					int supl1 = superlayer[i]; // 1 based
//
//					if ((sect1 == _sector) && (supl1 == _superLayer)) {
//						int lay1 = layer[i]; // 1 based
//						int wire1 = wire[i]; // 1 based
//						getWire(lay1, wire1, coords);
//
//						// drawDOCA(Graphics g, IContainer container, int layer,
//						// int wire, double doca2d, Color fillColor, Color
//						// lineColor) {
//
//						double tbdoca = doca[i];
//						if (showTBDOCA() && !Double.isNaN(tbdoca) && (tbdoca > .01)) {
//							Support3D.drawTube(drawable, coords[0], coords[1],
//									coords[2], coords[3], coords[4], coords[5],
//									(float) tbdoca, CedColors.tbDocaLine);
//						}
//					}
//				} catch (NullPointerException e) {
//					System.err.println("null pointer in SectorSuperLayer time based hit drawing");
//					e.printStackTrace();
//				}
//			} // for loop
//
//		} //hit count > 0
	}

	/**
	 * Get the 1-based sector [1..6]
	 * 
	 * @return the 1-based sector [1..6]
	 */
	public int getSector() {
		return _sector;
	}

	/**
	 * Get the 1-based super layer [1..6]
	 * 
	 * @return the 1-based super layer [1..6]
	 */
	public int getSuperLayer() {
		return _superLayer;
	}

	private void getWire(int layer, int wire, float coords[]) {
		org.jlab.geom.prim.Line3D dcwire = DCGeometry.getWire(_sector,
				_superLayer, layer, wire);
		org.jlab.geom.prim.Point3D p0 = dcwire.origin();
		org.jlab.geom.prim.Point3D p1 = dcwire.end();
		coords[0] = (float) p0.x();
		coords[1] = (float) p0.y();
		coords[2] = (float) p0.z();
		coords[3] = (float) p1.x();
		coords[4] = (float) p1.y();
		coords[5] = (float) p1.z();
	}

	// show DCs?
	@Override
	protected boolean show() {
		boolean showdc = ((ForwardPanel3D) _panel3D).show(CedPanel3D.SHOW_DC);
		return showdc && showSector(_sector);
	}

	// show GEMC DOCAs?
	private boolean showGemcDOCA() {
		return ((ForwardPanel3D) _panel3D).show(CedPanel3D.SHOW_GEMC_DOCA);
	}

	// show Time based DOCAs?
	private boolean showTBDOCA() {
		return ((ForwardPanel3D) _panel3D).show(CedPanel3D.SHOW_TB_DOCA);
	}

}
