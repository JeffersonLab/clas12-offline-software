package cnuphys.ced.cedview.projecteddc;

import java.awt.Graphics;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.ced.common.AMcHitDrawer;

public class McHitDrawer extends AMcHitDrawer {

	public McHitDrawer(ProjectedDCView view) {
		super(view);
	}

	@Override
	protected boolean correctSector(int sector) {
		return sector == ((ProjectedDCView)_view).getSector();
	}


	@Override
	protected void drawGemCXYZHits_FTOF(Graphics g, IContainer container) {
	}

}
