package cnuphys.splot.fit;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import cnuphys.splot.edit.VerticalFlowLayout;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.CommonBorder;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.TextFieldSlider;
import cnuphys.splot.style.EnumComboBox;

public class FitEditorPanel extends JPanel {

	// properties changed
	public static final String POLYNOMIALORDERPROP = "Polynomial Order";
	public static final String GAUSSIANNUMPROP = "Number of Gaussians";
	public static final String USERMSPROP = "Use RMS in Legend";
	public static final String STATERRPROP = "Show Stat Errors";

	private static final Font _font = Environment.getInstance().getCommonFont(10);
	private static final Font _font2 = Environment.getInstance().getCommonFont(9);

	// change fit style
	EnumComboBox _fitSelector;

	// polynomial order
	protected TextFieldSlider _polynomialOrderSelector;

	// number of gaussians
	protected TextFieldSlider _gaussianCountSelector;

	// use rms or sigma for histo
	protected JCheckBox _rmsOrCB;
	// stat error button
	protected JCheckBox _statErrorCB;
	// panel for two checkboxes
	protected JPanel _histoCBPanel;

	/**
	 * A Fit editing panel
	 */
	public FitEditorPanel() {
		addContent();
		setBorder(new CommonBorder("Fit"));
		Environment.getInstance().commonize(this, null);
	}

	// add the components
	private void addContent() {
		// setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		setLayout(new VerticalFlowLayout());

		_fitSelector = FitType.getComboBox(FitType.LINE);

		createPolySelector();
		createNumGaussSelector();
		createRMSOrSigmaCB();
		createStatErrorCB();

		_histoCBPanel = new JPanel();
		_histoCBPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 1));
		_histoCBPanel.add(_rmsOrCB);
		_histoCBPanel.add(_statErrorCB);

		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		sp.add(_fitSelector);
		add(sp);
	}

	// create the selector for the number of polygons
	private void createPolySelector() {

		String labels[] = { "0", "2", "4", "6", "8", "10", "12", "14", "16" };
		_polynomialOrderSelector = new TextFieldSlider(0, 16, 2, _font, 1, labels, 180, 40, "Polynomial Order") {

			@Override
			public double sliderValueToRealValue() {
				return getValue();
			}

			@Override
			public int realValueToSliderValue(double val) {
				return (int) val;
			}

			@Override
			public String valueString(double val) {
				return "" + getValue();
			}

			@Override
			public void valueChanged() {
				firePropertyChange(POLYNOMIALORDERPROP, -1, _polynomialOrderSelector.getValue());
			}

		};
	}

	private void createRMSOrSigmaCB() {
		_rmsOrCB = new JCheckBox("RMS in Legend", true);
		_rmsOrCB.setFont(_font2);

		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean selected = _rmsOrCB.isSelected();
				_rmsOrCB.firePropertyChange(USERMSPROP, !selected, selected);
			}

		};

		_rmsOrCB.addItemListener(il);
	}

	private void createStatErrorCB() {
		_statErrorCB = new JCheckBox("Statistical Errors", false);
		_statErrorCB.setFont(_font2);

		ItemListener il = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean selected = _statErrorCB.isSelected();
				_statErrorCB.firePropertyChange(STATERRPROP, !selected, selected);
			}

		};

		_statErrorCB.addItemListener(il);
	}

	// create the selector for the number of gaussians
	private void createNumGaussSelector() {

		String labels[] = { "1", "2", "3", "4", "5", "6" };
		_gaussianCountSelector = new TextFieldSlider(1, 6, 1, _font, 1, labels, 180, 40, "Number of Gaussians") {

			@Override
			public double sliderValueToRealValue() {
				return getValue();
			}

			@Override
			public int realValueToSliderValue(double val) {
				return (int) val;
			}

			@Override
			public String valueString(double val) {
				return "" + getValue();
			}

			@Override
			public void valueChanged() {
				firePropertyChange(GAUSSIANNUMPROP, -1, _gaussianCountSelector.getValue());
			}

		};
	}

	/**
	 * Reconfigure fit widgets based on fit type
	 * 
	 * @param curve the active curve
	 */
	public void reconfigure(DataColumn curve) {
		if (curve == null) {
			remove(_polynomialOrderSelector);
			remove(_gaussianCountSelector);
			remove(_histoCBPanel);
		}
		else {

			if (curve.isHistogram1D()) {
				HistoData hd = curve.getHistoData();
				carefulAdd(_histoCBPanel);
			}

			switch (curve.getFit().getFitType()) {

			case POLYNOMIAL:
			case ALTPOLYNOMIAL:
				remove(_gaussianCountSelector);
				carefulAdd(_polynomialOrderSelector);
				break;

			case GAUSSIANS:
				remove(_polynomialOrderSelector);
				carefulAdd(_gaussianCountSelector);
				break;

			case POLYPLUSGAUSS:
				carefulAdd(_gaussianCountSelector);
				carefulAdd(_polynomialOrderSelector);
				break;

			default:
				remove(_polynomialOrderSelector);
				remove(_gaussianCountSelector);
				break;
			}
		}

		// super.validate();
	}

	// set components enabled
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_fitSelector.setEnabled(enabled);
		_polynomialOrderSelector.setEnabled(enabled);
		_gaussianCountSelector.setEnabled(enabled);
		_rmsOrCB.setEnabled(enabled);
		_statErrorCB.setEnabled(enabled);
	}

	private void carefulAdd(Component comp) {
		for (Component c : this.getComponents()) {
			if (c == comp) {
				return;
			}
		}
		add(comp);
	}

	/**
	 * FitEditorPanel Set the choices
	 * 
	 * @param fit the new choices
	 */
	public void setFit(DataColumn curve) {

		if (curve.isHistogram1D()) {
			HistoData hd = curve.getHistoData();
			_rmsOrCB.setSelected(hd.useRmsInHistoLegend());
		}

		Fit fit = curve.getFit();
		if (fit != null) {
			_fitSelector.setSelectedItem(fit.getFitType().getName());
			_polynomialOrderSelector.setValue(fit.getPolynomialOrder());
			_gaussianCountSelector.setValue(fit.getNumGaussian());
		}
	}

	/**
	 * Further enable/disable based on fit type
	 * 
	 * @param type
	 */
	public void fitSpecific(FitType type) {
		switch (type) {
		case POLYNOMIAL:
		case ALTPOLYNOMIAL:
			_polynomialOrderSelector.setEnabled(true);
			_gaussianCountSelector.setEnabled(false);
			break;

		case GAUSSIANS:
			_polynomialOrderSelector.setEnabled(false);
			_gaussianCountSelector.setEnabled(true);
			break;

		case POLYPLUSGAUSS:
			_polynomialOrderSelector.setEnabled(true);
			_gaussianCountSelector.setEnabled(true);
			break;

		default:
			_polynomialOrderSelector.setEnabled(false);
			_gaussianCountSelector.setEnabled(false);
			break;
		}
	}

	/**
	 * Get the line selector
	 * 
	 * @return the line selector
	 */
	public EnumComboBox getFitSelector() {
		return _fitSelector;
	}

	/**
	 * Get the polynomial order slider
	 * 
	 * @return polynomial order slider
	 */
	public TextFieldSlider getPolynomialOrderSelector() {
		return _polynomialOrderSelector;
	}

	/**
	 * Get the number of gaussian slider
	 * 
	 * @return number of gaussian slider
	 */
	public TextFieldSlider getNumGaussianSelector() {
		return _gaussianCountSelector;
	}

	/**
	 * Get the rms or sigma check box
	 * 
	 * @return the rms or sigma check box
	 */
	public JCheckBox getNumRMSCheckBox() {
		return _rmsOrCB;
	}

	/**
	 * Get the draw stat error check box
	 * 
	 * @return the draw stat error checkbox
	 */
	public JCheckBox getStatErrorCheckBox() {
		return _statErrorCB;
	}

}
