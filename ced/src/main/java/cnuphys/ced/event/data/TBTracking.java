package cnuphys.ced.event.data;

import org.jlab.io.base.DataEvent;

public class TBTracking extends DetectorData {
//
//	bank name: [TimeBasedTrkg::TBClusters] column name: [id] full name: [TimeBasedTrkg::TBClusters.id] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [status] full name: [TimeBasedTrkg::TBClusters.status] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [sector] full name: [TimeBasedTrkg::TBClusters.sector] data type: byte
//	bank name: [TimeBasedTrkg::TBClusters] column name: [superlayer] full name: [TimeBasedTrkg::TBClusters.superlayer] data type: byte
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit1_ID] full name: [TimeBasedTrkg::TBClusters.Hit1_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit2_ID] full name: [TimeBasedTrkg::TBClusters.Hit2_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit3_ID] full name: [TimeBasedTrkg::TBClusters.Hit3_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit4_ID] full name: [TimeBasedTrkg::TBClusters.Hit4_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit5_ID] full name: [TimeBasedTrkg::TBClusters.Hit5_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit6_ID] full name: [TimeBasedTrkg::TBClusters.Hit6_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit7_ID] full name: [TimeBasedTrkg::TBClusters.Hit7_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit8_ID] full name: [TimeBasedTrkg::TBClusters.Hit8_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit9_ID] full name: [TimeBasedTrkg::TBClusters.Hit9_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit10_ID] full name: [TimeBasedTrkg::TBClusters.Hit10_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit11_ID] full name: [TimeBasedTrkg::TBClusters.Hit11_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [Hit12_ID] full name: [TimeBasedTrkg::TBClusters.Hit12_ID] data type: short
//	bank name: [TimeBasedTrkg::TBClusters] column name: [avgWire] full name: [TimeBasedTrkg::TBClusters.avgWire] data type: float
//	bank name: [TimeBasedTrkg::TBClusters] column name: [fitChisqProb] full name: [TimeBasedTrkg::TBClusters.fitChisqProb] data type: float
//	bank name: [TimeBasedTrkg::TBClusters] column name: [fitSlope] full name: [TimeBasedTrkg::TBClusters.fitSlope] data type: float
//	bank name: [TimeBasedTrkg::TBClusters] column name: [fitSlopeErr] full name: [TimeBasedTrkg::TBClusters.fitSlopeErr] data type: float
//	bank name: [TimeBasedTrkg::TBClusters] column name: [fitInterc] full name: [TimeBasedTrkg::TBClusters.fitInterc] data type: float
//	bank name: [TimeBasedTrkg::TBClusters] column name: [fitIntercErr] full name: [TimeBasedTrkg::TBClusters.fitIntercErr] data type: float
//	bank name: [TimeBasedTrkg::TBClusters] column name: [size] full name: [TimeBasedTrkg::TBClusters.size] data type: byte
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [id] full name: [TimeBasedTrkg::TBCrosses.id] data type: short
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [status] full name: [TimeBasedTrkg::TBCrosses.status] data type: short
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [sector] full name: [TimeBasedTrkg::TBCrosses.sector] data type: byte
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [region] full name: [TimeBasedTrkg::TBCrosses.region] data type: byte
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [Cluster_ID] full name: [TimeBasedTrkg::TBCrosses.Cluster_ID] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [x] full name: [TimeBasedTrkg::TBCrosses.x] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [y] full name: [TimeBasedTrkg::TBCrosses.y] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [z] full name: [TimeBasedTrkg::TBCrosses.z] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [err_x] full name: [TimeBasedTrkg::TBCrosses.err_x] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [err_y] full name: [TimeBasedTrkg::TBCrosses.err_y] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [err_z] full name: [TimeBasedTrkg::TBCrosses.err_z] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [ux] full name: [TimeBasedTrkg::TBCrosses.ux] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [uy] full name: [TimeBasedTrkg::TBCrosses.uy] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [uz] full name: [TimeBasedTrkg::TBCrosses.uz] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [err_ux] full name: [TimeBasedTrkg::TBCrosses.err_ux] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [err_uy] full name: [TimeBasedTrkg::TBCrosses.err_uy] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [err_uz] full name: [TimeBasedTrkg::TBCrosses.err_uz] data type: float
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [Segment1_ID] full name: [TimeBasedTrkg::TBCrosses.Segment1_ID] data type: short
//	bank name: [TimeBasedTrkg::TBCrosses] column name: [Segment2_ID] full name: [TimeBasedTrkg::TBCrosses.Segment2_ID] data type: short
//	bank name: [TimeBasedTrkg::TBHits] column name: [id] full name: [TimeBasedTrkg::TBHits.id] data type: short
//	bank name: [TimeBasedTrkg::TBHits] column name: [status] full name: [TimeBasedTrkg::TBHits.status] data type: short
//	bank name: [TimeBasedTrkg::TBHits] column name: [sector] full name: [TimeBasedTrkg::TBHits.sector] data type: byte
//	bank name: [TimeBasedTrkg::TBHits] column name: [superlayer] full name: [TimeBasedTrkg::TBHits.superlayer] data type: byte
//	bank name: [TimeBasedTrkg::TBHits] column name: [layer] full name: [TimeBasedTrkg::TBHits.layer] data type: byte
//	bank name: [TimeBasedTrkg::TBHits] column name: [wire] full name: [TimeBasedTrkg::TBHits.wire] data type: short
//	bank name: [TimeBasedTrkg::TBHits] column name: [time] full name: [TimeBasedTrkg::TBHits.time] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [doca] full name: [TimeBasedTrkg::TBHits.doca] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [docaError] full name: [TimeBasedTrkg::TBHits.docaError] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [trkDoca] full name: [TimeBasedTrkg::TBHits.trkDoca] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [timeResidual] full name: [TimeBasedTrkg::TBHits.timeResidual] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [LR] full name: [TimeBasedTrkg::TBHits.LR] data type: byte
//	bank name: [TimeBasedTrkg::TBHits] column name: [LocX] full name: [TimeBasedTrkg::TBHits.LocX] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [LocY] full name: [TimeBasedTrkg::TBHits.LocY] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [X] full name: [TimeBasedTrkg::TBHits.X] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [Z] full name: [TimeBasedTrkg::TBHits.Z] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [B] full name: [TimeBasedTrkg::TBHits.B] data type: float
//	bank name: [TimeBasedTrkg::TBHits] column name: [clusterID] full name: [TimeBasedTrkg::TBHits.clusterID] data type: short
//	bank name: [TimeBasedTrkg::TBHits] column name: [trkID] full name: [TimeBasedTrkg::TBHits.trkID] data type: byte
//	bank name: [TimeBasedTrkg::TBSegmentTrajectory] column name: [segmentID] full name: [TimeBasedTrkg::TBSegmentTrajectory.segmentID] data type: short
//	bank name: [TimeBasedTrkg::TBSegmentTrajectory] column name: [sector] full name: [TimeBasedTrkg::TBSegmentTrajectory.sector] data type: byte
//	bank name: [TimeBasedTrkg::TBSegmentTrajectory] column name: [superlayer] full name: [TimeBasedTrkg::TBSegmentTrajectory.superlayer] data type: byte
//	bank name: [TimeBasedTrkg::TBSegmentTrajectory] column name: [layer] full name: [TimeBasedTrkg::TBSegmentTrajectory.layer] data type: byte
//	bank name: [TimeBasedTrkg::TBSegmentTrajectory] column name: [matchedHitID] full name: [TimeBasedTrkg::TBSegmentTrajectory.matchedHitID] data type: short
//	bank name: [TimeBasedTrkg::TBSegmentTrajectory] column name: [trkDoca] full name: [TimeBasedTrkg::TBSegmentTrajectory.trkDoca] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [id] full name: [TimeBasedTrkg::TBSegments.id] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [status] full name: [TimeBasedTrkg::TBSegments.status] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [sector] full name: [TimeBasedTrkg::TBSegments.sector] data type: byte
//	bank name: [TimeBasedTrkg::TBSegments] column name: [superlayer] full name: [TimeBasedTrkg::TBSegments.superlayer] data type: byte
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Cluster_ID] full name: [TimeBasedTrkg::TBSegments.Cluster_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit1_ID] full name: [TimeBasedTrkg::TBSegments.Hit1_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit2_ID] full name: [TimeBasedTrkg::TBSegments.Hit2_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit3_ID] full name: [TimeBasedTrkg::TBSegments.Hit3_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit4_ID] full name: [TimeBasedTrkg::TBSegments.Hit4_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit5_ID] full name: [TimeBasedTrkg::TBSegments.Hit5_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit6_ID] full name: [TimeBasedTrkg::TBSegments.Hit6_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit7_ID] full name: [TimeBasedTrkg::TBSegments.Hit7_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit8_ID] full name: [TimeBasedTrkg::TBSegments.Hit8_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit9_ID] full name: [TimeBasedTrkg::TBSegments.Hit9_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit10_ID] full name: [TimeBasedTrkg::TBSegments.Hit10_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit11_ID] full name: [TimeBasedTrkg::TBSegments.Hit11_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [Hit12_ID] full name: [TimeBasedTrkg::TBSegments.Hit12_ID] data type: short
//	bank name: [TimeBasedTrkg::TBSegments] column name: [avgWire] full name: [TimeBasedTrkg::TBSegments.avgWire] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [fitChisqProb] full name: [TimeBasedTrkg::TBSegments.fitChisqProb] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [fitSlope] full name: [TimeBasedTrkg::TBSegments.fitSlope] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [fitSlopeErr] full name: [TimeBasedTrkg::TBSegments.fitSlopeErr] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [fitInterc] full name: [TimeBasedTrkg::TBSegments.fitInterc] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [fitIntercErr] full name: [TimeBasedTrkg::TBSegments.fitIntercErr] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [SegEndPoint1X] full name: [TimeBasedTrkg::TBSegments.SegEndPoint1X] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [SegEndPoint1Z] full name: [TimeBasedTrkg::TBSegments.SegEndPoint1Z] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [SegEndPoint2X] full name: [TimeBasedTrkg::TBSegments.SegEndPoint2X] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [SegEndPoint2Z] full name: [TimeBasedTrkg::TBSegments.SegEndPoint2Z] data type: float
//	bank name: [TimeBasedTrkg::TBSegments] column name: [size] full name: [TimeBasedTrkg::TBSegments.size] data type: byte
//	bank name: [TimeBasedTrkg::TBTracks] column name: [id] full name: [TimeBasedTrkg::TBTracks.id] data type: short
//	bank name: [TimeBasedTrkg::TBTracks] column name: [status] full name: [TimeBasedTrkg::TBTracks.status] data type: short
//	bank name: [TimeBasedTrkg::TBTracks] column name: [sector] full name: [TimeBasedTrkg::TBTracks.sector] data type: byte
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c1_x] full name: [TimeBasedTrkg::TBTracks.c1_x] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c1_y] full name: [TimeBasedTrkg::TBTracks.c1_y] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c1_z] full name: [TimeBasedTrkg::TBTracks.c1_z] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c1_ux] full name: [TimeBasedTrkg::TBTracks.c1_ux] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c1_uy] full name: [TimeBasedTrkg::TBTracks.c1_uy] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c1_uz] full name: [TimeBasedTrkg::TBTracks.c1_uz] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c3_x] full name: [TimeBasedTrkg::TBTracks.c3_x] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c3_y] full name: [TimeBasedTrkg::TBTracks.c3_y] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c3_z] full name: [TimeBasedTrkg::TBTracks.c3_z] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c3_ux] full name: [TimeBasedTrkg::TBTracks.c3_ux] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c3_uy] full name: [TimeBasedTrkg::TBTracks.c3_uy] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [c3_uz] full name: [TimeBasedTrkg::TBTracks.c3_uz] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [t1_x] full name: [TimeBasedTrkg::TBTracks.t1_x] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [t1_y] full name: [TimeBasedTrkg::TBTracks.t1_y] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [t1_z] full name: [TimeBasedTrkg::TBTracks.t1_z] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [t1_px] full name: [TimeBasedTrkg::TBTracks.t1_px] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [t1_py] full name: [TimeBasedTrkg::TBTracks.t1_py] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [t1_pz] full name: [TimeBasedTrkg::TBTracks.t1_pz] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [Vtx0_x] full name: [TimeBasedTrkg::TBTracks.Vtx0_x] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [Vtx0_y] full name: [TimeBasedTrkg::TBTracks.Vtx0_y] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [Vtx0_z] full name: [TimeBasedTrkg::TBTracks.Vtx0_z] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [p0_x] full name: [TimeBasedTrkg::TBTracks.p0_x] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [p0_y] full name: [TimeBasedTrkg::TBTracks.p0_y] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [p0_z] full name: [TimeBasedTrkg::TBTracks.p0_z] data type: float
//	bank name: [TimeBasedTrkg::TBTracks] column name: [Cross1_ID] full name: [TimeBasedTrkg::TBTracks.Cross1_ID] data type: short
//	bank name: [TimeBasedTrkg::TBTracks] column name: [Cross2_ID] full name: [TimeBasedTrkg::TBTracks.Cross2_ID] data type: short
//	bank name: [TimeBasedTrkg::TBTracks] column name: [Cross3_ID] full name: [TimeBasedTrkg::TBTracks.Cross3_ID] data type: short
//	bank name: [TimeBasedTrkg::TBTracks] column name: [q] full name: [TimeBasedTrkg::TBTracks.q] data type: byte
//	bank name: [TimeBasedTrkg::TBTracks] column name: [pathlength] full name: [TimeBasedTrkg::TBTracks.pathlength] data type: float
	
	
	//singleton
	private static TBTracking _instance;
	
	/**
	 * Public access to the singleton
	 * @return the FTOF singleton
	 */
	public static TBTracking getInstance() {
		if (_instance == null) {
			_instance = new TBTracking();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
	}

}
