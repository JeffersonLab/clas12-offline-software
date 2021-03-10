package cnuphys.ced.cedview.magfieldview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.cedview.SliceView;
import cnuphys.ced.cedview.sectorview.DisplaySectors;
import cnuphys.ced.cedview.sectorview.SwimTrajectoryDrawer;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.item.BeamLineItem;
import cnuphys.ced.item.MagFieldItem;
import cnuphys.magfield.GridCoordinate;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;

/**
 * The mag field view is used for testing the magnetic field
 * 
 * @author heddle
 *
 */
public class MagfieldView extends SliceView implements ChangeListener {
	
	private static final Color TRANSSOLENOID = new Color(30, 30, 190, 64);
	private static final Color TRANSTORUS = new Color(190, 0, 30, 64);
	
	// used to draw swum trajectories (if any) in the after drawer
	private SwimTrajectoryDrawer _swimTrajectoryDrawer;

	//the beamline
	private BeamLineItem _beamLineItem;


	private MagfieldView(DisplaySectors displaySectors, Object... keyVals) {
		super(displaySectors, keyVals);
		
		// draws any swum trajectories (in the after draw)
		_swimTrajectoryDrawer = new SwimTrajectoryDrawer(this);

		
		addItems();
		setBeforeDraw();
		setAfterDraw();
	}

	public static MagfieldView createMagfieldView(DisplaySectors displaySectors) {
		MagfieldView view = null;

		double xo = -500; // cm. Think of sector 1. x is "vertical"
		double zo = -300.0; // cm. Think of sector 1. z is "horizontal"
		double wheight = 1000;
		double wwidth = 900;

		Dimension d = GraphicsUtilities.screenFraction(0.65);

		// give container same aspect ratio
		int height = d.height;
		int width = (int) ((wwidth * height) / wheight);

		String title = "Magnetic Field Testing View";
		switch (displaySectors) {
		case SECTORS14:
			title += " sectors 1 and 4";
			break;
		case SECTORS25:
			title += " sectors 2 and 5";
			break;
		case SECTORS36:
			title += " sectors 3 and 6";
			break;
		}

		if (CLONE_COUNT[displaySectors.ordinal()] > 0) {
			title += "_(" + CLONE_COUNT[displaySectors.ordinal()] + ")";
		}

		// create the view
		view = new MagfieldView(displaySectors, PropertySupport.WORLDSYSTEM,
				new Rectangle2D.Double(zo, xo, wwidth, wheight),
				PropertySupport.LEFT, LEFT, PropertySupport.TOP, TOP, 
				PropertySupport.WIDTH, width, PropertySupport.HEIGHT, height, PropertySupport.TOOLBAR, true,
				PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS, PropertySupport.VISIBLE, true,
				PropertySupport.BACKGROUND, Color.white, PropertySupport.TITLE, title,
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view, ControlPanel.DISPLAYARRAY + 
				ControlPanel.PHISLIDER + ControlPanel.FEEDBACK + ControlPanel.FIELDLEGEND, 
				DisplayBits.MAGFIELD + DisplayBits.MAGGRID, 3, 5);

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
		_beamLineItem = new BeamLineItem(detectorLayer);
	}

	/**
	 * Set the view's before draw
	 */
	private void setBeforeDraw() {
		IDrawable beforeDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}

		};
		getContainer().setBeforeDraw(beforeDraw);
	}

	/**
	 * Set the view's after draw
	 */
	private void setAfterDraw() {
		IDrawable afterDraw = new DrawableAdapter() {

			@Override
			public void draw(Graphics g, IContainer container) {
				
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// draw trajectories
				_swimTrajectoryDrawer.draw(g, container);

				//field map grids
				drawGrids(g, container);

				// scale
				if (_scaleDrawer != null) {
		//			if ((_scaleDrawer != null) && showScale()) {
					_scaleDrawer.draw(g, container);
				}
				
				//redraw beamline
				_beamLineItem.draw(g2, container);

			}

		};
		getContainer().setAfterDraw(afterDraw);
	}

	// draw the field map grids
	private void drawGrids(Graphics g, IContainer container) {
		
		if (!showMagGrid()) {
			return;
		}

		Solenoid solenoid = MagneticFields.getInstance().getSolenoid();
		Torus torus = MagneticFields.getInstance().getTorus();
		
		Rectangle.Double wr = new Rectangle.Double();
		worldBounds(container, wr);

		GridCoordinate rhoCoordinate;
		GridCoordinate zCoordinate;

		if (solenoid != null) {
			rhoCoordinate = solenoid.getQ2Coordinate();
			zCoordinate = solenoid.getQ3Coordinate();
			drawGrid(g, container, TRANSSOLENOID, wr, rhoCoordinate, zCoordinate);
		}

		if (torus != null) {
			rhoCoordinate = torus.getQ2Coordinate();
			zCoordinate = torus.getQ3Coordinate();
			drawGrid(g, container, TRANSTORUS, wr, rhoCoordinate, zCoordinate);
		}

	}
	
	//get the world bounds
	private void worldBounds(IContainer container, Rectangle.Double wr) {
		Rectangle b = container.getComponent().getBounds();
		b.x = 0;
		b.y = 0;
		container.localToWorld(b, wr);
	}

	// draw a grid
	private void drawGrid(Graphics g, IContainer container, Color color, 
			Rectangle.Double worldBounds,
			GridCoordinate rhoGrid,
			GridCoordinate zGrid) {

		g.setColor(color);
		Point p0 = new Point();
		Point p1 = new Point();
		
		double rhoMax = rhoGrid.getMax();
		double minY = worldBounds.getMinY();
		double maxY = worldBounds.getMaxY();
		double zMin = zGrid.getMin();
		double zMax = zGrid.getMax();

		for (int iz = 0; iz < zGrid.getNumPoints(); iz++) {
			double z = zGrid.getValue(iz);
			
			if ((z >= worldBounds.getMinX()) && (z <= worldBounds.getMaxX())) {
				
				
				if (minY > 0) {
					minY = Math.min(minY, rhoMax);
				}
				if (minY < 0) {
					minY = Math.max(minY, -rhoMax);
				}
				
				if (maxY > 0) {
					maxY = Math.min(maxY, rhoMax);
				}
				if (maxY < 0) {
					maxY = Math.max(maxY, -rhoMax);
				}

				
				container.worldToLocal(p0, z, minY);
				container.worldToLocal(p1, z, maxY);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}

		}
		
		for (int ir = 0; ir < rhoGrid.getNumPoints(); ir++) {
			double yplus = rhoGrid.getValue(ir);
			double yminus = -yplus;
			
			double z0 = Math.max(zMin, worldBounds.getMinX());
			double z1 = Math.min(zMax, worldBounds.getMaxX());

			if ((yplus >= minY) && (yplus <= maxY)) {
				container.worldToLocal(p0, z0, yplus);
				container.worldToLocal(p1, z1, yplus);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
			
			if ((yminus >= minY) && (yminus <= maxY)) {
				container.worldToLocal(p0, z0, yminus);
				container.worldToLocal(p1, z1, yminus);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}

			
		}

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();

		if (source == _controlPanel.getPhiSlider()) {
			// change the projection plane
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

		int sector = getSector(container, pp, wp);
		feedbackStrings.add("$yellow$ sector " + sector);
		commonFeedbackStrings(container, pp, wp, feedbackStrings);

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

		MagfieldView view = createMagfieldView(_displaySectors);
		view.setBounds(vr);
		return view;

	}

}
