package cnuphys.fastMCed.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.geometry.FTOFPanel;
import cnuphys.fastMCed.geometry.GeometryManager;
import cnuphys.fastMCed.streaming.StreamManager;
import cnuphys.fastMCed.view.sector.SectorView;

public class FTOFPanelItem extends PolygonItem {
	
	// ftof constants
	public static final int PANEL_1A = 0;
	public static final int PANEL_1B = 1;
	public static final int PANEL_2 = 2;
	public static final String panelNames[] = { "Panel 1A", "Panel 1B",
			"Panel 2" };
	private static final String briefPNames[] = { "1A", "1B",
	"2" };


	private FTOFPanel _ftofPanel;

	// 1-based sector
	private int _sector;

	// the container sector view
	private SectorView _view;

	/**
	 * Create a FTOFPanelItem
	 * 
	 * @param logLayer
	 *            the Layer this item is on.
	 */
	public FTOFPanelItem(LogicalLayer logLayer, FTOFPanel panel, int sector) {
		super(logLayer, getShell((SectorView) logLayer.getContainer().getView(), panel, sector));

		_ftofPanel = panel;
		_sector = sector;

		_name = (panel != null) ? name(panel.getPanelType()) : "??";

		// _style.setFillColor(X11Colors.getX11Color("Wheat", 128));
		_style.setFillColor(Color.white);
		_style.setLineWidth(0);
		_view = (SectorView) getLayer().getContainer().getView();
	}
	
	/**
	 * Get the name from the panel type
	 * 
	 * @param panelType
	 *            one of the constants (PANEL_1A, PANEL_1B, PANEL_2)
	 * @return the name of the panel type
	 */
	public static String name(int panelType) {
		if ((panelType < 0) || (panelType > 2)) {
			return "???";
		} else {
			return panelNames[panelType];
		}
	}


	/**
	 * Custom drawer for the item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		// TODO use dirty. If the item is not dirty, should be able to draw
		// the _lastDrawnPolygon directly;
		
		//don't draw if streaming
		if (StreamManager.getInstance().isStarted()) {
			return;
		}

		Point2D.Double path[] = getShell(_view, _ftofPanel, _sector);
		if (path == null) {
			return;
		}

		setPath(path);
		super.drawItem(g, container);

		// hits
		drawHits(g, container);

		Point2D.Double wp[] = GeometryManager.allocate(4);

		//draw a line marking paddle boundary
		for (int i = 0; i < _ftofPanel.getCount(); i++) {
			boolean isects = _ftofPanel.getPaddle(i, _view.getProjectionPlane(), wp);
			if (isects) {
			if (_sector > 3) {
				wp[0].y = -wp[0].y;
				wp[3].y = -wp[3].y;
			}

			WorldGraphicsUtilities.drawWorldLine(g, container, wp[0], wp[3], _style);
			}
		}

	}

	// draw any hits
	private void drawHits(Graphics g, IContainer container) {
		drawSingleModeHits(g, container);
	}




	// works for both showMcTruth and not
	private void drawSingleModeHits(Graphics g, IContainer container) {
		
		// draw tdc adc hits
//		TdcAdcHitList hits = FTOF.getInstance().getTdcAdcHits();
//		if ((hits != null) && !hits.isEmpty()) {
//			byte sect = (byte) _sector;
//			byte layer = (byte) (_ftofPanel.getPanelType() + 1);
//			for (TdcAdcHit hit : hits) {
//				if ((hit.sector == _sector) && (hit.layer == layer)) {
//					Point2D.Double wp[] = getPaddle(_view, hit.component - 1, _ftofPanel, _sector);
//
//					if (wp != null) {
//						Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);
//						WorldGraphicsUtilities.drawPath2D(g, container, path, Color.white, _style.getLineColor(), 0,
//								LineStyle.SOLID, true);
//						WorldGraphicsUtilities.drawPath2D(g, container, path, hits.adcColor(hit), _style.getLineColor(),
//								0, LineStyle.SOLID, true);
//					}
//				}
//			}
//		}



	}

	/**
	 * Get the FTOFPanel which contains the geometry
	 * 
	 * @return the ftofPanel
	 */
	public FTOFPanel getFtofPanel() {
		return _ftofPanel;
	}

	/**
	 * Get a paddle outline of the tof panel.
	 * 
	 * @param view
	 *            the view being rendered.
	 * @param index
	 *            the zero-based paddle index.
	 * @param panel
	 *            the panel holding the geometry data
	 * @param sector
	 *            the 1-based sector 1..6
	 * @return
	 */
	private static Point2D.Double[] getPaddle(SectorView view, int index, FTOFPanel panel, int sector) {

		//hide if don't fully intersect, which happens as phi moves away from midplane
		if (!doesPaddleFullIntersectPlane(view, index, panel)) {
			return null;
		}
		
		Point2D.Double wp[] = GeometryManager.allocate(4);

		panel.getPaddle(index, view.getProjectionPlane(), wp);

		// lower sectors (4, 5, 6) (need sign flip
		if (sector > 3) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
	}
	
	//does paddle fully intersect projection plane?
	private static boolean doesPaddleFullIntersectPlane(SectorView view, int index, 
			FTOFPanel panel) {
	
		return panel.paddleFullyIntersects(index, view.getProjectionPlane());
	}
	


	/**
	 * Get the shell of the tof panel.
	 * 
	 * @param view
	 *            the view being rendered.
	 * @param panel
	 *            the panel holding the geometry data
	 * @param sector
	 *            the 1-based sector 1..6
	 * @return
	 */
	private static Point2D.Double[] getShell(SectorView view, FTOFPanel panel, int sector) {
		if (panel == null) {
			return null;
		}

		Point2D.Double wp[] = panel.getShell(view.getProjectionPlane());

		// lower sectors (4, 5, 6) (need sign flip
		if (sector > 3) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
	}

	/**
	 * Add any appropriate feedback strings
	 * panel. Default implementation returns the item's name.
	 * 
	 * @param container
	 *            the Base container.
	 * @param screenPoint
	 *            the mouse location.
	 * @param worldPoint
	 *            the corresponding world point.
	 * @param feedbackStrings
	 *            the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {
		
		//no feedback if streaming
		if (StreamManager.getInstance().isStarted()) {
			return;
		}


		// which paddle?

		for (int index = 0; index < _ftofPanel.getCount(); index++) {

			Point2D.Double wp[] = getPaddle(_view, index, _ftofPanel, _sector);
			
			
			if (wp != null) {
				Path2D.Double path = WorldGraphicsUtilities.worldPolygonToPath(wp);

				if (path.contains(worldPoint)) {

//					// have a tdc adc hit?
//					TdcAdcHitList hits = FTOF.getInstance().getTdcAdcHits();
//					byte sect = (byte) _sector;
//					byte layer = (byte) (_ftofPanel.getPanelType() + 1);
//					short paddle = (short) (index + 1);
//					TdcAdcHit hit = hits.get(sect, layer, paddle);
//					if (hit != null) {
//						hit.tdcAdcFeedback(getName(), "paddle", feedbackStrings);
//					} else {
//						feedbackStrings
//								.add("$Orange Red$" + getName() + "  sector " + _sector + " paddle " + (index + 1));
//					}
//					feedbackStrings.add("$Orange Red$paddle length " + FTOFGeometry.getLength(_ftofPanel.getPanelType(), index) + " cm");
					
					break;
				} // path contains wp
			} // end wp != null
		} // end which paddle
	}

}
