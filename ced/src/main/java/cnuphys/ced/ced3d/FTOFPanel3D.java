package cnuphys.ced.ced3d;

import java.awt.Color;

import com.jogamp.opengl.GLAutoDrawable;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.event.data.ColumnData;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.geometry.FTOFGeometry;
import bCNU3D.Panel3D;

public class FTOFPanel3D extends DetectorItem3D {

	// individual paddles
	private FTOFPaddle3D _paddles[];

	// one based sector [1..6]
	private final int _sector;

	// "superlayer" [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	private final int _superLayer;

	/**
	 * An FTOF Panel 3D item
	 * 
	 * @param panel3d
	 *            the owner graphical panel
	 * @param sector
	 *            the sector 1..6
	 * @param superLayer
	 *            the super layer [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
	 */
	public FTOFPanel3D(Panel3D panel3d, int sector, int superLayer) {
		super(panel3d);
		_sector = sector;
		_superLayer = superLayer;

		_paddles = new FTOFPaddle3D[FTOFGeometry.numPaddles[superLayer]];
		for (int paddleId = 1; paddleId <= _paddles.length; paddleId++) {
			_paddles[paddleId - 1] = new FTOFPaddle3D(sector, superLayer,
					paddleId);
		}
	}

	/**
	 * Get the number of paddles
	 * 
	 * @return the number of paddles
	 */
	public int getPaddleCount() {
		return _paddles.length;
	}

	/**
	 * Get the paddle
	 * 
	 * @param paddleId
	 *            the 1-based index
	 * @return the paddle
	 */
	public FTOFPaddle3D getPaddle(int paddleId) {
		return _paddles[paddleId - 1];
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {
	}

	@Override
	public void drawData(GLAutoDrawable drawable) {

		int hitCount = DataSupport.ftofGetHitCount(_superLayer);
		if (hitCount < 1) {
			return;
		}


		int pid[] = null;
		int sector[] = null;
		int paddleId[] = null;
		double x[] = null;
		double y[] = null;
		double z[] = null;


		switch (_superLayer) {
		case DataSupport.PANEL_1A:
			pid = ColumnData.getIntArray("FTOF1A::true.pid");
			x = ColumnData.getDoubleArray("FTOF1A::true.avgX");
			y = ColumnData.getDoubleArray("FTOF1A::true.avgY");
			z = ColumnData.getDoubleArray("FTOF1A::true.avgZ");
			sector = ColumnData.getIntArray("FTOF1A::dgtz.sector");
			paddleId = ColumnData.getIntArray("FTOF1A::dgtz.paddle");
			break;
		case DataSupport.PANEL_1B:
			pid = ColumnData.getIntArray("FTOF1B::true.pid");
			x = ColumnData.getDoubleArray("FTOF1B::true.avgX");
			y = ColumnData.getDoubleArray("FTOF1B::true.avgY");
			z = ColumnData.getDoubleArray("FTOF1B::true.avgZ");
			sector = ColumnData.getIntArray("FTOF1B::dgtz.sector");
			paddleId = ColumnData.getIntArray("FTOF1B::dgtz.paddle");
			break;
		case DataSupport.PANEL_2:
			pid = ColumnData.getIntArray("FTOF2B::true.pid");
			x = ColumnData.getDoubleArray("FTOF2B::true.avgX");
			y = ColumnData.getDoubleArray("FTOF2B::true.avgY");
			z = ColumnData.getDoubleArray("FTOF2B::true.avgZ");
			sector = ColumnData.getIntArray("FTOF2B::dgtz.sector");
			paddleId = ColumnData.getIntArray("FTOF2B::dgtz.paddle");
			break;
		}

		if (paddleId == null) {
			Log.getInstance().warning("null paddleId array in FTOFPanel3D");
			return;
		}

		for (int i = 0; i < hitCount; i++) {
			if (sector[i] == _sector) {
				try {
					if (showMCTruth() && (pid != null)) {
						Color color = truthColor(pid, i);
						getPaddle(paddleId[i]).drawPaddle(drawable, color);
						// convert mm to cm
						double xcm = x[i] / 10;
						double ycm = y[i] / 10;
						double zcm = z[i] / 10;
						drawMCPoint(drawable, xcm, ycm, zcm, color);

					} else {
						getPaddle(paddleId[i]).drawPaddle(drawable, dgtzColor);
					}
				} catch (ArrayIndexOutOfBoundsException oob) {
					System.err.println("sector: " + _sector);
					System.err.println("superLayer: " + _superLayer);
					System.err.println("paddleId: " + paddleId[i]);
					oob.printStackTrace();
				}
			}
		}
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

	// show FTOFs?
	@Override
	protected boolean show() {
		return ((FTOF3D) getParent()).show();
	}

}
