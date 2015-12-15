package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.ced.event.data.FTOFDataContainer;
import cnuphys.lund.X11Colors;

import com.jogamp.opengl.GLAutoDrawable;

public class FTOF3D extends DetectorItem3D {

	// private int drawList = -1;

	// individual paddles indices are PANEL_1A, PANEL_1B, PANEL_2 (0,1,2)
	// and are child items
	private FTOFPanel3D _panels[];

	// one based sector [1..6]
	private final int _sector;

	/**
	 * The 3D FTOF
	 * 
	 * @param panel3d
	 *            the 3D panel owner
	 * @param sector
	 *            the 1-based sector [1..6]
	 */
	public FTOF3D(Panel3D panel3d, int sector) {
		super(panel3d);
		_sector = sector;

		// add the three panels as child items
		_panels = new FTOFPanel3D[3];
		for (int panelId = 0; panelId < 3; panelId++) {
			_panels[panelId] = new FTOFPanel3D(panel3d, sector, panelId);
			addChild(_panels[panelId]);
		}
	}

	@Override
	public void drawShape(GLAutoDrawable drawable) {

		// GL2 gl = drawable.getGL().getGL2();
		Color outlineColor = X11Colors.getX11Color("Light Sky Blue",
				getVolumeAlpha());

		for (FTOFPanel3D panel : _panels) {
			for (int paddleId = 1; paddleId <= panel.getPaddleCount(); paddleId++) {
				panel.getPaddle(paddleId).drawPaddle(drawable, outlineColor);
			}
		}

		// gl.glCallList(drawList);

		// if (drawList < 0) {
		// System.err.println("Creating drawlist for FTOF sector " + _sector);
		// drawList = gl.glGenLists(1);
		// gl.glNewList(drawList, GL2.GL_COMPILE);
		// for (FTOFPanel3D panel : _panels) {
		// for (int paddleId = 1; paddleId <= panel.getPaddleCount();
		// paddleId++) {
		// panel.getPaddle(paddleId)
		// .drawPaddle(drawable, outlineColor);
		// }
		// }
		// gl.glEndList();
		// }
		//
		// gl.glCallList(drawList);

	}

	/**
	 * Create a set of same color and size points for use on a Panel3D.
	 * 
	 * @param panel3D the owner 3D panel
	 * @param coords the points as [x1, y1, z1, ..., xn, yn, zn]
	 * @param color the color of the points
	 * @param pointSize the drawing size of the points
	 */
//	public PointSet3D(Panel3D panel3D, float[] coords, Color color,
//			float pointSize, boolean circular) {
	
	
	@Override
	public void drawData(GLAutoDrawable drawable) {
		FTOFDataContainer ftofData = _eventManager.getFTOFData();

		//DRAW reconstructed hits
		int sector[] = ftofData.ftofrec_ftofhits_sector;
		if (sector == null) {
			return;
		}
		float recX[] = ftofData.ftofrec_ftofhits_x;
		float recY[] = ftofData.ftofrec_ftofhits_y;
		float recZ[] = ftofData.ftofrec_ftofhits_z;

		int numHits = sector.length;
		// System.err.println("FTOF DRAWDATA NUM HITS: " + numHits);

		float xyz[] = new float[3];
		for (int i = 0; i < numHits; i++) {
			if (_sector == sector[i]) {
				xyz[0] = recX[i];
				xyz[1] = recY[i];
				xyz[2] = recZ[i];

				 Support3D.drawPoints(drawable, xyz, Color.cyan, Color.black,
				 10, true);

			}
		}
	}

	// show FTOFs?
	@Override
	protected boolean show() {
		boolean showtof = ((ForwardPanel3D) _panel3D)
				.show(CedPanel3D.SHOW_FTOF);
		return showtof && showSector(_sector);
	}

	/**
	 * Get the sector [1..6]
	 * 
	 * @return the sector 1..6
	 */
	public int getSector() {
		return _sector;
	}

}
