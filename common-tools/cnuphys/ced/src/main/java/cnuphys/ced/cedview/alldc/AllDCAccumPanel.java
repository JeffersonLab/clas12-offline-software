package cnuphys.ced.cedview.alldc;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.ced.cedview.CedView;
import cnuphys.splot.plot.GraphicsUtilities;

@SuppressWarnings("serial")
public class AllDCAccumPanel extends JPanel implements ActionListener, ItemListener {

	// the view owner
	private AllDCAccumView _allDCAccumView;

	// the reset button
	private JButton _resetButton;

	public AllDCAccumPanel(CedView view) {
		_allDCAccumView = (AllDCAccumView) view;
		setLayout(new BorderLayout(2, 2));
		addCenter();
		addSouth();
	}

	// add the center panel

	private JCheckBox _currentEventButton;
	private JRadioButton _allDataButton;
	private JRadioButton _hideNoiseButton;
	private JRadioButton _noiseOnlyButton;

	private void addCenter() {
		JPanel panel = new JPanel();
		panel.setLayout(new VerticalFlowLayout());

		ButtonGroup bg = new ButtonGroup();

		_allDataButton = addRB("Show all data", bg, true, panel);
		_hideNoiseButton = addRB("Exclude noise data", bg, false, panel);
		_noiseOnlyButton = addRB("Show noise only", bg, false, panel);

		add(panel, BorderLayout.CENTER);
	}

	private JRadioButton addRB(String label, ButtonGroup bg, boolean selected, JPanel panel) {
		JRadioButton rb = new JRadioButton(label, selected);
		rb.addItemListener(this);
		bg.add(rb);
		GraphicsUtilities.setSizeSmall(rb);
		panel.add(rb);
		return rb;
	}

	// add the south panel
	private void addSouth() {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));

		_resetButton = new JButton("Reset");
		GraphicsUtilities.setSizeSmall(_resetButton);
		_resetButton.addActionListener(this);
		p.add(_resetButton);
		add(p, BorderLayout.SOUTH);
	}

//	private JCheckBox _currentEventButton;
//	private JRadioButton _allDataButton;
//	private JRadioButton _hideNoiseButton;
//	private JRadioButton _noiseOnlyButton;

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == _resetButton) {
			_allDCAccumView.reset();
		} else {
			System.err.println(e.getActionCommand());
		}

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();

		if (source == _currentEventButton) {

		} else if (source instanceof JRadioButton) {
			JRadioButton rb = (JRadioButton) source;

			if (rb.isSelected()) {
				if (rb == _allDataButton) {
					_allDCAccumView.setMode(AllDCAccumView.SHOW_ALL_MODE);
				} else if (rb == _hideNoiseButton) {
					_allDCAccumView.setMode(AllDCAccumView.HIDE_NOISE_MODE);
				} else if (rb == _noiseOnlyButton) {
					_allDCAccumView.setMode(AllDCAccumView.NOISE_ONLY_MODE);
				}
//				System.err.println("[" + rb.getActionCommand() + "] is selected");
			} else {
//				System.err.println("[" + rb.getActionCommand() + "] is NOT selected");
			}
		}

	}
}
