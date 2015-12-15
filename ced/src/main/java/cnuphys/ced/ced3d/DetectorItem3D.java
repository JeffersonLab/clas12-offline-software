package cnuphys.ced.ced3d;

import java.awt.Color;

import item3D.Item3D;
import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import com.jogamp.opengl.GLAutoDrawable;

public abstract class DetectorItem3D extends Item3D {

	protected static final Color dgtzColor = new Color(255, 0, 0);
	protected static final float MC_POINTSIZE = 3f;
	protected static final float CROSS_POINTSIZE = 5f;
	protected static final Color cosmicColor = Color.lightGray;

	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	public DetectorItem3D(Panel3D panel3d) {
		super(panel3d);
	}

	@Override
	public void draw(GLAutoDrawable drawable) {

		if (!show()) {
			return;
		}

		if (showVolumes() && (getVolumeAlpha() > 2)) {
			drawShape(drawable);
		}

		if (!_eventManager.isAccumulating()) {
			drawData(drawable);
		}

	}

	/**
	 * Draw the boundary
	 * 
	 * @param drawable
	 *            the GL drawable
	 */
	public abstract void drawShape(GLAutoDrawable drawable);

	/**
	 * Draw the data
	 * 
	 * @param drawable
	 *            the GL drawable
	 */
	public abstract void drawData(GLAutoDrawable drawable);

	// show MC Truth?
	protected boolean showMCTruth() {
		return ((CedPanel3D) _panel3D).show(CedPanel3D.SHOW_TRUTH);
	}

	// show Volumes?
	protected boolean showVolumes() {
		return ((CedPanel3D) _panel3D).show(CedPanel3D.SHOW_VOLUMES);
	}

	// an overall show
	protected abstract boolean show();

	// show reconstructed crosses?
	protected boolean showCrosses() {
		return ((CedPanel3D) _panel3D).show(CedPanel3D.SHOW_RECON_CROSSES);
	}

	// show cosmic tracks?
	protected boolean showCosmics() {
		return ((CedPanel3D) _panel3D).show(CedPanel3D.SHOW_COSMICS);
	}

	/**
	 * Get the alpha value used for drawing detector outlines
	 * 
	 * @return the alpha used for drawing detector outlines
	 */
	protected int getVolumeAlpha() {
		return ((CedPanel3D) _panel3D).getVolumeAlpha();
	}

	/**
	 * Obtain the MC truth color, which corresponds
	 * 
	 * @param pid
	 * @param index
	 * @param showMCTruth
	 * @return
	 */
	protected static Color truthColor(int pid[], int index) {

		if ((pid == null) || (index < 0) || (index >= pid.length)) {
			return Color.black;
		}

		LundId lid = LundSupport.getInstance().get(pid[index]);
		if (lid == null) {
			return dgtzColor;
		}

		return lid.getStyle().getFillColor();
	}

	/**
	 * Draw a MC 3D point
	 * 
	 * @param drawable
	 * @param xcm
	 * @param ycm
	 * @param zcm
	 * @param truthColor
	 */
	protected void drawMCPoint(GLAutoDrawable drawable, double xcm, double ycm,
			double zcm, Color truthColor) {
		Support3D.drawPoint(drawable, xcm, ycm, zcm, Color.black,
				MC_POINTSIZE + 2, true);
		Support3D.drawPoint(drawable, xcm, ycm, zcm, truthColor, MC_POINTSIZE, true);

	}

	protected void drawCrossPoint(GLAutoDrawable drawable, double xcm,
			double ycm, double zcm, Color crossColor) {
		Support3D.drawPoint(drawable, xcm, ycm, zcm, Color.red,
				CROSS_POINTSIZE + 2, true);
		Support3D.drawPoint(drawable, xcm, ycm, zcm, crossColor,
				CROSS_POINTSIZE, true);

	}

	public boolean showSector(int sector) {

		CedPanel3D p3d = (CedPanel3D) _panel3D;

		if (sector == 1) {
			return p3d.show(CedPanel3D.SHOW_SECTOR_1);
		} else if (sector == 2) {
			return p3d.show(CedPanel3D.SHOW_SECTOR_2);
		} else if (sector == 3) {
			return p3d.show(CedPanel3D.SHOW_SECTOR_3);
		} else if (sector == 4) {
			return p3d.show(CedPanel3D.SHOW_SECTOR_4);
		} else if (sector == 5) {
			return p3d.show(CedPanel3D.SHOW_SECTOR_5);
		} else if (sector == 6) {
			return p3d.show(CedPanel3D.SHOW_SECTOR_6);
		}

		return false;
	}

}
