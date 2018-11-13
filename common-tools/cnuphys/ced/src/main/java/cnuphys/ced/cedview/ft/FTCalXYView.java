package cnuphys.ced.cedview.ft;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.AdcHit;
import cnuphys.ced.event.data.AdcHitList;
import cnuphys.ced.event.data.FTCAL;
import cnuphys.ced.geometry.FTCALGeometry;

public class FTCalXYView extends CedXYView {
	
	
	//for naming clones
	private static int CLONE_COUNT = 0;
	
	//base title
	private static final String _baseTitle = "FTCal XY";


	// units are cm
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(
			20., -20., -40., 40.);

	// the CND xy polygons
	FTCalXYPolygon ftCalPoly[] = new FTCalXYPolygon[332];
	
	private short[] indices = new short[476];

	/**
	 * Create a FTCalXYView View
	 * 
	 */
	public FTCalXYView(Object... keyVals) {
		super(keyVals);
		
		for (int i = 0; i < indices.length; i++) {
			indices[i] = -1;
		}

		//good IDs are the component ids
		short goodIds[] = FTCALGeometry.getGoodIds();
		for (int i = 0; i < 332; i++) {
			int id = goodIds[i];
			indices[id] = (short)i; //reverse mapping
			ftCalPoly[i] = new FTCalXYPolygon(id);
		}

	}

	/**
	 * Create a FTCalXYView view
	 * 
	 * @return a FTCalXYView View
	 */
	public static FTCalXYView createFTCalXYView() {
		FTCalXYView view = null;

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.35);

		// make it square
		int width = d.width;
		int height = width;
		
		String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));


		// create the view
		view = new FTCalXYView(PropertySupport.WORLDSYSTEM,
				_defaultWorldRectangle, PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height, PropertySupport.LEFTMARGIN,
				LMARGIN, PropertySupport.TOPMARGIN, TMARGIN,
				PropertySupport.RIGHTMARGIN, RMARGIN, PropertySupport.BOTTOMMARGIN,
				BMARGIN, PropertySupport.TOOLBAR, true,
				PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true,
				PropertySupport.TITLE, title,
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.DISPLAYARRAY
				+ ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND,
				DisplayBits.ACCUMULATION
				+ DisplayBits.MCTRUTH, 3, 5);

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
				Rectangle b = component.getBounds();

				// ignore b.x and b.y as usual

				b.x = 0;
				b.y = 0;

				Rectangle screenRect = container.getInsetRectangle();
				g.setColor(Color.white);
				g.fillRect(screenRect.x, screenRect.y, screenRect.width,
						screenRect.height);
				
				drawGrid(g, container);


				for (FTCalXYPolygon poly : ftCalPoly) {
					poly.draw(g, container);
				}

			}
			
			//draw the ftcal grid
			private void drawGrid(Graphics g, IContainer container) {
				
				g.setColor(Color.lightGray);
				
				double range[] = new double[2];
				Point p0 = new Point();
				Point p1 = new Point();
				double vmax = FTCALGeometry.getMaxAbsXYExtent();
				
				for (int ix = -11; ix <= 11; ix++) {
					if ((ix < -3) || (ix > 4)) {
						FTCALGeometry.indexToRange(ix, range);
						container.worldToLocal(p0, range[0], -vmax);
						container.worldToLocal(p1, range[0], vmax);		
						g.drawLine(p0.x, p0.y, p1.x, p1.y);
						
						if (ix == 11) {
							container.worldToLocal(p0, range[1], -vmax);
							container.worldToLocal(p1, range[1], vmax);		
							g.drawLine(p0.x, p0.y, p1.x, p1.y);							
						}
						
						for (int iy = -11; iy <= 11; iy++) {
							if ((iy < -3) || (iy > 4)) {
								FTCALGeometry.indexToRange(iy, range);
								container.worldToLocal(p0, -vmax, range[0]);
								container.worldToLocal(p1, vmax, range[0]);		
								g.drawLine(p0.x, p0.y, p1.x, p1.y);
								
								if (iy == 11) {
									container.worldToLocal(p0, -vmax, range[1]);
									container.worldToLocal(p1, vmax, range[1]);		
									g.drawLine(p0.x, p0.y, p1.x, p1.y);							
								}
							}
						}
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
				}
				else {
					drawAccumulatedHits(g, container);
				}
				Rectangle screenRect = getActiveScreenRectangle(container);
				drawAxes(g, container, screenRect, false);
			}

		};
		getContainer().setAfterDraw(afterDraw);
	}
	
	
	//single event drawer
	private void drawSingleEventHits(Graphics g, IContainer container) {
		
		AdcHitList hits = FTCAL.getInstance().getHits();
		
		System.err.println("DRAWING FTCAL HITS count " + ((hits == null) ? 0 : hits.size()));
		if ((hits != null) && !hits.isEmpty()) {
			for (AdcHit hit : hits) {
				if (hit != null) {
					short id = hit.component;
					short index = indices[id];
					if (index >= 0) {
						FTCalXYPolygon poly = ftCalPoly[index];
						Color color = hits.adcColor(hit);
						g.setColor(color);
						g.fillPolygon(poly);
						g.setColor(Color.black);
						g.drawPolygon(poly);
					}
					else {
						System.err.println("indexing problem in FT");
					}
				}
			}
		}
	}
	
	//accumulated hits drawer
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		

		int medianHit = AccumulationManager.getInstance().getMedianFTCALCount();

		int acchits[] = AccumulationManager.getInstance().getAccumulatedFTCALData();
		for (int i = 0; i < acchits.length; i++) {
			if (acchits[i] > 0) {
				int index = indices[i];
				if (index >= 0) {
					FTCalXYPolygon poly = ftCalPoly[index];
					double fract = getMedianSetting()*(((double) acchits[i]) / (1 + medianHit));

					Color color = AccumulationManager.getInstance().getColor(fract);
					g.setColor(color);
					g.fillPolygon(poly);
					g.setColor(Color.black);
					g.drawPolygon(poly);
				}
				else {
					System.err.println("indexing problem in FTCAL");
				}
			}
		}
		
	}


	/**
	 * This adds the detector items. The AllDC view is not faithful to geometry.
	 * All we really uses in the number of superlayers, number of layers, and
	 * number of wires.
	 */
	@Override
	protected void addItems() {
	}

	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param screenPoint
	 *            the pixel point
	 * @param worldPoint
	 *            the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		basicFeedback(container, screenPoint, worldPoint, "cm", feedbackStrings);
		
		int xindex = FTCALGeometry.valToIndex(worldPoint.x);
		if (xindex != 0) {
			int yindex = FTCALGeometry.valToIndex(worldPoint.y);
		    if (yindex != 0) {

				boolean found = false;

				for (int index = 0; index < ftCalPoly.length; index++) {
					FTCalXYPolygon poly = ftCalPoly[index];
					found = poly.getFeedbackStrings(container, screenPoint, worldPoint, feedbackStrings);

					if (found) {

						AdcHitList hits = FTCAL.getInstance().getHits();
						if ((hits != null) && !hits.isEmpty()) {
							short component = FTCALGeometry.getGoodId(index);
							AdcHit hit = hits.get(1, 1, component);

							// hack
							if (hit == null) {
								hit = hits.get(0, 0, component);
							}
							if (hit != null) {
								hit.tdcAdcFeedback(feedbackStrings);
							}
						}

						break;
					}
				} //end for index
		    
		    }
		}



	}
	
	
	/**
	 * Clone the view. 
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT++;
		
		//limit
		if (CLONE_COUNT > 2) {
			return null;
		}
		
		Rectangle vr = getBounds();
		vr.x += 40;
		vr.y += 40;
		
		FTCalXYView view = createFTCalXYView();
		view.setBounds(vr);
		return view;

	}

}
