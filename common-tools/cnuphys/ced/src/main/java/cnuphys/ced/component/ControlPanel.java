package cnuphys.ced.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.feedback.FeedbackPane;
import cnuphys.bCNU.graphics.colorscale.ColorModelLegend;
import cnuphys.bCNU.graphics.colorscale.ColorModelPanel;
import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.alldc.AllDCDisplayPanel;
import cnuphys.ced.cedview.central.CentralZView;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.item.MagFieldItem;

@SuppressWarnings("serial")
/**
 * This is the control panel that sits on the side of the view
 * 
 * @author heddle
 *
 */
public class ControlPanel extends JPanel implements ChangeListener {
	private static final int SLIDERWIDTH = 210;
	private static final int FEEDBACKWIDTH = 220;

	// widths of some optional widgets
	private static final int FULLWIDTH = 220;

	/** Bit used to create a display array */
	public static final int DISPLAYARRAY = 01;

	/** Bit used to create a phi slider */
	public static final int PHISLIDER = 02;

	/** Bit used to create a torus legend */
	public static final int FIELDLEGEND = 04;

	/** Bit used to create a Lund particle legend */
	public static final int DRAWLEGEND = 010;

	/** Bit used to create a target slider */
	public static final int TARGETSLIDER = 020;

	/** Bit used to create a feedback pane */
	public static final int FEEDBACK = 040;

	/** Bit used to create an accumulation legend */
	public static final int ACCUMULATIONLEGEND = 0100;

	/** Bit used to create an accumulation legend */
	public static final int NOISECONTROL = 0200;

	/** Bit used to make phi slider have full 360 degree range */
	public static final int PHI_SLIDER_BIG = 0400;
	
	/** and adc threshold slider */
	public static final int ADCTHRESHOLDSLIDER = 01000;
	
	/** all dc display panel */
	public static final int ALLDCDISPLAYPANEL = 02000;

	// the view parent
	private CedView _view;

	// the display array
	private DisplayArray _displayArray;

	// magnetic field display
	private MagFieldDisplayArray _magFieldDisplayArray;

	// control the nominal target z
	private JSlider _targetSlider;

	// control the value of phi
	private JSlider _phiSlider;
	
	
	//control threshold value of adc to display
	private JSlider _adcThresholdSlider;
	private CommonBorder _adcThresholdBorder;

	// the feedback pane
	private FeedbackPane _feedbackPane;

	// noise display panel
	private NoisePanel _noisePanel;

	// colums and gaps for display array
	private int _nc;
	private int _hgap;

	// color model panel for accumulation
	private ColorModelPanel _colorPanel;
	
	//only for all dc view
	private AllDCDisplayPanel _allDCDisplayPanel;
	
	//the tabbed pane
	private JTabbedPane _tabbedPane;

	/**
	 * Create a view control panel
	 * 
	 * @param container        the parent container
	 * @param controlPanelBits the bits fo which components are added
	 * @param displayArrayBits the bits for which display flags are added to the
	 *                         display array.
	 */
	public ControlPanel(CedView view, int controlPanelBits, int displayArrayBits, int nc, int hgap) {
		_view = view;

		_nc = nc;
		_hgap = hgap;

		setLayout(new BorderLayout(0, 2));

		Box box = Box.createVerticalBox();

		// add the tabbed pane
		_tabbedPane = addTabbedPane(view, controlPanelBits, displayArrayBits);
		box.add(_tabbedPane);

		// feedback
		if (Bits.checkBit(controlPanelBits, FEEDBACK)) {
			_feedbackPane = new FeedbackPane(FEEDBACKWIDTH);
			view.getContainer().setFeedbackPane(_feedbackPane);
		}

		add(box, BorderLayout.NORTH);

		add(_feedbackPane, BorderLayout.CENTER);
		validate();
	}

	/**
	 * Add a component to the south, below the feedback.
	 * 
	 * @param component the added component
	 */
	public void addSouth(JComponent component) {
		add(component, BorderLayout.SOUTH);
	}


	/**
	 * Get the color scale model if there is one.
	 * 
	 * @return the color scale model for accumulation, etc.
	 */
	public ColorScaleModel getColorScaleModel() {
		if (_colorPanel != null) {
			return _colorPanel.getColorScaleModel();
		}

		return null;
	}
	
	/**
	 * Get the tabbed pane for customization
	 * @return the tabbed pane
	 */
	public JTabbedPane getTabbedPane() {
		return _tabbedPane;
	}

	// use a tabbed pane to save space
	private JTabbedPane addTabbedPane(CedView view, int controlPanelBits, int displayArrayBits) {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFont(Fonts.smallFont);

		// dc noise control?
		if (Bits.checkBit(controlPanelBits, NOISECONTROL)) {
			_noisePanel = new NoisePanel(_view);
		}

		// mag field

		JPanel magFieldPanel = null;
		if (Bits.checkBit(displayArrayBits, DisplayBits.MAGFIELD)) {
			magFieldPanel = new JPanel();

			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(2, 1, 0, 8));

			magFieldPanel.setLayout(new BorderLayout(4, 4));
			_magFieldDisplayArray = new MagFieldDisplayArray(_view, displayArrayBits);
			magFieldPanel.add(_magFieldDisplayArray, BorderLayout.NORTH);

			if (Bits.checkBit(controlPanelBits, FIELDLEGEND)) {
				ColorModelLegend legend;
				int gap = 30;
				if (_view instanceof CentralZView) {
					legend = new ColorModelLegend(MagFieldItem._colorScaleModelSolenoid, FULLWIDTH - 2 * gap,
							"Field (T)", gap);
				} else {
					legend = new ColorModelLegend(MagFieldItem._colorScaleModelTorus, FULLWIDTH - 2 * gap, "Field (T)",
							gap);
				}

				ColorModelLegend glegend = new ColorModelLegend(MagFieldItem._colorScaleModelGradient,
						FULLWIDTH - 2 * gap, "Gradient Magnitude (T/m)", gap);

				panel.add(legend);
				panel.add(glegend);
				magFieldPanel.add(panel, BorderLayout.SOUTH);
			}

		}

		// options
		if ((Bits.checkBit(controlPanelBits, DISPLAYARRAY)) && (displayArrayBits != 0)) {
			_displayArray = new DisplayArray(_view, displayArrayBits, _nc, _hgap);
		}
		
		// phi slider
		
		Box phiSlider = null;
		if (Bits.checkBit(controlPanelBits, PHISLIDER)) {
			boolean isBig = Bits.checkBit(controlPanelBits, PHI_SLIDER_BIG);
			phiSlider = createPhiSlider(isBig);
		}

		// legend
		JPanel legend = null;
		if (Bits.checkBit(controlPanelBits, DRAWLEGEND)) {
			legend = DrawingLegend.getLegendPanel(_view);
		}


		if (_displayArray != null) {
			
			JPanel sp = new JPanel();
			sp.setLayout(new BoxLayout(sp, BoxLayout.Y_AXIS));
			
			
			_displayArray.setBorder(new CommonBorder("Visibility"));
			sp.add(_displayArray);
			
			if (Bits.checkBit(controlPanelBits, ALLDCDISPLAYPANEL)) {
				_allDCDisplayPanel = new AllDCDisplayPanel(_view);
				sp.add(_allDCDisplayPanel);
			}
			
			// accumulation
			if (Bits.checkBit(controlPanelBits, ACCUMULATIONLEGEND)) {
				_colorPanel = new ColorModelPanel(_view, AccumulationManager.colorScaleModel, 160,
						"Relative Accumulation or ADC Value", 10, _view.getMedianSetting(), true, true);

				sp.add(_colorPanel);
			}
			
			//adc threshold
			if (Bits.checkBit(controlPanelBits, ADCTHRESHOLDSLIDER)) {
				sp.add(createAdcThresholdSlider());
			}

			
			tabbedPane.add(sp, "display");
		}
		
		if (phiSlider != null) {
			tabbedPane.add(phiSlider, "phi");
		}


		if (legend != null) {
			tabbedPane.add(legend, "legend");
		}

		if (magFieldPanel != null) {
			tabbedPane.add(magFieldPanel, "field");
		}

		if (_noisePanel != null) {
			tabbedPane.add(_noisePanel, "noise");
		}

		return tabbedPane;
	}

//	/**
//	 * Get the slider for the accumulation legend
//	 * 
//	 * @return the slider
//	 */
//	public JSlider getAccumulationSlider() {
//		return (_colorPanel == null) ? null : _colorPanel.getSlider();
//	}

//	/**
//	 * Create the slider used to control the target z
//	 * 
//	 * @return the slider used to control the target z
//	 */
//	private Box createTargetSlider() {
//		Box box = Box.createVerticalBox();
//
//		int targ_min = -200;
//		int targ_max = 200;
//		int targ_init = 0;
//
//		_targetSlider = new JSlider(SwingConstants.HORIZONTAL, targ_min, targ_max, targ_init);
//
//		_targetSlider.setMajorTickSpacing(100);
//		_targetSlider.setMinorTickSpacing(10);
//		_targetSlider.setPaintTicks(true);
//		_targetSlider.setPaintLabels(true);
//		_targetSlider.setFont(Fonts.tinyFont);
//		_targetSlider.setFocusable(false); // so ugly focus border not drawn
//
//		if (_view instanceof ChangeListener) {
//			_targetSlider.addChangeListener((ChangeListener) _view);
//		}
//
//		Dimension d = _targetSlider.getPreferredSize();
//		d.width = SLIDERWIDTH;
//		_targetSlider.setPreferredSize(d);
//		box.add(_targetSlider);
//
//		box.setBorder(new CommonBorder("Target Z (cm)"));
//		return box;
//	}

	
	/**
	 * Create the slider used to control the target z
	 * 
	 * @return the slider used to control the target z
	 */
	private Box createAdcThresholdSlider() {
		Box box = Box.createVerticalBox();

		int slider_min = 0;
		int slider_max = 1000;
		int slider_init = _view.getAdcThresholdDefault();

		_adcThresholdSlider = new JSlider(SwingConstants.HORIZONTAL, slider_min, slider_max, slider_init);

		_adcThresholdSlider.setMajorTickSpacing(250);
		_adcThresholdSlider.setMinorTickSpacing(50);

		
		_adcThresholdSlider.setPaintTicks(true);
		_adcThresholdSlider.setPaintLabels(true);
		_adcThresholdSlider.setFont(Fonts.tinyFont);
		_adcThresholdSlider.setFocusable(false); // so ugly focus border not drawn

		if (_view instanceof ChangeListener) {
			_adcThresholdSlider.addChangeListener((ChangeListener) _view);
		}

		Dimension d = _adcThresholdSlider.getPreferredSize();
		d.width = SLIDERWIDTH;
		_adcThresholdSlider.setPreferredSize(d);
		box.add(_adcThresholdSlider);

		_adcThresholdBorder = new CommonBorder("ADC Display Threshold (" + _view.getAdcThresholdDefault() + ")");
		box.setBorder(_adcThresholdBorder);
		return box;
	}
	
	
	/**
	 * Create the slider used to control the target z
	 * 
	 * @return the slider used to control the target z
	 */
	private Box createPhiSlider(boolean isBig) {
		Box box = Box.createVerticalBox();

		int phi_min = -25;
		int phi_max = 25;
		int phi_init = 0;
		if (isBig) {
			phi_min = -180;
			phi_max = 180;
			phi_init = 90;
		}

		_phiSlider = new JSlider(SwingConstants.HORIZONTAL, phi_min, phi_max, phi_init);
		if (!isBig) {
			_phiSlider.setMajorTickSpacing(5);
			_phiSlider.setMinorTickSpacing(0);
		} else {
			_phiSlider.setMajorTickSpacing(60);
			_phiSlider.setMinorTickSpacing(10);
		}
		_phiSlider.setPaintTicks(true);
		_phiSlider.setPaintLabels(true);
		_phiSlider.setFont(Fonts.tinyFont);
		_phiSlider.setFocusable(false); // so ugly focus border not drawn

		if (_view instanceof ChangeListener) {
			_phiSlider.addChangeListener((ChangeListener) _view);
		}

		Dimension d = _phiSlider.getPreferredSize();
		d.width = SLIDERWIDTH;
		_phiSlider.setPreferredSize(d);
		box.add(_phiSlider);

		if (isBig) {
			box.setBorder(new CommonBorder(
					UnicodeSupport.SMALL_PHI + " (deg)"));
		} else {
			box.setBorder(new CommonBorder(
					UnicodeSupport.CAPITAL_DELTA + UnicodeSupport.SMALL_PHI + " relative to midplane (deg)"));
		}
		return box;
	}
	
	/**
	 * Get the slider for adc threshold.
	 * 
	 * @return the slider for adc threshold.
	 */
	public JSlider getAdcThresholdSlider() {
		return _adcThresholdSlider;
	}

	/**
	 * Get the adc threshold border so we can adjust the title.
	 * @return the adc threshold border
	 */
	public CommonBorder getAdcThresholdBorder() {
		return _adcThresholdBorder;
	}

	/**
	 * Get the slider for target position.
	 * 
	 * @return the slider for target position.
	 */
	public JSlider getTargetSlider() {
		return _targetSlider;
	}

	/**
	 * Get the slider for the relative phi.
	 * 
	 * @return the slider for the relative phi.
	 */
	public JSlider getPhiSlider() {
		return _phiSlider;
	}

	/**
	 * Get the display options array
	 * 
	 * @return the display options array
	 */
	public DisplayArray getDisplayArray() {
		return _displayArray;
	}

	/**
	 * Get the mag field options array
	 * 
	 * @return the mag fielddisplay options array
	 */
	public MagFieldDisplayArray getMagFieldDisplayArray() {
		return _magFieldDisplayArray;
	}

	/**
	 * Convenience method to see it we show results of the noise analysis
	 * 
	 * @return <code>true</code> if we are to show results of the noise analysis
	 */
	public boolean showNoiseAnalysis() {
		if (_noisePanel == null) {
			return false;
		}
		return _noisePanel.showNoiseAnalysis();
	}

	/**
	 * Convenience method to see it we show the segment masks
	 * 
	 * @return <code>true</code> if we are to show the masks.
	 */
	public boolean showMasks() {
		if (_noisePanel == null) {
			return false;
		}
		return _noisePanel.showMasks();
	}

	/**
	 * Convenience method to see it we show the scale
	 * 
	 * @return <code>true</code> if we are to show the scale.
	 */
	public boolean showScale() {
		if (_displayArray == null) {
			return false;
		}
		return _displayArray.showScale();
	}

	/**
	 * Convenience method to see it we hide the noise hits
	 * 
	 * @return <code>true</code> if we are to hide the noise hits
	 */
	public boolean hideNoise() {
		if (_noisePanel == null) {
			return true;
		}

		return _noisePanel.hideNoise();
	}
	
	/**
	 * Get the display panel used by the all dc view
	 * @return the display panel used by the all dc view
	 */
	public AllDCDisplayPanel getAllDCDisplayPanel() {
		return _allDCDisplayPanel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (_colorPanel != null) {
//			JSlider slider = _colorPanel.getSlider();
//
//			if (source == slider) {
//				double val = _colorPanel.getValue();
//				_view.setMedianSetting(val);
//				_view.refresh();
//			}
		}

	}

}
