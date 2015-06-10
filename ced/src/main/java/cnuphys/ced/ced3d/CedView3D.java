package cnuphys.ced.ced3d;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.util.PrintUtilities;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.component.PIDLegend;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.swim.Swimming;

public class CedView3D extends BaseView implements IClasIoEventListener, SwimTrajectoryListener, IAccumulationListener, ActionListener {

    public static final float xdist = -100f;
    public static final float ydist = 0f;
    public static final float zdist = -1600f;
    
    private final float thetax = 0f;
    private final float thetay = 90f;
    private final float thetaz = 90f;
    
    
    //the menu bar
    private final JMenuBar _menuBar;
    

    // the event manager
    private final ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

   
    //the 3D panel
    private final CedPanel3D _panel3D;
    
    //menu
    private JMenuItem _printMenuItem;

    public CedView3D() {
	super(AttributeType.TITLE, "3D View", AttributeType.ICONIFIABLE,
		true, AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE,
		true, AttributeType.RESIZABLE, true, AttributeType.VISIBLE, true);
	
	_eventManager.addPhysicsListener(this, 2);

	// listen for trajectory changes
	Swimming.addSwimTrajectoryListener(this);
	
	_menuBar = new JMenuBar();
	setJMenuBar(_menuBar);
 	addMenus();
 	
 	

	setLayout(new BorderLayout(2, 2));
	_panel3D = new CedPanel3D(thetax, thetay, thetaz, xdist, ydist, zdist);
	
	add(_panel3D, BorderLayout.CENTER);
	pack();
	AccumulationManager.getInstance().addAccumulationListener(this);
    }
    
    private void addMenus() {
	JMenu fileMenu = new JMenu("File");
	_printMenuItem = new JMenuItem("Print...");
	_printMenuItem.addActionListener(this);
	fileMenu.add(_printMenuItem);
	_menuBar.add(fileMenu);
    }
    
    @Override
    public void newClasIoEvent(EvioDataEvent event) {
	if (!_eventManager.isAccumulating()) {
//	    setData(event);
//	    setEventNumber(_eventManager.getEventNumber());
//	    fixButtons();
	    _panel3D.refresh();	
	}
    }

    @Override
    public void openedNewEventFile(String path) {
	_panel3D.refresh();	
    }

    @Override
    public void accumulationEvent(int reason) {
	switch (reason) {
	case AccumulationManager.ACCUMULATION_STARTED:
	    break;

	case AccumulationManager.ACCUMULATION_CANCELLED:
	    _panel3D.refresh();
	    break;

	case AccumulationManager.ACCUMULATION_FINISHED:
	    _panel3D.refresh();
	    break;
	}
    }

    @Override
    public void trajectoriesChanged() {
	if (!_eventManager.isAccumulating()) {
	    _panel3D.refresh();
	}
   }

    @Override
    public void actionPerformed(ActionEvent e) {
	
	Object source = e.getSource();
	if (source == _printMenuItem) {
		PrintUtilities.printComponent(_panel3D);
	}
    }


}
