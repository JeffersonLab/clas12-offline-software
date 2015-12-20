package cnuphys.ced.cedview.bst;

import java.util.List;

import cnuphys.ced.cedview.CedView;
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
	public static void markPanelHits(CedView view,  List<BSTxyPanel> panels) {
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

		//NOTE this uses "true" (gemc data) to segment the z direction
		double z[] = bstData.bst_true_avgLz;

		int len = z.length;
		for (int i = 0; i < len; i++) {
			for (BSTxyPanel panel : panels) {
				if ((panel.getLayer() == bstData.bst_dgtz_layer[i])
						&& (panel.getSector() == bstData.bst_dgtz_sector[i])) {

					if (view.showMcTruth() && (z != null)) {
						int zindex = panel.getZIndex(z[i]);

						if (zindex >= 0) {
							panel.hit[zindex] = true;
						}
					}
					else {
						//no "true (gemc z vals)" data use full z range
						panel.hit[0] = true;
						panel.hit[1] = true;
						panel.hit[2] = true;
					}
					
					break;
				}
			}
		}

	}

}
