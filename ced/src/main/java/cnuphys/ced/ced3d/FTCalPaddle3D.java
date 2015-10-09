package cnuphys.ced.ced3d;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.geometry.FTCALGeometry;
import cnuphys.lund.X11Colors;

public class FTCalPaddle3D extends DetectorItem3D {

	// paddle ID
	private int _id;

	// the cached vertices
	private float[] _coords = new float[24];

	// frame the paddle?
	private static boolean _frame = true;

	public FTCalPaddle3D(Panel3D panel3d, int id) {
		super(panel3d);
		_id = id;

		FTCALGeometry.paddleVertices(_id, _coords);
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		Color outlineColor = X11Colors.getX11Color("Medium Spring Green",
				getVolumeAlpha());

		Support3D.drawQuad(drawable, _coords, 0, 1, 2, 3, outlineColor, 1f,
				_frame);
		Support3D.drawQuad(drawable, _coords, 3, 7, 6, 2, outlineColor, 1f,
				_frame);
		Support3D.drawQuad(drawable, _coords, 0, 4, 7, 3, outlineColor, 1f,
				_frame);
		Support3D.drawQuad(drawable, _coords, 0, 4, 5, 1, outlineColor, 1f,
				_frame);
		Support3D.drawQuad(drawable, _coords, 1, 5, 6, 2, outlineColor, 1f,
				_frame);
		Support3D.drawQuad(drawable, _coords, 4, 5, 6, 7, outlineColor, 1f,
				_frame);
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
	}

	@Override
	protected boolean show() {
		return true;
	}

}
