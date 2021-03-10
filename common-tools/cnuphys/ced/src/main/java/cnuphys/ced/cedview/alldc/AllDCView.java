package cnuphys.ced.cedview.alldc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import cnuphys.ced.cedview.CedView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.geometry.GeoConstants;
import cnuphys.ced.item.AllDCSuperLayer;
import cnuphys.bCNU.component.IRollOverListener;
import cnuphys.bCNU.component.RollOverPanel;
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
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;

/**
 * The AllDC view is a non-faithful representation of all six sectors of
 * driftchambers. It is very useful for occupancy plots.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AllDCView extends CedView implements IRollOverListener {
	
	//rollover colors
	private static final Color inactiveFG = Color.cyan;
	private static final Color inactiveBG = Color.black;
	private static final Color activeFG = Color.yellow;
	private static final Color activeBG = Color.darkGray;
	
	//roll over labels
	private static final String HB_ROLLOVER = "Reg Hit Based DC Clusters";
	private static final String TB_ROLLOVER = "Reg Time Based DC Clusters";
	private static final String AIHB_ROLLOVER = "AI Hit Based DC Clusters";
	private static final String AITB_ROLLOVER = "AI Time Based DC Clusters";
	private static final String SNR_ROLLOVER = "SNR DC Clusters";
	
	//rollover labels
	private static String roLabels[] = {HB_ROLLOVER, 
			TB_ROLLOVER, 
			AIHB_ROLLOVER,
			AITB_ROLLOVER,
			SNR_ROLLOVER};
	
	//rollover boolean flags
	private boolean _roShowHBDCClusters;
	private boolean _roShowTBDCClusters;
	private boolean _roShowAIHBDCClusters;
	private boolean _roShowAITBDCClusters;
	private boolean _roShowSNRDCClusters;
	
	
	//cluster drawer
	private ClusterDrawer _clusterDrawer;

	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	protected static final String _baseTitle = "All Drift Chambers";

	/**
	 * A sector rectangle for each sector
	 */
	protected Rectangle2D.Double _sectorWorldRects[];

	// font for label text
	protected static final Font labelFont = Fonts.commonFont(Font.PLAIN, 12);

	/**
	 * Used for drawing the sector rects.
	 */
	protected Styled _sectorStyle;

	// The optional "before" drawer for this view
	protected IDrawable _beforeDraw;
	
	//rollover panel for drawing clusters
	private RollOverPanel _rollOverPanel;

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
	 * Create an allDCView
	 * 
	 * @param keyVals variable set of arguments.
	 */
	protected AllDCView(Object... keyVals) {
		super(keyVals);

		setSectorWorldRects();
		setBeforeDraw();
		setAfterDraw();
		addItems();
	}

	/**
	 * Convenience method for creating an AllDC View.
	 * 
	 * @return a new AllDCView.
	 */
	public static AllDCView createAllDCView() {
		AllDCView view = null;

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.65);

		// create the view
		view = new AllDCView(PropertySupport.WORLDSYSTEM, _defaultWorldRectangle, PropertySupport.WIDTH, d.width, 
				PropertySupport.HEIGHT, d.height, // container height, not total view width
				PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true, PropertySupport.TITLE,
				_baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")")),
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.NOISECONTROL + ControlPanel.DISPLAYARRAY
				+ ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND
				+ ControlPanel.ALLDCDISPLAYPANEL,
				DisplayBits.ACCUMULATION + DisplayBits.MCTRUTH, 3, 5);
		view.add(view._controlPanel, BorderLayout.EAST);
		
		customize(view);

		view.pack();
		return view;
	}
	
//	public RollOverPanel(String title, int numCols, 
//			Font font, Color fg, Color bg, String... labels) {

	
	//add the rollover panel
	private static void customize(AllDCView view) {
		JTabbedPane tabbedPane =  view._controlPanel.getTabbedPane();
		view._rollOverPanel = new RollOverPanel("DC Clusters", 1, Fonts.mediumFont, inactiveFG, inactiveBG, 
				roLabels);
		
		view._rollOverPanel.addRollOverListener(view);
		tabbedPane.add(view._rollOverPanel, "DC Clusters");
		
		view._clusterDrawer = new ClusterDrawer(view);
	}

	/**
	 * Create the before drawer to draw the sector outlines.
	 */
	private void setBeforeDraw() {
		// style for sector rects

		_sectorStyle = new Styled(X11Colors.getX11Color("dark slate gray"));
		_sectorStyle.setLineColor(Color.lightGray);

		// use a before-drawer to sector dividers and labels
		_beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				g.setFont(labelFont);

				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				for (int sector = 0; sector < GeoConstants.NUM_SECTOR; sector++) {
					WorldGraphicsUtilities.drawWorldRectangle(g, container, _sectorWorldRects[sector], _sectorStyle);
					double left = _sectorWorldRects[sector].x;
					double top = _sectorWorldRects[sector].y + _sectorWorldRects[sector].height;
					g.setColor(Color.red);
					WorldGraphicsUtilities.drawWorldText(g, container, left, top, "Sector " + (sector + 1), 8, 14);
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
				
				if (_roShowSNRDCClusters) {
					_clusterDrawer.drawSNRDCClusters(g, container);					
				}

				if (_roShowHBDCClusters) {
					_clusterDrawer.drawHBDCClusters(g, container);
				}
				
				if (_roShowTBDCClusters) {
					_clusterDrawer.drawTBDCClusters(g, container);					
				}

				if (_roShowAIHBDCClusters) {
					_clusterDrawer.drawAIHBDCClusters(g, container);
				}
				
				if (_roShowAITBDCClusters) {
					_clusterDrawer.drawAITBDCClusters(g, container);					
				}


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

		AllDCView view = createAllDCView();
		view.setBounds(vr);
		return view;

	}
	
	/**
	 * Display raw DC hits?
	 * 
	 * @return <code> if we should display raw hits
	 */
	public boolean showRawHits() {
		return _controlPanel.getAllDCDisplayPanel().showRawHits();
	}

	/**
	 * Display hit based hits?
	 * 
	 * @return <code> if we should display hit based hits
	 */
	public boolean showHBHits() {
		return _controlPanel.getAllDCDisplayPanel().showHBHits();
	}

	/**
	 * Display time based hits?
	 * 
	 * @return <code> if we should display time based hits
	 */
	public boolean showTBHits() {
		return _controlPanel.getAllDCDisplayPanel().showTBHits();
	}
	
	/**
	 * Display AI hit based hits?
	 * 
	 * @return <code> if we should display AI hit based hits
	 */
	public boolean showAIHBHits() {
		return _controlPanel.getAllDCDisplayPanel().showAIHBHits();
	}

	/**
	 * Display AI time based hits?
	 * 
	 * @return <code> if we should display AI time based hits
	 */
	public boolean showAITBHits() {
		return _controlPanel.getAllDCDisplayPanel().showAITBHits();
	}

	/**
	 * Display neural net marked hits?
	 * 
	 * @return <code> if we should display neural net marked hits
	 */
	public boolean showNNHits() {
		return _controlPanel.getAllDCDisplayPanel().showNNHits();
	}
	


	@Override
	public void RollOverMouseEnter(JLabel label, MouseEvent e) {
		
		String text = label.getText();
		if (text.contains(HB_ROLLOVER)) {
			_roShowHBDCClusters = true;
		}
		else if (text.contains(TB_ROLLOVER)) {
			_roShowTBDCClusters = true;
		}
		else if (text.contains(AIHB_ROLLOVER)) {
			_roShowAIHBDCClusters = true;
		}
		else if (text.contains(AITB_ROLLOVER)) {
			_roShowAITBDCClusters = true;
		}

		else if (text.contains(SNR_ROLLOVER)) {
			_roShowSNRDCClusters = true;
		}
		
		label.setForeground(activeFG);
		label.setBackground(activeBG);
		
		refresh();
	}

	@Override
	public void RollOverMouseExit(JLabel label, MouseEvent e) {
		
		if (e.isAltDown() || e.isControlDown() || e.isMetaDown()) {
			return;
		}

		String text = label.getText();
		if (text.contains(HB_ROLLOVER)) {
			_roShowHBDCClusters = false;
		}
		else if (text.contains(TB_ROLLOVER)) {
			_roShowTBDCClusters = false;
		}
		else if (text.contains(AIHB_ROLLOVER)) {
			_roShowAIHBDCClusters = false;
		}
		else if (text.contains(AITB_ROLLOVER)) {
			_roShowAITBDCClusters = false;
		}
		else if (text.contains(SNR_ROLLOVER)) {
			_roShowSNRDCClusters = false;
		}

		label.setForeground(inactiveFG);
		label.setBackground(inactiveBG);
		
		refresh();
	}

}
