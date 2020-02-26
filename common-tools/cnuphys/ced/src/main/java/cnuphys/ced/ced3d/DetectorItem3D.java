package cnuphys.ced.ced3d;

import java.awt.Color;

import item3D.Item3D;
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
	
	protected static final float STRIPLINEWIDTH = 10f;
	protected static final float WIRELINEWIDTH = 3f;

	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();
	
	protected CedPanel3D _cedPanel3D;

	public DetectorItem3D(CedPanel3D panel3D) {
		super(panel3D);
		_cedPanel3D = panel3D;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {

		if (!show()) {
			return;
		}

		if (_cedPanel3D.showVolumes() && (getVolumeAlpha() > 2)) {
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

	// an overall show
	protected abstract boolean show();

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
	 * to the LundId
	 * @param pid the particle lund id array
	 * @param index index into the array
	 * @return the truth color
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
	 * Obtain the MC truth color, which corresponds
	 * to the LundId
	 * @param lundId
	 * @return the truth color
	 */
	protected static Color truthColor(int lundId) {

		LundId lid = LundSupport.getInstance().get(lundId);
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


}
