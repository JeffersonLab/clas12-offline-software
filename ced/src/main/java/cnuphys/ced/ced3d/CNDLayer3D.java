package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Panel3D;
import cnuphys.lund.X11Colors;

import com.jogamp.opengl.GLAutoDrawable;

public class CNDLayer3D extends DetectorItem3D {

    // 1-based layer 1..3
    private final int _layer;

    // the paddles
    private CNDPaddle3D _paddles[];

    public CNDLayer3D(Panel3D panel3d, int layer) {
	super(panel3d);
	_layer = layer;

	_paddles = new CNDPaddle3D[48];
	for (int paddleId = 1; paddleId <= 48; paddleId++) {
	    _paddles[paddleId - 1] = new CNDPaddle3D(layer, paddleId);
	}
    }

    @Override
    public void drawShape(GLAutoDrawable drawable) {
	Color outlineColor = X11Colors.getX11Color("Medium Spring Green",
		getVolumeAlpha());

	for (int paddleId = 1; paddleId <= 48; paddleId++) {
	    getPaddle(paddleId).drawPaddle(drawable, outlineColor);
	}
    }

    @Override
    public void drawData(GLAutoDrawable drawable) {
    }

    @Override
    protected boolean show() {
	CentralPanel3D cpan = (CentralPanel3D) _panel3D;
	if (!cpan.show(CedPanel3D.SHOW_CND)) {
	    return false;
	}

	switch (_layer) {
	case 1:
	    return cpan.show(CedPanel3D.SHOW_CND_LAYER_1);

	case 2:
	    return cpan.show(CedPanel3D.SHOW_CND_LAYER_2);

	case 3:
	    return cpan.show(CedPanel3D.SHOW_CND_LAYER_3);
	}
	return false;
    }

    /**
     * Get the 3D Paddle
     * 
     * @param paddleId the paddle Id [..48]
     * @return the 3D paddle
     */
    public CNDPaddle3D getPaddle(int paddleId) {
	if ((paddleId < 1) || (paddleId > 48)) {
	    return null;
	}

	return _paddles[paddleId - 1];
    }
}
