package cnuphys.ced.cedview.alldc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.ced.cedview.CedView;
import cnuphys.ced.component.ControlPanel;
import cnuphys.ced.component.DisplayBits;
import cnuphys.ced.event.data.DC;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;

/**
 * The AllDCAccum view is a non-faithful representation of all six sectors of
 * driftchambers. It is always in accumulated mode.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class AllDCAccumView extends AllDCView {

	// for naming clones
	private static int CLONE_COUNT = 0;

	/**
	 * Create an allDCView
	 * 
	 * @param keyVals variable set of arguments.
	 */
	private AllDCAccumView(Object... keyVals) {
		super(AllDCMode.ACCUMULATEONLY, keyVals);
	}

	/**
	 * Convenience method for creating an AllDC View.
	 * 
	 * @return a new AllDCView.
	 */
	public static AllDCAccumView createAllDCAccumView() {
		AllDCAccumView view = null;

		// set to a fraction of screen
		Dimension d = GraphicsUtilities.screenFraction(0.65);

		// create the view
		view = new AllDCAccumView(PropertySupport.WORLDSYSTEM, _defaultWorldRectangle, PropertySupport.WIDTH, d.width, // container
																														// width,
																														// not
																														// total
																														// view
																														// width
				PropertySupport.HEIGHT, d.height, // container height, not total view width
				PropertySupport.TOOLBAR, true, PropertySupport.TOOLBARBITS, CedView.TOOLBARBITS,
				PropertySupport.VISIBLE, true, PropertySupport.TITLE,
				_baseTitle[AllDCMode.ACCUMULATEONLY.ordinal()] + ((CLONE_COUNT == 0) ? "" : ("_(" + CLONE_COUNT + ")")),
				PropertySupport.STANDARDVIEWDECORATIONS, true);

		view._controlPanel = new ControlPanel(view,
				ControlPanel.FEEDBACK + ControlPanel.ACCUMULATIONLEGEND + ControlPanel.ALLDC_ACCUM_ONLY, 0, 3, 5);

		view.add(view._controlPanel, BorderLayout.EAST);
		view.pack();
		return view;
	}

	/**
	 * Some view specific feedback. Should always call super.getFeedbackStrings
	 * first.
	 * 
	 * @param container   the base container for the view.
	 * @param screenPoint the pixel point
	 * @param worldPoint  the corresponding world location.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings) {

		// get the common information
		super.getFeedbackStrings(container, screenPoint, worldPoint, feedbackStrings);
		// feedbackStrings.add("#DC hits: " + _numHits);

		int sector = getSector(container, screenPoint, worldPoint);

		double totalOcc = 100. * DC.getInstance().totalOccupancy();
		double sectorOcc = 100. * DC.getInstance().totalSectorOccupancy(sector);
		String occStr = "total DC occ " + DoubleFormat.doubleFormat(totalOcc, 2) + "%" + " sector " + sector + " occ "
				+ DoubleFormat.doubleFormat(sectorOcc, 2) + "%";
		feedbackStrings.add("$aqua$" + occStr);

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

		AllDCAccumView view = createAllDCAccumView();
		view.setBounds(vr);
		return view;

	}

}
