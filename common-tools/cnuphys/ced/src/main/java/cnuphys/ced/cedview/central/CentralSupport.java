package cnuphys.ced.cedview.central;

import java.util.List;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.cedview.CedView;
import cnuphys.ced.geometry.BSTxyPanel;

public class CentralSupport {

	/**
	 * Mark which panels have hits. Used by CentralZView and SectorView(s).
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
				Log.getInstance().warning("Unexpected null panel in CentralSupport.markPanelHits.");
			}
		}

	} //markPanelHits

}
