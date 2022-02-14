package cnuphys.splot.style;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import cnuphys.splot.edit.ColorLabel;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.CommonBorder;
import cnuphys.splot.plot.DoubleFormat;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.TextFieldSlider;

public class StyleEditorPanel extends JPanel {

	public static final String SYMBOLSIZEPROP = "Symbol Size";
	public static final String LINEWIDTHPROP = "Line Width";

	private static final Font _font = Environment.getInstance().getCommonFont(10);

	// change symbol type
	private EnumComboBox _symbolSelector;

	// change symbol border style
	private EnumComboBox _fitLineStyleSelector;

	// symbol border color
	protected ColorLabel _borderColor;

	// fit line color
	protected ColorLabel _fitLineColor;

	// symbol color
	protected ColorLabel _symbolColor;

	// symbol size
	protected TextFieldSlider _symbolSizeSelector;

	// curve line size
	protected TextFieldSlider _lineSizeSelector;

	/**
	 * Create the stye editing panel.
	 */
	public StyleEditorPanel(DataSetType type) {
		addContent(type);
		setBorder(new CommonBorder("Style"));

		Environment.getInstance().commonize(this, null);
	}

	// add the content
	private void addContent(DataSetType type) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// symbol panel
		JPanel symPan = null;
		if (type != DataSetType.H1D) {
			symPan = flowPanel(FlowLayout.LEFT);
			_symbolSelector = SymbolType.getComboBox(SymbolType.NOSYMBOL);
			_symbolColor = new ColorLabel(null, Color.black, _font, "Symbol");
			symPan.add(_symbolSelector);
			symPan.add(_symbolColor);

			_borderColor = new ColorLabel(null, Color.black, _font, "Symbol Border");
			symPan.add(_borderColor);

		}
		else {
			symPan = flowPanel(FlowLayout.LEFT);
			_symbolColor = new ColorLabel(null, Color.black, _font, "Histogram Fill");
			symPan.add(_symbolColor);

			_borderColor = new ColorLabel(null, Color.black, _font, "Histogram Border");
			symPan.add(_borderColor);
		}

		// symbol border panel
		JPanel borderPanel = flowPanel(FlowLayout.LEFT);
		_fitLineStyleSelector = LineStyle.getComboBox(LineStyle.SOLID);

		_fitLineColor = new ColorLabel(null, Color.black, _font, "Curve Line");

		// make same size
		if (_symbolSelector != null) {
			Dimension dim1 = _symbolSelector.getPreferredSize();
			Dimension dim2 = _fitLineStyleSelector.getPreferredSize();
			int mw = Math.max(dim1.width, dim2.width);
			dim1.width = mw;
			_symbolSelector.setPreferredSize(dim1);
			_fitLineStyleSelector.setPreferredSize(dim1);
		}

		borderPanel.add(_fitLineStyleSelector);
		borderPanel.add(_fitLineColor);

		if (type != DataSetType.H1D) {
			String labels[] = { "4", "8", "12", "16", "20", "24", "28" };
			_symbolSizeSelector = new TextFieldSlider(4, 28, 8, _font, 1, labels, 180, 40, "Symbol Size") {

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
					firePropertyChange(SYMBOLSIZEPROP, -1, _symbolSizeSelector.getValue());
				}

			};
		}

		// line size
		String szLabels[] = { "0", "2", "4", "6", "8", "10", "12" };
		_lineSizeSelector = new TextFieldSlider(0, 24, 1, _font, 1, szLabels, 180, 40, "Curve Line Width") {

			@Override
			public double sliderValueToRealValue() {
				return getValue() / 2.;
			}

			@Override
			public int realValueToSliderValue(double val) {
				return (int) (2 * val);
			}

			@Override
			public String valueString(double val) {
				double dval = sliderValueToRealValue();
				return DoubleFormat.doubleFormat(dval, 1);
			}

			@Override
			public void valueChanged() {
				firePropertyChange(LINEWIDTHPROP, -1, _lineSizeSelector.getValue());
			}

		};

		if (symPan != null) {
			add(symPan);
		}
		add(borderPanel);
		if (type != DataSetType.H1D) {
			add(_symbolSizeSelector);
		}
		add(_lineSizeSelector);

	}

	private JPanel flowPanel(int constraint) {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(constraint));
		Environment.getInstance().commonize(sp, null);
		return sp;
	}

	// set components enabled
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (_symbolSelector != null) {
			_symbolSelector.setEnabled(enabled);
		}
		if (_fitLineStyleSelector != null) {
			_fitLineStyleSelector.setEnabled(enabled);
		}
		if (_symbolColor != null) {
			_symbolColor.setEnabled(enabled);
		}
		if (_borderColor != null) {
			_borderColor.setEnabled(enabled);
		}
		if (_symbolSizeSelector != null) {
			_symbolSizeSelector.setEnabled(enabled);
		}
		if (_lineSizeSelector != null) {
			_lineSizeSelector.setEnabled(enabled);
		}
		if (_fitLineColor != null) {
			_fitLineColor.setEnabled(enabled);
		}

	}

	/**
	 * Set the choices
	 * 
	 * @param style the new choices
	 */
	public void setStyle(IStyled style) {
		if (style != null) {
			if (_symbolSelector != null) {
				_symbolSelector.setSelectedItem(style.getSymbolType().getName());
			}
			if (_fitLineStyleSelector != null) {
				_fitLineStyleSelector.setSelectedItem(style.getFitLineStyle().getName());
			}
			if (_symbolColor != null) {
				_symbolColor.setColor(style.getFillColor());
			}
			if (_borderColor != null) {
				_borderColor.setColor(style.getBorderColor());
			}
			if (_fitLineColor != null) {
				_fitLineColor.setColor(style.getFitLineColor());
			}
			if (_symbolSizeSelector != null) {
				_symbolSizeSelector.setValue(style.getSymbolSize());
			}
		}
	}

	/**
	 * Get the symbols size slider
	 * 
	 * @return symbols size slider
	 */
	public TextFieldSlider getSymbolSizeSelector() {
		return _symbolSizeSelector;
	}

	/**
	 * Get the line width slider
	 * 
	 * @return line width slider
	 */
	public TextFieldSlider getLineWidthSelector() {
		return _lineSizeSelector;
	}

	/**
	 * Get the symbol selector
	 * 
	 * @return the symbol selector
	 */
	public EnumComboBox getSymbolSelector() {
		return _symbolSelector;
	}

	/**
	 * Get the border selector
	 * 
	 * @return the border selector
	 */
	public EnumComboBox getBorderSelector() {
		return _fitLineStyleSelector;
	}

	/**
	 * Get the symbol color selector
	 * 
	 * @return the symbol color selector
	 */
	public ColorLabel getSymbolColor() {
		return _symbolColor;
	}

	/**
	 * Get the line color selector
	 * 
	 * @return the line color selector
	 */
	public ColorLabel getBorderColor() {
		return _borderColor;
	}

	/**
	 * Get the fit line color selector
	 * 
	 * @return the fit line color selector
	 */
	public ColorLabel getFitLineColor() {
		return _fitLineColor;
	}
}
