package cnuphys.ced.cedview.sectorview;

import java.awt.Graphics;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.magneticfield.swim.ASwimTrajectoryDrawer;
import cnuphys.ced.cedview.SliceView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.geometry.GeometryManager;
import cnuphys.magfield.FastMath;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.SwimTrajectory2D;

public class SwimTrajectoryDrawer extends ASwimTrajectoryDrawer {

	private SliceView _view;

	public SwimTrajectoryDrawer(SliceView view) {
		_view = view;
	}

	/**
	 * Actual drawing method
	 * 
	 * @param g         the graphics context
	 * @param container the base container
	 */
	@Override
	public void draw(Graphics g, IContainer container) {
		if (!ClasIoEventManager.getInstance().isAccumulating() && _view.isSingleEventMode()) {
			super.draw(g, container);
		}
	}
	
	@Override
	public void drawTrajectories(Graphics g, IContainer container) {
		
		
		for (SwimTrajectory2D trajectory2D : _trajectories2D) {
			
			boolean show = true;
			
			String source = trajectory2D.getSource();

			if (source != null) {
				if (source.contains("HitBasedTrkg::HBTracks")) {
					show = _view.showHB();
				} else if (source.contains("TimeBasedTrkg::TBTracks")) {
					show = _view.showTB();
				} else if (source.contains("HitBasedTrkg::AITracks")) {
					show = _view.showAIHB();
				} else if (source.contains("TimeBasedTrkg::AITracks")) {
					show = _view.showAITB();
				} else if (source.contains("REC::Particle")) {
					show = _view.showRecPart();
				}
			}
			
			
			if (show) {
				drawSwimTrajectory(g, container, trajectory2D);
			}
			
			
			if (_view.showSectorChange()) {
				markSectorChanges(g, container, trajectory2D);
			}
		}
	}

	
	/**
	 * Get the distance of closest approach to any 2D (projected) trajectory.
	 * 
	 * @param wp the point in question
	 * @return the closest distance. The closest trajectory will be cached in
	 *         <code>closestTrajectory</code>.
	 */
	@Override
	public double closestApproach(Point2D.Double wp) {
		_closestTrajectory = null;

		double minDist = Double.POSITIVE_INFINITY;
		if ((_trajectories2D == null) || (_trajectories2D.size() < 1)) {
			return minDist;
		}

		// loop over all trajectories that are drawn
		for (SwimTrajectory2D trajectory2D : _trajectories2D) {
			
			boolean show = true;
			
			String source = trajectory2D.getSource();

			if (source != null) {
				if (source.contains("HitBasedTrkg::HBTracks")) {
					show = _view.showHB();
				} else if (source.contains("TimeBasedTrkg::TBTracks")) {
					show = _view.showTB();
				} else if (source.contains("HitBasedTrkg::AITracks")) {
					show = _view.showAIHB();
				} else if (source.contains("TimeBasedTrkg::AITracks")) {
					show = _view.showAITB();
				}
			}
			
			if (!show) {
				continue;
			}

			double dist = trajectory2D.closestDistance(wp);

			if (dist < minDist) {
				_closestTrajectory = trajectory2D;
				minDist = dist;
			}
		}

		return minDist;
	}

	/**
	 * Here we have a chance to veto a trajectory. For example, we may decide that
	 * the trajectory won't appear on this view (assuming a view owns this drawer)
	 * and so don't bother to compute it. The default implementation vetoes nothing.
	 * 
	 * @param trajectory the trajectory to test.
	 * @return <code>true</code> if this trajectory is vetoed.
	 */
	@Override
	protected boolean veto(SwimTrajectory trajectory) {
		// if (true) return false;

		if ((trajectory.userObject != null) && (trajectory.userObject instanceof SectorView)) {
			System.err.println("Vetoed wrong userobject");
			return (trajectory.userObject != _view);
		}

		boolean onThisView = _view.inThisView(getMostCommonSector(trajectory));
		return !onThisView;
	}

	/**
	 * Get the average phi for this trajectory based on positions, not directions
	 * 
	 * @return the average phi value in degrees
	 */
	public int getMostCommonSector(SwimTrajectory traj) {

		int sector[] = { 0, 0, 0, 0, 0, 0, 0 };

		// System.err.println("\n--------------");

		int step = 1;
		for (int i = step; i < traj.size(); i += step) {
			double pos[] = traj.get(i);
			double x = pos[SwimTrajectory.X_IDX];
			double y = pos[SwimTrajectory.Y_IDX];
			double tp = FastMath.atan2Deg(y, x);

			// System.err.println(" >> SECTOR: " + GeometryManager.getSector(tp));
			sector[GeometryManager.getSector(tp)] += 1;
		}

		int maxSector = 1;

		for (int i = 2; i <= 6; i++) {
			if (sector[i] > sector[maxSector]) {
				maxSector = i;
			}
		}

		// System.err.println("MAX SECTOR: " + maxSector);
		return maxSector;
	}

	/**
	 * From detector xyz get the projected world point.
	 * 
	 * @param v3d the 3D vector (meters)
	 * @param wp  the projected world point.
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

		String source = trajectory.getSource().toLowerCase();

		if (source.contains("hbtracks")) {
			return _view.showHB();
		} else if (source.contains("tbtracks")) {
			return _view.showTB();
		} else if (source.contains("cvtrec")) {
			return _view.showCVTTracks();
		}

		return true;
	}

}
