package cnuphys.ced.cedview.projecteddc;


import cnuphys.ced.common.CedViewDrawer;

public abstract class ProjectedViewDrawer extends CedViewDrawer {

	// the SectorView being rendered.
	protected ProjectedDCView _view;

	public ProjectedViewDrawer(ProjectedDCView view) {
		super(view);
		_view = view;
	}
}
