package cnuphys.fastMCed.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.feedback.FeedbackPane;
import cnuphys.bCNU.graphics.colorscale.ColorModelLegend;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.fastMCed.item.MagFieldItem;
import cnuphys.fastMCed.snr.NoisePanel;

@SuppressWarnings("serial")
/**
 * This is the control panel that sits on the side of the view
 * 
 * @author heddle
 *
 */
public class ControlPanel extends JPanel  {

	private static final int SLIDERWIDTH = 210;
	private static final int FEEDBACKWIDTH = 220;

	// widths of some optional widgets
	private static final int FULLWIDTH = 220;

	/**
	 * Bit used to create a display array
	 */
	public static final int DISPLAYARRAY = 01;

	/**
	 * Bit used to create a phi slider
	 */
	public static final int PHISLIDER = 02;

	/**
	 * Bit used to create a torus legend
	 */
	public static final int FIELDLEGEND = 04;

	/**
	 * Bit used to create a target slider
	 */
	public static final int TARGETSLIDER = 020;

	/**
	 * Bit used to create a feedback pane
	 */
	public static final int FEEDBACK = 040;
	
	/**
	 * Bit used to create a noise panel
	 */
	public static final int NOISECONTROL = 0200;

	/**
	 * Bit used to make phi slider have full 360 degree range
	 */
	public static final int PHI_SLIDER_BIG = 0400;

	// the view parent
	private AView _view;

	// the display array
	private DisplayArray _displayArray;

	// magnetic field display
	private MagFieldDisplayArray _magFieldDisplayArray;

	// control the nominal target z
	private JSlider _targetSlider;

	// control the value of phi
	private JSlider _phiSlider;

	// the feedback pane
	private FeedbackPane _feedbackPane;
	
	// noise display panel
	private NoisePanel _noisePanel;

	// colums and gaps for display array
	private int _nc;
	private int _hgap;

	private static int MIN_WIDTH = 250;

	/**
	 * Create a view control panel
	 * 
	 * @param container
	 *            the parent container
	 * @param controlPanelBits
	 *            the bits fo which components are added
	 * @param displayArrayBits
	 *            the bits for which display flags are added to the display
	 *            array.
	 */
	public ControlPanel(AView view, int controlPanelBits, int displayArrayBits, int nc, int hgap) {
		_view = view;

		_nc = nc;
		_hgap = hgap;

		setLayout(new BorderLayout(0, 2));

		Box box = Box.createVerticalBox();

		box.add(addTabbedPane(view, controlPanelBits, displayArrayBits));

		// feedback
		if (Bits.checkBit(controlPanelBits, FEEDBACK)) {
			_feedbackPane = new FeedbackPane(FEEDBACKWIDTH);
			view.getContainer().setFeedbackPane(_feedbackPane);
			// box.add(_feedbackPane);
			// box.add(Box.createVerticalGlue());
		}

		add(box, BorderLayout.NORTH);

		add(_feedbackPane, BorderLayout.CENTER);

	}

	@Override
	public Dimension getMinimumSize() {
		Dimension d = super.getMinimumSize();
		if (MIN_WIDTH > d.width) {
			d.width = MIN_WIDTH;
		}
		return d;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (MIN_WIDTH > d.width) {
			d.width = MIN_WIDTH;
		}
		return d;
	}

	/**
	 * Add a component to the south, below the feedback.
	 * 
	 * @param component
	 *            the added component
	 */
	public void addSouth(JComponent component) {
		add(component, BorderLayout.SOUTH);
	}

	// use a tabbed pane to save space
	private JTabbedPane addTabbedPane(AView view, int controlPanelBits, int displayArrayBits) {
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFont(Fonts.smallFont);
		
		// dc noise control
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
				legend = new ColorModelLegend(MagFieldItem._colorScaleModelTorus, FULLWIDTH - 2 * gap, "Field (T)",
						gap);

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

		// every thing else on a "general" panel

		boolean addBasic = false;
		JPanel basic = new JPanel();
		basic.setLayout(new BorderLayout(2, 2));
		// Box box = Box.createVerticalBox();

		// target phi slider
		if (Bits.checkBit(controlPanelBits, PHISLIDER)) {
			boolean isBig = Bits.checkBit(controlPanelBits, PHI_SLIDER_BIG);
			basic.add(createPhiSlider(isBig), BorderLayout.NORTH);
			addBasic = true;
		}

		// target z slider
		if (Bits.checkBit(controlPanelBits, TARGETSLIDER)) {
			// let's disable--just takes up space
			// box.add(createTargetSlider());
		}

		// basic.add(box);

		if (_displayArray != null) {
			JPanel sp = new JPanel();
			sp.setLayout(new BorderLayout(2, 2));

			_displayArray.setBorder(new CommonBorder("Visibility"));
			sp.add(_displayArray, BorderLayout.NORTH);

			tabbedPane.add(sp, "display");
		}

		if (addBasic) {
			tabbedPane.add(basic, "basic");
		}
		
		if (magFieldPanel != null) {
			tabbedPane.add(magFieldPanel, "field");
		}

		if (_noisePanel != null) {
			tabbedPane.add(_noisePanel, "noise");
		}

		return tabbedPane;
	}

	/**
	 * Create the slider used to control the target z
	 * 
	 * @return the slider used to control the target z
	 */
	private Box createTargetSlider() {
		Box box = Box.createVerticalBox();

		int targ_min = -200;
		int targ_max = 200;
		int targ_init = 0;

		_targetSlider = new JSlider(SwingConstants.HORIZONTAL, targ_min, targ_max, targ_init);

		_targetSlider.setMajorTickSpacing(100);
		_targetSlider.setMinorTickSpacing(10);
		_targetSlider.setPaintTicks(true);
		_targetSlider.setPaintLabels(true);
		_targetSlider.setFont(Fonts.tinyFont);
		_targetSlider.setFocusable(false); // so ugly focus border not drawn

		if (_view instanceof ChangeListener) {
			_targetSlider.addChangeListener((ChangeListener) _view);
		}

		Dimension d = _targetSlider.getPreferredSize();
		d.width = SLIDERWIDTH;
		_targetSlider.setPreferredSize(d);
		box.add(_targetSlider);

		box.setBorder(new CommonBorder("Target Z (cm)"));
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

		box.setBorder(new CommonBorder(
				UnicodeSupport.CAPITAL_DELTA + UnicodeSupport.SMALL_PHI + " relative to midplane (deg)"));
		return box;
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
	 * Convenience method to see it we show results of the SNR analysis
	 * 
	 * @return <code>true</code> if we are to show results of the SNR analysis
	 */
	public boolean showSNRAnalysis() {
		return _noisePanel.showSNRAnalysis();
	}

	/**
	 * Convenience method to see it we show the segment masks
	 * 
	 * @return <code>true</code> if we are to show the masks.
	 */
	public boolean showMasks() {
		return _noisePanel.showMasks();
	}

	/**
	 * Convenience method to see it we hide the noise hits
	 * 
	 * @return <code>true</code> if we are to hide the noise hits
	 */
	public boolean hideNoise() {
		return _noisePanel.hideNoise();
	}

}