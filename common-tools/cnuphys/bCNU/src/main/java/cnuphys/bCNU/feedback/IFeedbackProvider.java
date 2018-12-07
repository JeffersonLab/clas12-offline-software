package cnuphys.bCNU.feedback;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.EventListener;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;

public interface IFeedbackProvider extends EventListener {

	/**
	 * Add any appropriate feedback strings
	 * panel.
	 * 
	 * @param container
	 *            the container.
	 * @param pp
	 *            the mouse location.
	 * @param wp
	 *            the corresponding world point.
	 * @param feedbackStrings
	 *            the List of feedback strings to add to.
	 */
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings);
}
