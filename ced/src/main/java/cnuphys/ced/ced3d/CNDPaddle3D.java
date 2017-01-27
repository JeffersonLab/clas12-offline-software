package cnuphys.ced.ced3d;

import cnuphys.ced.geometry.CNDGeometry;

public class CNDPaddle3D  extends Paddle3D {


	/**
	 * @param layer
	 *            the layer [1,2,3]
	 * @param paddleId
	 *            1-based paddle Id [1..48]
	 */
	public CNDPaddle3D(int layer, int paddleId) {
		super(layer, paddleId);
	}


	@Override
	protected void fillVertices() {
		CNDGeometry.paddleVertices(_layerId, _paddleId, _coords);
	}

}
