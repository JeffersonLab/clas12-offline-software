package cnuphys.cnf.event;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.component.ActionLabel;
import cnuphys.cnf.event.table.NodeTable;

/**
 * Panel that shows which banks are present in an event
 * 
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class PresentBankPanel extends JPanel
		implements ActionListener, IEventListener {

	// the event manager
	private EventManager _eventManager = EventManager.getInstance();

	// hash table
	private Hashtable<String, ActionLabel> _alabels = new Hashtable<String, ActionLabel>(193);

	// the node table
	private NodeTable _nodeTable;

	private Hashtable<String, BankDialog> _dataBanks = new Hashtable<>(193);

	/**
	 * This panel holds all the known banks in a grid of buttons. Banks present will
	 * be clickable, and will cause the table to scroll to that name
	 * 
	 * @param nodeTable the table
	 */
	public PresentBankPanel(NodeTable nodeTable) {
		_nodeTable = nodeTable;
		_eventManager.addEventListener(this, 1);
		setLayout(new GridLayout(40, 4, 2, 0));

		// get all the known banks
		String[] allBanks = _eventManager.getKnownBanks();
		for (String s : allBanks) {
			if (!skip(s)) {
				makeLabel(s);
			}
		}

		setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));
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

				BankDialog bdlog = _dataBanks.get(s);
				if (bdlog != null) {
					if (inCurrent) {
						bdlog.update();
					} else {
						bdlog.clear();
					}
				}
			}
		}
	}

	// convenience method to make a button
	private ActionLabel makeLabel(final String label) {
		final ActionLabel alabel = new ActionLabel(label, false, true);
		alabel.setOpaque(true);

		MouseListener ml = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (_eventManager.isBankInCurrentEvent(label)) {
					int clickCount = e.getClickCount();

					if (clickCount == 1) {
						_nodeTable.makeNameVisible(label);
					} else if (clickCount == 2) {
						BankDialog bdlog = _dataBanks.get(label);

						if (bdlog == null) {
							bdlog = new BankDialog(label);
							_dataBanks.put(label, bdlog);
						}
						bdlog.update();

						if (!bdlog.isVisible()) {
							bdlog.setVisible(true);
						}
						
						bdlog.toFront();
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

	@Override
	public void newEvent(DataEvent event, boolean isStreaming) {
		if (!isStreaming) {
			update();
		}
	}

	@Override
	public void openedNewEventFile(File file) {
	}

	/**
	 * Rewound the current file
	 * @param file the file
	 */
	@Override
	public void rewoundFile(File file) {
		
	}


	/**
	 * Streaming start message
	 * @param file file being streamed
	 * @param numToStream number that will be streamed
	 */
	@Override
	public void streamingStarted(File file, int numToStream) {
	}
	
	/**
	 * Streaming ended message
	 * @param file the file that was streamed
	 * @param int the reason the streaming ended
	 */
	@Override
	public void streamingEnded(File file, int reason) {
	}
}
