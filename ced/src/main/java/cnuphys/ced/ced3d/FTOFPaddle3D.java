package cnuphys.ced.ced3d;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import bCNU3D.Panel3D;
import bCNU3D.Support3D;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.event.data.FTOFDataContainer;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

import com.jogamp.opengl.GLAutoDrawable;

public class FTOFPaddle3D extends DetectorItem3D {

    //one based sector [1..6]
    private final int _sector;
    
    // "superlayer" [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
    private final int _superLayer;
    
    //1 -based paddle Id
    private final int _paddleId;
    
    //the vertices
    private float[] coords = new float[24];

    protected static final Color outlineColor = new Color(64, 64, 0, 16);
   
    /**
     * 
     * @param panel3d the owener panel
     * @param sector 1-based sector
     * @param superLayer the "superlayer" [PANEL_1A, PANEL_1B, PANEL_2] (0, 1, 2)
     * @param paddleId 1-based paddle Id
     */
    public FTOFPaddle3D(Panel3D panel3d, int sector, int superLayer, int paddleId) {
	super(panel3d);
	_sector = sector;
	_superLayer = superLayer;
	_paddleId = paddleId;
	FTOFGeometry.paddleVertices(sector, superLayer, paddleId, coords);
     }

    @Override
    public void drawShape(GLAutoDrawable drawable) {
 	drawMe(drawable, outlineColor);
   }
    
    private void drawMe(GLAutoDrawable drawable, Color color) {
	if (!show()) {
	    return;
	}
	boolean frame = true;
 	Support3D.drawQuad(drawable, coords, 0, 1, 2, 3, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 3, 7, 6, 2, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 0, 4, 7, 3, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 0, 4, 5, 1, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 1, 5, 6, 2, color, 1f, frame);
 	Support3D.drawQuad(drawable, coords, 4, 5, 6, 7, color, 1f, frame);	
    }

    @Override
    public void drawData(GLAutoDrawable drawable) {
	if (!show()) {
	    return;
	}
	
	// the overall container
	FTOFDataContainer ftofData = _eventManager.getFTOFData();

	int pid[] = null;
	int sector[] = null;
	int paddles[] = null;
	int hitCount = ftofData.getHitCount(_superLayer);
	
	if (hitCount < 1) {
	    return;
	}

	switch (_superLayer) {
	case FTOFDataContainer.PANEL_1A:
	    pid = ftofData.ftof1a_true_pid;
	    sector = ftofData.ftof1a_dgtz_sector;
	    paddles = ftofData.ftof1a_dgtz_paddle;
	    break;
	case FTOFDataContainer.PANEL_1B:
	    pid = ftofData.ftof1b_true_pid;
	    sector = ftofData.ftof1b_dgtz_sector;
	    paddles = ftofData.ftof1b_dgtz_paddle;
	    break;
	case FTOFDataContainer.PANEL_2B:
	    pid = ftofData.ftof2b_true_pid;
	    sector = ftofData.ftof2b_dgtz_sector;
	    paddles = ftofData.ftof2b_dgtz_paddle;
	    break;
	}

	if (paddles == null) {
	    Log.getInstance().warning("null paddles array in FTOFPanel3D");
	    return;
	}
	
	for (int i = 0; i < hitCount; i++) {
	    if ((sector[i] == _sector) && (paddles[i] == _paddleId)) {
		drawMe(drawable, dgtzColor);
	    }
	}
	
    }
    
    //show FTOFs?
    private boolean show() {
	return ((CedPanel3D)_panel3D).show(CedPanel3D.SHOW_FTOF);
    }


}
