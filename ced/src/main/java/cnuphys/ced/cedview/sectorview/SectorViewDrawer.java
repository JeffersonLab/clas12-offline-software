package cnuphys.ced.cedview.sectorview;

import cnuphys.ced.common.CedViewDrawer;

public abstract class SectorViewDrawer extends CedViewDrawer {

	// the SectorView being rendered.
	protected SectorView _view;

	public SectorViewDrawer(SectorView view) {
		super(view);
		_view = view;
	}
}
