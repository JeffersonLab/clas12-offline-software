package cnuphys.ced.ced3d;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.lund.X11Colors;

public class CTOF3D extends DetectorItem3D {

	// child layer items
	private CTOFPaddle3D _paddles[];

	/**
	 * The 3D CND
	 * 
	 * @param panel3d
	 *            the 3D panel owner
	 */
	public CTOF3D(CedPanel3D panel3D) {
		super(panel3D);

		_paddles = new CTOFPaddle3D[48];
		for (int paddleId = 1; paddleId <= 48; paddleId++) {
			_paddles[paddleId - 1] = new CTOFPaddle3D(paddleId);
		}
		
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
		
		Color noHitColor = X11Colors.getX11Color("Dodger blue", getVolumeAlpha());
		Color hitColor = X11Colors.getX11Color("red", getVolumeAlpha());
		TdcAdcHitList hits = CTOF.getInstance().getHits();


		for (int paddleId = 1; paddleId <= 48; paddleId++) {

			TdcAdcHit hit = null;
			if ((hits != null) && !hits.isEmpty()) {
				hit = hits.get(0, 0, paddleId);
			}

			_paddles[paddleId - 1].drawPaddle(drawable, hit == null ? noHitColor : hitColor);
		}
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {
		// Children handle drawing

	}

	@Override
	protected boolean show() {
		return _cedPanel3D.showCTOF();
	}

}
