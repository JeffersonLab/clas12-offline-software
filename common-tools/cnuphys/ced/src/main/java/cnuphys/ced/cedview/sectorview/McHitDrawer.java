package cnuphys.ced.cedview.sectorview;

import cnuphys.ced.common.AMcHitDrawer;

public class McHitDrawer extends AMcHitDrawer {

	public McHitDrawer(SectorView view) {
		super(view);
	}


	@Override
	protected boolean correctSector(int sector) {
		boolean showPoint = false;
		switch (((SectorView)_view).getDisplaySectors()) {
		case SECTORS14:
			showPoint = ((sector == 1) || (sector == 4));
			break;
		case SECTORS25:
			showPoint = ((sector == 2) || (sector == 5));
			break;
		case SECTORS36:
			showPoint = ((sector == 3) || (sector == 6));
			break;
		}
		return showPoint;
	}

}
