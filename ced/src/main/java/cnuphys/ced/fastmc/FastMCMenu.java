package cnuphys.ced.fastmc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;

public class FastMCMenu extends JMenu implements ActionListener, IClasIoEventListener {
	
    //the fast mc manager
	private FastMCManager _fastMCManager = FastMCManager.getInstance();
	
	//the open menu
	private JMenuItem  _openItem;
	
	//define acceptance
	private JMenuItem _acceptanceItem;
	
	//the next menu item
	private JMenuItem _nextItem;
	/**
	 * Create a FastMC Menu
	 */
	public FastMCMenu() {
		super("FastMC");
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
		
		_openItem = addItem("Open Lund File...");
		_acceptanceItem = addItem("Define Acceptance...");
		addSeparator();

		_nextItem = addItem("Next FastMC Event");

		
		fixMenuState();
		setEnabled(false);
	}
	
	//add a menu item
	private JMenuItem addItem(String label) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(this);
		add(item);
		return item;
	}

	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	public void newFastMCGenEvent(PhysicsEvent event) {
	}
	
	@Override
	public void newClasIoEvent(EvioDataEvent event) {
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	@Override
	public void changedEventSource(EventSourceType source) {
		setEnabled(source == EventSourceType.FASTMC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == _openItem) {
			_fastMCManager.openFile();
		}
		else if (o == _nextItem) {
			_fastMCManager.nextEvent();
		}
		else if (o == _acceptanceItem) {
			//TODO implement
		}
		
		fixMenuState();
	}
	
	
	//fix the menus state
	private void fixMenuState() {
		boolean  goodFile = (_fastMCManager.getCurrentFile() != null);
		
		_nextItem.setEnabled(goodFile);
	}

}
