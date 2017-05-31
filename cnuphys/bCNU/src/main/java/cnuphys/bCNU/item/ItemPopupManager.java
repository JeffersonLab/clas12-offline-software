package cnuphys.bCNU.item;

import java.awt.Point;

import javax.swing.JPopupMenu;

import cnuphys.bCNU.graphics.container.IContainer;

public class ItemPopupManager {

	/**
	 * prepare for popup. The ight click callback should call this/
	 * 
	 * @param item
	 *            the item being popped up
	 * @param container
	 *            the container being rendered
	 * @param pp
	 *            the right click location
	 */

	public static void prepareForPopup(AItem item, IContainer container,
			Point pp) {

		if ((item == null) || (pp == null)) {
			return;
		}

		JPopupMenu ip = item.getPopupMenu(container, pp);

		if (ip == null) {
			return;
		}
		// ip.setLightWeightPopupEnabled(false);
		ip.show(container.getComponent(), pp.x, pp.y);
	}
}
