package cnuphys.ced.cedview.bst;

import java.util.List;

import cnuphys.ced.cedview.CedView;
import cnuphys.ced.event.data.BST;
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

		int hitCount = BST.hitCount();
		if (hitCount < 1) {
			return;
		}

		//NOTE this uses "true" (gemc data) to segment the z direction
		double z[] = BST.avgLz();
		int bstsector[] = BST.sector();
		int bstlayer[] = BST.layer();

		int len = z.length;
		for (int i = 0; i < len; i++) {
			for (BSTxyPanel panel : panels) {
				if ((panel.getLayer() == bstlayer[i])
						&& (panel.getSector() == bstsector[i])) {

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
