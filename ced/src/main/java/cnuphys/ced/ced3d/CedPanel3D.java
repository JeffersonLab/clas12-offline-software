package cnuphys.ced.ced3d;

import item3D.Axes3D;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.ced.component.PIDLegend;
import cnuphys.ced.geometry.FTOFGeometry;
import bCNU3D.Panel3D;

public class CedPanel3D extends Panel3D {
        
    private final float xymax = 600f;
    private final float zmax = 600f;
    private final float zmin = -100f;

    //Check box array
    private CheckBoxArray _checkBoxArray; 
    
    //show what particles are present
    private final PIDLegend _pidLegend;
 
    //labels for the check box
    public static final String SHOW_TRUTH = "Monte Carlo Truth";
    public static final String SHOW_DC = "Drift Chambers (DC)";
    public static final String SHOW_FTOF = "Forward Time of Flight (FTOF)";
    private static final String _cbaLabels[] = {SHOW_TRUTH, SHOW_DC, SHOW_FTOF};

    /*
     * The panel that holds the 3D objects
     * @param angleX the initial x rotation angle in degrees
     * @param angleY the initial y rotation angle in degrees
     * @param angleZ the initial z rotation angle in degrees
     * @param xdist move viewpoint left/right
     * @param ydist move viewpoint up/down
     * @param zdist the initial viewer z distance should be negative
     */
    public CedPanel3D(float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {
	super(angleX, angleY, angleZ, xDist, yDist, zDist);
	_pidLegend = new PIDLegend(this);
 	add(_pidLegend, BorderLayout.NORTH);
	
	_checkBoxArray = new CheckBoxArray(1, 4, 4, _cbaLabels);
	add(_checkBoxArray, BorderLayout.EAST);

	ActionListener al = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		refresh();
	    }
	    
	};
	for (String s : _cbaLabels) {
	    AbstractButton ab = _checkBoxArray.getButton(s); 
	    ab.setSelected(true);
	    ab.addActionListener(al);
	}
	fixSize();
    }
    
    @Override
    public void refresh() {
	super.refresh();
        _pidLegend.repaint();
    }

    @Override
    public void createInitialItems() {
	//coordinate axes
	Axes3D axes = new Axes3D(this, -xymax, xymax, -xymax, xymax, zmin, zmax, Color.darkGray, 1f, 11, 
		Color.darkGray, new Font("SansSerif", Font.PLAIN, 12));
	addItem(axes);
	
	//trajectory drawer
	TrajectoryDrawer3D trajDrawer = new TrajectoryDrawer3D(this);
	addItem(trajDrawer);
	
	
	//dc super layers
	for (int sector = 1; sector <= 61; sector++) {
	    for (int superlayer = 1; superlayer <= 6; superlayer++) {
		DCSuperLayer3D dcsl = new DCSuperLayer3D(this, sector, superlayer);
		addItem(dcsl);
	    }
	}
	
	// tof paddles
	for (int sector = 1; sector <= 6; sector++) {
	    for (int superlayer = 0; superlayer < 3; superlayer++) {
		for (int paddleId = 1; paddleId <= FTOFGeometry.numPaddles[superlayer]; paddleId++) {
		    FTOFPaddle3D ftofpad = new FTOFPaddle3D(this, sector,
			    superlayer, paddleId);
		    addItem(ftofpad);
		}
	    }
	}
   }
    
    /**
     * This gets the z step used by the mouse and key adapters, to see how
     * fast we move in or in in response to mouse wheel or up/down arrows.
     * It should be overridden to give something sensible. like the scale/100;
     * @return the z step (changes to zDist) for moving in and out
     */
    @Override
    public float getZStep() {
	return (zmax-zmin)/50f;
    }


    //a fixed fraction of the screen
    private void fixSize() {
	Dimension d = GraphicsUtilities.screenFraction(0.70);
	d.width = d.height;
	gljpanel.setPreferredSize(d);
    }

    /**
     * Check if a feature should be drawn
     * @param label the label for the check box on the option array
     * @return <code>true</code> if the feature should be drawn
     */
    public boolean show(String label) {
	AbstractButton ab = _checkBoxArray.getButton(label);
	return (ab == null) ? false : ab.isSelected();
    }

}
