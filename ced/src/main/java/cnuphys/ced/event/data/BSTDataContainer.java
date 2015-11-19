package cnuphys.ced.event.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;

public class BSTDataContainer extends ADataContainer {

    /** ADC value for the strip */
    public int[] bst_dgtz_ADC;

    /** Time information */
    public int[] bst_dgtz_bco;

    /** Hit Number */
    public int[] bst_dgtz_hitn;

    /** Layer Number */
    public int[] bst_dgtz_layer;

    /** Sector Number */
    public int[] bst_dgtz_sector;

    /** Strip Number */
    public int[] bst_dgtz_strip;

    /** Average X position in local reference system */
    public double[] bst_true_avgLx;

    /** Average Y position in local reference system */
    public double[] bst_true_avgLy;

    /** Average Z position in local reference system */
    public double[] bst_true_avgLz;

    /** Average time */
    public double[] bst_true_avgT;

    /** Average X position in global reference system */
    public double[] bst_true_avgX;

    /** Average Y position in global reference system */
    public double[] bst_true_avgY;

    /** Average Z position in global reference system */
    public double[] bst_true_avgZ;

    /** Hit1 Number */
    public int[] bst_true_hitn;

    /** ID of the mother of the first particle entering the sensitive volume */
    public int[] bst_true_mpid;

    /**
     * Track ID of the mother of the first particle entering the sensitive
     * volume
     */
    public int[] bst_true_mtid;

    /**
     * x component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] bst_true_mvx;

    /**
     * y component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] bst_true_mvy;

    /**
     * z component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] bst_true_mvz;

    /**
     * Track ID of the original track that generated the first particle entering
     * the sensitive volume
     */
    public int[] bst_true_otid;

    /** ID of the first particle entering the sensitive volume */
    public int[] bst_true_pid;

    /** x component of momentum of the particle entering the sensitive volume */
    public double[] bst_true_px;

    /** y component of momentum of the particle entering the sensitive volume */
    public double[] bst_true_py;

    /** z component of momentum of the particle entering the sensitive volume */
    public double[] bst_true_pz;

    /** Track ID of the first particle entering the sensitive volume */
    public int[] bst_true_tid;

    /** Total Energy Deposited */
    public double[] bst_true_totEdep;

    /** Energy of the track */
    public double[] bst_true_trackE;

    /**
     * x component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] bst_true_vx;

    /**
     * y component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] bst_true_vy;

    /**
     * z component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] bst_true_vz;

    /** centroid strip number */
    public double[] bstrec_clusters_centroid;

    /** cluster total energy */
    public double[] bstrec_clusters_ETot;

    /** Index of hit 1 in cluster */
    public int[] bstrec_clusters_Hit1_ID;

    /** Index of hit 2 in cluster */
    public int[] bstrec_clusters_Hit2_ID;

    /** Index of hit 3 in cluster */
    public int[] bstrec_clusters_Hit3_ID;

    /** Index of hit 4 in cluster */
    public int[] bstrec_clusters_Hit4_ID;

    /** Index of hit 5 in cluster */
    public int[] bstrec_clusters_Hit5_ID;

    /** ID */
    public int[] bstrec_clusters_ID;

    /** layer */
    public int[] bstrec_clusters_layer;

    /** sector */
    public int[] bstrec_clusters_sector;

    /** energy of the seed */
    public double[] bstrec_clusters_seedE;

    /** seed strip */
    public int[] bstrec_clusters_seedStrip;

    /** cluster size */
    public int[] bstrec_clusters_size;

    /** ID of cosmic-region 1 cross in the track */
    public int[] bstrec_cosmics_Cross1_ID;

    /** ID of cosmic-region 2 cross in the track */
    public int[] bstrec_cosmics_Cross2_ID;

    /** ID of cosmic-region 3 cross in the track */
    public int[] bstrec_cosmics_Cross3_ID;

    /** ID of cosmic-region 4 cross in the track */
    public int[] bstrec_cosmics_Cross4_ID;

    /** ID of cosmic-region 5 cross in the track */
    public int[] bstrec_cosmics_Cross5_ID;

    /** ID of cosmic-region 6 cross in the track */
    public int[] bstrec_cosmics_Cross6_ID;

    /** ID of cosmic-region 7 cross in the track */
    public int[] bstrec_cosmics_Cross7_ID;

    /** ID of cosmic-region 8 cross in the track */
    public int[] bstrec_cosmics_Cross8_ID;

    /** ID */
    public int[] bstrec_cosmics_ID;

    /**
     * the chi^2 from the Kalman Fit to the strips (the fitted values are strip
     * numbers)
     */
    public double[] bstrec_cosmics_KF_chi2;

    /** the number of degrees of freedom for the Kalman fit */
    public int[] bstrec_cosmics_KF_ndf;

    /** phi (deg) */
    public double[] bstrec_cosmics_phi;

    /** theta (deg) */
    public double[] bstrec_cosmics_theta;

    /** trkline_yx_interc */
    public double[] bstrec_cosmics_trkline_yx_interc;

    /** trkline_yx_slope */
    public double[] bstrec_cosmics_trkline_yx_slope;

    /** trkline_yz_interc */
    public double[] bstrec_cosmics_trkline_yz_interc;

    /** trkline_yz_slope */
    public double[] bstrec_cosmics_trkline_yz_slope;

    /** ID of the bottom layer cluster in the Cross */
    public int[] bstrec_crosses_Cluster1_ID;

    /** ID of the top layer cluster in the Cross */
    public int[] bstrec_crosses_Cluster2_ID;

    /** BST cross x-coordinate error */
    public double[] bstrec_crosses_err_x;

    /** BST cross y-coordinate error */
    public double[] bstrec_crosses_err_y;

    /** BST cross z-coordinate error */
    public double[] bstrec_crosses_err_z;

    /** ID */
    public int[] bstrec_crosses_ID;

    /** region */
    public int[] bstrec_crosses_region;

    /** sector */
    public int[] bstrec_crosses_sector;

    /** BST cross x-direction (track unit tangent vector at the cross) */
    public double[] bstrec_crosses_ux;

    /** BST cross y-direction (track unit tangent vector at the cross) */
    public double[] bstrec_crosses_uy;

    /** BST cross z-direction (track unit tangent vector at the cross) */
    public double[] bstrec_crosses_uz;

    /** BST cross x-coordinate */
    public double[] bstrec_crosses_x;

    /** BST cross y-coordinate */
    public double[] bstrec_crosses_y;

    /** BST cross z-coordinate */
    public double[] bstrec_crosses_z;

    /** associated cluster ID */
    public int[] bstrec_hits_clusterID;

    /** fitted hit residual */
    public double[] bstrec_hits_fitResidual;

    /** hit ID */
    public int[] bstrec_hits_ID;

    /** hit layer */
    public int[] bstrec_hits_layer;

    /** hit sector */
    public int[] bstrec_hits_sector;

    /** hit strip */
    public int[] bstrec_hits_strip;

    /** tracking status */
    public int[] bstrec_hits_trkingStat;

    /**
     * x-coordinate of a helical trk direction extrapolated to a cylinder at 25
     * cm radius from the lab origin (cm unit)
     */
    public double[] bstrec_tracks_c_ux;

    /**
     * y-coordinate of a helical trk direction extrapolated to a cylinder at 25
     * cm radius from the lab origin (cm unit)
     */
    public double[] bstrec_tracks_c_uy;

    /**
     * z-coordinate of a helical trk direction extrapolated to a cylinder at 25
     * cm radius from the lab origin (cm unit)
     */
    public double[] bstrec_tracks_c_uz;

    /**
     * x-coordinate of a helical trk point extrapolated to a cylinder at 25 cm
     * radius from the lab origin (cm unit)
     */
    public double[] bstrec_tracks_c_x;

    /**
     * y-coordinate of a helical trk point extrapolated to a cylinder at 25 cm
     * radius from the lab origin (cm unit)
     */
    public double[] bstrec_tracks_c_y;

    /**
     * z-coordinate of a helical trk point extrapolated to a cylinder at 25 cm
     * radius from the lab origin (cm unit)b
     */
    public double[] bstrec_tracks_c_z;

    /** helical track fit covariance matrix element : delta_d0^2 */
    public double[] bstrec_tracks_cov_d02;

    /** helical track fit covariance matrix element : delta_d0.delta_phi0 */
    public double[] bstrec_tracks_cov_d0phi0;

    /** helical track fit covariance matrix element : delta_d0.delta_rho */
    public double[] bstrec_tracks_cov_d0rho;

    /** helical track fit covariance matrix element : delta_phi0^2 */
    public double[] bstrec_tracks_cov_phi02;

    /** helical track fit covariance matrix element : delta_phi0.delta_rho */
    public double[] bstrec_tracks_cov_phi0rho;

    /** helical track fit covariance matrix element : delta_rho.delta_rho */
    public double[] bstrec_tracks_cov_rho2;

    /** helical track fit covariance matrix element : delta_tandip^2 */
    public double[] bstrec_tracks_cov_tandip2;

    /** helical track fit covariance matrix element : delta_z0^2 */
    public double[] bstrec_tracks_cov_z02;

    /** ID of region 1 cross in the track */
    public int[] bstrec_tracks_Cross1_ID;

    /** ID of region 2 cross in the track */
    public int[] bstrec_tracks_Cross2_ID;

    /** ID of region 3 cross in the track */
    public int[] bstrec_tracks_Cross3_ID;

    /** ID of region 4 cross in the track */
    public int[] bstrec_tracks_Cross4_ID;

    /** helical track fit parameter: Distance of Closest Approach */
    public double[] bstrec_tracks_d0;

    /** fitting method (1 for global fit, 2 for Kalman Filter) */
    public int[] bstrec_tracks_fittingMethod;

    /** ID */
    public int[] bstrec_tracks_ID;

    /** total momentum */
    public double[] bstrec_tracks_p;

    /** total pathlength from the origin to the reference point (in cm) */
    public double[] bstrec_tracks_pathlength;

    /** helical track fit parameter: phi at DOCA */
    public double[] bstrec_tracks_phi0;

    /** transverse momentum */
    public double[] bstrec_tracks_pt;

    /** charge */
    public int[] bstrec_tracks_q;

    /** helical track fit parameter: dip angle */
    public double[] bstrec_tracks_tandip;

    /** helical track fit parameter: value of z at the DOCA */
    public double[] bstrec_tracks_z0;

    /** Estimated Centroid */
    public double[] bstrec_trajectory_CalcCentroidStrip;

    /** ID */
    public int[] bstrec_trajectory_ID;

    /** Layer of Intersection of Track */
    public int[] bstrec_trajectory_LayerTrackIntersPlane;

    /** Local phi of Intersection of Track */
    public double[] bstrec_trajectory_PhiTrackIntersPlane;

    /** Sector of Intersection of Track */
    public int[] bstrec_trajectory_SectorTrackIntersPlane;

    /** Local theta of Intersection of Track */
    public double[] bstrec_trajectory_ThetaTrackIntersPlane;

    /** Local angle of Intersection of Track with the module surface */
    public double[] bstrec_trajectory_trkToMPlnAngl;

    /** x of Intersection of Track */
    public double[] bstrec_trajectory_XtrackIntersPlane;

    /** y of Intersection of Track */
    public double[] bstrec_trajectory_YtrackIntersPlane;

    /** z of Intersection of Track */
    public double[] bstrec_trajectory_ZtrackIntersPlane;

    public BSTDataContainer(ClasIoEventManager eventManager) {
	super(eventManager);
    }

    @Override
    public int getHitCount(int option) {
	int hitCount = (bst_dgtz_sector == null) ? 0 : bst_dgtz_sector.length;
	return hitCount;
    }

    @Override
    public void load(EvioDataEvent event) {
	if (event == null) {
	    return;
	}

	if (event.hasBank("BST::dgtz")) {
	    bst_dgtz_ADC = event.getInt("BST::dgtz.ADC");
	    bst_dgtz_bco = event.getInt("BST::dgtz.bco");
	    bst_dgtz_hitn = event.getInt("BST::dgtz.hitn");
	    bst_dgtz_layer = event.getInt("BST::dgtz.layer");
	    bst_dgtz_sector = event.getInt("BST::dgtz.sector");
	    bst_dgtz_strip = event.getInt("BST::dgtz.strip");
	} // BST::dgtz

	if (event.hasBank("BST::true")) {
	    bst_true_avgLx = event.getDouble("BST::true.avgLx");
	    bst_true_avgLy = event.getDouble("BST::true.avgLy");
	    bst_true_avgLz = event.getDouble("BST::true.avgLz");
	    bst_true_avgT = event.getDouble("BST::true.avgT");
	    bst_true_avgX = event.getDouble("BST::true.avgX");
	    bst_true_avgY = event.getDouble("BST::true.avgY");
	    bst_true_avgZ = event.getDouble("BST::true.avgZ");
	    bst_true_hitn = event.getInt("BST::true.hitn");
	    bst_true_mpid = event.getInt("BST::true.mpid");
	    bst_true_mtid = event.getInt("BST::true.mtid");
	    bst_true_mvx = event.getDouble("BST::true.mvx");
	    bst_true_mvy = event.getDouble("BST::true.mvy");
	    bst_true_mvz = event.getDouble("BST::true.mvz");
	    bst_true_otid = event.getInt("BST::true.otid");
	    bst_true_pid = event.getInt("BST::true.pid");
	    bst_true_px = event.getDouble("BST::true.px");
	    bst_true_py = event.getDouble("BST::true.py");
	    bst_true_pz = event.getDouble("BST::true.pz");
	    bst_true_tid = event.getInt("BST::true.tid");
	    bst_true_totEdep = event.getDouble("BST::true.totEdep");
	    bst_true_trackE = event.getDouble("BST::true.trackE");
	    bst_true_vx = event.getDouble("BST::true.vx");
	    bst_true_vy = event.getDouble("BST::true.vy");
	    bst_true_vz = event.getDouble("BST::true.vz");
	} // BST::true

	if (event.hasBank("BSTRec::Clusters")) {
	    bstrec_clusters_centroid = event
		    .getDouble("BSTRec::Clusters.centroid");
	    bstrec_clusters_ETot = event.getDouble("BSTRec::Clusters.ETot");
	    bstrec_clusters_Hit1_ID = event.getInt("BSTRec::Clusters.Hit1_ID");
	    bstrec_clusters_Hit2_ID = event.getInt("BSTRec::Clusters.Hit2_ID");
	    bstrec_clusters_Hit3_ID = event.getInt("BSTRec::Clusters.Hit3_ID");
	    bstrec_clusters_Hit4_ID = event.getInt("BSTRec::Clusters.Hit4_ID");
	    bstrec_clusters_Hit5_ID = event.getInt("BSTRec::Clusters.Hit5_ID");
	    bstrec_clusters_ID = event.getInt("BSTRec::Clusters.ID");
	    bstrec_clusters_layer = event.getInt("BSTRec::Clusters.layer");
	    bstrec_clusters_sector = event.getInt("BSTRec::Clusters.sector");
	    bstrec_clusters_seedE = event.getDouble("BSTRec::Clusters.seedE");
	    bstrec_clusters_seedStrip = event
		    .getInt("BSTRec::Clusters.seedStrip");
	    bstrec_clusters_size = event.getInt("BSTRec::Clusters.size");
	} // BSTRec::Clusters

	if (event.hasBank("BSTRec::Cosmics")) {
	    bstrec_cosmics_Cross1_ID = event
		    .getInt("BSTRec::Cosmics.Cross1_ID");
	    bstrec_cosmics_Cross2_ID = event
		    .getInt("BSTRec::Cosmics.Cross2_ID");
	    bstrec_cosmics_Cross3_ID = event
		    .getInt("BSTRec::Cosmics.Cross3_ID");
	    bstrec_cosmics_Cross4_ID = event
		    .getInt("BSTRec::Cosmics.Cross4_ID");
	    bstrec_cosmics_Cross5_ID = event
		    .getInt("BSTRec::Cosmics.Cross5_ID");
	    bstrec_cosmics_Cross6_ID = event
		    .getInt("BSTRec::Cosmics.Cross6_ID");
	    bstrec_cosmics_Cross7_ID = event
		    .getInt("BSTRec::Cosmics.Cross7_ID");
	    bstrec_cosmics_Cross8_ID = event
		    .getInt("BSTRec::Cosmics.Cross8_ID");
	    bstrec_cosmics_ID = event.getInt("BSTRec::Cosmics.ID");
	    bstrec_cosmics_KF_chi2 = event.getDouble("BSTRec::Cosmics.KF_chi2");
	    bstrec_cosmics_KF_ndf = event.getInt("BSTRec::Cosmics.KF_ndf");
	    bstrec_cosmics_phi = event.getDouble("BSTRec::Cosmics.phi");
	    bstrec_cosmics_theta = event.getDouble("BSTRec::Cosmics.theta");
	    bstrec_cosmics_trkline_yx_interc = event
		    .getDouble("BSTRec::Cosmics.trkline_yx_interc");
	    bstrec_cosmics_trkline_yx_slope = event
		    .getDouble("BSTRec::Cosmics.trkline_yx_slope");
	    bstrec_cosmics_trkline_yz_interc = event
		    .getDouble("BSTRec::Cosmics.trkline_yz_interc");
	    bstrec_cosmics_trkline_yz_slope = event
		    .getDouble("BSTRec::Cosmics.trkline_yz_slope");
	} // BSTRec::Cosmics

	if (event.hasBank("BSTRec::Crosses")) {
	    bstrec_crosses_Cluster1_ID = event
		    .getInt("BSTRec::Crosses.Cluster1_ID");
	    bstrec_crosses_Cluster2_ID = event
		    .getInt("BSTRec::Crosses.Cluster2_ID");
	    bstrec_crosses_err_x = event.getDouble("BSTRec::Crosses.err_x");
	    bstrec_crosses_err_y = event.getDouble("BSTRec::Crosses.err_y");
	    bstrec_crosses_err_z = event.getDouble("BSTRec::Crosses.err_z");
	    bstrec_crosses_ID = event.getInt("BSTRec::Crosses.ID");
	    bstrec_crosses_region = event.getInt("BSTRec::Crosses.region");
	    bstrec_crosses_sector = event.getInt("BSTRec::Crosses.sector");
	    bstrec_crosses_ux = event.getDouble("BSTRec::Crosses.ux");
	    bstrec_crosses_uy = event.getDouble("BSTRec::Crosses.uy");
	    bstrec_crosses_uz = event.getDouble("BSTRec::Crosses.uz");
	    bstrec_crosses_x = event.getDouble("BSTRec::Crosses.x");
	    bstrec_crosses_y = event.getDouble("BSTRec::Crosses.y");
	    bstrec_crosses_z = event.getDouble("BSTRec::Crosses.z");
	} // BSTRec::Crosses

	if (event.hasBank("BSTRec::Hits")) {
	    bstrec_hits_clusterID = event.getInt("BSTRec::Hits.clusterID");
	    bstrec_hits_fitResidual = event
		    .getDouble("BSTRec::Hits.fitResidual");
	    bstrec_hits_ID = event.getInt("BSTRec::Hits.ID");
	    bstrec_hits_layer = event.getInt("BSTRec::Hits.layer");
	    bstrec_hits_sector = event.getInt("BSTRec::Hits.sector");
	    bstrec_hits_strip = event.getInt("BSTRec::Hits.strip");
	    bstrec_hits_trkingStat = event.getInt("BSTRec::Hits.trkingStat");
	} // BSTRec::Hits

	if (event.hasBank("BSTRec::Tracks")) {
	    bstrec_tracks_c_ux = event.getDouble("BSTRec::Tracks.c_ux");
	    bstrec_tracks_c_uy = event.getDouble("BSTRec::Tracks.c_uy");
	    bstrec_tracks_c_uz = event.getDouble("BSTRec::Tracks.c_uz");
	    bstrec_tracks_c_x = event.getDouble("BSTRec::Tracks.c_x");
	    bstrec_tracks_c_y = event.getDouble("BSTRec::Tracks.c_y");
	    bstrec_tracks_c_z = event.getDouble("BSTRec::Tracks.c_z");
	    bstrec_tracks_cov_d02 = event.getDouble("BSTRec::Tracks.cov_d02");
	    bstrec_tracks_cov_d0phi0 = event
		    .getDouble("BSTRec::Tracks.cov_d0phi0");
	    bstrec_tracks_cov_d0rho = event
		    .getDouble("BSTRec::Tracks.cov_d0rho");
	    bstrec_tracks_cov_phi02 = event
		    .getDouble("BSTRec::Tracks.cov_phi02");
	    bstrec_tracks_cov_phi0rho = event
		    .getDouble("BSTRec::Tracks.cov_phi0rho");
	    bstrec_tracks_cov_rho2 = event.getDouble("BSTRec::Tracks.cov_rho2");
	    bstrec_tracks_cov_tandip2 = event
		    .getDouble("BSTRec::Tracks.cov_tandip2");
	    bstrec_tracks_cov_z02 = event.getDouble("BSTRec::Tracks.cov_z02");
	    bstrec_tracks_Cross1_ID = event.getInt("BSTRec::Tracks.Cross1_ID");
	    bstrec_tracks_Cross2_ID = event.getInt("BSTRec::Tracks.Cross2_ID");
	    bstrec_tracks_Cross3_ID = event.getInt("BSTRec::Tracks.Cross3_ID");
	    bstrec_tracks_Cross4_ID = event.getInt("BSTRec::Tracks.Cross4_ID");
	    bstrec_tracks_d0 = event.getDouble("BSTRec::Tracks.d0");
	    bstrec_tracks_fittingMethod = event
		    .getInt("BSTRec::Tracks.fittingMethod");
	    bstrec_tracks_ID = event.getInt("BSTRec::Tracks.ID");
	    bstrec_tracks_p = event.getDouble("BSTRec::Tracks.p");
	    bstrec_tracks_pathlength = event
		    .getDouble("BSTRec::Tracks.pathlength");
	    bstrec_tracks_phi0 = event.getDouble("BSTRec::Tracks.phi0");
	    bstrec_tracks_pt = event.getDouble("BSTRec::Tracks.pt");
	    bstrec_tracks_q = event.getInt("BSTRec::Tracks.q");
	    bstrec_tracks_tandip = event.getDouble("BSTRec::Tracks.tandip");
	    bstrec_tracks_z0 = event.getDouble("BSTRec::Tracks.z0");
	} // BSTRec::Tracks

	if (event.hasBank("BSTRec::Trajectory")) {
	    bstrec_trajectory_CalcCentroidStrip = event
		    .getDouble("BSTRec::Trajectory.CalcCentroidStrip");
	    bstrec_trajectory_ID = event.getInt("BSTRec::Trajectory.ID");
	    bstrec_trajectory_LayerTrackIntersPlane = event
		    .getInt("BSTRec::Trajectory.LayerTrackIntersPlane");
	    bstrec_trajectory_PhiTrackIntersPlane = event
		    .getDouble("BSTRec::Trajectory.PhiTrackIntersPlane");
	    bstrec_trajectory_SectorTrackIntersPlane = event
		    .getInt("BSTRec::Trajectory.SectorTrackIntersPlane");
	    bstrec_trajectory_ThetaTrackIntersPlane = event
		    .getDouble("BSTRec::Trajectory.ThetaTrackIntersPlane");
	    bstrec_trajectory_trkToMPlnAngl = event
		    .getDouble("BSTRec::Trajectory.trkToMPlnAngl");
	    bstrec_trajectory_XtrackIntersPlane = event
		    .getDouble("BSTRec::Trajectory.XtrackIntersPlane");
	    bstrec_trajectory_YtrackIntersPlane = event
		    .getDouble("BSTRec::Trajectory.YtrackIntersPlane");
	    bstrec_trajectory_ZtrackIntersPlane = event
		    .getDouble("BSTRec::Trajectory.ZtrackIntersPlane");
	} // BSTRec::Trajectory

    } // load

    @Override
    public void clear() {
	bst_dgtz_ADC = null;
	bst_dgtz_bco = null;
	bst_dgtz_hitn = null;
	bst_dgtz_layer = null;
	bst_dgtz_sector = null;
	bst_dgtz_strip = null;
	bst_true_avgLx = null;
	bst_true_avgLy = null;
	bst_true_avgLz = null;
	bst_true_avgT = null;
	bst_true_avgX = null;
	bst_true_avgY = null;
	bst_true_avgZ = null;
	bst_true_hitn = null;
	bst_true_mpid = null;
	bst_true_mtid = null;
	bst_true_mvx = null;
	bst_true_mvy = null;
	bst_true_mvz = null;
	bst_true_otid = null;
	bst_true_pid = null;
	bst_true_px = null;
	bst_true_py = null;
	bst_true_pz = null;
	bst_true_tid = null;
	bst_true_totEdep = null;
	bst_true_trackE = null;
	bst_true_vx = null;
	bst_true_vy = null;
	bst_true_vz = null;
	bstrec_clusters_centroid = null;
	bstrec_clusters_ETot = null;
	bstrec_clusters_Hit1_ID = null;
	bstrec_clusters_Hit2_ID = null;
	bstrec_clusters_Hit3_ID = null;
	bstrec_clusters_Hit4_ID = null;
	bstrec_clusters_Hit5_ID = null;
	bstrec_clusters_ID = null;
	bstrec_clusters_layer = null;
	bstrec_clusters_sector = null;
	bstrec_clusters_seedE = null;
	bstrec_clusters_seedStrip = null;
	bstrec_clusters_size = null;
	bstrec_cosmics_Cross1_ID = null;
	bstrec_cosmics_Cross2_ID = null;
	bstrec_cosmics_Cross3_ID = null;
	bstrec_cosmics_Cross4_ID = null;
	bstrec_cosmics_Cross5_ID = null;
	bstrec_cosmics_Cross6_ID = null;
	bstrec_cosmics_Cross7_ID = null;
	bstrec_cosmics_Cross8_ID = null;
	bstrec_cosmics_ID = null;
	bstrec_cosmics_KF_chi2 = null;
	bstrec_cosmics_KF_ndf = null;
	bstrec_cosmics_phi = null;
	bstrec_cosmics_theta = null;
	bstrec_cosmics_trkline_yx_interc = null;
	bstrec_cosmics_trkline_yx_slope = null;
	bstrec_cosmics_trkline_yz_interc = null;
	bstrec_cosmics_trkline_yz_slope = null;
	bstrec_crosses_Cluster1_ID = null;
	bstrec_crosses_Cluster2_ID = null;
	bstrec_crosses_err_x = null;
	bstrec_crosses_err_y = null;
	bstrec_crosses_err_z = null;
	bstrec_crosses_ID = null;
	bstrec_crosses_region = null;
	bstrec_crosses_sector = null;
	bstrec_crosses_ux = null;
	bstrec_crosses_uy = null;
	bstrec_crosses_uz = null;
	bstrec_crosses_x = null;
	bstrec_crosses_y = null;
	bstrec_crosses_z = null;
	bstrec_hits_clusterID = null;
	bstrec_hits_fitResidual = null;
	bstrec_hits_ID = null;
	bstrec_hits_layer = null;
	bstrec_hits_sector = null;
	bstrec_hits_strip = null;
	bstrec_hits_trkingStat = null;
	bstrec_tracks_c_ux = null;
	bstrec_tracks_c_uy = null;
	bstrec_tracks_c_uz = null;
	bstrec_tracks_c_x = null;
	bstrec_tracks_c_y = null;
	bstrec_tracks_c_z = null;
	bstrec_tracks_cov_d02 = null;
	bstrec_tracks_cov_d0phi0 = null;
	bstrec_tracks_cov_d0rho = null;
	bstrec_tracks_cov_phi02 = null;
	bstrec_tracks_cov_phi0rho = null;
	bstrec_tracks_cov_rho2 = null;
	bstrec_tracks_cov_tandip2 = null;
	bstrec_tracks_cov_z02 = null;
	bstrec_tracks_Cross1_ID = null;
	bstrec_tracks_Cross2_ID = null;
	bstrec_tracks_Cross3_ID = null;
	bstrec_tracks_Cross4_ID = null;
	bstrec_tracks_d0 = null;
	bstrec_tracks_fittingMethod = null;
	bstrec_tracks_ID = null;
	bstrec_tracks_p = null;
	bstrec_tracks_pathlength = null;
	bstrec_tracks_phi0 = null;
	bstrec_tracks_pt = null;
	bstrec_tracks_q = null;
	bstrec_tracks_tandip = null;
	bstrec_tracks_z0 = null;
	bstrec_trajectory_CalcCentroidStrip = null;
	bstrec_trajectory_ID = null;
	bstrec_trajectory_LayerTrackIntersPlane = null;
	bstrec_trajectory_PhiTrackIntersPlane = null;
	bstrec_trajectory_SectorTrackIntersPlane = null;
	bstrec_trajectory_ThetaTrackIntersPlane = null;
	bstrec_trajectory_trkToMPlnAngl = null;
	bstrec_trajectory_XtrackIntersPlane = null;
	bstrec_trajectory_YtrackIntersPlane = null;
	bstrec_trajectory_ZtrackIntersPlane = null;
    } // clear

    /**
     * Get the number of reconstructed crosses
     * 
     * @return the number of reconstructed crosses
     */
    public int getCrossCount() {
	return (bstrec_crosses_sector == null) ? 0
		: bstrec_crosses_sector.length;
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

    /**
     * Get a collection of all strip, adc doublets for a given sector and layer
     * 
     * @param sector the 1-based sector
     * @param layer the 1-based layer
     * @return a collection of all strip, adc doublets for a given sector and
     *         layer. It is a collection of integer arrays. For each array, the
     *         0 entry is the 1-based strip and the 1 entry is the adc.
     */
    public Vector<int[]> allStripsForSectorAndLayer(int sector, int layer) {
	Vector<int[]> strips = new Vector<int[]>();

	if (bst_dgtz_sector != null) {
	    for (int hitIndex = 0; hitIndex < bst_dgtz_sector.length; hitIndex++) {
		if ((bst_dgtz_sector[hitIndex] == sector)
			&& (bst_dgtz_layer[hitIndex] == layer)) {
		    int data[] = { bst_dgtz_strip[hitIndex],
			    bst_dgtz_ADC[hitIndex] };
		    strips.add(data);
		}
	    }
	}

	// sort based on strips
	if (strips.size() > 1) {
	    Comparator<int[]> c = new Comparator<int[]>() {

		@Override
		public int compare(int[] o1, int[] o2) {
		    return Integer.compare(o1[0], o2[0]);
		}
	    };

	    Collections.sort(strips, c);
	}

	return strips;
    }

    @Override
    public void finalEventPrep(EvioDataEvent event) {
	extractUniqueLundIds(bst_true_pid);
    }

}
