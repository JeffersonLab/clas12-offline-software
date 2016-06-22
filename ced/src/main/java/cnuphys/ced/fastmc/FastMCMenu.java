package cnuphys.ced.fastmc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.component.BusyPanel;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.frame.Ced;

public class FastMCMenu extends JMenu implements ActionListener, ItemListener, IClasIoEventListener {
	
    //the fast mc manager
	private FastMCManager _fastMCManager = FastMCManager.getInstance();
	
	//the open menu
	private JMenuItem  _openItem;
	
	//define acceptance
	private JMenu _acceptanceMenu;
	
	//hard coded acceptance definitions
	private JCheckBoxMenuItem _eItem;
	private JCheckBoxMenuItem _pItem;
	
	//the next menu item
	private JMenuItem _nextItem;
	
	//stream events
	private JMenuItem _streamItem;
		
	/**
	 * Create a FastMC Menu
	 */
	public FastMCMenu() {
		super("FastMC");
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
		
		_openItem = addItem("Open Lund File...");
		_acceptanceMenu = new JMenu("Acceptance Rule");
		add(_acceptanceMenu);
		
		//had hardcoded rules
		_eItem = addCheckBoxItem("e- in 36 DC layers", AcceptanceManager.getInstance().getElectronCondition().isActive());
		_pItem = addCheckBoxItem("p in 36 DC layers", AcceptanceManager.getInstance().getProtonCondition().isActive());
		
		addSeparator();

		_nextItem = addItem("Next FastMC Event");
		_streamItem = addItem("Stream all Events");
		
		fixMenuState();
//		setEnabled(false);
	}
	
	//add a menu item
	private JCheckBoxMenuItem addCheckBoxItem(String label, boolean on) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(label, on);
		item.addItemListener(this);
		_acceptanceMenu.add(item);
		return item;
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
	@Override
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
		} else if (o == _nextItem) {
			_fastMCManager.nextEvent();
		} else if (o == _streamItem) {
			threadedScanFile();
		}
		fixMenuState();
	}
	
	//read in a separate thread
	private void threadedScanFile() {

		final BusyPanel busyPanel = Ced.getBusyPanel();
		
		
		
//		Runnable runnable = new Runnable() {
//
//			@Override
//			public void run() {
//				_fastMCManager.setStreaming(true);
//				_fastMCManager.getTimer().restart();
//				File file = _fastMCManager.getCurrentFile();
//				String path = (file != null) ? file.getName() : "";
////				busyPanel.setText("Reading " + path);
////				busyPanel.setVisible(true);
//				int count = 0;
//				do {
//					if ((count % 100) == 0) {
//						System.err.println("Getting Event  " + count);
//						_fastMCManager.getTimer().report();
//					}
//					count++;
////					System.err.println("Getting Event  " + count);
//					_fastMCManager.nextEvent();
////					System.err.println("Done  " + count);
//				} while (_fastMCManager.getCurrentGenEvent() != null);
//				
//				_fastMCManager.getTimer().done();
//				_fastMCManager.getTimer().report();
////				busyPanel.setVisible(false);
//				Ced.getCed().fixTitle();
//				_fastMCManager.setStreaming(false);
//
//			}
//			
//		};
//		Thread thread = new Thread(runnable);
//		thread.start();
//		
		
		

		class MyWorker extends SwingWorker<String, Void> {
			@Override
			protected String doInBackground() {
				_fastMCManager.setStreaming(true);
				int count = 0;
				do {
					count++;
					if ((count % 100) == 0) {
						busyPanel.setText("Processing event  " + count);
					}
					_fastMCManager.nextEvent();
				} while (_fastMCManager.getCurrentGenEvent() != null);
				_fastMCManager.setStreaming(false);
				return "Done. Processed " + count + " events.";
			}

			@Override
			protected void done() {
				busyPanel.setVisible(false);
				Ced.getCed().fixTitle();
			}
		}

		busyPanel.setVisible(true);
		new MyWorker().execute();
	}

	
	//fix the menus state
	private void fixMenuState() {
		boolean  goodFile = (_fastMCManager.getCurrentFile() != null);
		
		_nextItem.setEnabled(goodFile);
		_streamItem.setEnabled(goodFile);
		
		Ced.getCed().getEventMenu().fixState();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object o = e.getSource();
		if (o == _eItem) {
			AcceptanceManager.getInstance().getElectronCondition().setActive(_eItem.isSelected());
			
		}
		else if (o == _pItem) {
			AcceptanceManager.getInstance().getProtonCondition().setActive(_pItem.isSelected());
		}
		AcceptanceManager.getInstance().retestCurrentEvent();
	}

}
