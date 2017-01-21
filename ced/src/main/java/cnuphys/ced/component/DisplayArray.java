package cnuphys.ced.component;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;

import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedView;

/**
 * Create the display flags based on bits. This allows for a common appearance
 * across all views
 * 
 * @author DHeddle
 * 
 */

@SuppressWarnings("serial")
public class DisplayArray extends CheckBoxArray implements ItemListener {

	/** property for inner outer */
	public static final String SHOWINNER_PROPERTY = "DisplayInner";

	/** Label and access to the monte carlo truth checkbox */
	public static final String MCTRUTH_LABEL = "GEMC Truth";

	/** Label and access to the single event button */
	public static final String SINGLEEVENT_LABEL = "Single";

	/** Label and access to the accumulated button */
	public static final String ACCUMULATED_LABEL = "Accumulated";

	/** Label and access to the accumulated button */
	public static final String LOG_ACCUMULATED_LABEL = "Log Accum.";

	/** Tag and access to the accumulated button group */
	public static final String ACCUMULATED_BUTTONGROUP = "AccumulatedButtonGroup";

	/** Tag and access to the inner/outer button group */
	public static final String INNEROUTER_BUTTONGROUP = "InnerOuterButtonGroup";

	/** Tag and access to the BST ispoint/cross button group */
	public static final String MIDPOINTCROSS_BUTTONGROUP = "MidpointCrossButtonGroup";

	/** Label for inner plane of ec */
	public static final String INNER_LABEL = "Inner Plane";

	/** Label for outer plane of ec */
	public static final String OUTER_LABEL = "Outer Plane";

	/** Label for u strips */
	public static final String U_LABEL = "U";

	/** Label for v strips */
	public static final String V_LABEL = "V";

	/** Label for w strips */
	public static final String W_LABEL = "W";

	/** Distance scale label */
	public static final String SCALE_LABEL = "Scale";

	/** BST Hits as points */
	public static final String MIDPOINTS_LABEL = "Strip Midpoints";

	/** BST Hits as crosses */
	public static final String COSMIC_LABEL = "Cosmic Tracks";
	
	private static final Color _buttonColor = X11Colors.getX11Color("Dark Red");

	/** Label for dc HB reconstructed crosses button */
	private static final String DC_HB_CROSS_LABEL = "HB Crosses";

	/** Label for dc TB reconstructed crosses button */
	private static final String DC_TB_CROSS_LABEL = "TB Crosses";

	/** Label for dc TB reconstructed doca button */
	private static final String DC_TB_DOCA_LABEL = "TB Doca";

	/** Label for dc TB reconstructed segment button */
	private static final String DC_HB_SEGMENT_LABEL = "HB Segments";

	/** Label for dc TB reconstructed segment button */
	private static final String DC_TB_SEGMENT_LABEL = "TB Segments";

	/** Label for bst reconstructed crosses button */
	private static final String RECONS_CROSS_LABEL = "BST/BMT Crosses";

	/** Label for ftof reconstructed hits button */
	private static final String FTOFRECONS_HIT_LABEL = "FTOF Hits";
	
	/** Global show HB */
	private static final String GLOBAL_HB_LABEL = "HB Data";

	/** Global show TB */
	private static final String GLOBAL_TB_LABEL = "TB Data";
	
	// controls whether any HB data displayed
	private AbstractButton _showHBButton;

	// controls whether any TB data displayed
	private AbstractButton _showTBButton;

	// controls whether dc HB reconstructed crosses are displayed
	private AbstractButton _dcHBCrossesButton;

	// controls whether dc TB reconstructed crosses are displayed
	private AbstractButton _dcTBCrossesButton;

	// controls whether dc TB reconstructed doca are displayed
	private AbstractButton _dcTBDocaButton;

	// controls whether dc TB reconstructed segments are displayed
	private AbstractButton _dcTBSegmentButton;
	
	// controls whether dc HB reconstructed segments are displayed
	private AbstractButton _dcHBSegmentButton;

	// controls whether bst reconstructed crosses are displayed
	private AbstractButton _reconsBSTCrossButton;

	// controls whether FTOF reconstructed hits are displayed
	private AbstractButton _reconsFTOFHitButton;


	// controls mc truth is displayed (when available)
	private AbstractButton _mcTruthButton;

	// controls cosmic tracks in BST (when available)
	private AbstractButton _cosmicButton;

	// controls whether distance scale displayed
	private AbstractButton _showScaleButton;

	// controls whether single events are displayed
	private AbstractButton _singleEventButton;

	// controls whether accumulated hits are displayed
	private AbstractButton _accumulatedButton;

	// controls whether log of accumulated hits are displayed
	private AbstractButton _log_accumulatedButton;

	// controls whether inner plane displayed for ec
	private AbstractButton _innerButton;

	// controls whether inner plane displayed for ec
	private AbstractButton _outerButton;

	// controls whether hits in BST are shown as midpoints of strips
	private AbstractButton _stripMidpointsButton;

	// controls whether we draw u strips
	private AbstractButton _uButton;

	// controls whether we draw v strips
	private AbstractButton _vButton;

	// controls whether we draw w strips
	private AbstractButton _wButton;

	// the parent view
	private CedView _view;

	/**
	 * Create a display flag array. This constructor produces a two column
	 * array.
	 * 
	 * @param view
	 *            the parent view
	 * @param bits
	 *            controls what flags are added
	 */
	public DisplayArray(CedView view, int bits, int nc, int hgap) {
		super(nc, hgap, 0);
		_view = view;

		boolean show_scale = true;
		boolean show_mctruth = true;
		boolean show_u = true;
		boolean show_v = true;
		boolean show_w = true;
		boolean show_cosmic = true;
		
		//recons flags
		boolean showDChbCrosses = true;
		boolean showDCtbCrosses = true;
		boolean showDCtbDoca = true;
		boolean showBSTreconsCrosses = true;
		boolean showFTOFreconsHits = true;
		
		boolean showDChbSegs = true;
		boolean showDCtbSegs = true;

		boolean showHB = true;
		boolean showTB = true;

		// innerouter?
		if (Bits.checkBit(bits, DisplayBits.INNEROUTER)) {
			_innerButton = add(INNER_LABEL, true, true, INNEROUTER_BUTTONGROUP,
					this, X11Colors.getX11Color("teal")).getCheckBox();

			_outerButton = add(OUTER_LABEL, false, true,
					INNEROUTER_BUTTONGROUP, this, X11Colors.getX11Color("teal"))
					.getCheckBox();
		}

		if (Bits.checkBit(bits, DisplayBits.UVWSTRIPS)) {
			_uButton = add(U_LABEL, show_u, true, this, Color.black)
					.getCheckBox();
			_vButton = add(V_LABEL, show_v, true, this, Color.black)
					.getCheckBox();
			_wButton = add(W_LABEL, show_w, true, this, Color.black)
					.getCheckBox();
		}

		// accumulation?
		if (Bits.checkBit(bits, DisplayBits.ACCUMULATION)) {
			_singleEventButton = add(SINGLEEVENT_LABEL,
					view.isSingleEventMode(), true,
					ACCUMULATED_BUTTONGROUP, this,
					X11Colors.getX11Color("teal")).getCheckBox();

			_accumulatedButton = add(ACCUMULATED_LABEL,
					view.isSimpleAccumulatedMode(), true,
					ACCUMULATED_BUTTONGROUP, this,
					X11Colors.getX11Color("teal")).getCheckBox();

			_log_accumulatedButton = add(LOG_ACCUMULATED_LABEL,
					view.isLogAccumulatedMode(), true,
					ACCUMULATED_BUTTONGROUP, this,
					X11Colors.getX11Color("teal")).getCheckBox();

		}

		// BST hits as midpoints of hit strips
		if (Bits.checkBit(bits, DisplayBits.BSTHITS)) {
			_stripMidpointsButton = add(MIDPOINTS_LABEL, false, true,
					this, X11Colors.getX11Color("maroon")).getCheckBox();

		}

		// display mc truth?
		if (Bits.checkBit(bits, DisplayBits.MCTRUTH)) {
			_mcTruthButton = add(MCTRUTH_LABEL, show_mctruth, true, this,
					Color.black).getCheckBox();
		}

		// cosmics?
		if (Bits.checkBit(bits, DisplayBits.COSMICS)) {
			_cosmicButton = add(COSMIC_LABEL, show_cosmic, true, this,
					Color.black).getCheckBox();
		}

		// display scale?
		if (Bits.checkBit(bits, DisplayBits.SCALE)) {
			_showScaleButton = add(SCALE_LABEL, show_scale, true, this,
					Color.black).getCheckBox();
		}
		
		// global hit based data
		if (Bits.checkBit(bits, DisplayBits.GLOBAL_HB)) {
			_showHBButton = add(GLOBAL_HB_LABEL, showHB, true,
					this, _buttonColor).getCheckBox();
		}

		// global times based data
		if (Bits.checkBit(bits, DisplayBits.GLOBAL_TB)) {
			_showTBButton = add(GLOBAL_TB_LABEL, showTB, true,
					this, _buttonColor).getCheckBox();
		}
		
		// dc hit based reonstructed crosses?
		if (Bits.checkBit(bits, DisplayBits.DC_HB_RECONS_CROSSES)) {
			_dcHBCrossesButton = add(DC_HB_CROSS_LABEL, showDChbCrosses, true,
					this, _buttonColor).getCheckBox();
		}

		// dc time based based reonstructed crosses?
		if (Bits.checkBit(bits, DisplayBits.DC_TB_RECONS_CROSSES)) {
			_dcTBCrossesButton = add(DC_TB_CROSS_LABEL, showDCtbCrosses, true,
					this, _buttonColor).getCheckBox();
		}
		
		// dc time based based reonstructed doca?
		if (Bits.checkBit(bits, DisplayBits.DC_TB_RECONS_DOCA)) {
			_dcTBDocaButton = add(DC_TB_DOCA_LABEL, showDCtbDoca, true,
					this, _buttonColor).getCheckBox();
		}
		
		// hit based based segments?
		if (Bits.checkBit(bits, DisplayBits.DC_HB_RECONS_SEGMENTS)) {
			_dcHBSegmentButton = add(DC_HB_SEGMENT_LABEL, showDChbSegs, true,
					this, _buttonColor).getCheckBox();
		}

		// time based based segments?
		if (Bits.checkBit(bits, DisplayBits.DC_TB_RECONS_SEGMENTS)) {
			_dcTBSegmentButton = add(DC_TB_SEGMENT_LABEL, showDCtbSegs, true,
					this, _buttonColor).getCheckBox();
		}


		if (Bits.checkBit(bits, DisplayBits.BSTRECONS_CROSSES)) {
			_reconsBSTCrossButton = add(RECONS_CROSS_LABEL,
					showBSTreconsCrosses, true, this, _buttonColor)
					.getCheckBox();
		}

		// ftof reconstructed hits
		if (Bits.checkBit(bits, DisplayBits.FTOFHITS)) {
			_reconsFTOFHitButton = add(FTOFRECONS_HIT_LABEL,
					showFTOFreconsHits, true, this, _buttonColor).getCheckBox();
		}


	//	setBorder(new CommonBorder("Display Options"));
	}

	/**
	 * A button has been clicked
	 * 
	 * @param e
	 *            the causal event
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		AbstractButton button = (AbstractButton) e.getSource();
		if (button == _singleEventButton) {
			_view.setMode(CedView.Mode.SINGLE_EVENT);
		}
		else if (button == _accumulatedButton) {
			_view.setMode(CedView.Mode.SIMPLEACCUMULATED);
		}
		else if (button == _log_accumulatedButton) {
			_view.setMode(CedView.Mode.LOGACCUMULATED);
		}
		else if (button == _innerButton) {
			_view.setBooleanProperty(SHOWINNER_PROPERTY, true);
		}
		else if (button == _outerButton) {
			_view.setBooleanProperty(SHOWINNER_PROPERTY, false);
		}

		// repaint the view
		if (_view != null) {
			_view.getContainer().refresh();
		}
	}

	/**
	 * Convenience method to see it we show the montecarlo truth.
	 * 
	 * @return <code>true</code> if we are to show the montecarlo truth, if it
	 *         is available.
	 */
	public boolean showMcTruth() {
		return (_mcTruthButton != null) && _mcTruthButton.isSelected();
	}
	
	/**
	 * Convenience method to see it we show thestrip midpoints.
	 * 
	 * @return <code>true</code> if we are to show the strip midpoints
	 * for hit strips.
	 */
	public boolean showStripMidpoints() {
		return (_stripMidpointsButton != null) && _stripMidpointsButton.isSelected();
	}

	/**
	 * Convenience method to see it we show the cosmic tracks.
	 * 
	 * @return <code>true</code> if we are to show the cosmic tracks, if it is
	 *         available.
	 */
	public boolean showCosmics() {
		return (_cosmicButton != null) && _cosmicButton.isSelected();
	}

	/**
	 * Convenience method to see if scale is displayed
	 * 
	 * @return <code>true</code> if we are to display the distance scale
	 */
	public boolean showScale() {
		return _showScaleButton == null ? false : _showScaleButton.isSelected();
	}

	/**
	 * Convenience method to see if u strips displayed
	 * 
	 * @return <code>true</code> if we are to display u strips
	 */
	public boolean showUStrips() {
		return _uButton == null ? false : _uButton.isSelected();
	}

	/**
	 * Convenience method to see if v strips displayed
	 * 
	 * @return <code>true</code> if we are to display v strips
	 */
	public boolean showVStrips() {
		return _vButton == null ? false : _vButton.isSelected();
	}

	/**
	 * Convenience method to see if w strips displayed
	 * 
	 * @return <code>true</code> if we are to display w strips
	 */
	public boolean showWStrips() {
		return _wButton == null ? false : _wButton.isSelected();
	}
	
	/**
	 * Convenience method to see if we show the ftof reconstructed hits.
	 * 
	 * @return <code>true</code> if we are to show dc hb reconstructed hits.
	 */
	public boolean showFTOFHits() {
		return (_reconsFTOFHitButton != null)
				&& _reconsFTOFHitButton.isSelected();
	}
	
	/**
	 * Convenience method global hit based display
	 * 
	 * @return <code>true</code> if we are to show hb globally
	 */
	public boolean showHB() {
		return (_showHBButton != null) && _showHBButton.isSelected();
	}

	/**
	 * Convenience method global time based display
	 * 
	 * @return <code>true</code> if we are to show tb globally
	 */
	public boolean showTB() {
		return (_showTBButton != null) && _showTBButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc hb reconstructed crosses.
	 * 
	 * @return <code>true</code> if we are to show dc hb reconstructed crosses.
	 */
	public boolean showDChbCrosses() {
		return showHB() && (_dcHBCrossesButton != null) && _dcHBCrossesButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc tb reconstructed crosses.
	 * 
	 * @return <code>true</code> if we are to show dc tb reconstructed crosses.
	 */
	public boolean showDCtbCrosses() {
		return showTB() && (_dcTBCrossesButton != null) && _dcTBCrossesButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc tb reconstructed crosses.
	 * 
	 * @return <code>true</code> if we are to show dc tb reconstructed crosses.
	 */
	public boolean showDCtbDoca() {
		return showTB() && (_dcTBDocaButton != null) && _dcTBDocaButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc hb reconstructed segments.
	 * 
	 * @return <code>true</code> if we are to show dc hb reconstructed crosses.
	 */
	public boolean showDChbSegments() {
		return showHB() && (_dcHBSegmentButton != null) && _dcHBSegmentButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the dc tb reconstructed segments.
	 * 
	 * @return <code>true</code> if we are to show dc tb reconstructed crosses.
	 */
	public boolean showDCtbSegments() {
		return showTB() && (_dcTBSegmentButton != null) && _dcTBSegmentButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the bst reconstructed crosses.
	 * 
	 * @return <code>true</code> if we are to show bst reconstructed crosses.
	 */
	public boolean showBSTReconsCrosses() {
		return (_reconsBSTCrossButton != null)
				&& _reconsBSTCrossButton.isSelected();
	}

}
