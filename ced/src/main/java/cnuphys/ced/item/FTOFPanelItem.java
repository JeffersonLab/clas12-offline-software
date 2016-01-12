package cnuphys.ced.item;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.DataSupport;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.geometry.FTOFPanel;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;

public class FTOFPanelItem extends PolygonItem {

	protected static RenderingHints renderHints = new RenderingHints(
			RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	static {
		renderHints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
	};

	private FTOFPanel _ftofPanel;

	// 1-based sector
	private int _sector;

	// the container sector view
	private SectorView _view;

	/**
	 * Create a FTOFPanelItem
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 */
	public FTOFPanelItem(LogicalLayer layer, FTOFPanel panel, int sector) {
		super(layer, getShell((SectorView) layer.getContainer().getView(),
				panel, sector));

		_ftofPanel = panel;
		_sector = sector;

		_name = (panel != null) ? FTOF.name(panel
				.getPanelType()) : "??";

		// _style.setFillColor(X11Colors.getX11Color("Wheat", 128));
		_style.setFillColor(Color.white);
		_style.setLineWidth(0);
		_view = (SectorView) getLayer().getContainer().getView();
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
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		Point2D.Double path[] = getShell(_view, _ftofPanel, _sector);
		if (path == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(renderHints);

		setPath(path);
		super.drawItem(g, container);

		// hits
		drawHits(g, container);

		Point2D.Double wp[] = new Point2D.Double[2];
		wp[0] = new Point2D.Double();
		wp[1] = new Point2D.Double();

		for (int i = 0; i < _ftofPanel.getCount(); i++) {
			if (_ftofPanel.getP0P3Edge(i, _view.getTransformation3D(), wp)) {
				if (_sector > 3) {
					wp[0].y = -wp[0].y;
					wp[1].y = -wp[1].y;
				}

				WorldGraphicsUtilities.drawWorldLine(g, container, wp[0],
						wp[1], _style);
			}
		}

	}

	// draw any hits
	private void drawHits(Graphics g, IContainer container) {
		
		if (_view.isSingleEventMode()) {
			drawSingleModeHits(g, container);
		}
		else {
			drawAccumulatedHits(g, container);
		}
	}
	
	
	private void drawAccumulatedHits(Graphics g, IContainer container) {
		int hits[][] = null;
		int maxHit = AccumulationManager.getInstance().getMaxDgtzFtofCount();
		
		int panelType = _ftofPanel.getPanelType();
		switch (panelType) {
		case FTOF.PANEL_1A:
			hits = AccumulationManager.getInstance().getAccumulatedDgtzFtof1aData();
			break;
		case FTOF.PANEL_1B:
			hits = AccumulationManager.getInstance().getAccumulatedDgtzFtof1bData();
			break;
		case FTOF.PANEL_2:
			hits = AccumulationManager.getInstance().getAccumulatedDgtzFtof2Data();
			break;
		}
		
		if (hits != null) {
			int sect0 = _sector - 1;
			for (int paddle0 = 0; paddle0 < hits[0].length; paddle0++) {
				
				int hit = hits[sect0][paddle0];
				double fract;
				if (_view.isSimpleAccumulatedMode()) {
					fract = ((double) hit) / maxHit;
				}
				else {
					fract = Math.log(hit+1.)/Math.log(maxHit+1.);
				}

				Color fc = AccumulationManager.getInstance().getColor(fract);
				Point2D.Double wp[] = getPaddle(_view, paddle0,
						_ftofPanel, _sector);

				if (wp != null) {
					Path2D.Double path = WorldGraphicsUtilities
							.worldPolygonToPath(wp);
					WorldGraphicsUtilities.drawPath2D(g, container, path, fc,
							_style.getLineColor(), 0, LineStyle.SOLID, true);
				}
			}
		}

	}

	//works for both showMcTruth and not
	private void drawSingleModeHits(Graphics g, IContainer container) {
		// the overall container

		int panelType = _ftofPanel.getPanelType();

		int hitCount = FTOF.hitCount(panelType);

		if (hitCount < 1) {
			return;
		}
		int pid[] = FTOF.pid(panelType);
		int sector[] = FTOF.sector(panelType);
		int paddles[] = FTOF.paddle(panelType);
		
		if (!_view.showMcTruth()) {
			pid = null;
		}
		
		if ((sector == null) || (paddles == null)) {
			return;
		}

		Color default_fc = Color.red;

		for (int i = 0; i < hitCount; i++) {
			if (sector[i] == _sector) {
				Color fc = default_fc;
				if (pid != null) {
					LundId lid = LundSupport.getInstance().get(pid[i]);
					if (lid != null) {
						fc = lid.getStyle().getFillColor();
					}
				}

				Point2D.Double wp[] = getPaddle(_view, (paddles[i] - 1),
						_ftofPanel, _sector);

				if (wp != null) {
					Path2D.Double path = WorldGraphicsUtilities
							.worldPolygonToPath(wp);
					WorldGraphicsUtilities.drawPath2D(g, container, path, fc,
							_style.getLineColor(), 0, LineStyle.SOLID, true);
				}

			}
		}

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
	private static Point2D.Double[] getPaddle(SectorView view, int index,
			FTOFPanel panel, int sector) {
		Point2D.Double wp[] = panel
				.getPaddle(index, view.getTransformation3D());

		if (wp == null) {
			return null;
		}

		// lower sectors (4, 5, 6) (need sign flip
		if (sector > 3) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
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
	private static Point2D.Double[] getShell(SectorView view, FTOFPanel panel,
			int sector) {
		if (panel == null) {
			return null;
		}

		Point2D.Double wp[] = panel.getShell(view.getTransformation3D());

		// lower sectors (4, 5, 6) (need sign flip
		if ((sector > 3) && (wp != null)) {
			for (Point2D.Double twp : wp) {
				twp.y = -twp.y;
			}
		}

		return wp;
	}

	/**
	 * Add any appropriate feedback strings for the headsup display or feedback
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
	public void getFeedbackStrings(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, List<String> feedbackStrings) {

		// which paddle?

		for (int index = 0; index < _ftofPanel.getCount(); index++) {

			Point2D.Double wp[] = getPaddle(_view, index, _ftofPanel, _sector);
			if (wp != null) {
				Path2D.Double path = WorldGraphicsUtilities
						.worldPolygonToPath(wp);

				if (path.contains(worldPoint)) {

					// hit?

					int panelType = _ftofPanel.getPanelType();

					int hitIndex = FTOF.hitIndex(_sector, index + 1,
							panelType);

					if (hitIndex < 0) {
						feedbackStrings.add("$Orange Red$" + getName()
								+ "  sector " + _sector + " paddle "
								+ (index + 1));
					} else {
						DataSupport.truePidFeedback(FTOF.pid(panelType), hitIndex, feedbackStrings);
						FTOF.dgtzFeedback(hitIndex, panelType, feedbackStrings);
					}

					break;
				} // path contains wp
			} // end wp != null
		} // end which paddle
	}

}
