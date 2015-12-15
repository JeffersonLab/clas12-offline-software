package cnuphys.ced.cedview.dcxy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Properties;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.Histo2DData;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.geometry.DCGeometry;
import cnuphys.ced.item.DCHexSectorItem;
import cnuphys.ced.item.HexSectorItem;

public class DCXYView extends HexView {

	// sector items
	private DCHexSectorItem _hexItems[];

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	// draws reconstructed crosses
	private CrossDrawer _crossDrawer;

	// draws mc hits
	private McHitDrawer _mcHitDrawer;

	// font for label text
	private static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);
	private static final Color TRANS = new Color(192, 192, 192, 128);

	protected static Rectangle2D.Double _defaultWorld;

	static {
		double _xsize = DCGeometry.getAbsMaxWireX();
		double _ysize = _xsize * 1.154734;

		_defaultWorld = new Rectangle2D.Double(_xsize, -_ysize, -2 * _xsize,
				2 * _ysize);

	}

	/**
	 * Create an allDCView
	 * 
	 * @param keyVals
	 *            variable set of arguments.
	 */
	private DCXYView(String title) {
		super(getAttributes(title));
		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);
		_crossDrawer = new CrossDrawer(this);
		_mcHitDrawer = new McHitDrawer(this);
		setBeforeDraw();
		setAfterDraw();
		getContainer().getComponent().setBackground(Color.gray);
	}

	// add the control panel
	@Override
	protected void addControls() {

		_controlPanel = new ControlPanel(this, ControlPanel.DISPLAYARRAY
				+ ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND
				+ ControlPanel.RECONSARRAY + ControlPanel.DRAWLEGEND, 
				DisplayBits.ACCUMULATION
				+ DisplayBits.DC_HB_RECONS_CROSSES
				+ DisplayBits.DC_TB_RECONS_CROSSES + DisplayBits.MCTRUTH, 2, 10);

		add(_controlPanel, BorderLayout.EAST);
		pack();
	}

	/**
	 * Used to create the DCXY view
	 * 
	 * @return the view
	 */
	public static DCXYView createDCXYView() {
		DCXYView view = new DCXYView("Drift Chambers XY");

		return view;
	}

	// add items to the view
	@Override
	protected void addItems() {
		LogicalLayer detectorLayer = getContainer().getLogicalLayer(
				_detectorLayerName);

		_hexItems = new DCHexSectorItem[6];

		for (int sector = 0; sector < 6; sector++) {
			_hexItems[sector] = new DCHexSectorItem(detectorLayer, this,
					sector + 1);
			_hexItems[sector].getStyle().setFillColor(Color.lightGray);
		}
	}

	/**
	 * Create the view's before drawer.
	 */
	private void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	private void setAfterDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				if (!_eventManager.isAccumulating()) {

					// draw trajectories
					_swimTrajectoryDrawer.draw(g, container);

					// mc hits
					if (getMode() == CedView.Mode.SINGLE_EVENT) {
						_mcHitDrawer.draw(g, container);
					} else {
						drawAccumulatedGemcGlobalHits(g, container);
					}

					// draw reconstructed dc crosses
					if (showDChbCrosses()) {
						_crossDrawer.setMode(CrossDrawer.HB);
						_crossDrawer.draw(g, container);
					}
					if (showDCtbCrosses()) {
						_crossDrawer.setMode(CrossDrawer.TB);
						_crossDrawer.draw(g, container);
					}
					drawCoordinateSystem(g, container);
				} // not acumulating
			}

		};

		getContainer().setAfterDraw(beforeDraw);
	}

	private void drawCoordinateSystem(Graphics g, IContainer container) {
		// draw coordinate system
		Component component = container.getComponent();
		Rectangle sr = component.getBounds();

		int left = 25;
		int right = left + 50;
		int bottom = sr.height - 20;
		int top = bottom - 50;
		g.setFont(labelFont);
		FontMetrics fm = getFontMetrics(labelFont);

		Rectangle r = new Rectangle(left - fm.stringWidth("x") - 4, top
				- fm.getHeight() / 2 + 1, (right - left + fm.stringWidth("x")
				+ fm.stringWidth("y") + 9), (bottom - top) + fm.getHeight() + 2);

		g.setColor(TRANS);
		g.fillRect(r.x, r.y, r.width, r.height);

		g.setColor(X11Colors.getX11Color("dark red"));
		g.drawLine(left, bottom, right, bottom);
		g.drawLine(right, bottom, right, top);

		g.drawString("y", right + 3, top + fm.getHeight() / 2 - 1);
		g.drawString("x", left - fm.stringWidth("x") - 2,
				bottom + fm.getHeight() / 2);

	}

	// draw the gemc global hits
	private void drawAccumulatedGemcGlobalHits(Graphics g, IContainer container) {
		Histo2DData dcXYGemc = AccumulationManager.getInstance()
				.getDcXYGemcAccumulatedData();
		if (dcXYGemc != null) {

			long counts[][] = dcXYGemc.getCounts();

			if (counts != null) {
				Rectangle2D.Double wr = new Rectangle2D.Double();
				Rectangle r = new Rectangle();

				double maxBinCount = dcXYGemc.getMaxZ();

				for (int i = 0; i < dcXYGemc.getNumberBinsX(); i++) {
					double x1 = dcXYGemc.getBinMinX(i);
					double x2 = dcXYGemc.getBinMaxX(i);

					for (int j = 0; j < dcXYGemc.getNumberBinsY(); j++) {
						if (counts[i][j] > 0) {
							double y1 = dcXYGemc.getBinMinY(j);
							double y2 = dcXYGemc.getBinMaxY(j);
							labPointsToWorldRect(x1, y1, x2, y2, wr);
							container.worldToLocal(r, wr);

							double fract = ((counts[i][j])) / maxBinCount;
							Color color = AccumulationManager
									.getColorScaleModel().getColor(fract);

							g.setColor(color);
							g.fillRect(r.x, r.y, r.width, r.height);
						}

					}
				}
			} // counts != null
		}
	}

	// get the attributes to pass to the super constructor
	private static Object[] getAttributes(String title) {

		Properties props = new Properties();
		props.put(PropertySupport.TITLE, title);

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.65);

		props.put(PropertySupport.WORLDSYSTEM, _defaultWorld);
		props.put(PropertySupport.WIDTH, (int) (0.866 * d.height));
		props.put(PropertySupport.HEIGHT, d.height);

		props.put(PropertySupport.TOOLBAR, true);
		props.put(PropertySupport.TOOLBARBITS, BaseToolBar.NODRAWING
				& ~BaseToolBar.RANGEBUTTON & ~BaseToolBar.TEXTFIELD
				& ~BaseToolBar.CONTROLPANELBUTTON & ~BaseToolBar.TEXTBUTTON
				& ~BaseToolBar.DELETEBUTTON);
		props.put(PropertySupport.VISIBLE, true);
		props.put(PropertySupport.HEADSUP, false);

		props.put(PropertySupport.BACKGROUND,
				X11Colors.getX11Color("Alice Blue"));
		props.put(PropertySupport.STANDARDVIEWDECORATIONS, true);

		return PropertySupport.toObjectArray(props);
	}

	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {

		container.worldToLocal(pp, wp);

		super.getFeedbackStrings(container, pp, wp, feedbackStrings);

		// reconstructed feedback?
		if (showDChbCrosses()) {
			_crossDrawer.setMode(CrossDrawer.HB);
			_crossDrawer.feedback(container, pp, wp, feedbackStrings);
		}
		if (showDCtbCrosses()) {
			_crossDrawer.setMode(CrossDrawer.TB);
			_crossDrawer.feedback(container, pp, wp, feedbackStrings);
		}

		if (showMcTruth()) {
			_mcHitDrawer.feedback(container, pp, wp, feedbackStrings);
		}

	}

	private void labPointsToWorldRect(double x1, double y1, double x2,
			double y2, Rectangle2D.Double wr) {
		Point2D.Double p2d = new Point2D.Double(x1, y1);
		double wx1 = p2d.x;
		double wy1 = p2d.y;

		p2d.setLocation(x2, y2);
		double wx2 = p2d.x;
		double wy2 = p2d.y;

		wr.setFrame(wx1, wy1, wx2 - wx1, wy2 - wy1);
	}

	/**
	 * Lab (CLAS) xy coordinates to local screen coordinates.
	 * 
	 * @param container
	 *            the drawing container
	 * @param pp
	 *            will hold the graphical world coordinates
	 * @param lab
	 *            the lab coordinates
	 */
	public static void labToLocal(IContainer container, Point pp,
			Point2D.Double lab) {
		container.worldToLocal(pp, lab);
	}

	/**
	 * Get the hex item for the given 1-based sector
	 * 
	 * @param sector
	 *            the 1-based sector
	 * @return the corresponding item
	 */
	public HexSectorItem getHexSectorItem(int sector) {
		return _hexItems[sector - 1];
	}

}
