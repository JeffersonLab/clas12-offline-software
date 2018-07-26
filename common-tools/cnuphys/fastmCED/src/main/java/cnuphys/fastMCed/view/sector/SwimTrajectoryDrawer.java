package cnuphys.fastMCed.view.sector;

import java.awt.Graphics;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.magneticfield.swim.ASwimTrajectoryDrawer;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.geometry.GeometryManager;
import cnuphys.fastMCed.streaming.StreamManager;
import cnuphys.magfield.FastMath;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.SwimTrajectory2D;

public class SwimTrajectoryDrawer extends ASwimTrajectoryDrawer {

	private SectorView _view;

	public SwimTrajectoryDrawer(SectorView view) {
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
		if (!StreamManager.getInstance().isStarted()) {
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
				&& (trajectory.userObject instanceof SectorView)) {
			return (trajectory.userObject != _view);
		}

		
		
		boolean onThisView = _view.inThisView(getMostCommonSector(trajectory));
		return !onThisView;
	}
	
	/**
	 * Get the average phi for this trajectory based on positions, not
	 * directions
	 * 
	 * @return the average phi value in degrees
	 */
	public int getMostCommonSector(SwimTrajectory traj) {
		
		int sector[] = {0,0,0,0,0,0,0};
		
	//	System.err.println("\n--------------");
				
		int step = 1;
		for (int i = step; i < traj.size(); i += step) {
			double pos[] = traj.get(i);
			double x = pos[SwimTrajectory.X_IDX];
			double y = pos[SwimTrajectory.Y_IDX];
			double tp = FastMath.atan2Deg(y, x);
			
	//		System.err.println("  >>  SECTOR: " + GeometryManager.getSector(tp));
			sector[GeometryManager.getSector(tp)] += 1;
		}

		int maxSector = 1;
		
		for (int i = 2; i <= 6; i++) {
			if (sector[i] > sector[maxSector]) {
				maxSector = i;
			}
		}
		
	//	System.err.println("MAX SECTOR: " + maxSector);
		return maxSector;
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
		
	//	String source  = trajectory.getSource().toLowerCase();		
		return true;
	}

}
