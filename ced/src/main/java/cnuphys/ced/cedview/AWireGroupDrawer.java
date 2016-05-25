package cnuphys.ced.cedview;

import java.awt.Graphics;
import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.log.Log;
import cnuphys.ced.clasio.ClasIoEventManager;

public abstract class AWireGroupDrawer extends DrawableAdapter {


	// convenient access to the event manager
	protected ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	@Override
	public abstract void draw(Graphics g, IContainer container);

	/**
	 * Convenience method to check if array is non-null and has the expected
	 * length.
	 * 
	 * @param array
	 *            the array to check.
	 * @param expectedLength
	 *            the length it should have.
	 * @return <code>true</code> if the array does not match--i.e., it is bad.
	 */
	protected boolean badIntArray(int array[], int expectedLength) {
		if ((array != null) && (array.length == expectedLength)) {
			return false;
		}
		Log.getInstance().warning(
				"Bad int Array in a Wire Group Drawer. Drawing Aborted.");
		return true;
	}

}
