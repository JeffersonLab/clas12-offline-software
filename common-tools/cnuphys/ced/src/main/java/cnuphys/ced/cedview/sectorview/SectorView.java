package cnuphys.ced.cedview.sectorview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jlab.geom.prim.Plane3D;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.central.CentralSupport;
import cnuphys.ced.common.CrossDrawer;
import cnuphys.ced.common.FMTCrossDrawer;
import cnuphys.ced.common.SuperLayerDrawing;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.geometry.BSTxyPanel;
import cnuphys.ced.geometry.FTOFGeometry;
import cnuphys.ced.geometry.FTOFPanel;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.ced.item.BeamLineItem;
import cnuphys.ced.item.FTOFPanelItem;
import cnuphys.ced.item.MagFieldItem;
import cnuphys.ced.item.SectorECItem;
import cnuphys.ced.item.SectorHTCCItem;
import cnuphys.ced.item.SectorLTCCItem;
import cnuphys.ced.item.SectorPCALItem;
import cnuphys.ced.item.SectorSuperLayer;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.SwimTrajectory2D;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.container.ScaleDrawer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.graphics.world.WorldPolygon;
import cnuphys.bCNU.item.YouAreHereItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.bCNU.util.VectorSupport;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.bCNU.view.PlotView;
import cnuphys.bCNU.view.ViewManager;

/**
 * This is the classic sector view.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class SectorView extends CedView implements ChangeListener {

	// each sector view has an upper and lower sector: 1-4, 2-5, 3-6
	public static final int UPPER_SECTOR = 0;
	public static final int LOWER_SECTOR = 1;

	// offset left and right
	private static int LEFT = 140;
	private static int DELTAH = 80;
	private static int TOP = 40;
	private static int DELTAV = 30;

	// for tilted axis
	private static final Color TRANSCOLOR = new Color(0, 0, 0, 64);
	private static final Color TRANSCOLOR2 = new Color(255, 255, 255, 64);
	
	// line stroke
	private static Stroke stroke = GraphicsUtilities.getStroke(1.5f,
			LineStyle.SOLID);

	// fill color
	private static final Color BSTHITFILL = new Color(255, 128, 0, 64);
	// private static final Color TRANS = new Color(192, 192, 192, 128);

	// HTCC Items, 8 per sector, not geometrically realistic
	private SectorHTCCItem _htcc[][] = new SectorHTCCItem[4][2];
	
	// LTCC Items, 36 per sector, not geometrically realistic
	private SectorLTCCItem _ltcc[][] = new SectorLTCCItem[18][2];
	
	//for naming clones
	private static int CLONE_COUNT[] = {0, 0, 0};
	
	//for special debug points
	private static ArrayList<DebugPoint> _debugPoints = new ArrayList<DebugPoint>(10);
	
	// superlayer (graphical) items. The first index [0..1] is for upper and
	// lower sectors.
	// the second is for for super layer 0..5debug
	private SectorSuperLayer _superLayers[][] = new SectorSuperLayer[2][6];

	// determines if the wire intersections must be recalculated. This is caused
	// by a change in phi using the phi slider.
	private Boolean _wiresDirty = true;

	// small number test
	private static final double TINY = 1.0e-10;

	// Holds what pair of sectors are being displayed
	private DisplaySectors _displaySectors;

	// the nominal target z in cm
	private double _targetZ = 0.0;

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	// for drawing MC hits
	private McHitDrawer _mcHitDrawer;

	// drawing reconstructed data
	private ReconDrawer _reconDrawer;

	// the value of phi in degrees (-30 to 30) relative to midplane.
	// Also some cached trig
	// private double _phiRelMidPlane = 0.0;

	// a scale drawer
	private ScaleDrawer _scaleDrawer = new ScaleDrawer("cm",
			ScaleDrawer.BOTTOMLEFT);

	// reconstructed cross drawer for DC (and feedback handler)
	private CrossDrawer _dcCrossDrawer;
	
	//for fmt
	private FMTCrossDrawer _fmtCrossDrawer;

	private static Color plotColors[] = { X11Colors.getX11Color("Dark Red"),
			X11Colors.getX11Color("Dark Blue"),
			X11Colors.getX11Color("Dark Green"), Color.black, Color.gray,
			X11Colors.getX11Color("wheat") };

	/**
	 * Create a sector view
	 * 
	 * @param keyVals
	 *            variable set of arguments.
	 */
	private SectorView(DisplaySectors displaySectors, Object... keyVals) {
		super(keyVals);
		_displaySectors = displaySectors;
		
		//the projection plane starts as midplane
		projectionPlane = GeometryManager.constantPhiPlane(0);
		
		addItems();
		setBeforeDraw();
		setAfterDraw();

		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);

		// dc cross drawer
		_dcCrossDrawer = new CrossDrawer(this);
		
		// fmt cross drawer
		_fmtCrossDrawer = new FMTCrossDrawer(this);


		// MC hit drawer
		_mcHitDrawer = new McHitDrawer(this);

		// Recon drawer
		_reconDrawer = new ReconDrawer(this);

		// debug points
//		if (_debugPoints.isEmpty()) {
//			_debugPoints.add(new DebugPoint(0, 4.257, 300.4));
//		}
	}

	/**
	 * Convenience method for creating a Sector View.
	 * 
	 * @param displaySectors
	 *            controls which opposite sectors are displayed.
	 * @return a new SectorView.
	 */
	public static SectorView createSectorView(DisplaySectors displaySectors) {
		SectorView view = null;

		double xo = -450.0; // cm. Think of sector 1. x is "vertical"
		double zo = -10.0; // cm. Think of sector 1. z is "horizontal"
		double wheight = -2 * xo;
		double wwidth = 840;

		Dimension d = GraphicsUtilities.screenFraction(0.65);

		// give container same aspect ratio
		int height = d.height;
		int width = (int) ((wwidth * height) / wheight);

		// give the view a title based on what sectors are displayed
		String title = "Sectors ";
		switch (displaySectors) {
		case SECTORS14:
			title += "1 and 4";
			break;
		case SECTORS25:
			title += "2 and 5";
			break;
		case SECTORS36:
			title += "3 and 6";
			break;
		}
		
		if (CLONE_COUNT[displaySectors.ordinal()] > 0) {
			title += "_(" + CLONE_COUNT[displaySectors.ordinal()] + ")";
		}

		// create the view
		view = new SectorView(displaySectors, PropertySupport.WORLDSYSTEM,
				new Rectangle2D.Double(zo, xo, wwidth, wheight),

				PropertySupport.LEFT, LEFT, PropertySupport.TOP, TOP,
				PropertySupport.WIDTH, width, PropertySupport.HEIGHT, height,
				PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS,
				CedView.TOOLBARBITS, PropertySupport.VISIBLE, true,
				PropertySupport.BACKGROUND,
				X11Colors.getX11Color("Alice Blue").darker(),
				// PropertySupport.BACKGROUND,
				// X11Colors.getX11Color("dark slate gray"),
				// PropertySupport.BACKGROUND, Color.lightGray,
				PropertySupport.TITLE, title,
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.NOISECONTROL
				+ ControlPanel.DISPLAYARRAY + ControlPanel.PHISLIDER
				+ ControlPanel.DRAWLEGEND + ControlPanel.FEEDBACK
				+ ControlPanel.FIELDLEGEND + ControlPanel.TARGETSLIDER
				+ ControlPanel.ACCUMULATIONLEGEND, DisplayBits.MAGFIELD
				+ DisplayBits.CROSSES
				+ DisplayBits.RECONHITS 
				+ DisplayBits.CLUSTERS + DisplayBits.FMTCROSSES
				+ DisplayBits.DC_HITS + DisplayBits.SEGMENTS 
				+ DisplayBits.GLOBAL_HB + DisplayBits.GLOBAL_TB
				+ DisplayBits.ACCUMULATION //+ DisplayBits.SCALE
				+ DisplayBits.MCTRUTH, 3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);

		view._displaySectors = displaySectors;
		view.pack();

		LEFT += DELTAH;
		TOP += DELTAV;

		return view;
	}

	/**
	 * Add all the items on this view
	 */
	private void addItems() {

		// add a field object, which won't do anything unless we can read in the
		// field.
		LogicalLayer magneticFieldLayer = getContainer().getLogicalLayer(
				_magneticFieldLayerName);
		new MagFieldItem(magneticFieldLayer, this);
		magneticFieldLayer.setVisible(false);

		LogicalLayer detectorLayer = getContainer().getLogicalLayer(
				_detectorLayerName);
		new BeamLineItem(detectorLayer);
		
		//add the ltcc items
		for (int ring = 1; ring <= 18; ring++) {
			for (int half = 1; half <= 2; half++) {
				
				switch (_displaySectors) {
				case SECTORS14:
					_ltcc[ring-1][half-1] = new SectorLTCCItem(detectorLayer, 
							this, 1, ring, half);
					_ltcc[ring-1][half-1] = new SectorLTCCItem(detectorLayer, 
									this, 4, ring, half);
					break;

				case SECTORS25:
					_ltcc[ring-1][half-1] = new SectorLTCCItem(detectorLayer, 
							this, 2, ring, half);
					_ltcc[ring-1][half-1] = new SectorLTCCItem(detectorLayer, 
									this, 5, ring, half);
					break;

				case SECTORS36:
					_ltcc[ring-1][half-1] = new SectorLTCCItem(detectorLayer, 
							this, 3, ring, half);
					_ltcc[ring-1][half-1] = new SectorLTCCItem(detectorLayer, 
									this, 6, ring, half);
					break;
				}
				
			}
		}

		
		//add the htcc items
		for (int ring = 1; ring <= 4; ring++) {
			for (int half = 1; half <= 2; half++) {
				
				switch (_displaySectors) {
				case SECTORS14:
					_htcc[ring-1][half-1] = new SectorHTCCItem(detectorLayer, 
							this, 1, ring, half);
							_htcc[ring-1][half-1] = new SectorHTCCItem(detectorLayer, 
									this, 4, ring, half);
					break;

				case SECTORS25:
					_htcc[ring-1][half-1] = new SectorHTCCItem(detectorLayer, 
							this, 2, ring, half);
							_htcc[ring-1][half-1] = new SectorHTCCItem(detectorLayer, 
									this, 5, ring, half);
					break;

				case SECTORS36:
					_htcc[ring-1][half-1] = new SectorHTCCItem(detectorLayer, 
							this, 3, ring, half);
							_htcc[ring-1][half-1] = new SectorHTCCItem(detectorLayer, 
									this, 6, ring, half);
					break;
				}
				
			}
		}

		// add the superlayer items
		for (int superLayer = 0; superLayer < 6; superLayer++) {
			// SectorSuperLayer constructor expects a 1-based index

			switch (_displaySectors) {
			case SECTORS14:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(
						detectorLayer, this, 1, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(
						detectorLayer, this, 4, superLayer + 1);
				break;

			case SECTORS25:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(
						detectorLayer, this, 2, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(
						detectorLayer, this, 5, superLayer + 1);
				break;

			case SECTORS36:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(
						detectorLayer, this, 3, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(
						detectorLayer, this, 6, superLayer + 1);
				break;
			}

			_superLayers[UPPER_SECTOR][superLayer].getStyle().setFillColor(
					Color.gray);
			_superLayers[LOWER_SECTOR][superLayer].getStyle().setFillColor(
					Color.gray);
		}

		// add forward time of flight items
		FTOFPanel panels[] = FTOFGeometry.getFtofPanel();
		for (FTOFPanel ftof : panels) {
			switch (_displaySectors) {
			case SECTORS14:
				new FTOFPanelItem(detectorLayer, ftof, 1);
				new FTOFPanelItem(detectorLayer, ftof, 4);
				break;

			case SECTORS25:
				new FTOFPanelItem(detectorLayer, ftof, 2);
				new FTOFPanelItem(detectorLayer, ftof, 5);
				break;

			case SECTORS36:
				new FTOFPanelItem(detectorLayer, ftof, 3);
				new FTOFPanelItem(detectorLayer, ftof, 6);
				break;
			}
		}

		// add EC items
		switch (_displaySectors) {
		case SECTORS14:
			for (int planeIndex = 0; planeIndex < 2; planeIndex++) {
				for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
					new SectorECItem(detectorLayer, planeIndex, stripIndex, 1);
					new SectorECItem(detectorLayer, planeIndex, stripIndex, 4);
				}
			}
			break;

		case SECTORS25:
			for (int planeIndex = 0; planeIndex < 2; planeIndex++) {
				for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
					new SectorECItem(detectorLayer, planeIndex, stripIndex, 2);
					new SectorECItem(detectorLayer, planeIndex, stripIndex, 5);
				}
			}
			break;

		case SECTORS36:
			for (int planeIndex = 0; planeIndex < 2; planeIndex++) {
				for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
					new SectorECItem(detectorLayer, planeIndex, stripIndex, 3);
					new SectorECItem(detectorLayer, planeIndex, stripIndex, 6);
				}
			}
			break;
		} // end switch

		// add PCAL items
		switch (_displaySectors) {
		case SECTORS14:
			for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
				new SectorPCALItem(detectorLayer, stripIndex, 1);
				new SectorPCALItem(detectorLayer, stripIndex, 4);
			}
			break;

		case SECTORS25:
			for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
				new SectorPCALItem(detectorLayer, stripIndex, 2);
				new SectorPCALItem(detectorLayer, stripIndex, 5);
			}
			break;

		case SECTORS36:
			for (int stripIndex = 0; stripIndex < 3; stripIndex++) {
				new SectorPCALItem(detectorLayer, stripIndex, 3);
				new SectorPCALItem(detectorLayer, stripIndex, 6);
			}
			break;
		} // end switch

	}
	
	/**
	 * Get the super layer drawer
	 * @param upperLower 0 for upper sector, 1 for lower sector
	 * @param superLayer super layer 1..6
	 * @return the drawer
	 */
	public SuperLayerDrawing getSuperLayerDrawer(int upperLower, int superLayer) {
		return _superLayers[upperLower][superLayer-1].getSuperLayerDrawer();
	}


	/**
	 * Set the views before draw
	 */
	private void setBeforeDraw() {
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				drawTiltedAxis(g, container, UPPER_SECTOR);
				drawTiltedAxis(g, container, LOWER_SECTOR);

				// if the wires are dirty, recompute their projections
				if (_wiresDirty) {

					for (int superLayer = 0; superLayer < 6; superLayer++) {
						_superLayers[UPPER_SECTOR][superLayer].dirtyWires();
						_superLayers[LOWER_SECTOR][superLayer].dirtyWires();
					}
					_wiresDirty = false;

				}
			}

		};
		getContainer().setBeforeDraw(beforeDraw);
	}

	// draw the tilted axis
	private void drawTiltedAxis(Graphics g, IContainer container, int sector) {

		Point2D.Double wp0 = new Point2D.Double();
		Point2D.Double wp1 = new Point2D.Double();
		Point p0 = new Point();
		Point p1 = new Point();

		double theta = Math.toRadians(25.);
		double phi;
		if (_displaySectors == DisplaySectors.SECTORS25) {
			phi = 60;
		} else if (_displaySectors == DisplaySectors.SECTORS36) {
			phi = 120.0;
		} else {
			phi = 0;
		}
		// lower sector
		if (sector == LOWER_SECTOR) {
			phi += 180;
		}
		phi = Math.toRadians(phi);

		projectClasToWorld(0, 0, 0, projectionPlane, wp0);
		container.worldToLocal(p0, wp0);

		for (int i = 1; i <= 10; i++) {
			double r = 100 * i;
			double rho = r * Math.sin(theta);
			double x = rho * Math.cos(phi);
			double y = rho * Math.sin(phi);
			double z = r * Math.cos(theta);
			projectClasToWorld(x, y, z, projectionPlane, wp1);

			container.worldToLocal(p1, wp1);

			if ((i % 2) == 0) {
				g.setColor(TRANSCOLOR);
			} else {
				g.setColor(TRANSCOLOR2);
			}
			g.drawLine(p0.x, p0.y, p1.x, p1.y);

			p0.x = p1.x;
			p0.y = p1.y;
		}
	}

	/**
	 * Set the views before draw
	 */
	private void setAfterDraw() {
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {

				// draw trajectories
				_swimTrajectoryDrawer.draw(g, container);

				// draw MC Hits
				_mcHitDrawer.draw(g, container);

				// draw reconstructed data
				_reconDrawer.draw(g, container);

				// draw bst panels
				drawBSTPanels(g, container);

				// draw reconstructed dc crosses
				
				if (showDCHBCrosses()) {
					_dcCrossDrawer.setMode(CrossDrawer.HB);
					_dcCrossDrawer.draw(g, container);
				}
				if (showDCTBCrosses()) {
					_dcCrossDrawer.setMode(CrossDrawer.TB);
					_dcCrossDrawer.draw(g, container);
				}
				
				//Other (not DC) Crosses
				if (showCrosses()) {
					_fmtCrossDrawer.draw(g, container);
				}

				// scale
				if ((_scaleDrawer != null) && showScale()) {
					_scaleDrawer.draw(g, container);
				}
				
				//secial debug points
				drawDebugPoints(g, container);

				// a clean rectangle
				Rectangle bounds = container.getComponent().getBounds();
				GraphicsUtilities.drawSimple3DRect(g, 0, 0, bounds.width - 1,
						bounds.height - 1, false);
			}

		};
		getContainer().setAfterDraw(afterDraw);
	}
	
	//points used for debugging
	private void drawDebugPoints(Graphics g, IContainer container) {
		for (DebugPoint dp : _debugPoints) {
			int sector = dp.sector();
			switch (_displaySectors) {
			case SECTORS14:
				if ((sector == 1) || (sector == 4)) {
					Point pp = dp.toLocal(container);
	//				System.err.println("DRAW DP AT " + pp);
					SymbolDraw.drawCross(g, pp.x, pp.y, 8, Color.BLACK);
				}
				break;
				
			case SECTORS25:
				if ((sector == 2) || (sector == 5)) {
					
				}
				break;
				
			case SECTORS36:
				if ((sector == 3) || (sector == 6)) {
					
				}
				break;
			}

		}
	}

	/**
	 * Get the display sectors which tell us which pair of sectors are being
	 * displayed
	 * 
	 * @return the display sectors type
	 */
	public DisplaySectors getDisplaySectors() {
		return _displaySectors;
	}

	
	/**
	 * From detector xyz get the projected world point.
	 * 
	 * @param x
	 *            the detector x coordinate
	 * @param y
	 *            the detector y coordinate
	 * @param z
	 *            the detector z coordinate
	 * @param wp
	 *            the projected 2D world point.
	 */
	@Override
	public void projectClasToWorld(double x, double y, double z,
			Plane3D projectionPlane, Point2D.Double wp) {
		
		super.projectClasToWorld(x, y, z, projectionPlane, wp);
		int sector = GeometryManager.getSector(x, y);
		if (sector > 3) {
		  wp.y = - wp.y;
		}
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
	 * @return the sector [1..6] or -1 for none.
	 */
	@Override
	public int getSector(IContainer container, Point screenPoint,
			Point2D.Double worldPoint) {
		boolean positive = worldPoint.y > 0.0;
		switch (_displaySectors) {
		case SECTORS14:
			return positive ? 1 : 4;

		case SECTORS25:
			return positive ? 2 : 5;

		case SECTORS36:
			return positive ? 3 : 6;
		}
		return -1;
	}
	
	/**
	 * Is the sector one of the two on this view
	 * @param sector the sector [1..6]
	 */
	public boolean containsSector(byte sector) {
		
		switch (_displaySectors) {
		case SECTORS14:
			return ((sector == 1) || (sector == 4));

		case SECTORS25:
			return ((sector == 2) || (sector == 5));

		case SECTORS36:
			return ((sector == 3) || (sector == 6));
		}
		
		return false;
	}

	/**
	 * This is used to listen for changes on components like sliders.
	 * 
	 * @param e
	 *            the causal event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();

		// change target z?
		if (source == _controlPanel.getTargetSlider()) {
			_targetZ = (_controlPanel.getTargetSlider().getValue());
			getContainer().refresh();
		} else if (source == _controlPanel.getPhiSlider()) {
			//change the projection plane
			projectionPlane = GeometryManager.constantPhiPlane(getSliderPhi());
			
			_wiresDirty = true;
			getContainer().setDirty(true);
			getContainer().refresh();
		}
	}

	/**
	 * Converts the local screen coordinate obtained by a previous localToWorld
	 * call to full 3D CLAS coordinates
	 * 
	 * @param screenPoint
	 *            the pixel point
	 * @param worldPoint
	 *            the corresponding world location.
	 * @param result
	 *            holds the result. It has five elements. Cartesian x, y, and z
	 *            are in 0, 1, and 2. Cylindrical rho and phi are in 3 and 3.
	 *            (And of course cylindrical z is the same as Cartesian z.)
	 */
	public void getCLASCordinates(IContainer container, Point screenPoint,
			Point2D.Double worldPoint, double result[]) {
		double x = worldPoint.y;
		double z = worldPoint.x;

		// we are essentially display a plan yp=0, with xp vertical and zp
		// horizontal. We need
		// to rotate around z by "phiRotate" to get the x and y coordinates.
		// Note
		// it is
		// a simple rotation since yp is zero
		double phiRotate = Math.toRadians(getPhiRotate());
		double y = x * Math.sin(phiRotate);
		x = x * Math.cos(phiRotate);

		double rho = x * x + y * y;
		rho = Math.sqrt(rho);

		// get absolute phi
		double absphi = getAbsolutePhi(container, screenPoint, worldPoint);

		result[0] = x;
		result[1] = y;
		result[2] = z;
		result[3] = rho;
		result[4] = absphi;
	}

	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container
	 *            the base container for the view.
	 * @param pp
	 *            the pixel point
	 * @param wp
	 *            the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {

		// get the common information
		super.getFeedbackStrings(container, pp, wp, feedbackStrings);

		double result[] = new double[3];
		worldToLabXYZ(wp, result);
		float x = (float)result[0];
		float y = (float)result[1];
		float z = (float)result[2];

		String xyz = "xyz " + vecStr(result) + " cm";

		feedbackStrings.add(xyz);

		// anchor (urhere) feedback?
		YouAreHereItem item = getContainer().getYouAreHereItem();
		if (item != null) {
			Point2D.Double anchor = item.getFocus();
			String anchorStr = "$khaki$Dist from ref. point: "
					+ valStr(anchor.distance(wp), 5) + " cm";
			feedbackStrings.add(anchorStr);
		}

		double rho = x * x + y * y;
		double r = Math.sqrt(rho + z * z);
		rho = Math.sqrt(rho);
		double theta = Math.toDegrees(Math.atan2(rho, z));

		// get absolute phi
		double absphi = getAbsolutePhi(container, pp, wp);

		String rtp = CedView.rThetaPhi + " (" + valStr(r, 2) + "cm, "
				+ valStr(theta, 2) + UnicodeSupport.DEGREE + ", "
				+ valStr(absphi, 2) + UnicodeSupport.DEGREE + ")";
		feedbackStrings.add(rtp);

		// cylindrical coordinates which are just the world coordinates!
		String rzp = CedView.rhoZPhi + " (" + valStr(rho, 2) + "cm, "
				+ valStr(z, 2) + "cm , " + valStr(absphi, 2)
				+ UnicodeSupport.DEGREE + ")";
		feedbackStrings.add(rzp);

		// sector coordinates
		worldToSector(wp, result);
		String sectxyz = "$yellow$Sector xyz " + vecStr(result) + " cm";
		feedbackStrings.add(sectxyz);

		// tilted sector
		sectorToTilted(result, result);
		String tiltsectxyz = "$yellow$Tilted sect xyz " + vecStr(result)
				+ " cm";
		feedbackStrings.add(tiltsectxyz);
				
		if (_activeProbe != null) {
			float field[] = new float[3];
			_activeProbe.field(x, y, z, field);
			
			float grad[] = new float[3];
			_activeProbe.gradient(x, y, z, grad);
			
			// convert to Tesla from kG
			field[0] /= 10.0;
			field[1] /= 10.0;
			field[2] /= 10.0;
			
			//convert kG/cm to T/m
			grad[0] *= 10.0;
			grad[1] *= 10.0;
			grad[2] *= 10.0;

			double bmag = VectorSupport.length(field);
			double gmag = VectorSupport.length(grad);
			feedbackStrings.add("$Lawn Green$"
					+ MagneticFields.getInstance().getActiveFieldDescription());
			
			boolean hasTorus = MagneticFields.getInstance().hasActiveTorus();
			boolean hasSolenoid = MagneticFields.getInstance().hasActiveSolenoid();
			
			//scale factors
			if (hasTorus || hasSolenoid) {
				String scaleStr = "";
				if (hasTorus) {
					double torusScale = MagneticFields.getInstance().getScaleFactor(FieldType.TORUS);
					scaleStr += "Torus scale " + valStr(torusScale, 3) + " ";
				}
				if (hasSolenoid) {
					double shiftZ = MagneticFields.getInstance().getShiftZ(FieldType.SOLENOID);
					String shiftStr = "Solenoid Z shift " + valStr(shiftZ, 3) + " cm ";
					feedbackStrings.add("$Lawn Green$" + shiftStr);
					
					double solenScale = MagneticFields.getInstance().getScaleFactor(FieldType.SOLENOID);
					scaleStr += "Solenoid scale " + valStr(solenScale, 3) + " ";
				}
				feedbackStrings.add("$Lawn Green$" + scaleStr);
			}

			
			feedbackStrings.add("$Lawn Green$Field " + valStr(bmag, 4) + " T "
					+ vecStr(field) + " T");
			feedbackStrings.add("$Lawn Green$Grad " + valStr(gmag, 4) + " T/m "
					+ vecStr(grad) + " T/m");
		}
		else {
			feedbackStrings.add("$Lawn Green$"
					+ MagneticFields.getInstance().getActiveFieldDescription());
			feedbackStrings.add("$Lawn Green$Field is Zero");
		}
		// near a swum trajectory?
		double mindist = _swimTrajectoryDrawer.closestApproach(wp);

		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(container)
				* mindist;

		//TODO FIX THIS
		_lastTrajStr = null;
		if (pixlen < 25.0) {
			SwimTrajectory2D traj2D = _swimTrajectoryDrawer
					.getClosestTrajectory();
			
			//in a sector change diamond
			int sectChangeIndices[] = traj2D.sectChangeIndices();
			if (sectChangeIndices != null) {
				Point scpp = new Point();
				Rectangle crect = new Rectangle();
				for (int idx : sectChangeIndices) {
					Point2D.Double scwp = traj2D.getPath()[idx];
					container.worldToLocal(scpp, scwp);
					crect.setBounds(scpp.x-4, scpp.y-4, 8, 8);
					if (crect.contains(pp)) {
						feedbackStrings.add(SwimTrajectory2D.fbColor + traj2D.sectorChangeString(idx));
					}
				}
			}
			
			if (traj2D != null) {
				traj2D.addToFeedback(feedbackStrings);
				_lastTrajStr = traj2D.summaryString();
			}
		}
		
		//DC Occupancy
		int sector = getSector(container, pp, wp);

		double totalOcc = 100.*DC.getInstance().totalOccupancy();
		double sectorOcc = 100.*DC.getInstance().totalSectorOccupancy(sector);
		String occStr = "total DC occ " + DoubleFormat.doubleFormat(totalOcc, 2) + "%" + " sector " + sector +
				" occ " + DoubleFormat.doubleFormat(sectorOcc, 2) + "%";
		feedbackStrings.add("$aqua$" + occStr);

		// reconstructed feedback?
		if (showDCHBCrosses()) {
			_dcCrossDrawer.setMode(CrossDrawer.HB);
			_dcCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}
		if (showDCTBCrosses()) {
			_dcCrossDrawer.setMode(CrossDrawer.TB);
			_dcCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}
		
		//Other (not DC) Crosses
		if (showCrosses()) {
			_fmtCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}

		if (showMcTruth()) {
			_mcHitDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}
		
		_reconDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);

	}

	// convenience call for double formatter
	private String valStr(double value, int numdec) {
		return DoubleFormat.doubleFormat(value, numdec);
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec
	 *            the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	private String vecStr(double v[]) {
		return "(" + DoubleFormat.doubleFormat(v[0], 2) + ", "
				+ DoubleFormat.doubleFormat(v[1], 2) + ", "
				+ DoubleFormat.doubleFormat(v[2], 2) + ")";
	}

	/**
	 * Returns a string representation of the form: "(x,y,z)".
	 * 
	 * @param numDec
	 *            the number of decimal places for each coordinate.
	 * @return a String representation of the vector
	 */
	private String vecStr(float v[]) {
		return "(" + DoubleFormat.doubleFormat(v[0], 3) + ", "
				+ DoubleFormat.doubleFormat(v[1], 3) + ", "
				+ DoubleFormat.doubleFormat(v[2], 3) + ")";
	}

	/**
	 * Are the given global x and y in this view? That is, does the sector they
	 * correspond to in this view based on the calculated phi?
	 * 
	 * @param x
	 *            the global x
	 * @param y
	 *            the global y in the same units as x
	 * @return <code>true</code> if the point is in
	 */
	public boolean inThisView(double x, double y) {
		if ((Math.abs(x) < TINY) && (Math.abs(y) < TINY)) {
			return true;
		}
		double tphi = Math.toDegrees(Math.atan2(y, x)) + 30;
		if (tphi < 0) {
			tphi = tphi + 360.0;
		}
		int sector = ((int) tphi) / 60;

		switch (sector) {
		case 0:
		case 3:
			return (_displaySectors == DisplaySectors.SECTORS14);
		case 1:
		case 4:
			return (_displaySectors == DisplaySectors.SECTORS25);
		case 2:
		case 5:
			return (_displaySectors == DisplaySectors.SECTORS36);
		default:
			System.err.println("Bad sector in inThisView: " + sector);
			return false;
		}
	}
	
	/**
	 * Check whether this sect is on this view
	 * @param sector the sector [1..6]
	 * @return <code>true</code> if the sector is on the view.
	 */
	public boolean inThisView(int sector) {
		switch (_displaySectors) {
		case SECTORS14:
			return ((sector == 1) || (sector == 4));

		case SECTORS25:
			return ((sector == 2) || (sector == 5));

		case SECTORS36:
			return ((sector == 3) || (sector == 6));
		}
		
		return false;
	}

	/**
	 * Check whether a give phi is included in this view's range.
	 * 
	 * @param phi
	 *            the value of phi on decimal degrees.
	 * @return <code>true</code> if it is included.
	 */
	public boolean inThisView(double phi) {
		while (phi < 0) {
			phi += 360.0;
		}
		switch (_displaySectors) {
		case SECTORS14:
			return between(phi, 330., 360.) || between(phi, 0., 30.)
					|| between(phi, 150., 210.);

		case SECTORS25:
			return between(phi, 30., 90.) || between(phi, 210., 270.);

		case SECTORS36:
			return between(phi, 90., 150.) || between(phi, 270., 330.);
		}

		return false;
	}

	// convenience in-range test
	private boolean between(double x, double xmin, double xmax) {
		return (x >= xmin) && (x <= xmax);
	}

	/**
	 * Get the rotation angle to transform from world coordinates to global
	 * coordinates. This is the sum of the phi for the upper sector of the view
	 * and the phi relative to the midplane.
	 * 
	 * @return the rotation angle to transform from world coordinates to global
	 *         coordinates, in degrees.
	 */
	public double getPhiRotate() {
		// double phiRotate = _phiRelMidPlane;
		double phiRotate = getSliderPhi();
		if (_displaySectors == DisplaySectors.SECTORS25) {
			phiRotate += 60.0;
		} else if (_displaySectors == DisplaySectors.SECTORS36) {
			phiRotate += 120.0;
		}
		return phiRotate;
	}
	
	public double getMidplanePhiRotate() {
		double phiRotate = 0;
		if (_displaySectors == DisplaySectors.SECTORS25) {
			phiRotate += 60.0;
		} else if (_displaySectors == DisplaySectors.SECTORS36) {
			phiRotate += 120.0;
		}
		return phiRotate;
	}
	
	/**
	 * Returns the absolute phi. This is the actual, global phi, e.g, -30 to 30
	 * for sector 1, 30 to 90 for sector 2, etc.
	 * 
	 * @return the absolute phi from the relative phi
	 */
	public double getAbsolutePhi(IContainer container, Point screenPoint,
			Point2D.Double worldPoint) {
		int sector = getSector(container, screenPoint, worldPoint);
		// return (sector - 1) * 60.0 + _phiRelMidPlane;
		return (sector - 1) * 60.0 + getSliderPhi();
	}

	/**
	 * Get the target z location.
	 * 
	 * @return the target position in cm on z axis
	 */
	public double getTargetZ() {
		return _targetZ;
	}

	/**
	 * Get the relative phi value, int the range [-30, 30].
	 * 
	 * @return the phi value--the slider setting. This is between -30 and 30 for
	 *         all sectors--i.e., this is the relative phi, not the absolute
	 *         phi.
	 */
	public double getSliderPhi() {
		// return _phiRelMidPlane;

		if (_controlPanel == null) {
			return 0.0;
		}
		return _controlPanel.getPhiSlider().getValue();
	}

	/**
	 * Get the relative phi (what the slider setting should be) corresponding to
	 * the absolute value of phi.
	 * 
	 * @param absPhi
	 *            the value of phi in degrees, e.g., from a MC track.
	 * @return the corresponding slider value;
	 */
	private double getRelativePhi(double absPhi) {
		while (absPhi < 360.0) {
			absPhi += 360.0;
		}

		while (absPhi > 30.0) {
			absPhi -= 60.0;
		}

		return absPhi;
	}

	/**
	 * Called by a container when a right click is not handled. The usual reason
	 * is that the right click was on an inert spot.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	@Override
	public boolean rightClicked(MouseEvent mouseEvent) {

		JPopupMenu popup = null;

		// near a swum trajectory?
		Point2D.Double wp = new Point2D.Double();
		getContainer().localToWorld(mouseEvent.getPoint(), wp);
		double mindist = _swimTrajectoryDrawer.closestApproach(wp);
		double pixlen = WorldGraphicsUtilities
				.getMeanPixelDensity(getContainer()) * mindist;

		if (pixlen < 25.0) {
			final SwimTrajectory2D traj2D = _swimTrajectoryDrawer
					.getClosestTrajectory();
			
			if (traj2D == null) {
				return false;
			}

			// get the phi from the trajectory
			final double desiredPhi = traj2D.getTrajectory3D().getOriginalPhi();

			if (popup == null) {
				popup = new JPopupMenu();
			}

			final JMenuItem rotateItem = new JMenuItem(
					"Rotate to match trajectory " + UnicodeSupport.SMALL_PHI
							+ ": " + valStr(desiredPhi, 3));

			final JMenuItem integralItem = new JMenuItem("<html>Plot  "
					+ UnicodeSupport.INTEGRAL + "|<bold>B</bold> "
					+ UnicodeSupport.TIMES + " <bold>dL</bold>|");

			ActionListener al = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent ae) {

					Object source = ae.getSource();
					if (source == rotateItem) {
						double sliderPhi = getRelativePhi(desiredPhi);
						_controlPanel.getPhiSlider().setValue((int) sliderPhi);
						getContainer().refresh();
					} else if (source == integralItem) {
						PlotView pview = Ced.getCed()
								.getPlotView();
						if (pview != null) {
							PlotCanvas canvas = pview.getPlotCanvas();
							try {
								SwimTrajectory traj = traj2D.getTrajectory3D();
								traj.computeBDL(FieldProbe.factory());

								// do we already have data?
								boolean havePlotData = (canvas.getDataSet() == null) ? false
										: canvas.getDataSet().dataAdded();

								if (!havePlotData) {
									initPlot(canvas, traj2D);
								} else { // have to add a curve
									int curveCount = canvas.getDataSet()
											.getCurveCount();
									DataSet dataSet = canvas.getDataSet();
									dataSet.addCurve(
											"X",
											traj2D.summaryString()
													+ " ["
													+ MagneticFields
															.getInstance().getActiveFieldDescription()
													+ "]");
									for (double v[] : traj) {
										dataSet.addToCurve(curveCount,
												v[SwimTrajectory.PATHLEN_IDX],
												v[SwimTrajectory.BXDL_IDX]);

										setCurveStyle(canvas, curveCount);
									}

								}

								ViewManager.getInstance().setVisible(pview,
										true);
								canvas.repaint();
							} catch (DataSetException e) {
								e.printStackTrace();
							}
						} // pview not null
					} // integral
				}
			};

			rotateItem.addActionListener(al);
			integralItem.addActionListener(al);
			popup.add(rotateItem);
			popup.add(integralItem);
		} // end near traj (pixlen)

		Point p = mouseEvent.getPoint();
		if (popup != null) {
			popup.show(getContainer().getComponent(), p.x, p.y);
			return true;
		}

		return false;
	}

	private void initPlot(PlotCanvas canvas, SwimTrajectory2D traj2D)
			throws DataSetException {
		SwimTrajectory traj = traj2D.getTrajectory3D();
		DataSet dataSet = new DataSet(DataSetType.XYXY, "X",
				traj2D.summaryString() + " ["
						+ MagneticFields.getInstance().getActiveFieldDescription() + "]");

		canvas.getParameters().setPlotTitle("Magnetic Field Integral");
		canvas.getParameters().setXLabel("Path Length (m)");
		canvas.getParameters().setYLabel(
				"<html>" + UnicodeSupport.INTEGRAL + "|<bold>B</bold> "
						+ UnicodeSupport.TIMES + " <bold>dL</bold>| kG-m");

		for (double v[] : traj) {
			dataSet.add(v[SwimTrajectory.PATHLEN_IDX],
					v[SwimTrajectory.BXDL_IDX]);
		}
		canvas.setDataSet(dataSet);
		setCurveStyle(canvas, 0);
	}

	private void setCurveStyle(PlotCanvas canvas, int index) {
		int cindex = index % plotColors.length;
		canvas.getDataSet().getCurveStyle(index)
				.setLineColor(plotColors[cindex]);
		canvas.getDataSet().getCurveStyle(index)
				.setFillColor(plotColors[cindex]);
		canvas.getDataSet().getCurveStyle(index)
				.setSymbolType(cnuphys.splot.style.SymbolType.X);
		canvas.getDataSet().getCurveStyle(index).setSymbolSize(6);
		canvas.getDataSet().getCurve(index).getFit()
				.setFitType(FitType.CUBICSPLINE);

	}

	/**
	 * Convert world (not global, but graphical world) to clas global (bab)
	 * 
	 * @param wp
	 *            the world point
	 * @param labXYZ
	 *            the clas global coordinates
	 */
	public void worldToLabXYZ(Point2D.Double wp, double[] labXYZ) {
		double perp = wp.y;
		double z = wp.x;

		// we are essentially display a plan yp=0, with xp vertical and zp
		// horizontal. We need to rotate around z by "phiRotate" to get the x
		// and y coordinates. Note
		// it is a simple rotation since yp is zero
		double phiRotate = Math.toRadians(getPhiRotate());
		labXYZ[0] = perp * Math.cos(phiRotate);
		labXYZ[1] = perp * Math.sin(phiRotate);
		labXYZ[2] = z;
	}

	/**
	 * Convert world (not global, but graphical world) to sector
	 * 
	 * @param wp
	 *            the world point
	 * @param sectorXYZ
	 *            the sector coordinates
	 */
	public void worldToSector(Point2D.Double wp, double[] sectorXYZ) {
		double perp = wp.y;
		double sectz = wp.x;
		double sphi = Math.toRadians(getSliderPhi());
		if (perp < 0) {
			perp = -perp;
			sphi = -sphi;
		}
		double sectx = perp * Math.cos(sphi);
		double secty = perp * Math.sin(sphi);

		sectorXYZ[0] = sectx;
		sectorXYZ[1] = secty;
		sectorXYZ[2] = sectz;
	}

	/**
	 * Simple test whether this view displays a given 1-based sector
	 * 
	 * @param sector
	 *            the sector [1..6]
	 * @return <code>true</code> if this view displays the given sector
	 */
	public boolean isSectorOnView(int sector) {
		switch (_displaySectors) {
		case SECTORS14:
			return (sector == 1) || (sector == 4);

		case SECTORS25:
			return (sector == 2) || (sector == 5);

		case SECTORS36:
			return (sector == 3) || (sector == 6);
		}

		return false;
	}

	// draw the BST panels
	private void drawBSTPanels(Graphics g, IContainer container) {
		List<BSTxyPanel> panels = GeometryManager.getBSTxyPanels();
		if (panels == null) {
			return;
		}

		int sector = 0;
		switch (_displaySectors) {
		case SECTORS14:
			sector = 1;
			break;

		case SECTORS25:
			sector = 2;
			break;

		case SECTORS36:
			sector = 3;
			break;
		}

		double phi = (sector - 1) * 60.0 + getSliderPhi();
		double cosphi = Math.cos(Math.toRadians(phi));
		double sinphi = Math.sin(Math.toRadians(phi));

		// set the perp distance
		for (BSTxyPanel panel : panels) {
			Point2D.Double avgXY = panel.getXyAverage();
			double perp = avgXY.y * cosphi - avgXY.x * sinphi;
			panel.setPerp(perp);
		}

		Collections.sort(panels);

		Graphics2D g2 = (Graphics2D) g;
		Shape oldClip = g2.getClip();
		// clip the active area
		Rectangle sr = container.getInsetRectangle();
		g2.clipRect(sr.x, sr.y, sr.width, sr.height);

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(stroke);
		g2.setColor(Color.black);

		// there are 132 panels
		// mark the hits if there is data
		CentralSupport.markPanelHits(this, panels);

		int index = 0;
		for (BSTxyPanel panel : panels) {

			int alpha = 10 + index / 3;
			Color col = new Color(128, 128, 128, alpha);
			Color col2 = new Color(128, 128, 128, alpha + 40);
			WorldPolygon poly[] = getFromBSTPanel(panel, cosphi, sinphi);

			for (int j = 0; j < 3; j++) {
				boolean hit = panel.hit[j];

				WorldGraphicsUtilities.drawWorldPolygon(g2, container, poly[j],
						hit ? BSTHITFILL : col, col2, 0, LineStyle.SOLID);
			}
		}

		// restore
		g2.setStroke(oldStroke);
		g2.setClip(oldClip);
	}

	/**
	 * Get the world graphic coordinates from lab XYZ
	 * 
	 * @param x
	 *            the lab x in cm
	 * @param y
	 *            the lab y in cm
	 * @param z
	 *            the lab z in cm
	 * @param wp
	 *            the world point
	 */
	private void labToWorldBST(double x, double y, double z, Point2D.Double wp,
			double cosphi, double sinphi) {
		wp.x = z;
		wp.y = x * cosphi + y * sinphi;

	}

	private WorldPolygon[] getFromBSTPanel(BSTxyPanel panel, double cosphi,
			double sinphi) {

		WorldPolygon polys[] = new WorldPolygon[3];

		// note conversion to cm from mm
		double x1 = panel.getX1() / 10;
		double x2 = panel.getX2() / 10;

		double y1 = panel.getY1() / 10;
		double y2 = panel.getY2() / 10;

		double z0 = panel.getZ0() / 10;
		double z1 = panel.getZ1() / 10;
		double z2 = panel.getZ2() / 10;
		double z3 = panel.getZ3() / 10;
		double z4 = panel.getZ4() / 10;
		double z5 = panel.getZ5() / 10;

		double x[] = new double[5];
		double y[] = new double[5];

		Point2D.Double wp = new Point2D.Double();

		labToWorldBST(x1, y1, z0, wp, cosphi, sinphi);
		x[0] = wp.x;
		y[0] = wp.y;

		labToWorldBST(x2, y2, z0, wp, cosphi, sinphi);
		x[1] = wp.x;
		y[1] = wp.y;

		labToWorldBST(x2, y2, z1, wp, cosphi, sinphi);
		x[2] = wp.x;
		y[2] = wp.y;

		labToWorldBST(x1, y1, z1, wp, cosphi, sinphi);
		x[3] = wp.x;
		y[3] = wp.y;

		x[4] = x[0];
		y[4] = y[0];

		polys[0] = new WorldPolygon(x, y, 5);

		labToWorldBST(x1, y1, z2, wp, cosphi, sinphi);
		x[0] = wp.x;
		y[0] = wp.y;

		labToWorldBST(x2, y2, z2, wp, cosphi, sinphi);
		x[1] = wp.x;
		y[1] = wp.y;

		labToWorldBST(x2, y2, z3, wp, cosphi, sinphi);
		x[2] = wp.x;
		y[2] = wp.y;

		labToWorldBST(x1, y1, z3, wp, cosphi, sinphi);
		x[3] = wp.x;
		y[3] = wp.y;

		x[4] = x[0];
		y[4] = y[0];

		polys[1] = new WorldPolygon(x, y, 5);

		labToWorldBST(x1, y1, z4, wp, cosphi, sinphi);
		x[0] = wp.x;
		y[0] = wp.y;

		labToWorldBST(x2, y2, z4, wp, cosphi, sinphi);
		x[1] = wp.x;
		y[1] = wp.y;

		labToWorldBST(x2, y2, z5, wp, cosphi, sinphi);
		x[2] = wp.x;
		y[2] = wp.y;

		labToWorldBST(x1, y1, z5, wp, cosphi, sinphi);
		x[3] = wp.x;
		y[3] = wp.y;

		x[4] = x[0];
		y[4] = y[0];

		polys[2] = new WorldPolygon(x, y, 5);

		return polys;
	}
	
	/**
	 * Draw a single wire. All indices are 1-based
	 * @param g
	 * @param container
	 * @param fillColor
	 * @param frameColor
	 * @param sector
	 * @param superlayer
	 * @param layer
	 * @param wire
	 * @param trkDoca
	 */
	public void drawDCHit(Graphics g, IContainer container, Color fillColor, Color frameColor, byte sector,
			byte superlayer, byte layer, short wire, float trkDoca, Point location) {

		SectorSuperLayer sectSL = _superLayers[(sector < 4) ? 0 : 1][superlayer-1];
		sectSL.drawDCHit(g, container, fillColor, frameColor, 
				layer, wire, trkDoca, location);
		
	}
	
	/**
	 * Clone the view. 
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT[_displaySectors.ordinal()]++;
		
		//limit
		if (CLONE_COUNT[_displaySectors.ordinal()] > 2) {
			return null;
		}
		
		Rectangle vr = getBounds();
		vr.x += 40;
		vr.y += 40;
		
		SectorView view = createSectorView(_displaySectors);
		view.setBounds(vr);
		return view;

	}
	
	class DebugPoint {
		
		//LAB coordinates
		private double phi;  //deg
		private double rho;  //cm
		private double z;   //cm
		private double x;
		private double y;
		
		public DebugPoint(double phi, double rho, double z) {
			this.phi = phi;
			this.rho = rho;
			this.z = z;
			double rphi = Math.toRadians(phi);
			x = rho*Math.cos(rphi);
			y = rho*Math.sin(rphi);
		}
		
		/**
		 * get the sector 1..6
		 * @return the sector 1..6
		 */
		public int sector() {
			return GeometryManager.getSector(phi);
		}
		
		public Point toLocal(IContainer container) {
			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			projectClasToWorld(x, y, z, projectionPlane, wp);
			container.worldToLocal(pp, wp);
			return pp;
		}
		
	}
	
}
