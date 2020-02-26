package cnuphys.ced.cedview.alltof;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.projecteddc.ISector;
import cnuphys.ced.cedview.projecteddc.SectorSelectorPanel;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.ced.geometry.FTOFGeometry;

public class TOFView extends CedView implements ISector {
	
	
	//for naming clones
	private static int CLONE_COUNT = 0;
	
	//base title
	private static final String _baseTitle = "CTOF and FTOF";

	
	// offset left and top
	private static int LEFT = 40;
	private static int TOP = 300;
	
	//some text stuff
	private static final Font _font = Fonts.hugeFont;

	public static final int FTOF_1A = 0;
	public static final int FTOF_1B = 1;
	public static final int FTOF_2  = 2;
	public static final int ALL_CTOF    = 3;

	//what sector 1..6
	private int _sector = 1;

	//detector logical layer
	private LogicalLayer _detectorLayer;

	//the shells
	private TOFShellItem[] shells;
	
	public TOFView(Object... keyVals) {
		super(keyVals);
		addItems();
		setBeforeDraw();
		setAfterDraw();
	}
	
	/**
	 * Convenience method for creating a TOFView View.
	 * 
	 * @return a new TOFView.
	 */
	public static TOFView createTOFView() {
		TOFView view = null;
		
		
		double xo = 0; // cm. 
		double zo = 0; // cm.
		double wheight = 650;
		double wwidth = 780;

		Dimension d = GraphicsUtilities.screenFraction(0.80);

		// give container same aspect ratio
		int height = d.height;
		int width = (int) ((wwidth * height) / wheight);

		String title = _baseTitle + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")"));

		// create the view
		view = new TOFView(PropertySupport.WORLDSYSTEM,
				new Rectangle2D.Double(zo, xo, wwidth, wheight),

				PropertySupport.LEFT, LEFT, PropertySupport.TOP, TOP,
				PropertySupport.WIDTH, width, PropertySupport.HEIGHT, height,
				PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS,
				CedView.NORANGETOOLBARBITS, PropertySupport.VISIBLE, true,
				PropertySupport.BACKGROUND,
				X11Colors.getX11Color("Antique White"),
				PropertySupport.TITLE, title,
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.DISPLAYARRAY 
				+ ControlPanel.DRAWLEGEND + ControlPanel.FEEDBACK
				+ ControlPanel.ACCUMULATIONLEGEND, DisplayBits.ACCUMULATION
				+ DisplayBits.MCTRUTH, 3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);
		
		//select which sector
		SectorSelectorPanel ssp = new SectorSelectorPanel(view);
		view._controlPanel.addSouth(ssp);

		view.pack();
		return view;
	}
	
	/**
	 * Add all the items on this view
	 */
	private void addItems() {
		_detectorLayer = getContainer().getLogicalLayer(_detectorLayerName);

		shells = new TOFShellItem[4];
//		public TOFShellItem(LogicalLayer layer,
//				double x0, double y0, String name, double width, double[] length) {
		
		shells[FTOF_1A] = new TOFShellItem(_detectorLayer, this, FTOF_1A, 10, 270, "Panel 1A", 15.22, FTOFGeometry.getLengths(FTOF_1A));
		shells[FTOF_1B] = new TOFShellItem(_detectorLayer, this, FTOF_1B, 320, 10,  "Panel 1B", 6.09, FTOFGeometry.getLengths(FTOF_1B));
		shells[FTOF_2] = new TOFShellItem(_detectorLayer, this, FTOF_2, 320, 400,  "Panel 2", 6.09, FTOFGeometry.getLengths(FTOF_2));

		double ctoflengths[] = new double[48];
		//fake lengths
		for (int i = 0; i < 48; i++) {
			ctoflengths[i] = 200;
		}
		shells[ALL_CTOF] = new TOFShellItem(_detectorLayer, this, ALL_CTOF, 90, 10,  "CTOF", 5.0, ctoflengths);

	}

	/**
	 * Set the views before draw
	 */
	private void setBeforeDraw() {
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				
				//draw text above panel 2
				if (shells[FTOF_2] != null) {
					Rectangle rr = new Rectangle();
					shells[FTOF_2].getStripRectangle(container, 4, rr);
					g.setFont(_font);
					g.setColor(Color.black);
					FontMetrics fm = container.getComponent().getFontMetrics(_font);
					
					int xc = rr.x + rr.width/2;
					int y = rr.y - 6*fm.getHeight();
					
					y = drawString(g, xc, y, "Sector " + _sector + " for FTOF 1A, 1B, 2", fm);
					y = drawString(g, xc, y, "Change sector at bottom right", fm);
					y = drawString(g, xc, y, "(Always shows complete CTOF)", fm);
					
				}
			}

		};
		getContainer().setBeforeDraw(beforeDraw);
	}
	
	private int drawString(Graphics g, int xc, int y, String s, FontMetrics fm) {
		g.drawString(s, xc-fm.stringWidth(s)/2, y);
		return y + fm.getHeight()+2;
	}

	/**
	 * Set the views before draw
	 */
	private void setAfterDraw() {
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				
				if (ClasIoEventManager.getInstance().isAccumulating()) {
					return;
				}

				if (isSingleEventMode()) {
					drawSingleEventData(g, container);
				}
				else {
					drawAccumulatedData(g, container);
				}
			}

		};
		getContainer().setAfterDraw(afterDraw);
	}
	
	
	private void drawSingleEventData(Graphics g, IContainer container) {
		//FTOF
		TdcAdcHitList hits = FTOF.getInstance().getTdcAdcHits();
		if ((hits != null) && !hits.isEmpty()) {
			
			Rectangle rr = new Rectangle();
			for (TdcAdcHit hit : hits) {
				if ((hit != null) && (hit.sector == _sector)) {
					int layer0 = hit.layer - 1;
					if ((layer0 >= 0) && (layer0 < 3)) {
						shells[layer0].getStripRectangle(container, hit.component-1, rr);
						Color color = hits.adcColor(hit);
						g.setColor(color);
						g.fillRect(rr.x, rr.y, rr.width, rr.height);
						g.setColor(Color.black);
						g.drawRect(rr.x, rr.y, rr.width, rr.height);
					}
				}
			}
		}
		
		
		//CTOF
		hits = CTOF.getInstance().getHits();
		if ((hits != null) && !hits.isEmpty()) {
			
			Rectangle rr = new Rectangle();
			for (TdcAdcHit hit : hits) {
				if (hit != null) {
					shells[ALL_CTOF].getStripRectangle(container, hit.component-1, rr);
					Color color = hits.adcColor(hit);
					g.setColor(color);
					g.fillRect(rr.x, rr.y, rr.width, rr.height);
					g.setColor(Color.black);
					g.drawRect(rr.x, rr.y, rr.width, rr.height);
				}
			}
		}

	}
	
	private void drawAccumulatedData(Graphics g, IContainer container) {
		
		Rectangle rr = new Rectangle();

		//FTOF
		drawAccumulatedData(g, container, FTOF_1A, rr);
		drawAccumulatedData(g, container, FTOF_1B, rr);
		drawAccumulatedData(g, container, FTOF_2, rr);
		
		//CTOF
		int medianHit = AccumulationManager.getInstance().getMedianCTOFCount();

		int ctofData[] = AccumulationManager.getInstance()
				.getAccumulatedCTOFData();
		
		for (int index = 0; index < 48; index++) {
			int hitCount = ctofData[index];
			shells[ALL_CTOF].getStripRectangle(container, index, rr);
		
			double fract = getMedianSetting()*(((double) hitCount) / (1 + medianHit));

			Color color = AccumulationManager.getInstance()
					.getColor(fract);
			g.setColor(color);
			g.fillRect(rr.x, rr.y, rr.width, rr.height);
			g.setColor(Color.black);
			g.drawRect(rr.x, rr.y, rr.width, rr.height);
		}

	}
	
	private void drawAccumulatedData(Graphics g, IContainer container, int panelType, Rectangle rr) {
		

		int hits[][] = null;
	
		int medianHit = 0;
		
		switch (panelType) {
		case FTOF.PANEL_1A:
			medianHit = AccumulationManager.getInstance().getMedianFTOF1ACount();
			hits = AccumulationManager.getInstance().getAccumulatedFTOF1AData();
			break;
		case FTOF.PANEL_1B:
			medianHit = AccumulationManager.getInstance().getMedianFTOF1BCount();
			hits = AccumulationManager.getInstance().getAccumulatedFTOF1BData();
			break;
		case FTOF.PANEL_2:
			medianHit = AccumulationManager.getInstance().getMedianFTOF2Count();
			hits = AccumulationManager.getInstance().getAccumulatedFTOF2Data();
			break;
		}

		
		if (hits != null) {
			int sect0 = _sector - 1;
			for (int paddle0 = 0; paddle0 < hits[sect0].length; paddle0++) {

				int hit = hits[sect0][paddle0];
				double fract = this.getMedianSetting() *(((double) hit) / (1 + medianHit));

				Color color = AccumulationManager.getInstance().getColor(fract);
				
				shells[panelType].getStripRectangle(container, paddle0, rr);
				g.setColor(color);
				g.fillRect(rr.x, rr.y, rr.width, rr.height);
				g.setColor(Color.black);
				g.drawRect(rr.x, rr.y, rr.width, rr.height);

			}
		}

	}


	/**
	 * Set the sector
	 * @param sector the new sector
	 */
	@Override
	public void setSector(int sector) {
		_sector = sector;
		getContainer().setDirty(true);
		getContainer().refresh();
	}

	
	/**
	 * Every view should be able to say what sector the current point location
	 * represents.
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param screenPoint
	 *            the pixel point
	 * @param worldPoint
	 *            the corresponding world location.
	 * @return the sector [1..6].
	 */
	@Override
	public int getSector(IContainer container, Point screenPoint, Double worldPoint) {
		return _sector;
	}
	
	/**
	 * Get the sector 1..6
	 * @return the sector
	 */
	@Override
	public int getSector() {
		return _sector;
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
		
		TOFView view = createTOFView();
		view.setBounds(vr);
		return view;

	}
	
}
