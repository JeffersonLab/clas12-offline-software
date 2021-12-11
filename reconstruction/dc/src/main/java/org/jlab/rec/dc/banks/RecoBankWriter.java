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

import trackfitter.fitter.utilities.*;

/**
 * A class to fill the reconstructed DC banks
 *
 * @author ziegler
 *
 */
public class RecoBankWriter {
    
    Banks bankNames = null;
//    /**
//     *
//     * Writes output banks
//     *
//     */
    
    public RecoBankWriter(Banks names) {
        this.bankNames= names;
    }

    public void updateListsWithClusterInfo(List<FittedHit> fhits,
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
        String name = bankNames.getHitsBank();
        
        int rejCnt = 0;
        for (int i = 0; i < hitlist.size(); i++) {
//            if (hitlist.get(i).get_Id() == -1 /*|| hitlist.get(i).get_Id()==0*/) { //PASS1
            if (hitlist.get(i).get_Id() == -1 || hitlist.get(i).get_Id()==0) {
                rejCnt++;
            }
        }
        DataBank bank = event.createBank(name, hitlist.size()-rejCnt);
        rejCnt=0;
        for (int i = 0; i < hitlist.size(); i++) {
//            if (hitlist.get(i).get_Id() == -1 /*|| hitlist.get(i).get_Id()==0*/) { //PASS1
            if (hitlist.get(i).get_Id() == -1 || hitlist.get(i).get_Id()==0) {
                rejCnt++;
                continue;
        }

        bank.setShort("id", i-rejCnt, (short) hitlist.get(i).get_Id());
        bank.setShort("status", i-rejCnt, (short) hitlist.get(i).get_QualityFac());
        bank.setByte("superlayer", i-rejCnt, (byte) hitlist.get(i).get_Superlayer());
        bank.setByte("layer", i-rejCnt, (byte) hitlist.get(i).get_Layer());
        bank.setByte("sector", i-rejCnt, (byte) hitlist.get(i).get_Sector());
        bank.setShort("wire", i-rejCnt, (short) hitlist.get(i).get_Wire());
        bank.setFloat("docaError", i-rejCnt, (float) hitlist.get(i).get_DocaErr());
        bank.setFloat("trkDoca", i-rejCnt, (float) hitlist.get(i).get_ClusFitDoca());
        bank.setFloat("LocX", i-rejCnt, (float) hitlist.get(i).get_lX());
        bank.setFloat("LocY", i-rejCnt, (float) hitlist.get(i).get_lY());
        bank.setFloat("X", i-rejCnt, (float) hitlist.get(i).get_X());
        bank.setFloat("Z", i-rejCnt, (float) hitlist.get(i).get_Z());
        bank.setByte("LR", i-rejCnt, (byte) hitlist.get(i).get_LeftRightAmb());
        bank.setShort("clusterID", i-rejCnt, (short) hitlist.get(i).get_AssociatedClusterID());
        bank.setInt("TDC",i-rejCnt,hitlist.get(i).get_TDC());
        
    }

    return bank;

    }   
    
    public DataBank fillHBHitsTrkIdBank(DataEvent event, List<FittedHit> hitlist) {
        String name = bankNames.getIdsBank(); 
        int rejCnt = 0;
        for (int i = 0; i < hitlist.size(); i++) {
//            if (hitlist.get(i).get_AssociatedHBTrackID() == -1 /*|| hitlist.get(i).get_Id()==0*/ //PASS1
//                    /*|| hitlist.get(i).getTFlight()==0*/) {
            if (hitlist.get(i).get_AssociatedHBTrackID() == -1 || hitlist.get(i).get_Id()==0 ){
//                    || hitlist.get(i).getTFlight()==0) {
                rejCnt++;
            }
        }
        DataBank bank = event.createBank(name, hitlist.size()-rejCnt);
        rejCnt=0;
        for (int i = 0; i < hitlist.size(); i++) {
            //output only for HOTs
//            if (hitlist.get(i).get_AssociatedHBTrackID() == -1 || hitlist.get(i).get_Id()==0 //PASS1
//                    /*|| hitlist.get(i).getTFlight()==0*/) {
            if (hitlist.get(i).get_AssociatedHBTrackID() == -1 || hitlist.get(i).get_Id()==0 ) {
//                    || hitlist.get(i).getTFlight()==0) {
                rejCnt++;
                continue;
            } 
            bank.setShort("id", i-rejCnt, (short) hitlist.get(i).get_Id());
            bank.setShort("tid", i-rejCnt, (short) hitlist.get(i).get_AssociatedHBTrackID());
            bank.setFloat("B", i-rejCnt, (float) hitlist.get(i).getB());
            bank.setFloat("TProp", i-rejCnt, (float) hitlist.get(i).getTProp());
            bank.setFloat("TFlight", i-rejCnt, (float) hitlist.get(i).getTFlight());
//            if(i>0 && hitlist.get(i).get_Sector()==hitlist.get(i-1).get_Sector() && 
//                     hitlist.get(i).get_Superlayer()==hitlist.get(i-1).get_Superlayer() && 
//                     hitlist.get(i).get_Layer()==hitlist.get(i-1).get_Layer() && 
//                     Math.abs(hitlist.get(i).get_Wire()-hitlist.get(i-1).get_Wire())==1 &&
//                     hitlist.get(i-1).getTFlight()>0) {// fix for double hits
//                bank.setFloat("B", i-rejCnt, (float) hitlist.get(i-1).getB());
//                bank.setFloat("TProp", i-rejCnt, (float) hitlist.get(i-1).getTProp());
//                bank.setFloat("TFlight", i-rejCnt, (float) hitlist.get(i-1).getTFlight());
//            }
//            if(hitlist.get(i).get_AssociatedHBTrackID()>-1 && !event.hasBank("MC::Particle")) {
//                bank.setFloat("TProp", i-rejCnt, (float) hitlist.get(i).getSignalPropagTimeAlongWire());
//                bank.setFloat("TFlight", i-rejCnt, (float) hitlist.get(i).getSignalTimeOfFlight());
//                if(i>0 && hitlist.get(i).get_Sector()==hitlist.get(i-1).get_Sector() && 
//                     hitlist.get(i).get_Superlayer()==hitlist.get(i-1).get_Superlayer() && 
//                     hitlist.get(i).get_Layer()==hitlist.get(i-1).get_Layer() && 
//                     Math.abs(hitlist.get(i).get_Wire()-hitlist.get(i-1).get_Wire())==1 &&
//                     hitlist.get(i-1).getTFlight()>0) {// fix for double hits
//                    bank.setFloat("TProp", i-rejCnt, (float) hitlist.get(i-1).getSignalPropagTimeAlongWire());
//                    bank.setFloat("TFlight", i-rejCnt, (float) hitlist.get(i-1).getSignalTimeOfFlight());
//                }
//            }
        }
    //bank.show();
    return bank;

}
/**
 *
 * @param event the EvioEvent
     * @param cluslist
 * @return clusters bank
 */
public DataBank fillHBClustersBank(DataEvent event, List<FittedCluster> cluslist) {
    String name = bankNames.getClustersBank();
    DataBank bank = event.createBank(name, cluslist.size());

    int[] hitIdxArray = new int[12];
    if(cluslist==null)
        return bank;
    for (int i = 0; i < cluslist.size(); i++) {
        if (cluslist.get(i) == null || cluslist.get(i).get_Id() == -1) {
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
     * @param seglist
     * @return segments bank
     */
    public DataBank fillHBSegmentsBank(DataEvent event, List<Segment> seglist) {
        String name = bankNames.getSegmentsBank();
        DataBank bank = event.createBank(name, seglist.size());
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
     * @param crosslist
     * @return crosses bank
     */
    public DataBank fillHBCrossesBank(DataEvent event, List<Cross> crosslist) {

        int banksize=0;
        for (Cross aCrosslist1 : crosslist) {
            if (aCrosslist1.get_Id() != -1)
                banksize++;
        }
        String name = bankNames.getCrossesBank();
        DataBank bank = event.createBank(name, banksize); 

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

    public DataBank fillHBTracksBank(DataEvent event, List<Track> candlist) {
        String name = bankNames.getTracksBank();
        DataBank bank = event.createBank(name, candlist.size()); 

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
            //fill associated IDs
            for(int r = 0; r < 3; r++) {
                bank.setShort("Cross"+String.valueOf(r+1)+"_ID", 
                    i, (short) -1);
            }
            for(int r = 0; r < 6; r++) {
                bank.setShort("Cluster"+String.valueOf(r+1)+"_ID", 
                    i, (short) -1);
            }
            for(int k = 0; k < candlist.get(i).size(); k++) {
                bank.setShort("Cross"+String.valueOf(candlist.get(i).get(k).get_Region())+"_ID", 
                    i, (short) candlist.get(i).get(k).get_Id());
                bank.setShort("Cluster"+String.valueOf(candlist.get(i).get(k).get_Region()*2-1)+"_ID", 
                        i, (short) candlist.get(i).get(k).get_Segment1().get_Id());
                bank.setShort("Cluster"+String.valueOf(candlist.get(i).get(k).get_Region()*2)+"_ID", 
                        i, (short) candlist.get(i).get(k).get_Segment2().get_Id());
            }
            if(candlist.get(i).getSingleSuperlayer()!=null) {
                bank.setShort("Cluster"+String.valueOf(candlist.get(i).getSingleSuperlayer().get_Superlayer())+"_ID", 
                        i, (short) candlist.get(i).getSingleSuperlayer().get_fittedCluster().get_Id());
            }
                
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
    /**
     *
     * @param event hipo event
     * @param candlist tracks
     * @return covariance matrix from HB fits to be used for starting TB tracking
     */
    private DataBank fillTrackCovMatBank(DataEvent event, List<Track> candlist) {

        DataBank bank = event.createBank(bankNames.getCovmatBank(), candlist.size());

        for (int i = 0; i < candlist.size(); i++) {
            bank.setShort("id", i, (short) candlist.get(i).get_Id());
            if(candlist.get(i).get_CovMat()!=null) {
                bank.setFloat("C11", i, (float) candlist.get(i).get_CovMat().get(0, 0));
                bank.setFloat("C12", i, (float) candlist.get(i).get_CovMat().get(0, 1));
                bank.setFloat("C13", i, (float) candlist.get(i).get_CovMat().get(0, 2));
                bank.setFloat("C14", i, (float) candlist.get(i).get_CovMat().get(0, 3));
                bank.setFloat("C15", i, (float) candlist.get(i).get_CovMat().get(0, 4));
                bank.setFloat("C21", i, (float) candlist.get(i).get_CovMat().get(1, 0));
                bank.setFloat("C22", i, (float) candlist.get(i).get_CovMat().get(1, 1));
                bank.setFloat("C23", i, (float) candlist.get(i).get_CovMat().get(1, 2));
                bank.setFloat("C24", i, (float) candlist.get(i).get_CovMat().get(1, 3));
                bank.setFloat("C25", i, (float) candlist.get(i).get_CovMat().get(1, 4));
                bank.setFloat("C31", i, (float) candlist.get(i).get_CovMat().get(2, 0));
                bank.setFloat("C32", i, (float) candlist.get(i).get_CovMat().get(2, 1));
                bank.setFloat("C33", i, (float) candlist.get(i).get_CovMat().get(2, 2));
                bank.setFloat("C34", i, (float) candlist.get(i).get_CovMat().get(2, 3));
                bank.setFloat("C35", i, (float) candlist.get(i).get_CovMat().get(2, 4));
                bank.setFloat("C41", i, (float) candlist.get(i).get_CovMat().get(3, 0));
                bank.setFloat("C42", i, (float) candlist.get(i).get_CovMat().get(3, 1));
                bank.setFloat("C43", i, (float) candlist.get(i).get_CovMat().get(3, 2));
                bank.setFloat("C44", i, (float) candlist.get(i).get_CovMat().get(3, 3));
                bank.setFloat("C45", i, (float) candlist.get(i).get_CovMat().get(3, 4));
                bank.setFloat("C51", i, (float) candlist.get(i).get_CovMat().get(4, 0));
                bank.setFloat("C52", i, (float) candlist.get(i).get_CovMat().get(4, 1));
                bank.setFloat("C53", i, (float) candlist.get(i).get_CovMat().get(4, 2));
                bank.setFloat("C54", i, (float) candlist.get(i).get_CovMat().get(4, 3));
                bank.setFloat("C55", i, (float) candlist.get(i).get_CovMat().get(4, 4));
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
        String name = bankNames.getHitsBank();
        DataBank bank = event.createBank(name, hitlist.size());
        
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
        return bank;

    }

    /**
     *
     * @param event the EvioEvent
     * @return clusters bank
     */
    private DataBank fillTBClustersBank(DataEvent event, List<FittedCluster> cluslist) {
        String name = bankNames.getClustersBank();
        DataBank bank = event.createBank(name, cluslist.size());
        
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
//            if(cluslist.get(i).size()<6)
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
        String name = bankNames.getSegmentsBank();
        DataBank bank = event.createBank(name, seglist.size());
        
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


    /**
     *
     * @param event the EvioEvent
     * @return crosses bank
     */
    private DataBank fillTBCrossesBank(DataEvent event, List<Cross> crosslist) {
        int banksize=0;
        for (Cross aCrosslist1 : crosslist) {
            if (aCrosslist1.get_Id() != -1)
                banksize++;
        }
        String name = bankNames.getCrossesBank();
        DataBank bank = event.createBank(name, banksize);
        
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
       
        String name = bankNames.getTracksBank(); 
        DataBank bank = event.createBank(name, candlist.size());
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
           //fill associated IDs
            for(int r = 0; r < 3; r++) {
                bank.setShort("Cross"+String.valueOf(r+1)+"_ID", 
                    i, (short) -1);
            }
            for(int r = 0; r < 6; r++) {
                bank.setShort("Cluster"+String.valueOf(r+1)+"_ID", 
                    i, (short) -1);
            }
            for(int k = 0; k < candlist.get(i).size(); k++) {
                bank.setShort("Cross"+String.valueOf(candlist.get(i).get(k).get_Region())+"_ID", 
                    i, (short) candlist.get(i).get(k).get_Id());
                bank.setShort("Cluster"+String.valueOf(candlist.get(i).get(k).get_Region()*2-1)+"_ID", 
                        i, (short) candlist.get(i).get(k).get_Segment1().get_Id());
                bank.setShort("Cluster"+String.valueOf(candlist.get(i).get(k).get_Region()*2)+"_ID", 
                        i, (short) candlist.get(i).get(k).get_Segment2().get_Id());
            }
            if(candlist.get(i).getSingleSuperlayer()!=null) {
                bank.setShort("Cluster"+String.valueOf(candlist.get(i).getSingleSuperlayer().get_Superlayer())+"_ID", 
                        i, (short) candlist.get(i).getSingleSuperlayer().get_fittedCluster().get_Id());
            }
            bank.setFloat("chi2", i, (float) candlist.get(i).get_FitChi2());
            bank.setShort("ndf", i, (short) candlist.get(i).get_FitNDF());
        }
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
        DataBank bank = event.createBank(bankNames.getTrajBank(), size);
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

    public void fillAllHBBanks(DataEvent event, List<FittedHit> fhits, List<FittedCluster> clusters,
            List<Segment> segments, List<Cross> crosses,
            List<Track> trkcands) {

        if (event == null) {
            return;
        }

        if (trkcands != null) {
            event.appendBanks(this.fillHBHitsBank(event, fhits),
                    this.fillHBClustersBank(event, clusters),
                    this.fillHBSegmentsBank(event, segments),
                    this.fillHBCrossesBank(event, crosses),
                    this.fillHBTracksBank(event, trkcands),
                    this.fillHBHitsTrkIdBank(event, fhits)
                    //this.fillTrackCovMatBank(event, trkcands)
            );

        }
        else if (crosses != null && trkcands == null) {
            event.appendBanks(this.fillHBHitsBank(event, fhits),
                    this.fillHBClustersBank(event, clusters),
                    this.fillHBSegmentsBank(event, segments),
                    this.fillHBCrossesBank(event, crosses)
            );
        }
        else if (segments != null && crosses == null) {
            event.appendBanks(this.fillHBHitsBank(event, fhits),
                    this.fillHBClustersBank(event, clusters),
                    this.fillHBSegmentsBank(event, segments)
            );
        }
        else if (clusters != null && segments == null) {

            event.appendBanks(this.fillHBHitsBank(event, fhits),
                    this.fillHBClustersBank(event, clusters)
            );
        }
        else if (fhits != null && clusters == null) {
            event.appendBanks(this.fillHBHitsBank(event, fhits)
            );
        }
    }

    public void fillAllTBBanks(DataEvent event, List<FittedHit> fhits, List<FittedCluster> clusters,
            List<Segment> segments, List<Cross> crosses,
            List<Track> trkcands) {

        if (event == null) {
            return;
        }

        if (trkcands != null) {
            event.appendBanks(this.fillTBHitsBank(event, fhits),
                    this.fillTBClustersBank(event, clusters),
                    this.fillTBSegmentsBank(event, segments),
                    this.fillTBCrossesBank(event, crosses),
                    this.fillTBTracksBank(event, trkcands),
                    this.fillTrajectoryBank(event, trkcands),
                    this.fillTrackCovMatBank(event, trkcands)
                    );
        }
        if (crosses != null && trkcands == null) {
            event.appendBanks(this.fillTBHitsBank(event, fhits),
                    this.fillTBClustersBank(event, clusters),
                    this.fillTBSegmentsBank(event, segments),
                    this.fillTBCrossesBank(event, crosses));
        }
        if (segments != null && crosses == null) {
            event.appendBanks(this.fillTBHitsBank(event, fhits),
                    this.fillTBClustersBank(event, clusters),
                    this.fillTBSegmentsBank(event, segments));
        }

        if (clusters != null && segments == null) {
            event.appendBanks(this.fillTBHitsBank(event, fhits),
                    this.fillTBClustersBank(event, clusters));
        }

        if (fhits != null && clusters == null) {
            event.appendBanks(this.fillTBHitsBank(event, fhits));
        }
    }
}
