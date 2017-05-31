package cnuphys.ced.ced3d;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;

public abstract class Paddle3D {


	// the cached vertices. 3 (xyz) times 8 corners = 24
	protected float[] _coords = new float[24];

	// frame the slab (paddle)?
	protected boolean _frame = true;
	
	//1 based layer ID
	protected final int _layerId;
	
	// 1-based ID
	protected final int _paddleId;

	public Paddle3D(int paddleId) {
		this(1, paddleId);
	}

	public Paddle3D(int layer, int paddleId) {
		_layerId = layer;
		_paddleId = paddleId;
		fillVertices();
	}

	// fill the coords
	protected abstract void fillVertices();
	
	/**
	 * Set whether we frame the paddle
	 * @param frame the new frame flag
	 */
	public void setFrame(boolean frame) {
		_frame = frame;
	}
	
	/**
	 * Draw the paddle
	 * 
	 * @param drawable
	 *            the drawable
	 * @param color
	 *            the color
	 */
	protected void drawPaddle(GLAutoDrawable drawable, Color color) {
		Support3D.drawQuad(drawable, _coords, 0, 1, 2, 3, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 3, 7, 6, 2, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 0, 4, 7, 3, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 0, 4, 5, 1, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 1, 5, 6, 2, color, 1f, _frame);
		Support3D.drawQuad(drawable, _coords, 4, 5, 6, 7, color, 1f, _frame);
	}
}
