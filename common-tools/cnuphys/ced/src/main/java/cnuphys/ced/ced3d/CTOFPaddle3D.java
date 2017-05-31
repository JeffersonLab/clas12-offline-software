package cnuphys.ced.ced3d;

import cnuphys.ced.geometry.CTOFGeometry;

public class CTOFPaddle3D extends Paddle3D {


	/**
	 * @param paddleId
	 *            1-based paddle Id [1..48]
	 */
	public CTOFPaddle3D(int paddleId) {
		super(paddleId);
	}


	@Override
	protected void fillVertices() {
		CTOFGeometry.paddleVertices(_paddleId, _coords);
	}
}
