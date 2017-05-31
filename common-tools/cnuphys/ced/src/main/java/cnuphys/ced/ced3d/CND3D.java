package cnuphys.ced.ced3d;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * 3D version of the central neutron detector
 * 
 * @author heddle
 *
 */
public class CND3D extends DetectorItem3D {

	// child layer items
	private CNDLayer3D _layers[];

	/**
	 * The 3D CND
	 * 
	 * @param panel3d
	 *            the 3D panel owner
	 */
	public CND3D(CedPanel3D panel3D) {
		super(panel3D);

		// add the three layers as child items
		_layers = new CNDLayer3D[3];
		for (int layer = 1; layer <= 3; layer++) {
			_layers[layer - 1] = new CNDLayer3D(panel3D, layer);
			addChild(_layers[layer - 1]);
		}

	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		// Children handle drawing

	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		// Children handle drawing

	}

	@Override
	protected boolean show() {
		return _cedPanel3D.showCND();
	}

}
