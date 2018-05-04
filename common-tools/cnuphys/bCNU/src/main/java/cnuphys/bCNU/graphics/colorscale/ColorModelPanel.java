package cnuphys.bCNU.graphics.colorscale;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JSlider;

import cnuphys.bCNU.graphics.component.CommonBorder;

public class ColorModelPanel extends JPanel {
	
	//the legend
	private ColorModelLegend _legend;
	
	//a slider
	private JSlider _slider;
	
	private static final int MAXVAL = 1000;
	
	/**
	 * Create a panel with a slider and a color legend
	 * @param model
	 * @param desiredWidth
	 * @param name
	 * @param gap
	 */
	public ColorModelPanel(ColorScaleModel model, int desiredWidth, String name, int gap, double initRelVal) {
		setLayout(new BorderLayout(4, 4));
		
		_legend = new ColorModelLegend(model, desiredWidth, null, gap);
		_legend.setBorder(null);
		
		_slider = new JSlider(JSlider.HORIZONTAL, 0, MAXVAL, MAXVAL/2) {
			
			@Override
			public Dimension getPreferredSize() {
				Dimension sd = super.getPreferredSize();
				sd.width = _legend.getPreferredSize().width - 2*gap;
				return sd;
			}
		};
		
		setValue(initRelVal);
		
		add(_legend, BorderLayout.CENTER);
		add(_slider, BorderLayout.SOUTH);
		setBorder(new CommonBorder(name));
	}
	
	/**
	 * Get the slider component
	 * @return the slider
	 */
	public JSlider getSlider() {
		return _slider;
	}
	
	/**
	 * Set the value of the slider
	 * @param val a fractional value [0.. 1]
	 */
	public void setValue(double val) {
		int ival = (int)(MAXVAL*val);
		ival = Math.max(0,  Math.min(MAXVAL, ival));
		_slider.setValue(ival);
	}
	
	/**
	 * Get the fractional value of the slider [0..1]
	 * @return the fractional value of the slider
	 */
	public double getValue() {
		int ival = _slider.getValue();
		return ((double)ival)/((double)MAXVAL);
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}


}
