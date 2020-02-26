package cnuphys.fastMCed.view;

import java.awt.Graphics;
import java.util.Vector;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.toolbar.AUserComponentDrawer;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.lund.LundId;

/**
 * This is used to draw the user component on the tool bar. For example, it will
 * put a legend of montecarlo particles--just the ones it finds in in the
 * current event.
 * 
 * @author heddle
 * 
 */

public class UserComponentLundDrawer extends AUserComponentDrawer {

	// convenience reference to event manager
	private static PhysicsEventManager _eventManager = PhysicsEventManager
			.getInstance();

	public UserComponentLundDrawer(AView view) {
		super(view);
	}

	/**
	 * Draw on the component.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the container on the view.
	 */
	@Override
	public void draw(Graphics g, IContainer container) {
		super.draw(g, container);
	}

	// convenience method to get the unique LundIds from a double (!) array of

	/**
	 * This method must be filled in to return all the unique LundIds associated
	 * with this event.
	 * 
	 * @return all the unique LundIds associated with this event.
	 */
	@Override
	protected Vector<LundId> getUniqueLundIds() {
		return _eventManager.uniqueLundIds();
	}

	@Override
	public void prepareForRemoval() {
	}

	@Override
	public void setDirty(boolean dirty) {
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public void setVisible(boolean visible) {
	}

}