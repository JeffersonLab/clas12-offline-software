package org.jlab.rec.dc.segment;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.clas.math.FastMath;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.trajectory.SegmentTrajectory;

/**
 * A Segment is a fitted cluster that has been pruned of hits with bad residuals
 * (see Constants)
 *
 * @author ziegler
 *
 */
public class SegmentFinder {
    
    /**
     * 
     * @param seg Segment
     * @param event HipoDataEvent
     * @param DcDetector DC detector utility
     */
    public void get_LayerEfficiencies(Segment seg, DataEvent event, DCGeant4Factory DcDetector) {
        if (seg!=null) {
            // get all the hits to obtain layer efficiency
            if (event.hasBank("DC::tdc") != false) {

                DataBank bankDGTZ = event.getBank("DC::tdc");

                int rows = bankDGTZ.rows();
                int[] sector = new int[rows];
                int[] layer = new int[rows];
                int[] wire = new int[rows];
                int[] tdc = new int[rows];

                for (int i = 0; i < rows; i++) {
                    sector[i] = bankDGTZ.getByte("sector", i);
                    layer[i] = bankDGTZ.getByte("layer", i);
                    wire[i] = bankDGTZ.getShort("component", i);
                    tdc[i] = bankDGTZ.getInt("TDC", i);
                }

                int size = layer.length;
                int[] layerNum = new int[size];
                int[] superlayerNum = new int[size];
                //double[] smearedTime = new double[size];

                for (int i = 0; i < size; i++) {
                    superlayerNum[i] = (layer[i] - 1) / 6 + 1;
                    layerNum[i] = layer[i] - (superlayerNum[i] - 1) * 6;

                }

                // Get the Segment Trajectory
                SegmentTrajectory trj = new SegmentTrajectory();
                trj.set_SegmentId(seg.get_Id());
                trj.set_Superlayer(seg.get_Superlayer());
                trj.set_Sector(seg.get_Sector());
                double[] trkDocas = new double[6];
                int[] matchHits = new int[6];

                int[][] matchedHits = new int[3][6]; // first arrays = how many wires off
                for (int i1 = 0; i1 < 3; i1++) {
                    for (int i2 = 0; i2 < 6; i2++) {
                        matchedHits[i1][i2] = -1;
                    }
                }

                for (int l = 0; l < 6; l++) {
                    //double z = GeometryLoader.dcDetector.getSector(0).getSuperlayer(seg.get_Superlayer()-1).getLayer(l).getComponent(0).getMidpoint().z();
                    double z = DcDetector.getWireMidpoint(seg.get_Sector() - 1, seg.get_Superlayer() - 1, l, 0).z;
                    double trkXMP = seg.get_fittedCluster().get_clusterLineFitSlopeMP() * z + seg.get_fittedCluster().get_clusterLineFitInterceptMP();
                    double trkX = seg.get_fittedCluster().get_clusterLineFitSlope() * z + seg.get_fittedCluster().get_clusterLineFitIntercept();

                    if (trkX == 0) {
                        continue; // should always get a cluster fit
                    }
                    int trjWire = trj.getWireOnTrajectory(seg.get_Sector(), seg.get_Superlayer(), l + 1, trkXMP, DcDetector);
                    //double x = GeometryLoader.dcDetector.getSector(0).getSuperlayer(seg.get_Superlayer()-1).getLayer(l).getComponent(trjWire-1).getMidpoint().x();
                    double x = DcDetector.getWireMidpoint(seg.get_Sector() - 1, seg.get_Superlayer() - 1, l, trjWire - 1).x;
                    double cosTkAng = FastMath.cos(Math.toRadians(6.)) * Math.sqrt(1. + seg.get_fittedCluster().get_clusterLineFitSlope() * seg.get_fittedCluster().get_clusterLineFitSlope());
                    double calc_doca = (x - trkX) * cosTkAng;
                    trkDocas[l] = calc_doca;

                    for (int j = 0; j < sector.length; j++) {
                        if (sector[j] == seg.get_Sector() && superlayerNum[j] == seg.get_Superlayer()) {
                            if (layerNum[j] == l + 1) {
                                for (int wo = 0; wo < 2; wo++) {
                                    if (Math.abs(trjWire - wire[j]) == wo) {
                                        matchedHits[wo][l] = (j + 1);
                                    }
                                }
                            }
                        }
                    }
                    matchHits[l] = -1;
                    for (int wo = 0; wo < 2; wo++) {
                        if (matchedHits[wo][l] != -1) {
                            matchHits[l] = matchedHits[wo][l];
                            wo = 2;
                        }
                    }
                }
                trj.setTrkDoca(trkDocas);
                trj.setMatchedHitId(matchHits);

                seg.set_Trajectory(trj);
            }
        }
    }
    /**
     * @param allClusters the list of fitted clusters
     * @param event
     * @param DcDetector
     * @param runLayersEffs
     * @return the list of segments obtained from the clusters
     */
    public List<Segment> get_Segments(List<FittedCluster> allClusters, DataEvent event, DCGeant4Factory DcDetector, boolean runLayersEffs) {
        List<Segment> segList = new ArrayList<>();
        for (FittedCluster fClus : allClusters) {

            if (fClus.size() > Constants.MAXCLUSSIZE) {
                continue;
            }
            if (fClus.get_TrkgStatus() == -1) {
                return segList;
            }

            Segment seg = new Segment(fClus);
            seg.set_fitPlane(DcDetector);
            
            if (runLayersEffs == true)
                this.get_LayerEfficiencies(seg, event, DcDetector);
            
            double sumRes=0;
            double sumTime=0;
            
            for(FittedHit h : seg) {
                sumRes+=h.get_TimeResidual();
                sumTime+=h.get_Time();
            }
            seg.set_ResiSum(sumRes);
            seg.set_TimeSum(sumTime);
            
            segList.add(seg);
        }

        //this.setAssociatedID(segList);
        return segList;

    }

    /**
     * 
     * @param clusters list of fitted clusters
     * @return list of time-based fitted clusters
     */
    public List<FittedCluster> selectTimeBasedSegments(
            List<FittedCluster> clusters) {
        List<FittedCluster> selTimeBasedSegments = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            double AveDoca = 0;
            double AveCelSz = 0;
            for (int j = 0; j < clusters.get(i).size(); j++) {
                AveDoca += clusters.get(i).get(j).get_Doca();
                AveCelSz += clusters.get(i).get(j).get_CellSize();
            }
            if (AveDoca < AveCelSz*1.2) {
                selTimeBasedSegments.add(clusters.get(i));
            }
            
        }
        return selTimeBasedSegments;
    }

}
