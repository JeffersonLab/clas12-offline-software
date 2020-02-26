package cnuphys.bCNU.item;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.layer.LogicalLayer;

public class YouAreHereItem extends PointItem {

	/**
	 * Constructor for a YouAreHereItem which is like a reference point.
	 * 
	 * @param layer
	 *            the Layer this item is on.
	 */
	public YouAreHereItem(LogicalLayer layer) {
		super(layer, ImageManager.getInstance().loadImageIcon(
				"images/youarehere.gif"));

		this.setAlignmentV(BOTTOM);
	}

	/**
	 * Convenience function to create a YouAreHere item
	 * 
	 * @param container
	 *            the container whose one and only YouAreHereItem is being
	 *            created.
	 * @param location
	 *            the default location.
	 * @return the create YouAreHereItem
	 */
	public static YouAreHereItem createYouAreHereItem(IContainer container,
			Point2D.Double location) {
		LogicalLayer glassLayer = container.getGlassLayer();
		YouAreHereItem item = new YouAreHereItem(glassLayer);

		container.setYouAreHereItem(item);

		item.setDraggable(true);
		item.setDeletable(true);
		item.setLocked(false);
		item.setFocus(location);
		return item;
	}

	/**
	 * Add any appropriate feedback strings
	 * panel.
	 * 
	 * @param container
	 *            the Base container.
	 * @param pp
	 *            the mouse location.
	 * @param Point2D
	 *            .Double the corresponding world point.
	 * @param feedbackStrings
	 *            the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {

		if (contains(container, pp)) {
			String s = "Anchor at " + container.getLocationString(_focus);
			feedbackStrings.add(s);
		}
	}

}
