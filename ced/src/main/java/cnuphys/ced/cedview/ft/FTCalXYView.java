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
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.CedXYView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.geometry.FTCALGeometry;

public class FTCalXYView extends CedXYView {

	// units are cm
	private static Rectangle2D.Double _defaultWorldRectangle = new Rectangle2D.Double(
			20., -20., -40., 40.);

	// the CND xy polygons
	FTCalXYPolygon ftCalPoly[] = new FTCalXYPolygon[332];

	/**
	 * Create a BST View
	 * 
	 * @param keyVals
	 */
	public FTCalXYView(Object... keyVals) {
		super(keyVals);

		//
		// _crossDrawer = new CrossDrawerXY(this);
		//
		// // draws any swum trajectories (in the after draw)
		// _swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);
		//
		// // default properties
		// setBooleanProperty(DisplayArray.HITCROSS_PROPERTY, true);
		//
		// //add the polys
		int goodIds[] = FTCALGeometry.getGoodIds();
		for (int i = 0; i < 332; i++) {
			int id = goodIds[i];
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

		// create the view
		view = new FTCalXYView(PropertySupport.WORLDSYSTEM,
				_defaultWorldRectangle, PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height, PropertySupport.LEFTMARGIN,
				LMARGIN, PropertySupport.TOPMARGIN, TMARGIN,
				PropertySupport.RIGHTMARGIN, RMARGIN, PropertySupport.BOTTOMMARGIN,
				BMARGIN, PropertySupport.TOOLBAR, true,
				PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true, PropertySupport.HEADSUP, false,
				PropertySupport.TITLE, "FTCal XY",
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.DISPLAYARRAY
				+ ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND,
				DisplayBits.ACCUMULATION
				+ DisplayBits.MCTRUTH, 2, 6);

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

				for (FTCalXYPolygon poly : ftCalPoly) {
					poly.draw(g, container);
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
				//
				// if (!_eventManager.isAccumulating()) {
				// drawBSTHits(g, container);
				//
				// if (showReconsBSTCrosses()) {
				// _crossDrawer.draw(g, container);
				// }
				//
				// if (showCosmics()) {
				// drawCosmicTracks(g, container);
				// }
				//
				// _swimTrajectoryDrawer.draw(g, container);
				Rectangle screenRect = getActiveScreenRectangle(container);
				drawAxes(g, container, screenRect, false);
				// }

			}

		};
		getContainer().setAfterDraw(afterDraw);
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

		double rad = Math.hypot(worldPoint.x, worldPoint.y);
		boolean found = false;

		if ((rad > 4.6) && (rad < 18)) {

			for (FTCalXYPolygon poly : ftCalPoly) {
				found = poly.getFeedbackStrings(container, screenPoint,
						worldPoint, feedbackStrings);
				if (found) {
					break;
				}
			}

		}


	}

}
