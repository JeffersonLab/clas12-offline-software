package cnuphys.ced.event.data;

import java.util.List;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;

public class CNDDataContainer extends ADataContainer {

	/** ADC Backward (u-turn geometry, same paddle as hit) */
	public int[] cnd_dgtz_ADCB;

	/** ADC Forward (u-turn geometry, lightguide paddle) */
	public int[] cnd_dgtz_ADCF;

	/** ADC Left */
	public int[] cnd_dgtz_ADCL;

	/** ADC Right */
	public int[] cnd_dgtz_ADCR;

	/** hit number */
	public int[] cnd_dgtz_hitn;

	/** layer */
	public int[] cnd_dgtz_layer;

	/** paddle number */
	public int[] cnd_dgtz_paddle;

	/** TDC Backward (u-turn geometry) */
	public int[] cnd_dgtz_TDCB;

	/** TDC Forward (u-turn geometry) */
	public int[] cnd_dgtz_TDCF;

	/** TDC Left */
	public int[] cnd_dgtz_TDCL;

	/** TDC Right */
	public int[] cnd_dgtz_TDCR;

	/** Average X position in local reference system */
	public double[] cnd_true_avgLx;

	/** Average Y position in local reference system */
	public double[] cnd_true_avgLy;

	/** Average Z position in local reference system */
	public double[] cnd_true_avgLz;

	/** Average time */
	public double[] cnd_true_avgT;

	/** Average X position in global reference system */
	public double[] cnd_true_avgX;

	/** Average Y position in global reference system */
	public double[] cnd_true_avgY;

	/** Average Z position in global reference system */
	public double[] cnd_true_avgZ;

	/** Hit1 Number */
	public int[] cnd_true_hitn;

	/** ID of the mother of the first particle entering the sensitive volume */
	public int[] cnd_true_mpid;

	/**
	 * Track ID of the mother of the first particle entering the sensitive
	 * volume
	 */
	public int[] cnd_true_mtid;

	/**
	 * x component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] cnd_true_mvx;

	/**
	 * y component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] cnd_true_mvy;

	/**
	 * z component of primary vertex of the mother of the particle entering the
	 * sensitive volume
	 */
	public double[] cnd_true_mvz;

	/**
	 * Track ID of the original track that generated the first particle entering
	 * the sensitive volume
	 */
	public int[] cnd_true_otid;

	/** ID of the first particle entering the sensitive volume */
	public int[] cnd_true_pid;

	/** x component of momentum of the particle entering the sensitive volume */
	public double[] cnd_true_px;

	/** y component of momentum of the particle entering the sensitive volume */
	public double[] cnd_true_py;

	/** z component of momentum of the particle entering the sensitive volume */
	public double[] cnd_true_pz;

	/** Track ID of the first particle entering the sensitive volume */
	public int[] cnd_true_tid;

	/** Total Energy Deposited */
	public double[] cnd_true_totEdep;

	/** Energy of the track */
	public double[] cnd_true_trackE;

	/**
	 * x component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] cnd_true_vx;

	/**
	 * y component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] cnd_true_vy;

	/**
	 * z component of primary vertex of the particle entering the sensitive
	 * volume
	 */
	public double[] cnd_true_vz;

	/** Reconstructed energy deposit */
	public double[] cndrec_hits_E;

	/**
	 * Index of reconstructed direct hit in the individual TDC/ADC signal array
	 * (for real data: CND Bank, starting at 0)
	 */
	public int[] cndrec_hits_indexD;

	/**
	 * Index of reconstructed indirect hit in the individual TDC/ADC signal
	 * array (for real data: CND Bank, starting at 0)
	 */
	public int[] cndrec_hits_indexI;

	/** layer number */
	public int[] cndrec_hits_layer;

	/** MC energy deposited in hit */
	public double[] cndrec_hits_MCE;

	/** MC flag. 1 (-1): correct (incorrect) matching of hits, 0: real data */
	public int[] cndrec_hits_MCflag;

	/** MC time of hit */
	public double[] cndrec_hits_MCt;

	/** MC x-position of hit (w.r.t. Central Detector center) */
	public double[] cndrec_hits_MCx;

	/** MC y-position of hit (w.r.t. Central Detector center) */
	public double[] cndrec_hits_MCy;

	/** MC z-position of hit (w.r.t. Central Detector center) */
	public double[] cndrec_hits_MCz;

	/** paddle number */
	public int[] cndrec_hits_paddle;

	/** Azimuthal angle (around beam axis) of the hit position */
	public double[] cndrec_hits_phi;

	/** Radial distance from beam axis to hit position */
	public double[] cndrec_hits_R;

	/** Time of flight to CND hit-position */
	public double[] cndrec_hits_Tof;

	/** X co-ordinate of the hit position, w.r.t. Central Detector center */
	public double[] cndrec_hits_X;

	/** Y co-ordinate of the hit position, w.r.t. Central Detector center */
	public double[] cndrec_hits_Y;

	/** Z co-ordinate of the hit position, w.r.t. Central Detector center */
	public double[] cndrec_hits_Z;

	public CNDDataContainer(ClasIoEventManager eventManager) {
		super(eventManager);
	}

	@Override
	public void clear() {
		cnd_dgtz_ADCB = null;
		cnd_dgtz_ADCF = null;
		cnd_dgtz_ADCL = null;
		cnd_dgtz_ADCR = null;
		cnd_dgtz_hitn = null;
		cnd_dgtz_layer = null;
		cnd_dgtz_paddle = null;
		cnd_dgtz_TDCB = null;
		cnd_dgtz_TDCF = null;
		cnd_dgtz_TDCL = null;
		cnd_dgtz_TDCR = null;
		cnd_true_avgLx = null;
		cnd_true_avgLy = null;
		cnd_true_avgLz = null;
		cnd_true_avgT = null;
		cnd_true_avgX = null;
		cnd_true_avgY = null;
		cnd_true_avgZ = null;
		cnd_true_hitn = null;
		cnd_true_mpid = null;
		cnd_true_mtid = null;
		cnd_true_mvx = null;
		cnd_true_mvy = null;
		cnd_true_mvz = null;
		cnd_true_otid = null;
		cnd_true_pid = null;
		cnd_true_px = null;
		cnd_true_py = null;
		cnd_true_pz = null;
		cnd_true_tid = null;
		cnd_true_totEdep = null;
		cnd_true_trackE = null;
		cnd_true_vx = null;
		cnd_true_vy = null;
		cnd_true_vz = null;
		cndrec_hits_E = null;
		cndrec_hits_indexD = null;
		cndrec_hits_indexI = null;
		cndrec_hits_layer = null;
		cndrec_hits_MCE = null;
		cndrec_hits_MCflag = null;
		cndrec_hits_MCt = null;
		cndrec_hits_MCx = null;
		cndrec_hits_MCy = null;
		cndrec_hits_MCz = null;
		cndrec_hits_paddle = null;
		cndrec_hits_phi = null;
		cndrec_hits_R = null;
		cndrec_hits_Tof = null;
		cndrec_hits_X = null;
		cndrec_hits_Y = null;
		cndrec_hits_Z = null;
	}

	@Override
	public void load(EvioDataEvent event) {
		if (event == null) {
			return;
		}

		if (event.hasBank("CND::dgtz")) {
			cnd_dgtz_ADCB = event.getInt("CND::dgtz.ADCB");
			cnd_dgtz_ADCF = event.getInt("CND::dgtz.ADCF");
			cnd_dgtz_ADCL = event.getInt("CND::dgtz.ADCL");
			cnd_dgtz_ADCR = event.getInt("CND::dgtz.ADCR");
			cnd_dgtz_hitn = event.getInt("CND::dgtz.hitn");
			cnd_dgtz_layer = event.getInt("CND::dgtz.layer");
			cnd_dgtz_paddle = event.getInt("CND::dgtz.paddle");
			cnd_dgtz_TDCB = event.getInt("CND::dgtz.TDCB");
			cnd_dgtz_TDCF = event.getInt("CND::dgtz.TDCF");
			cnd_dgtz_TDCL = event.getInt("CND::dgtz.TDCL");
			cnd_dgtz_TDCR = event.getInt("CND::dgtz.TDCR");
		} // CND::dgtz

		if (event.hasBank("CND::true")) {
			cnd_true_avgLx = event.getDouble("CND::true.avgLx");
			cnd_true_avgLy = event.getDouble("CND::true.avgLy");
			cnd_true_avgLz = event.getDouble("CND::true.avgLz");
			cnd_true_avgT = event.getDouble("CND::true.avgT");
			cnd_true_avgX = event.getDouble("CND::true.avgX");
			cnd_true_avgY = event.getDouble("CND::true.avgY");
			cnd_true_avgZ = event.getDouble("CND::true.avgZ");
			cnd_true_hitn = event.getInt("CND::true.hitn");
			cnd_true_mpid = event.getInt("CND::true.mpid");
			cnd_true_mtid = event.getInt("CND::true.mtid");
			cnd_true_mvx = event.getDouble("CND::true.mvx");
			cnd_true_mvy = event.getDouble("CND::true.mvy");
			cnd_true_mvz = event.getDouble("CND::true.mvz");
			cnd_true_otid = event.getInt("CND::true.otid");
			cnd_true_pid = event.getInt("CND::true.pid");
			cnd_true_px = event.getDouble("CND::true.px");
			cnd_true_py = event.getDouble("CND::true.py");
			cnd_true_pz = event.getDouble("CND::true.pz");
			cnd_true_tid = event.getInt("CND::true.tid");
			cnd_true_totEdep = event.getDouble("CND::true.totEdep");
			cnd_true_trackE = event.getDouble("CND::true.trackE");
			cnd_true_vx = event.getDouble("CND::true.vx");
			cnd_true_vy = event.getDouble("CND::true.vy");
			cnd_true_vz = event.getDouble("CND::true.vz");
		} // CND::true

		if (event.hasBank("CNDRec::hits")) {
			cndrec_hits_E = event.getDouble("CNDRec::hits.E");
			cndrec_hits_indexD = event.getInt("CNDRec::hits.indexD");
			cndrec_hits_indexI = event.getInt("CNDRec::hits.indexI");
			cndrec_hits_layer = event.getInt("CNDRec::hits.layer");
			cndrec_hits_MCE = event.getDouble("CNDRec::hits.MCE");
			cndrec_hits_MCflag = event.getInt("CNDRec::hits.MCflag");
			cndrec_hits_MCt = event.getDouble("CNDRec::hits.MCt");
			cndrec_hits_MCx = event.getDouble("CNDRec::hits.MCx");
			cndrec_hits_MCy = event.getDouble("CNDRec::hits.MCy");
			cndrec_hits_MCz = event.getDouble("CNDRec::hits.MCz");
			cndrec_hits_paddle = event.getInt("CNDRec::hits.paddle");
			cndrec_hits_phi = event.getDouble("CNDRec::hits.phi");
			cndrec_hits_R = event.getDouble("CNDRec::hits.R");
			cndrec_hits_Tof = event.getDouble("CNDRec::hits.Tof");
			cndrec_hits_X = event.getDouble("CNDRec::hits.X");
			cndrec_hits_Y = event.getDouble("CNDRec::hits.Y");
			cndrec_hits_Z = event.getDouble("CNDRec::hits.Z");
		} // CNDRec::hits
	}

	@Override
	public int getHitCount(int option) {
		int hitCount = (cnd_dgtz_paddle == null) ? 0 : cnd_dgtz_paddle.length;
		return hitCount;
	}

	@Override
	public void finalEventPrep(EvioDataEvent event) {
		extractUniqueLundIds(cnd_true_pid);
	}

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

}
