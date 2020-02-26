package cnuphys.fastMCed.streaming;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;

public class StreamDialog extends JDialog implements IStreamProcessor {

	/**
	 * Maximum number of events we will allow for streaming
	 */
	public static final int MAXSTREAMCOUNT = 1000000;

	// why the dialog closed.
	private int _reason = DialogUtilities.CANCEL_RESPONSE;

	// get the number of events
	private JTextField _numberField;

	// progress bar as events are streamed
	private JProgressBar _progressBar;

	// generator label
	private JLabel _generatorLabel;

	// number of events total
	private JLabel _totalLabel;

	// number of events remaining
	private JLabel _remainingLabel;

	// number of remaining events
	private int _numRemaining;

	// the ClasIO event manager
	private PhysicsEventManager _eventManager = PhysicsEventManager.getInstance();
	
	
	//buttons
	private JButton _runButton;
	private JButton _resumeButton;
	private JButton _closeButton;


	private static int lastCount = 1000;

	/**
	 * Constructor for the streaming Dialog
	 */
	public StreamDialog() {
		setTitle("Stream Events");
		setModal(false);
		setIconImage(ImageManager.cnuIcon.getImage());

		// close is like a cancel
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				handleClose();
			}
		};
		addWindowListener(wa);

		addComponents();
		pack();
		GraphicsUtilities.centerComponent(this);

		StreamManager.getInstance().addStreamListener(this);
	}

	// add all the widgets
	private void addComponents() {
		setLayout(new BorderLayout(6, 6));

		Box box = Box.createVerticalBox();


		// path label

		Box subBox = Box.createVerticalBox();
		_generatorLabel = new JLabel();
		_totalLabel = new JLabel();
		_remainingLabel = new JLabel();
		
		fixNumRemaining();

		subBox.add(DialogUtilities.paddedPanel(6, 6, _generatorLabel));
		subBox.add(DialogUtilities.paddedPanel(6, 6, _totalLabel));
		subBox.add(DialogUtilities.paddedPanel(6, 6, _remainingLabel));
		subBox.setBorder(new CommonBorder(""));
		box.add(DialogUtilities.paddedPanel(6, 6, subBox));

		// streaming panel
		box.add(streamPanel(_numRemaining > 0));

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
	
	private void fixNumRemaining() {
		_numRemaining = _eventManager.getNumRemainingEvents();
		if (_numRemaining > 0) {
			_generatorLabel.setText(_eventManager.getGeneratorDescription());
			_totalLabel.setText("Total number: "
					+ _eventManager.getEventCount());
			_remainingLabel.setText("Remaining: " + _numRemaining);
		} else {
			_generatorLabel.setText("No event generator.");
			_totalLabel.setText("Total number: 0");
			_remainingLabel.setText("Remaining: 0");
		}
		
	}

	// the number-to-stream panel
	private JPanel streamPanel(boolean hasSource) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		panel.add(new JLabel("Number to stream: "));
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
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		_runButton = new JButton(" Run ");
		_resumeButton = new JButton(" Resume ");
		_closeButton = new JButton(" Close ");

		ActionListener alist = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();

				if (source == _runButton) {
					handleRun();
				}
				else if (source == _resumeButton) {
					StreamManager.getInstance().setStreamState(StreamReason.STARTED);
				}
				else if (source == _closeButton) {
					handleClose();
				}


			}

		};
		
		_runButton.addActionListener(alist);
		_resumeButton.addActionListener(alist);
		_closeButton.addActionListener(alist);

		panel.add(_runButton);
		panel.add(_resumeButton);
		panel.add(_closeButton);
		
		fixButtons(StreamManager.getInstance().getStreamState());
		
		return panel;

	}
	
	private void handleClose() {
		doClose(DialogUtilities.CANCEL_RESPONSE);
	}
	//convenience
	private StreamReason getState() {
		return StreamManager.getInstance().getStreamState();
	}
	
	private boolean isStarted() {
		return getState() == StreamReason.STARTED;
	}
	
	private boolean isPaused() {
		return getState() == StreamReason.PAUSED;
	}

	@Override
	public void setVisible(boolean vis) {

		if (!vis) {
			StreamManager.getInstance().setStreamState(StreamReason.STOPPED);
			StreamManager.getInstance().removeStreamListener(this);
		}
		super.setVisible(vis);
	}
	
	private void handleRun() {

		// if number field was not enabled treat as cancel.
		if (!_numberField.isEnabled()) {
			_reason = DialogUtilities.CANCEL_RESPONSE;
			setVisible(false);
			return;
		}


		try {
			int count = Integer.parseInt(_numberField.getText().trim());
			if (count < 1) {
				_reason = DialogUtilities.CANCEL_RESPONSE;
			} else {
				count = Math.min(count, MAXSTREAMCOUNT);
				count = Math.min(count, _numRemaining);
			}
			final int fcount = count;

			Runnable runnable = new Runnable() {

				@Override
				public void run() {
	//				_eventManager.setStreaming(true);

					int modCount = Math.max(2, fcount / 100);

					int count = 0;
					while ((isStarted() || isPaused()) && (count < fcount)) {

						if (isPaused()) {
							
							try {
								Thread.sleep(1000);
								//System.err.println("Stream is paused.");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						} else {
							if (_eventManager.moreEvents()) {
								_eventManager.nextEvent();
								count++;
							}

							if (((count + 1) % modCount) == 0) {
								int value = (int) ((100.0 * count) / fcount);
								fixNumRemaining();
								_progressBar.setValue(value);

							}
						}
					}
					
					
					// we are done streaming
					
					StreamManager.getInstance().setStreamState(StreamReason.STOPPED);

					
					setVisible(false);

//					if (StreamManager.getInstance().isStopped()) {
//						setVisible(false);
//					}
//					
//					//				_eventManager.setStreaming(false);
										
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							//reload last event
							_eventManager.reloadCurrentEvent();

							ViewManager.getInstance()
									.refreshAllViews();
							
						}
					});

					fixNumRemaining();
	//				doClose(DialogUtilities.OK_RESPONSE);

				} // run
			};

			StreamManager.getInstance().setStreamState(StreamReason.STARTED);

			(new Thread(runnable)).start();

			lastCount = count;
		} catch (Exception e) {
			_reason = DialogUtilities.CANCEL_RESPONSE;
		}
		
	}

	// user has hit ok or cancel
	private void doClose(int reason) {
		_reason = reason;
		setVisible(false);
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
	
	private void fixButtons(StreamReason reason) {
		//fix buttons
		switch(reason) {
		case STARTED:
			_runButton.setEnabled(false);
			_resumeButton.setEnabled(false);
			break;
		case STOPPED:
			_runButton.setEnabled(true);
			_resumeButton.setEnabled(false);
			break;
		case PAUSED:
			_runButton.setEnabled(false);
			_resumeButton.setEnabled(true);
			fixNumRemaining();
			break;
		}
	}

	//STARTED, STOPPED, PAUSED, FINISHED, RESUMED
	@Override
	public void streamingChange(StreamReason reason) {
		fixButtons(reason);
	}

	@Override
	public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		return StreamProcessStatus.CONTINUE;
	}

	@Override
	public String flagExplanation() {
		return "No way this happened.";
	}
	

}