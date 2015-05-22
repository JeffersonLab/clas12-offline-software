package cnuphys.bCNU.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.feedback.IFeedbackProvider;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;

/**
 * A simple view used to test the tool bar.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class DrawingView extends BaseView implements IFeedbackProvider {

    // reserved view type for drawing view
    public static final int DRAWINGVIEWTYPE = -77002;

    /**
     * Create a drawing view
     * 
     * @param keyVals
     *            variable set of arguments.
     */
    private DrawingView(Object... keyVals) {
	super(keyVals);
	addItems();
	getContainer().getFeedbackControl().addFeedbackProvider(this);
    }

    /**
     * This adds the detector items.
     */
    private void addItems() {
    }

    /**
     * Convenience method for creating a Drawing View.
     * 
     * @return a new DrawingView object
     */
    public static DrawingView createDrawingView() {
	DrawingView view = null;

	// set to a fraction of screen
	Dimension d = GraphicsUtilities.screenFraction(0.5);

	int width = d.width;
	int height = d.height;

	// create the view
	view = new DrawingView(
		AttributeType.WORLDSYSTEM,
		new Rectangle2D.Double(0.0, 0.0, width, height),
		AttributeType.WIDTH,
		width, // container width, not total view width
		AttributeType.HEIGHT,
		height, // container height, not total view width
		AttributeType.TOOLBAR, true, AttributeType.TOOLBARBITS,
		BaseToolBar.EVERYTHING, AttributeType.VISIBLE, true,
		AttributeType.BACKGROUND, Color.white, AttributeType.HEADSUP,
		true, AttributeType.TITLE, " Drawing View ",
		AttributeType.STANDARDVIEWDECORATIONS, true,
		AttributeType.VIEWTYPE, DRAWINGVIEWTYPE);

	view.pack();
	return view;
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
    public void getFeedbackStrings(IContainer container, Point pp,
	    Point2D.Double wp, List<String> feedbackStrings) {

    }

}
