package cnuphys.ced.cedview.central;

import java.util.List;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.event.data.BST;
import cnuphys.ced.geometry.SVTxyPanel;

public class CentralSupport {

	/**
	 * Mark which panels have hits. Used by BSTzView and SectorView(s).
	 * 
	 * @param panels
	 *            the list of panels
	 */
	public static void markPanelHits(CedView view,  List<SVTxyPanel> panels) {
		
		if (panels == null) {
			return;
		}

		for (SVTxyPanel panel : panels) {
			if (panel != null) {
				panel.hit[0] = false;
				panel.hit[1] = false;
				panel.hit[2] = false;
			}
			else {
				Log.getInstance().warning("Unexpected null panel in BSTSupport.markPanelHits.");
			}
		}

		int hitCount = BST.hitCount();
		if (hitCount < 1) {
			return;
		}

		//NOTE this uses "true" (gemc data) to segment the z direction
		double z[] = BST.avgLz();
		int bstsector[] = BST.sector();
		int bstlayer[] = BST.layer();

		boolean segmentZ = (z != null) && (view.showMcTruth());

		if ((bstsector != null) && (bstlayer != null)) {
			int len = (bstsector == null) ? 0 : bstsector.length;
			for (int i = 0; i < len; i++) {
				for (SVTxyPanel panel : panels) {

					if ((panel.getLayer() == bstlayer[i]) && (panel.getSector() == bstsector[i])) {

						if (segmentZ) {
							int zindex = panel.getZIndex(z[i]);

							if (zindex >= 0) {
								panel.hit[zindex] = true;
							}
						} else {
							panel.hit[0] = true;
							panel.hit[1] = true;
							panel.hit[2] = true;
						}
						
						break;
					}
				}
			}
		}

	} //markPanelHits

}
