package org.jlab.rec.dc.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.track.Track;
//import org.jlab.rec.dc.trajectory.SegmentTrajectory;

import trackfitter.fitter.utilities.*;

/**
 * A class to fill the reconstructed DC banks
 *
 * @author ziegler
 *
 */
public class RecoBankWriter {

//    /**
//     *
//     * Writes output banks
//     *
//     */
//    public RecoBankWriter() {
//        // empty constructor
//
//    }

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

    private DataBank fillHBHitsBank(DataEvent event, List<FittedHit> hitlist) {

        DataBank bank = event.createBank("HitBasedTrkg::HBHits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {
            if (hitlist.get(i).get_Id() == -1) {
                continue;
        }

            bank.setShort("id", i, (short) hitlist.get(i).get_Id());
            bank.setShort("status", i, (short) 0);
            bank.setByte("superlayer", i, (byte) hitlist.get(i).get_Superlayer());
            bank.setByte("layer", i, (byte) hitlist.get(i).get_Layer());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setShort("wire", i, (short) hitlist.get(i).get_Wire());
            bank.setFloat("docaError", i, (float) hitlist.get(i).get_DocaErr());
            bank.setFloat("trkDoca", i, (float) hitlist.get(i).get_ClusFitDoca());
            bank.setFloat("LocX", i, (float) hitlist.get(i).get_lX());
            bank.setFloat("LocY", i, (float) hitlist.get(i).get_lY());
            bank.setFloat("X", i, (float) hitlist.get(i).get_X());
            bank.setFloat("Z", i, (float) hitlist.get(i).get_Z());
            bank.setByte("LR", i, (byte) hitlist.get(i).get_LeftRightAmb());
            bank.setShort("clusterID", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setByte("trkID", i, (byte) hitlist.get(i).get_AssociatedHBTrackID());

            bank.setInt("TDC",i,hitlist.get(i).get_TDC());
            bank.setFloat("B", i, (float) hitlist.get(i).getB());
            bank.setFloat("TProp", i, (float) hitlist.get(i).getTProp());
            bank.setFloat("TFlight", i, (float) hitlist.get(i).getTFlight());

            if(hitlist.get(i).get_AssociatedHBTrackID()>-1 && !event.hasBank("MC::Particle")) {
                bank.setFloat("TProp", i, (float) hitlist.get(i).getSignalPropagTimeAlongWire());
                bank.setFloat("TFlight", i, (float) hitlist.get(i).getSignalTimeOfFlight());
            }
        }

        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return clusters bank
     */
    private DataBank fillHBClustersBank(DataEvent event, List<FittedCluster> cluslist) {

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
            int status = 0;
            if(cluslist.get(i).size()<6)
                status = 1;
            bank.setShort("status", i, (short) status);
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
    private DataBank fillHBSegmentsBank(DataEvent event, List<Segment> seglist) {

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
//
//    /**
//     *
//     * @param event the EvioEvent
//     * @return segments bank
//     */
////    private DataBank fillHBSegmentsTrajectoryBank(DataEvent event, List<Segment> seglist) {
//        DataBank bank = event.createBank("HitBasedTrkg::HBSegmentTrajectory", seglist.size() * 6);
//
//        int index = 0;
//        for (Segment aSeglist : seglist) {
//            if (aSeglist.get_Id() == -1) {
//                continue;
//            }
//            SegmentTrajectory trj = aSeglist.get_Trajectory();
//            for (int l = 0; l < 6; l++) {
//                bank.setShort("segmentID", index, (short) trj.get_SegmentId());
//                bank.setByte("sector", index, (byte) trj.get_Sector());
//                bank.setByte("superlayer", index, (byte) trj.get_Superlayer());
//                bank.setByte("layer", index, (byte) (l + 1));
//                bank.setShort("matchedHitID", index, (short) trj.getMatchedHitId()[l]);
//                bank.setFloat("trkDoca", index, (float) trj.getTrkDoca()[l]);
//                index++;
//            }
//        }
//        //bank.show();
//        return bank;
//    }

    /**
     *
     * @param event the EvioEvent
     * @return crosses bank
     */
    private DataBank fillHBCrossesBank(DataEvent event, List<Cross> crosslist) {

        int banksize=0;
        for (Cross aCrosslist1 : crosslist) {
            if (aCrosslist1.get_Id() != -1)
                banksize++;
        }

        DataBank bank = event.createBank("HitBasedTrkg::HBCrosses", banksize);

        int index=0;
        for (Cross aCrosslist : crosslist) {
            if (aCrosslist.get_Id() != -1) {
                bank.setShort("id", index, (short) aCrosslist.get_Id());
                bank.setShort("status", index, (short) 0);
                bank.setByte("sector", index, (byte) aCrosslist.get_Sector());
                bank.setByte("region", index, (byte) aCrosslist.get_Region());
                bank.setFloat("x", index, (float) aCrosslist.get_Point().x());
                bank.setFloat("y", index, (float) aCrosslist.get_Point().y());
                bank.setFloat("z", index, (float) aCrosslist.get_Point().z());
                bank.setFloat("err_x", index, (float) aCrosslist.get_PointErr().x());
                bank.setFloat("err_y", index, (float) aCrosslist.get_PointErr().y());
                bank.setFloat("err_z", index, (float) aCrosslist.get_PointErr().z());
                bank.setFloat("ux", index, (float) aCrosslist.get_Dir().x());
                bank.setFloat("uy", index, (float) aCrosslist.get_Dir().y());
                bank.setFloat("uz", index, (float) aCrosslist.get_Dir().z());
                bank.setFloat("err_ux", index, (float) aCrosslist.get_DirErr().x());
                bank.setFloat("err_uy", index, (float) aCrosslist.get_DirErr().y());
                bank.setFloat("err_uz", index, (float) aCrosslist.get_DirErr().z());
                bank.setShort("Segment1_ID", index, (short) aCrosslist.get_Segment1().get_Id());
                bank.setShort("Segment2_ID", index, (short) aCrosslist.get_Segment2().get_Id());
                index++;
            }
        }
        return bank;
    }

    private DataBank fillHBTracksBank(DataEvent event, List<Track> candlist) {

        DataBank bank = event.createBank("HitBasedTrkg::HBTracks", candlist.size());

        for (int i = 0; i < candlist.size(); i++) {
            bank.setShort("id", i, (short) candlist.get(i).get_Id());
            bank.setByte("sector", i, (byte) candlist.get(i).get_Sector());
            bank.setByte("q", i, (byte) candlist.get(i).get_Q());
            bank.setShort("status", i, (short) (100+candlist.get(i).get_Status()*10+candlist.get(i).get_MissingSuperlayer()));
            if(candlist.get(i).get_PreRegion1CrossPoint()!=null) {
                bank.setFloat("c1_x", i, (float) candlist.get(i).get_PreRegion1CrossPoint().x());
                bank.setFloat("c1_y", i, (float) candlist.get(i).get_PreRegion1CrossPoint().y());
                bank.setFloat("c1_z", i, (float) candlist.get(i).get_PreRegion1CrossPoint().z());
                bank.setFloat("c1_ux", i, (float) candlist.get(i).get_PreRegion1CrossDir().x());
                bank.setFloat("c1_uy", i, (float) candlist.get(i).get_PreRegion1CrossDir().y());
                bank.setFloat("c1_uz", i, (float) candlist.get(i).get_PreRegion1CrossDir().z());
            }
            if(candlist.get(i).get_PostRegion3CrossPoint()!=null) {
                bank.setFloat("c3_x", i, (float) candlist.get(i).get_PostRegion3CrossPoint().x());
                bank.setFloat("c3_y", i, (float) candlist.get(i).get_PostRegion3CrossPoint().y());
                bank.setFloat("c3_z", i, (float) candlist.get(i).get_PostRegion3CrossPoint().z());
                bank.setFloat("c3_ux", i, (float) candlist.get(i).get_PostRegion3CrossDir().x());
                bank.setFloat("c3_uy", i, (float) candlist.get(i).get_PostRegion3CrossDir().y());
                bank.setFloat("c3_uz", i, (float) candlist.get(i).get_PostRegion3CrossDir().z());
            }
            if(candlist.get(i).get_Region1TrackX()!=null) {
                bank.setFloat("t1_x", i, (float) candlist.get(i).get_Region1TrackX().x());
                bank.setFloat("t1_y", i, (float) candlist.get(i).get_Region1TrackX().y());
                bank.setFloat("t1_z", i, (float) candlist.get(i).get_Region1TrackX().z());
                bank.setFloat("t1_px", i, (float) candlist.get(i).get_Region1TrackP().x());
                bank.setFloat("t1_py", i, (float) candlist.get(i).get_Region1TrackP().y());
                bank.setFloat("t1_pz", i, (float) candlist.get(i).get_Region1TrackP().z());
            }
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
            bank.setFloat("x", i, (float) candlist.get(i).getFinalStateVec().x());
            bank.setFloat("y", i, (float) candlist.get(i).getFinalStateVec().y());
            bank.setFloat("z", i, (float) candlist.get(i).getFinalStateVec().getZ());
            bank.setFloat("tx", i, (float) candlist.get(i).getFinalStateVec().tanThetaX());
            bank.setFloat("ty", i, (float) candlist.get(i).getFinalStateVec().tanThetaY());
            
        }
        //bank.show();
        return bank;
    }
    String[][] covMatElts = new String[][] {{"delx_delx", "delx_dely", "delx_delz", "delx_delpx", "delx_delpy", "delx_delpz"},
        {"dely_delx", "dely_dely", "dely_delz", "dely_delpx", "dely_delpy", "dely_delpz"},
        {"delz_delx", "delz_dely", "delz_delz", "delz_delpx", "delz_delpy", "delz_delpz"},
        {"delpx_delx", "delpx_dely", "delpx_delz", "delpx_delpx", "delpx_delpy", "delpx_delpz"},
        {"delpy_delx", "delpy_dely", "delpy_delz", "delpy_delpx", "delpy_delpy", "delpy_delpz"},
        {"delpz_delx", "delpz_dely", "delpz_delz", "delpz_delpx", "delpz_delpy", "delpz_delpz"}
    };
    String[][] covMatEltsSVRep = new String[][] {{"delx_delx", "delx_dely", "delx_deltx", "delx_delty", "delx_delQ"},
        {"dely_delx", "dely_dely", "dely_deltx", "dely_delty", "dely_delQ"},
        {"deltx_delx", "deltx_dely", "deltx_deltx", "deltx_delty", "deltx_delQ"},
        {"delty_delx", "delty_dely", "delty_deltx", "delty_delty", "delty_delQ"},
        {"delQ_delx", "delQ_dely", "delQ_deltx", "delQ_delty", "delQ_delQ"}
    };
    
    /**
     *
     * @param event hipo event
     * @param candlist tracks
     * @return covariance matrix from HB fits to be used for starting TB tracking
     */
    private DataBank fillTrackCovMatBank(DataEvent event, List<Track> candlist) {

        DataBank bank = event.createBank("TimeBasedTrkg::TBCovMat", candlist.size());

        for (int i = 0; i < candlist.size(); i++) {
            bank.setShort("id", i, (short) candlist.get(i).get_Id());
            
            if(candlist.get(i).get_CovMat()!=null) {
                for(int mi = 0; mi <6; mi++) {
                    for(int mj = 0; mj <6; mj++) {
                        bank.setFloat(covMatElts[mi][mj], i, (float) candlist.get(i).get_CovMatLab()[mi][mj]);
                    }
                }
            }
        }
        //bank.show();
        return bank;
    }
    
    private DataBank fillTrackCovMatBankSVRep(DataEvent event, List<Track> candlist) {
        
        DataBank bank = event.createBank("TimeBasedTrkg::TBCovMatSVRep", candlist.size());

        for (int i = 0; i < candlist.size(); i++) {
            bank.setShort("id", i, (short) candlist.get(i).get_Id());
            
            if(candlist.get(i).get_CovMat()!=null) {
                for(int mi = 0; mi <5; mi++) {
                    for(int mj = 0; mj <5; mj++) {
                        bank.setFloat(covMatEltsSVRep[mi][mj], i, (float) candlist.get(i).get_CovMat().get(mi, mj));
                    }
                }
            }
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
     private DataBank fillTBHitsBank(DataEvent event, List<FittedHit> hitlist) {
        if(event.hasBank("TimeBasedTrkg::TBHits")) { // for second pass tracking
                HipoDataEvent de = (HipoDataEvent) event;
               // HipoEvent dde = de.getHipoEvent();
//                HipoGroup group = dde.getGroup("TimeBasedTrkg::TBHits");
                ////event.show();
                //group.show();
                //dde.removeGroup("TimeBasedTrkg::TBHits");
        }
        DataBank bank = event.createBank("TimeBasedTrkg::TBHits", hitlist.size());

        for (int i = 0; i < hitlist.size(); i++) {
            if (hitlist.get(i).get_Id() == -1) {
                continue;
            }
            if(hitlist.get(i).get_TrkResid()==999)
                hitlist.get(i).set_AssociatedTBTrackID(-1);
            bank.setShort("id", i, (short) hitlist.get(i).get_Id());
            bank.setShort("status", i, (short) hitlist.get(i).get_QualityFac());
            bank.setByte("superlayer", i, (byte) hitlist.get(i).get_Superlayer());
            bank.setByte("layer", i, (byte) hitlist.get(i).get_Layer());
            bank.setByte("sector", i, (byte) hitlist.get(i).get_Sector());
            bank.setShort("wire", i, (short) hitlist.get(i).get_Wire());

            bank.setFloat("X", i, (float) hitlist.get(i).get_X());
            bank.setFloat("Z", i, (float) hitlist.get(i).get_Z());
            bank.setByte("LR", i, (byte) hitlist.get(i).get_LeftRightAmb());

            // checks the existing schema to fill the time
            //System.out.println(" has entry "+bank.getDescriptor().hasEntry("time"));
            /*
            correctedTime = (this.get_Time() - this.get_DeltaTimeBeta());
            */
            if(bank.getDescriptor().hasEntry("time")){
               bank.setFloat("time", i, (float) (hitlist.get(i).get_Time() - hitlist.get(i).get_DeltaTimeBeta()));
            }
            if(bank.getDescriptor().hasEntry("tBeta")){
               bank.setFloat("tBeta", i, (float) hitlist.get(i).get_DeltaTimeBeta());
            }
            if(bank.getDescriptor().hasEntry("fitResidual")){
               bank.setFloat("fitResidual", i, (float) hitlist.get(i).get_TrkResid());
            }
            if(bank.getDescriptor().hasEntry("Alpha")){
               bank.setFloat("Alpha", i, (float) hitlist.get(i).getAlpha());
            }
            bank.setFloat("doca", i, (float) hitlist.get(i).get_Doca());
            bank.setFloat("docaError", i, (float) hitlist.get(i).get_DocaErr());
            bank.setFloat("trkDoca", i, (float) hitlist.get(i).get_ClusFitDoca());

            bank.setShort("clusterID", i, (short) hitlist.get(i).get_AssociatedClusterID());
            bank.setByte("trkID", i, (byte) hitlist.get(i).get_AssociatedTBTrackID());
            bank.setFloat("timeResidual", i, (float) hitlist.get(i).get_TimeResidual());
            
            bank.setInt("TDC",i,hitlist.get(i).get_TDC());
            bank.setFloat("B", i, (float) hitlist.get(i).getB());
            bank.setFloat("TProp", i, (float) hitlist.get(i).getTProp());
            bank.setFloat("TFlight", i, (float) hitlist.get(i).getTFlight());
            bank.setFloat("T0", i, (float) hitlist.get(i).getT0());
            bank.setFloat("TStart", i, (float) hitlist.get(i).getTStart());
            if(bank.getDescriptor().hasEntry("beta")){
               bank.setFloat("beta", i, (float) hitlist.get(i).get_Beta());
            }
            if(hitlist.get(i).get_AssociatedTBTrackID()>-1 && !event.hasBank("MC::Particle")) {
                if(hitlist.get(i).getSignalPropagTimeAlongWire()==0 || hitlist.get(i).get_AssociatedTBTrackID()<1) {
                    bank.setFloat("TProp", i, (float) hitlist.get(i).getTProp()); //old value if track fit failed
                } else {
                    bank.setFloat("TProp", i, (float) hitlist.get(i).getSignalPropagTimeAlongWire()); //new calculated value
                }
                if(hitlist.get(i).getSignalTimeOfFlight()==0 || hitlist.get(i).get_AssociatedTBTrackID()<1) {
                    bank.setFloat("TFlight", i, (float) hitlist.get(i).getTFlight());
                } else {
                    bank.setFloat("TFlight", i, (float) hitlist.get(i).getSignalTimeOfFlight());
                }
            }

        }
        //System.out.println(" Created Bank "); bank.show();
        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return clusters bank
     */
    private DataBank fillTBClustersBank(DataEvent event, List<FittedCluster> cluslist) {
        if(event.hasBank("TimeBasedTrkg::TBClusters")) { // for second pass tracking
                HipoDataEvent de = (HipoDataEvent) event;
                //HipoEvent dde = de.getHipoEvent();
//                HipoGroup group = dde.getGroup("TimeBasedTrkg::TBClusters");
                ////event.show();
                //group.show();
                //dde.removeGroup("TimeBasedTrkg::TBClusters");
        }
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
//            int status =0;
            if(cluslist.get(i).size()<6)
//                status = 1;
            bank.setShort("status", i, (short) 0);
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
    private DataBank fillTBSegmentsBank(DataEvent event, List<Segment> seglist) {
        if(event.hasBank("TimeBasedTrkg::TBSegments")) { // for second pass tracking
                HipoDataEvent de = (HipoDataEvent) event;
                //HipoEvent dde = de.getHipoEvent();
//                HipoGroup group = dde.getGroup("TimeBasedTrkg::TBSegments");
                ////event.show();
                //group.show();
                //dde.removeGroup("TimeBasedTrkg::TBSegments");
        }
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
            bank.setShort("status", i, (short) seglist.get(i).get_Status());
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
            bank.setFloat("resiSum", i, (float) seglist.get(i).get_ResiSum());
            bank.setFloat("timeSum", i, (float) seglist.get(i).get_TimeSum());
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

//    /**
//     *
//     * @param event the EvioEvent
//     * @return segments bank
//     */
//    private DataBank fillTBSegmentsTrajectoryBank(DataEvent event, List<Segment> seglist) {
//        if(event.hasBank("TimeBasedTrkg::TBSegmentTrajectory")) { // for second pass tracking
//                HipoDataEvent de = (HipoDataEvent) event;
//                HipoEvent dde = de.getHipoEvent();
////                HipoGroup group = dde.getGroup("TimeBasedTrkg::TBSegmentTrajectory");
//                ////event.show();
//                //group.show();
//                dde.removeGroup("TimeBasedTrkg::TBSegmentTrajectory");
//        }
//        DataBank bank = event.createBank("TimeBasedTrkg::TBSegmentTrajectory", seglist.size() * 6);
//
//        int index = 0;
//        for (Segment aSeglist : seglist) {
//            if (aSeglist.get_Id() == -1) {
//                continue;
//            }
//            SegmentTrajectory trj = aSeglist.get_Trajectory();
//            for (int l = 0; l < 6; l++) {
//                bank.setShort("segmentID", index, (short) trj.get_SegmentId());
//                bank.setByte("sector", index, (byte) trj.get_Sector());
//                bank.setByte("superlayer", index, (byte) trj.get_Superlayer());
//                bank.setByte("layer", index, (byte) (l + 1));
//                bank.setShort("matchedHitID", index, (short) trj.getMatchedHitId()[l]);
//                bank.setFloat("trkDoca", index, (float) trj.getTrkDoca()[l]);
//                index++;
//            }
//        }
//        //bank.show();
//        return bank;
//    }

    /**
     *
     * @param event the EvioEvent
     * @return crosses bank
     */
    private DataBank fillTBCrossesBank(DataEvent event, List<Cross> crosslist) {

        if(event.hasBank("TimeBasedTrkg::TBCrosses")) { // for second pass tracking
                HipoDataEvent de = (HipoDataEvent) event;
                //HipoEvent dde = de.getHipoEvent();
//                HipoGroup group = dde.getGroup("TimeBasedTrkg::TBCrosses");
                ////event.show();
                //group.show();
                //dde.removeGroup("TimeBasedTrkg::TBCrosses");
        }
        int banksize=0;
        for (Cross aCrosslist1 : crosslist) {
            if (aCrosslist1.get_Id() != -1)
                banksize++;
        }
        DataBank bank = event.createBank("TimeBasedTrkg::TBCrosses", banksize);
        int index=0;
        for (Cross aCrosslist : crosslist) {
            if (aCrosslist.get_Id() != -1) {
                bank.setShort("id", index, (short) aCrosslist.get_Id());
                bank.setShort("status", index, (short) (aCrosslist.get_Segment1().get_Status() + aCrosslist.get_Segment2().get_Status()));
                bank.setByte("sector", index, (byte) aCrosslist.get_Sector());
                bank.setByte("region", index, (byte) aCrosslist.get_Region());
                bank.setFloat("x", index, (float) aCrosslist.get_Point().x());
                bank.setFloat("y", index, (float) aCrosslist.get_Point().y());
                bank.setFloat("z", index, (float) aCrosslist.get_Point().z());
                bank.setFloat("err_x", index, (float) aCrosslist.get_PointErr().x());
                bank.setFloat("err_y", index, (float) aCrosslist.get_PointErr().y());
                bank.setFloat("err_z", index, (float) aCrosslist.get_PointErr().z());
                bank.setFloat("ux", index, (float) aCrosslist.get_Dir().x());
                bank.setFloat("uy", index, (float) aCrosslist.get_Dir().y());
                bank.setFloat("uz", index, (float) aCrosslist.get_Dir().z());
                bank.setFloat("err_ux", index, (float) aCrosslist.get_DirErr().x());
                bank.setFloat("err_uy", index, (float) aCrosslist.get_DirErr().y());
                bank.setFloat("err_uz", index, (float) aCrosslist.get_DirErr().z());
                bank.setShort("Segment1_ID", index, (short) aCrosslist.get_Segment1().get_Id());
                bank.setShort("Segment2_ID", index, (short) aCrosslist.get_Segment2().get_Id());
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
    private DataBank fillTBTracksBank(DataEvent event, List<Track> candlist) {
    //    if(event.hasBank("TimeBasedTrkg::TBTracks")) { // for second pass tracking
    //            HipoDataEvent de = (HipoDataEvent) event;
                //HipoEvent dde = de.getHipoEvent();
//                HipoGroup group = dde.getGroup("TimeBasedTrkg::TBTracks");
                ////event.show();
                //group.show();
                //dde.removeGroup("TimeBasedTrkg::TBTracks");
    //    }
        DataBank bank = event.createBank("TimeBasedTrkg::TBTracks", candlist.size());
        for (int i = 0; i < candlist.size(); i++) { 
            bank.setShort("id", i, (short) candlist.get(i).get_Id());
            bank.setShort("status", i, (short) (100+candlist.get(i).get_Status()*10+candlist.get(i).get_MissingSuperlayer()));
            bank.setByte("sector", i, (byte) candlist.get(i).get_Sector());
            bank.setByte("q", i, (byte) candlist.get(i).get_Q());
            //bank.setFloat("p", i, (float) candlist.get(i).get_P());
            if(candlist.get(i).get_PreRegion1CrossPoint()!=null) {
                bank.setFloat("c1_x", i, (float) candlist.get(i).get_PreRegion1CrossPoint().x());
                bank.setFloat("c1_y", i, (float) candlist.get(i).get_PreRegion1CrossPoint().y());
                bank.setFloat("c1_z", i, (float) candlist.get(i).get_PreRegion1CrossPoint().z());
                bank.setFloat("c1_ux", i, (float) candlist.get(i).get_PreRegion1CrossDir().x());
                bank.setFloat("c1_uy", i, (float) candlist.get(i).get_PreRegion1CrossDir().y());
                bank.setFloat("c1_uz", i, (float) candlist.get(i).get_PreRegion1CrossDir().z());
            }
            if(candlist.get(i).get_PostRegion3CrossPoint()!=null) {
                bank.setFloat("c3_x", i, (float) candlist.get(i).get_PostRegion3CrossPoint().x());
                bank.setFloat("c3_y", i, (float) candlist.get(i).get_PostRegion3CrossPoint().y());
                bank.setFloat("c3_z", i, (float) candlist.get(i).get_PostRegion3CrossPoint().z());
                bank.setFloat("c3_ux", i, (float) candlist.get(i).get_PostRegion3CrossDir().x());
                bank.setFloat("c3_uy", i, (float) candlist.get(i).get_PostRegion3CrossDir().y());
                bank.setFloat("c3_uz", i, (float) candlist.get(i).get_PostRegion3CrossDir().z());
            }
            if(candlist.get(i).get_Region1TrackX()!=null) {
                bank.setFloat("t1_x", i, (float) candlist.get(i).get_Region1TrackX().x());
                bank.setFloat("t1_y", i, (float) candlist.get(i).get_Region1TrackX().y());
                bank.setFloat("t1_z", i, (float) candlist.get(i).get_Region1TrackX().z());
                bank.setFloat("t1_px", i, (float) candlist.get(i).get_Region1TrackP().x());
                bank.setFloat("t1_py", i, (float) candlist.get(i).get_Region1TrackP().y());
                bank.setFloat("t1_pz", i, (float) candlist.get(i).get_Region1TrackP().z());
            }
            bank.setFloat("pathlength", i, (float) candlist.get(i).get_TotPathLen());
            bank.setFloat("Vtx0_x", i, (float) candlist.get(i).get_Vtx0().x());
            bank.setFloat("Vtx0_y", i, (float) candlist.get(i).get_Vtx0().y());
            bank.setFloat("Vtx0_z", i, (float) candlist.get(i).get_Vtx0().z());
            bank.setFloat("p0_x", i, (float) candlist.get(i).get_pAtOrig().x());
            bank.setFloat("p0_y", i, (float) candlist.get(i).get_pAtOrig().y());
            bank.setFloat("p0_z", i, (float) candlist.get(i).get_pAtOrig().z());
            if(candlist.get(i).size()==3) {
                bank.setShort("Cross1_ID", i, (short) candlist.get(i).get(0).get_Id());
                bank.setShort("Cross2_ID", i, (short) candlist.get(i).get(1).get_Id());
                bank.setShort("Cross3_ID", i, (short) candlist.get(i).get(2).get_Id());
            }
            if(candlist.get(i).size()==2) {
                bank.setShort("Cross1_ID", i, (short) candlist.get(i).get(0).get_Id());
                bank.setShort("Cross2_ID", i, (short) candlist.get(i).get(1).get_Id());
                bank.setShort("Cross3_ID", i, (short) -1);
            }
            if(candlist.get(i).size()==1) {
                bank.setShort("Cross1_ID", i, (short) candlist.get(i).get(0).get_Id());
                bank.setShort("Cross2_ID", i, (short) -1);
                bank.setShort("Cross3_ID", i, (short) -1);
            }
            bank.setFloat("chi2", i, (float) candlist.get(i).get_FitChi2());
            bank.setShort("ndf", i, (short) candlist.get(i).get_FitNDF());
            
            bank.setFloat("x", i, (float) candlist.get(i).getFinalStateVec().x());
            bank.setFloat("y", i, (float) candlist.get(i).getFinalStateVec().y());
            bank.setFloat("z", i, (float) candlist.get(i).getFinalStateVec().getZ());
            bank.setFloat("tx", i, (float) candlist.get(i).getFinalStateVec().tanThetaX());
            bank.setFloat("ty", i, (float) candlist.get(i).getFinalStateVec().tanThetaY());
            
            
            bank.setFloat("x0", i, (float) candlist.get(i).getOriginStateVec().x());
            bank.setFloat("y0", i, (float) candlist.get(i).getOriginStateVec().y());
            bank.setFloat("z0", i, (float) candlist.get(i).getOriginStateVec().getZ());
            bank.setFloat("tx0", i, (float) candlist.get(i).getOriginStateVec().tanThetaX());
            bank.setFloat("ty0", i, (float) candlist.get(i).getOriginStateVec().tanThetaY());
            
            if(candlist.get(i).getFinalStateVecMC()!=null) {
                bank.setFloat("x_mc", i, (float) candlist.get(i).getFinalStateVecMC().x());
                bank.setFloat("y_mc", i, (float) candlist.get(i).getFinalStateVecMC().y());
                bank.setFloat("z_mc", i, (float) candlist.get(i).getFinalStateVecMC().getZ());
                bank.setFloat("tx_mc", i, (float) candlist.get(i).getFinalStateVecMC().tanThetaX());
                bank.setFloat("ty_mc", i, (float) candlist.get(i).getFinalStateVecMC().tanThetaY());
                
                bank.setFloat("x0_mc", i, (float) candlist.get(i).getOriginStateVecMC().x());
                bank.setFloat("y0_mc", i, (float) candlist.get(i).getOriginStateVecMC().y());
                bank.setFloat("z0_mc", i, (float) candlist.get(i).getOriginStateVecMC().getZ());
                bank.setFloat("tx0_mc", i, (float) candlist.get(i).getOriginStateVecMC().tanThetaX());
                bank.setFloat("ty0_mc", i, (float) candlist.get(i).getOriginStateVecMC().tanThetaY());
            } else {
                bank.setFloat("x_mc", i, (float) -999);
                bank.setFloat("y_mc", i, (float) -999);
                bank.setFloat("z_mc", i, (float) -999);
                bank.setFloat("tx_mc", i, (float) -999);
                bank.setFloat("ty_mc", i, (float) -999);
                
                bank.setFloat("x0_mc", i, (float) -999);
                bank.setFloat("y0_mc", i, (float) -999);
                bank.setFloat("z0_mc", i, (float) -999);
                bank.setFloat("tx0_mc", i, (float) -999);
                bank.setFloat("ty0_mc", i, (float) -999);
            }
            
        }
        //bank.show();
        return bank;

    }

    private DataBank fillTrajectoryBank(DataEvent event, List<Track> tracks) {
        int size=0;
        for (Track track : tracks) {
            if (track == null)
                continue;
            if (track.trajectory == null)
                continue;
            size+=track.trajectory.size();
        }       
        DataBank bank = event.createBank("TimeBasedTrkg::Trajectory", size);
        int i1=0;
        for (Track track : tracks) {
            if (track == null)
                continue;
            if (track.trajectory == null)
                continue;

            for (int j = 0; j < track.trajectory.size(); j++) {
                if (track.trajectory.get(j).getDetName().equals("DC") && (track.trajectory.get(j).getLayerId() - 6) % 6 != 0)
                    continue;  // save the last layer in a superlayer

                bank.setShort("id",       i1, (short) track.get_Id());
                bank.setByte("detector",  i1, (byte) track.trajectory.get(j).getDetId());
                bank.setByte("layer",     i1, (byte) track.trajectory.get(j).getLayerId());
                bank.setFloat("x",        i1, (float) track.trajectory.get(j).getX());
                bank.setFloat("y",        i1, (float) track.trajectory.get(j).getY());
                bank.setFloat("z",        i1, (float) track.trajectory.get(j).getZ());
                bank.setFloat("tx",       i1, (float) ((float) track.trajectory.get(j).getpX() / track.get_P()));
                bank.setFloat("ty",       i1, (float) ((float) track.trajectory.get(j).getpY() / track.get_P()));
                bank.setFloat("tz",       i1, (float) ((float) track.trajectory.get(j).getpZ() / track.get_P()));
                bank.setFloat("B",        i1, (float) track.trajectory.get(j).getiBdl());
                bank.setFloat("path",     i1, (float) track.trajectory.get(j).getPathLen());
                i1++;
            }
        }
        return bank;
    }

    public List<FittedHit> createRawHitList(List<Hit> hits) {

        List<FittedHit> fhits = new ArrayList<>();

        for (Hit hit : hits) {
            FittedHit fhit = new FittedHit(hit.get_Sector(), hit.get_Superlayer(),
                    hit.get_Layer(), hit.get_Wire(), hit.get_TDC(),
                    hit.get_Id());
            fhit.set_Id(hit.get_Id());
            fhit.set_DocaErr(hit.get_DocaErr());
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
                //    rbc.fillTrackCovMatBank(event, trkcands)
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
                    rbc.fillTBTracksBank(event, trkcands),
                    rbc.fillTrackCovMatBank(event, trkcands),
                    rbc.fillTrackCovMatBankSVRep(event, trkcands),
                    rbc.fillTrajectoryBank(event, trkcands));

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
}
