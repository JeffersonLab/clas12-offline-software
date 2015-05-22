package cnuphys.ced.cedview.sectorview;

import java.awt.Graphics;
import java.awt.geom.Point2D;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.magneticfield.swim.ASwimTrajectoryDrawer;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.swim.SwimTrajectory;

public class SwimTrajectoryDrawer extends ASwimTrajectoryDrawer {

    private SectorView _view;

    public SwimTrajectoryDrawer(SectorView view) {
	_view = view;
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
	if (!ClasIoEventManager.getInstance().isAccumulating()) {
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

	double phi = trajectory.getOriginalPhi();
	return !_view.inThisView(phi);
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
	_view.getWorldFromLabXYZ(100 * v3d[0], 100 * v3d[1], 100 * v3d[2], wp);

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

}
