package cnuphys.bCNU.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import cnuphys.bCNU.view.BaseView;

public class QuickZoomMenu extends JMenu implements ActionListener {
    
    //base view owner
    private BaseView _view;
    
    //default zoom
    private JMenuItem _defaultZoomItem;

    public QuickZoomMenu(BaseView view) {
	super("Quick Zoom");
	_view = view;
	
	_defaultZoomItem = new JMenuItem("Default Zoom");
	_defaultZoomItem.addActionListener(this);
	add(_defaultZoomItem);
    }
    
    /**
     * Get the base view owner
     * @return the base view owner
     */
    public BaseView getView() {
	return _view;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

	Object source = e.getSource();

	if (source == _defaultZoomItem) {
	    if ((_view != null) && (_view.getContainer() != null)) {
		_view.getContainer().restoreDefaultWorld();
	    }
	}

    }
}
