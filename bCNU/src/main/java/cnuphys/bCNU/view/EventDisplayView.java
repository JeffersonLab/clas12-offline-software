package cnuphys.bCNU.view;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.et.ETSupport;
import cnuphys.bCNU.event.EventControl;
import cnuphys.bCNU.event.IPhysicsEventListener;
import cnuphys.bCNU.feedback.IFeedbackProvider;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.swim.Swimming;

import org.jlab.coda.jevio.EvioEvent;

/**
 * This is the abstract base class for all views that display events.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public abstract class EventDisplayView extends BaseView implements
		IPhysicsEventListener, IFeedbackProvider, SwimTrajectoryListener {

	// are we showing single events or are we showing accumulated data
	public enum Mode {
		SINGLE_EVENT, ACCUMULATED
	};

	// our mode
	private Mode _mode = Mode.SINGLE_EVENT;

	/**
	 * A string that has r-theta-phi using unicode greek characters
	 */
	public static final String rThetaPhi = "r" + UnicodeSupport.SMALL_THETA
			+ UnicodeSupport.SMALL_PHI;

	/**
	 * A string that has rho-theta-phi using unicode greek characters
	 */
	public static final String rhoZPhi = UnicodeSupport.SMALL_RHO + "z"
			+ UnicodeSupport.SMALL_PHI;

	/**
	 * A string that hasrho-phi using unicode greek characters for hex views
	 */
	public static final String rhoPhi = UnicodeSupport.SMALL_RHO
			+ UnicodeSupport.SMALL_PHI;

	/**
	 * Current evio event
	 */
	protected static EvioEvent _currentEvent;

	/**
	 * Name for the detector layer.
	 */
	public static final String _detectorLayerName = "Detectors";

	/**
	 * Name for the magnetic field layer.
	 */
	public static final String _magneticFieldLayerName = "Magnetic Field";

	/**
	 * Create an EventDisplayView
	 * 
	 * @param keyVals
	 *            variable set of arguments.
	 */
	public EventDisplayView(Object... keyVals) {
		super(keyVals);

		EventControl.getInstance().addPhysicsListener(this);
		getContainer().getFeedbackControl().addFeedbackProvider(this);

		// add the magnetic field layer--mostly unused.
		getContainer().addLogicalLayer(_magneticFieldLayerName);

		// add the detector drawing layer
		getContainer().addLogicalLayer(_detectorLayerName);

		// listen for trajectory changes
		Swimming.addSwimTrajectoryListener(this);
	}

	/**
	 * A new event has arrived from jevio. This is called by the generic
	 * EventContol object. By the time we get here any detector specific parsing
	 * on this event should be done, provided you haven't put the detector
	 * specific parsing in a separate thread. This is the actual event not a
	 * copy so it should not be modified.
	 * 
	 * @param event
	 *            the new event.
	 */
	@Override
	public void newPhysicsEvent(final EvioEvent event) {

		_currentEvent = event;
		if (!EventControl.getInstance().isAccumulating()) {
			getContainer().redoFeedback();
			getContainer().refresh();
			if (getContainer().getHeadsUp() != null) {
				getContainer().getHeadsUp().updateHeadsUp(null, null);
			}
		}
	}

	/**
	 * Clear the event.
	 */
	public void clearEvent() {
		_currentEvent = null;
		getContainer().refresh();
	}

	/**
	 * Some common feedback. Subclasses should override and then call
	 * super.getFeedbackStrings
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

		// add some information about the current event
		if (_currentEvent == null) {
			feedbackStrings.add("$orange red$No event");
		} else {
			feedbackStrings
					.add("$orange red$" + _currentEvent.getDescription());

			if (EventControl.isSourceET()) {
				feedbackStrings.add("$orange red$" + "ET event "
						+ ETSupport.getETEventNumber());
			} else {
				feedbackStrings.add("$orange red$" + "event # "
						+ _currentEvent.getEventNumber());
			}
		}

	}

	/**
	 * Convenience method to get the detector layer from the container.
	 * 
	 * @return the detector layer.
	 */
	public LogicalLayer getDetectorLayer() {
		return getContainer().getLogicalLayer(_detectorLayerName);
	}

	/**
	 * Convenience method to get the magnetic field layer from the container.
	 * 
	 * @return the magnetic field layer.
	 */
	public LogicalLayer getMagneticFieldLayer() {
		return getContainer().getLogicalLayer(_magneticFieldLayerName);
	}

	/**
	 * Get the mode for the display.
	 * 
	 * @return the mode. This is either Mode.SINGLE_EVENT or Mode.ACCUMULATED.
	 */
	public Mode getMode() {
		return _mode;
	}

	/**
	 * Set the mode for this view.
	 * 
	 * @param mode
	 *            the mode to set. This is either Mode.SINGLE_EVENT or
	 *            Mode.ACCUMULATED.
	 */
	public void setMode(Mode mode) {
		_mode = mode;
	}

	/**
	 * The swum trajectories have changed
	 */
	@Override
	public void trajectoriesChanged() {
		getContainer().refresh();
	}
}
