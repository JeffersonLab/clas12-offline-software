package cnuphys.bCNU.feedback;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.event.EventListenerList;

import cnuphys.bCNU.application.GlobalOptions;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.TextUtilities;

public class FeedbackControl {

    // the parent container for which this object controls the feedback
    private IContainer _container;

    // List of feedback providers for the parent container
    private EventListenerList _listenerList;

    // the newly acquired feedback strings
    private ArrayList<String> _newFeedbackStrings = new ArrayList<String>(50);

    // the previous feedback strings
    private ArrayList<String> _oldFeedbackStrings = new ArrayList<String>(50);

    private static int skipUpdateLimit = 10;

    private int skippedUpdates = 0;

    /**
     * Create a feedback controller for a container.
     * 
     * @param container
     */
    public FeedbackControl(IContainer container) {
	_container = container;
    }

    /**
     * Request feedback strings from all providers.
     * 
     * @param pp
     *            the screen location of the mouse.
     * @param wp
     *            the corresponding world points.
     */
    private void requestFeedbackStrings(Point pp, Point2D.Double wp) {

	_newFeedbackStrings.clear();

	if (_listenerList == null) {
	    return;
	}

	// Guaranteed to return a non-null array
	Object[] listeners = _listenerList.getListenerList();

	// This weird loop is the bullet proof way of notifying all listeners.
	// for (int i = listeners.length - 2; i >= 0; i -= 2) {
	// order is flipped so it goes in order as added
	for (int i = 0; i < listeners.length; i += 2) {
	    if (listeners[i] == IFeedbackProvider.class) {
		((IFeedbackProvider) listeners[i + 1]).getFeedbackStrings(
			_container, pp, wp, _newFeedbackStrings);
	    }
	}
    }

    /**
     * Add a Feedback provider.
     * 
     * @param provider
     *            the Feedback provider listener to add.
     */
    public void addFeedbackProvider(IFeedbackProvider provider) {

	if (_listenerList == null) {
	    _listenerList = new EventListenerList();
	}

	// avoid adding duplicates
	_listenerList.remove(IFeedbackProvider.class, provider);
	_listenerList.add(IFeedbackProvider.class, provider);
    }

    /**
     * Remove a Feedback provider.
     * 
     * @param provider
     *            the Feedback provider to remove.
     */
    public void removeFeedbackProvider(IFeedbackProvider provider) {

	if ((provider == null) || (_listenerList == null)) {
	    return;
	}

	_listenerList.remove(IFeedbackProvider.class, provider);
    }

    /**
     * The mouse has moved, so update the feedback
     * 
     * @param mouseEvent
     *            the screen location
     * @param wp
     *            the corresponding world point.
     * @param dragging
     *            <code>true</code> if we are dragging
     */
    public void updateFeedback(MouseEvent mouseEvent, Point2D.Double wp,
	    boolean dragging) {

	// skip feedback if control down
	if (mouseEvent.isControlDown()) {
	    return;
	}

	// get strings from all providers
	requestFeedbackStrings(mouseEvent.getPoint(), wp);

	// always update if we have a urhere
	boolean haveURHere = (_container.getYouAreHereItem() != null);

	// if the strings haven't changed, return
	if (!haveURHere
		&& (skippedUpdates < skipUpdateLimit)
		&& (TextUtilities.equalStringLists(_oldFeedbackStrings,
			_newFeedbackStrings))) {
	    skippedUpdates++;
	    return;
	}

	// update feedback pane if there is one
	if (_container.getFeedbackPane() != null) {
	    _container.getFeedbackPane().updateFeedback(_newFeedbackStrings);
	}
	// update heads up if there is one and the global flag is set
	if (GlobalOptions.isHeadsUpVisible()) {
	    if ((_container.getHeadsUp() != null) && !dragging) {

		// it is ok if there is not a view (as in a standalone app) but
		// if there is
		// a view it should be on top
		if ((_container.getView() == null)
			|| _container.getView().isOnTop()) {
		    _container.getHeadsUp().updateHeadsUp(_newFeedbackStrings,
			    mouseEvent);
		    skippedUpdates = 0; // reset
		}
	    }
	}

	// swap old and new
	ArrayList<String> temp = _oldFeedbackStrings;
	_oldFeedbackStrings = _newFeedbackStrings;
	_newFeedbackStrings = temp;
	_newFeedbackStrings.clear();

    }

}
