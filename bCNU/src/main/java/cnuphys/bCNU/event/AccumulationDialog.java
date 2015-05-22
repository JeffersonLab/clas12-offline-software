package cnuphys.bCNU.event;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.dialog.ButtonPanel;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.view.EventView;
import cnuphys.bCNU.view.ViewManager;

@SuppressWarnings("serial")
public class AccumulationDialog extends JDialog {

    /**
     * Maximum number of events we will allow for accumulation
     */
    public static final int MAXACCUMULATIONCOUNT = 1000000;

    // why the dialog closed.
    private int _reason = DialogUtilities.CANCEL_RESPONSE;

    // if checked, clear existing accumulated data before new accumulation
    private JCheckBox _clearButton;

    // get the number of events
    private JTextField _numberField;

    // progress bar as events are accumulated
    private JProgressBar _progressBar;

    // path to event file
    private JLabel _pathLabel;

    // number of events total
    private JLabel _totalLabel;

    // number of events remaining
    private JLabel _remainingLabel;

    // number of remaining events
    private int _numRemaining;

    // Object that accumulates and stores data
    private IAccumulator _accumulator;

    private static boolean lastState = false;
    private static int lastCount = 1000;

    /**
     * Constructor for the accumulation Dialog
     */
    public AccumulationDialog(IAccumulator accumulator) {
	setTitle("Accumulate Events");
	setModal(true);

	_accumulator = accumulator;

	// close is like a cancel
	WindowAdapter wa = new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent we) {
		doClose(DialogUtilities.CANCEL_RESPONSE);
	    }
	};
	addWindowListener(wa);

	addComponents();
	pack();
	GraphicsUtilities.centerComponent(this);

    }

    // add all the widgets
    private void addComponents() {
	setLayout(new BorderLayout(6, 6));

	Box box = Box.createVerticalBox();

	boolean hasEventFile = EventView.hasEventFile();
	_numRemaining = 0;
	if (hasEventFile) {
	    _numRemaining = EventView.numberOfRemainingEvents();
	}

	// add the clear toggle
	_clearButton = new JCheckBox(" Clear accumulated data ", lastState);
	_clearButton.setEnabled(hasEventFile);
	box.add(DialogUtilities.paddedPanel(20, 6, _clearButton));

	// path label

	Box subBox = Box.createVerticalBox();
	_pathLabel = new JLabel();
	_totalLabel = new JLabel();
	_remainingLabel = new JLabel();

	if (hasEventFile) {
	    _pathLabel.setText(EventView.eventFilePath());
	    _totalLabel.setText("Total number: " + EventView.numberOfEvents());
	    _remainingLabel.setText("Remaining: " + _numRemaining);
	} else {
	    _pathLabel.setText("No event file or source.");
	    _totalLabel.setText("Total number: 0");
	    _remainingLabel.setText("Remaining: 0");
	}
	subBox.add(DialogUtilities.paddedPanel(6, 6, _pathLabel));
	subBox.add(DialogUtilities.paddedPanel(6, 6, _totalLabel));
	subBox.add(DialogUtilities.paddedPanel(6, 6, _remainingLabel));
	subBox.setBorder(new CommonBorder("Event File"));
	box.add(DialogUtilities.paddedPanel(6, 6, subBox));

	// accumulation panel
	box.add(accumulationPanel(hasEventFile));

	// progress bar
	_progressBar = new JProgressBar(0, 100) {
	    @Override
	    public Dimension getPreferredSize() {
		return new Dimension(160, 20);
	    }
	};
	_progressBar.setStringPainted(true);

	box.add(DialogUtilities.paddedPanel(20, 6, _progressBar));

	// add the completed composite box
	add(box, BorderLayout.NORTH);

	// the closeout buttons
	add(createButtonPanel(), BorderLayout.SOUTH);

	// padding
	add(Box.createHorizontalStrut(4), BorderLayout.EAST);
	add(Box.createHorizontalStrut(4), BorderLayout.WEST);

    }

    // the number-to-accumulate panel
    private JPanel accumulationPanel(boolean hasSource) {
	JPanel panel = new JPanel();
	panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
	panel.add(new JLabel("Number to accumulate: "));
	_numberField = new JTextField(6);

	if (!hasSource) {
	    _numberField.setText("0");
	    _numberField.setEnabled(false);
	} else {
	    _numberField.setText("" + lastCount);
	}
	panel.add(_numberField);
	return panel;
    }

    /**
     * Create the button panel.
     * 
     * @return the button panel.
     */
    private JPanel createButtonPanel() {
	// closeout buttons-- use OK and CANCEL

	// buttons

	ActionListener alist = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (ButtonPanel.OK_LABEL.equals(command)) {
		    doClose(DialogUtilities.OK_RESPONSE);
		}

		else if (ButtonPanel.CANCEL_LABEL.equals(command)) {
		    doClose(DialogUtilities.CANCEL_RESPONSE);
		}

		else if (ButtonPanel.APPLY_LABEL.equals(command)) {
		    doClose(DialogUtilities.APPLY_RESPONSE);
		}

	    }

	};

	return ButtonPanel.closeOutPanel(ButtonPanel.USE_OKCANCEL, alist, 50);

    }

    // user has hit ok or cancel
    private void doClose(int reason) {
	_reason = reason;

	if (reason == DialogUtilities.OK_RESPONSE) {

	    // if number field was not enabled treat as cancel.
	    if (!_numberField.isEnabled()) {
		_reason = DialogUtilities.CANCEL_RESPONSE;
		setVisible(false);
		return;
	    }

	    // clear data?
	    boolean lastState = _clearButton.isSelected();
	    if (lastState) {
		_accumulator.clear();
	    }

	    try {
		int count = Integer.parseInt(_numberField.getText().trim());
		if (count < 1) {
		    _reason = DialogUtilities.CANCEL_RESPONSE;
		} else {
		    count = Math.min(count, MAXACCUMULATIONCOUNT);
		    count = Math.min(count, _numRemaining);
		}
		final int fcount = count;

		Runnable runnable = new Runnable() {

		    @Override
		    public void run() {
			EventControl.getInstance().setAccumulating(true);

			for (int i = 0; i < fcount; i++) {
			    try {
				EventView.requestEvent();
			    } catch (Exception e) {
				e.printStackTrace();
				break;
			    }

			    if (((i + 1) % 100) == 0) {
				int value = (int) ((100.0 * i) / fcount);
				_progressBar.setValue(value);
			    }
			}
			EventControl.getInstance().setAccumulating(false);

			SwingUtilities.invokeLater(new Runnable() {
			    @Override
			    public void run() {
				setVisible(false);
				ViewManager.getInstance()
					.refreshAllContainerViews();
			    }
			});

		    }
		};

		(new Thread(runnable)).start();

		lastCount = count;
	    } catch (Exception e) {
		_reason = DialogUtilities.CANCEL_RESPONSE;
	    }

	    ViewManager.getInstance().refreshAllContainerViews();
	} else {
	    setVisible(false);
	}
    }

    /**
     * Why the dialog closed.
     * 
     * @return either DialogUtilities.OK_RESPONSE or
     *         DialogUtilities.CANCEL_RESPONSE
     */
    public int getReason() {
	return _reason;
    }

}
