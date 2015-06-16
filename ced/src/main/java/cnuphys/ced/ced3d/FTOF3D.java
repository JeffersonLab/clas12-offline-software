package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Panel3D;
import cnuphys.lund.X11Colors;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

public class FTOF3D extends DetectorItem3D {

    private int drawList = -1;

    // individual paddles indices are PANEL_1A, PANEL_1B, PANEL_2 (0,1,2)
    private FTOFPanel3D _panels[];

    // one based sector [1..6]
    private final int _sector;

    public FTOF3D(Panel3D panel3d, int sector) {
	super(panel3d);
	_sector = sector;

	_panels = new FTOFPanel3D[3];
	for (int panelId = 0; panelId < 3; panelId++) {
	    _panels[panelId] = new FTOFPanel3D(panel3d, sector, panelId);
	    addChild(_panels[panelId]);
	}
    }

    @Override
    public void drawShape(GLAutoDrawable drawable) {

	GL2 gl = drawable.getGL().getGL2();
	Color outlineColor = X11Colors.getX11Color("Light Sky Blue", getVolumeAlpha());

	for (FTOFPanel3D panel : _panels) {
	    for (int paddleId = 1; paddleId <= panel.getPaddleCount(); paddleId++) {
		panel.getPaddle(paddleId).drawPaddle(drawable, outlineColor);
	    }
	}

	gl.glCallList(drawList);

//	if (drawList < 0) {
//	    System.err.println("Creating drawlist for FTOF sector " + _sector);
//	    drawList = gl.glGenLists(1);
//	    gl.glNewList(drawList, GL2.GL_COMPILE);
//	    for (FTOFPanel3D panel : _panels) {
//		for (int paddleId = 1; paddleId <= panel.getPaddleCount(); paddleId++) {
//		    panel.getPaddle(paddleId)
//			    .drawPaddle(drawable, outlineColor);
//		}
//	    }
//	    gl.glEndList();
//	}
//
//	gl.glCallList(drawList);

    }

    @Override
    public void drawData(GLAutoDrawable drawable) {
	// children panels handle it
    }

    // show FTOFs?
    @Override
    protected boolean show() {
	boolean showtof = ((ForwardPanel3D) _panel3D).show(ForwardPanel3D.SHOW_FTOF);
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
