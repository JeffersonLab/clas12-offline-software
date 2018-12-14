package cnuphys.fastMCed.snr;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.fastMCed.view.AView;

@SuppressWarnings("serial")
public class NoisePanel extends JPanel implements ActionListener, ItemListener {

	// view being controlled
	private AView _view;

	// check whether we should display result of SNR analysis
	private JCheckBox _showSNRAnalysis;

	// check whether we display the noise algorithm masks
	private JCheckBox _showMasks;

	// if we use noise analysis, do we highlight noise?
	private JRadioButton _highlightNoise;

	// if we use noise analysis, do we hide noise?
	private JRadioButton _hideNoise;

	/**
	 * Create a panel for controlling noise display
	 * 
	 * @param view
	 */
	public NoisePanel(AView view) {
		_view = view;

		ButtonGroup bg = new ButtonGroup();

		_showSNRAnalysis = ComponentSupport.makeCheckBox("SNR analysis",
				true, true, this);
		_highlightNoise = ComponentSupport.makeRadioButton("Highlight noise",
				bg, false, true, this);
		_hideNoise = ComponentSupport.makeRadioButton("Hide noise", bg, false,
				false, this);
		_showMasks = ComponentSupport.makeCheckBox("Show masks", false, true,
				this);

		JPanel subPanel1 = new JPanel();
		subPanel1.setLayout(new FlowLayout(FlowLayout.LEFT));
		subPanel1.add(_showSNRAnalysis);
		subPanel1.add(_showMasks);

		JPanel subPanel2 = new JPanel();
		subPanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		subPanel2.add(_highlightNoise);
		subPanel2.add(_hideNoise);

		setLayout(new VerticalFlowLayout());
		add(subPanel1);
		add(subPanel2);

		checkButtons();
		setBorder(new CommonBorder("DC Noise algorithm drawing"));
	}

	/**
	 * Convenience method to see it we show the SNR analysis
	 * 
	 * @return <code>true</code> if we are to show the SNR Analysis.
	 */
	public boolean showSNRAnalysis() {
		return _showSNRAnalysis.isSelected();
	}

	/**
	 * Convenience method to see it we show the noise analysis masks
	 * 
	 * @return <code>true</code> if we are to show the masks.
	 */
	public boolean showMasks() {
		return showSNRAnalysis() && _showMasks.isSelected();
	}

	/**
	 * Convenience method to see if we hide noise.
	 * 
	 * @return <code>true</code> if we are to hide noise. This is only relevant
	 *         if we are using the noise analysis.
	 */
	public boolean hideNoise() {
		return showSNRAnalysis() && _hideNoise.isSelected();
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		checkButtons();
		_view.getContainer().refresh();
	}

	// check all the button states
	private void checkButtons() {
		boolean useSNR = showSNRAnalysis();
		_showMasks.setEnabled(useSNR);
		_hideNoise.setEnabled(useSNR);
		_highlightNoise.setEnabled(useSNR);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		_view.getContainer().refresh();
	}

}