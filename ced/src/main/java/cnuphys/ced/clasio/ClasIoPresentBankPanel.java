package cnuphys.ced.clasio;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.component.ActionLabel;
import cnuphys.ced.clasio.table.NodeTable;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;

/**
 * Panel that shows which banks are present in an event
 * 
 * @author heddle
 *
 */
public class ClasIoPresentBankPanel extends JPanel implements ActionListener,
	IClasIoEventListener, IAccumulationListener {

    // the event manager
    private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

    // hash table
    private Hashtable<String, ActionLabel> _alabels = new Hashtable<String, ActionLabel>(
	    193);

    // the node table
    private NodeTable _nodeTable;

    /**
     * This panel holds all the known banks in a grid of buttons. Banks present
     * will be clickable, and will cause the table to scroll to that name
     * 
     * @param nodeTable
     *            the table
     */
    public ClasIoPresentBankPanel(NodeTable nodeTable) {
	_nodeTable = nodeTable;
	_eventManager.addPhysicsListener(this, 1);
	setLayout(new GridLayout(25, 3, 0, 0));

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
	if ("CLAS6EVENT::particle".equals(s)) {
	    return true;
	} else if ("SIMEVENT::particle".equals(s)) {
	    return true;
	}
	return false;
    }

    // update as the result of a new event arriving
    private void update() {
	String[] allBanks = _eventManager.getKnownBanks();
	for (String s : allBanks) {
	    ActionLabel alabel = _alabels.get(s);

	    if (alabel != null) {
		alabel.setEnabled(_eventManager.isBankInCurrentEvent(s));
	    }
	}
    }

    // convenience method to make a button
    private ActionLabel makeLabel(String label) {
	ActionLabel alabel = new ActionLabel(label, false);
	alabel.addActionListener(this);
	_alabels.put(label, alabel);
	add(alabel);
	return alabel;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
	_nodeTable.makeNameVisible(ae.getActionCommand());
    }

    @Override
    public void newClasIoEvent(EvioDataEvent event) {
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
	    System.err.println("ACCUM FINISHED");
	    update();
	    break;
	}
    }

}
