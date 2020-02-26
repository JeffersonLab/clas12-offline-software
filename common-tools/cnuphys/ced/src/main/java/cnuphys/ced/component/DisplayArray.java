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
	public static final String MCTRUTH_LABEL = "Truth";

	/** Label and access to the single event button */
	public static final String SINGLEEVENT_LABEL = "Single";

	/** Label and access to the accumulated button */
	public static final String ACCUMULATED_LABEL = "Accum.";

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
	//public static final String SCALE_LABEL = "Scale";

	/** BST Hits as crosses */
	public static final String COSMIC_LABEL = "Cosmic Tracks";
	
	private static final Color _buttonColor = X11Colors.getX11Color("Dark Red");

	/** Label for reconstructed crosses*/
	private static final String CROSS_LABEL = "Crosses";

	/** Label for dc HB Hits button */
	private static final String DC_HIT_LABEL = "DC Recon Hits";

	/** Label for dc reconstructed segments button */
	private static final String SEGMENT_LABEL = "Segments";

	/** Label for reconstructed hits (other than dc) button */
	private static final String RECON_HIT_LABEL = "Recon Hits";
	
	/** Label for reconstructed clusters button */
	public static final String CLUSTER_LABEL = "Clusters";
		
	/** Label for reconstructed fmt crosses button */
	public static final String FMTCROSS_LABEL = "FMT Crosses";

	/** Global show HB */
	private static final String GLOBAL_HB_LABEL = "HB Data";

	/** Global show TB */
	private static final String GLOBAL_TB_LABEL = "TB Data";
	
	/** Global show ADC hits */
	private static final String GLOBAL_ADC_HIT_LABEL = "ADC Hits";
	
	/** Label for reconstructed CVT Tracks */
	private static final String CVT_TRACK_LABEL = "CVT Tracks";


	
	// controls whether any HB data displayed
	private AbstractButton _showHBButton;

	// controls whether any TB data displayed
	private AbstractButton _showTBButton;

	// controls whether dc reconstructed Hits are displayed
	private AbstractButton _dcHitsButton;

	// controls whether reconstructed segments are displayed
	private AbstractButton _segmentButton;
	
	// controls whether reconstructed crosses are displayed
	private AbstractButton _crossButton;

	// controls whether reconstructed hits (not DC) are displayed
	private AbstractButton _reconHitButton;
	
	// controls whether ADC hits 
	private AbstractButton _adcHitButton;
	
	// controls display od cvt reconstructed tracks
	private AbstractButton _cvtTrackButton;
	
	// controls whether reconstructed clusters are displayed
	private AbstractButton _clusterButton;
	
	// controls whether reconstructed fmt crosses are displayed
	private AbstractButton _fmtCrossButton;

	// controls mc truth is displayed (when available)
	private AbstractButton _mcTruthButton;

	// controls cosmic tracks in BST (when available)
	private AbstractButton _cosmicButton;

	// controls whether distance scale displayed
	//private AbstractButton _showScaleButton;

	// controls whether single events are displayed
	private AbstractButton _singleEventButton;

	// controls whether accumulated hits are displayed
	private AbstractButton _accumulatedButton;

	// controls whether inner plane displayed for ec
	private AbstractButton _innerButton;

	// controls whether inner plane displayed for ec
	private AbstractButton _outerButton;

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

		// innerouter?
		if (Bits.checkBit(bits, DisplayBits.INNEROUTER)) {
			_innerButton = add(INNER_LABEL, true, true, INNEROUTER_BUTTONGROUP,
					this, X11Colors.getX11Color("teal")).getCheckBox();

			_outerButton = add(OUTER_LABEL, false, true,
					INNEROUTER_BUTTONGROUP, this, X11Colors.getX11Color("teal"))
					.getCheckBox();
		}

		if (Bits.checkBit(bits, DisplayBits.UVWSTRIPS)) {
			_uButton = add(U_LABEL, true, true, this, Color.black)
					.getCheckBox();
			_vButton = add(V_LABEL, true, true, this, Color.black)
					.getCheckBox();
			_wButton = add(W_LABEL, true, true, this, Color.black)
					.getCheckBox();
		}

		// accumulation?
		if (Bits.checkBit(bits, DisplayBits.ACCUMULATION)) {
			_singleEventButton = add(SINGLEEVENT_LABEL,
					view.isSingleEventMode(), true,
					ACCUMULATED_BUTTONGROUP, this,
					X11Colors.getX11Color("teal")).getCheckBox();

			_accumulatedButton = add(ACCUMULATED_LABEL,
					view.isAccumulatedMode(), true,
					ACCUMULATED_BUTTONGROUP, this,
					X11Colors.getX11Color("teal")).getCheckBox();

		}

		// display mc truth?
		if (Bits.checkBit(bits, DisplayBits.MCTRUTH)) {
			_mcTruthButton = add(MCTRUTH_LABEL, true, true, this,
					Color.black).getCheckBox();
		}

		// cosmics?
		if (Bits.checkBit(bits, DisplayBits.COSMICS)) {
			_cosmicButton = add(COSMIC_LABEL, true, true, this,
					Color.black).getCheckBox();
		}

		// display scale?
//		if (Bits.checkBit(bits, DisplayBits.SCALE)) {
//			_showScaleButton = add(SCALE_LABEL, show_scale, true, this,
//					Color.black).getCheckBox();
//		}
		
		// global hit based data
		if (Bits.checkBit(bits, DisplayBits.GLOBAL_HB)) {
			_showHBButton = add(GLOBAL_HB_LABEL, true, true,
					this, _buttonColor).getCheckBox();
		}

		// global time based data
		if (Bits.checkBit(bits, DisplayBits.GLOBAL_TB)) {
			_showTBButton = add(GLOBAL_TB_LABEL, true, true,
					this, _buttonColor).getCheckBox();
		}
		
		// reonstructed crosses?
		if (Bits.checkBit(bits, DisplayBits.CROSSES)) {
			_crossButton = add(CROSS_LABEL, true, true,
					this, _buttonColor).getCheckBox();
		}

		
		// dc reonstructed hits?
		if (Bits.checkBit(bits, DisplayBits.DC_HITS)) {
			_dcHitsButton = add(DC_HIT_LABEL, true, true,
					this, _buttonColor).getCheckBox();
		}
		
		// reconstructed dc segments?
		if (Bits.checkBit(bits, DisplayBits.SEGMENTS)) {
			_segmentButton = add(SEGMENT_LABEL, true, true,
					this, _buttonColor).getCheckBox();
		}


		// other (not DC) reconstructed hits
		if (Bits.checkBit(bits, DisplayBits.RECONHITS)) {
			_reconHitButton = add(RECON_HIT_LABEL,
					true, true, this, _buttonColor).getCheckBox();
		}
		
		if (Bits.checkBit(bits, DisplayBits.CVTTRACKS)) {
			_cvtTrackButton = add(CVT_TRACK_LABEL,
					true, true, this, _buttonColor).getCheckBox();
		}

		
		// ADC hits
		if (Bits.checkBit(bits, DisplayBits.ADC_HITS)) {
			_adcHitButton = add(GLOBAL_ADC_HIT_LABEL,
					true, true, this, _buttonColor).getCheckBox();
		}

		
		// reconstructed clusters
		if (Bits.checkBit(bits, DisplayBits.CLUSTERS)) {
			_clusterButton = add(CLUSTER_LABEL,
					true, true, this, _buttonColor).getCheckBox();
		}
		
		// reconstructed clusters
		if (Bits.checkBit(bits, DisplayBits.FMTCROSSES)) {
			_fmtCrossButton = add(FMTCROSS_LABEL,
					true, true, this, _buttonColor).getCheckBox();
		}


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
			_view.setMode(CedView.Mode.ACCUMULATED);
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
		return true;
//		return _showScaleButton == null ? false : _showScaleButton.isSelected();
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
	 * Convenience method to see if we show the reconstructed clusters.
	 * 
	 * @return <code>true</code> if we are to show reconstructed clusters
	 */
	public boolean showClusters() {
		return (_clusterButton != null)
				&& _clusterButton.isSelected();
	}
	
	/**
	 * Convenience method to see if we show the reconstructed FMT Crosses.
	 * 
	 * @return <code>true</code> if we are to show reconstructed FMT Crosses
	 */
	public boolean showFMTCrosses() {
		return (_fmtCrossButton != null)
				&& _fmtCrossButton.isSelected();
	}

	
	/**
	 * Convenience method to see if we show the reconstructed hits.
	 * These are reconstructed hits except DC hits
	 * @return <code>true</code> if we are to show reconstructed hits.
	 */
	public boolean showReconHits() {
		return (_reconHitButton != null)
				&& _reconHitButton.isSelected();
	}
	
	/**
	 * Convenience method to see if we show the ADC hits.
	 * These are ADC hits 
	 * @return <code>true</code> if we are to show ADC hits.
	 */
	public boolean showADCHits() {
		return (_adcHitButton != null)
				&& _adcHitButton.isSelected();
	}
	
	
	/**
	 * Convenience method to see if we show CVT reconstructed tracks.
	 * These are ADC hits except 
	 * @return <code>true</code> if we are to show ADC hits.
	 */
	public boolean showCVTTracks() {
		return (_cvtTrackButton != null)
				&& _cvtTrackButton.isSelected();
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
	 * Convenience method to see if we show the dc HB reconstructed hits.
	 * 
	 * @return <code>true</code> if we are to show dc HB reconstructed hits.
	 */
	public boolean showDCHBHits() {
		return showHB() && (_dcHitsButton != null) && _dcHitsButton.isSelected();
	}
	
	/**
	 * Convenience method to see if we show the dc TB reconstructed hits.
	 * 
	 * @return <code>true</code> if we are to show dc TB reconstructed hits.
	 */
	public boolean showDCTBHits() {
		return showTB() && (_dcHitsButton != null) && _dcHitsButton.isSelected();
	}

	/**
	 * Convenience method to see if we show the reconstructed segments.
	 * 
	 * @return <code>true</code> if we are to showreconstructed crosses.
	 */
	public boolean showSegments() {
		return (_segmentButton != null) && _segmentButton.isSelected();
	}


	/**
	 * Convenience method to see if we show the dc hb reconstructed segments.
	 * 
	 * @return <code>true</code> if we are to show dc hb reconstructed crosses.
	 */
	public boolean showDCHBSegments() {
		return showHB() && showSegments();
	}

	/**
	 * Convenience method to see if we show the dc tb reconstructed segments.
	 * 
	 * @return <code>true</code> if we are to show dc tb reconstructed crosses.
	 */
	public boolean showDCTBSegments() {
		return showTB() && showSegments();
	}

	/**
	 * Convenience method to see if we show the  reconstructed crosses.
	 * 
	 * @return <code>true</code> if we are to show  reconstructed crosses.
	 */
	public boolean showCrosses() {
		return (_crossButton != null)
				&& _crossButton.isSelected();
	}
	
	/**
	 * Convenience method to see if we show the dc HB reconstructed crosses.
	 * 
	 * @return <code>true</code> if we are to show dc HB reconstructed crosses.
	 */
	public boolean showDCHBCrosses() {
		return showHB() && showCrosses();
	}
	
	/**
	 * Convenience method to see if we show the dc TB reconstructed crosses.
	 * 
	 * @return <code>true</code> if we are to show dc TB reconstructed crosses.
	 */
	public boolean showDCTBCrosses() {
		return showTB() && showCrosses();
	}


}
