package cnuphys.ced.event.data;

import java.util.List;

import org.jlab.clas.detector.DetectorResponse;
import org.jlab.io.base.DataEvent;

import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.noise.NoiseManager;
import cnuphys.splot.plot.DoubleFormat;

/**
 * static methods to centralize getting data arrays related to DC
 * @author heddle
 *
 */
public class DC {
	


	//id of the hit
	  static public short[] get_id_HBHits() {return ColumnData.getShortArray("HitBasedTrkg::HBHits.id");}

	//id of the hit
	  static public short[] get_status_HBHits() {return ColumnData.getShortArray("HitBasedTrkg::HBHits.status");}

	//DC sector
	  static public byte[] get_sector_HBHits() {return ColumnData.getByteArray("HitBasedTrkg::HBHits.sector");}

	//DC superlayer (1...6)
	  static public byte[] get_superlayer_HBHits() {return ColumnData.getByteArray("HitBasedTrkg::HBHits.superlayer");}

	//DC layer in superlayer (1...6)
	  static public byte[] get_layer_HBHits() {return ColumnData.getByteArray("HitBasedTrkg::HBHits.layer");}

	//wire id of DC
	  static public short[] get_wire_HBHits() {return ColumnData.getShortArray("HitBasedTrkg::HBHits.wire");}

	//raw time of the hit
	  static public float[] get_time_HBHits() {return ColumnData.getFloatArray("HitBasedTrkg::HBHits.time");}

	//track doca of the hit (in cm)
	  static public float[] get_trkDoca_HBHits() {return ColumnData.getFloatArray("HitBasedTrkg::HBHits.trkDoca");}

	//error on track doca of the hit
	  static public float[] get_docaError_HBHits() {return ColumnData.getFloatArray("HitBasedTrkg::HBHits.docaError");}

	//Left/Right ambiguity of the hit
	  static public byte[] get_LR_HBHits() {return ColumnData.getByteArray("HitBasedTrkg::HBHits.LR");}

	//x in planar local coordinate system
	  static public float[] get_LocX_HBHits() {return ColumnData.getFloatArray("HitBasedTrkg::HBHits.LocX");}

	//y in planar local coordinate system
	  static public float[] get_LocY_HBHits() {return ColumnData.getFloatArray("HitBasedTrkg::HBHits.LocY");}

	//wire x-coordinate  in tilted-sector
	  static public float[] get_X_HBHits() {return ColumnData.getFloatArray("HitBasedTrkg::HBHits.X");}

	//wire z-coordinate  in tilted-sector
	  static public float[] get_Z_HBHits() {return ColumnData.getFloatArray("HitBasedTrkg::HBHits.Z");}

	//B-field intensity at hit position in tilted-sector system
	  static public float[] get_B_HBHits() {return ColumnData.getFloatArray("HitBasedTrkg::HBHits.B");}

	//ID of associated cluster
	  static public short[] get_clusterID_HBHits() {return ColumnData.getShortArray("HitBasedTrkg::HBHits.clusterID");}

	//ID of associated track
	  static public byte[] get_trkID_HBHits() {return ColumnData.getByteArray("HitBasedTrkg::HBHits.trkID");}

	//id of the cluster
	  static public short[] get_id_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.id");}

	//status of the cluster
	  static public short[] get_status_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.status");}

	//sector of the cluster
	  static public byte[] get_sector_HBClusters() {return ColumnData.getByteArray("HitBasedTrkg::HBClusters.sector");}

	//superlayer of the cluster
	  static public byte[] get_superlayer_HBClusters() {return ColumnData.getByteArray("HitBasedTrkg::HBClusters.superlayer");}

	//id of hit1 in cluster
	  static public short[] get_Hit1_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit1_ID");}

	//id of hit2 in cluster
	  static public short[] get_Hit2_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit2_ID");}

	//id of hit3 in cluster
	  static public short[] get_Hit3_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit3_ID");}

	//id of hit4 in cluster
	  static public short[] get_Hit4_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit4_ID");}

	//id of hit5 in cluster
	  static public short[] get_Hit5_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit5_ID");}

	//id of hit6 in cluster
	  static public short[] get_Hit6_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit6_ID");}

	//id of hit7 in cluster
	  static public short[] get_Hit7_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit7_ID");}

	//id of hit8 in cluster
	  static public short[] get_Hit8_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit8_ID");}

	//id of hit9 in cluster
	  static public short[] get_Hit9_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit9_ID");}

	//id of hit10 in cluster
	  static public short[] get_Hit10_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit10_ID");}

	//id of hit11 in cluster
	  static public short[] get_Hit11_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit11_ID");}

	//id of hit12 in cluster
	  static public short[] get_Hit12_ID_HBClusters() {return ColumnData.getShortArray("HitBasedTrkg::HBClusters.Hit12_ID");}

	//average wire number
	  static public float[] get_avgWire_HBClusters() {return ColumnData.getFloatArray("HitBasedTrkg::HBClusters.avgWire");}

	//fit chisq prob.
	  static public float[] get_fitChisqProb_HBClusters() {return ColumnData.getFloatArray("HitBasedTrkg::HBClusters.fitChisqProb");}

	//line fit slope
	  static public float[] get_fitSlope_HBClusters() {return ColumnData.getFloatArray("HitBasedTrkg::HBClusters.fitSlope");}

	//error on slope
	  static public float[] get_fitSlopeErr_HBClusters() {return ColumnData.getFloatArray("HitBasedTrkg::HBClusters.fitSlopeErr");}

	//line fit intercept
	  static public float[] get_fitInterc_HBClusters() {return ColumnData.getFloatArray("HitBasedTrkg::HBClusters.fitInterc");}

	//error on the intercept
	  static public float[] get_fitIntercErr_HBClusters() {return ColumnData.getFloatArray("HitBasedTrkg::HBClusters.fitIntercErr");}

	//cluster size
	  static public byte[] get_size_HBClusters() {return ColumnData.getByteArray("HitBasedTrkg::HBClusters.size");}

	//id of the segment
	  static public short[] get_id_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.id");}

	//status of the segment
	  static public short[] get_status_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.status");}

	//sector of the segment
	  static public byte[] get_sector_HBSegments() {return ColumnData.getByteArray("HitBasedTrkg::HBSegments.sector");}

	//superlayer of superlayer
	  static public byte[] get_superlayer_HBSegments() {return ColumnData.getByteArray("HitBasedTrkg::HBSegments.superlayer");}

	//associated cluster id
	  static public short[] get_Cluster_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Cluster_ID");}

	//id of hit1 in cluster
	  static public short[] get_Hit1_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit1_ID");}

	//id of hit2 in cluster
	  static public short[] get_Hit2_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit2_ID");}

	//id of hit3 in cluster
	  static public short[] get_Hit3_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit3_ID");}

	//id of hit4 in cluster
	  static public short[] get_Hit4_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit4_ID");}

	//id of hit5 in cluster
	  static public short[] get_Hit5_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit5_ID");}

	//id of hit6 in cluster
	  static public short[] get_Hit6_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit6_ID");}

	//id of hit7 in cluster
	  static public short[] get_Hit7_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit7_ID");}

	//id of hit8 in cluster
	  static public short[] get_Hit8_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit8_ID");}

	//id of hit9 in cluster
	  static public short[] get_Hit9_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit9_ID");}

	//id of hit10 in cluster
	  static public short[] get_Hit10_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit10_ID");}

	//id of hit11 in cluster
	  static public short[] get_Hit11_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit11_ID");}

	//id of hit12 in cluster
	  static public short[] get_Hit12_ID_HBSegments() {return ColumnData.getShortArray("HitBasedTrkg::HBSegments.Hit12_ID");}

	//average wire number
	  static public float[] get_avgWire_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.avgWire");}

	//fit chisq prob.
	  static public float[] get_fitChisqProb_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.fitChisqProb");}

	//line fit slope
	  static public float[] get_fitSlope_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.fitSlope");}

	//error on slope
	  static public float[] get_fitSlopeErr_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.fitSlopeErr");}

	//line fit intercept
	  static public float[] get_fitInterc_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.fitInterc");}

	//error on the intercept
	  static public float[] get_fitIntercErr_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.fitIntercErr");}

	//Segment 1st endpoint x coordinate in the sector coordinate system (for ced display)
	  static public float[] get_SegEndPoint1X_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.SegEndPoint1X");}

	//Segment 1st endpoint z coordinate in the sector coordinate system (for ced display)
	  static public float[] get_SegEndPoint1Z_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.SegEndPoint1Z");}

	//Segment 2nd endpoint x coordinate in the sector coordinate system (for ced display)
	  static public float[] get_SegEndPoint2X_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.SegEndPoint2X");}

	//Segment 2nd endpoint z coordinate in the sector coordinate system (for ced display)
	  static public float[] get_SegEndPoint2Z_HBSegments() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegments.SegEndPoint2Z");}

	//size of segment
	  static public byte[] get_size_HBSegments() {return ColumnData.getByteArray("HitBasedTrkg::HBSegments.size");}

	//id of the segment
	  static public short[] get_segmentID_HBSegmentTrajectory() {return ColumnData.getShortArray("HitBasedTrkg::HBSegmentTrajectory.segmentID");}

	//sector of FTOF
	  static public byte[] get_sector_HBSegmentTrajectory() {return ColumnData.getByteArray("HitBasedTrkg::HBSegmentTrajectory.sector");}

	//superlayer
	  static public byte[] get_superlayer_HBSegmentTrajectory() {return ColumnData.getByteArray("HitBasedTrkg::HBSegmentTrajectory.superlayer");}

	//layer
	  static public byte[] get_layer_HBSegmentTrajectory() {return ColumnData.getByteArray("HitBasedTrkg::HBSegmentTrajectory.layer");}

	//matched hit id
	  static public short[] get_matchedHitID_HBSegmentTrajectory() {return ColumnData.getShortArray("HitBasedTrkg::HBSegmentTrajectory.matchedHitID");}

	//calculated track doca
	  static public float[] get_trkDoca_HBSegmentTrajectory() {return ColumnData.getFloatArray("HitBasedTrkg::HBSegmentTrajectory.trkDoca");}

	//id of the cross
	  static public short[] get_id_HBCrosses() {return ColumnData.getShortArray("HitBasedTrkg::HBCrosses.id");}

	//status of the cross
	  static public short[] get_status_HBCrosses() {return ColumnData.getShortArray("HitBasedTrkg::HBCrosses.status");}

	//sector of the cross
	  static public byte[] get_sector_HBCrosses() {return ColumnData.getByteArray("HitBasedTrkg::HBCrosses.sector");}

	//region of the cross
	  static public byte[] get_region_HBCrosses() {return ColumnData.getByteArray("HitBasedTrkg::HBCrosses.region");}

	//DC track cross x-coordinate (in the DC tilted sector coordinate system)
	  static public float[] get_x_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.x");}

	//DC track cross y-coordinate (in the DC tilted sector coordinate system)
	  static public float[] get_y_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.y");}

	//DC track cross z-coordinate (in the DC tilted sector coordinate system)
	  static public float[] get_z_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.z");}

	//DC track cross x-coordinate uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_x_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.err_x");}

	//DC track cross y-coordinate uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_y_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.err_y");}

	//DC track cross z-coordinate uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_z_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.err_z");}

	//DC track cross x-direction (in the DC tilted sector coordinate system)
	  static public float[] get_ux_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.ux");}

	//DC track cross y-direction (in the DC tilted sector coordinate system)
	  static public float[] get_uy_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.uy");}

	//DC track cross z-direction (in the DC tilted sector coordinate system)
	  static public float[] get_uz_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.uz");}

	//DC track cross x-direction uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_ux_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.err_ux");}

	//DC track cross y-direction uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_uy_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.err_uy");}

	//DC track cross z-direction uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_uz_HBCrosses() {return ColumnData.getFloatArray("HitBasedTrkg::HBCrosses.err_uz");}

	//id of first superlater used in segment
	  static public short[] get_Segment1_ID_HBCrosses() {return ColumnData.getShortArray("HitBasedTrkg::HBCrosses.Segment1_ID");}

	//id of second superlater used in segment
	  static public short[] get_Segment2_ID_HBCrosses() {return ColumnData.getShortArray("HitBasedTrkg::HBCrosses.Segment2_ID");}

	//id of the track
	  static public short[] get_id_HBTracks() {return ColumnData.getShortArray("HitBasedTrkg::HBTracks.id");}

	//status of the track
	  static public short[] get_status_HBTracks() {return ColumnData.getShortArray("HitBasedTrkg::HBTracks.status");}

	//sector of the track
	  static public byte[] get_sector_HBTracks() {return ColumnData.getByteArray("HitBasedTrkg::HBTracks.sector");}

	//Upstream Region 1 cross x-position in the lab (in cm)
	  static public float[] get_c1_x_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c1_x");}

	//Upstream Region 1 cross y-position in the lab (in cm)
	  static public float[] get_c1_y_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c1_y");}

	//Upstream Region 1 cross z-position in the lab (in cm)
	  static public float[] get_c1_z_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c1_z");}

	//Upstream Region 1 cross unit x-direction vector in the lab
	  static public float[] get_c1_ux_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c1_ux");}

	//Upstream Region 1 cross unit y-direction vector in the lab
	  static public float[] get_c1_uy_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c1_uy");}

	//Upstream Region 1 cross unit z-direction vector in the lab
	  static public float[] get_c1_uz_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c1_uz");}

	//Downstream Region 3 cross x-position in the lab (in cm)
	  static public float[] get_c3_x_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c3_x");}

	//Downstream Region 3 cross y-position in the lab (in cm)
	  static public float[] get_c3_y_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c3_y");}

	//Downstream Region 3 cross z-position in the lab (in cm)
	  static public float[] get_c3_z_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c3_z");}

	//Downstream Region 3 cross unit x-direction vector in the lab
	  static public float[] get_c3_ux_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c3_ux");}

	//Downstream Region 3 cross unit y-direction vector in the lab
	  static public float[] get_c3_uy_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c3_uy");}

	//Downstream Region 3 cross unit z-direction vector in the lab
	  static public float[] get_c3_uz_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.c3_uz");}

	//Upstream Region 1 track x-position in the lab (in cm)
	  static public float[] get_t1_x_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.t1_x");}

	//Upstream Region 1 track y-position in the lab (in cm)
	  static public float[] get_t1_y_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.t1_y");}

	//Upstream Region 1 track z-position in the lab (in cm)
	  static public float[] get_t1_z_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.t1_z");}

	//Upstream Region 1 track unit x-momentum vector in the lab
	  static public float[] get_t1_px_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.t1_px");}

	//Upstream Region 1 track unit y-momentum vector in the lab
	  static public float[] get_t1_py_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.t1_py");}

	//Upstream Region 1 track unit z-momentum vector in the lab
	  static public float[] get_t1_pz_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.t1_pz");}

	//Vertex x-position of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_Vtx0_x_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.Vtx0_x");}

	//Vertex y-position of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_Vtx0_y_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.Vtx0_y");}

	//Vertex z-position of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_Vtx0_z_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.Vtx0_z");}

	//3-momentum x-coordinate of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_p0_x_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.p0_x");}

	//3-momentum y-coordinate of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_p0_y_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.p0_y");}

	//3-momentum z-coordinate of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_p0_z_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.p0_z");}

	//id of first cross on track
	  static public short[] get_Cross1_ID_HBTracks() {return ColumnData.getShortArray("HitBasedTrkg::HBTracks.Cross1_ID");}

	//id of second cross on track
	  static public short[] get_Cross2_ID_HBTracks() {return ColumnData.getShortArray("HitBasedTrkg::HBTracks.Cross2_ID");}

	//id of third cross on track
	  static public short[] get_Cross3_ID_HBTracks() {return ColumnData.getShortArray("HitBasedTrkg::HBTracks.Cross3_ID");}

	//charge of the track
	  static public byte[] get_q_HBTracks() {return ColumnData.getByteArray("HitBasedTrkg::HBTracks.q");}

	//pathlength of the track
	  static public float[] get_pathlength_HBTracks() {return ColumnData.getFloatArray("HitBasedTrkg::HBTracks.pathlength");}

	//id of the hit
	  static public short[] get_id_TBHits() {return ColumnData.getShortArray("TimeBasedTrkg::TBHits.id");}

	//id of the hit
	  static public short[] get_status_TBHits() {return ColumnData.getShortArray("TimeBasedTrkg::TBHits.status");}

	//DC sector
	  static public byte[] get_sector_TBHits() {return ColumnData.getByteArray("TimeBasedTrkg::TBHits.sector");}

	//DC superlayer (1...6)
	  static public byte[] get_superlayer_TBHits() {return ColumnData.getByteArray("TimeBasedTrkg::TBHits.superlayer");}

	//DC layer in superlayer (1...6)
	  static public byte[] get_layer_TBHits() {return ColumnData.getByteArray("TimeBasedTrkg::TBHits.layer");}

	//wire id of DC
	  static public short[] get_wire_TBHits() {return ColumnData.getShortArray("TimeBasedTrkg::TBHits.wire");}

	//raw time of the hit
	  static public float[] get_time_TBHits() {return ColumnData.getFloatArray("TimeBasedTrkg::TBHits.time");}

	//doca of the hit calculated from TDC (in cm)
	  static public float[] get_doca_TBHits() {return ColumnData.getFloatArray("TimeBasedTrkg::TBHits.doca");}

	//uncertainty on doca of the hit calculated from TDC (in cm)
	  static public float[] get_docaError_TBHits() {return ColumnData.getFloatArray("TimeBasedTrkg::TBHits.docaError");}

	//track doca of the hit (in cm)
	  static public float[] get_trkDoca_TBHits() {return ColumnData.getFloatArray("TimeBasedTrkg::TBHits.trkDoca");}

	//time residual of the hit (in cm)
	  static public float[] get_timeResidual_TBHits() {return ColumnData.getFloatArray("TimeBasedTrkg::TBHits.timeResidual");}

	//Left/Right ambiguity of the hit
	  static public byte[] get_LR_TBHits() {return ColumnData.getByteArray("TimeBasedTrkg::TBHits.LR");}

	//wire x-coordinate  in tilted-sector
	  static public float[] get_X_TBHits() {return ColumnData.getFloatArray("TimeBasedTrkg::TBHits.X");}

	//wire z-coordinate  in tilted-sector
	  static public float[] get_Z_TBHits() {return ColumnData.getFloatArray("TimeBasedTrkg::TBHits.Z");}

	//B-field intensity at hit position in tilted-sector system
	  static public float[] get_B_TBHits() {return ColumnData.getFloatArray("TimeBasedTrkg::TBHits.B");}

	//ID of associated cluster
	  static public short[] get_clusterID_TBHits() {return ColumnData.getShortArray("TimeBasedTrkg::TBHits.clusterID");}

	//ID of associated track
	  static public byte[] get_trkID_TBHits() {return ColumnData.getByteArray("TimeBasedTrkg::TBHits.trkID");}

	//id of the cluster
	  static public short[] get_id_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.id");}

	//status of the cluster
	  static public short[] get_status_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.status");}

	//sector of the cluster
	  static public byte[] get_sector_TBClusters() {return ColumnData.getByteArray("TimeBasedTrkg::TBClusters.sector");}

	//superlayer of the cluster
	  static public byte[] get_superlayer_TBClusters() {return ColumnData.getByteArray("TimeBasedTrkg::TBClusters.superlayer");}

	//id of hit1 in cluster
	  static public short[] get_Hit1_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit1_ID");}

	//id of hit2 in cluster
	  static public short[] get_Hit2_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit2_ID");}

	//id of hit3 in cluster
	  static public short[] get_Hit3_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit3_ID");}

	//id of hit4 in cluster
	  static public short[] get_Hit4_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit4_ID");}

	//id of hit5 in cluster
	  static public short[] get_Hit5_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit5_ID");}

	//id of hit6 in cluster
	  static public short[] get_Hit6_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit6_ID");}

	//id of hit7 in cluster
	  static public short[] get_Hit7_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit7_ID");}

	//id of hit8 in cluster
	  static public short[] get_Hit8_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit8_ID");}

	//id of hit9 in cluster
	  static public short[] get_Hit9_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit9_ID");}

	//id of hit10 in cluster
	  static public short[] get_Hit10_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit10_ID");}

	//id of hit11 in cluster
	  static public short[] get_Hit11_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit11_ID");}

	//id of hit12 in cluster
	  static public short[] get_Hit12_ID_TBClusters() {return ColumnData.getShortArray("TimeBasedTrkg::TBClusters.Hit12_ID");}

	//average wire number
	  static public float[] get_avgWire_TBClusters() {return ColumnData.getFloatArray("TimeBasedTrkg::TBClusters.avgWire");}

	//fit chisq prob.
	  static public float[] get_fitChisqProb_TBClusters() {return ColumnData.getFloatArray("TimeBasedTrkg::TBClusters.fitChisqProb");}

	//line fit slope
	  static public float[] get_fitSlope_TBClusters() {return ColumnData.getFloatArray("TimeBasedTrkg::TBClusters.fitSlope");}

	//error on slope
	  static public float[] get_fitSlopeErr_TBClusters() {return ColumnData.getFloatArray("TimeBasedTrkg::TBClusters.fitSlopeErr");}

	//line fit intercept
	  static public float[] get_fitInterc_TBClusters() {return ColumnData.getFloatArray("TimeBasedTrkg::TBClusters.fitInterc");}

	//error on the intercept
	  static public float[] get_fitIntercErr_TBClusters() {return ColumnData.getFloatArray("TimeBasedTrkg::TBClusters.fitIntercErr");}

	//cluster size
	  static public byte[] get_size_TBClusters() {return ColumnData.getByteArray("TimeBasedTrkg::TBClusters.size");}

	//id of the segment
	  static public short[] get_id_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.id");}

	//status of the segment
	  static public short[] get_status_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.status");}

	//sector of the segment
	  static public byte[] get_sector_TBSegments() {return ColumnData.getByteArray("TimeBasedTrkg::TBSegments.sector");}

	//superlayer of superlayer
	  static public byte[] get_superlayer_TBSegments() {return ColumnData.getByteArray("TimeBasedTrkg::TBSegments.superlayer");}

	//associated cluster id
	  static public short[] get_Cluster_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Cluster_ID");}

	//id of hit1 in cluster
	  static public short[] get_Hit1_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit1_ID");}

	//id of hit2 in cluster
	  static public short[] get_Hit2_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit2_ID");}

	//id of hit3 in cluster
	  static public short[] get_Hit3_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit3_ID");}

	//id of hit4 in cluster
	  static public short[] get_Hit4_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit4_ID");}

	//id of hit5 in cluster
	  static public short[] get_Hit5_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit5_ID");}

	//id of hit6 in cluster
	  static public short[] get_Hit6_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit6_ID");}

	//id of hit7 in cluster
	  static public short[] get_Hit7_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit7_ID");}

	//id of hit8 in cluster
	  static public short[] get_Hit8_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit8_ID");}

	//id of hit9 in cluster
	  static public short[] get_Hit9_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit9_ID");}

	//id of hit10 in cluster
	  static public short[] get_Hit10_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit10_ID");}

	//id of hit11 in cluster
	  static public short[] get_Hit11_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit11_ID");}

	//id of hit12 in cluster
	  static public short[] get_Hit12_ID_TBSegments() {return ColumnData.getShortArray("TimeBasedTrkg::TBSegments.Hit12_ID");}

	//average wire number
	  static public float[] get_avgWire_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.avgWire");}

	//fit chisq prob.
	  static public float[] get_fitChisqProb_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.fitChisqProb");}

	//line fit slope
	  static public float[] get_fitSlope_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.fitSlope");}

	//error on slope
	  static public float[] get_fitSlopeErr_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.fitSlopeErr");}

	//line fit intercept
	  static public float[] get_fitInterc_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.fitInterc");}

	//error on the intercept
	  static public float[] get_fitIntercErr_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.fitIntercErr");}

	//Segment 1st endpoint x coordinate in the sector coordinate system (for ced display)
	  static public float[] get_SegEndPoint1X_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.SegEndPoint1X");}

	//Segment 1st endpoint z coordinate in the sector coordinate system (for ced display)
	  static public float[] get_SegEndPoint1Z_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.SegEndPoint1Z");}

	//Segment 2nd endpoint x coordinate in the sector coordinate system (for ced display)
	  static public float[] get_SegEndPoint2X_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.SegEndPoint2X");}

	//Segment 2nd endpoint z coordinate in the sector coordinate system (for ced display)
	  static public float[] get_SegEndPoint2Z_TBSegments() {return ColumnData.getFloatArray("TimeBasedTrkg::TBSegments.SegEndPoint2Z");}

	//size of segment
	  static public byte[] get_size_TBSegments() {return ColumnData.getByteArray("TimeBasedTrkg::TBSegments.size");}

	//id of the cross
	  static public short[] get_id_TBCrosses() {return ColumnData.getShortArray("TimeBasedTrkg::TBCrosses.id");}

	//status of the cross
	  static public short[] get_status_TBCrosses() {return ColumnData.getShortArray("TimeBasedTrkg::TBCrosses.status");}

	//sector of the cross
	  static public byte[] get_sector_TBCrosses() {return ColumnData.getByteArray("TimeBasedTrkg::TBCrosses.sector");}

	//region of the cross
	  static public byte[] get_region_TBCrosses() {return ColumnData.getByteArray("TimeBasedTrkg::TBCrosses.region");}

	//Energy of the hit
	  static public float[] get_Cluster_ID_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.Cluster_ID");}

	//DC track cross x-coordinate (in the DC tilted sector coordinate system)
	  static public float[] get_x_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.x");}

	//DC track cross y-coordinate (in the DC tilted sector coordinate system)
	  static public float[] get_y_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.y");}

	//DC track cross z-coordinate (in the DC tilted sector coordinate system)
	  static public float[] get_z_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.z");}

	//DC track cross x-coordinate uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_x_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.err_x");}

	//DC track cross y-coordinate uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_y_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.err_y");}

	//DC track cross z-coordinate uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_z_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.err_z");}

	//DC track cross x-direction (in the DC tilted sector coordinate system)
	  static public float[] get_ux_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.ux");}

	//DC track cross y-direction (in the DC tilted sector coordinate system)
	  static public float[] get_uy_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.uy");}

	//DC track cross z-direction (in the DC tilted sector coordinate system)
	  static public float[] get_uz_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.uz");}

	//DC track cross x-direction uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_ux_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.err_ux");}

	//DC track cross y-direction uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_uy_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.err_uy");}

	//DC track cross z-direction uncertainty (in the DC tilted sector coordinate system)
	  static public float[] get_err_uz_TBCrosses() {return ColumnData.getFloatArray("TimeBasedTrkg::TBCrosses.err_uz");}

	//id of first superlater used in segment
	  static public short[] get_Segment1_ID_TBCrosses() {return ColumnData.getShortArray("TimeBasedTrkg::TBCrosses.Segment1_ID");}

	//id of second superlater used in segment
	  static public short[] get_Segment2_ID_TBCrosses() {return ColumnData.getShortArray("TimeBasedTrkg::TBCrosses.Segment2_ID");}

	//id of the track
	  static public short[] get_id_TBTracks() {return ColumnData.getShortArray("TimeBasedTrkg::TBTracks.id");}

	//status of the track
	  static public short[] get_status_TBTracks() {return ColumnData.getShortArray("TimeBasedTrkg::TBTracks.status");}

	//sector of the track
	  static public byte[] get_sector_TBTracks() {return ColumnData.getByteArray("TimeBasedTrkg::TBTracks.sector");}

	//Upstream Region 1 cross x-position in the lab (in cm)
	  static public float[] get_c1_x_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c1_x");}

	//Upstream Region 1 cross y-position in the lab (in cm)
	  static public float[] get_c1_y_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c1_y");}

	//Upstream Region 1 cross z-position in the lab (in cm)
	  static public float[] get_c1_z_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c1_z");}

	//Upstream Region 1 cross unit x-direction vector in the lab
	  static public float[] get_c1_ux_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c1_ux");}

	//Upstream Region 1 cross unit y-direction vector in the lab
	  static public float[] get_c1_uy_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c1_uy");}

	//Upstream Region 1 cross unit z-direction vector in the lab
	  static public float[] get_c1_uz_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c1_uz");}

	//Downstream Region 3 cross x-position in the lab (in cm)
	  static public float[] get_c3_x_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c3_x");}

	//Downstream Region 3 cross y-position in the lab (in cm)
	  static public float[] get_c3_y_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c3_y");}

	//Downstream Region 3 cross z-position in the lab (in cm)
	  static public float[] get_c3_z_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c3_z");}

	//Downstream Region 3 cross unit x-direction vector in the lab
	  static public float[] get_c3_ux_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c3_ux");}

	//Downstream Region 3 cross unit y-direction vector in the lab
	  static public float[] get_c3_uy_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c3_uy");}

	//Downstream Region 3 cross unit z-direction vector in the lab
	  static public float[] get_c3_uz_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.c3_uz");}

	//Upstream Region 1 track x-position in the lab (in cm)
	  static public float[] get_t1_x_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.t1_x");}

	//Upstream Region 1 track y-position in the lab (in cm)
	  static public float[] get_t1_y_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.t1_y");}

	//Upstream Region 1 track z-position in the lab (in cm)
	  static public float[] get_t1_z_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.t1_z");}

	//Upstream Region 1 track unit x-momentum vector in the lab
	  static public float[] get_t1_px_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.t1_px");}

	//Upstream Region 1 track unit y-momentum vector in the lab
	  static public float[] get_t1_py_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.t1_py");}

	//Upstream Region 1 track unit z-momentum vector in the lab
	  static public float[] get_t1_pz_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.t1_pz");}

	//Vertex x-position of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_Vtx0_x_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.Vtx0_x");}

	//Vertex y-position of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_Vtx0_y_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.Vtx0_y");}

	//Vertex z-position of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_Vtx0_z_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.Vtx0_z");}

	//3-momentum x-coordinate of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_p0_x_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.p0_x");}

	//3-momentum y-coordinate of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_p0_y_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.p0_y");}

	//3-momentum z-coordinate of the swam track to the DOCA to the beamline (in cm)
	  static public float[] get_p0_z_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.p0_z");}

	//id of first cross on track
	  static public short[] get_Cross1_ID_TBTracks() {return ColumnData.getShortArray("TimeBasedTrkg::TBTracks.Cross1_ID");}

	//id of second cross on track
	  static public short[] get_Cross2_ID_TBTracks() {return ColumnData.getShortArray("TimeBasedTrkg::TBTracks.Cross2_ID");}

	//id of third cross on track
	  static public short[] get_Cross3_ID_TBTracks() {return ColumnData.getShortArray("TimeBasedTrkg::TBTracks.Cross3_ID");}

	//charge of the track
	  static public byte[] get_q_TBTracks() {return ColumnData.getByteArray("TimeBasedTrkg::TBTracks.q");}

	//pathlength of the track
	  static public float[] get_pathlength_TBTracks() {return ColumnData.getFloatArray("TimeBasedTrkg::TBTracks.pathlength");}

	//id of the track
	  static public short[] get_id_TBCovMat() {return ColumnData.getShortArray("TimeBasedTrkg::TBCovMat.id");}
	  
	/**
	 * Get the pid array from the true data
	 * @return the pid array
	 */
	public static int[] pid() {
		return ColumnData.getIntArray("DC::true.pid");
	}
	
	public List<DetectorResponse> getDetectorResponses() {
		return null;
	}
			
	/**
	 * Get the sector array from the dgtz array
	 * @return the sector array
	 */
	public static byte[] sector() {
		return ColumnData.getByteArray("DC::tdc.sector");
	}

	/**
	 * Get the superlayer array from the dgtz array
	 * @return the superlayer array
	 */
	public static byte[] superlayer() {
		byte fulllayer[] =  ColumnData.getByteArray("DC::tdc.layer");
		if (fulllayer == null) {
			return null;
		}
		
		for (int index = 0; index < fulllayer.length; index++) {
			fulllayer[index] = (byte) (1 + ((fulllayer[index]-1) / 6));
		}
		
		return fulllayer;
	}

	/**
	 * Get the layer array from the dgtz array
	 * @return the layer array with layers [1..6]
	 */
	public static byte[] layer() {
		byte fulllayer[] =  ColumnData.getByteArray("DC::tdc.layer");
		if (fulllayer == null) {
			return null;
		}
		
		for (int index = 0; index < fulllayer.length; index++) {
			fulllayer[index] = (byte) (1 + ((fulllayer[index]-1) % 6));
		}
		
		return fulllayer;
	}

	/**
	 * Get the wire array from the dgtz array
	 * @return the wire array
	 */
	public static short[] wire() {
		return ColumnData.getShortArray("DC::tdc.component");
	}
	
	/**
	 * Get the sectors of time based segments
	 * 
	 * @return the sectors of time based segments
	 */
	public static byte[] timeBasedSegmentSector() {
		return get_sector_TBSegments();
	}

	/**
	 * Get the superlayers of time based segments
	 * 
	 * @return the superlayers of time based segments
	 */
	public static byte[] timeBasedSegmentSuperlayer() {
		return get_superlayer_TBSegments();
	}
	
	/**
	 * Get the start x coordinates in the midplane in sector system
	 * @return the start x coordinates
	 */
	public static float[] timeBasedSegment1X() {
		return get_SegEndPoint1X_TBSegments();
	}

	/**
	 * Get the start z coordinates in the midplane in sector system
	 * @return the start z coordinates
	 */
	public static float[] timeBasedSegment1Z() {
		return get_SegEndPoint1Z_TBSegments();
	}
	
	
	/**
	 * Get the end x coordinates in the midplane in sector system
	 * @return the end x coordinates
	 */
	public static float[] timeBasedSegment2X() {
		return get_SegEndPoint2X_TBSegments();
	}

	/**
	 * Get the end z coordinates in the midplane in sector system
	 * @return the end z coordinates
	 */
	public static float[] timeBasedSegment2Z() {
		return get_SegEndPoint2Z_TBSegments();
	}

	
	/**
	 * Get the number of time based segments
	 * 
	 * @return the number of time based segments
	 */
	public static int timeBasedSegmentCount() {
		byte sector[] = timeBasedSegmentSector();
		return (sector == null) ? 0 : sector.length;
	}

	/**
	 * Get the DOCAs from time based reconstruction
	 * @return the DOCAs from reconstruction
	 */
	public static double[] timeBasedTrkgDoca() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBHits.doca");
	}
	
	/**
	 * Get the sectors of time based hits
	 * 
	 * @return the sectors of time based hits
	 */
	public static int[] timeBasedTrkgSector() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBHits.sector");
	}

	/**
	 * Get the superlayer of time based hits
	 * 
	 * @return the super layers of time based hits
	 */
	public static int[] timeBasedTrkgSuperlayer() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBHits.superlayer");
	}

	/**
	 * Get the layers of time based hits
	 * 
	 * @return the layers of time based hits
	 */
	public static int[] timeBasedTrkgLayer() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBHits.layer");
	}

	/**
	 * Get the wires of time based hits
	 * 
	 * @return the wires of time based hits
	 */
	public static int[] timeBasedTrkgWire() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBHits.wire");
	}
	
	
	/**
	 * Get the number of time based hits
	 * 
	 * @return the number of time based tracks
	 */
	public static int timeBasedTrkgHitCount() {
		int sector[] = timeBasedTrkgSector();
		return (sector == null) ? 0 : sector.length;
	}
	

	/**
	 * Get the LR array from the dgtz data
	 * @return the LR array
	 */
	public static byte[] LR() {
		return ColumnData.getByteArray("DC::doca.LR");
	}
	
	/**
	 * Get the unsmeared doca array from the dgtz data
	 * @return the unsmeared simulation doca array
	 */
	public static float[] doca() {
		return ColumnData.getFloatArray("DC::doca.doca");
	}
		
	/**
	 * Get the time array from the dgtz data
	 * @return the time array
	 */
	public static float[] time() {
		return ColumnData.getFloatArray("DC::doca.time");
	}
	
	/**
	 * Get the sdoca array from the dgtz data
	 * @return the sdoca array
	 */
	public static float[] sdoca() {
		return ColumnData.getFloatArray("DC::doca.sdoca");
	}
	
	/**
	 * Get the stime array from the dgtz data
	 * @return the stime array
	 */
	public static float[] stime() {
		return ColumnData.getFloatArray("DC::doca.stime");
	}
	
	/**
	 * Get the trackE array from the true data
	 * @return the trackE array
	 */
	public static double[] trackE() {
		return ColumnData.getDoubleArray("DC::true.trackE");
	}

	/**
	 * Get the avgX array from the true data
	 * @return the avgX array
	 */
	public static double[] avgX() {
		return ColumnData.getDoubleArray("DC::true.avgX");
	}
	
	/**
	 * Get the avgY array from the true data
	 * @return the avgY array
	 */
	public static double[] avgY() {
		return ColumnData.getDoubleArray("DC::true.avgY");
	}
	
	/**
	 * Get the avgZ array from the true data
	 * @return the avgZ array
	 */
	public static double[] avgZ() {
		return ColumnData.getDoubleArray("DC::true.avgZ");
	}
		
	/**
	 * Get the tdc array from the tdc data
	 * @return the tdc array
	 */
	public static int[] tdc() {
		return ColumnData.getIntArray("DC::tdc.TDC");
	}
	/**
	 * Get the hit count 
	 * @return the hit count
	 */
	public static int hitCount() {
		byte sector[] = sector();
		return (sector == null) ? 0 : sector.length;
	}
	
	/**
	 * Get the time based tracking momentum array
	 * @return the time based tracking momentum array
	 */
	public static double[] timeBasedTrackP() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBTracks.p");
	}
	
	/**
	 * Get the time based tracking crosses sector array
	 * @return the time based tracking crosses sector array
	 */
	public static int[] timeBasedCrossSector() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBCrosses.sector");
	}

	/**
	 * Get the hit based tracking crosses sector
	 * @return the hit based tracking crosses sector array
	 */
	public static int[] hitBasedCrossSector() {
		return ColumnData.getIntArray("HitBasedTrkg::HBCrosses.sector");
	}
	
	
	/**
	 * Get the time based tracking crosses region array
	 * @return the time based tracking crosses region array
	 */
	public static int[] timeBasedCrossRegion() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBCrosses.region");
	}

	/**
	 * Get the hit based tracking crosses region
	 * @return the hit based tracking crosses region array
	 */
	public static int[] hitBasedCrossRegion() {
		return ColumnData.getIntArray("HitBasedTrkg::HBCrosses.region");
	}

	
	/**
	 * Get the time based tracking crosses ID array
	 * @return the time based tracking crosses ID array
	 */
	public static int[] timeBasedCrossID() {
		return ColumnData.getIntArray("TimeBasedTrkg::TBCrosses.ID");
	}

	/**
	 * Get the hit based tracking crosses ID
	 * @return the hit based tracking crosses ID array
	 */
	public static int[] hitBasedCrossID() {
		return ColumnData.getIntArray("HitBasedTrkg::HBCrosses.ID");
	}

	/**
	 * get the time based crosses X position
	 * @return the time based crosses X position
	 */
	public static double[] timeBasedCrossX() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.x");
	}
	
	/**
	 * get the time based crosses Y position
	 * @return the time based crosses Y position
	 */
	public static double[] timeBasedCrossY() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.y");
	}
	
	/**
	 * get the time based crosses Z position
	 * @return the time based crosses Z position
	 */
	public static double[] timeBasedCrossZ() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.z");
	}
	
	/**
	 * get the time based crosses Ux direction
	 * @return the time based crosses X direction
	 */
	public static double[] timeBasedCrossUx() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.ux");
	}
	
	/**
	 * get the time based crosses Uy direction
	 * @return the time based crosses Uy direction
	 */
	public static double[] timeBasedCrossUy() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.uy");
	}
	
	/**
	 * get the time based crosses Uz direction
	 * @return the time based crosses Uz direction
	 */
	public static double[] timeBasedCrossUz() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.uz");
	}

	/**
	 * get the time based crosses X error
	 * @return the time based crosses X error
	 */
	public static double[] timeBasedCrossErrX() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.err_x");
	}
	
	/**
	 * get the time based crosses Y error
	 * @return the time based crosses Y error
	 */
	public static double[] timeBasedCrossErrY() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.err_y");
	}
	
	/**
	 * get the time based crosses Z error
	 * @return the time based crosses Z error
	 */
	public static double[] timeBasedCrossErrZ() {
		return ColumnData.getDoubleArray("TimeBasedTrkg::TBCrosses.err_z");
	}

	/**
	 * get the hit based crosses X position
	 * @return the hit based crosses X position
	 */
	public static double[] hitBasedCrossX() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.x");
	}
	
	/**
	 * get the hit based crosses Y position
	 * @return the hit based crosses Y position
	 */
	public static double[] hitBasedCrossY() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.y");
	}
	
	/**
	 * get the hit based crosses Z position
	 * @return the hit based crosses Z position
	 */
	public static double[] hitBasedCrossZ() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.z");
	}
	
	/**
	 * get the hit based crosses Ux direction
	 * @return the hit based crosses X direction
	 */
	public static double[] hitBasedCrossUx() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.ux");
	}
	
	/**
	 * get the hit based crosses Uy direction
	 * @return the hit based crosses Uy direction
	 */
	public static double[] hitBasedCrossUy() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.uy");
	}
	
	/**
	 * get the hit based crosses Uz direction
	 * @return the hit based crosses Uz direction
	 */
	public static double[] hitBasedCrossUz() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.uz");
	}

	/**
	 * get the hit based crosses X error
	 * @return the hit based crosses X error
	 */
	public static double[] hitBasedCrossErrX() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.err_x");
	}
	
	/**
	 * get the hit based crosses Y error
	 * @return the hit based crosses Y error
	 */
	public static double[] hitBasedCrossErrY() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.err_y");
	}
	
	/**
	 * get the hit based crosses Z error
	 * @return the hit based crosses Z error
	 */
	public static double[] hitBasedCrossErrZ() {
		return ColumnData.getDoubleArray("HitBasedTrkg::HBCrosses.err_z");
	}
	
	
	/**
	 * Some truth feedback for DC
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void trueFeedback(int hitIndex,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}

		double trackE[] = trackE();
		if ((trackE == null) || (hitIndex >= trackE.length)) {
			return;
		}

		double etrack = trackE[hitIndex] / 1000; // to gev
		feedbackStrings.add(DataSupport.trueColor + "true energy "
				+ DoubleFormat.doubleFormat(etrack, 2) + " GeV");
	}

	
	/**
	 * Some DC noise feedback
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void noiseFeedback(int hitIndex,
			List<String> feedbackStrings) {
		if (hitIndex < 0) {
			return;
		}

		boolean noise[] = NoiseManager.getInstance().getNoise();

		if ((noise == null) || (hitIndex >= noise.length)) {
			return;
		}

		String noiseStr = DataSupport.prelimColor + "Noise Hit Guess: "
				+ (((noise != null) && noise[hitIndex]) ? "noise"
						: "not noise");
		feedbackStrings.add(noiseStr);

	}

	
	/**
	 * Add some dgtz hit feedback for dc
	 * 
	 * @param hitIndex the hit index
	 * @param feedbackStrings the collection of feedback strings
	 */
	public static void dcBanksFeedback(int hitIndex,
			List<String> feedbackStrings) {

		if (hitIndex < 0) {
			return;
		}
		
		int hitCount = hitCount();
		if ((hitCount > 0) && (hitIndex < hitCount)) {
			byte sector[] = sector();
			byte superlayer[] = superlayer();
			byte layer[] = layer();
			short wire[] = wire();
			int tdc[] = tdc();
			byte LR[] = LR();
			float doca[] = doca();
			float sdoca[] = sdoca();
			float time[] = time();
			float stime[] = stime();

			feedbackStrings.add(DataSupport.dgtzColor + "sector " + sector[hitIndex]
					+ "  superlayer " + superlayer[hitIndex] + "  layer "
					+ layer[hitIndex] + "  wire " + wire[hitIndex]);

			String lraStr = DataSupport.safeString(LR, hitIndex);
			String tdcStr = DataSupport.safeString(tdc, hitIndex);
			String docaStr = DataSupport.safeString(doca, hitIndex, 1);
			String timeStr = DataSupport.safeString(time, hitIndex, 1);
			String sdocaStr = DataSupport.safeString(sdoca, hitIndex, 1);
			String stimeStr = DataSupport.safeString(stime, hitIndex, 1);

			feedbackStrings.add(DataSupport.dgtzColor + "LRA " + lraStr + "  tdc " + tdcStr);
			feedbackStrings.add(
					DataSupport.dgtzColor + "doca " + docaStr + "  sdoca " + sdocaStr + " mm");
			feedbackStrings.add(
					DataSupport.dgtzColor + "time " + timeStr + "  stime " + stimeStr + " ns");
			
		} //hitCount > 0

	}
	
	/**
	 * Get the index of the dc hit
	 * 
	 * @param sect the 1-based sector
	 * @param supl the 1-based superlayer
	 * @param lay the 1-based layer
	 * @param wireid the 1-based wire
	 * @return the index of a hit with these parameters, or -1 if not found
	 */
	public static int hitIndex(int sect, int supl, int lay, int wireid) {

		int hitCount = hitCount();
		if (hitCount > 0) {
			byte sector[] = DC.sector();
			byte superlayer[] = DC.superlayer();
			byte layer[] = DC.layer();
			short wire[] = DC.wire();
			
			for (int i = 0; i < hitCount; i++) {
				if ((sect == sector[i]) && (supl == superlayer[i])
						&& (lay == layer[i]) && (wireid == wire[i])) {
					return i;
				}
			}

		}
		
		return -1;
	}


	/**
	 * Get the number of hit based crosses
	 * 
	 * @return he number of hit based crosses
	 */
	public static int hitBasedCrossCount() {
		int sector[] = ColumnData.getIntArray("HitBasedTrkg::HBCrosses.sector");
		return (sector == null) ? 0 : sector.length;
	}

	/**
	 * Get the number of time based crosses
	 * 
	 * @return the number of time based crosses
	 */
	public static int timeBasedCrossCount() {
		int sector[] = ColumnData
				.getIntArray("TimeBasedTrkg::TBCrosses.sector");
		return (sector == null) ? 0 : sector.length;
	}

	/**
	 * Get the number of time based tracks
	 * 
	 * @return the number of time based tracks
	 */
	public static int timeBasedTrackCount() {
		int sector[] = ColumnData.getIntArray("TimeBasedTrkg::TBTracks.sector");
		return (sector == null) ? 0 : sector.length;
	}
	
}
