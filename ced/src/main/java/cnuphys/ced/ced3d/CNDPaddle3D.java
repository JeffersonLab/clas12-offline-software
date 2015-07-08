package cnuphys.ced.ced3d;

import java.awt.Color;

import bCNU3D.Support3D;

import com.jogamp.opengl.GLAutoDrawable;

import cnuphys.ced.geometry.CNDGeometry;

public class CNDPaddle3D {

    //1 based layerID 1..3
    private final int _layerId;
   
    //1 -based paddle Id 1..48
    private final int _paddleId;
    
    //the cached vertices
    private float[] _coords = new float[24];
    
    //frame the paddle?
    private static boolean _frame = true;

    /**
     * @param sector 1-based sector
     * @param superLayer the layer [1,2,3]
     * @param paddleId 1-based paddle Id [1..48]
     */
    public CNDPaddle3D(int layer, int paddleId) {
	_layerId = layer;
	_paddleId = paddleId;
	
	//get and cache the vertices
	CNDGeometry.paddleVertices(layer, paddleId, _coords);
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
