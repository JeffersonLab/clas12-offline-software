package cnuphys.ced.event.data;

import java.util.List;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;

public class RecEventDataContainer extends ADataContainer {

    /** detector id */
    public int[] eventhb_detector_detector;

    /** energy to intersection point */
    public float[] eventhb_detector_energy;

    /** x coordinate of matched hit */
    public float[] eventhb_detector_hX;

    /** y coordinate of matched hit */
    public float[] eventhb_detector_hY;

    /** z coordinate of matched hit */
    public float[] eventhb_detector_hZ;

    /** index to detector banks row */
    public int[] eventhb_detector_index;

    /** layer number */
    public int[] eventhb_detector_layer;

    /** path to intersection point */
    public float[] eventhb_detector_path;

    /** index to particle section */
    public int[] eventhb_detector_pindex;

    /** sector number */
    public int[] eventhb_detector_sector;

    /** superlayer number */
    public int[] eventhb_detector_superlayer;

    /** time to intersection point */
    public float[] eventhb_detector_time;

    /** x coordinate of track intersection */
    public float[] eventhb_detector_X;

    /** y coordinate of track intersection */
    public float[] eventhb_detector_Y;

    /** z coordinate of track intersection */
    public float[] eventhb_detector_Z;

    /** beta of the particle */
    public float[] eventhb_particle_beta;

    /** charge of the particle */
    public int[] eventhb_particle_charge;

    /** chi2 for assigned particle id (0.0-1.0) */
    public float[] eventhb_particle_chi2pid;

    /** particle mass calculated from beta */
    public float[] eventhb_particle_mass;

    /** particle ID */
    public int[] eventhb_particle_pid;

    /** x component of the momentum */
    public float[] eventhb_particle_px;

    /** y component of the momentum */
    public float[] eventhb_particle_py;

    /** z component of the momentum */
    public float[] eventhb_particle_pz;

    /** status of the particle */
    public int[] eventhb_particle_status;

    /** x coordinate of vertex */
    public float[] eventhb_particle_vx;

    /** y coordinate of vertex */
    public float[] eventhb_particle_vy;

    /** z coordinate of vertex */
    public float[] eventhb_particle_vz;

    /** x component of momentum */
    public float[] recevent_particle_beta;

    /** charge of the particle */
    public byte[] recevent_particle_charge;

    /** y component of momentum */
    public float[] recevent_particle_chi2ecin;

    /** y component of momentum */
    public float[] recevent_particle_chi2ecout;

    /** y component of momentum */
    public float[] recevent_particle_chi2pcal;

    /** y component of momentum */
    public float[] recevent_particle_chi2sc;

    /** particle ID */
    public int[] recevent_particle_ecinidx;

    /** y component of momentum */
    public float[] recevent_particle_ecinX;

    /** y component of momentum */
    public float[] recevent_particle_ecinY;

    /** y component of momentum */
    public float[] recevent_particle_ecinZ;

    /** particle ID */
    public int[] recevent_particle_ecoutidx;

    /** y component of momentum */
    public float[] recevent_particle_ecoutX;

    /** y component of momentum */
    public float[] recevent_particle_ecoutY;

    /** y component of momentum */
    public float[] recevent_particle_ecoutZ;

    /** particle mass calculated from beta */
    public float[] recevent_particle_mass;

    /** particle ID */
    public int[] recevent_particle_pcalidx;

    /** y component of momentum */
    public float[] recevent_particle_pcalX;

    /** y component of momentum */
    public float[] recevent_particle_pcalY;

    /** y component of momentum */
    public float[] recevent_particle_pcalZ;

    /** particle ID */
    public int[] recevent_particle_pid;

    /** z component of momentum */
    public float[] recevent_particle_px;

    /** z component of momentum */
    public float[] recevent_particle_py;

    /** z component of momentum */
    public float[] recevent_particle_pz;

    /** particle ID */
    public int[] recevent_particle_scidx;

    /** y component of momentum */
    public float[] recevent_particle_scpath;

    /** y component of momentum */
    public float[] recevent_particle_sctime;

    /** y component of momentum */
    public float[] recevent_particle_scX;

    /** y component of momentum */
    public float[] recevent_particle_scY;

    /** y component of momentum */
    public float[] recevent_particle_scZ;

    /** status of the particle */
    public byte[] recevent_particle_status;

    /** x component of vertex */
    public float[] recevent_particle_vx;

    /** y component of vertex */
    public float[] recevent_particle_vy;

    /** z component of vertex */
    public float[] recevent_particle_vz;

    public RecEventDataContainer(ClasIoEventManager eventManager) {
	super(eventManager);
    }

    @Override
    public int getHitCount(int option) {
	return (eventhb_particle_pid == null) ? 0 : eventhb_particle_pid.length;
    }

    @Override
    public void load(EvioDataEvent event) {
	if (event == null) {
	    return;
	}

	if (event.hasBank("EVENTHB::detector")) {
	    eventhb_detector_detector = event
		    .getInt("EVENTHB::detector.detector");
	    eventhb_detector_energy = event
		    .getFloat("EVENTHB::detector.energy");
	    eventhb_detector_hX = event.getFloat("EVENTHB::detector.hX");
	    eventhb_detector_hY = event.getFloat("EVENTHB::detector.hY");
	    eventhb_detector_hZ = event.getFloat("EVENTHB::detector.hZ");
	    eventhb_detector_index = event.getInt("EVENTHB::detector.index");
	    eventhb_detector_layer = event.getInt("EVENTHB::detector.layer");
	    eventhb_detector_path = event.getFloat("EVENTHB::detector.path");
	    eventhb_detector_pindex = event.getInt("EVENTHB::detector.pindex");
	    eventhb_detector_sector = event.getInt("EVENTHB::detector.sector");
	    eventhb_detector_superlayer = event
		    .getInt("EVENTHB::detector.superlayer");
	    eventhb_detector_time = event.getFloat("EVENTHB::detector.time");
	    eventhb_detector_X = event.getFloat("EVENTHB::detector.X");
	    eventhb_detector_Y = event.getFloat("EVENTHB::detector.Y");
	    eventhb_detector_Z = event.getFloat("EVENTHB::detector.Z");
	} // EVENTHB::detector

	if (event.hasBank("EVENTHB::particle")) {
	    eventhb_particle_beta = event.getFloat("EVENTHB::particle.beta");
	    eventhb_particle_charge = event.getInt("EVENTHB::particle.charge");
	    eventhb_particle_chi2pid = event
		    .getFloat("EVENTHB::particle.chi2pid");
	    eventhb_particle_mass = event.getFloat("EVENTHB::particle.mass");
	    eventhb_particle_pid = event.getInt("EVENTHB::particle.pid");
	    eventhb_particle_px = event.getFloat("EVENTHB::particle.px");
	    eventhb_particle_py = event.getFloat("EVENTHB::particle.py");
	    eventhb_particle_pz = event.getFloat("EVENTHB::particle.pz");
	    eventhb_particle_status = event.getInt("EVENTHB::particle.status");
	    eventhb_particle_vx = event.getFloat("EVENTHB::particle.vx");
	    eventhb_particle_vy = event.getFloat("EVENTHB::particle.vy");
	    eventhb_particle_vz = event.getFloat("EVENTHB::particle.vz");
	} // EVENTHB::particle

	if (event.hasBank("RECEVENT::particle")) {
	    recevent_particle_beta = event.getFloat("RECEVENT::particle.beta");
	    recevent_particle_charge = event
		    .getByte("RECEVENT::particle.charge");
	    recevent_particle_chi2ecin = event
		    .getFloat("RECEVENT::particle.chi2ecin");
	    recevent_particle_chi2ecout = event
		    .getFloat("RECEVENT::particle.chi2ecout");
	    recevent_particle_chi2pcal = event
		    .getFloat("RECEVENT::particle.chi2pcal");
	    recevent_particle_chi2sc = event
		    .getFloat("RECEVENT::particle.chi2sc");
	    recevent_particle_ecinidx = event
		    .getInt("RECEVENT::particle.ecinidx");
	    recevent_particle_ecinX = event
		    .getFloat("RECEVENT::particle.ecinX");
	    recevent_particle_ecinY = event
		    .getFloat("RECEVENT::particle.ecinY");
	    recevent_particle_ecinZ = event
		    .getFloat("RECEVENT::particle.ecinZ");
	    recevent_particle_ecoutidx = event
		    .getInt("RECEVENT::particle.ecoutidx");
	    recevent_particle_ecoutX = event
		    .getFloat("RECEVENT::particle.ecoutX");
	    recevent_particle_ecoutY = event
		    .getFloat("RECEVENT::particle.ecoutY");
	    recevent_particle_ecoutZ = event
		    .getFloat("RECEVENT::particle.ecoutZ");
	    recevent_particle_mass = event.getFloat("RECEVENT::particle.mass");
	    recevent_particle_pcalidx = event
		    .getInt("RECEVENT::particle.pcalidx");
	    recevent_particle_pcalX = event
		    .getFloat("RECEVENT::particle.pcalX");
	    recevent_particle_pcalY = event
		    .getFloat("RECEVENT::particle.pcalY");
	    recevent_particle_pcalZ = event
		    .getFloat("RECEVENT::particle.pcalZ");
	    recevent_particle_pid = event.getInt("RECEVENT::particle.pid");
	    recevent_particle_px = event.getFloat("RECEVENT::particle.px");
	    recevent_particle_py = event.getFloat("RECEVENT::particle.py");
	    recevent_particle_pz = event.getFloat("RECEVENT::particle.pz");
	    recevent_particle_scidx = event.getInt("RECEVENT::particle.scidx");
	    recevent_particle_scpath = event
		    .getFloat("RECEVENT::particle.scpath");
	    recevent_particle_sctime = event
		    .getFloat("RECEVENT::particle.sctime");
	    recevent_particle_scX = event.getFloat("RECEVENT::particle.scX");
	    recevent_particle_scY = event.getFloat("RECEVENT::particle.scY");
	    recevent_particle_scZ = event.getFloat("RECEVENT::particle.scZ");
	    recevent_particle_status = event
		    .getByte("RECEVENT::particle.status");
	    recevent_particle_vx = event.getFloat("RECEVENT::particle.vx");
	    recevent_particle_vy = event.getFloat("RECEVENT::particle.vy");
	    recevent_particle_vz = event.getFloat("RECEVENT::particle.vz");
	} // RECEVENT::particle

    } // load

    @Override
    public void clear() {
	eventhb_detector_detector = null;
	eventhb_detector_energy = null;
	eventhb_detector_hX = null;
	eventhb_detector_hY = null;
	eventhb_detector_hZ = null;
	eventhb_detector_index = null;
	eventhb_detector_layer = null;
	eventhb_detector_path = null;
	eventhb_detector_pindex = null;
	eventhb_detector_sector = null;
	eventhb_detector_superlayer = null;
	eventhb_detector_time = null;
	eventhb_detector_X = null;
	eventhb_detector_Y = null;
	eventhb_detector_Z = null;
	eventhb_particle_beta = null;
	eventhb_particle_charge = null;
	eventhb_particle_chi2pid = null;
	eventhb_particle_mass = null;
	eventhb_particle_pid = null;
	eventhb_particle_px = null;
	eventhb_particle_py = null;
	eventhb_particle_pz = null;
	eventhb_particle_status = null;
	eventhb_particle_vx = null;
	eventhb_particle_vy = null;
	eventhb_particle_vz = null;
	recevent_particle_beta = null;
	recevent_particle_charge = null;
	recevent_particle_chi2ecin = null;
	recevent_particle_chi2ecout = null;
	recevent_particle_chi2pcal = null;
	recevent_particle_chi2sc = null;
	recevent_particle_ecinidx = null;
	recevent_particle_ecinX = null;
	recevent_particle_ecinY = null;
	recevent_particle_ecinZ = null;
	recevent_particle_ecoutidx = null;
	recevent_particle_ecoutX = null;
	recevent_particle_ecoutY = null;
	recevent_particle_ecoutZ = null;
	recevent_particle_mass = null;
	recevent_particle_pcalidx = null;
	recevent_particle_pcalX = null;
	recevent_particle_pcalY = null;
	recevent_particle_pcalZ = null;
	recevent_particle_pid = null;
	recevent_particle_px = null;
	recevent_particle_py = null;
	recevent_particle_pz = null;
	recevent_particle_scidx = null;
	recevent_particle_scpath = null;
	recevent_particle_sctime = null;
	recevent_particle_scX = null;
	recevent_particle_scY = null;
	recevent_particle_scZ = null;
	recevent_particle_status = null;
	recevent_particle_vx = null;
	recevent_particle_vy = null;
	recevent_particle_vz = null;
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
    }
}
