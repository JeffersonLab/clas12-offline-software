package cnuphys.ced.cedview.alldc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jlab.io.base.DataEvent;

import cnuphys.ced.cedview.CedView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCTdcHit;
import cnuphys.ced.event.data.DCTdcHitList;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.item.AllDCSuperLayer;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.Styled;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;

/**
 * The AllDC view is a non-faithful representation of all six sectors of
 * driftchambers. It is very useful for occupancy plots.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AllDCAccumView extends CedView implements IAllDC {

	// display modes
	public static final int SHOW_ALL_MODE = 0;
	public static final int HIDE_NOISE_MODE = 1;
	public static final int NOISE_ONLY_MODE = 2;

	// default mode
	private int _mode = SHOW_ALL_MODE;

	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	protected static final String _baseTitle = "Accumulation Only";

	// counts for all data indices sector, superlayer, layer. wire
	private int _counts[][][][] = new int[6][6][6][112];

	// superlayer counts
	private int _superlayerCounts[][] = new int[6][6];

	// sector counts
	private int _sectorCounts[] = new int[6];

	/**
	 * A sector rectangle for each sector
	 */
	protected Rectangle2D.Double _sectorWorldRects[];

	// font for label text
	protected static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);

	/**
	 * Used for drawing the sector rects.
	 */
	protected Styled _sectorStyle;

	// The optional "before" drawer for this view
	protected IDrawable _beforeDraw;

	/**
	 * The all dc view is rendered on 2x3 grid. Each grid is 1x1 in world
	 * coordinates. Thus the whole view has width = 3 and height = 2. These offesets
	 * move the sector to the right spot on the grid.
	 */
	protected static double _xoffset[] = { 0.0, 1.0, 2.0, 0.0, 1.0, 2.0 };

	/**
	 * The all dc view is rendered on 2x3 grid. Each grid is 1x1 in world
	 * coordinates. Thus the whole view has width = 3 and height = 2. These offesets
	 * move the sector to the right spot on the grid.
	 */
	protected static double _yoffset[] = { 1.0, 1.0, 1.0, 0.0, 0.0, 0.0 };

	// all the superlayer items indexed by sector (0..5) and superlayer (0..5)
	protected AllDCSuperLayer _superLayerItems[][];

	protected static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(0.0, 0.0, 3.0, 2.0);

	/**
	 * Create an allDCAccumView
	 * 
	 * @param keyVals variable set of arguments.
	 */
	protected AllDCAccumView(Object... keyVals) {
		super(keyVals);

		setSectorWorldRects();
		setBeforeDraw();
		setAfterDraw();
		addItems();

		_eventManager.addSpecialEventListener(this);
	}

	/**
	 * Convenience method for creating an AllDC View.
	 * 
	 * @return a new AllDCAccumView.
	 */
	public static AllDCAccumView createAllDCAccumView() {
		AllDCAccumView view = null;

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.65);

		// create the view
		view = new AllDCAccumView(PropertySupport.WORLDSYSTEM, _defaultWorldRectangle, PropertySupport.WIDTH, d.width, // container
																														// width,
																														// not
																														// total
																														// view
																														// width
				PropertySupport.HEIGHT, d.height, // container height, not total view width
				PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true, PropertySupport.TITLE,
				_baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")")),
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.NOISECONTROL + ControlPanel.DISPLAYARRAY
				+ ControlPanel.FEEDBACK + ControlPanel.ALLDC_ACCUM_ONLY, DisplayBits.ACCUMULATION + DisplayBits.MCTRUTH,
				3, 5);
		view.add(view._controlPanel, BorderLayout.EAST);

		view.pack();
		return view;
	}

	/**
	 * Create the before drawer to draw the sector outlines.
	 */
	private void setBeforeDraw() {
		// style for sector rects

		_sectorStyle = new Styled(Color.black);
		_sectorStyle.setLineColor(Color.yellow);

		// use a before-drawer to sector dividers and labels
		_beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				g.setFont(labelFont);
				for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
					WorldGraphicsUtilities.drawWorldRectangle(g, container, _sectorWorldRects[sector], _sectorStyle);
					double left = _sectorWorldRects[sector].x;
					double top = _sectorWorldRects[sector].y + _sectorWorldRects[sector].height;
					g.setColor(Color.cyan);
					WorldGraphicsUtilities.drawWorldText(g, container, left, top, "Sector " + (sector + 1), 8, 12);
				}
			}

		};

		getContainer().setBeforeDraw(_beforeDraw);
	}

	/**
	 * Set the views before draw
	 */
	private void setAfterDraw() {
		IDrawable _afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
			}

		};
		getContainer().setAfterDraw(_afterDraw);
	}

	/**
	 * Setup the sector world rects
	 */
	private void setSectorWorldRects() {

		_sectorWorldRects = new Rectangle2D.Double[6];

		Rectangle2D.Double defaultWorld = _defaultWorldRectangle;
		double left = defaultWorld.getMinX();
		double right = defaultWorld.getMaxX();
		double top = defaultWorld.getMaxY();
		double bottom = defaultWorld.getMinY();
		double ymid = defaultWorld.getCenterY();
		double x13 = left + defaultWorld.width / 3.0;
		double x23 = right - defaultWorld.width / 3.0;

		_sectorWorldRects[0] = new Rectangle2D.Double(left, ymid, x13 - left, top - ymid);
		_sectorWorldRects[1] = new Rectangle2D.Double(x13, ymid, x23 - x13, top - ymid);
		_sectorWorldRects[2] = new Rectangle2D.Double(x23, ymid, right - x13, top - ymid);

		_sectorWorldRects[3] = new Rectangle2D.Double(left, bottom, x13 - left, ymid - bottom);
		_sectorWorldRects[4] = new Rectangle2D.Double(x13, bottom, x23 - x13, ymid - bottom);
		_sectorWorldRects[5] = new Rectangle2D.Double(x23, bottom, right - x23, ymid - bottom);
	}

	/**
	 * This adds the detector items. The AllDC view is not faithful to geometry. All
	 * we really uses in the number of superlayers, number of layers, and number of
	 * wires.
	 */
	private void addItems() {
		// use sector 0 all the same
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(_detectorLayerName);

		double width = 0.92; // full width of each sector is 1.0;
		double xo = (1.0 - width) / 2.0;

		// sizing the height is more difficult. Total height is 1.0.
		double bottomMargin = 0.03; // from bottom to superlayer 1 in world
		// coords
		double topMargin = 0.06; // will get bigger if TOF added
		double superLayerGap = 0.02; // between superlayers
		double regionGap = 0.04; // between regions
		double whiteSpace = bottomMargin + topMargin + 3 * superLayerGap + 2 * regionGap;
		double height = (1.0 - whiteSpace) / GeoConstants.NUM_SUPERLAYER;

		// cache all the superlayer items we are about to create
		_superLayerItems = new AllDCSuperLayer[GeoConstants.NUM_SECTOR][GeoConstants.NUM_SUPERLAYER];

		// loop over the sectors and add 6 superlayer items for each sector
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			double yo = bottomMargin;
			for (int superLayer = 0; superLayer < GeoConstants.NUM_SUPERLAYER; superLayer++) {
				Rectangle2D.Double wr = new Rectangle2D.Double(_xoffset[sector] + xo, _yoffset[sector] + yo, width,
						height);

				// note we add superlayer items with 0-based sector and
				// superLayer
				// note we flip for lower sectors

				_superLayerItems[sector][superLayer] = null;
				if (sector < 3) {
					_superLayerItems[sector][superLayer] = new AllDCSuperLayer(detectorLayer, this, wr, sector,
							superLayer, GeoConstants.NUM_WIRE);
				} else {
					_superLayerItems[sector][superLayer] = new AllDCSuperLayer(detectorLayer, this, wr, sector,
							5 - superLayer, GeoConstants.NUM_WIRE);
				}

				if ((superLayer % 2) == 0) {
					yo += superLayerGap + height;
				} else {
					yo += regionGap + height;
				}
			}

		}
	}

	/**
	 * Get the AllDCSuperLayer item for the given sector and superlayer.
	 * 
	 * @param sector     the zero-based sector [0..5]
	 * @param superLayer the zero based super layer [0..5]
	 * @return the AllDCSuperLayer item for the given sector and superlayer (or
	 *         <code>null</code>).
	 */
	public AllDCSuperLayer getAllDCSuperLayer(int sector, int superLayer) {
		if ((sector < 0) || (sector >= GeoConstants.NUM_SECTOR)) {
			return null;
		}
		if ((superLayer < 0) || (superLayer >= GeoConstants.NUM_SUPERLAYER)) {
			return null;
		}
		return _superLayerItems[sector][superLayer];
	}

	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container   the base container for the view.
	 * @param screenPoint the pixel point
	 * @param worldPoint  the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		// get the common information
		super.getFeedbackStrings(container, screenPoint, worldPoint, feedbackStrings);
		// feedbackStrings.add("#DC hits: " + _numHits);

		int sector = getSector(container, screenPoint, worldPoint);

		double totalOcc = 100. * DC.getInstance().totalOccupancy();
		double sectorOcc = 100. * DC.getInstance().totalSectorOccupancy(sector);
		String occStr = "Total DC occ " + DoubleFormat.doubleFormat(totalOcc, 2) + "%" + " sector " + sector + " occ "
				+ DoubleFormat.doubleFormat(sectorOcc, 2) + "%";
		feedbackStrings.add("$aqua$" + occStr);

	}

	/**
	 * Get the sector corresponding to the current pointer location..
	 * 
	 * @param container   the base container for the view.
	 * @param screenPoint the pixel point
	 * @param worldPoint  the corresponding world location.
	 * @return the sector [1..6] or -1 for none.
	 */
	@Override
	public int getSector(IContainer container, Point screenPoint, Point2D.Double worldPoint) {
		for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
			if (_sectorWorldRects[sector].contains(worldPoint)) {
				return sector + 1; // convert to 1-based index
			}
		}
		return -1;
	}

	/**
	 * Get the world rectangle for a given cell (the wire is in the center)
	 * 
	 * @param sector     the 1-based sector
	 * @param superLayer the 1-based super layer
	 * @param layer      the 1-based layer [1..6]
	 * @param wire       the 1-based wire [1..] return the world rectangle cell for
	 *                   this layer, wire
	 */
	public void getCell(int sector, int superLayer, int layer, int wire, Rectangle2D.Double wr) {
		_superLayerItems[sector - 1][superLayer - 1].getCell(layer, wire, wr);
	}

	/**
	 * Clone the view.
	 * 
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT++;

		// limit
		if (CLONE_COUNT > 2) {
			return null;
		}

		Rectangle vr = getBounds();
		vr.x += 40;
		vr.y += 40;

		AllDCAccumView view = createAllDCAccumView();
		view.setBounds(vr);
		return view;

	}

	/**
	 * Get the underlying view
	 * 
	 * @return the underlying view
	 */
	@Override
	public CedView getView() {
		return this;
	}

	/**
	 * Is this the standard alldc view?
	 * 
	 * @return <code>true/code> if this is the standard alldc view.
	 */
	@Override
	public boolean isStandardAllDCView() {
		return false;
	}

	// reset the accumulated data
	public void reset() {
		System.err.println("reset");

		for (int sect = 0; sect < 6; sect++) {
			_sectorCounts[sect] = 0;
			for (int supl = 0; supl < 6; supl++) {
				_superlayerCounts[sect][supl] = 0;
				for (int lay = 0; lay < 6; lay++) {
					for (int wire = 0; wire < 112; wire++) {
						_counts[sect][supl][lay][wire] = 0;
					}
				}
			}
		}

		refresh();
	}

	/**
	 * Set the display mode
	 * 
	 * @param mode the new mode default is SHOW_ALL_MODE
	 */
	public void setMode(int mode) {
		_mode = mode;
		// System.err.println("MODE = " + _mode);
		refresh();
	}

	/**
	 * A new event has arrived.
	 * 
	 * @param event the new event.
	 */
	@Override
	public void newClasIoEvent(final DataEvent event) {
		super.newClasIoEvent(event);
//		System.err.println("HEY MAN");

		DCTdcHitList hits = DC.getInstance().getTDCHits();
		if ((hits != null) && !hits.isEmpty()) {
			for (DCTdcHit hit : hits) {
				logHit(hit);
			}
		}
	}

	private void logHit(DCTdcHit hit) {
		int sect0 = hit.sector - 1;
		int supl0 = hit.superlayer - 1;
		int lay0 = hit.layer6 - 1;
		int wire0 = hit.wire - 1;

		// System.err.println("LOGGED HIT");
		_counts[sect0][supl0][lay0][wire0] += 1;
	}

	/**
	 * Augmented feedback called by the superlayer item
	 * 
	 * @param sector          the sector [1..6]
	 * @param superlayer      the superlayer [1..6]
	 * @param layer           the layer [1..6]
	 * @param wire            the wire [1..112]
	 * @param feedbackStrings
	 */
	public void augmentedFeedback(int sector, int superlayer, int layer, int wire, List<String> feedbackStrings) {
		augFB(0, "sector " + sector, feedbackStrings);
		augFB(0, "superlayer " + superlayer, feedbackStrings);
		augFB(0, "layer " + layer, feedbackStrings);
		augFB(0, "wire " + wire, feedbackStrings);

		int counts = getCounts(sector, superlayer, layer, wire);
		augFB(1, "counts " + counts, feedbackStrings);
	}

	// 1-based indices
	private int getCounts(int sector, int superlayer, int layer, int wire) {
		return _counts[sector - 1][superlayer - 1][layer - 1][wire - 1];
	}

	// counts in a given superlayer
	private int getSuperlayerCounts(int sector, int superlayer) {
		int counts = 0;

		for (int layer = 1; layer <= 6; layer++) {
			for (int wire = 1; wire <= 112; wire++) {
				counts += getCounts(sector, superlayer, layer, wire);
			}
		}

		return counts;
	}

	// counts in a given sector
	private int getSectorCounts(int sector) {
		int counts = 0;

		for (int superlayer = 1; superlayer <= 6; superlayer++) {
			for (int layer = 1; layer <= 6; layer++) {
				for (int wire = 1; wire <= 112; wire++) {
					counts += getCounts(sector, superlayer, layer, wire);
				}
			}
		}

		return counts;
	}

	// add a colored string

	private static final String colors[] = { "$yellow$", "$white$", "$orange$" };

	private void augFB(int col, String msg, List<String> feedbackStrings) {
		feedbackStrings.add(colors[col] + msg);
	}

}
