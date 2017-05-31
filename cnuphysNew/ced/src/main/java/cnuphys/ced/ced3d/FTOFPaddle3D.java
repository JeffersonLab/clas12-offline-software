package cnuphys.ced.ced3d;

import java.awt.Color;
import bCNU3D.Support3D;
import cnuphys.ced.geometry.FTOFGeometry;
import com.jogamp.opengl.GLAutoDrawable;

public class FTOFPaddle3D {

	// one based sector [1..6]
	private final int _sector;

	// "superlayer" [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	private final int _superLayer;

	// 1 -based paddle Id
	private final int _paddleId;

	// the cached vertices
	private float[] _coords = new float[24];

	// frame the paddle?
	private static boolean _frame = true;

	/**
	 * @param sector
	 *            1-based sector
	 * @param superLayer
	 *            the "superlayer" [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 * @param paddleId
	 *            1-based paddle Id
	 */
	public FTOFPaddle3D(int sector, int superLayer, int paddleId) {
		_sector = sector;
		_superLayer = superLayer;
		_paddleId = paddleId;

		// get and cache the vertices
		FTOFGeometry.paddleVertices(sector, superLayer, paddleId, _coords);
	}

	/**
	 * Get the sector [1..6]
	 * 
	 * @return the sector 1..6
	 */
	public int getSector() {
		return _sector;
	}

	/**
	 * Get the superlayer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 * 
	 * @return the superlayer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 */
	public int getSuperLayer() {
		return _superLayer;
	}

	/**
	 * Get the 1-based paddleId 1..
	 * 
	 * @return the paddle Id
	 */
	public int getPaddleId() {
		return _paddleId;
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
