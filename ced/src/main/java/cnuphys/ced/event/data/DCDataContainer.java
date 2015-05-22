package cnuphys.ced.event.data;

import java.util.List;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.splot.plot.DoubleFormat;

public class DCDataContainer extends ADataContainer {

    /** 2D distance between closest track step to the wire hit */
    public double[] dc_dgtz_doca;

    /** hit number */
    public int[] dc_dgtz_hitn;

    /** view */
    public int[] dc_dgtz_layer;

    /** Left/Right: -1 (right) if the track is between beam and the closest wire */
    public int[] dc_dgtz_LR;

    /** smeared doca */
    public double[] dc_dgtz_sdoca;

    /** sector number */
    public int[] dc_dgtz_sector;

    /** sdoca/drift velocity in each region */
    public double[] dc_dgtz_stime;

    /** layer number */
    public int[] dc_dgtz_superlayer;

    /** doca/drif velocity in each region 53,26,36 um/ns */
    public double[] dc_dgtz_time;

    /** strip number */
    public int[] dc_dgtz_wire;

    /** Average X position in local reference system */
    public double[] dc_true_avgLx;

    /** Average Y position in local reference system */
    public double[] dc_true_avgLy;

    /** Average Z position in local reference system */
    public double[] dc_true_avgLz;

    /** Average time */
    public double[] dc_true_avgT;

    /** Average X position in global reference system */
    public double[] dc_true_avgX;

    /** Average Y position in global reference system */
    public double[] dc_true_avgY;

    /** Average Z position in global reference system */
    public double[] dc_true_avgZ;

    /** Hit1 Number */
    public int[] dc_true_hitn;

    /** ID of the mother of the first particle entering the sensitive volume */
    public int[] dc_true_mpid;

    /**
     * Track ID of the mother of the first particle entering the sensitive
     * volume
     */
    public int[] dc_true_mtid;

    /**
     * x component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] dc_true_mvx;

    /**
     * y component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] dc_true_mvy;

    /**
     * z component of primary vertex of the mother of the particle entering the
     * sensitive volume
     */
    public double[] dc_true_mvz;

    /**
     * Track ID of the original track that generated the first particle entering
     * the sensitive volume
     */
    public int[] dc_true_otid;

    /** ID of the first particle entering the sensitive volume */
    public int[] dc_true_pid;

    /** x component of momentum of the particle entering the sensitive volume */
    public double[] dc_true_px;

    /** y component of momentum of the particle entering the sensitive volume */
    public double[] dc_true_py;

    /** z component of momentum of the particle entering the sensitive volume */
    public double[] dc_true_pz;

    /** Track ID of the first particle entering the sensitive volume */
    public int[] dc_true_tid;

    /** Total Energy Deposited */
    public double[] dc_true_totEdep;

    /** Energy of the track */
    public double[] dc_true_trackE;

    /**
     * x component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] dc_true_vx;

    /**
     * y component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] dc_true_vy;

    /**
     * z component of primary vertex of the particle entering the sensitive
     * volume
     */
    public double[] dc_true_vz;

    /** average wire number */
    public double[] hitbasedtrkg_hbclusters_avgWire;

    /** fit chisq prob. */
    public double[] hitbasedtrkg_hbclusters_fitChisqProb;

    /** line fit intercept */
    public double[] hitbasedtrkg_hbclusters_fitInterc;

    /** error on the intercept */
    public double[] hitbasedtrkg_hbclusters_fitIntercErr;

    /** line fit slope */
    public double[] hitbasedtrkg_hbclusters_fitSlope;

    /** error on slope */
    public double[] hitbasedtrkg_hbclusters_fitSlopeErr;

    /** Index of hit 10 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit10_ID;

    /** Index of hit 11 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit11_ID;

    /** Index of hit 12 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit12_ID;

    /** Index of hit 1 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit1_ID;

    /** Index of hit 2 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit2_ID;

    /** Index of hit 3 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit3_ID;

    /** Index of hit 4 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit4_ID;

    /** Index of hit 5 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit5_ID;

    /** Index of hit 6 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit6_ID;

    /** Index of hit 7 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit7_ID;

    /** Index of hit 8 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit8_ID;

    /** Index of hit 9 in segment */
    public int[] hitbasedtrkg_hbclusters_Hit9_ID;

    /** ID */
    public int[] hitbasedtrkg_hbclusters_ID;

    /** sector */
    public int[] hitbasedtrkg_hbclusters_sector;

    /** superlayer */
    public int[] hitbasedtrkg_hbclusters_superlayer;

    /**
     * DC track cross x-direction error (in the DC tilted sector coordinate
     * system)
     */
    public double[] hitbasedtrkg_hbcrosses_err_ux;

    /**
     * DC track cross y-direction error (in the DC tilted sector coordinate
     * system)
     */
    public double[] hitbasedtrkg_hbcrosses_err_uy;

    /**
     * DC track cross z-direction error (in the DC tilted sector coordinate
     * system)
     */
    public double[] hitbasedtrkg_hbcrosses_err_uz;

    /**
     * DC track cross x-coordinate error (in the DC tilted sector coordinate
     * system)
     */
    public double[] hitbasedtrkg_hbcrosses_err_x;

    /**
     * DC track cross y-coordinate error (in the DC tilted sector coordinate
     * system)
     */
    public double[] hitbasedtrkg_hbcrosses_err_y;

    /**
     * DC track cross z-coordinate error (in the DC tilted sector coordinate
     * system)
     */
    public double[] hitbasedtrkg_hbcrosses_err_z;

    /** ID */
    public int[] hitbasedtrkg_hbcrosses_ID;

    /** region */
    public int[] hitbasedtrkg_hbcrosses_region;

    /** sector */
    public int[] hitbasedtrkg_hbcrosses_sector;

    /** ID of the bottom layer segment in the Cross */
    public int[] hitbasedtrkg_hbcrosses_Segment1_ID;

    /** ID of the top layer segment in the Cross */
    public int[] hitbasedtrkg_hbcrosses_Segment2_ID;

    /** DC track cross x-direction (in the DC tilted sector coordinate system) */
    public double[] hitbasedtrkg_hbcrosses_ux;

    /** DC track cross y-direction (in the DC tilted sector coordinate system) */
    public double[] hitbasedtrkg_hbcrosses_uy;

    /** DC track cross z-direction (in the DC tilted sector coordinate system) */
    public double[] hitbasedtrkg_hbcrosses_uz;

    /** DC track cross x-coordinate (in the DC tilted sector coordinate system) */
    public double[] hitbasedtrkg_hbcrosses_x;

    /** DC track cross y-coordinate (in the DC tilted sector coordinate system) */
    public double[] hitbasedtrkg_hbcrosses_y;

    /** DC track cross z-coordinate (in the DC tilted sector coordinate system) */
    public double[] hitbasedtrkg_hbcrosses_z;

    /** associated cluster ID */
    public int[] hitbasedtrkg_hbhits_clusterID;

    /** doca (cm) */
    public double[] hitbasedtrkg_hbhits_doca;

    /** hit id */
    public int[] hitbasedtrkg_hbhits_id;

    /** hit layer */
    public int[] hitbasedtrkg_hbhits_layer;

    /** x in planar local coordinate system */
    public double[] hitbasedtrkg_hbhits_locX;

    /** y in planar local coordinate */
    public double[] hitbasedtrkg_hbhits_locY;

    /** left-right ambiguity assignment */
    public int[] hitbasedtrkg_hbhits_LR;

    /** hit sector */
    public int[] hitbasedtrkg_hbhits_sector;

    /** hit superlayer */
    public int[] hitbasedtrkg_hbhits_superlayer;

    /** time (ns) */
    public double[] hitbasedtrkg_hbhits_time;

    /** hit wire */
    public int[] hitbasedtrkg_hbhits_wire;

    /** wire x-coordinate in tilted-sector */
    public double[] hitbasedtrkg_hbhits_X;

    /** wire z-coordinate in tilted-sector */
    public double[] hitbasedtrkg_hbhits_Z;

    /** average wire number */
    public double[] hitbasedtrkg_hbsegments_avgWire;

    /** associated cluster ID */
    public int[] hitbasedtrkg_hbsegments_Cluster_ID;

    /** fit chisq prob. */
    public double[] hitbasedtrkg_hbsegments_fitChisqProb;

    /** line fit intercept */
    public double[] hitbasedtrkg_hbsegments_fitInterc;

    /** error on the intercept */
    public double[] hitbasedtrkg_hbsegments_fitIntercErr;

    /** line fit slope */
    public double[] hitbasedtrkg_hbsegments_fitSlope;

    /** error on slope */
    public double[] hitbasedtrkg_hbsegments_fitSlopeErr;

    /** Index of hit 10 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit10_ID;

    /** Index of hit 11 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit11_ID;

    /** Index of hit 12 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit12_ID;

    /** Index of hit 1 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit1_ID;

    /** Index of hit 2 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit2_ID;

    /** Index of hit 3 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit3_ID;

    /** Index of hit 4 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit4_ID;

    /** Index of hit 5 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit5_ID;

    /** Index of hit 6 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit6_ID;

    /** Index of hit 7 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit7_ID;

    /** Index of hit 8 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit8_ID;

    /** Index of hit 9 in segment */
    public int[] hitbasedtrkg_hbsegments_Hit9_ID;

    /** ID */
    public int[] hitbasedtrkg_hbsegments_ID;

    /** sector */
    public int[] hitbasedtrkg_hbsegments_sector;

    /** superlayer */
    public int[] hitbasedtrkg_hbsegments_superlayer;

    /** Region 3 cross unit x-direction vector in the lab */
    public double[] hitbasedtrkg_hbtracks_c3_ux;

    /** Region 3 cross unit y-direction vector in the lab */
    public double[] hitbasedtrkg_hbtracks_c3_uy;

    /** Region 3 cross unit z-direction vector in the lab */
    public double[] hitbasedtrkg_hbtracks_c3_uz;

    /** Region 3 cross x-position in the lab */
    public double[] hitbasedtrkg_hbtracks_c3_x;

    /** Region 3 cross y-position in the lab */
    public double[] hitbasedtrkg_hbtracks_c3_y;

    /** Region 3 cross z-position in the lab */
    public double[] hitbasedtrkg_hbtracks_c3_z;

    /** ID of region 1 cross in the track */
    public int[] hitbasedtrkg_hbtracks_Cross1_ID;

    /** ID of region 2 cross in the track */
    public int[] hitbasedtrkg_hbtracks_Cross2_ID;

    /** ID of region 3 cross in the track */
    public int[] hitbasedtrkg_hbtracks_Cross3_ID;

    /** ID */
    public int[] hitbasedtrkg_hbtracks_ID;

    /** momentum */
    public double[] hitbasedtrkg_hbtracks_p;

    /** 3-momentum x-coordinate of the swam track at z=0 in the lab frame */
    public double[] hitbasedtrkg_hbtracks_p0_x;

    /** 3-momentum y-coordinate of the swam track at z=0 in the lab frame */
    public double[] hitbasedtrkg_hbtracks_p0_y;

    /** 3-momentum z-coordinate of the swam track at z=0 in the lab frame */
    public double[] hitbasedtrkg_hbtracks_p0_z;

    /** path length from the cross of region 3 to z =0 in cm */
    public double[] hitbasedtrkg_hbtracks_pathlength;

    /** charge */
    public int[] hitbasedtrkg_hbtracks_q;

    /** sector */
    public int[] hitbasedtrkg_hbtracks_sector;

    /** Vertex x-position of the swam track at z=0 in the lab frame */
    public double[] hitbasedtrkg_hbtracks_Vtx0_x;

    /** Vertex y-position of the swam track at z=0 in the lab frame */
    public double[] hitbasedtrkg_hbtracks_Vtx0_y;

    /** Vertex z-position of the swam track at z=0 in the lab frame */
    public double[] hitbasedtrkg_hbtracks_Vtx0_z;

    /** average wire number */
    public double[] timebasedtrkg_tbclusters_avgWire;

    /** fit chisq prob. */
    public double[] timebasedtrkg_tbclusters_fitChisqProb;

    /** line fit intercept */
    public double[] timebasedtrkg_tbclusters_fitInterc;

    /** error on the intercept */
    public double[] timebasedtrkg_tbclusters_fitIntercErr;

    /** line fit slope */
    public double[] timebasedtrkg_tbclusters_fitSlope;

    /** error on slope */
    public double[] timebasedtrkg_tbclusters_fitSlopeErr;

    /** Index of hit 10 in segment */
    public int[] timebasedtrkg_tbclusters_Hit10_ID;

    /** Index of hit 11 in segment */
    public int[] timebasedtrkg_tbclusters_Hit11_ID;

    /** Index of hit 12 in segment */
    public int[] timebasedtrkg_tbclusters_Hit12_ID;

    /** Index of hit 1 in segment */
    public int[] timebasedtrkg_tbclusters_Hit1_ID;

    /** Index of hit 2 in segment */
    public int[] timebasedtrkg_tbclusters_Hit2_ID;

    /** Index of hit 3 in segment */
    public int[] timebasedtrkg_tbclusters_Hit3_ID;

    /** Index of hit 4 in segment */
    public int[] timebasedtrkg_tbclusters_Hit4_ID;

    /** Index of hit 5 in segment */
    public int[] timebasedtrkg_tbclusters_Hit5_ID;

    /** Index of hit 6 in segment */
    public int[] timebasedtrkg_tbclusters_Hit6_ID;

    /** Index of hit 7 in segment */
    public int[] timebasedtrkg_tbclusters_Hit7_ID;

    /** Index of hit 8 in segment */
    public int[] timebasedtrkg_tbclusters_Hit8_ID;

    /** Index of hit 9 in segment */
    public int[] timebasedtrkg_tbclusters_Hit9_ID;

    /** ID */
    public int[] timebasedtrkg_tbclusters_ID;

    /** sector */
    public int[] timebasedtrkg_tbclusters_sector;

    /** superlayer */
    public int[] timebasedtrkg_tbclusters_superlayer;

    /**
     * DC track cross x-direction error (in the DC tilted sector coordinate
     * system)
     */
    public double[] timebasedtrkg_tbcrosses_err_ux;

    /**
     * DC track cross y-direction error (in the DC tilted sector coordinate
     * system)
     */
    public double[] timebasedtrkg_tbcrosses_err_uy;

    /**
     * DC track cross z-direction error (in the DC tilted sector coordinate
     * system)
     */
    public double[] timebasedtrkg_tbcrosses_err_uz;

    /**
     * DC track cross x-coordinate error (in the DC tilted sector coordinate
     * system)
     */
    public double[] timebasedtrkg_tbcrosses_err_x;

    /**
     * DC track cross y-coordinate error (in the DC tilted sector coordinate
     * system)
     */
    public double[] timebasedtrkg_tbcrosses_err_y;

    /**
     * DC track cross z-coordinate error (in the DC tilted sector coordinate
     * system)
     */
    public double[] timebasedtrkg_tbcrosses_err_z;

    /** ID */
    public int[] timebasedtrkg_tbcrosses_ID;

    /** region */
    public int[] timebasedtrkg_tbcrosses_region;

    /** sector */
    public int[] timebasedtrkg_tbcrosses_sector;

    /** ID of the bottom layer segment in the Cross */
    public int[] timebasedtrkg_tbcrosses_Segment1_ID;

    /** ID of the top layer segment in the Cross */
    public int[] timebasedtrkg_tbcrosses_Segment2_ID;

    /** DC track cross x-direction (in the DC tilted sector coordinate system) */
    public double[] timebasedtrkg_tbcrosses_ux;

    /** DC track cross y-direction (in the DC tilted sector coordinate system) */
    public double[] timebasedtrkg_tbcrosses_uy;

    /** DC track cross z-direction (in the DC tilted sector coordinate system) */
    public double[] timebasedtrkg_tbcrosses_uz;

    /** DC track cross x-coordinate (in the DC tilted sector coordinate system) */
    public double[] timebasedtrkg_tbcrosses_x;

    /** DC track cross y-coordinate (in the DC tilted sector coordinate system) */
    public double[] timebasedtrkg_tbcrosses_y;

    /** DC track cross z-coordinate (in the DC tilted sector coordinate system) */
    public double[] timebasedtrkg_tbcrosses_z;

    /** associated cluster ID */
    public int[] timebasedtrkg_tbhits_clusterID;

    /** distance to the wire */
    public double[] timebasedtrkg_tbhits_doca;

    /** hit id */
    public int[] timebasedtrkg_tbhits_id;

    /** hit layer */
    public int[] timebasedtrkg_tbhits_layer;

    /** left-right ambiguity assignment */
    public int[] timebasedtrkg_tbhits_LR;

    /** hit sector */
    public int[] timebasedtrkg_tbhits_sector;

    /** hit superlayer */
    public int[] timebasedtrkg_tbhits_superlayer;

    /** time */
    public double[] timebasedtrkg_tbhits_time;

    /** hit residual */
    public double[] timebasedtrkg_tbhits_timeResidual;

    /** hit wire */
    public int[] timebasedtrkg_tbhits_wire;

    /** hit x-coordinate in tilted-sector */
    public double[] timebasedtrkg_tbhits_X;

    /** hit z-coordinate in tilted-sector */
    public double[] timebasedtrkg_tbhits_Z;

    /** average wire number */
    public double[] timebasedtrkg_tbsegments_avgWire;

    /** associated cluster ID */
    public int[] timebasedtrkg_tbsegments_Cluster_ID;

    /** fit chisq prob. */
    public double[] timebasedtrkg_tbsegments_fitChisqProb;

    /** line fit intercept */
    public double[] timebasedtrkg_tbsegments_fitInterc;

    /** error on the intercept */
    public double[] timebasedtrkg_tbsegments_fitIntercErr;

    /** line fit slope */
    public double[] timebasedtrkg_tbsegments_fitSlope;

    /** error on slope */
    public double[] timebasedtrkg_tbsegments_fitSlopeErr;

    /** Index of hit 10 in segment */
    public int[] timebasedtrkg_tbsegments_Hit10_ID;

    /** Index of hit 11 in segment */
    public int[] timebasedtrkg_tbsegments_Hit11_ID;

    /** Index of hit 12 in segment */
    public int[] timebasedtrkg_tbsegments_Hit12_ID;

    /** Index of hit 1 in segment */
    public int[] timebasedtrkg_tbsegments_Hit1_ID;

    /** Index of hit 2 in segment */
    public int[] timebasedtrkg_tbsegments_Hit2_ID;

    /** Index of hit 3 in segment */
    public int[] timebasedtrkg_tbsegments_Hit3_ID;

    /** Index of hit 4 in segment */
    public int[] timebasedtrkg_tbsegments_Hit4_ID;

    /** Index of hit 5 in segment */
    public int[] timebasedtrkg_tbsegments_Hit5_ID;

    /** Index of hit 6 in segment */
    public int[] timebasedtrkg_tbsegments_Hit6_ID;

    /** Index of hit 7 in segment */
    public int[] timebasedtrkg_tbsegments_Hit7_ID;

    /** Index of hit 8 in segment */
    public int[] timebasedtrkg_tbsegments_Hit8_ID;

    /** Index of hit 9 in segment */
    public int[] timebasedtrkg_tbsegments_Hit9_ID;

    /** ID */
    public int[] timebasedtrkg_tbsegments_ID;

    /** sector */
    public int[] timebasedtrkg_tbsegments_sector;

    /** superlayer */
    public int[] timebasedtrkg_tbsegments_superlayer;

    /** C11 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C11;

    /** C12 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C12;

    /** C13 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C13;

    /** C14 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C14;

    /** C15 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C15;

    /** C21 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C21;

    /** C22 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C22;

    /** C23 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C23;

    /** C24 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C24;

    /** C25 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C25;

    /** C31 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C31;

    /** C32 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C32;

    /** C33 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C33;

    /** C34 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C34;

    /** C35 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C35;

    /** Region 3 cross unit x-direction vector in the lab */
    public double[] timebasedtrkg_tbtracks_c3_ux;

    /** Region 3 cross unit y-direction vector in the lab */
    public double[] timebasedtrkg_tbtracks_c3_uy;

    /** Region 3 cross unit z-direction vector in the lab */
    public double[] timebasedtrkg_tbtracks_c3_uz;

    /** Region 3 cross x-position in the lab */
    public double[] timebasedtrkg_tbtracks_c3_x;

    /** Region 3 cross y-position in the lab */
    public double[] timebasedtrkg_tbtracks_c3_y;

    /** Region 3 cross z-position in the lab */
    public double[] timebasedtrkg_tbtracks_c3_z;

    /** C41 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C41;

    /** C42 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C42;

    /** C43 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C43;

    /** C44 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C44;

    /** C45 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C45;

    /** C51 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C51;

    /** C52 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C52;

    /** C53 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C53;

    /** C54 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C54;

    /** C55 covariance matrix element at last superlayer used in the fit */
    public double[] timebasedtrkg_tbtracks_C55;

    /** ID of region 1 cross in the track */
    public int[] timebasedtrkg_tbtracks_Cross1_ID;

    /** ID of region 2 cross in the track */
    public int[] timebasedtrkg_tbtracks_Cross2_ID;

    /** ID of region 3 cross in the track */
    public int[] timebasedtrkg_tbtracks_Cross3_ID;

    /** fit chi^2 */
    public double[] timebasedtrkg_tbtracks_fitChisq;

    /** ID */
    public int[] timebasedtrkg_tbtracks_ID;

    /** momentum */
    public double[] timebasedtrkg_tbtracks_p;

    /** 3-momentum x-coordinate of the swam track at z=0 in the lab frame */
    public double[] timebasedtrkg_tbtracks_p0_x;

    /** 3-momentum y-coordinate of the swam track at z=0 in the lab frame */
    public double[] timebasedtrkg_tbtracks_p0_y;

    /** 3-momentum z-coordinate of the swam track at z=0 in the lab frame */
    public double[] timebasedtrkg_tbtracks_p0_z;

    /** path length from the cross of region 3 to z =0 in cm */
    public double[] timebasedtrkg_tbtracks_pathlength;

    /** charge */
    public int[] timebasedtrkg_tbtracks_q;

    /** sector */
    public int[] timebasedtrkg_tbtracks_sector;

    /** Vertex x-position of the swam track at z=0 in the lab frame */
    public double[] timebasedtrkg_tbtracks_Vtx0_x;

    /** Vertex y-position of the swam track at z=0 in the lab frame */
    public double[] timebasedtrkg_tbtracks_Vtx0_y;

    /** Vertex z-position of the swam track at z=0 in the lab frame */
    public double[] timebasedtrkg_tbtracks_Vtx0_z;

    public DCDataContainer(ClasIoEventManager eventManager) {
	super(eventManager);
    }

    @Override
    public int getHitCount(int option) {
	int hitCount = (dc_dgtz_sector == null) ? 0 : dc_dgtz_sector.length;
	return hitCount;
    }

    @Override
    public void load(EvioDataEvent event) {
	if (event == null) {
	    return;
	}

	if (event.hasBank("DC::dgtz")) {
	    dc_dgtz_doca = event.getDouble("DC::dgtz.doca");
	    dc_dgtz_hitn = event.getInt("DC::dgtz.hitn");
	    dc_dgtz_layer = event.getInt("DC::dgtz.layer");
	    dc_dgtz_LR = event.getInt("DC::dgtz.LR");
	    dc_dgtz_sdoca = event.getDouble("DC::dgtz.sdoca");
	    dc_dgtz_sector = event.getInt("DC::dgtz.sector");
	    dc_dgtz_stime = event.getDouble("DC::dgtz.stime");
	    dc_dgtz_superlayer = event.getInt("DC::dgtz.superlayer");
	    dc_dgtz_time = event.getDouble("DC::dgtz.time");
	    dc_dgtz_wire = event.getInt("DC::dgtz.wire");
	} // DC::dgtz

	if (event.hasBank("DC::true")) {
	    dc_true_avgLx = event.getDouble("DC::true.avgLx");
	    dc_true_avgLy = event.getDouble("DC::true.avgLy");
	    dc_true_avgLz = event.getDouble("DC::true.avgLz");
	    dc_true_avgT = event.getDouble("DC::true.avgT");
	    dc_true_avgX = event.getDouble("DC::true.avgX");
	    dc_true_avgY = event.getDouble("DC::true.avgY");
	    dc_true_avgZ = event.getDouble("DC::true.avgZ");
	    dc_true_hitn = event.getInt("DC::true.hitn");
	    dc_true_mpid = event.getInt("DC::true.mpid");
	    dc_true_mtid = event.getInt("DC::true.mtid");
	    dc_true_mvx = event.getDouble("DC::true.mvx");
	    dc_true_mvy = event.getDouble("DC::true.mvy");
	    dc_true_mvz = event.getDouble("DC::true.mvz");
	    dc_true_otid = event.getInt("DC::true.otid");
	    dc_true_pid = event.getInt("DC::true.pid");
	    dc_true_px = event.getDouble("DC::true.px");
	    dc_true_py = event.getDouble("DC::true.py");
	    dc_true_pz = event.getDouble("DC::true.pz");
	    dc_true_tid = event.getInt("DC::true.tid");
	    dc_true_totEdep = event.getDouble("DC::true.totEdep");
	    dc_true_trackE = event.getDouble("DC::true.trackE");
	    dc_true_vx = event.getDouble("DC::true.vx");
	    dc_true_vy = event.getDouble("DC::true.vy");
	    dc_true_vz = event.getDouble("DC::true.vz");
	} // DC::true

	if (event.hasBank("HitBasedTrkg::HBClusters")) {
	    hitbasedtrkg_hbclusters_avgWire = event
		    .getDouble("HitBasedTrkg::HBClusters.avgWire");
	    hitbasedtrkg_hbclusters_fitChisqProb = event
		    .getDouble("HitBasedTrkg::HBClusters.fitChisqProb");
	    hitbasedtrkg_hbclusters_fitInterc = event
		    .getDouble("HitBasedTrkg::HBClusters.fitInterc");
	    hitbasedtrkg_hbclusters_fitIntercErr = event
		    .getDouble("HitBasedTrkg::HBClusters.fitIntercErr");
	    hitbasedtrkg_hbclusters_fitSlope = event
		    .getDouble("HitBasedTrkg::HBClusters.fitSlope");
	    hitbasedtrkg_hbclusters_fitSlopeErr = event
		    .getDouble("HitBasedTrkg::HBClusters.fitSlopeErr");
	    hitbasedtrkg_hbclusters_Hit10_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit10_ID");
	    hitbasedtrkg_hbclusters_Hit11_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit11_ID");
	    hitbasedtrkg_hbclusters_Hit12_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit12_ID");
	    hitbasedtrkg_hbclusters_Hit1_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit1_ID");
	    hitbasedtrkg_hbclusters_Hit2_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit2_ID");
	    hitbasedtrkg_hbclusters_Hit3_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit3_ID");
	    hitbasedtrkg_hbclusters_Hit4_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit4_ID");
	    hitbasedtrkg_hbclusters_Hit5_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit5_ID");
	    hitbasedtrkg_hbclusters_Hit6_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit6_ID");
	    hitbasedtrkg_hbclusters_Hit7_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit7_ID");
	    hitbasedtrkg_hbclusters_Hit8_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit8_ID");
	    hitbasedtrkg_hbclusters_Hit9_ID = event
		    .getInt("HitBasedTrkg::HBClusters.Hit9_ID");
	    hitbasedtrkg_hbclusters_ID = event
		    .getInt("HitBasedTrkg::HBClusters.ID");
	    hitbasedtrkg_hbclusters_sector = event
		    .getInt("HitBasedTrkg::HBClusters.sector");
	    hitbasedtrkg_hbclusters_superlayer = event
		    .getInt("HitBasedTrkg::HBClusters.superlayer");
	} // HitBasedTrkg::HBClusters

	if (event.hasBank("HitBasedTrkg::HBCrosses")) {
	    hitbasedtrkg_hbcrosses_err_ux = event
		    .getDouble("HitBasedTrkg::HBCrosses.err_ux");
	    hitbasedtrkg_hbcrosses_err_uy = event
		    .getDouble("HitBasedTrkg::HBCrosses.err_uy");
	    hitbasedtrkg_hbcrosses_err_uz = event
		    .getDouble("HitBasedTrkg::HBCrosses.err_uz");
	    hitbasedtrkg_hbcrosses_err_x = event
		    .getDouble("HitBasedTrkg::HBCrosses.err_x");
	    hitbasedtrkg_hbcrosses_err_y = event
		    .getDouble("HitBasedTrkg::HBCrosses.err_y");
	    hitbasedtrkg_hbcrosses_err_z = event
		    .getDouble("HitBasedTrkg::HBCrosses.err_z");
	    hitbasedtrkg_hbcrosses_ID = event
		    .getInt("HitBasedTrkg::HBCrosses.ID");
	    hitbasedtrkg_hbcrosses_region = event
		    .getInt("HitBasedTrkg::HBCrosses.region");
	    hitbasedtrkg_hbcrosses_sector = event
		    .getInt("HitBasedTrkg::HBCrosses.sector");
	    hitbasedtrkg_hbcrosses_Segment1_ID = event
		    .getInt("HitBasedTrkg::HBCrosses.Segment1_ID");
	    hitbasedtrkg_hbcrosses_Segment2_ID = event
		    .getInt("HitBasedTrkg::HBCrosses.Segment2_ID");
	    hitbasedtrkg_hbcrosses_ux = event
		    .getDouble("HitBasedTrkg::HBCrosses.ux");
	    hitbasedtrkg_hbcrosses_uy = event
		    .getDouble("HitBasedTrkg::HBCrosses.uy");
	    hitbasedtrkg_hbcrosses_uz = event
		    .getDouble("HitBasedTrkg::HBCrosses.uz");
	    hitbasedtrkg_hbcrosses_x = event
		    .getDouble("HitBasedTrkg::HBCrosses.x");
	    hitbasedtrkg_hbcrosses_y = event
		    .getDouble("HitBasedTrkg::HBCrosses.y");
	    hitbasedtrkg_hbcrosses_z = event
		    .getDouble("HitBasedTrkg::HBCrosses.z");
	} // HitBasedTrkg::HBCrosses

	if (event.hasBank("HitBasedTrkg::HBHits")) {
	    hitbasedtrkg_hbhits_clusterID = event
		    .getInt("HitBasedTrkg::HBHits.clusterID");
	    hitbasedtrkg_hbhits_doca = event
		    .getDouble("HitBasedTrkg::HBHits.doca");
	    hitbasedtrkg_hbhits_id = event.getInt("HitBasedTrkg::HBHits.id");
	    hitbasedtrkg_hbhits_layer = event
		    .getInt("HitBasedTrkg::HBHits.layer");
	    hitbasedtrkg_hbhits_locX = event
		    .getDouble("HitBasedTrkg::HBHits.locX");
	    hitbasedtrkg_hbhits_locY = event
		    .getDouble("HitBasedTrkg::HBHits.locY");
	    hitbasedtrkg_hbhits_LR = event.getInt("HitBasedTrkg::HBHits.LR");
	    hitbasedtrkg_hbhits_sector = event
		    .getInt("HitBasedTrkg::HBHits.sector");
	    hitbasedtrkg_hbhits_superlayer = event
		    .getInt("HitBasedTrkg::HBHits.superlayer");
	    hitbasedtrkg_hbhits_time = event
		    .getDouble("HitBasedTrkg::HBHits.time");
	    hitbasedtrkg_hbhits_wire = event
		    .getInt("HitBasedTrkg::HBHits.wire");
	    hitbasedtrkg_hbhits_X = event.getDouble("HitBasedTrkg::HBHits.X");
	    hitbasedtrkg_hbhits_Z = event.getDouble("HitBasedTrkg::HBHits.Z");
	} // HitBasedTrkg::HBHits

	if (event.hasBank("HitBasedTrkg::HBSegments")) {
	    hitbasedtrkg_hbsegments_avgWire = event
		    .getDouble("HitBasedTrkg::HBSegments.avgWire");
	    hitbasedtrkg_hbsegments_Cluster_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Cluster_ID");
	    hitbasedtrkg_hbsegments_fitChisqProb = event
		    .getDouble("HitBasedTrkg::HBSegments.fitChisqProb");
	    hitbasedtrkg_hbsegments_fitInterc = event
		    .getDouble("HitBasedTrkg::HBSegments.fitInterc");
	    hitbasedtrkg_hbsegments_fitIntercErr = event
		    .getDouble("HitBasedTrkg::HBSegments.fitIntercErr");
	    hitbasedtrkg_hbsegments_fitSlope = event
		    .getDouble("HitBasedTrkg::HBSegments.fitSlope");
	    hitbasedtrkg_hbsegments_fitSlopeErr = event
		    .getDouble("HitBasedTrkg::HBSegments.fitSlopeErr");
	    hitbasedtrkg_hbsegments_Hit10_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit10_ID");
	    hitbasedtrkg_hbsegments_Hit11_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit11_ID");
	    hitbasedtrkg_hbsegments_Hit12_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit12_ID");
	    hitbasedtrkg_hbsegments_Hit1_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit1_ID");
	    hitbasedtrkg_hbsegments_Hit2_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit2_ID");
	    hitbasedtrkg_hbsegments_Hit3_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit3_ID");
	    hitbasedtrkg_hbsegments_Hit4_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit4_ID");
	    hitbasedtrkg_hbsegments_Hit5_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit5_ID");
	    hitbasedtrkg_hbsegments_Hit6_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit6_ID");
	    hitbasedtrkg_hbsegments_Hit7_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit7_ID");
	    hitbasedtrkg_hbsegments_Hit8_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit8_ID");
	    hitbasedtrkg_hbsegments_Hit9_ID = event
		    .getInt("HitBasedTrkg::HBSegments.Hit9_ID");
	    hitbasedtrkg_hbsegments_ID = event
		    .getInt("HitBasedTrkg::HBSegments.ID");
	    hitbasedtrkg_hbsegments_sector = event
		    .getInt("HitBasedTrkg::HBSegments.sector");
	    hitbasedtrkg_hbsegments_superlayer = event
		    .getInt("HitBasedTrkg::HBSegments.superlayer");
	} // HitBasedTrkg::HBSegments

	if (event.hasBank("HitBasedTrkg::HBTracks")) {
	    hitbasedtrkg_hbtracks_c3_ux = event
		    .getDouble("HitBasedTrkg::HBTracks.c3_ux");
	    hitbasedtrkg_hbtracks_c3_uy = event
		    .getDouble("HitBasedTrkg::HBTracks.c3_uy");
	    hitbasedtrkg_hbtracks_c3_uz = event
		    .getDouble("HitBasedTrkg::HBTracks.c3_uz");
	    hitbasedtrkg_hbtracks_c3_x = event
		    .getDouble("HitBasedTrkg::HBTracks.c3_x");
	    hitbasedtrkg_hbtracks_c3_y = event
		    .getDouble("HitBasedTrkg::HBTracks.c3_y");
	    hitbasedtrkg_hbtracks_c3_z = event
		    .getDouble("HitBasedTrkg::HBTracks.c3_z");
	    hitbasedtrkg_hbtracks_Cross1_ID = event
		    .getInt("HitBasedTrkg::HBTracks.Cross1_ID");
	    hitbasedtrkg_hbtracks_Cross2_ID = event
		    .getInt("HitBasedTrkg::HBTracks.Cross2_ID");
	    hitbasedtrkg_hbtracks_Cross3_ID = event
		    .getInt("HitBasedTrkg::HBTracks.Cross3_ID");
	    hitbasedtrkg_hbtracks_ID = event
		    .getInt("HitBasedTrkg::HBTracks.ID");
	    hitbasedtrkg_hbtracks_p = event
		    .getDouble("HitBasedTrkg::HBTracks.p");
	    hitbasedtrkg_hbtracks_p0_x = event
		    .getDouble("HitBasedTrkg::HBTracks.p0_x");
	    hitbasedtrkg_hbtracks_p0_y = event
		    .getDouble("HitBasedTrkg::HBTracks.p0_y");
	    hitbasedtrkg_hbtracks_p0_z = event
		    .getDouble("HitBasedTrkg::HBTracks.p0_z");
	    hitbasedtrkg_hbtracks_pathlength = event
		    .getDouble("HitBasedTrkg::HBTracks.pathlength");
	    hitbasedtrkg_hbtracks_q = event.getInt("HitBasedTrkg::HBTracks.q");
	    hitbasedtrkg_hbtracks_sector = event
		    .getInt("HitBasedTrkg::HBTracks.sector");
	    hitbasedtrkg_hbtracks_Vtx0_x = event
		    .getDouble("HitBasedTrkg::HBTracks.Vtx0_x");
	    hitbasedtrkg_hbtracks_Vtx0_y = event
		    .getDouble("HitBasedTrkg::HBTracks.Vtx0_y");
	    hitbasedtrkg_hbtracks_Vtx0_z = event
		    .getDouble("HitBasedTrkg::HBTracks.Vtx0_z");
	} // HitBasedTrkg::HBTracks

	if (event.hasBank("TimeBasedTrkg::TBClusters")) {
	    timebasedtrkg_tbclusters_avgWire = event
		    .getDouble("TimeBasedTrkg::TBClusters.avgWire");
	    timebasedtrkg_tbclusters_fitChisqProb = event
		    .getDouble("TimeBasedTrkg::TBClusters.fitChisqProb");
	    timebasedtrkg_tbclusters_fitInterc = event
		    .getDouble("TimeBasedTrkg::TBClusters.fitInterc");
	    timebasedtrkg_tbclusters_fitIntercErr = event
		    .getDouble("TimeBasedTrkg::TBClusters.fitIntercErr");
	    timebasedtrkg_tbclusters_fitSlope = event
		    .getDouble("TimeBasedTrkg::TBClusters.fitSlope");
	    timebasedtrkg_tbclusters_fitSlopeErr = event
		    .getDouble("TimeBasedTrkg::TBClusters.fitSlopeErr");
	    timebasedtrkg_tbclusters_Hit10_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit10_ID");
	    timebasedtrkg_tbclusters_Hit11_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit11_ID");
	    timebasedtrkg_tbclusters_Hit12_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit12_ID");
	    timebasedtrkg_tbclusters_Hit1_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit1_ID");
	    timebasedtrkg_tbclusters_Hit2_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit2_ID");
	    timebasedtrkg_tbclusters_Hit3_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit3_ID");
	    timebasedtrkg_tbclusters_Hit4_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit4_ID");
	    timebasedtrkg_tbclusters_Hit5_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit5_ID");
	    timebasedtrkg_tbclusters_Hit6_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit6_ID");
	    timebasedtrkg_tbclusters_Hit7_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit7_ID");
	    timebasedtrkg_tbclusters_Hit8_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit8_ID");
	    timebasedtrkg_tbclusters_Hit9_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.Hit9_ID");
	    timebasedtrkg_tbclusters_ID = event
		    .getInt("TimeBasedTrkg::TBClusters.ID");
	    timebasedtrkg_tbclusters_sector = event
		    .getInt("TimeBasedTrkg::TBClusters.sector");
	    timebasedtrkg_tbclusters_superlayer = event
		    .getInt("TimeBasedTrkg::TBClusters.superlayer");
	} // TimeBasedTrkg::TBClusters

	if (event.hasBank("TimeBasedTrkg::TBCrosses")) {
	    timebasedtrkg_tbcrosses_err_ux = event
		    .getDouble("TimeBasedTrkg::TBCrosses.err_ux");
	    timebasedtrkg_tbcrosses_err_uy = event
		    .getDouble("TimeBasedTrkg::TBCrosses.err_uy");
	    timebasedtrkg_tbcrosses_err_uz = event
		    .getDouble("TimeBasedTrkg::TBCrosses.err_uz");
	    timebasedtrkg_tbcrosses_err_x = event
		    .getDouble("TimeBasedTrkg::TBCrosses.err_x");
	    timebasedtrkg_tbcrosses_err_y = event
		    .getDouble("TimeBasedTrkg::TBCrosses.err_y");
	    timebasedtrkg_tbcrosses_err_z = event
		    .getDouble("TimeBasedTrkg::TBCrosses.err_z");
	    timebasedtrkg_tbcrosses_ID = event
		    .getInt("TimeBasedTrkg::TBCrosses.ID");
	    timebasedtrkg_tbcrosses_region = event
		    .getInt("TimeBasedTrkg::TBCrosses.region");
	    timebasedtrkg_tbcrosses_sector = event
		    .getInt("TimeBasedTrkg::TBCrosses.sector");
	    timebasedtrkg_tbcrosses_Segment1_ID = event
		    .getInt("TimeBasedTrkg::TBCrosses.Segment1_ID");
	    timebasedtrkg_tbcrosses_Segment2_ID = event
		    .getInt("TimeBasedTrkg::TBCrosses.Segment2_ID");
	    timebasedtrkg_tbcrosses_ux = event
		    .getDouble("TimeBasedTrkg::TBCrosses.ux");
	    timebasedtrkg_tbcrosses_uy = event
		    .getDouble("TimeBasedTrkg::TBCrosses.uy");
	    timebasedtrkg_tbcrosses_uz = event
		    .getDouble("TimeBasedTrkg::TBCrosses.uz");
	    timebasedtrkg_tbcrosses_x = event
		    .getDouble("TimeBasedTrkg::TBCrosses.x");
	    timebasedtrkg_tbcrosses_y = event
		    .getDouble("TimeBasedTrkg::TBCrosses.y");
	    timebasedtrkg_tbcrosses_z = event
		    .getDouble("TimeBasedTrkg::TBCrosses.z");
	} // TimeBasedTrkg::TBCrosses

	if (event.hasBank("TimeBasedTrkg::TBHits")) {
	    timebasedtrkg_tbhits_clusterID = event
		    .getInt("TimeBasedTrkg::TBHits.clusterID");
	    timebasedtrkg_tbhits_doca = event
		    .getDouble("TimeBasedTrkg::TBHits.doca");
	    timebasedtrkg_tbhits_id = event.getInt("TimeBasedTrkg::TBHits.id");
	    timebasedtrkg_tbhits_layer = event
		    .getInt("TimeBasedTrkg::TBHits.layer");
	    timebasedtrkg_tbhits_LR = event.getInt("TimeBasedTrkg::TBHits.LR");
	    timebasedtrkg_tbhits_sector = event
		    .getInt("TimeBasedTrkg::TBHits.sector");
	    timebasedtrkg_tbhits_superlayer = event
		    .getInt("TimeBasedTrkg::TBHits.superlayer");
	    timebasedtrkg_tbhits_time = event
		    .getDouble("TimeBasedTrkg::TBHits.time");
	    timebasedtrkg_tbhits_timeResidual = event
		    .getDouble("TimeBasedTrkg::TBHits.timeResidual");
	    timebasedtrkg_tbhits_wire = event
		    .getInt("TimeBasedTrkg::TBHits.wire");
	    timebasedtrkg_tbhits_X = event.getDouble("TimeBasedTrkg::TBHits.X");
	    timebasedtrkg_tbhits_Z = event.getDouble("TimeBasedTrkg::TBHits.Z");
	} // TimeBasedTrkg::TBHits

	if (event.hasBank("TimeBasedTrkg::TBSegments")) {
	    timebasedtrkg_tbsegments_avgWire = event
		    .getDouble("TimeBasedTrkg::TBSegments.avgWire");
	    timebasedtrkg_tbsegments_Cluster_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Cluster_ID");
	    timebasedtrkg_tbsegments_fitChisqProb = event
		    .getDouble("TimeBasedTrkg::TBSegments.fitChisqProb");
	    timebasedtrkg_tbsegments_fitInterc = event
		    .getDouble("TimeBasedTrkg::TBSegments.fitInterc");
	    timebasedtrkg_tbsegments_fitIntercErr = event
		    .getDouble("TimeBasedTrkg::TBSegments.fitIntercErr");
	    timebasedtrkg_tbsegments_fitSlope = event
		    .getDouble("TimeBasedTrkg::TBSegments.fitSlope");
	    timebasedtrkg_tbsegments_fitSlopeErr = event
		    .getDouble("TimeBasedTrkg::TBSegments.fitSlopeErr");
	    timebasedtrkg_tbsegments_Hit10_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit10_ID");
	    timebasedtrkg_tbsegments_Hit11_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit11_ID");
	    timebasedtrkg_tbsegments_Hit12_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit12_ID");
	    timebasedtrkg_tbsegments_Hit1_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit1_ID");
	    timebasedtrkg_tbsegments_Hit2_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit2_ID");
	    timebasedtrkg_tbsegments_Hit3_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit3_ID");
	    timebasedtrkg_tbsegments_Hit4_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit4_ID");
	    timebasedtrkg_tbsegments_Hit5_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit5_ID");
	    timebasedtrkg_tbsegments_Hit6_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit6_ID");
	    timebasedtrkg_tbsegments_Hit7_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit7_ID");
	    timebasedtrkg_tbsegments_Hit8_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit8_ID");
	    timebasedtrkg_tbsegments_Hit9_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.Hit9_ID");
	    timebasedtrkg_tbsegments_ID = event
		    .getInt("TimeBasedTrkg::TBSegments.ID");
	    timebasedtrkg_tbsegments_sector = event
		    .getInt("TimeBasedTrkg::TBSegments.sector");
	    timebasedtrkg_tbsegments_superlayer = event
		    .getInt("TimeBasedTrkg::TBSegments.superlayer");
	} // TimeBasedTrkg::TBSegments

	if (event.hasBank("TimeBasedTrkg::TBTracks")) {
	    timebasedtrkg_tbtracks_C11 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C11");
	    timebasedtrkg_tbtracks_C12 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C12");
	    timebasedtrkg_tbtracks_C13 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C13");
	    timebasedtrkg_tbtracks_C14 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C14");
	    timebasedtrkg_tbtracks_C15 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C15");
	    timebasedtrkg_tbtracks_C21 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C21");
	    timebasedtrkg_tbtracks_C22 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C22");
	    timebasedtrkg_tbtracks_C23 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C23");
	    timebasedtrkg_tbtracks_C24 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C24");
	    timebasedtrkg_tbtracks_C25 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C25");
	    timebasedtrkg_tbtracks_C31 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C31");
	    timebasedtrkg_tbtracks_C32 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C32");
	    timebasedtrkg_tbtracks_C33 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C33");
	    timebasedtrkg_tbtracks_C34 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C34");
	    timebasedtrkg_tbtracks_C35 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C35");
	    timebasedtrkg_tbtracks_c3_ux = event
		    .getDouble("TimeBasedTrkg::TBTracks.c3_ux");
	    timebasedtrkg_tbtracks_c3_uy = event
		    .getDouble("TimeBasedTrkg::TBTracks.c3_uy");
	    timebasedtrkg_tbtracks_c3_uz = event
		    .getDouble("TimeBasedTrkg::TBTracks.c3_uz");
	    timebasedtrkg_tbtracks_c3_x = event
		    .getDouble("TimeBasedTrkg::TBTracks.c3_x");
	    timebasedtrkg_tbtracks_c3_y = event
		    .getDouble("TimeBasedTrkg::TBTracks.c3_y");
	    timebasedtrkg_tbtracks_c3_z = event
		    .getDouble("TimeBasedTrkg::TBTracks.c3_z");
	    timebasedtrkg_tbtracks_C41 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C41");
	    timebasedtrkg_tbtracks_C42 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C42");
	    timebasedtrkg_tbtracks_C43 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C43");
	    timebasedtrkg_tbtracks_C44 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C44");
	    timebasedtrkg_tbtracks_C45 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C45");
	    timebasedtrkg_tbtracks_C51 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C51");
	    timebasedtrkg_tbtracks_C52 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C52");
	    timebasedtrkg_tbtracks_C53 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C53");
	    timebasedtrkg_tbtracks_C54 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C54");
	    timebasedtrkg_tbtracks_C55 = event
		    .getDouble("TimeBasedTrkg::TBTracks.C55");
	    timebasedtrkg_tbtracks_Cross1_ID = event
		    .getInt("TimeBasedTrkg::TBTracks.Cross1_ID");
	    timebasedtrkg_tbtracks_Cross2_ID = event
		    .getInt("TimeBasedTrkg::TBTracks.Cross2_ID");
	    timebasedtrkg_tbtracks_Cross3_ID = event
		    .getInt("TimeBasedTrkg::TBTracks.Cross3_ID");
	    timebasedtrkg_tbtracks_fitChisq = event
		    .getDouble("TimeBasedTrkg::TBTracks.fitChisq");
	    timebasedtrkg_tbtracks_ID = event
		    .getInt("TimeBasedTrkg::TBTracks.ID");
	    timebasedtrkg_tbtracks_p = event
		    .getDouble("TimeBasedTrkg::TBTracks.p");
	    timebasedtrkg_tbtracks_p0_x = event
		    .getDouble("TimeBasedTrkg::TBTracks.p0_x");
	    timebasedtrkg_tbtracks_p0_y = event
		    .getDouble("TimeBasedTrkg::TBTracks.p0_y");
	    timebasedtrkg_tbtracks_p0_z = event
		    .getDouble("TimeBasedTrkg::TBTracks.p0_z");
	    timebasedtrkg_tbtracks_pathlength = event
		    .getDouble("TimeBasedTrkg::TBTracks.pathlength");
	    timebasedtrkg_tbtracks_q = event
		    .getInt("TimeBasedTrkg::TBTracks.q");
	    timebasedtrkg_tbtracks_sector = event
		    .getInt("TimeBasedTrkg::TBTracks.sector");
	    timebasedtrkg_tbtracks_Vtx0_x = event
		    .getDouble("TimeBasedTrkg::TBTracks.Vtx0_x");
	    timebasedtrkg_tbtracks_Vtx0_y = event
		    .getDouble("TimeBasedTrkg::TBTracks.Vtx0_y");
	    timebasedtrkg_tbtracks_Vtx0_z = event
		    .getDouble("TimeBasedTrkg::TBTracks.Vtx0_z");
	} // TimeBasedTrkg::TBTracks

    } // load

    @Override
    public void clear() {
	dc_dgtz_doca = null;
	dc_dgtz_hitn = null;
	dc_dgtz_layer = null;
	dc_dgtz_LR = null;
	dc_dgtz_sdoca = null;
	dc_dgtz_sector = null;
	dc_dgtz_stime = null;
	dc_dgtz_superlayer = null;
	dc_dgtz_time = null;
	dc_dgtz_wire = null;
	dc_true_avgLx = null;
	dc_true_avgLy = null;
	dc_true_avgLz = null;
	dc_true_avgT = null;
	dc_true_avgX = null;
	dc_true_avgY = null;
	dc_true_avgZ = null;
	dc_true_hitn = null;
	dc_true_mpid = null;
	dc_true_mtid = null;
	dc_true_mvx = null;
	dc_true_mvy = null;
	dc_true_mvz = null;
	dc_true_otid = null;
	dc_true_pid = null;
	dc_true_px = null;
	dc_true_py = null;
	dc_true_pz = null;
	dc_true_tid = null;
	dc_true_totEdep = null;
	dc_true_trackE = null;
	dc_true_vx = null;
	dc_true_vy = null;
	dc_true_vz = null;
	hitbasedtrkg_hbclusters_avgWire = null;
	hitbasedtrkg_hbclusters_fitChisqProb = null;
	hitbasedtrkg_hbclusters_fitInterc = null;
	hitbasedtrkg_hbclusters_fitIntercErr = null;
	hitbasedtrkg_hbclusters_fitSlope = null;
	hitbasedtrkg_hbclusters_fitSlopeErr = null;
	hitbasedtrkg_hbclusters_Hit10_ID = null;
	hitbasedtrkg_hbclusters_Hit11_ID = null;
	hitbasedtrkg_hbclusters_Hit12_ID = null;
	hitbasedtrkg_hbclusters_Hit1_ID = null;
	hitbasedtrkg_hbclusters_Hit2_ID = null;
	hitbasedtrkg_hbclusters_Hit3_ID = null;
	hitbasedtrkg_hbclusters_Hit4_ID = null;
	hitbasedtrkg_hbclusters_Hit5_ID = null;
	hitbasedtrkg_hbclusters_Hit6_ID = null;
	hitbasedtrkg_hbclusters_Hit7_ID = null;
	hitbasedtrkg_hbclusters_Hit8_ID = null;
	hitbasedtrkg_hbclusters_Hit9_ID = null;
	hitbasedtrkg_hbclusters_ID = null;
	hitbasedtrkg_hbclusters_sector = null;
	hitbasedtrkg_hbclusters_superlayer = null;
	hitbasedtrkg_hbcrosses_err_ux = null;
	hitbasedtrkg_hbcrosses_err_uy = null;
	hitbasedtrkg_hbcrosses_err_uz = null;
	hitbasedtrkg_hbcrosses_err_x = null;
	hitbasedtrkg_hbcrosses_err_y = null;
	hitbasedtrkg_hbcrosses_err_z = null;
	hitbasedtrkg_hbcrosses_ID = null;
	hitbasedtrkg_hbcrosses_region = null;
	hitbasedtrkg_hbcrosses_sector = null;
	hitbasedtrkg_hbcrosses_Segment1_ID = null;
	hitbasedtrkg_hbcrosses_Segment2_ID = null;
	hitbasedtrkg_hbcrosses_ux = null;
	hitbasedtrkg_hbcrosses_uy = null;
	hitbasedtrkg_hbcrosses_uz = null;
	hitbasedtrkg_hbcrosses_x = null;
	hitbasedtrkg_hbcrosses_y = null;
	hitbasedtrkg_hbcrosses_z = null;
	hitbasedtrkg_hbhits_clusterID = null;
	hitbasedtrkg_hbhits_doca = null;
	hitbasedtrkg_hbhits_id = null;
	hitbasedtrkg_hbhits_layer = null;
	hitbasedtrkg_hbhits_locX = null;
	hitbasedtrkg_hbhits_locY = null;
	hitbasedtrkg_hbhits_LR = null;
	hitbasedtrkg_hbhits_sector = null;
	hitbasedtrkg_hbhits_superlayer = null;
	hitbasedtrkg_hbhits_time = null;
	hitbasedtrkg_hbhits_wire = null;
	hitbasedtrkg_hbhits_X = null;
	hitbasedtrkg_hbhits_Z = null;
	hitbasedtrkg_hbsegments_avgWire = null;
	hitbasedtrkg_hbsegments_Cluster_ID = null;
	hitbasedtrkg_hbsegments_fitChisqProb = null;
	hitbasedtrkg_hbsegments_fitInterc = null;
	hitbasedtrkg_hbsegments_fitIntercErr = null;
	hitbasedtrkg_hbsegments_fitSlope = null;
	hitbasedtrkg_hbsegments_fitSlopeErr = null;
	hitbasedtrkg_hbsegments_Hit10_ID = null;
	hitbasedtrkg_hbsegments_Hit11_ID = null;
	hitbasedtrkg_hbsegments_Hit12_ID = null;
	hitbasedtrkg_hbsegments_Hit1_ID = null;
	hitbasedtrkg_hbsegments_Hit2_ID = null;
	hitbasedtrkg_hbsegments_Hit3_ID = null;
	hitbasedtrkg_hbsegments_Hit4_ID = null;
	hitbasedtrkg_hbsegments_Hit5_ID = null;
	hitbasedtrkg_hbsegments_Hit6_ID = null;
	hitbasedtrkg_hbsegments_Hit7_ID = null;
	hitbasedtrkg_hbsegments_Hit8_ID = null;
	hitbasedtrkg_hbsegments_Hit9_ID = null;
	hitbasedtrkg_hbsegments_ID = null;
	hitbasedtrkg_hbsegments_sector = null;
	hitbasedtrkg_hbsegments_superlayer = null;
	hitbasedtrkg_hbtracks_c3_ux = null;
	hitbasedtrkg_hbtracks_c3_uy = null;
	hitbasedtrkg_hbtracks_c3_uz = null;
	hitbasedtrkg_hbtracks_c3_x = null;
	hitbasedtrkg_hbtracks_c3_y = null;
	hitbasedtrkg_hbtracks_c3_z = null;
	hitbasedtrkg_hbtracks_Cross1_ID = null;
	hitbasedtrkg_hbtracks_Cross2_ID = null;
	hitbasedtrkg_hbtracks_Cross3_ID = null;
	hitbasedtrkg_hbtracks_ID = null;
	hitbasedtrkg_hbtracks_p = null;
	hitbasedtrkg_hbtracks_p0_x = null;
	hitbasedtrkg_hbtracks_p0_y = null;
	hitbasedtrkg_hbtracks_p0_z = null;
	hitbasedtrkg_hbtracks_pathlength = null;
	hitbasedtrkg_hbtracks_q = null;
	hitbasedtrkg_hbtracks_sector = null;
	hitbasedtrkg_hbtracks_Vtx0_x = null;
	hitbasedtrkg_hbtracks_Vtx0_y = null;
	hitbasedtrkg_hbtracks_Vtx0_z = null;
	timebasedtrkg_tbclusters_avgWire = null;
	timebasedtrkg_tbclusters_fitChisqProb = null;
	timebasedtrkg_tbclusters_fitInterc = null;
	timebasedtrkg_tbclusters_fitIntercErr = null;
	timebasedtrkg_tbclusters_fitSlope = null;
	timebasedtrkg_tbclusters_fitSlopeErr = null;
	timebasedtrkg_tbclusters_Hit10_ID = null;
	timebasedtrkg_tbclusters_Hit11_ID = null;
	timebasedtrkg_tbclusters_Hit12_ID = null;
	timebasedtrkg_tbclusters_Hit1_ID = null;
	timebasedtrkg_tbclusters_Hit2_ID = null;
	timebasedtrkg_tbclusters_Hit3_ID = null;
	timebasedtrkg_tbclusters_Hit4_ID = null;
	timebasedtrkg_tbclusters_Hit5_ID = null;
	timebasedtrkg_tbclusters_Hit6_ID = null;
	timebasedtrkg_tbclusters_Hit7_ID = null;
	timebasedtrkg_tbclusters_Hit8_ID = null;
	timebasedtrkg_tbclusters_Hit9_ID = null;
	timebasedtrkg_tbclusters_ID = null;
	timebasedtrkg_tbclusters_sector = null;
	timebasedtrkg_tbclusters_superlayer = null;
	timebasedtrkg_tbcrosses_err_ux = null;
	timebasedtrkg_tbcrosses_err_uy = null;
	timebasedtrkg_tbcrosses_err_uz = null;
	timebasedtrkg_tbcrosses_err_x = null;
	timebasedtrkg_tbcrosses_err_y = null;
	timebasedtrkg_tbcrosses_err_z = null;
	timebasedtrkg_tbcrosses_ID = null;
	timebasedtrkg_tbcrosses_region = null;
	timebasedtrkg_tbcrosses_sector = null;
	timebasedtrkg_tbcrosses_Segment1_ID = null;
	timebasedtrkg_tbcrosses_Segment2_ID = null;
	timebasedtrkg_tbcrosses_ux = null;
	timebasedtrkg_tbcrosses_uy = null;
	timebasedtrkg_tbcrosses_uz = null;
	timebasedtrkg_tbcrosses_x = null;
	timebasedtrkg_tbcrosses_y = null;
	timebasedtrkg_tbcrosses_z = null;
	timebasedtrkg_tbhits_clusterID = null;
	timebasedtrkg_tbhits_doca = null;
	timebasedtrkg_tbhits_id = null;
	timebasedtrkg_tbhits_layer = null;
	timebasedtrkg_tbhits_LR = null;
	timebasedtrkg_tbhits_sector = null;
	timebasedtrkg_tbhits_superlayer = null;
	timebasedtrkg_tbhits_time = null;
	timebasedtrkg_tbhits_timeResidual = null;
	timebasedtrkg_tbhits_wire = null;
	timebasedtrkg_tbhits_X = null;
	timebasedtrkg_tbhits_Z = null;
	timebasedtrkg_tbsegments_avgWire = null;
	timebasedtrkg_tbsegments_Cluster_ID = null;
	timebasedtrkg_tbsegments_fitChisqProb = null;
	timebasedtrkg_tbsegments_fitInterc = null;
	timebasedtrkg_tbsegments_fitIntercErr = null;
	timebasedtrkg_tbsegments_fitSlope = null;
	timebasedtrkg_tbsegments_fitSlopeErr = null;
	timebasedtrkg_tbsegments_Hit10_ID = null;
	timebasedtrkg_tbsegments_Hit11_ID = null;
	timebasedtrkg_tbsegments_Hit12_ID = null;
	timebasedtrkg_tbsegments_Hit1_ID = null;
	timebasedtrkg_tbsegments_Hit2_ID = null;
	timebasedtrkg_tbsegments_Hit3_ID = null;
	timebasedtrkg_tbsegments_Hit4_ID = null;
	timebasedtrkg_tbsegments_Hit5_ID = null;
	timebasedtrkg_tbsegments_Hit6_ID = null;
	timebasedtrkg_tbsegments_Hit7_ID = null;
	timebasedtrkg_tbsegments_Hit8_ID = null;
	timebasedtrkg_tbsegments_Hit9_ID = null;
	timebasedtrkg_tbsegments_ID = null;
	timebasedtrkg_tbsegments_sector = null;
	timebasedtrkg_tbsegments_superlayer = null;
	timebasedtrkg_tbtracks_C11 = null;
	timebasedtrkg_tbtracks_C12 = null;
	timebasedtrkg_tbtracks_C13 = null;
	timebasedtrkg_tbtracks_C14 = null;
	timebasedtrkg_tbtracks_C15 = null;
	timebasedtrkg_tbtracks_C21 = null;
	timebasedtrkg_tbtracks_C22 = null;
	timebasedtrkg_tbtracks_C23 = null;
	timebasedtrkg_tbtracks_C24 = null;
	timebasedtrkg_tbtracks_C25 = null;
	timebasedtrkg_tbtracks_C31 = null;
	timebasedtrkg_tbtracks_C32 = null;
	timebasedtrkg_tbtracks_C33 = null;
	timebasedtrkg_tbtracks_C34 = null;
	timebasedtrkg_tbtracks_C35 = null;
	timebasedtrkg_tbtracks_c3_ux = null;
	timebasedtrkg_tbtracks_c3_uy = null;
	timebasedtrkg_tbtracks_c3_uz = null;
	timebasedtrkg_tbtracks_c3_x = null;
	timebasedtrkg_tbtracks_c3_y = null;
	timebasedtrkg_tbtracks_c3_z = null;
	timebasedtrkg_tbtracks_C41 = null;
	timebasedtrkg_tbtracks_C42 = null;
	timebasedtrkg_tbtracks_C43 = null;
	timebasedtrkg_tbtracks_C44 = null;
	timebasedtrkg_tbtracks_C45 = null;
	timebasedtrkg_tbtracks_C51 = null;
	timebasedtrkg_tbtracks_C52 = null;
	timebasedtrkg_tbtracks_C53 = null;
	timebasedtrkg_tbtracks_C54 = null;
	timebasedtrkg_tbtracks_C55 = null;
	timebasedtrkg_tbtracks_Cross1_ID = null;
	timebasedtrkg_tbtracks_Cross2_ID = null;
	timebasedtrkg_tbtracks_Cross3_ID = null;
	timebasedtrkg_tbtracks_fitChisq = null;
	timebasedtrkg_tbtracks_ID = null;
	timebasedtrkg_tbtracks_p = null;
	timebasedtrkg_tbtracks_p0_x = null;
	timebasedtrkg_tbtracks_p0_y = null;
	timebasedtrkg_tbtracks_p0_z = null;
	timebasedtrkg_tbtracks_pathlength = null;
	timebasedtrkg_tbtracks_q = null;
	timebasedtrkg_tbtracks_sector = null;
	timebasedtrkg_tbtracks_Vtx0_x = null;
	timebasedtrkg_tbtracks_Vtx0_y = null;
	timebasedtrkg_tbtracks_Vtx0_z = null;
    } // clear

    /**
     * Get the index of the dc hit
     * 
     * @param sect
     *            the 1-based sector
     * @param supl
     *            the 1-based superlayer
     * @param lay
     *            the 1-based layer
     * @param wireid
     *            the 1-based wire
     * @return the index of a hit with these parameters, or -1 if not found
     */
    public int getHitIndex(int sect, int supl, int lay, int wireid) {

	for (int i = 0; i < getHitCount(0); i++) {
	    if ((sect == dc_dgtz_sector[i]) && (supl == dc_dgtz_superlayer[i])
		    && (lay == dc_dgtz_layer[i]) && (wireid == dc_dgtz_wire[i])) {
		return i;
	    }
	}
	return -1;
    }

    @Override
    public void addPreliminaryFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
	// noise guess
	boolean noise[] = NoiseManager.getInstance().getNoise();
	String noiseStr = prelimColor
		+ "Noise Hit Guess: "
		+ (((noise != null) && noise[hitIndex]) ? "noise" : "not noise");
	feedbackStrings.add(noiseStr);

    }

    @Override
    public void addTrueFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {

	if ((dc_true_trackE != null) && (dc_true_trackE.length > hitIndex)) {
	    double etrack = dc_true_trackE[hitIndex] / 1000; // to gev
	    feedbackStrings.add(trueColor + "true energy "
		    + DoubleFormat.doubleFormat(etrack, 2) + " GeV");
	}
    }

    @Override
    public void addDgtzFeedback(int hitIndex, int option,
	    List<String> feedbackStrings) {
	feedbackStrings.add(dgtxColor + "sector " + dc_dgtz_sector[hitIndex]
		+ "  superlayer " + dc_dgtz_superlayer[hitIndex] + "  layer "
		+ dc_dgtz_layer[hitIndex] + "  wire " + dc_dgtz_wire[hitIndex]);

	String lraStr = safeString(dc_dgtz_LR, hitIndex);
	String hitStr = safeString(dc_dgtz_hitn, hitIndex);
	String docaStr = safeString(dc_dgtz_doca, hitIndex, 1);
	String timeStr = safeString(dc_dgtz_time, hitIndex, 1);
	String sdocaStr = safeString(dc_dgtz_sdoca, hitIndex, 1);
	String stimeStr = safeString(dc_dgtz_stime, hitIndex, 1);

	feedbackStrings.add(dgtxColor + "LRA " + lraStr + "  hit " + hitStr);
	feedbackStrings.add(dgtxColor + "doca " + docaStr + "  sdoca "
		+ sdocaStr + " mm");
	feedbackStrings.add(dgtxColor + "time " + timeStr + "  stime "
		+ stimeStr + " ns");
    }

    @Override
    public void addFinalFeedback(int option, List<String> feedbackStrings) {
    }

    @Override
    public void addReconstructedFeedback(int option,
	    List<String> feedbackStrings) {
	int reconTrackCount = getTimeBasedTrackCount();
	if (reconTrackCount > 0) {
	    feedbackStrings.add(reconColor + "TB #reconstructed tracks "
		    + reconTrackCount);
	    for (int i = 0; i < reconTrackCount; i++) {
		feedbackStrings
			.add(reconColor + "TB trk# " + (i + 1) + " recon p "
				+ safeString(timebasedtrkg_tbtracks_p, i, 5)
				+ " Gev/c");
	    }
	}
    }

    /**
     * Get the number of hits in the hit based reconstruction
     * 
     * @return he number of hits in the hit based reconstruction
     */
    public int getHitBasedHitCount() {
	return (hitbasedtrkg_hbhits_sector == null) ? 0
		: hitbasedtrkg_hbhits_sector.length;
    }

    /**
     * Get the number of hit based crosses
     * 
     * @return he number of hit based crosses
     */
    public int getHitBasedCrossCount() {
	return (hitbasedtrkg_hbcrosses_sector == null) ? 0
		: hitbasedtrkg_hbcrosses_sector.length;
    }

    /**
     * Get the number of hits in the time based reconstruction
     * 
     * @return he number of hits in the time based reconstruction
     */
    public int getTimeBasedHitCount() {
	return (timebasedtrkg_tbhits_sector == null) ? 0
		: timebasedtrkg_tbhits_sector.length;
    }

    /**
     * Get the number of time based crosses
     * 
     * @return the number of time based crosses
     */
    public int getTimeBasedCrossCount() {
	return (timebasedtrkg_tbcrosses_sector == null) ? 0
		: timebasedtrkg_tbcrosses_sector.length;
    }

    /**
     * Get the number of time based tracks
     * 
     * @return the number of time based tracks
     */
    public int getTimeBasedTrackCount() {
	return (timebasedtrkg_tbtracks_sector == null) ? 0
		: timebasedtrkg_tbtracks_sector.length;
    }

    /**
     * Get the number of hit based based tracks
     * 
     * @return the number of hit based tracks
     */
    public int getHitBasedTrackCount() {
	return (hitbasedtrkg_hbtracks_sector == null) ? 0
		: hitbasedtrkg_hbtracks_sector.length;
    }

    @Override
    public void finalEventPrep(EvioDataEvent event) {
	extractUniqueLundIds(dc_true_pid);
    }

}
