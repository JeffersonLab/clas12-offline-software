package org.jlab.rec.dc.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.trajectory.SegmentTrajectory;

import trackfitter.fitter.utilities.*;

/**
 * A class to fill the reconstructed DC banks
 *
 * @author ziegler
 *
 */
public class RecoBankWriter {

    /**
     *
     * Writes output banks
     *
     */
    public RecoBankWriter() {
        // empty constructor

    }

    public void updateListsListWithClusterInfo(List<FittedHit> fhits,
            List<FittedCluster> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            clusters.get(i).set_Id(i + 1);
            for (int j = 0; j < clusters.get(i).size(); j++) {

                clusters.get(i).get(j).set_AssociatedClusterID(clusters.get(i).get_Id());

                for (int k = 0; k < fhits.size(); k++) {
                    if (fhits.get(k).get_Id() == clusters.get(i).get(j).get_Id()) {
                        fhits.remove(k);
                        fhits.add(clusters.get(i).get(j));

                    }
                }
            }
        }

    }

    public DataBank fillHBHitsBank(DataEvent event, List<FittedHit> hitlist) {

        DataBank bank = event.createBank("HitBasedTrkg::HBHits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {
            if (hitlist.get(i).get_Id() == -1) {
                continue;
            }
            bank.setShort("id", i, (short) hitlist.get(i).get_Id());
            bank.setShort("status", i, (short) 1);
            bank.setByte("superlayer", i, (byte) hitlist.get(i).get_Superlayer());
            bank.setByte("layer", i, (byte) hitlist.get(i).get_Layer());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setShort("wire", i, (short) hitlist.get(i).get_Wire());
            bank.setFloat("time", i, (float) hitlist.get(i).get_Time());
            bank.setFloat("docaError", i, (float) hitlist.get(i).get_DocaErr());
            bank.setFloat("trkDoca", i, (float) hitlist.get(i).get_ClusFitDoca());
            bank.setFloat("LocX", i, (float) hitlist.get(i).get_lX());
            bank.setFloat("LocY", i, (float) hitlist.get(i).get_lY());
            bank.setFloat("X", i, (float) hitlist.get(i).get_X());
            bank.setFloat("Z", i, (float) hitlist.get(i).get_Z());
            bank.setByte("LR", i, (byte) hitlist.get(i).get_LeftRightAmb());
            bank.setShort("clusterID", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setByte("trkID", i, (byte) hitlist.get(i).get_AssociatedHBTrackID());
            bank.setFloat("B", i, (float) hitlist.get(i).get_B());
        }

        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return clusters bank
     */
    public DataBank fillHBClustersBank(DataEvent event, List<FittedCluster> cluslist) {

        DataBank bank = event.createBank("HitBasedTrkg::HBClusters", cluslist.size());

        int[] hitIdxArray = new int[12];

        for (int i = 0; i < cluslist.size(); i++) {
            if (cluslist.get(i).get_Id() == -1) {
                continue;
            }
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            double chi2 = 0;

            bank.setShort("id", i, (short) cluslist.get(i).get_Id());
            bank.setShort("status", i, (short) 1);
            bank.setByte("superlayer", i, (byte) cluslist.get(i).get_Superlayer());
            bank.setByte("sector", i, (byte) cluslist.get(i).get_Sector());

            bank.setFloat("avgWire", i, (float) cluslist.get(i).getAvgwire());
            bank.setByte("size", i, (byte) cluslist.get(i).size());

            double fitSlope = cluslist.get(i).get_clusterLineFitSlope();
            double fitInterc = cluslist.get(i).get_clusterLineFitIntercept();

            bank.setFloat("fitSlope", i, (float) fitSlope);
            bank.setFloat("fitSlopeErr", i, (float) cluslist.get(i).get_clusterLineFitSlopeErr());
            bank.setFloat("fitInterc", i, (float) fitInterc);
            bank.setFloat("fitIntercErr", i, (float) cluslist.get(i).get_clusterLineFitInterceptErr());

            for (int j = 0; j < cluslist.get(i).size(); j++) {
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = cluslist.get(i).get(j).get_Id();
                }

                double residual = cluslist.get(i).get(j).get_ClusFitDoca() / (cluslist.get(i).get(j).get_CellSize() / Math.sqrt(12.));
                chi2 += residual * residual;
            }
            bank.setFloat("fitChisqProb", i, (float) ProbChi2perNDF.prob(chi2, cluslist.get(i).size() - 2));

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }
        }

        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return segments bank
     */
    public DataBank fillHBSegmentsBank(DataEvent event, List<Segment> seglist) {

        DataBank bank = event.createBank("HitBasedTrkg::HBSegments", seglist.size());

        int[] hitIdxArray = new int[12]; // only saving 12 hits for now

        for (int i = 0; i < seglist.size(); i++) {

            if (seglist.get(i).get_Id() == -1) {
                continue;
            }

            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }

            double chi2 = 0;

            bank.setShort("id", i, (short) seglist.get(i).get_Id());
            bank.setByte("superlayer", i, (byte) seglist.get(i).get_Superlayer());
            bank.setByte("sector", i, (byte) seglist.get(i).get_Sector());

            FittedCluster cls = seglist.get(i).get_fittedCluster();
            bank.setShort("Cluster_ID", i, (short) cls.get_Id());

            bank.setFloat("avgWire", i, (float) cls.getAvgwire());
            bank.setByte("size", i, (byte) seglist.get(i).size());

            bank.setFloat("fitSlope", i, (float) cls.get_clusterLineFitSlope());
            bank.setFloat("fitSlopeErr", i, (float) cls.get_clusterLineFitSlopeErr());
            bank.setFloat("fitInterc", i, (float) cls.get_clusterLineFitIntercept());
            bank.setFloat("fitIntercErr", i, (float) cls.get_clusterLineFitInterceptErr());

            bank.setFloat("SegEndPoint1X", i, (float) seglist.get(i).get_SegmentEndPoints()[0]);
            bank.setFloat("SegEndPoint1Z", i, (float) seglist.get(i).get_SegmentEndPoints()[1]);
            bank.setFloat("SegEndPoint2X", i, (float) seglist.get(i).get_SegmentEndPoints()[2]);
            bank.setFloat("SegEndPoint2Z", i, (float) seglist.get(i).get_SegmentEndPoints()[3]);

            for (int j = 0; j < seglist.get(i).size(); j++) {
                if (seglist.get(i).get_Id() == -1) {
                    continue;
                }
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = seglist.get(i).get(j).get_Id();
                }

                double residual = seglist.get(i).get(j).get_ClusFitDoca() / (seglist.get(i).get(j).get_CellSize() / Math.sqrt(12.));
                chi2 += residual * residual;
            }
            bank.setFloat("fitChisqProb", i, (float) ProbChi2perNDF.prob(chi2, seglist.get(i).size() - 2));

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }
        }

        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return segments bank
     */
    public DataBank fillHBSegmentsTrajectoryBank(DataEvent event, List<Segment> seglist) {
        DataBank bank = event.createBank("HitBasedTrkg::HBSegmentTrajectory", seglist.size() * 6);

        int index = 0;
        for (int i = 0; i < seglist.size(); i++) {
            if (seglist.get(i).get_Id() == -1) {
                continue;
            }
            SegmentTrajectory trj = seglist.get(i).get_Trajectory();
            for (int l = 0; l < 6; l++) {
                bank.setShort("segmentID", index, (short) trj.get_SegmentId());
                bank.setByte("sector", index, (byte) trj.get_Sector());
                bank.setByte("superlayer", index, (byte) trj.get_Superlayer());
                bank.setByte("layer", index, (byte) (l + 1));
                bank.setShort("matchedHitID", index, (short) trj.getMatchedHitId()[l]);
                bank.setFloat("trkDoca", index, (float) trj.getTrkDoca()[l]);
                index++;
            }
        }
        bank.show();
        return bank;
    }

    /**
     *
     * @param event the EvioEvent
     * @return crosses bank
     */
    public DataBank fillHBCrossesBank(DataEvent event, List<Cross> crosslist) {

        int banksize=0;
         for (int i = 0; i < crosslist.size(); i++) {
            if (crosslist.get(i).get_Id() != -1) 
                banksize++;
        }
         
        DataBank bank = event.createBank("HitBasedTrkg::HBCrosses", banksize);
        
        int index=0;
        for (int i = 0; i < crosslist.size(); i++) {
            if (crosslist.get(i).get_Id() != -1) {              
                bank.setShort("id", index, (short) crosslist.get(i).get_Id());
                bank.setShort("status", index, (short) 1);
                bank.setByte("sector", index, (byte) crosslist.get(i).get_Sector());
                bank.setByte("region", index, (byte) crosslist.get(i).get_Region());
                bank.setFloat("x", index, (float) crosslist.get(i).get_Point().x());
                bank.setFloat("y", index, (float) crosslist.get(i).get_Point().y());
                bank.setFloat("z", index, (float) crosslist.get(i).get_Point().z());
                bank.setFloat("err_x", index, (float) crosslist.get(i).get_PointErr().x());
                bank.setFloat("err_y", index, (float) crosslist.get(i).get_PointErr().y());
                bank.setFloat("err_z", index, (float) crosslist.get(i).get_PointErr().z());
                bank.setFloat("ux", index, (float) crosslist.get(i).get_Dir().x());
                bank.setFloat("uy", index, (float) crosslist.get(i).get_Dir().y());
                bank.setFloat("uz", index, (float) crosslist.get(i).get_Dir().z());
                bank.setFloat("err_ux", index, (float) crosslist.get(i).get_DirErr().x());
                bank.setFloat("err_uy", index, (float) crosslist.get(i).get_DirErr().y());
                bank.setFloat("err_uz", index, (float) crosslist.get(i).get_DirErr().z());
                bank.setShort("Segment1_ID", index, (short) crosslist.get(i).get_Segment1().get_Id());
                bank.setShort("Segment2_ID", index, (short) crosslist.get(i).get_Segment2().get_Id());
                index++;
            }
        }
       
        return bank;

    }

    public DataBank fillHBTracksBank(DataEvent event, List<Track> candlist) {

        DataBank bank = event.createBank("HitBasedTrkg::HBTracks", candlist.size());

        for (int i = 0; i < candlist.size(); i++) {
            bank.setShort("id", i, (short) candlist.get(i).get_Id());
            bank.setByte("sector", i, (byte) candlist.get(i).get_Sector());
            bank.setByte("q", i, (byte) candlist.get(i).get_Q());
            //bank.setFloat("p", i, (float) candlist.get(i).get_P());
            bank.setFloat("c1_x", i, (float) candlist.get(i).get_PreRegion1CrossPoint().x());
            bank.setFloat("c1_y", i, (float) candlist.get(i).get_PreRegion1CrossPoint().y());
            bank.setFloat("c1_z", i, (float) candlist.get(i).get_PreRegion1CrossPoint().z());
            bank.setFloat("c1_ux", i, (float) candlist.get(i).get_PreRegion1CrossDir().x());
            bank.setFloat("c1_uy", i, (float) candlist.get(i).get_PreRegion1CrossDir().y());
            bank.setFloat("c1_uz", i, (float) candlist.get(i).get_PreRegion1CrossDir().z());
            bank.setFloat("c3_x", i, (float) candlist.get(i).get_PostRegion3CrossPoint().x());
            bank.setFloat("c3_y", i, (float) candlist.get(i).get_PostRegion3CrossPoint().y());
            bank.setFloat("c3_z", i, (float) candlist.get(i).get_PostRegion3CrossPoint().z());
            bank.setFloat("c3_ux", i, (float) candlist.get(i).get_PostRegion3CrossDir().x());
            bank.setFloat("c3_uy", i, (float) candlist.get(i).get_PostRegion3CrossDir().y());
            bank.setFloat("c3_uz", i, (float) candlist.get(i).get_PostRegion3CrossDir().z());
            bank.setFloat("t1_x", i, (float) candlist.get(i).get_Region1TrackX().x());
            bank.setFloat("t1_y", i, (float) candlist.get(i).get_Region1TrackX().y());
            bank.setFloat("t1_z", i, (float) candlist.get(i).get_Region1TrackX().z());
            bank.setFloat("t1_px", i, (float) candlist.get(i).get_Region1TrackP().x());
            bank.setFloat("t1_py", i, (float) candlist.get(i).get_Region1TrackP().y());
            bank.setFloat("t1_pz", i, (float) candlist.get(i).get_Region1TrackP().z());
            bank.setFloat("pathlength", i, (float) candlist.get(i).get_TotPathLen());
            bank.setFloat("Vtx0_x", i, (float) candlist.get(i).get_Vtx0().x());
            bank.setFloat("Vtx0_y", i, (float) candlist.get(i).get_Vtx0().y());
            bank.setFloat("Vtx0_z", i, (float) candlist.get(i).get_Vtx0().z());
            bank.setFloat("p0_x", i, (float) candlist.get(i).get_pAtOrig().x());
            bank.setFloat("p0_y", i, (float) candlist.get(i).get_pAtOrig().y());
            bank.setFloat("p0_z", i, (float) candlist.get(i).get_pAtOrig().z());
            bank.setShort("Cross1_ID", i, (short) candlist.get(i).get(0).get_Id());
            bank.setShort("Cross2_ID", i, (short) candlist.get(i).get(1).get_Id());
            bank.setShort("Cross3_ID", i, (short) candlist.get(i).get(2).get_Id());
            bank.setShort("status", i, (short) candlist.get(i).status);
            bank.setFloat("chi2", i, (float) candlist.get(i).get_FitChi2());
            bank.setShort("ndf", i, (short) candlist.get(i).get_FitNDF());
        }
        //bank.show();
        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return hits bank
     *
     */
    public DataBank fillTBHitsBank(DataEvent event, List<FittedHit> hitlist) {

        DataBank bank = event.createBank("TimeBasedTrkg::TBHits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {
            if (hitlist.get(i).get_Id() == -1) {
                continue;
            }
            bank.setShort("id", i, (short) hitlist.get(i).get_Id());
            bank.setShort("status", i, (short) 1);
            bank.setByte("superlayer", i, (byte) hitlist.get(i).get_Superlayer());
            bank.setByte("layer", i, (byte) hitlist.get(i).get_Layer());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setShort("wire", i, (short) hitlist.get(i).get_Wire());

            bank.setFloat("X", i, (float) hitlist.get(i).get_X());
            bank.setFloat("Z", i, (float) hitlist.get(i).get_Z());
            bank.setByte("LR", i, (byte) hitlist.get(i).get_LeftRightAmb());

            bank.setFloat("time", i, (float) hitlist.get(i).get_Time());
            bank.setFloat("doca", i, (float) hitlist.get(i).get_Doca());
            bank.setFloat("docaError", i, (float) hitlist.get(i).get_DocaErr());
            bank.setFloat("trkDoca", i, (float) hitlist.get(i).get_ClusFitDoca());

            bank.setShort("clusterID", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setByte("trkID", i, (byte) hitlist.get(i).get_AssociatedTBTrackID());
            bank.setFloat("timeResidual", i, (float) hitlist.get(i).get_TimeResidual());

            bank.setFloat("B", i, (float) hitlist.get(i).get_B());

        }
        //System.out.println(" Created Bank "); bank.show();
        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return clusters bank
     */
    public DataBank fillTBClustersBank(DataEvent event, List<FittedCluster> cluslist) {

        DataBank bank = event.createBank("TimeBasedTrkg::TBClusters", cluslist.size());

        int[] hitIdxArray = new int[12];

        for (int i = 0; i < cluslist.size(); i++) {
            if (cluslist.get(i).get_Id() == -1) {
                continue;
            }
            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }
            double chi2 = 0;

            bank.setShort("id", i, (short) cluslist.get(i).get_Id());
            bank.setShort("status", i, (short) 1);
            bank.setByte("superlayer", i, (byte) cluslist.get(i).get_Superlayer());
            bank.setByte("sector", i, (byte) cluslist.get(i).get_Sector());

            bank.setFloat("avgWire", i, (float) cluslist.get(i).getAvgwire());
            bank.setByte("size", i, (byte) cluslist.get(i).size());

            double fitSlope = cluslist.get(i).get_clusterLineFitSlope();
            double fitInterc = cluslist.get(i).get_clusterLineFitIntercept();

            bank.setFloat("fitSlope", i, (float) fitSlope);
            bank.setFloat("fitSlopeErr", i, (float) cluslist.get(i).get_clusterLineFitSlopeErr());
            bank.setFloat("fitInterc", i, (float) fitInterc);
            bank.setFloat("fitIntercErr", i, (float) cluslist.get(i).get_clusterLineFitInterceptErr());

            for (int j = 0; j < cluslist.get(i).size(); j++) {
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = cluslist.get(i).get(j).get_Id();
                }

                double residual = cluslist.get(i).get(j).get_ClusFitDoca() / (cluslist.get(i).get(j).get_CellSize() / Math.sqrt(12.));
                chi2 += residual * residual;
            }
            bank.setFloat("fitChisqProb", i, (float) ProbChi2perNDF.prob(chi2, cluslist.get(i).size() - 2));

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }
        }

        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return segments bank
     */
    public DataBank fillTBSegmentsBank(DataEvent event, List<Segment> seglist) {

        DataBank bank = event.createBank("TimeBasedTrkg::TBSegments", seglist.size());

        int[] hitIdxArray = new int[12];

        for (int i = 0; i < seglist.size(); i++) {
            if (seglist.get(i).get_Id() == -1) {
                continue;
            }

            for (int j = 0; j < hitIdxArray.length; j++) {
                hitIdxArray[j] = -1;
            }

            double chi2 = 0;

            bank.setShort("id", i, (short) seglist.get(i).get_Id());
            bank.setShort("status", i, (short) 1);
            bank.setByte("superlayer", i, (byte) seglist.get(i).get_Superlayer());
            bank.setByte("sector", i, (byte) seglist.get(i).get_Sector());
            FittedCluster cls = seglist.get(i).get_fittedCluster();
            bank.setShort("Cluster_ID", i, (short) cls.get_Id());

            bank.setFloat("avgWire", i, (float) cls.getAvgwire());
            bank.setByte("size", i, (byte) seglist.get(i).size());
            bank.setFloat("fitSlope", i, (float) cls.get_clusterLineFitSlope());
            bank.setFloat("fitSlopeErr", i, (float) cls.get_clusterLineFitSlopeErr());
            bank.setFloat("fitInterc", i, (float) cls.get_clusterLineFitIntercept());
            bank.setFloat("fitIntercErr", i, (float) cls.get_clusterLineFitInterceptErr());

            bank.setFloat("SegEndPoint1X", i, (float) seglist.get(i).get_SegmentEndPoints()[0]);
            bank.setFloat("SegEndPoint1Z", i, (float) seglist.get(i).get_SegmentEndPoints()[1]);
            bank.setFloat("SegEndPoint2X", i, (float) seglist.get(i).get_SegmentEndPoints()[2]);
            bank.setFloat("SegEndPoint2Z", i, (float) seglist.get(i).get_SegmentEndPoints()[3]);

            for (int j = 0; j < seglist.get(i).size(); j++) {
                if (j < hitIdxArray.length) {
                    hitIdxArray[j] = seglist.get(i).get(j).get_Id();
                }

                double residual = seglist.get(i).get(j).get_ClusFitDoca() / (seglist.get(i).get(j).get_CellSize() / Math.sqrt(12.));
                chi2 += residual * residual;
            }
            bank.setFloat("fitChisqProb", i, (float) ProbChi2perNDF.prob(chi2, seglist.get(i).size() - 2));

            for (int j = 0; j < hitIdxArray.length; j++) {
                String hitStrg = "Hit";
                hitStrg += (j + 1);
                hitStrg += "_ID";
                bank.setShort(hitStrg, i, (short) hitIdxArray[j]);
            }
        }

        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return segments bank
     */
    public DataBank fillTBSegmentsTrajectoryBank(DataEvent event, List<Segment> seglist) {
        DataBank bank = event.createBank("TimeBasedTrkg::TBSegmentTrajectory", seglist.size() * 6);

        int index = 0;
        for (int i = 0; i < seglist.size(); i++) {
            if (seglist.get(i).get_Id() == -1) {
                continue;
            }
            SegmentTrajectory trj = seglist.get(i).get_Trajectory();
            for (int l = 0; l < 6; l++) {
                bank.setShort("segmentID", index, (short) trj.get_SegmentId());
                bank.setByte("sector", index, (byte) trj.get_Sector());
                bank.setByte("superlayer", index, (byte) trj.get_Superlayer());
                bank.setByte("layer", index, (byte) (l + 1));
                bank.setShort("matchedHitID", index, (short) trj.getMatchedHitId()[l]);
                bank.setFloat("trkDoca", index, (float) trj.getTrkDoca()[l]);
                index++;
            }
        }
        bank.show();
        return bank;
    }

    /**
     *
     * @param event the EvioEvent
     * @return crosses bank
     */
    public DataBank fillTBCrossesBank(DataEvent event, List<Cross> crosslist) {

        
        int banksize=0;
         for (int i = 0; i < crosslist.size(); i++) {
            if (crosslist.get(i).get_Id() != -1) 
                banksize++;
        }
        DataBank bank = event.createBank("TimeBasedTrkg::TBCrosses", banksize); 
        int index=0;
        for (int i = 0; i < crosslist.size(); i++) {
            if (crosslist.get(i).get_Id() != -1) {              
                bank.setShort("id", index, (short) crosslist.get(i).get_Id());
                bank.setShort("status", index, (short) crosslist.get(i).get_Id());
                bank.setByte("sector", index, (byte) crosslist.get(i).get_Sector());
                bank.setByte("region", index, (byte) crosslist.get(i).get_Region());
                bank.setFloat("x", index, (float) crosslist.get(i).get_Point().x());
                bank.setFloat("y", index, (float) crosslist.get(i).get_Point().y());
                bank.setFloat("z", index, (float) crosslist.get(i).get_Point().z());
                bank.setFloat("err_x", index, (float) crosslist.get(i).get_PointErr().x());
                bank.setFloat("err_y", index, (float) crosslist.get(i).get_PointErr().y());
                bank.setFloat("err_z", index, (float) crosslist.get(i).get_PointErr().z());
                bank.setFloat("ux", index, (float) crosslist.get(i).get_Dir().x());
                bank.setFloat("uy", index, (float) crosslist.get(i).get_Dir().y());
                bank.setFloat("uz", index, (float) crosslist.get(i).get_Dir().z());
                bank.setFloat("err_ux", index, (float) crosslist.get(i).get_DirErr().x());
                bank.setFloat("err_uy", index, (float) crosslist.get(i).get_DirErr().y());
                bank.setFloat("err_uz", index, (float) crosslist.get(i).get_DirErr().z());
                bank.setShort("Segment1_ID", index, (short) crosslist.get(i).get_Segment1().get_Id());
                bank.setShort("Segment2_ID", index, (short) crosslist.get(i).get_Segment2().get_Id());
                index++;
            }
        }
        
        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return segments bank
     */
    public DataBank fillTBTracksBank(DataEvent event, List<Track> candlist) {

        DataBank bank = event.createBank("TimeBasedTrkg::TBTracks", candlist.size());

        for (int i = 0; i < candlist.size(); i++) {
            bank.setShort("id", i, (short) candlist.get(i).get_Id());
            bank.setShort("status", i, (short) 1);
            bank.setByte("sector", i, (byte) candlist.get(i).get_Sector());
            bank.setByte("q", i, (byte) candlist.get(i).get_Q());
            //bank.setFloat("p", i, (float) candlist.get(i).get_P());
            bank.setFloat("c1_x", i, (float) candlist.get(i).get_PreRegion1CrossPoint().x());
            bank.setFloat("c1_y", i, (float) candlist.get(i).get_PreRegion1CrossPoint().y());
            bank.setFloat("c1_z", i, (float) candlist.get(i).get_PreRegion1CrossPoint().z());
            bank.setFloat("c1_ux", i, (float) candlist.get(i).get_PreRegion1CrossDir().x());
            bank.setFloat("c1_uy", i, (float) candlist.get(i).get_PreRegion1CrossDir().y());
            bank.setFloat("c1_uz", i, (float) candlist.get(i).get_PreRegion1CrossDir().z());
            bank.setFloat("c3_x", i, (float) candlist.get(i).get_PostRegion3CrossPoint().x());
            bank.setFloat("c3_y", i, (float) candlist.get(i).get_PostRegion3CrossPoint().y());
            bank.setFloat("c3_z", i, (float) candlist.get(i).get_PostRegion3CrossPoint().z());
            bank.setFloat("c3_ux", i, (float) candlist.get(i).get_PostRegion3CrossDir().x());
            bank.setFloat("c3_uy", i, (float) candlist.get(i).get_PostRegion3CrossDir().y());
            bank.setFloat("c3_uz", i, (float) candlist.get(i).get_PostRegion3CrossDir().z());
            bank.setFloat("t1_x", i, (float) candlist.get(i).get_Region1TrackX().x());
            bank.setFloat("t1_y", i, (float) candlist.get(i).get_Region1TrackX().y());
            bank.setFloat("t1_z", i, (float) candlist.get(i).get_Region1TrackX().z());
            bank.setFloat("t1_px", i, (float) candlist.get(i).get_Region1TrackP().x());
            bank.setFloat("t1_py", i, (float) candlist.get(i).get_Region1TrackP().y());
            bank.setFloat("t1_pz", i, (float) candlist.get(i).get_Region1TrackP().z());
            bank.setFloat("pathlength", i, (float) candlist.get(i).get_TotPathLen());
            bank.setFloat("Vtx0_x", i, (float) candlist.get(i).get_Vtx0().x());
            bank.setFloat("Vtx0_y", i, (float) candlist.get(i).get_Vtx0().y());
            bank.setFloat("Vtx0_z", i, (float) candlist.get(i).get_Vtx0().z());
            bank.setFloat("p0_x", i, (float) candlist.get(i).get_pAtOrig().x());
            bank.setFloat("p0_y", i, (float) candlist.get(i).get_pAtOrig().y());
            bank.setFloat("p0_z", i, (float) candlist.get(i).get_pAtOrig().z());
            bank.setShort("Cross1_ID", i, (short) candlist.get(i).get(0).get_Id());
            bank.setShort("Cross2_ID", i, (short) candlist.get(i).get(1).get_Id());
            bank.setShort("Cross3_ID", i, (short) candlist.get(i).get(2).get_Id());
            bank.setFloat("chi2", i, (float) candlist.get(i).get_FitChi2());
            bank.setShort("ndf", i, (short) candlist.get(i).get_FitNDF());

            // save to a separate bank
            /*
			Matrix covMat = new Matrix(5,5);
			if(candlist.get(i).get_CovMat()!=null)
				covMat = candlist.get(i).get_CovMat();
			
			double[][] c = new double[covMat.getRowDimension()][covMat.getColumnDimension()];		
			
			for(int rw = 0; rw< covMat.getRowDimension(); rw++) {
				for(int cl = 0; cl< covMat.getColumnDimension(); cl++) {
					c[rw][cl] = covMat.get(rw, cl);
				}	
			}
			bank.setFloat("C11", i, (float) c[0][0]);
			bank.setFloat("C12", i, (float) c[0][1]);
			bank.setFloat("C13", i, (float) c[0][2]);
			bank.setFloat("C14", i, (float) c[0][3]);
			bank.setFloat("C15", i, (float) c[0][4]);
			bank.setFloat("C21", i, (float) c[1][0]);
			bank.setFloat("C22", i, (float) c[1][1]);
			bank.setFloat("C23", i, (float) c[1][2]);
			bank.setFloat("C24", i, (float) c[1][3]);
			bank.setFloat("C25", i, (float) c[1][4]);
			bank.setFloat("C31", i, (float) c[2][0]);
			bank.setFloat("C32", i, (float) c[2][1]);
			bank.setFloat("C33", i, (float) c[2][2]);
			bank.setFloat("C34", i, (float) c[2][3]);
			bank.setFloat("C35", i, (float) c[2][4]);
			bank.setFloat("C41", i, (float) c[3][0]);
			bank.setFloat("C42", i, (float) c[3][1]);
			bank.setFloat("C43", i, (float) c[3][2]);
			bank.setFloat("C44", i, (float) c[3][3]);
			bank.setFloat("C45", i, (float) c[3][4]);
			bank.setFloat("C51", i, (float) c[4][0]);
			bank.setFloat("C52", i, (float) c[4][1]);
			bank.setFloat("C53", i, (float) c[4][2]);
			bank.setFloat("C54", i, (float) c[4][3]);
			bank.setFloat("C55", i, (float) c[4][4]);
             */
            //bank.setFloat("fitChisq", i, (float) candlist.get(i).get_FitChi2());
        }
        //bank.show();
        return bank;

    }

    public List<FittedHit> createRawHitList(List<Hit> hits) {

        List<FittedHit> fhits = new ArrayList<FittedHit>();

        for (int i = 0; i < hits.size(); i++) {

            FittedHit fhit = new FittedHit(hits.get(i).get_Sector(), hits.get(i).get_Superlayer(),
                    hits.get(i).get_Layer(), hits.get(i).get_Wire(), hits.get(i).get_Time(),
                    hits.get(i).get_DocaErr(), hits.get(i).get_B(), hits.get(i).get_Id());
            fhit.set_Doca(hits.get(i).get_Doca());
            fhits.add(fhit);
        }
        return fhits;
    }

    public void fillAllHBBanks(DataEvent event, RecoBankWriter rbc, List<FittedHit> fhits, List<FittedCluster> clusters,
            List<Segment> segments, List<Cross> crosses,
            List<Track> trkcands) {

        if (event == null) {
            return;
        }

        if (trkcands != null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits),
                    rbc.fillHBClustersBank(event, clusters),
                    rbc.fillHBSegmentsBank(event, segments),
                    rbc.fillHBCrossesBank(event, crosses),
                    rbc.fillHBTracksBank(event, trkcands)
            );

        }
        if (crosses != null && trkcands == null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits),
                    rbc.fillHBClustersBank(event, clusters),
                    rbc.fillHBSegmentsBank(event, segments),
                    rbc.fillHBCrossesBank(event, crosses)
            );
        }
        if (segments != null && crosses == null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits),
                    rbc.fillHBClustersBank(event, clusters),
                    rbc.fillHBSegmentsBank(event, segments)
            );
        }
        if (clusters != null && segments == null) {

            event.appendBanks(rbc.fillHBHitsBank(event, fhits),
                    rbc.fillHBClustersBank(event, clusters)
            );
        }
        if (fhits != null && clusters == null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits)
            );
        }
    }

    public void fillAllTBBanks(DataEvent event, RecoBankWriter rbc, List<FittedHit> fhits, List<FittedCluster> clusters,
            List<Segment> segments, List<Cross> crosses,
            List<Track> trkcands) {

        if (event == null) {
            return;
        }

        if (trkcands != null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters),
                    rbc.fillTBSegmentsBank(event, segments),
                    rbc.fillTBCrossesBank(event, crosses),
                    rbc.fillTBTracksBank(event, trkcands));

        }
        if (crosses != null && trkcands == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters),
                    rbc.fillTBSegmentsBank(event, segments),
                    rbc.fillTBCrossesBank(event, crosses));
        }
        if (segments != null && crosses == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters),
                    rbc.fillTBSegmentsBank(event, segments));
        }

        if (clusters != null && segments == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters));
        }

        if (fhits != null && clusters == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits));
        }
    }

    public void fillAllHBBanksCalib(DataEvent event, RecoBankWriter rbc, List<FittedHit> fhits, List<FittedCluster> clusters,
            List<Segment> segments, List<Cross> crosses,
            List<Track> trkcands) {

        if (event == null) {
            return;
        }

        if (trkcands != null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits),
                    rbc.fillHBClustersBank(event, clusters),
                    rbc.fillHBSegmentsBank(event, segments),
                    rbc.fillHBSegmentsTrajectoryBank(event, segments),
                    rbc.fillHBCrossesBank(event, crosses),
                    rbc.fillHBTracksBank(event, trkcands)
            );

        }
        if (crosses != null && trkcands == null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits),
                    rbc.fillHBClustersBank(event, clusters),
                    rbc.fillHBSegmentsBank(event, segments),
                    rbc.fillHBSegmentsTrajectoryBank(event, segments),
                    rbc.fillHBCrossesBank(event, crosses)
            );
        }
        if (segments != null && crosses == null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits),
                    rbc.fillHBClustersBank(event, clusters),
                    rbc.fillHBSegmentsBank(event, segments),
                    rbc.fillHBSegmentsTrajectoryBank(event, segments)
            );
        }
        if (clusters != null && segments == null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits),
                    rbc.fillHBClustersBank(event, clusters)
            );
        }
        if (fhits != null && clusters == null) {
            event.appendBanks(rbc.fillHBHitsBank(event, fhits)
            );
        }
    }

    public void fillAllTBBanksCalib(DataEvent event, RecoBankWriter rbc, List<FittedHit> fhits, List<FittedCluster> clusters,
            List<Segment> segments, List<Cross> crosses,
            List<Track> trkcands) {

        if (event == null) {
            return;
        }

        if (trkcands != null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters),
                    rbc.fillTBSegmentsBank(event, segments),
                    rbc.fillTBSegmentsTrajectoryBank(event, segments),
                    rbc.fillTBCrossesBank(event, crosses),
                    rbc.fillTBTracksBank(event, trkcands));
        }
        if (crosses != null && trkcands == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters),
                    rbc.fillTBSegmentsBank(event, segments),
                    rbc.fillTBSegmentsTrajectoryBank(event, segments),
                    rbc.fillTBCrossesBank(event, crosses));
        }
        if (segments != null && crosses == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters),
                    rbc.fillTBSegmentsBank(event, segments),
                    rbc.fillTBSegmentsTrajectoryBank(event, segments));
        }
        if (segments != null && crosses == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters),
                    rbc.fillTBSegmentsBank(event, segments));
        }
        if (clusters != null && segments == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits),
                    rbc.fillTBClustersBank(event, clusters));
        }

        if (fhits != null && clusters == null) {
            event.appendBanks(rbc.fillTBHitsBank(event, fhits));
        }
    }

}
