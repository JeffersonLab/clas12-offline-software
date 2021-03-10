package cnuphys.ced.ced3d;

import java.awt.Color;
import java.util.List;
import bCNU3D.Support3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.frame.CedColors;
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

		// mc tracks
		if (SwimMenu.getInstance().showMonteCarloTracks()) {
			List<SwimTrajectory> trajectories = Swimming.getMCTrajectories();

			if (trajectories != null) {

				for (SwimTrajectory trajectory : trajectories) {
					drawSwimTrajectory(drawable, trajectory, null);
				}

			}
		}

		// reconstructed?
		if (SwimMenu.getInstance().showReconstructedTracks()) {
			List<SwimTrajectory> trajectories = Swimming.getReconTrajectories();

			if (trajectories != null) {

				for (SwimTrajectory trajectory : trajectories) {

					boolean show = false;
					Color color = null;

					String source = trajectory.getSource();
					if (source != null) {
						if (source.contains("HitBasedTrkg::HBTracks")) {
							show = _cedPanel3D.showHBTrack();
							color = CedColors.HB_COLOR;
						} else if (source.contains("TimeBasedTrkg::TBTracks")) {
							show = _cedPanel3D.showTBTrack();
							color = CedColors.TB_COLOR;
						} else if (source.contains("HitBasedTrkg::AITracks")) {
							show = _cedPanel3D.showAIHBTrack();
							color = CedColors.AIHB_COLOR;
						} else if (source.contains("TimeBasedTrkg::AITracks")) {
							show = _cedPanel3D.showAITBTrack();
							color = CedColors.AITB_COLOR;
						} else if (source.contains("REC::Particle")) {
							show = _cedPanel3D.showRecTrack();
							color = Color.darkGray;
						} else if (source.contains("CVTRec::Tracks")) {
							show = _cedPanel3D.showCVTTrack();
							color = CedColors.CVT_COLOR;
						}
					}

					LundId lid = trajectory.getLundId();
					
					if (lid != null) {
						color = lid.getStyle().getLineColor();
					}
					if (show) {
						drawSwimTrajectory(drawable, trajectory, color);
					}
				}

			}
		}
	}

	// draw a trajectory in 3D
	private void drawSwimTrajectory(GLAutoDrawable drawable, SwimTrajectory traj, Color color) {
		int size = traj.size();
		if (size < 2) {
			return;
		}

		float coords[] = new float[3 * size];

		if (color == null) {
			LundId lid = traj.getLundId();
			LundStyle style = LundStyle.getStyle(lid);

			color = Color.black;

			if (style != null) {
				color = style.getFillColor();
			}
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
