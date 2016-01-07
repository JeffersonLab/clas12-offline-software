package cnuphys.ced.cedview.dcxy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jlab.geom.prim.Line3D;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.HexView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.DC;
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
		
	//exach superlayer in a different color
	private static Color _wireColors[] = {Color.red, X11Colors.getX11Color("dark red"), 
			X11Colors.getX11Color("cadet blue"), X11Colors.getX11Color("dark blue"),
			X11Colors.getX11Color("olive"), X11Colors.getX11Color("dark green")};

	// font for label text
	private static final Font labelFont = Fonts.commonFont(Font.PLAIN, 11);
	private static final Color TRANS = new Color(192, 192, 192, 128);
	private static final Color TRANSTEXT = new Color(64, 64, 192, 40);
	private static final Font _font = Fonts.commonFont(Font.BOLD, 60);
	
	private static Stroke stroke = GraphicsUtilities.getStroke(0.5f,
			LineStyle.SOLID);


	protected static Rectangle2D.Double _defaultWorld;

	static {
		double _xsize = 1.02*DCGeometry.getAbsMaxWireX();
		double _ysize = 1.02*_xsize * 1.154734;

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
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JComponent wireLegend = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				Rectangle b = getBounds();
				g.setColor(Color.darkGray);
				g.fillRect(b.x, b.y, b.width, b.height);
				int yc = b.y + b.height/2;
				int linelen = 40;
				g.setFont(Fonts.mediumFont);
				FontMetrics fm = this.getFontMetrics(Fonts.mediumFont);
				
				int x = 6;
				for (int supl0 = 0; supl0 < 6; supl0++) {
					String s = " superlayer " + (supl0+1) + "    ";
					g.setColor(_wireColors[supl0]);
					g.drawLine(x, yc, x+linelen, yc);
					x = x + linelen + 4;
					g.setColor(Color.white);
					g.drawString(s, x, yc+4);
					x += fm.stringWidth(s);
				}
			}
			
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		};
		panel.add(wireLegend, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEtchedBorder());
		add(panel, BorderLayout.SOUTH);
	}

	// add the control panel
	@Override
	protected void addControls() {

		_controlPanel = new ControlPanel(this, ControlPanel.DISPLAYARRAY
				+ ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND
				+ ControlPanel.DRAWLEGEND, 
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

					drawHits(g, container);

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
					drawSectorNumbers(g, container);
				} // not acumulating
			}

		};

		getContainer().setAfterDraw(beforeDraw);
	}
	
	private void drawHits(Graphics g, IContainer container) {
		
		if (isSingleEventMode()) {
			if (showMcTruth()) {
				_mcHitDrawer.draw(g, container);
				
				int hitCount = DC.hitCount();
				
				if (hitCount > 0) {
					int sector[] = DC.sector();
					int superlayer[] = DC.superlayer();
					int layer[] = DC.layer();
					int wire[] = DC.wire();
					
					Graphics2D g2 = (Graphics2D)g;
					Stroke oldStroke = g2.getStroke();
					g2.setStroke(stroke);
					
					Point pp1 = new Point();
					Point pp2 = new Point();
					Point2D.Double wp1 = new Point2D.Double();
					Point2D.Double wp2 = new Point2D.Double();
					
					for (int hit = 0; hit < hitCount; hit++) {
						g.setColor(_wireColors[superlayer[hit]-1]);
						Line3D line = DCGeometry.getWire(sector[hit], superlayer[hit], layer[hit], wire[hit]);
						wp1.setLocation(line.origin().x(), line.origin().y());
						wp2.setLocation(line.end().x(), line.end().y());
						container.worldToLocal(pp1, wp1);
						container.worldToLocal(pp2, wp2);
						g.drawLine(pp1.x, pp1.y, pp2.x, pp2.y);
					}
					
					g2.setStroke(oldStroke);

				} //hitCount > 0
				
			}
		}
		else {
			drawAccumulatedHits(g, container);
		}
	}
	
	//draw the sector numbers
	private void drawSectorNumbers(Graphics g, IContainer container) {
		double r3over2 = Math.sqrt(3)/2;
		
		double x = 320;
		double y = 0;
		FontMetrics fm = getFontMetrics(_font);
		g.setFont(_font);
		g.setColor(TRANSTEXT);
		Point pp = new Point();

		
		for (int sect = 1; sect <= 6; sect++) {
			container.worldToLocal(pp, x, y);
			
			String s = "" + sect;
			int sw = fm.stringWidth(s);
			
			g.drawString(s, pp.x- sw/2, pp.y + fm.getHeight()/2);
			
			if (sect != 6) {
				double tx = x;
				double ty = y;
				x = 0.5*tx - r3over2*ty;
				y = r3over2*tx + 0.5*ty;
			}
		}
	}

	//draw the coordinate system
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
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		
		int dcAccumulatedData[][][][] = AccumulationManager.getInstance()
				.getAccumulatedDgtzDcData();
		int maxHit = AccumulationManager.getInstance().getMaxDgtzDcCount();
		if (maxHit < 1) {
			return;
		}

		Point pp1 = new Point();
		Point pp2 = new Point();
		Point2D.Double wp1 = new Point2D.Double();
		Point2D.Double wp2 = new Point2D.Double();

		for (int sect0 = 0; sect0 < 6; sect0++) {
			for (int supl0 = 0; supl0 < 6; supl0++) {
				for (int lay0 = 0; lay0 < 6; lay0++) {
					for (int wire0 = 0; wire0 < 112; wire0++) {

						double fract;
						int hitCount = dcAccumulatedData[sect0][supl0][lay0][wire0];

						if (hitCount > 0) {
							if (isSimpleAccumulatedMode()) {
								fract = ((double) hitCount) / maxHit;
							}
							else {
								fract = Math.log(hitCount + 1.)
										/ Math.log(maxHit + 1.);
							}

							Color color = AccumulationManager.getInstance()
									.getAlphaColor(fract, 128);
							g.setColor(color);
							Line3D line = DCGeometry.getWire(sect0 + 1,
									supl0 + 1, lay0 + 1, wire0 + 1);
							wp1.setLocation(line.origin().x(),
									line.origin().y());
							wp2.setLocation(line.end().x(), line.end().y());
							container.worldToLocal(pp1, wp1);
							container.worldToLocal(pp2, wp2);
							g.drawLine(pp1.x, pp1.y, pp2.x, pp2.y);
						} // hitcount > 0
					}
				}
			}
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
		props.put(PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS);
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
