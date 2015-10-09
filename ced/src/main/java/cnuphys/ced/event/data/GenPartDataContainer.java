package cnuphys.ced.event.data;

import java.util.List;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;

public class GenPartDataContainer extends ADataContainer {

	/** ID of the particle */
	public int[] genpart_true_pid;

	/** x component of momentum Mev/c^2 */
	public double[] genpart_true_px;

	/** y component of momentum Mev/c^2 */
	public double[] genpart_true_py;

	/** z component of momentum Mev/c^2 */
	public double[] genpart_true_pz;

	/** x position of vertex mm */
	public double[] genpart_true_vx;

	/** y position of vertex mm */
	public double[] genpart_true_vy;

	/** z position of vertex mm */
	public double[] genpart_true_vz;

	public GenPartDataContainer(ClasIoEventManager eventManager) {
		super(eventManager);
	}

	@Override
	public int getHitCount(int option) {
		return 0;
	}

	@Override
	public void load(EvioDataEvent event) {
		if (event == null) {
			return;
		}

		if (event.hasBank("GenPart::true")) {
			genpart_true_pid = event.getInt("GenPart::true.pid");
			genpart_true_px = event.getDouble("GenPart::true.px");
			genpart_true_py = event.getDouble("GenPart::true.py");
			genpart_true_pz = event.getDouble("GenPart::true.pz");
			genpart_true_vx = event.getDouble("GenPart::true.vx"); // mm
			genpart_true_vy = event.getDouble("GenPart::true.vy"); // mm
			genpart_true_vz = event.getDouble("GenPart::true.vz"); // mm
		} // GenPart::true

	} // load

	@Override
	public void clear() {
		genpart_true_pid = null;
		genpart_true_px = null;
		genpart_true_py = null;
		genpart_true_pz = null;
		genpart_true_vx = null;
		genpart_true_vy = null;
		genpart_true_vz = null;
	} // clear

	@Override
	public void addPreliminaryFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {
	}

	@Override
	public void addTrueFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {
	}

	@Override
	public void addDgtzFeedback(int hitIndex, int option,
			List<String> feedbackStrings) {
	}

	@Override
	public void addFinalFeedback(int option, List<String> feedbackStrings) {
	}

	@Override
	public void addReconstructedFeedback(int option,
			List<String> feedbackStrings) {
	}

	@Override
	public void finalEventPrep(EvioDataEvent event) {
		extractUniqueLundIds(genpart_true_pid);
	}

}
