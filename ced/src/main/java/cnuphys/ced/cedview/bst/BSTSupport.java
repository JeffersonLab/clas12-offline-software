package cnuphys.ced.cedview.bst;

import java.util.List;

import cnuphys.bCNU.log.Log;
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
		
		if (panels == null) {
			return;
		}

		for (BSTxyPanel panel : panels) {
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
		
		if ((z != null) && (bstsector != null) && (bstlayer != null)) {
			try {
				int len = (z == null) ? 0 : z.length;
				for (int i = 0; i < len; i++) {
					for (BSTxyPanel panel : panels) {
						if ((panel.getLayer() == bstlayer[i])
								&& (panel.getSector() == bstsector[i])) {

							if (view.showMcTruth()) {
								int zindex = panel.getZIndex(z[i]);

								if (zindex >= 0) {
									panel.hit[zindex] = true;
								}
							}
							else {
								//note "true (gemc z vals)" data use full z range
								panel.hit[0] = true;
								panel.hit[1] = true;
								panel.hit[2] = true;
							}
							
							break;
						}
					}
				} //end for i = 0 < len
			}
			catch (NullPointerException e) {
				Log.getInstance().exception(e);
				e.printStackTrace();
			}
		}


	} //markPanelHits

}
