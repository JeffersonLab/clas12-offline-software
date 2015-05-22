package cnuphys.ced.event.data;

import java.util.List;
import java.util.Vector;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.util.VectorSupport;
import cnuphys.ced.clasio.ClasIoEventManager;

public class ECDataContainer extends ADataContainer {

    private static final String uvwStr[] = { "U", "V", "W" };

    public static final int EC_OPTION = 0;
    public static final int PCAL_OPTION = 1;

    /** ADC */
    public int[] ec_dgtz_ADC;

    /** hit number */
    public int[] ec_dgtz_hitn;

    /** sector number */
    public int[] ec_dgtz_sector;

    /** stack (1=inner, 2=outer) number */
    public int[] ec_dgtz_stack;

    /** strip number */
    public int[] ec_dgtz_strip;

    /** TDC */
    public int[] ec_dgtz_TDC;

    /** view (1=u, 2=v, 3=w) */
    public int[] ec_dgtz_view;

    /** Average X position in local reference system */
    public double[] ec_true_avgLx;

    /** Average Y position in local reference system */
    public double[] ec_true_avgLy;

    /** Average Z position in local reference system */
    public double[] ec_true_avgLz;

    /** Average time */
    public double[] ec_true_avgT;

    /** Average X position in global reference system */
    public double[] ec_true_avgX;

    /** Average Y position in global reference system */
    public double[] ec_true_avgY;

    /** Average Z position in global reference system */
    public double[] ec_true_avgZ;

    /** Hit1 Number */
    public int[] ec_true_hitn;

    /** ID of the mother of the first particle entering the sensitive volume */
    public int[] ec_true_mpid;

    /**
     * Track ID of the mother of the first particle entering the sensitive
     * volume
     */
    public int[] ec_true_mtid;

    /**
     * x component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] ec_true_mvx;

    /**
     * y component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] ec_true_mvy;

    /**
     * z component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] ec_true_mvz;

    /**
     * Track ID of the original track that generated the first particle entering
     * the sensitive volume
     */
    public int[] ec_true_otid;

    /** ID of the first particle entering the sensitive volume */
    public int[] ec_true_pid;

    /** x component of momentum of the particle entering the sensitive volume */
    public double[] ec_true_px;

    /** y component of momentum of the particle entering the sensitive volume */
    public double[] ec_true_py;

    /** z component of momentum of the particle entering the sensitive volume */
    public double[] ec_true_pz;

    /** Track ID of the first particle entering the sensitive volume */
    public int[] ec_true_tid;

    /** Total Energy Deposited */
    public double[] ec_true_totEdep;

    /** Energy of the track */
    public double[] ec_true_trackE;

    /**
     * x component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] ec_true_vx;

    /**
     * y component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] ec_true_vy;

    /**
     * z component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] ec_true_vz;

    /** superlayer */
    public double[] ecrec_clusters_chi2;

    /** x coordinate error */
    public double[] ecrec_clusters_dX;

    /** y coordinate error */
    public double[] ecrec_clusters_dY;

    /** z coordinate error */
    public double[] ecrec_clusters_dZ;

    /** superlayer */
    public double[] ecrec_clusters_energy;

    /** sector number */
    public int[] ecrec_clusters_sector;

    /** superlayer */
    public int[] ecrec_clusters_superlayer;

    /** superlayer */
    public double[] ecrec_clusters_time;

    /** width in U layer */
    public double[] ecrec_clusters_widthU;

    /** width in V layer */
    public double[] ecrec_clusters_widthV;

    /** width in W layer */
    public double[] ecrec_clusters_widthW;

    /** x coordinate */
    public double[] ecrec_clusters_X;

    /** y coordinate */
    public double[] ecrec_clusters_Y;

    /** z coordinate */
    public double[] ecrec_clusters_Z;

    /** sector number */
    public double[] ecrec_hits_energy;

    /** sector number */
    public int[] ecrec_hits_sector;

    /** sector number */
    public int[] ecrec_hits_strip;

    /** sector number */
    public int[] ecrec_hits_superlayer;

    /** sector number */
    public double[] ecrec_hits_time;

    /** sector number */
    public int[] ecrec_hits_view;

    /** Peak Energy */
    public double[] ecrec_peaks_energy;

    /** sector number (1..6) */
    public int[] ecrec_peaks_sector;

    /** PCAL=0, EC inner=1, EC outter=2 */
    public int[] ecrec_peaks_superlayer;

    /** Peak Time */
    public double[] ecrec_peaks_time;

    /** U=0,V=1,W=2 */
    public int[] ecrec_peaks_view;

    /** width of the peak */
    public double[] ecrec_peaks_width;

    /** strip end X coordinate */
    public double[] ecrec_peaks_Xe;

    /** strip origin X coordinate */
    public double[] ecrec_peaks_Xo;

    /** strip end Y coordinate */
    public double[] ecrec_peaks_Ye;

    /** strip origin Y coordinate */
    public double[] ecrec_peaks_Yo;

    /** strip end Z coordinate */
    public double[] ecrec_peaks_Ze;

    /** strip origin Z coordinate */
    public double[] ecrec_peaks_Zo;

    /** ADC */
    public int[] pcal_dgtz_ADC;

    /** hit number */
    public int[] pcal_dgtz_hitn;

    /** sector number */
    public int[] pcal_dgtz_sector;

    /** stack (1=inner, 2=outer) number */
    public int[] pcal_dgtz_stack;

    /** strip number */
    public int[] pcal_dgtz_strip;

    /** TDC */
    public int[] pcal_dgtz_TDC;

    /** view (1=u, 2=v, 3=w) */
    public int[] pcal_dgtz_view;

    /** Average X position in local reference system */
    public double[] pcal_true_avgLx;

    /** Average Y position in local reference system */
    public double[] pcal_true_avgLy;

    /** Average Z position in local reference system */
    public double[] pcal_true_avgLz;

    /** Average time */
    public double[] pcal_true_avgT;

    /** Average X position in global reference system */
    public double[] pcal_true_avgX;

    /** Average Y position in global reference system */
    public double[] pcal_true_avgY;

    /** Average Z position in global reference system */
    public double[] pcal_true_avgZ;

    /** Hit1 Number */
    public int[] pcal_true_hitn;

    /** ID of the mother of the first particle entering the sensitive volume */
    public int[] pcal_true_mpid;

    /**
     * Track ID of the mother of the first particle entering the sensitive
     * volume
     */
    public int[] pcal_true_mtid;

    /**
     * x component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] pcal_true_mvx;

    /**
     * y component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] pcal_true_mvy;

    /**
     * z component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] pcal_true_mvz;

    /**
     * Track ID of the original track that generated the first particle entering
     * the sensitive volume
     */
    public int[] pcal_true_otid;

    /** ID of the first particle entering the sensitive volume */
    public int[] pcal_true_pid;

    /** x component of momentum of the particle entering the sensitive volume */
    public double[] pcal_true_px;

    /** y component of momentum of the particle entering the sensitive volume */
    public double[] pcal_true_py;

    /** z component of momentum of the particle entering the sensitive volume */
    public double[] pcal_true_pz;

    /** Track ID of the first particle entering the sensitive volume */
    public int[] pcal_true_tid;

    /** Total Energy Deposited */
    public double[] pcal_true_totEdep;

    /** Energy of the track */
    public double[] pcal_true_trackE;

    /**
     * x component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] pcal_true_vx;

    /**
     * y component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] pcal_true_vy;

    /**
     * z component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] pcal_true_vz;

    public ECDataContainer(ClasIoEventManager eventManager) {
	super(eventManager);
    }

    @Override
    public int getHitCount(int option) {
	// option= 0 for ec, else pcal
	int hitCount = 0;

	if (option == EC_OPTION) {
	    hitCount = (ec_dgtz_sector == null) ? 0 : ec_dgtz_sector.length;
	} else {
	    hitCount = (pcal_dgtz_sector == null) ? 0 : pcal_dgtz_sector.length;
	}
	return hitCount;
    }

    @Override
    public void load(EvioDataEvent event) {
	if (event == null) {
	    return;
	}

	if (event.hasBank("EC::dgtz")) {
	    ec_dgtz_ADC = event.getInt("EC::dgtz.ADC");
	    ec_dgtz_hitn = event.getInt("EC::dgtz.hitn");
	    ec_dgtz_sector = event.getInt("EC::dgtz.sector");
	    ec_dgtz_stack = event.getInt("EC::dgtz.stack");
	    ec_dgtz_strip = event.getInt("EC::dgtz.strip");
	    ec_dgtz_TDC = event.getInt("EC::dgtz.TDC");
	    ec_dgtz_view = event.getInt("EC::dgtz.view");
	} // EC::dgtz

	if (event.hasBank("EC::true")) {
	    ec_true_avgLx = event.getDouble("EC::true.avgLx");
	    ec_true_avgLy = event.getDouble("EC::true.avgLy");
	    ec_true_avgLz = event.getDouble("EC::true.avgLz");
	    ec_true_avgT = event.getDouble("EC::true.avgT");
	    ec_true_avgX = event.getDouble("EC::true.avgX");
	    ec_true_avgY = event.getDouble("EC::true.avgY");
	    ec_true_avgZ = event.getDouble("EC::true.avgZ");
	    ec_true_hitn = event.getInt("EC::true.hitn");
	    ec_true_mpid = event.getInt("EC::true.mpid");
	    ec_true_mtid = event.getInt("EC::true.mtid");
	    ec_true_mvx = event.getDouble("EC::true.mvx");
	    ec_true_mvy = event.getDouble("EC::true.mvy");
	    ec_true_mvz = event.getDouble("EC::true.mvz");
	    ec_true_otid = event.getInt("EC::true.otid");
	    ec_true_pid = event.getInt("EC::true.pid");
	    ec_true_px = event.getDouble("EC::true.px");
	    ec_true_py = event.getDouble("EC::true.py");
	    ec_true_pz = event.getDouble("EC::true.pz");
	    ec_true_tid = event.getInt("EC::true.tid");
	    ec_true_totEdep = event.getDouble("EC::true.totEdep");
	    ec_true_trackE = event.getDouble("EC::true.trackE");
	    ec_true_vx = event.getDouble("EC::true.vx");
	    ec_true_vy = event.getDouble("EC::true.vy");
	    ec_true_vz = event.getDouble("EC::true.vz");
	} // EC::true

	if (event.hasBank("ECRec::clusters")) {
	    ecrec_clusters_chi2 = event.getDouble("ECRec::clusters.chi2");
	    ecrec_clusters_dX = event.getDouble("ECRec::clusters.dX");
	    ecrec_clusters_dY = event.getDouble("ECRec::clusters.dY");
	    ecrec_clusters_dZ = event.getDouble("ECRec::clusters.dZ");
	    ecrec_clusters_energy = event.getDouble("ECRec::clusters.energy");
	    ecrec_clusters_sector = event.getInt("ECRec::clusters.sector");
	    ecrec_clusters_superlayer = event
		    .getInt("ECRec::clusters.superlayer");
	    ecrec_clusters_time = event.getDouble("ECRec::clusters.time");
	    ecrec_clusters_widthU = event.getDouble("ECRec::clusters.widthU");
	    ecrec_clusters_widthV = event.getDouble("ECRec::clusters.widthV");
	    ecrec_clusters_widthW = event.getDouble("ECRec::clusters.widthW");
	    ecrec_clusters_X = event.getDouble("ECRec::clusters.X");
	    ecrec_clusters_Y = event.getDouble("ECRec::clusters.Y");
	    ecrec_clusters_Z = event.getDouble("ECRec::clusters.Z");
	} // ECRec::clusters

	if (event.hasBank("ECRec::hits")) {
	    ecrec_hits_energy = event.getDouble("ECRec::hits.energy");
	    ecrec_hits_sector = event.getInt("ECRec::hits.sector");
	    ecrec_hits_strip = event.getInt("ECRec::hits.strip");
	    ecrec_hits_superlayer = event.getInt("ECRec::hits.superlayer");
	    ecrec_hits_time = event.getDouble("ECRec::hits.time");
	    ecrec_hits_view = event.getInt("ECRec::hits.view");
	} // ECRec::hits

	if (event.hasBank("ECRec::peaks")) {
	    ecrec_peaks_energy = event.getDouble("ECRec::peaks.energy");
	    ecrec_peaks_sector = event.getInt("ECRec::peaks.sector");
	    ecrec_peaks_superlayer = event.getInt("ECRec::peaks.superlayer");
	    ecrec_peaks_time = event.getDouble("ECRec::peaks.time");
	    ecrec_peaks_view = event.getInt("ECRec::peaks.view");
	    ecrec_peaks_width = event.getDouble("ECRec::peaks.width");
	    ecrec_peaks_Xe = event.getDouble("ECRec::peaks.Xe");
	    ecrec_peaks_Xo = event.getDouble("ECRec::peaks.Xo");
	    ecrec_peaks_Ye = event.getDouble("ECRec::peaks.Ye");
	    ecrec_peaks_Yo = event.getDouble("ECRec::peaks.Yo");
	    ecrec_peaks_Ze = event.getDouble("ECRec::peaks.Ze");
	    ecrec_peaks_Zo = event.getDouble("ECRec::peaks.Zo");
	} // ECRec::peaks

	if (event.hasBank("PCAL::dgtz")) {
	    pcal_dgtz_ADC = event.getInt("PCAL::dgtz.ADC");
	    pcal_dgtz_hitn = event.getInt("PCAL::dgtz.hitn");
	    pcal_dgtz_sector = event.getInt("PCAL::dgtz.sector");
	    pcal_dgtz_stack = event.getInt("PCAL::dgtz.stack");
	    pcal_dgtz_strip = event.getInt("PCAL::dgtz.strip");
	    pcal_dgtz_TDC = event.getInt("PCAL::dgtz.TDC");
	    pcal_dgtz_view = event.getInt("PCAL::dgtz.view");
	} // PCAL::dgtz

	if (event.hasBank("PCAL::true")) {
	    pcal_true_avgLx = event.getDouble("PCAL::true.avgLx");
	    pcal_true_avgLy = event.getDouble("PCAL::true.avgLy");
	    pcal_true_avgLz = event.getDouble("PCAL::true.avgLz");
	    pcal_true_avgT = event.getDouble("PCAL::true.avgT");
	    pcal_true_avgX = event.getDouble("PCAL::true.avgX");
	    pcal_true_avgY = event.getDouble("PCAL::true.avgY");
	    pcal_true_avgZ = event.getDouble("PCAL::true.avgZ");
	    pcal_true_hitn = event.getInt("PCAL::true.hitn");
	    pcal_true_mpid = event.getInt("PCAL::true.mpid");
	    pcal_true_mtid = event.getInt("PCAL::true.mtid");
	    pcal_true_mvx = event.getDouble("PCAL::true.mvx");
	    pcal_true_mvy = event.getDouble("PCAL::true.mvy");
	    pcal_true_mvz = event.getDouble("PCAL::true.mvz");
	    pcal_true_otid = event.getInt("PCAL::true.otid");
	    pcal_true_pid = event.getInt("PCAL::true.pid");
	    pcal_true_px = event.getDouble("PCAL::true.px");
	    pcal_true_py = event.getDouble("PCAL::true.py");
	    pcal_true_pz = event.getDouble("PCAL::true.pz");
	    pcal_true_tid = event.getInt("PCAL::true.tid");
	    pcal_true_totEdep = event.getDouble("PCAL::true.totEdep");
	    pcal_true_trackE = event.getDouble("PCAL::true.trackE");
	    pcal_true_vx = event.getDouble("PCAL::true.vx");
	    pcal_true_vy = event.getDouble("PCAL::true.vy");
	    pcal_true_vz = event.getDouble("PCAL::true.vz");
	} // PCAL::true

    } // load

    @Override
    public void clear() {
	ec_dgtz_ADC = null;
	ec_dgtz_hitn = null;
	ec_dgtz_sector = null;
	ec_dgtz_stack = null;
	ec_dgtz_strip = null;
	ec_dgtz_TDC = null;
	ec_dgtz_view = null;
	ec_true_avgLx = null;
	ec_true_avgLy = null;
	ec_true_avgLz = null;
	ec_true_avgT = null;
	ec_true_avgX = null;
	ec_true_avgY = null;
	ec_true_avgZ = null;
	ec_true_hitn = null;
	ec_true_mpid = null;
	ec_true_mtid = null;
	ec_true_mvx = null;
	ec_true_mvy = null;
	ec_true_mvz = null;
	ec_true_otid = null;
	ec_true_pid = null;
	ec_true_px = null;
	ec_true_py = null;
	ec_true_pz = null;
	ec_true_tid = null;
	ec_true_totEdep = null;
	ec_true_trackE = null;
	ec_true_vx = null;
	ec_true_vy = null;
	ec_true_vz = null;
	ecrec_clusters_chi2 = null;
	ecrec_clusters_dX = null;
	ecrec_clusters_dY = null;
	ecrec_clusters_dZ = null;
	ecrec_clusters_energy = null;
	ecrec_clusters_sector = null;
	ecrec_clusters_superlayer = null;
	ecrec_clusters_time = null;
	ecrec_clusters_widthU = null;
	ecrec_clusters_widthV = null;
	ecrec_clusters_widthW = null;
	ecrec_clusters_X = null;
	ecrec_clusters_Y = null;
	ecrec_clusters_Z = null;
	ecrec_hits_energy = null;
	ecrec_hits_sector = null;
	ecrec_hits_strip = null;
	ecrec_hits_superlayer = null;
	ecrec_hits_time = null;
	ecrec_hits_view = null;
	ecrec_peaks_energy = null;
	ecrec_peaks_sector = null;
	ecrec_peaks_superlayer = null;
	ecrec_peaks_time = null;
	ecrec_peaks_view = null;
	ecrec_peaks_width = null;
	ecrec_peaks_Xe = null;
	ecrec_peaks_Xo = null;
	ecrec_peaks_Ye = null;
	ecrec_peaks_Yo = null;
	ecrec_peaks_Ze = null;
	ecrec_peaks_Zo = null;
	pcal_dgtz_ADC = null;
	pcal_dgtz_hitn = null;
	pcal_dgtz_sector = null;
	pcal_dgtz_stack = null;
	pcal_dgtz_strip = null;
	pcal_dgtz_TDC = null;
	pcal_dgtz_view = null;
	pcal_true_avgLx = null;
	pcal_true_avgLy = null;
	pcal_true_avgLz = null;
	pcal_true_avgT = null;
	pcal_true_avgX = null;
	pcal_true_avgY = null;
	pcal_true_avgZ = null;
	pcal_true_hitn = null;
	pcal_true_mpid = null;
	pcal_true_mtid = null;
	pcal_true_mvx = null;
	pcal_true_mvy = null;
	pcal_true_mvz = null;
	pcal_true_otid = null;
	pcal_true_pid = null;
	pcal_true_px = null;
	pcal_true_py = null;
	pcal_true_pz = null;
	pcal_true_tid = null;
	pcal_true_totEdep = null;
	pcal_true_trackE = null;
	pcal_true_vx = null;
	pcal_true_vy = null;
	pcal_true_vz = null;
    } // clear

    @Override
    public void addPreliminaryFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {

	if (option == EC_OPTION) {

	    feedbackStrings.add("==== " + uvwStr[ec_dgtz_view[hitIndex] - 1]
		    + " strip " + ec_dgtz_strip[hitIndex] + " ====");

	    addXYZFeedback(hitIndex, ec_true_avgX, ec_true_avgY, ec_true_avgZ,
		    ec_true_avgLx, ec_true_avgLy, ec_true_avgLz,
		    feedbackStrings);
	} else { // pcal
	    feedbackStrings.add("==== " + uvwStr[pcal_dgtz_view[hitIndex] - 1]
		    + " strip " + pcal_dgtz_strip[hitIndex] + " ====");

	    addXYZFeedback(hitIndex, pcal_true_avgX, pcal_true_avgY,
		    pcal_true_avgZ, pcal_true_avgLx, pcal_true_avgLy,
		    pcal_true_avgLz, feedbackStrings);
	}
    }

    private void addXYZFeedback(int hitIndex, double x[], double y[],
	    double z[], double lx[], double ly[], double lz[],
	    List<String> feedbackStrings) {

	if (hitIndex < 0) {
	    return;
	}

	if ((x != null) && (y != null) && (z != null) && (hitIndex < x.length)) {
	    double v[] = new double[3];
	    // to cm
	    v[0] = x[hitIndex] / 10;
	    v[1] = y[hitIndex] / 10;
	    v[2] = z[hitIndex] / 10;
	    feedbackStrings.add(trueColor + "hit global xyz "
		    + VectorSupport.toString(v, 2));
	}

	if ((lx != null) && (ly != null) && (lz != null)
		&& (hitIndex < lx.length)) {
	    double v[] = new double[3];
	    // to cm
	    v[0] = lx[hitIndex] / 10;
	    v[1] = ly[hitIndex] / 10;
	    v[2] = lz[hitIndex] / 10;
	    feedbackStrings.add(trueColor + "hit local xyz "
		    + VectorSupport.toString(v, 2));
	}
    }

    @Override
    public void addTrueFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
    }

    @Override
    public void addDgtzFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {

	if (option == EC_OPTION) {
	    String hitStr = safeString(ec_dgtz_hitn, hitIndex);
	    String adcStr = safeString(ec_dgtz_ADC, hitIndex);
	    String tdcStr = safeString(ec_dgtz_TDC, hitIndex);

	    feedbackStrings.add(dgtxColor + "adc " + adcStr + "  tdc " + tdcStr
		    + " ns" + "  hit " + hitStr);
	} else {
	    String hitStr = safeString(pcal_dgtz_hitn, hitIndex);
	    String adcStr = safeString(pcal_dgtz_ADC, hitIndex);
	    String tdcStr = safeString(pcal_dgtz_TDC, hitIndex);

	    feedbackStrings.add(dgtxColor + "adc " + adcStr + "  tdc " + tdcStr
		    + " ns" + "  hit " + hitStr);
	}
    }

    @Override
    public void addFinalFeedback(int option, List<String> feedbackStrings) {
    }

    @Override
    public void addReconstructedFeedback(int option,
	    List<String> feedbackStrings) {
    }

    /**
     * Get the index of the dc hit
     * 
     * @param sect
     *            the 1-based sector
     * @param stack
     *            the 1-based stack (inner = 1, outer = 2) index
     * @param view
     *            the 1-based strip type (u, v, w) = (1, 2, 3)
     * @param strip
     *            the 1-based strip
     * @param option
     *            either EC_OPTION or PCAL_OPTION
     * @return the index of a hit with these parameters, or -1 if not found
     */
    public Vector<HitRecord> getMatchingHits(int sect, int stack, int view,
	    int strip, int option) {

	Vector<HitRecord> hits = null;
	for (int i = 0; i < getHitCount(option); i++) {

	    if (option == EC_OPTION) {
		if ((sect == ec_dgtz_sector[i]) && (stack == ec_dgtz_stack[i])
			&& (view == ec_dgtz_view[i])
			&& (strip == ec_dgtz_strip[i])) {
		    if (hits == null) {
			hits = new Vector<HitRecord>();
		    }
		    hits.add(new HitRecord(this, ec_true_avgX, ec_true_avgY,
			    ec_true_avgZ, i, sect, stack, view, strip));
		}
	    } else { // pcal
		if ((sect == pcal_dgtz_sector[i])
			&& (stack == pcal_dgtz_stack[i])
			&& (view == pcal_dgtz_view[i])
			&& (strip == pcal_dgtz_strip[i])) {
		    if (hits == null) {
			hits = new Vector<HitRecord>();
		    }
		    hits.add(new HitRecord(this, pcal_true_avgX,
			    pcal_true_avgY, pcal_true_avgZ, i, sect, stack,
			    view, strip));
		}
	    }
	}
	return hits;
    }

    @Override
    public void finalEventPrep(EvioDataEvent event) {
	extractUniqueLundIds(ec_true_pid);
	extractUniqueLundIds(pcal_true_pid);
    }

}
