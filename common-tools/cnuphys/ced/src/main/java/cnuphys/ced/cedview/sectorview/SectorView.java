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
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.SliceView;
import cnuphys.ced.cedview.central.CentralSupport;
import cnuphys.ced.common.CrossDrawer;
import cnuphys.ced.common.FMTCrossDrawer;
import cnuphys.ced.common.SuperLayerDrawing;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DCReconHit;
import cnuphys.ced.event.data.DCTdcHit;
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
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.graphics.world.WorldGraphicsUtilities;
import cnuphys.bCNU.graphics.world.WorldPolygon;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.util.UnicodeSupport;
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
public class SectorView extends SliceView implements ChangeListener {


	// fill color
	private static final Color BSTHITFILL = new Color(255, 128, 0, 64);

	// HTCC Items, 8 per sector, not geometrically realistic
	private SectorHTCCItem _htcc[][] = new SectorHTCCItem[4][2];

	// LTCC Items, 36 per sector, not geometrically realistic
	private SectorLTCCItem _ltcc[][] = new SectorLTCCItem[18][2];

	// superlayer (graphical) items. The first index [0..1] is for upper and
	// lower sectors.
	// the second is for for super layer 0..5debug
	private SectorSuperLayer _superLayers[][] = new SectorSuperLayer[2][6];

	// determines if the wire intersections must be recalculated. This is caused
	// by a change in phi using the phi slider.
	private Boolean _wiresDirty = true;

	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	// for drawing MC hits
	private McHitDrawer _mcHitDrawer;

	// drawing reconstructed data
	private ReconDrawer _reconDrawer;
	
	// reconstructed cross drawer for DC (and feedback handler)
	private CrossDrawer _dcCrossDrawer;

	// for fmt
	private FMTCrossDrawer _fmtCrossDrawer;
	
	
	//redraw the segments?
	private boolean segmentsOnTop = true;


	private static Color plotColors[] = { X11Colors.getX11Color("Dark Red"), X11Colors.getX11Color("Dark Blue"),
			X11Colors.getX11Color("Dark Green"), Color.black, Color.gray, X11Colors.getX11Color("wheat") };

	/**
	 * Create a sector view
	 * 
	 * @param keyVals variable set of arguments.
	 */
	private SectorView(DisplaySectors displaySectors, Object... keyVals) {
		super(displaySectors, keyVals);
		
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
		
		addItems();
		setBeforeDraw();
		setAfterDraw();

	}

	/**
	 * Convenience method for creating a Sector View.
	 * 
	 * @param displaySectors controls which opposite sectors are displayed.
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

				PropertySupport.LEFT, LEFT, PropertySupport.TOP, TOP, PropertySupport.WIDTH, width,
				PropertySupport.HEIGHT, height, PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS,
				CedView.TOOLBARBITS, PropertySupport.VISIBLE, true, PropertySupport.BACKGROUND,
				X11Colors.getX11Color("Alice Blue").darker(),
				PropertySupport.TITLE, title, PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view,
				ControlPanel.NOISECONTROL + ControlPanel.DISPLAYARRAY + ControlPanel.PHISLIDER + ControlPanel.DRAWLEGEND
						+ ControlPanel.FEEDBACK + ControlPanel.FIELDLEGEND + ControlPanel.TARGETSLIDER
						+ ControlPanel.ACCUMULATIONLEGEND,
				DisplayBits.MAGFIELD + DisplayBits.CROSSES + DisplayBits.RECONHITS + DisplayBits.CLUSTERS
						+ DisplayBits.FMTCROSSES + DisplayBits.RECPART + DisplayBits.DC_HITS + DisplayBits.SEGMENTS + DisplayBits.GLOBAL_HB + DisplayBits.GLOBAL_NN
						+ DisplayBits.GLOBAL_AIHB + DisplayBits.GLOBAL_AITB
						+ DisplayBits.GLOBAL_TB + DisplayBits.ACCUMULATION + DisplayBits.DOCA + DisplayBits.MCTRUTH +
						DisplayBits.SECTORCHANGE + DisplayBits.RECCAL,
				3, 5);

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
		LogicalLayer magneticFieldLayer = getContainer().getLogicalLayer(_magneticFieldLayerName);
		new MagFieldItem(magneticFieldLayer, this);
		magneticFieldLayer.setVisible(false);

		LogicalLayer detectorLayer = getContainer().getLogicalLayer(_detectorLayerName);
		new BeamLineItem(detectorLayer);

		// add the ltcc items
		for (int ring = 1; ring <= 18; ring++) {
			for (int half = 1; half <= 2; half++) {

				switch (_displaySectors) {
				case SECTORS14:
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, 1, ring, half);
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, 4, ring, half);
					break;

				case SECTORS25:
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, 2, ring, half);
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, 5, ring, half);
					break;

				case SECTORS36:
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, 3, ring, half);
					_ltcc[ring - 1][half - 1] = new SectorLTCCItem(detectorLayer, this, 6, ring, half);
					break;
				}

			}
		}

		// add the htcc items
		for (int ring = 1; ring <= 4; ring++) {
			for (int half = 1; half <= 2; half++) {

				switch (_displaySectors) {
				case SECTORS14:
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, 1, ring, half);
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, 4, ring, half);
					break;

				case SECTORS25:
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, 2, ring, half);
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, 5, ring, half);
					break;

				case SECTORS36:
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, 3, ring, half);
					_htcc[ring - 1][half - 1] = new SectorHTCCItem(detectorLayer, this, 6, ring, half);
					break;
				}

			}
		}

		// add the superlayer items
		for (int superLayer = 0; superLayer < 6; superLayer++) {
			// SectorSuperLayer constructor expects a 1-based index

			switch (_displaySectors) {
			case SECTORS14:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 1, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 4, superLayer + 1);
				break;

			case SECTORS25:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 2, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 5, superLayer + 1);
				break;

			case SECTORS36:
				_superLayers[UPPER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 3, superLayer + 1);
				_superLayers[LOWER_SECTOR][superLayer] = new SectorSuperLayer(detectorLayer, this, 6, superLayer + 1);
				break;
			}

			_superLayers[UPPER_SECTOR][superLayer].getStyle().setFillColor(Color.gray);
			_superLayers[LOWER_SECTOR][superLayer].getStyle().setFillColor(Color.gray);
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
	 * 
	 * @param upperLower 0 for upper sector, 1 for lower sector
	 * @param superLayer super layer 1..6
	 * @return the drawer
	 */
	public SuperLayerDrawing getSuperLayerDrawer(int upperLower, int superLayer) {
		return _superLayers[upperLower][superLayer - 1].getSuperLayerDrawer();
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
				if (showAIDCHBCrosses()) {
					_dcCrossDrawer.setMode(CrossDrawer.AIHB);
					_dcCrossDrawer.draw(g, container);
				}
				if (showAIDCTBCrosses()) {
					_dcCrossDrawer.setMode(CrossDrawer.AITB);
					_dcCrossDrawer.draw(g, container);
				}


				// Other (not DC) Crosses
				if (showCrosses()) {
					_fmtCrossDrawer.draw(g, container);
				}

				// scale
				if ((_scaleDrawer != null) && showScale()) {
					_scaleDrawer.draw(g, container);
				}

				// redraw segments
				if (segmentsOnTop) {
//					System.err.println("REDRAW SEGMENTS");
					redrawSegments(g, container);
				}

				// a clean rectangle
				Rectangle bounds = container.getComponent().getBounds();
				GraphicsUtilities.drawSimple3DRect(g, 0, 0, bounds.width - 1, bounds.height - 1, false);
			}

		};
		getContainer().setAfterDraw(afterDraw);
	}



	// redraw the segments on top
	private void redrawSegments(Graphics g, IContainer container) {

		// secty loop is just upper and lower (0-1, not 0-5)
		for (int sect = 0; sect < 2; sect++) {
			for (int supl = 0; supl < 6; supl++) {
				_superLayers[sect][supl].drawSegments(g, container);
			}
		}
	}



	/**
	 * This is used to listen for changes on components like sliders.
	 * 
	 * @param e the causal event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();

		// change target z?
		if (source == _controlPanel.getTargetSlider()) {
			_targetZ = (_controlPanel.getTargetSlider().getValue());
			getContainer().refresh();
		} else if (source == _controlPanel.getPhiSlider()) {
			// change the projection plane
			projectionPlane = GeometryManager.constantPhiPlane(getSliderPhi());

			_wiresDirty = true;
			getContainer().setDirty(true);
			getContainer().refresh();
		}
	}


	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container the base container for the view.
	 * @param pp        the pixel point
	 * @param wp        the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp, Point2D.Double wp, List<String> feedbackStrings) {

		// get the common information
		super.getFeedbackStrings(container, pp, wp, feedbackStrings);
		commonFeedbackStrings(container, pp, wp, feedbackStrings);

		// near a swum trajectory?
		double mindist = _swimTrajectoryDrawer.closestApproach(wp);

		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(container) * mindist;

		// TODO FIX THIS
		_lastTrajStr = null;
		if (pixlen < 25.0) {
			SwimTrajectory2D traj2D = _swimTrajectoryDrawer.getClosestTrajectory();

			// in a sector change diamond
			int sectChangeIndices[] = traj2D.sectChangeIndices();
			if (sectChangeIndices != null) {
				Point scpp = new Point();
				Rectangle crect = new Rectangle();
				for (int idx : sectChangeIndices) {
					Point2D.Double scwp = traj2D.getPath()[idx];
					container.worldToLocal(scpp, scwp);
					crect.setBounds(scpp.x - 4, scpp.y - 4, 8, 8);
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

		// DC Occupancy
		int sector = getSector(container, pp, wp);

		double totalOcc = 100. * DC.getInstance().totalOccupancy();
		double sectorOcc = 100. * DC.getInstance().totalSectorOccupancy(sector);
		String occStr = "Total DC occ " + DoubleFormat.doubleFormat(totalOcc, 2) + "%" + " sector " + sector + " occ "
				+ DoubleFormat.doubleFormat(sectorOcc, 2) + "%";
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
		if (showAIDCHBCrosses()) {
			_dcCrossDrawer.setMode(CrossDrawer.AIHB);
			_dcCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}
		if (showAIDCTBCrosses()) {
			_dcCrossDrawer.setMode(CrossDrawer.AITB);
			_dcCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}


		// Other (not DC) Crosses
		if (showCrosses()) {
			_fmtCrossDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}

		if (showMcTruth()) {
			_mcHitDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);
		}

		//draws HB hits and segs, TB hits and segs, and nn overlays
		_reconDrawer.vdrawFeedback(container, pp, wp, feedbackStrings, 0);

	}



	/**
	 * Called by a container when a right click is not handled. The usual reason is
	 * that the right click was on an inert spot.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public boolean rightClicked(MouseEvent mouseEvent) {

		JPopupMenu popup = null;

		// near a swum trajectory?
		Point2D.Double wp = new Point2D.Double();
		getContainer().localToWorld(mouseEvent.getPoint(), wp);
		double mindist = _swimTrajectoryDrawer.closestApproach(wp);
		double pixlen = WorldGraphicsUtilities.getMeanPixelDensity(getContainer()) * mindist;

		if (pixlen < 25.0) {
			final SwimTrajectory2D traj2D = _swimTrajectoryDrawer.getClosestTrajectory();

			if (traj2D == null) {
				return false;
			}

			// get the phi from the trajectory
			final double desiredPhi = traj2D.getTrajectory3D().getOriginalPhi();

			if (popup == null) {
				popup = new JPopupMenu();
			}

			final JMenuItem rotateItem = new JMenuItem(
					"Rotate to match trajectory " + UnicodeSupport.SMALL_PHI + ": " + valStr(desiredPhi, 3));

			final JMenuItem integralItem = new JMenuItem("<html>Plot  " + UnicodeSupport.INTEGRAL + "|<bold>B</bold> "
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
						PlotView pview = Ced.getCed().getPlotView();
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
									int curveCount = canvas.getDataSet().getCurveCount();
									DataSet dataSet = canvas.getDataSet();
									dataSet.addCurve("X", traj2D.summaryString() + " ["
											+ MagneticFields.getInstance().getActiveFieldDescription() + "]");
									for (double v[] : traj) {
										dataSet.addToCurve(curveCount, v[SwimTrajectory.PATHLEN_IDX],
												v[SwimTrajectory.BXDL_IDX]);

										setCurveStyle(canvas, curveCount);
									}

								}

								ViewManager.getInstance().setVisible(pview, true);
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

	//initialize the bdl plot
	private void initPlot(PlotCanvas canvas, SwimTrajectory2D traj2D) throws DataSetException {
		SwimTrajectory traj = traj2D.getTrajectory3D();
		DataSet dataSet = new DataSet(DataSetType.XYXY, "X",
				traj2D.summaryString() + " [" + MagneticFields.getInstance().getActiveFieldDescription() + "]");

		canvas.getParameters().setPlotTitle("Magnetic Field Integral");
		canvas.getParameters().setXLabel("Path Length (m)");
		canvas.getParameters().setYLabel("<html>" + UnicodeSupport.INTEGRAL + "|<bold>B</bold> " + UnicodeSupport.TIMES
				+ " <bold>dL</bold>| kG-m");

		for (double v[] : traj) {
			dataSet.add(v[SwimTrajectory.PATHLEN_IDX], v[SwimTrajectory.BXDL_IDX]);
		}
		canvas.setDataSet(dataSet);
		setCurveStyle(canvas, 0);
	}

	//set the curve style
	private void setCurveStyle(PlotCanvas canvas, int index) {
		int cindex = index % plotColors.length;
		canvas.getDataSet().getCurveStyle(index).setFitLineColor(plotColors[cindex]);
		canvas.getDataSet().getCurveStyle(index).setBorderColor(plotColors[cindex]);
		canvas.getDataSet().getCurveStyle(index).setFillColor(plotColors[cindex]);
		canvas.getDataSet().getCurveStyle(index).setSymbolType(cnuphys.splot.style.SymbolType.X);
		canvas.getDataSet().getCurveStyle(index).setSymbolSize(6);
		canvas.getDataSet().getCurve(index).getFit().setFitType(FitType.CUBICSPLINE);

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

				WorldGraphicsUtilities.drawWorldPolygon(g2, container, poly[j], hit ? BSTHITFILL : col, col2, 0,
						LineStyle.SOLID);
			}
		}

		// restore
		g2.setStroke(oldStroke);
		g2.setClip(oldClip);
	}

	/**
	 * Get the world graphic coordinates from lab XYZ
	 * 
	 * @param x  the lab x in cm
	 * @param y  the lab y in cm
	 * @param z  the lab z in cm
	 * @param wp the world point
	 */
	private void labToWorldBST(double x, double y, double z, Point2D.Double wp, double cosphi, double sinphi) {
		wp.x = z;
		wp.y = x * cosphi + y * sinphi;

	}

	private WorldPolygon[] getFromBSTPanel(BSTxyPanel panel, double cosphi, double sinphi) {

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
	 * Draw a recon hit from hit based or time based tracking
	 * 
	 * @param g the Graphics context
	 * @param container the drawing container
	 * @param fillColor the fill color
	 * @param frameColor the border color
	 */
	public void drawDCReconHit(Graphics g, IContainer container, Color fillColor, Color frameColor, DCReconHit hit,
			boolean isTimeBased) {

		SectorSuperLayer sectSL = _superLayers[(hit.sector < 4) ? 0 : 1][hit.superlayer - 1];
		sectSL.drawDCReconHit(g, container, fillColor, frameColor, hit, isTimeBased);

	}
	
	/**
	 * Draw a raw dc hit (and also used for NN overlays) from hit based or time based tracking
	 * 
	 * @param g the Graphics context
	 * @param container the drawing container
	 * @param fillColor the fill color
	 * @param frameColor the border color
	 * @param hit the  hit to  draw
	 */
	public void drawDCRawHit(Graphics g, IContainer container, Color fillColor, Color frameColor, DCTdcHit hit) {

		SectorSuperLayer sectSL = _superLayers[(hit.sector < 4) ? 0 : 1][hit.superlayer - 1];
		sectSL.drawDCRawHit(g, container, fillColor, frameColor, hit);

	}


	/**
	 * Clone the view.
	 * 
	 * @return the cloned view
	 */
	@Override
	public BaseView cloneView() {
		super.cloneView();
		CLONE_COUNT[_displaySectors.ordinal()]++;

		// limit
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



}
