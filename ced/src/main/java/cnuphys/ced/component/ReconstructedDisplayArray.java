package cnuphys.ced.component;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;

import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedView;

/**
 * Display flags for reconstructed data
 * 
 * @author heddle
 *
 */
public class ReconstructedDisplayArray extends CheckBoxArray implements
	ItemListener {

    private static final Color _buttonColor = X11Colors.getX11Color("Dark Red");

    /** Label for dc HB reconstructed hits button */
    private static final String DC_HB_HIT_LABEL = "DC HB Hits";

    /** Label for dc HB reconstructed crosses button */
    private static final String DC_HB_CROSS_LABEL = "DC HB Crosses";

    /** Label for dc TB reconstructed hits button */
    private static final String DC_TB_HIT_LABEL = "DC TB Hits";

    /** Label for dc TB reconstructed crosses button */
    private static final String DC_TB_CROSS_LABEL = "DC TB Crosses";

    /** Label for bst reconstructed crosses button */
    private static final String BSTRECONS_CROSS_LABEL = "SVT Crosses";

    /** Label for ftof reconstructed hits button */
    private static final String FTOFRECONS_HIT_LABEL = "FTOF Hits";

    // controls whether dc HB recons are displayed
    private AbstractButton _dcHBHitsButton;

    // controls whether dc HB reconstructed crosses are displayed
    private AbstractButton _dcHBCrossesButton;

    // controls whether dc TB recons are displayed
    private AbstractButton _dcTBHitsButton;

    // controls whether dc TB reconstructed crosses are displayed
    private AbstractButton _dcTBCrossesButton;

    // controls whether bst reconstructed crosses are displayed
    private AbstractButton _reconsBSTCrossButton;

    // controls whether FTOF reconstructed hits are displayed
    private AbstractButton _reconsFTOFHitButton;

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
    public ReconstructedDisplayArray(CedView view, int bits, int nc, int hgap) {
	super(nc, hgap, 0);
	_view = view;

	boolean showDChbCrosses = true;
	boolean showDChbHits = true;
	boolean showDCtbCrosses = true;
	boolean showDCtbHits = true;
	boolean showBSTreconsCrosses = true;
	boolean showFTOFreconsHits = true;

	// dc hit based reonstructed crosses?
	if (Bits.checkBit(bits, DisplayBits.DC_HB_RECONS_CROSSES)) {
	    _dcHBCrossesButton = add(DC_HB_CROSS_LABEL, showDChbCrosses, true,
		    this, _buttonColor).getCheckBox();
	}

	// display hit based reconstructed hits?
	if (Bits.checkBit(bits, DisplayBits.DC_HB_RECONS_HITS)) {
	    _dcHBHitsButton = add(DC_HB_HIT_LABEL, showDChbHits, true, this,
		    _buttonColor).getCheckBox();
	}

	// dc time based based reonstructed crosses?
	if (Bits.checkBit(bits, DisplayBits.DC_TB_RECONS_CROSSES)) {
	    _dcTBCrossesButton = add(DC_TB_CROSS_LABEL, showDCtbCrosses, true,
		    this, _buttonColor).getCheckBox();
	}

	// display time based based reconstructed hits?
	if (Bits.checkBit(bits, DisplayBits.DC_TB_RECONS_HITS)) {
	    _dcTBHitsButton = add(DC_TB_HIT_LABEL, showDCtbHits, true, this,
		    _buttonColor).getCheckBox();
	}

	if (Bits.checkBit(bits, DisplayBits.BSTRECONS_CROSSES)) {
	    _reconsBSTCrossButton = add(BSTRECONS_CROSS_LABEL,
		    showBSTreconsCrosses, true, this, _buttonColor)
		    .getCheckBox();
	}

	// ftof reconstructed hits
	if (Bits.checkBit(bits, DisplayBits.FTOFHITS)) {
	    _reconsFTOFHitButton = add(FTOFRECONS_HIT_LABEL,
		    showFTOFreconsHits, true, this, _buttonColor).getCheckBox();
	}

	setBorder(new CommonBorder("Reconstructed Display Options"));
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

	// repaint the container
	if (_view != null) {
	    _view.getContainer().refresh();
	}
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
     * Convenience method to see if we show the dc hb reconstructed hits.
     * 
     * @return <code>true</code> if we are to show dc hb reconstructed hits.
     */
    public boolean showDChbHits() {
	return (_dcHBHitsButton != null) && _dcHBHitsButton.isSelected();
    }

    /**
     * Convenience method to see if we show the dc hb reconstructed crosses.
     * 
     * @return <code>true</code> if we are to show dc hb reconstructed crosses.
     */
    public boolean showDChbCrosses() {
	return (_dcHBCrossesButton != null) && _dcHBCrossesButton.isSelected();
    }

    /**
     * Convenience method to see if we show the dc tb reconstructed hits.
     * 
     * @return <code>true</code> if we are to show dc tb reconstructed hits.
     */
    public boolean showDCtbHits() {
	return (_dcTBHitsButton != null) && _dcTBHitsButton.isSelected();
    }

    /**
     * Convenience method to see if we show the dc tb reconstructed crosses.
     * 
     * @return <code>true</code> if we are to show dc tb reconstructed crosses.
     */
    public boolean showDCtbCrosses() {
	return (_dcTBCrossesButton != null) && _dcTBCrossesButton.isSelected();
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