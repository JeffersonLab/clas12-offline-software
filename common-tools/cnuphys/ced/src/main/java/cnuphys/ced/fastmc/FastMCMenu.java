package cnuphys.ced.fastmc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

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
	
	//pause item
	private JMenuItem _pauseItem;
	
	//generate neural net data
	private JMenuItem _generateNNDataItem;
	
	//define acceptance
	private JMenu _acceptanceMenu;
	
	//used to pause streaming
	private boolean _paused = false;
	
	//the neural net data dialog
	private NNDataDialog _nnDataDialog;
	
	//hard coded acceptance definitions
	private JCheckBoxMenuItem _eItem;
	private JCheckBoxMenuItem _pItem;
	
	//the next menu item
	private JMenuItem _nextItem;
	
	//stream events
	private JMenuItem _streamItem;
	
	//count events streamed
	private int _streamCount = 0;
		
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
		
		_nextItem = addItem("Next FastMC Event");
		addSeparator();
		_streamItem = addItem("Stream all Events");
		_pauseItem = addItem("Pause Streaming");
		_pauseItem.setEnabled(false);
		
		addSeparator();
		_generateNNDataItem = addItem("Generate Neural Net Data...");

		
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
	public void newClasIoEvent(DataEvent event) {
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
			_streamCount = 0;
			_paused = false;
			_pauseItem.setEnabled(false);
		_fastMCManager.openFile();
		} else if (o == _nextItem) {
			_fastMCManager.nextEvent();
		} else if (o == _streamItem) {
			_paused = false;
			threadedScanFile();
		}
		else if (o == _pauseItem) {
			_paused = true;
			_pauseItem.setEnabled(false);
		}
		else if (o == _generateNNDataItem) {
			generateNeuralNetData();
		}
		fixMenuState();
	}
	
	//generate neural net data
	private void generateNeuralNetData() {
		if (_nnDataDialog == null) {
			_nnDataDialog = new NNDataDialog();
		}
		_nnDataDialog.setVisible(true);
	}
	
	//read in a separate thread
	private void threadedScanFile() {

		final BusyPanel busyPanel = Ced.getBusyPanel();

		class MyWorker extends SwingWorker<String, Void> {
			@Override
			protected String doInBackground() {
				_fastMCManager.setStreaming(true);
				_pauseItem.setEnabled(true);
				_openItem.setEnabled(false);
				do {
					_streamCount++;
					if ((_streamCount % 100) == 0) {
						busyPanel.setText("Processing event  " + _streamCount);
					}
					_fastMCManager.nextEvent();
				} while ((_fastMCManager.getCurrentGenEvent() != null) && !_paused);
				
				_fastMCManager.setStreaming(false);
				if (!_paused) {
					_streamCount = 0;
				}
				_paused = false;
				_pauseItem.setEnabled(false);
				_openItem.setEnabled(true);
				return "Done";
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
		_pauseItem.setEnabled(_fastMCManager.isStreaming());
		
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
