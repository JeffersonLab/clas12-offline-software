package cnuphys.ced.ced3d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

import com.jogamp.opengl.awt.GLJPanel;

import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.PrintUtilities;
import cnuphys.ced.component.PIDLegend;
import bCNU3D.Panel3D;

public class CedPanel3D extends Panel3D {
        

    //Check box array
    private CheckBoxArray _checkBoxArray; 
    
    //show what particles are present
    private final PIDLegend _pidLegend;
    
 
    //display array labels
    private String _cbaLabels[];

    /*
     * The panel that holds the 3D objects
     * @param angleX the initial x rotation angle in degrees
     * @param angleY the initial y rotation angle in degrees
     * @param angleZ the initial z rotation angle in degrees
     * @param xdist move viewpoint left/right
     * @param ydist move viewpoint up/down
     * @param zdist the initial viewer z distance should be negative
     */
    public CedPanel3D(float angleX, float angleY, float angleZ, 
	    float xDist, float yDist, float zDist,
	    String ...cbaLabels) {
	super(angleX, angleY, angleZ, xDist, yDist, zDist);
	
	_cbaLabels = cbaLabels;
	_pidLegend = new PIDLegend(this);
 	add(_pidLegend, BorderLayout.NORTH);
 	
 	gljpanel.setBorder(new CommonBorder());
 	final GLJPanel gljp = gljpanel;
	
 	addEast();

	ActionListener al = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		refresh();
		gljp.requestFocus();
	    }
	    
	};
	for (String s : _cbaLabels) {
	    AbstractButton ab = _checkBoxArray.getButton(s); 
	    ab.setSelected(true);
	    ab.addActionListener(al);
	}
	fixSize();
    }
    
    //add eastern panel
    private void addEast() {
	JPanel ep = new JPanel();
	ep.setLayout(new VerticalFlowLayout());
	
	ep.add(new KeyboardLegend());
	_checkBoxArray = new CheckBoxArray(2, 4, 4, _cbaLabels);
	_checkBoxArray.setBorder(new CommonBorder());
	ep.add(_checkBoxArray);
	

	add(ep, BorderLayout.EAST);
    }
    
    @Override
    public void refresh() {
	super.refresh();
        _pidLegend.repaint();
    }


    /**
     * Print the panel.
     */
    @Override
    public void print() {
	PrintUtilities.printComponent(this);
    }

    /**
     * Snapshot of the panel.
     */
    @Override
    public void snapshot() {
	GraphicsUtilities.saveAsPng(this);
    }

    //a fixed fraction of the screen
    private void fixSize() {
	Dimension d = GraphicsUtilities.screenFraction(0.60);
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
