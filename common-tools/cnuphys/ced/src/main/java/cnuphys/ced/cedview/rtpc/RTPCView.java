package cnuphys.ced.cedview.rtpc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.ClasIoEventManager.EventSourceType;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.RTPC;
import cnuphys.ced.event.data.RTPCHit;
import cnuphys.ced.event.data.RTPCHitList;

import cnuphys.lund.X11Colors;


public class RTPCView extends CedXYView implements ChangeListener {
	
	/* layers are the z divisions and will be the vertical axis, with a physical range 
	 * of -192mm to 192 mm and 96 divisions, so each division in (2*192)/96 = 4mm.
	 * 
	 * components are the phi divisions and will be the horizontal axis, with a physical
	 * range of 0 to 360 deg and 180 divisions, so each division is 2 deg.
	 */
	
	private static final double DELZ = 4.; //mm
	private static final double DELPHI = 2.; //deg
	private static final double ZMIN = -192;
	private static final double PHIMIN = 0;
	private static final double ZMAX = 192;
	private static final double PHIMAX = 360;
//	private static final double STRETCHZMIN = 1.005*ZMIN;
//	private static final double STRETCHPHIMIN = 1.005*PHIMIN;
//	private static final double STRETCHZMAX = 1.005*ZMAX;
//	private static final double STRETCHPHIMAX = 1.005*PHIMAX;
	
	//the local screen rect
	private Rectangle screenRect;
	
	// for naming clones
	private static int CLONE_COUNT = 0;

	// base title
	private static final String _baseTitle = "RTPC";

	// units are 
//	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(STRETCHPHIMIN, STRETCHZMIN, 
//			(STRETCHPHIMAX - STRETCHPHIMIN), (STRETCHZMAX - STRETCHZMIN));
	
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(-10, -200, 380, 400);

	/**
	 * Create an RTPC view
	 * @param keyVals the properties
	 */
	public RTPCView(Object... keyVals) {
		super(keyVals);
	}
	

	/**
	 * Create an RTPC View view
	 * 
	 * @return an RTPC View View
	 */
	public static RTPCView createRTPCView() {
		RTPCView view = null;

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.4);

		// make it square
		int width = d.width;
		int height = width;

		String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));

		// create the view
		view = new RTPCView(PropertySupport.WORLDSYSTEM, _defaultWorldRectangle, 
				PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height, 
				PropertySupport.LEFTMARGIN, LMARGIN, 
				PropertySupport.TOPMARGIN, TMARGIN,
				PropertySupport.RIGHTMARGIN, RMARGIN, 
				PropertySupport.BOTTOMMARGIN, BMARGIN, 
				PropertySupport.TOOLBAR, true, 
				PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS, 
				PropertySupport.VISIBLE, true,
				PropertySupport.TITLE, title, 
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view,
				ControlPanel.DISPLAYARRAY + ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND + ControlPanel.ADCTHRESHOLDSLIDER,
				DisplayBits.ACCUMULATION + DisplayBits.MCTRUTH, 3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();

		return view;
	}

	/**
	 * Create the view's before drawer.
	 */
	@Override
	protected void setBeforeDraw() {
		// use a before-drawer to sector dividers and labels
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				Component component = container.getComponent();
//				Rectangle b = component.getBounds();
//
//				// ignore b.x and b.y as usual
//
//				b.x = 0;
//				b.y = 0;

				screenRect = container.getInsetRectangle();
				g.setColor(X11Colors.getX11Color("alice blue"));
				g.fillRect(screenRect.x, screenRect.y, screenRect.width, screenRect.height);

				drawGrid(g, container, screenRect);
				g.setColor(Color.black);
				g.drawRect(screenRect.x, screenRect.y, screenRect.width, screenRect.height);
			}

			// draw the rtpc pad board grid
			private void drawGrid(Graphics g, IContainer container, Rectangle screenRect) {
				
				Rectangle.Double wr = new Rectangle.Double();
				container.localToWorld(screenRect, wr);
				
				double xmin = wr.x;
				double xmax =  wr.getMaxX();
				double ymin = wr.y;
				double ymax =  wr.getMaxY();

				
				Point p0 = new Point();
				Point p1 = new Point();
			
				//horizontal lines
				for (int i = 0; i <= RTPC.NUMLAYER; i++) {
					double z = ZMIN + i*DELZ;
					
					
					if ((z >= ymin) && (z <= ymax)) {
						g.setColor((Math.abs(z) < 1) ? Color.gray : Color.lightGray);

						container.worldToLocal(p0, Math.max(PHIMIN, xmin), z);
						container.worldToLocal(p1, Math.min(PHIMAX, xmax), z);
						g.drawLine(p0.x, p0.y, p1.x, p1.y);
					}
					
					
				}
				
				// vertical lines
				for (int i = 0; i <= RTPC.NUMCOMPONENT; i++) {
					double phi = PHIMIN + i * DELPHI;
					
					
					if ((phi >= xmin) && (phi <= xmax)) {
						g.setColor((Math.abs(phi-180) < 1) ? Color.gray : Color.lightGray);

						container.worldToLocal(p0, phi, Math.max(ZMIN, ymin));
						container.worldToLocal(p1, phi, Math.min(ZMAX, ymax));
						g.drawLine(p0.x, p0.y, p1.x, p1.y);
					}
				}



			}

		};

		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Set the view's after draw
	 */
	@Override
	protected void setAfterDraw() {
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				if (isSingleEventMode()) {
					drawSingleEventHits(g, container);
				} else {
					drawAccumulatedHits(g, container);
				}

				Rectangle screenRect = getActiveScreenRectangle(container);
				drawAxes(g, container, screenRect);

			}

		};
		getContainer().setAfterDraw(afterDraw);
	}

	// single event drawer
	private void drawSingleEventHits(Graphics g, IContainer container) {
		RTPCHitList hits = RTPC.getInstance().getHits();
//		System.err.println("DRAWING RTPC HITS count " + ((hits == null) ? 0 : hits.size()));
		
		if ((hits != null) && !hits.isEmpty()) {
			
			Rectangle hr = new Rectangle();
			Point p0 = new Point();
			Point p1 = new Point();

			for (RTPCHit hit : hits) {
				if (hit != null) {

					if (hit.adc >= getAdcThreshold()) {
						// component and layer are 1-based
						short component = hit.component;
						byte layer = hit.layer;

						setRect(container, component, layer, hr, p0, p1);
						g.setColor(Color.red);
						g.fillRect(hr.x, hr.y, hr.width, hr.height);
					}

				}
			}
		}

	}
	
	//set the rect based on component and layer
	//component and layer are 1-based
	private void setRect(IContainer container, short component, byte layer, Rectangle hr, Point p0, Point p1) {
		double phi = PHIMIN + (component - 1)*DELPHI;
		double z = ZMIN + (layer - 1)*DELZ;
		
		container.worldToLocal(p0, phi, z);
		container.worldToLocal(p1, phi+DELPHI, z+DELZ);
		 
		hr.setBounds(p0.x, p1.y, p1.x -  p0.x, p0.y - p1.y);
		
//		System.err.println("RTPC hit rect: " +  hr);
	}

	// accumulated hits drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		int medianHit = AccumulationManager.getInstance().getMedianRTPCCount();

		Rectangle hr = new Rectangle();
		Point p0 = new Point();
		Point p1 = new Point();

		int counts[][] = AccumulationManager.getInstance().getAccumulatedRTPCData();
		for (short cm1 = 0; cm1 < RTPC.NUMCOMPONENT; cm1++) {
			for (byte lm1 = 0; lm1 < RTPC.NUMLAYER; lm1++) {
				if (counts[cm1][lm1] > 0) {
										
					short component =  (short)(cm1 + 1);
					byte layer = (byte)(lm1 + 1);
					setRect(container, component, layer, hr, p0, p1);
					
					double fract = getMedianSetting() * (((double) counts[cm1][lm1]) / (1 + medianHit));

					Color color = AccumulationManager.getInstance().getColor(getColorScaleModel(), fract);
					g.setColor(color);
					g.fillRect(hr.x, hr.y, hr.width, hr.height);
					
//					System.err.println("[" + component + ", " + layer + "] count: " +
//							counts[cm1][lm1] + "  fract: " + fract + "  medianCount: " +  medianHit);
					
				}
			}
			
		}

	}

	/**
	 * This adds the detector items. The AllDC view is not faithful to geometry. All
	 * we really uses in the number of superlayers, number of layers, and number of
	 * wires.
	 */
	@Override
	protected void addItems() {
	}
	
	
	//find the matching hits
	private ArrayList<RTPCHit> matchingHits(short component, byte layer) {
		ArrayList<RTPCHit> list =  new ArrayList<>();
		
		RTPCHitList hits = RTPC.getInstance().getHits();
//		System.err.println("DRAWING RTPC HITS count " + ((hits == null) ? 0 : hits.size()));
		
		if ((hits != null) && !hits.isEmpty()) {
			
			for (RTPCHit hit : hits) {
				if (hit != null) {
					if ((component ==  hit.component) && (layer == hit.layer)) {
						list.add(hit);
					}
				}
			}
		}

		
		return list;
	}

	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container   the base container for the view.
	 * @param screenPoint the pixel point
	 * @param wp  the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double wp,
			List<String> feedbackStrings) {
		
		boolean haveEvent = false;

		EventSourceType estype = ClasIoEventManager.getInstance().getEventSourceType();
		switch (estype) {
		case HIPOFILE:
//		case HIPORING:
		case ET:
		case EVIOFILE:
			haveEvent = (_eventManager.getCurrentEvent() != null);
			break;
		}

		// add some information about the current event
		if (!haveEvent) {
			feedbackStrings.add("$orange red$No event");
		} else {
			feedbackStrings.add("$orange red$" + "sequential event " + _eventManager.getSequentialEventNumber());
			
			int trueEventNum = _eventManager.getTrueEventNumber();
			feedbackStrings.add("$orange red$" + "true event " + ((trueEventNum < 0) ? "n/a" : trueEventNum ));
			feedbackStrings.add("$orange red$" + _eventManager.getCurrentSourceDescription());
		}

		
		if ((screenRect == null) || !screenRect.contains(screenPoint)) {
			return;
		}
		
		if ((wp.x < PHIMIN) || (wp.x > PHIMAX)) {
			return;
		}
		
		if ((wp.y < ZMIN) || (wp.y > ZMAX)) {
			return;
		}

		String locStr = String.format("(%6.2f" + UnicodeSupport.DEGREE + ", %6.2f mm" , 
				wp.x, wp.y);
		String fullStr = "(" + UnicodeSupport.SMALL_PHI + ", z ) = " + locStr;
		
		feedbackStrings.add(fullStr);
		
		
		//layer and component
		Point pp = new Point();
		worldToComponentLayer(container, pp, wp);
		
		short component = (short)(pp.x);
		byte layer = (byte)(pp.y);
		
		String clStr = "component " + pp.x + ", layer " + pp.y;
		feedbackStrings.add(clStr);
		
		//hits
		ArrayList<RTPCHit> list = matchingHits(component, layer);
		if ((list != null) && !list.isEmpty()) {
			feedbackStrings.add("num hits: " + list.size());
			for (RTPCHit hit : list) {
				
				String s = String.format("  adc %d ped %d time %-7.2f", hit.adc, hit.ped, hit.time);
				feedbackStrings.add(s);
			}
		}

	}
	
	//after return, p.x is the components 1..180 and p.y is layer 1..96
	private void worldToComponentLayer(IContainer container, Point p, Point2D.Double wp) {
		p.x = 1 + (int)((wp.x - PHIMIN)/DELPHI);
		p.y = 1 + (int)((wp.y - ZMIN)/DELZ);
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

		RTPCView view = createRTPCView();
		view.setBounds(vr);
		return view;

	}
	

	// draw the axes
	protected void drawAxes(Graphics g, IContainer container, Rectangle bounds) {
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Rectangle sr = getActiveScreenRectangle(container);
		// Rectangle sr = container.getInsetRectangle();

		g2.setFont(Fonts.defaultFont);
		FontMetrics fm = container.getComponent().getFontMetrics(g2.getFont());
		int fh = fm.getAscent();

		Rectangle2D.Double wr = new Rectangle2D.Double();
		container.localToWorld(sr, wr);
		Point2D.Double wp = new Point2D.Double();
		Point pp = new Point();

		// x values
		double del = wr.width / 40.;
		wp.y = wr.y;
		int bottom = sr.y + sr.height;
		for (int i = 0; i <= 40; i = i + 5) {
			wp.x = (int) (wr.x + del * i);

			if ((wp.x >= PHIMIN) && (wp.x <= PHIMAX)) {

				container.worldToLocal(pp, wp);
				g2.drawLine(pp.x, bottom, pp.x, bottom - 12);

				String vs = valueString(wp.x);
				int xs = pp.x - fm.stringWidth(vs) / 2;

				g2.drawString(vs, xs, bottom + fh + 3);

			} else {
				g2.drawLine(pp.x, bottom, pp.x, bottom - 5);
			}
		}

		del = wr.height / 40.;
		wp.x = wr.x;
		for (int i = 0; i <= 40; i = i + 5) {
			wp.y = (int) (wr.y + del * i);

			if ((wp.y >= ZMIN) && (wp.y <= ZMAX)) {

				container.worldToLocal(pp, wp);
				g2.drawLine(sr.x, pp.y, sr.x + 12, pp.y);
				String vs = valueString(wp.y);
				int xs = sr.x - fm.stringWidth(vs) - 3;

				g2.drawString(vs, xs, pp.y + fh / 2);
			} else {
				g2.drawLine(sr.x, pp.y, sr.x + 5, pp.y);
			}
		}

		// draw coordinate system

		int left = sr.x + 25;
		int right = left + 50;
		bottom = sr.y + sr.height - 20;
		int top = bottom - 50;
		// g.setFont(labelFont);
		// fm = getFontMetrics(labelFont);

		Rectangle r = new Rectangle(left - fm.stringWidth(UnicodeSupport.SMALL_PHI) - 4, top - fm.getHeight() / 2 + 1,
				(right - left + fm.stringWidth("z") + fm.stringWidth("z") + 9), (bottom - top) + fm.getHeight() + 2);

		g2.setColor(TRANS1);
		g2.fillRect(r.x, r.y, r.width, r.height);

		g2.setColor(X11Colors.getX11Color("dark red"));
		g2.drawLine(left, bottom, right, bottom);
		g2.drawLine(left, bottom, left, top);

		g2.drawString("z", left - fm.stringWidth("z") - 2, top + fm.getHeight() / 2 - 1);
		g2.drawString(UnicodeSupport.SMALL_PHI, right-3, bottom + fm.getHeight());

	}
	
	/**
	 * Get the default value for the adc threshold
	 * 
	 * @return the default value for the adc threshold
	 */
	@Override
	public int getAdcThresholdDefault() {
		return 340;
	}
	
	/**
	 * Some views (e.g., RTPC) have a threshold. Thay must override.
	 * @return the adc threshold for viewing hits
	 */
	@Override
	public int getAdcThreshold() {
		return _controlPanel.getAdcThresholdSlider().getValue();
	}


	

	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();

		// change target z?
		if (source == _controlPanel.getAdcThresholdSlider()) {
			_controlPanel.getAdcThresholdBorder().setTitle("ADC Display Threshold (" + getAdcThreshold() + ")");
			getContainer().refresh();
			_controlPanel.getAdcThresholdSlider().getParent().repaint();
		} 
	}

}
