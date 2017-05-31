package cnuphys.ced.cedview;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.feedback.IFeedbackProvider;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.clasio.ClasIoEventManager;

public abstract class AFeedbackProvider implements IFeedbackProvider {

	// convenient access to the event manager
	protected ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	@Override
	public abstract void getFeedbackStrings(IContainer container,
			Point screenPoint, Point2D.Double worldPoint,
			List<String> feedbackStrings);

}
