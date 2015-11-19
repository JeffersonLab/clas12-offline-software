package cnuphys.ced.event.data;

import java.util.List;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;

/**
 * Micromegas data
 * 
 * @author heddle
 *
 */
public class BMTContainer extends ADataContainer {

    /** Edep on the strip */
    public double[] bmt_dgtz_Edep;

    /** Hit Number */
    public int[] bmt_dgtz_hitn;

    /** Layer Number */
    public int[] bmt_dgtz_layer;

    /** Sector Number */
    public int[] bmt_dgtz_sector;

    /** Strip Number */
    public int[] bmt_dgtz_strip;

    /** Average X position in local reference system */
    public double[] bmt_true_avgLx;

    /** Average Y position in local reference system */
    public double[] bmt_true_avgLy;

    /** Average Z position in local reference system */
    public double[] bmt_true_avgLz;

    /** Average time */
    public double[] bmt_true_avgT;

    /** Average X position in global reference system */
    public double[] bmt_true_avgX;

    /** Average Y position in global reference system */
    public double[] bmt_true_avgY;

    /** Average Z position in global reference system */
    public double[] bmt_true_avgZ;

    /** Hit1 Number */
    public int[] bmt_true_hitn;

    /** ID of the mother of the first particle entering the sensitive volume */
    public int[] bmt_true_mpid;

    /**
     * Track ID of the mother of the first particle entering the sensitive
     * volume
     */
    public int[] bmt_true_mtid;

    /**
     * x component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] bmt_true_mvx;

    /**
     * y component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] bmt_true_mvy;

    /**
     * z component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] bmt_true_mvz;

    /**
     * Track ID of the original track that generated the first particle entering
     * the sensitive volume
     */
    public int[] bmt_true_otid;

    /** ID of the first particle entering the sensitive volume */
    public int[] bmt_true_pid;

    /** x component of momentum of the particle entering the sensitive volume */
    public double[] bmt_true_px;

    /** y component of momentum of the particle entering the sensitive volume */
    public double[] bmt_true_py;

    /** z component of momentum of the particle entering the sensitive volume */
    public double[] bmt_true_pz;

    /** Track ID of the first particle entering the sensitive volume */
    public int[] bmt_true_tid;

    /** Total Energy Deposited */
    public double[] bmt_true_totEdep;

    /** Energy of the track */
    public double[] bmt_true_trackE;

    /**
     * x component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] bmt_true_vx;

    /**
     * y component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] bmt_true_vy;

    /**
     * z component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] bmt_true_vz;

    /** centroid strip number */
    public double[] bmtrec_clusters_centroid;

    /** cluster total energy */
    public double[] bmtrec_clusters_ETot;

    /** Index of hit 1 in cluster */
    public int[] bmtrec_clusters_Hit1_ID;

    /** Index of hit 2 in cluster */
    public int[] bmtrec_clusters_Hit2_ID;

    /** Index of hit 3 in cluster */
    public int[] bmtrec_clusters_Hit3_ID;

    /** Index of hit 4 in cluster */
    public int[] bmtrec_clusters_Hit4_ID;

    /** Index of hit 5 in cluster */
    public int[] bmtrec_clusters_Hit5_ID;

    /** ID */
    public int[] bmtrec_clusters_ID;

    /** layer */
    public int[] bmtrec_clusters_layer;

    /** sector */
    public int[] bmtrec_clusters_sector;

    /** energy of the seed */
    public double[] bmtrec_clusters_seedE;

    /** seed strip */
    public int[] bmtrec_clusters_seedStrip;

    /** cluster size */
    public int[] bmtrec_clusters_size;

    /** ID of the bottom layer cluster in the Cross */
    public int[] bmtrec_crosses_Cluster1_ID;

    /** ID of the top layer cluster in the Cross */
    public int[] bmtrec_crosses_Cluster2_ID;

    /** BMT cross x-coordinate error */
    public double[] bmtrec_crosses_err_x;

    /** BMT cross y-coordinate error */
    public double[] bmtrec_crosses_err_y;

    /** BMT cross z-coordinate error */
    public double[] bmtrec_crosses_err_z;

    /** ID */
    public int[] bmtrec_crosses_ID;

    /** region */
    public int[] bmtrec_crosses_region;

    /** sector */
    public int[] bmtrec_crosses_sector;

    /** BMT cross x-direction (track unit tangent vector at the cross) */
    public double[] bmtrec_crosses_ux;

    /** BMT cross y-direction (track unit tangent vector at the cross) */
    public double[] bmtrec_crosses_uy;

    /** BMT cross z-direction (track unit tangent vector at the cross) */
    public double[] bmtrec_crosses_uz;

    /** BMT cross x-coordinate */
    public double[] bmtrec_crosses_x;

    /** BMT cross y-coordinate */
    public double[] bmtrec_crosses_y;

    /** BMT cross z-coordinate */
    public double[] bmtrec_crosses_z;

    /** associated cluster ID */
    public int[] bmtrec_hits_clusterID;

    /** fitted hit residual */
    public double[] bmtrec_hits_fitResidual;

    /** ID */
    public int[] bmtrec_hits_ID;

    /** hit layer */
    public int[] bmtrec_hits_layer;

    /** hit sector */
    public int[] bmtrec_hits_sector;

    /** hit strip */
    public int[] bmtrec_hits_strip;

    /** tracking status */
    public int[] bmtrec_hits_trkingStat;

    public BMTContainer(ClasIoEventManager eventManager) {
	super(eventManager);
	// TODO Auto-generated constructor stub
    }

    @Override
    public int getHitCount(int option) {
	int hitCount = (bmt_dgtz_sector == null) ? 0 : bmt_dgtz_sector.length;
	return hitCount;
    }

    @Override
    public void load(EvioDataEvent event) {
	if (event == null) {
	    return;
	}

	if (event.hasBank("BMT::dgtz")) {
	    bmt_dgtz_Edep = event.getDouble("BMT::dgtz.Edep");
	    bmt_dgtz_hitn = event.getInt("BMT::dgtz.hitn");
	    bmt_dgtz_layer = event.getInt("BMT::dgtz.layer");
	    bmt_dgtz_sector = event.getInt("BMT::dgtz.sector");
	    bmt_dgtz_strip = event.getInt("BMT::dgtz.strip");
	} // BMT::dgtz

	if (event.hasBank("BMT::true")) {
	    bmt_true_avgLx = event.getDouble("BMT::true.avgLx");
	    bmt_true_avgLy = event.getDouble("BMT::true.avgLy");
	    bmt_true_avgLz = event.getDouble("BMT::true.avgLz");
	    bmt_true_avgT = event.getDouble("BMT::true.avgT");
	    bmt_true_avgX = event.getDouble("BMT::true.avgX");
	    bmt_true_avgY = event.getDouble("BMT::true.avgY");
	    bmt_true_avgZ = event.getDouble("BMT::true.avgZ");
	    bmt_true_hitn = event.getInt("BMT::true.hitn");
	    bmt_true_mpid = event.getInt("BMT::true.mpid");
	    bmt_true_mtid = event.getInt("BMT::true.mtid");
	    bmt_true_mvx = event.getDouble("BMT::true.mvx");
	    bmt_true_mvy = event.getDouble("BMT::true.mvy");
	    bmt_true_mvz = event.getDouble("BMT::true.mvz");
	    bmt_true_otid = event.getInt("BMT::true.otid");
	    bmt_true_pid = event.getInt("BMT::true.pid");
	    bmt_true_px = event.getDouble("BMT::true.px");
	    bmt_true_py = event.getDouble("BMT::true.py");
	    bmt_true_pz = event.getDouble("BMT::true.pz");
	    bmt_true_tid = event.getInt("BMT::true.tid");
	    bmt_true_totEdep = event.getDouble("BMT::true.totEdep");
	    bmt_true_trackE = event.getDouble("BMT::true.trackE");
	    bmt_true_vx = event.getDouble("BMT::true.vx");
	    bmt_true_vy = event.getDouble("BMT::true.vy");
	    bmt_true_vz = event.getDouble("BMT::true.vz");
	} // BMT::true

	if (event.hasBank("BMTRec::Clusters")) {
	    bmtrec_clusters_centroid = event
		    .getDouble("BMTRec::Clusters.centroid");
	    bmtrec_clusters_ETot = event.getDouble("BMTRec::Clusters.ETot");
	    bmtrec_clusters_Hit1_ID = event.getInt("BMTRec::Clusters.Hit1_ID");
	    bmtrec_clusters_Hit2_ID = event.getInt("BMTRec::Clusters.Hit2_ID");
	    bmtrec_clusters_Hit3_ID = event.getInt("BMTRec::Clusters.Hit3_ID");
	    bmtrec_clusters_Hit4_ID = event.getInt("BMTRec::Clusters.Hit4_ID");
	    bmtrec_clusters_Hit5_ID = event.getInt("BMTRec::Clusters.Hit5_ID");
	    bmtrec_clusters_ID = event.getInt("BMTRec::Clusters.ID");
	    bmtrec_clusters_layer = event.getInt("BMTRec::Clusters.layer");
	    bmtrec_clusters_sector = event.getInt("BMTRec::Clusters.sector");
	    bmtrec_clusters_seedE = event.getDouble("BMTRec::Clusters.seedE");
	    bmtrec_clusters_seedStrip = event
		    .getInt("BMTRec::Clusters.seedStrip");
	    bmtrec_clusters_size = event.getInt("BMTRec::Clusters.size");
	} // BMTRec::Clusters

	if (event.hasBank("BMTRec::Crosses")) {
	    bmtrec_crosses_Cluster1_ID = event
		    .getInt("BMTRec::Crosses.Cluster1_ID");
	    bmtrec_crosses_Cluster2_ID = event
		    .getInt("BMTRec::Crosses.Cluster2_ID");
	    bmtrec_crosses_err_x = event.getDouble("BMTRec::Crosses.err_x");
	    bmtrec_crosses_err_y = event.getDouble("BMTRec::Crosses.err_y");
	    bmtrec_crosses_err_z = event.getDouble("BMTRec::Crosses.err_z");
	    bmtrec_crosses_ID = event.getInt("BMTRec::Crosses.ID");
	    bmtrec_crosses_region = event.getInt("BMTRec::Crosses.region");
	    bmtrec_crosses_sector = event.getInt("BMTRec::Crosses.sector");
	    bmtrec_crosses_ux = event.getDouble("BMTRec::Crosses.ux");
	    bmtrec_crosses_uy = event.getDouble("BMTRec::Crosses.uy");
	    bmtrec_crosses_uz = event.getDouble("BMTRec::Crosses.uz");
	    bmtrec_crosses_x = event.getDouble("BMTRec::Crosses.x");
	    bmtrec_crosses_y = event.getDouble("BMTRec::Crosses.y");
	    bmtrec_crosses_z = event.getDouble("BMTRec::Crosses.z");
	} // BMTRec::Crosses

	if (event.hasBank("BMTRec::Hits")) {
	    bmtrec_hits_clusterID = event.getInt("BMTRec::Hits.clusterID");
	    bmtrec_hits_fitResidual = event
		    .getDouble("BMTRec::Hits.fitResidual");
	    bmtrec_hits_ID = event.getInt("BMTRec::Hits.ID");
	    bmtrec_hits_layer = event.getInt("BMTRec::Hits.layer");
	    bmtrec_hits_sector = event.getInt("BMTRec::Hits.sector");
	    bmtrec_hits_strip = event.getInt("BMTRec::Hits.strip");
	    bmtrec_hits_trkingStat = event.getInt("BMTRec::Hits.trkingStat");
	} // BMTRec::Hits

    } // load

    @Override
    public void clear() {
	bmt_dgtz_Edep = null;
	bmt_dgtz_hitn = null;
	bmt_dgtz_layer = null;
	bmt_dgtz_sector = null;
	bmt_dgtz_strip = null;
	bmt_true_avgLx = null;
	bmt_true_avgLy = null;
	bmt_true_avgLz = null;
	bmt_true_avgT = null;
	bmt_true_avgX = null;
	bmt_true_avgY = null;
	bmt_true_avgZ = null;
	bmt_true_hitn = null;
	bmt_true_mpid = null;
	bmt_true_mtid = null;
	bmt_true_mvx = null;
	bmt_true_mvy = null;
	bmt_true_mvz = null;
	bmt_true_otid = null;
	bmt_true_pid = null;
	bmt_true_px = null;
	bmt_true_py = null;
	bmt_true_pz = null;
	bmt_true_tid = null;
	bmt_true_totEdep = null;
	bmt_true_trackE = null;
	bmt_true_vx = null;
	bmt_true_vy = null;
	bmt_true_vz = null;
	bmtrec_clusters_centroid = null;
	bmtrec_clusters_ETot = null;
	bmtrec_clusters_Hit1_ID = null;
	bmtrec_clusters_Hit2_ID = null;
	bmtrec_clusters_Hit3_ID = null;
	bmtrec_clusters_Hit4_ID = null;
	bmtrec_clusters_Hit5_ID = null;
	bmtrec_clusters_ID = null;
	bmtrec_clusters_layer = null;
	bmtrec_clusters_sector = null;
	bmtrec_clusters_seedE = null;
	bmtrec_clusters_seedStrip = null;
	bmtrec_clusters_size = null;
	bmtrec_crosses_Cluster1_ID = null;
	bmtrec_crosses_Cluster2_ID = null;
	bmtrec_crosses_err_x = null;
	bmtrec_crosses_err_y = null;
	bmtrec_crosses_err_z = null;
	bmtrec_crosses_ID = null;
	bmtrec_crosses_region = null;
	bmtrec_crosses_sector = null;
	bmtrec_crosses_ux = null;
	bmtrec_crosses_uy = null;
	bmtrec_crosses_uz = null;
	bmtrec_crosses_x = null;
	bmtrec_crosses_y = null;
	bmtrec_crosses_z = null;
	bmtrec_hits_clusterID = null;
	bmtrec_hits_fitResidual = null;
	bmtrec_hits_ID = null;
	bmtrec_hits_layer = null;
	bmtrec_hits_sector = null;
	bmtrec_hits_strip = null;
	bmtrec_hits_trkingStat = null;
    } // clear

    @Override
    public void finalEventPrep(EvioDataEvent event) {
	extractUniqueLundIds(bmt_true_pid);
   }

    @Override
    public void addPreliminaryFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
	// TODO Auto-generated method stub

    }

    @Override
    public void addTrueFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
	// TODO Auto-generated method stub

    }

    @Override
    public void addDgtzFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
	// TODO Auto-generated method stub

    }

    @Override
    public void addFinalFeedback(int option, List<String> feedbackStrings) {
	// TODO Auto-generated method stub

    }

    @Override
    public void addReconstructedFeedback(int option,
	    List<String> feedbackStrings) {
	// TODO Auto-generated method stub

    }

}
