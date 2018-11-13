package cnuphys.ced.clasio;

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

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.dialog.ButtonPanel;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.frame.Ced;

public class ClasIoAccumulationDialog extends JDialog {

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

	// the ClasIO event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// Object that accumulates and stores data
	// This will be the accumulation manager
	private IAccumulator _accumulator;

	private static boolean lastState = false;
	private static int lastCount = 1000;

	/**
	 * Constructor for the accumulation Dialog
	 */
	public ClasIoAccumulationDialog(IAccumulator accumulator) {
		setTitle("Accumulate Events");
		setModal(true);
		setIconImage(ImageManager.cnuIcon.getImage());

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

		_numRemaining = _eventManager.getNumRemainingEvents();

		// add the clear toggle
		_clearButton = new JCheckBox(" Clear accumulated data ", lastState);
		box.add(DialogUtilities.paddedPanel(20, 6, _clearButton));

		// path label

		Box subBox = Box.createVerticalBox();
		_pathLabel = new JLabel();
		_totalLabel = new JLabel();
		_remainingLabel = new JLabel();

		if (_numRemaining > 0) {
			_pathLabel.setText(_eventManager.getCurrentSourceDescription());
			_totalLabel.setText("Total number: "
					+ _eventManager.getEventCount());
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
		box.add(accumulationPanel(_numRemaining > 0));

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
				AccumulationManager.getInstance().notifyListeners(
						AccumulationManager.ACCUMULATION_CANCELLED);
				setVisible(false);
				return;
			}

			// clear data?
			boolean lastState = _clearButton.isSelected();
			if (lastState) {
	//			System.err.println("ClasIoAccum Clear");
				_accumulator.clear();
			}

			try {
				int count = Integer.parseInt(_numberField.getText().trim());
				if (count < 1) {
					_reason = DialogUtilities.CANCEL_RESPONSE;
				} else {
					count = Math.min(count, MAXACCUMULATIONCOUNT);
					count = Math.min(count, _numRemaining-1);
				}
				final int fcount = count;

				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						_eventManager.setAccumulating(true);

						int modCount = Math.max(2, fcount / 100);

						int count = 0;
						while (isVisible() && (count < fcount)) {
							
							if (_eventManager.hasEvent()) {
								DataEvent event = _eventManager.getNextEvent();
								if (event ==  null) {
									try {
										
										System.err.println("SLEEP count = " + count + "/" + fcount);
										Thread.sleep(30);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								else {
									count++;
								}
							}
							
							
//							try {
//								EventSourceType estype = _eventManager.getEventSourceType();
//								switch (estype) {
//								case HIPORING:
//									if (_eventManager.hasEvent()) {
//										DataEvent event = _eventManager.getNextEvent();
//										if (event == null) {
////											Log.getInstance().warning(
////													"A \"should not have happened\" null event in the accumulator.");
//											Thread.sleep(50);
//										} else {
//											count++;
//										}
//									} else {
//										Thread.sleep(50);
//									}
//									break;
//								default:
//									_eventManager.getNextEvent();
//									count++;
//									break;
//								}
//
//
//							} catch (Exception e) {
//								e.printStackTrace();
//								break;
//							}

							if (((count + 1) % modCount) == 0) {
								int value = (int) ((100.0 * count) / fcount);
								_progressBar.setValue(value);

								if (Ced.getCed().playDCOccupancy()) {
									DC.getInstance().playOccupancyTone(75);
								}
							}
						}
						
						
						// we are done accumulating
						_eventManager.setAccumulating(false);
						AccumulationManager.getInstance().notifyListeners(
								AccumulationManager.ACCUMULATION_FINISHED);
												
						
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setVisible(false);
								//reload last event
								_eventManager.reloadCurrentEvent();

								ViewManager.getInstance()
										.refreshAllViews();
								
							}
						});

					}
				};

				AccumulationManager.getInstance().notifyListeners(AccumulationManager.ACCUMULATION_STARTED);

				(new Thread(runnable)).start();

				lastCount = count;
			} catch (Exception e) {
				_reason = DialogUtilities.CANCEL_RESPONSE;
			}

		} //ok
		else {
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