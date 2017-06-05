package cnuphys.ced.dcnoise.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.dialog.ButtonPanel;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.view.ViewManager;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.snr.NoiseReductionParameters;

@SuppressWarnings("serial")
public class NoiseParameterDialog extends JDialog {

	// left (toward higher wire numbers)
	private static final int LEAN_LEFT = 0;

	// left (toward lower wire numbers)
	private static final int LEAN_RIGHT = 1;

	private static final String leanStr[] = { "left leaning shifts",
			"right leaning shifts" };

	private static final String[] slStrings = { "Super Layer 1 (Region 1)",
			"Super Layer 2 (Region 1)", "Super Layer 3 (Region 2)",
			"Super Layer 4 (Region 2)", "Super Layer 5 (Region 3)",
			"Super Layer 6 (Region 3)" };

	// spinner maxes for layer shifts
	private static final int _spinnerMax[] = { 0, 12, 14, 16, 18, 20 };

	// graphically represents the current selections
	private MiniChamber _miniChamber;

	// for specifying which superlayer is being edited
	private JComboBox _superLayerComboBox;

	// for specifying number of missing layers
	private JComboBox _missingLayerComboBox;

	// finding segments only
	private JRadioButton segmentButton;

	// finding tracks
	private JRadioButton trackButton;

	// copies used for editing
	private NoiseReductionParameters _clonedParameters[];
	private int _hotIndex = 0;

	private JLabel _chamberLabel = new JLabel(slStrings[0]);

	private JSpinner _shiftSpinner[][];

	// why the dialog closed.
	private int _reason = DialogUtilities.CANCEL_RESPONSE;

	/**
	 * Create a dialog for editing the noise parameters. There is just one set
	 * of six (one per superlayer) common to all sectors.
	 */
	public NoiseParameterDialog() {
		setTitle("DC Noise Algorithm Parameters");
		setModal(true);
		setIconImage(ImageManager.cnuIcon.getImage());

		// close is like a cancel
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				doClose(DialogUtilities.CANCEL_RESPONSE);
			}
		};
		addWindowListener(wa);

		_shiftSpinner = new JSpinner[2][GeoConstants.NUM_LAYER];
		addComponents();

		// clone the data from sector 0 (all sectors the same). Edit the clone
		// in case user hits cancel.
		_clonedParameters = new NoiseReductionParameters[GeoConstants.NUM_SUPERLAYER];
		for (int superLayer = 0; superLayer < GeoConstants.NUM_SUPERLAYER; superLayer++) {
			_clonedParameters[superLayer] = new NoiseReductionParameters();
			_clonedParameters[superLayer].copyEditableParameters(NoiseManager
					.getInstance().getParameters(0, superLayer));
		}

		stuffHotParameters();

		pack();
		GraphicsUtilities.centerComponent(this);
	}

	// add all the widgets
	private void addComponents() {
		setLayout(new BorderLayout(4, 4));

		Box box = Box.createVerticalBox();
		box.add(Box.createVerticalStrut(6));

		JLabel label = new JLabel("Parameters are the same for all sectors");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		box.add(label);
		box.add(Box.createVerticalStrut(6));

		box.add(findTrackPanel());
		box.add(Box.createVerticalStrut(6));

		// add combo for selected superlayer
		createSuperLayerComboBox();
		_superLayerComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		box.add(DialogUtilities.paddedPanel(20, 6, _superLayerComboBox));

		// add the main parameter panel
		box.add(Box.createVerticalStrut(6));
		box.add(parameterBox());

		// add the completed composite box
		add(box, BorderLayout.NORTH);

		// the mini chamber
		_miniChamber = new MiniChamber();
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BorderLayout(4, 4));
		subPanel.add(_miniChamber, BorderLayout.CENTER);
		subPanel.add(_chamberLabel, BorderLayout.NORTH);
		subPanel.setBorder(new CommonBorder());
		add(subPanel, BorderLayout.CENTER);

		// the closeout buttons
		add(createButtonPanel(), BorderLayout.SOUTH);

		// padding
		add(Box.createHorizontalStrut(4), BorderLayout.EAST);
		add(Box.createHorizontalStrut(4), BorderLayout.WEST);
	}

	private JPanel findTrackPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));

		ButtonGroup bg = new ButtonGroup();

		segmentButton = new JRadioButton("Superlayer Segments",
				!NoiseReductionParameters.lookForTracks());
		trackButton = new JRadioButton("Tracks",
				NoiseReductionParameters.lookForTracks());

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				NoiseReductionParameters.setLookForTracks(trackButton
						.isSelected());
				ClasIoEventManager.getInstance().reloadCurrentEvent();
			}

		};

		segmentButton.addActionListener(al);
		trackButton.addActionListener(al);

		bg.add(segmentButton);
		bg.add(trackButton);

		panel.add(segmentButton);
		panel.add(trackButton);
		return panel;
	}

	// which superlayer selector
	private void createSuperLayerComboBox() {

		_superLayerComboBox = new JComboBox(slStrings);
		_superLayerComboBox.setSelectedIndex(_hotIndex);

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				_hotIndex = _superLayerComboBox.getSelectedIndex();
				_chamberLabel.setText(slStrings[_hotIndex]);
				stuffHotParameters();
			}

		};
		_superLayerComboBox.addActionListener(al);
	}

	// number of missing layer selector
	private void createMissingLayerComboBox() {
		String[] slStrings = { "Allow no missing layers",
				"Allow 1 missing layer", "Allow 2 missing layers",
				"Allow 3 missing layers", "Allow 4 missing layers" };

		_missingLayerComboBox = new JComboBox(slStrings);
		_missingLayerComboBox.setSelectedIndex(2);

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int index = _missingLayerComboBox.getSelectedIndex();
				NoiseReductionParameters hot = _clonedParameters[_hotIndex];
				hot.setAllowedMissingLayers(index);
			}

		};
		_missingLayerComboBox.addActionListener(al);

	}

	// create the main parameter panel
	private Box parameterBox() {
		Box box = Box.createVerticalBox();

		createMissingLayerComboBox();
		box.add(DialogUtilities.paddedPanel(20, 6, _missingLayerComboBox));
		box.add(Box.createVerticalStrut(6));

		Box hbox = Box.createHorizontalBox();
		hbox.add(createLayerShiftArray(LEAN_LEFT));
		hbox.add(Box.createHorizontalStrut(8));
		hbox.add(createLayerShiftArray(LEAN_RIGHT));
		box.add(hbox);

		box.setBorder(new CommonBorder("Algorithm Parameters"));
		return box;
	}

	// stuff the parameters from the current hot index.
	private void stuffHotParameters() {
		NoiseReductionParameters hot = _clonedParameters[_hotIndex];
		_missingLayerComboBox.setSelectedIndex(hot.getAllowedMissingLayers());

		// stuff the layer shifts
		int leftShifts[] = hot.getLeftLayerShifts();
		for (int i = 0; i < leftShifts.length; i++) {
			_shiftSpinner[LEAN_LEFT][i].setValue(leftShifts[i]);
		}
		int rightShifts[] = hot.getRightLayerShifts();
		for (int i = 0; i < rightShifts.length; i++) {
			_shiftSpinner[LEAN_RIGHT][i].setValue(rightShifts[i]);
		}

		_miniChamber.setParameters(hot);
	}

	/**
	 * Create a panel for the layer shifts.
	 * 
	 * @param lean
	 *            either LEAN_LEFT or LEAN_RIGHT.
	 * @return the layer shift panel.
	 */
	private JPanel createLayerShiftArray(final int lean) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6, 1, 0, 8));

		ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) (e.getSource());
				for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
					if (spinner == _shiftSpinner[lean][layer]) {
						NoiseReductionParameters hot = _clonedParameters[_hotIndex];
						if (lean == LEAN_LEFT) {
							int leftShifts[] = hot.getLeftLayerShifts();
							SpinnerNumberModel model = (SpinnerNumberModel) spinner
									.getModel();
							leftShifts[layer] = model.getNumber().intValue();
							_miniChamber.repaint();
						} else {
							int rightShifts[] = hot.getRightLayerShifts();
							SpinnerNumberModel model = (SpinnerNumberModel) spinner
									.getModel();
							rightShifts[layer] = model.getNumber().intValue();
							_miniChamber.repaint();
						}
						break;
					}
				}
			} // state changed

		};

		for (int layer = 0; layer < GeoConstants.NUM_LAYER; layer++) {
			SpinnerNumberModel model = new SpinnerNumberModel(0, 0,
					_spinnerMax[layer], 1);
			_shiftSpinner[lean][layer] = new JSpinner(model);
			_shiftSpinner[lean][layer].addChangeListener(changeListener);
			if (layer == 0) {
				_shiftSpinner[lean][layer].setEnabled(false);
			}
			_shiftSpinner[lean][layer].setEditor(new JSpinner.DefaultEditor(
					_shiftSpinner[lean][layer]));
			panel.add(_shiftSpinner[lean][layer]);
		}

		panel.setBorder(new CommonBorder(leanStr[lean]));
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

		return ButtonPanel.closeOutPanel(ButtonPanel.USE_OKCANCELAPPLY, alist,
				30);

	}

	// user has hit ok or cancel
	private void doClose(int reason) {
		_reason = reason;

		if (reason == DialogUtilities.CANCEL_RESPONSE) {
			setVisible(false);
		}

		if ((reason == DialogUtilities.OK_RESPONSE)
				|| (reason == DialogUtilities.APPLY_RESPONSE)) {
			// copy from clone to real data--all sectors the same

			for (int sector = 0; sector < 6; sector++) {
				for (int superLayer = 0; superLayer < GeoConstants.NUM_SUPERLAYER; superLayer++) {
					NoiseManager
							.getInstance()
							.getParameters(sector, superLayer)
							.copyEditableParameters(
									_clonedParameters[superLayer]);
				}
			}
			ViewManager.getInstance().refreshAllViews();

			if (reason == DialogUtilities.OK_RESPONSE) {
				setVisible(false);
			}
			ClasIoEventManager.getInstance().reloadCurrentEvent();
		} // ok or apply
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

	/**
	 * Main program for testing
	 * 
	 * @param arg
	 * @return
	 */
	public static void main(String arg[]) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				NoiseParameterDialog dialog = new NoiseParameterDialog();
				dialog.setVisible(true);
			}
		});

	}
}
