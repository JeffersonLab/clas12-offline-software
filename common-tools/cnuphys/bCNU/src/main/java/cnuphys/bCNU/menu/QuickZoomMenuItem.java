package cnuphys.bCNU.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import cnuphys.bCNU.view.BaseView;

public class QuickZoomMenuItem extends JMenuItem {
    
    public QuickZoomMenuItem(String title, final BaseView view, 
	    final double xmin, final double ymin, final double xmax, final double ymax) {
	super(title);
	
	ActionListener al = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		view.getContainer().zoom(xmin, xmax, ymin, ymax);
		
	    }
	    
	};
	
	addActionListener(al);
    }
}
