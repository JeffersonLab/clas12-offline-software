package cnuphys.ced.cedview.bst;

import java.util.List;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.BSTDataContainer;
import cnuphys.ced.geometry.BSTxyPanel;

public class BSTSupport {

	/**
	 * Mark which panels have hits. Used by BSTzView and SectorView(s).
	 * 
	 * @param panels
	 *            the list of panels
	 */
	public static void markPanelHits(List<BSTxyPanel> panels) {
		for (BSTxyPanel panel : panels) {
			panel.hit[0] = false;
			panel.hit[1] = false;
			panel.hit[2] = false;
		}

		ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();
		BSTDataContainer bstData = _eventManager.getBSTData();
		int hitCount = bstData.getHitCount(0);
		if (hitCount < 1) {
			return;
		}

		double z[] = bstData.bst_true_avgLz;
		if (z == null) {
			return;
		}

		int len = z.length;
		for (int i = 0; i < len; i++) {
			for (BSTxyPanel panel : panels) {
				if ((panel.getLayer() == bstData.bst_dgtz_layer[i])
						&& (panel.getSector() == bstData.bst_dgtz_sector[i])) {
					int zindex = panel.getZIndex(z[i]);

					if (zindex >= 0) {
						panel.hit[zindex] = true;
					}
					break;
				}
			}
		}

	}

}
