package cnuphys.bCNU.graphics.colorscale;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.view.BaseView;

public class ColorModelPanel extends JPanel implements ActionListener {

	// the legend
	private ColorModelLegend _legend;

	// a slider
	// private JSlider _slider;

	// the model
	private ColorScaleModel _model;
	private ColorScaleModel _monoModel;

	// max value of the slider
	private static final int MAXVAL = 1000;

	// radio buttons
	private JRadioButton _colorRB;
	private JRadioButton _monoRB;
	private BaseView _view;

	/**
	 * Create a panel with a slider and a color legend
	 * 
	 * @param model        the color model
	 * @param desiredWidth pixel width
	 * @param name         a name
	 * @param gap          a spacing
	 * @param initRelValue normalized value 0..1
	 */
	public ColorModelPanel(ColorScaleModel model, int desiredWidth, String name, int gap, double initRelVal) {
		this(null, model, desiredWidth, name, gap, initRelVal, false, false);
	}

	/**
	 * Create a panel with a slider and a color legend
	 * 
	 * @param model        the color model
	 * @param desiredWidth pixel width
	 * @param name         a name
	 * @param gap          a spacing
	 * @param initRelValue normalized value 0..1
	 * @param incRB        if <code>true</code> include the color monochrome radio
	 *                     buttons
	 * @param colorDefault if <code>true</code> default to color
	 */
	public ColorModelPanel(BaseView view, ColorScaleModel model, int desiredWidth, String name, int gap,
			double initRelVal, boolean includeRadioButtons, boolean colorDefault) {

		_view = view;
		_model = model;

		setLayout(new BorderLayout(4, 4));

		_legend = new ColorModelLegend(_model, desiredWidth, null, gap);
		_legend.setBorder(null);

//		_slider = new JSlider(JSlider.HORIZONTAL, 0, MAXVAL, MAXVAL/2) {
//			
//			@Override
//			public Dimension getPreferredSize() {
//				Dimension sd = super.getPreferredSize();
//				sd.width = _legend.getPreferredSize().width - 2*gap;
//				return sd;
//			}
//		};

//		setValue(initRelVal);

		if (includeRadioButtons) {
			addNorth(colorDefault);
		}

		add(_legend, BorderLayout.CENTER);
//		add(_slider, BorderLayout.SOUTH);
		setBorder(new CommonBorder(name));
	}

	private void addNorth(boolean colorDefault) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));

		ButtonGroup bg = new ButtonGroup();

		_colorRB = new JRadioButton("Color", colorDefault);
		_monoRB = new JRadioButton("Monochrome", !colorDefault);

		_colorRB.addActionListener(this);
		_monoRB.addActionListener(this);

		bg.add(_colorRB);
		bg.add(_monoRB);

		GraphicsUtilities.setSizeSmall(_colorRB);
		GraphicsUtilities.setSizeSmall(_monoRB);

		panel.add(_colorRB);
		panel.add(_monoRB);

		add(panel, BorderLayout.NORTH);
	}

//	/**
//	 * Get the slider component
//	 * @return the slider
//	 */
//	public JSlider getSlider() {
//		return _slider;
//	}
//	
//	/**
//	 * Set the value of the slider
//	 * @param val a fractional value [0.. 1]
//	 */
//	public void setValue(double val) {
//		int ival = (int)(MAXVAL*val);
//		ival = Math.max(0,  Math.min(MAXVAL, ival));
//		_slider.setValue(ival);
//	}
//	
//	/**
//	 * Get the fractional value of the slider [0..1]
//	 * @return the fractional value of the slider
//	 */
//	public double getValue() {
//		int ival = _slider.getValue();
//		return ((double)ival)/((double)MAXVAL);
//	}

//	/**
//	 * Gets the absolute value
//	 * This assumes a uniform values array
//	 * @return the absolute value
//	 */
//	public double getAbsoluteValue() {
//		double relVal = getValue();
//		double vals[] = _model.values;
//		
//		int len = vals.length;
//		
//		double min = vals[0];
//		double max = vals[len-1];
//		
//		return min + relVal*(max - min);
//	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}

	/**
	 * Main program for testing
	 * 
	 * @param arg command arguments ignored
	 */
	public static void main(String arg[]) {
		// now make the frame to display
		JFrame testFrame = new JFrame("Color Scale");

		Color colors[] = ColorScaleModel.getWeatherMapColors(4);
		double values[] = ColorScaleModel.uniformValueArray(colors, 100, 500);
		ColorScaleModel model = new ColorScaleModel("Test", values, colors);

		/**
		 * Create a panel with a slider and a color legend
		 * 
		 * @param model        the color model
		 * @param desiredWidth pixel width
		 * @param name         a name
		 * @param gap          a spacing
		 * @param initRelValue normalized value 0..1
		 */

		ColorModelPanel panel = new ColorModelPanel(null, model, 280, "Panel", 20, 0.5, true, true);

		testFrame.setLayout(new BorderLayout(8, 8));
		testFrame.add(panel, BorderLayout.CENTER);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

//		ChangeListener changeListener = new ChangeListener() {
//
//			@Override
//			public void stateChanged(ChangeEvent e) {
//				double val = panel.getAbsoluteValue();
//				System.out.println("value: " + val);
//			}
//			
//		};

//		panel.getSlider().addChangeListener(changeListener);

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

	}

	/**
	 * Get the color scale model if there is one.
	 * 
	 * @return the color scale model for accumulation, etc.
	 */
	public ColorScaleModel getColorScaleModel() {
		if (_legend != null) {
			return _legend.getColorScaleModel();
		}

		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _colorRB) {
			System.out.println("dude");
			_legend.setColorScaleModel(_model);
			if (_view != null) {
				_view.refresh();
			}

		} else if (e.getSource() == _monoRB) {
			if (_monoModel == null) {
				_monoModel = ColorScaleModel.getMonochromeModel(_model);
			}
			_legend.setColorScaleModel(_monoModel);

			if (_view != null) {
				_view.refresh();
			}
		}

	}

}
