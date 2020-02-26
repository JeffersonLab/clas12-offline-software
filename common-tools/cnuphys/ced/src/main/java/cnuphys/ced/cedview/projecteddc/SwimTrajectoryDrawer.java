package cnuphys.ced.cedview.projecteddc;

import java.awt.Graphics;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.magneticfield.swim.ASwimTrajectoryDrawer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.SwimTrajectory2D;

public class SwimTrajectoryDrawer extends ASwimTrajectoryDrawer {

	private ProjectedDCView _view;

	public SwimTrajectoryDrawer(ProjectedDCView view) {
		_view = view;
		_markSectChanges = true;
	}

	/**
	 * Actual drawing method
	 * 
	 * @param g
	 *            the graphics context
	 * @param container
	 *            the base container
	 */
	@Override
	public void draw(Graphics g, IContainer container) {
		if (!ClasIoEventManager.getInstance().isAccumulating() && _view.isSingleEventMode()) {
			super.draw(g, container);
		}
	}

	/**
	 * Here we have a chance to veto a trajectory. For example, we may decide
	 * that the trajectory won't appear on this view (assuming a view owns this
	 * drawer) and so don't bother to compute it. The default implementation
	 * vetoes nothing.
	 * 
	 * @param trajectory
	 *            the trajectory to test.
	 * @return <code>true</code> if this trajectory is vetoed.
	 */
	@Override
	protected boolean veto(SwimTrajectory trajectory) {
		if ((trajectory.userObject != null)
				&& (trajectory.userObject instanceof ProjectedDCView)) {
			return (trajectory.userObject != _view);
		}

		double phi = trajectory.getOriginalPhi();
		boolean onThisView = _view.inThisView(phi);
//		System.err.println("ORIG PHI PDC: " + phi +  " on view: " + onThisView);
		return !onThisView;
	}

	/**
	 * From detector xyz get the projected world point.
	 * 
	 * @param v3d
	 *            the 3D vector (meters)
	 * @param wp
	 *            the projected world point.
	 */
	@Override
	public void project(double[] v3d, Point2D.Double wp) {

		// the path in the 3D traj is in meters. We want cm.
		_view.projectClasToWorld(100 * v3d[0], 100 * v3d[1], 100 * v3d[2], _view.getProjectionPlane(), wp);

	}

	@Override
	public void prepareForRemoval() {
	}

	@Override
	public void setDirty(boolean dirty) {
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public void setVisible(boolean visible) {
	}
	
	@Override
	public boolean acceptSimpleTrack(SwimTrajectory2D trajectory) {
		
		String source  = trajectory.getSource().toLowerCase();

		if (source.contains("hbtracks")) {
			return _view.showHB();
		}
		else if (source.contains("tbtracks")) {
			return _view.showTB();
			}
		else if (source.contains("cvtrec")) {
			return _view.showCVTTracks();
		}
		
		return true;
	}

}
