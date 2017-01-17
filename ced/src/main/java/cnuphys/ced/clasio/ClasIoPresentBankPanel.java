package cnuphys.ced.clasio;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.component.ActionLabel;
import cnuphys.ced.clasio.table.NodeTable;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.ced.fastmc.FastMCManager;

/**
 * Panel that shows which banks are present in an event
 * 
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class ClasIoPresentBankPanel extends JPanel implements ActionListener,
		IClasIoEventListener, IAccumulationListener {
	
	// the event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// hash table
	private Hashtable<String, ActionLabel> _alabels = new Hashtable<String, ActionLabel>(
			193);

	// the node table
	private NodeTable _nodeTable;

	private Hashtable<String, ClasIoBankDialog> _dataBanks = new Hashtable<String, ClasIoBankDialog>(
			193);

	/**
	 * This panel holds all the known banks in a grid of buttons. Banks present
	 * will be clickable, and will cause the table to scroll to that name
	 * 
	 * @param nodeTable
	 *            the table
	 */
	public ClasIoPresentBankPanel(NodeTable nodeTable) {
		_nodeTable = nodeTable;
		_eventManager.addClasIoEventListener(this, 1);
		setLayout(new GridLayout(40, 4, 2, 0));

		// get all the known banks
		String[] allBanks = _eventManager.getKnownBanks();
		for (String s : allBanks) {
			if (!skip(s)) {
				makeLabel(s);
			}
		}

		setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));
		AccumulationManager.getInstance().addAccumulationListener(this);
	}

	// skip certain irrelevant banks
	private boolean skip(String s) {
		// if ("CLAS6EVENT::particle".equals(s)) {
		// return true;
		// } else if ("SIMEVENT::particle".equals(s)) {
		// return true;
		// }
		return false;
	}

	// update as the result of a new event arriving
	private void update() {
		String[] allBanks = _eventManager.getKnownBanks();
		for (String s : allBanks) {
			ActionLabel alabel = _alabels.get(s);

			if (alabel != null) {
				boolean inCurrent = _eventManager.isBankInCurrentEvent(s);
				alabel.setEnabled(inCurrent);

				ClasIoBankDialog bd = _dataBanks.get(s);
				if (bd != null) {
					if (inCurrent) {
						bd.update();
					} else {
						bd.setVisible(false);
					}
				}
			}
		}
	}

	// convenience method to make a button
	private ActionLabel makeLabel(final String label) {
		final ActionLabel alabel = new ActionLabel(label, false);
		alabel.setOpaque(true);

		MouseListener ml = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (_eventManager.isBankInCurrentEvent(label)) {
					int clickCount = e.getClickCount();

					if (clickCount == 1) {
						_nodeTable.makeNameVisible(label);
					} else if (clickCount == 2) {
						ClasIoBankDialog bd = _dataBanks.get(label);

						if (bd == null) {
							bd = new ClasIoBankDialog(label);
							_dataBanks.put(label, bd);
						}
						bd.update();
						bd.setVisible(true);
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (_eventManager.isBankInCurrentEvent(label)) {
					alabel.setBackground(Color.yellow);
				}

			}

			@Override
			public void mouseExited(MouseEvent e) {
				alabel.setBackground(null);
			}

		};

		alabel.addMouseListener(ml);

		// alabel.addActionListener(this);
		_alabels.put(label, alabel);
		add(alabel);
		return alabel;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		// _nodeTable.makeNameVisible(ae.getActionCommand());
	}

	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	@Override
	public void newFastMCGenEvent(PhysicsEvent event) {
		if (!FastMCManager.getInstance().isStreaming()) {
			update();
		}
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		if (!_eventManager.isAccumulating()) {
			update();
		}
	}

	@Override
	public void openedNewEventFile(String path) {
	}

	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
			update();
			break;
		}
	}
	
	/**
	 * Change the event source type
	 * @param source the new source: File, ET, FastMC
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}


}
