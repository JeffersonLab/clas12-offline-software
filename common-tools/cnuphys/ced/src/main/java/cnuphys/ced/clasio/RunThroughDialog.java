package cnuphys.ced.clasio;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.dialog.ButtonPanel;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;

import cnuphys.ced.event.data.DC;
import cnuphys.ced.frame.Ced;

public class RunThroughDialog extends JDialog {

	// why the dialog closed.
	private int _reason = DialogUtilities.CANCEL_RESPONSE;


	// progress bar as events are accumulated
	private JProgressBar _progressBar;

	//description
	private JLabel _describeLabel;
	
	// path to event file
	private JLabel _pathLabel;

	// number of events total
	private JLabel _totalLabel;

	// the ClasIO event manager
	private ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();
	
	private IRunThrough _rthrough;

	/**
	 * Constructor for the runthrough Dialog
	 */
	public RunThroughDialog(IRunThrough rthrough) {
		_rthrough = rthrough;
		setTitle("Run through all events");
		setModal(false);
		setIconImage(ImageManager.cnuIcon.getImage());

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

		// path label

		Box subBox = Box.createVerticalBox();
		_describeLabel = new JLabel("Creating a one-time mapping of sequential to true event number.");
		_pathLabel = new JLabel("Current event source: " + _eventManager.getCurrentSourceDescription());
		_totalLabel = new JLabel("Total number of events: " + _eventManager.getEventCount());

		subBox.add(DialogUtilities.paddedPanel(6, 6, _describeLabel));
		subBox.add(DialogUtilities.paddedPanel(6, 6, _pathLabel));
		subBox.add(DialogUtilities.paddedPanel(6, 6, _totalLabel));
		subBox.setBorder(new CommonBorder("Event File"));
		box.add(DialogUtilities.paddedPanel(6, 6, subBox));


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

				if (ButtonPanel.CANCEL_LABEL.equals(command)) {
					doClose(DialogUtilities.CANCEL_RESPONSE);
				}

			}

		};

		return ButtonPanel.closeOutPanel(ButtonPanel.USE_CANCEL, alist, 50);

	}
	
	public void process() {
		try {
			final int totalCount = _eventManager.getEventCount();
			if (totalCount < 1) {
				_reason = DialogUtilities.CANCEL_RESPONSE;
			}
			
			_eventManager.gotoEvent(1);

			Runnable runnable = new Runnable() {

				@Override
				public void run() {

					int modCount = Math.max(2, totalCount / 100);
					boolean nullEvent = false;

					int count = 0;
					while (isVisible() && !nullEvent && _eventManager.hasEvent()) {
						count++;

						DataEvent event = _eventManager.bareNextEvent();
						nullEvent = (event == null);

						if (!nullEvent) {
							_rthrough.nextRunthroughEvent(event);

							if (((count + 1) % modCount) == 0) {
								int value = (int) ((100.0 * count) / totalCount);
								_progressBar.setValue(value);
							}
						}
					}

					// we are done 

					_rthrough.runThroughtDone();
					doClose(DialogUtilities.OK_RESPONSE);
				}
			};

			(new Thread(runnable)).start();

		} catch (Exception e) {
			doClose(DialogUtilities.CANCEL_RESPONSE);
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
	 * @return either DialogUtilities.OK_RESPONSE or DialogUtilities.CANCEL_RESPONSE
	 */
	public int getReason() {
		return _reason;
	}

}