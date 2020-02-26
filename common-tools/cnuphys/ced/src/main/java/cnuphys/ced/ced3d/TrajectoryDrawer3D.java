package cnuphys.ced.ced3d;

import java.awt.Color;
import java.util.List;
import bCNU3D.Support3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundStyle;
import cnuphys.lund.LundSupport;
import cnuphys.swim.SwimMenu;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimming;

import com.jogamp.opengl.GLAutoDrawable;

import item3D.Item3D;

public class TrajectoryDrawer3D extends Item3D {

	private CedPanel3D _cedPanel3D;
	
	public TrajectoryDrawer3D(CedPanel3D panel3D) {
		super(panel3D);
		_cedPanel3D = panel3D;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {

		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		if (SwimMenu.getInstance().showMonteCarloTracks()) {
			List<SwimTrajectory> trajectories = Swimming.getMCTrajectories();

			if (trajectories != null) {

				for (SwimTrajectory trajectory : trajectories) {
					drawSwimTrajectory(drawable, trajectory);
				}

			}
		}

		// reconstructed?
		if (SwimMenu.getInstance().showReconstructedTracks()) {
			List<SwimTrajectory> trajectories = Swimming.getReconTrajectories();

			if (trajectories != null) {

				boolean showHB = _cedPanel3D.showHBTrack();
				boolean showTB = _cedPanel3D.showTBTrack();
				boolean showCVT = _cedPanel3D.showCVTTrack();

				for (SwimTrajectory trajectory : trajectories) {
					LundId lid = trajectory.getLundId();
					if ((showCVT && LundSupport.isCVT(lid)) ||(showHB && LundSupport.isHB(lid)) || (showTB && LundSupport.isTB(lid))) {
						drawSwimTrajectory(drawable, trajectory);
					}
				}

			}
		}
	}

	// draw a trajectory in 3D
	private void drawSwimTrajectory(GLAutoDrawable drawable, SwimTrajectory traj) {
		int size = traj.size();
		if (size < 2) {
			return;
		}

		float coords[] = new float[3 * size];

		LundId lid = traj.getLundId();
		LundStyle style = LundStyle.getStyle(lid);
		Color color = Color.black;

		if (style != null) {
			color = style.getFillColor();
		}

		for (int i = 0; i < size; i++) {
			double v[] = traj.get(i);
			int j = i * 3;
			// convert to cm
			coords[j] = 100 * (float) v[0];
			coords[j + 1] = 100 * (float) v[1];
			coords[j + 2] = 100 * (float) v[2];
		}

		Support3D.drawPolyLine(drawable, coords, color, 2f);
	}
}
