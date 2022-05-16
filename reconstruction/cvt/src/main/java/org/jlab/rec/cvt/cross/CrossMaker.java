package org.jlab.rec.cvt.cross;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.bmt.BMTConstants;

import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.svt.SVTParameters;

/**
 * Driver class to make crosses
 *
 * @author ziegler
 *
 */
public class CrossMaker {

    public CrossMaker() {

    }

    /**
     *
     * @param clusters clusters
     * @return list of crosses for the SVT and BMT
     */
    public ArrayList<ArrayList<Cross>> findCrosses(List<Cluster> clusters) {
        // instantiate array of clusters that are sorted by detector (SVT, BMT [C, Z]) and inner/outer layers
        ArrayList<ArrayList<Cluster>> sortedClusters = new ArrayList<>();
        // fill the sorted list
        sortedClusters = this.sortClusterByDetectorAndIO(clusters);
        // array indexes: array index 0 (1) = svt inner (outer) layer clusters, 2 (3) = bmt inner (outer) layers
        ArrayList<Cluster> svt_innerlayrclus = sortedClusters.get(0);
        ArrayList<Cluster> svt_outerlayrclus = sortedClusters.get(1);
        ArrayList<Cluster> bmt_Clayrclus = sortedClusters.get(2);
        ArrayList<Cluster> bmt_Zlayrclus = sortedClusters.get(3);
        ArrayList<Cluster> rbmt_Clayrclus = new ArrayList<>();
        ArrayList<Cluster> rbmt_Zlayrclus = new ArrayList<>();
        
        for(Cluster cl : bmt_Zlayrclus) { 
            if(cl.getLayer()==Constants.getInstance().getBMTLayerExcld()
                    && cl.getPhi0()>Math.toRadians(Constants.getInstance().getBMTPhiZRangeExcld()[0][0])
                    && cl.getPhi0()<=Math.toRadians(Constants.getInstance().getBMTPhiZRangeExcld()[0][1]) ) {
                cl.flagForExclusion = true;
                rbmt_Zlayrclus.add(cl); 
            }
        }
        if(bmt_Zlayrclus.size()>0) {
            bmt_Zlayrclus.removeAll(rbmt_Zlayrclus);
        }
        for(Cluster cl : bmt_Clayrclus) { 
            if(cl.getLayer()==Constants.getInstance().getBMTLayerExcld()
                    && cl.getZ()>Constants.getInstance().getBMTPhiZRangeExcld()[1][0]
                    && cl.getZ()<=Constants.getInstance().getBMTPhiZRangeExcld()[1][1]) {
                cl.flagForExclusion = true;
                rbmt_Clayrclus.add(cl); 
            }
        }
        if(bmt_Clayrclus.size()>0) {
            bmt_Clayrclus.removeAll(rbmt_Clayrclus);
        }
        // arrays of BMT and SVT crosses
        ArrayList<Cross> BMTCrosses = this.findBMTCrosses(bmt_Clayrclus, bmt_Zlayrclus,1000);
        ArrayList<Cross> SVTCrosses = this.findSVTCrosses(svt_innerlayrclus, svt_outerlayrclus);
        
        // instantiate the arraylists of sorted Crosses by detector type
        ArrayList<ArrayList<Cross>> sortedCrosses = new ArrayList<ArrayList<Cross>>();
        // array index 0 = SVT crosses, 1 = BMT crosses
        sortedCrosses.add(0, SVTCrosses);
        sortedCrosses.add(1, BMTCrosses);
        // output the sorted array
        return sortedCrosses;
    }

    /**
     *
     * @param svt_innerlayrclus svt inner layer clusters
     * @param svt_outerlayrclus svt outer layer clusters
     * @return the list of SVT crosses reconstructed from clusters in the inner
     * and outer layers in a module
     */
    public ArrayList<Cross> findSVTCrosses(
            List<Cluster> svt_innerlayrclus,
            List<Cluster> svt_outerlayrclus) {
        // instantiate the list of crosses
        ArrayList<Cross> crosses = new ArrayList<>();
        int rid = 0; // cross id
        //loop over the clusters
        // inner clusters
        for (Cluster inlayerclus : svt_innerlayrclus) {
            if(inlayerclus.getTotalEnergy()<SVTParameters.ETOTCUT)
                continue;
            // outer clusters
            for (Cluster outlayerclus : svt_outerlayrclus) {
                if(outlayerclus.getTotalEnergy()<SVTParameters.ETOTCUT)
                    continue;
                // the diffence in layers between outer and inner is 1 for a double layer
                if (outlayerclus.getLayer() - inlayerclus.getLayer() != 1) {
                    continue;
                }
                // the sectors must be the same
                if (outlayerclus.getSector() != inlayerclus.getSector()) {
                    continue;
                }
                
                    // define new cross ))
                // a cut to avoid looping over all strips - from geometry there is a minimum (maximum) strip sum of inner and outer layers that can give a strip intersection
                if ((inlayerclus.getMinStrip() + outlayerclus.getMinStrip() > SVTParameters.MINSTRIPSUM)
                        && (inlayerclus.getMaxStrip() + outlayerclus.getMaxStrip() < SVTParameters.MAXSTRIPSUM)) { // the intersection is valid

                    // define new cross 
                    Cross this_cross = new Cross(DetectorType.BST, BMTType.UNDEFINED, inlayerclus.getSector(), inlayerclus.getRegion(), rid++);
                    // cluster1 is the inner layer cluster
                    this_cross.setCluster1(inlayerclus);
                    // cluster2 is the outer layer cluster
                    this_cross.setCluster2(outlayerclus);
                    this_cross.setId(rid);
                    // sets the cross parameters (point3D and associated error) from the SVT geometry
                    this_cross.updateSVTCross(null); 
                    // the uncorrected point obtained from default estimate that the track is at 90 deg wrt the module should not be null
                    if (this_cross.getPoint0() != null) {
                        //pass the cross to the arraylist of crosses
                        this_cross.setId(crosses.size() + 1);
                        this_cross.setDetector(DetectorType.BST);
                        calcCentErr(this_cross, this_cross.getCluster1());
                        calcCentErr(this_cross, this_cross.getCluster2());
                        crosses.add(this_cross);
                    }

                }
            }
        }
        for (Cross c : crosses) {
            int rg  = c.getRegion();
            c.setOrderedRegion(rg);
        }
        return crosses;
    }

    public void calcCentErr(Cross c, Cluster Cluster1) {
        double Z = Constants.getInstance().SVTGEOMETRY.toLocal(Cluster1.getLayer(),
                                                 Cluster1.getSector(),
                                                 c.getPoint()).z();        
        if(Z>SVTGeometry.getModuleLength()) Z=SVTGeometry.getModuleLength();
        else if(Z<0) Z=0;
        Cluster1.setCentroidError(Cluster1.getResolutionAlongZ(Z) /(SVTGeometry.getPitch() / Math.sqrt(12.)));
        Cluster1.setResolution(Cluster1.getResolutionAlongZ(Z) );
    }
    /**
     *
     * @param Clayrclus C layer BMT clusters
     * @param Zlayrclus Z layer BMT clusters
     * @return list of reconstructed peudocrosses for the BMT which contain
     * measured x,y position for Z and measured z position for C detectors.
     */
    public ArrayList<Cross> findBMTCrosses(
            ArrayList<Cluster> Clayrclus,
            ArrayList<Cluster> Zlayrclus, 
            int idx) {
        //instanciates the list of crosses
        ArrayList<Cross> crosses = new ArrayList<>();

        // For BMT start id at last id from existing list
        int pid = idx;
        for (Cluster Zlayerclus : Zlayrclus) {
            if (Zlayerclus.getTotalEnergy() < BMTConstants.ETOTCUT) {
                continue;
            }
            // Z detector --> meas phi
            // define new cross 
            Cross this_cross = new Cross(DetectorType.BMT, BMTType.Z, Zlayerclus.getSector(), Zlayerclus.getRegion(), pid++);
            this_cross.setId(pid);
            this_cross.setCluster1(Zlayerclus); 
            this_cross.updateBMTCross(null, null);
            if (this_cross.getPoint0() != null) {
                //make arraylist
                crosses.add(this_cross);
            }

        }

        for (Cluster Clayerclus : Clayrclus) {
            if (Clayerclus.getTotalEnergy() < BMTConstants.ETOTCUT) {
                continue;
            }
            // C detector --> meas z
            // define new cross 
            Cross this_cross = new Cross(DetectorType.BMT, BMTType.C, Clayerclus.getSector(), Clayerclus.getRegion(), pid++);
            this_cross.setId(pid);
            this_cross.setCluster1(Clayerclus);
            this_cross.updateBMTCross(null, null);
            if (this_cross.getPoint0() != null) {
                //make arraylist
                crosses.add(this_cross);
            }
        }

        for (Cross c : crosses) {
            int rg  =  3 + 
                    Constants.getInstance().BMTGEOMETRY.getLayer( c.getRegion(), c.getType()) ;
            c.setOrderedRegion(rg);
        }
        return crosses;
        
    }
    
    /**
     *
     * @param clusters the clusters
     * @return arraylist of clusters sorted by detector type and inner/outer
     * layer in a double layer
     */
    public ArrayList<ArrayList<Cluster>> sortClusterByDetectorAndIO(
            List<Cluster> clusters) {

        ArrayList<ArrayList<Cluster>> sortedClusters = new ArrayList<>();
        // svt
        ArrayList<Cluster> svt_innerlayrclus = new ArrayList<>();
        ArrayList<Cluster> svt_outerlayrclus = new ArrayList<>();

        // bmt
        ArrayList<Cluster> bmt_Clayrclus = new ArrayList<>();
        ArrayList<Cluster> bmt_Zlayrclus = new ArrayList<>();

        // Sorting by layer first:
        for (Cluster theclus : clusters) {
            if (theclus.getDetector() == DetectorType.BMT) {
                if (BMTGeometry.getDetectorType(theclus.getLayer()) == BMTType.Z) {
                    bmt_Zlayrclus.add(theclus);
                }

                if (BMTGeometry.getDetectorType(theclus.getLayer()) == BMTType.C) {
                    bmt_Clayrclus.add(theclus); 
                }

            }
            if (theclus.getDetector() == DetectorType.BST) {
                if (theclus.getLayer() % 2 == 0) {
                    svt_outerlayrclus.add(theclus);
                }

                if (theclus.getLayer() % 2 == 1) {
                    svt_innerlayrclus.add(theclus);
                }

            }
        }
        // create the sorted array
        sortedClusters.add(0, svt_innerlayrclus);
        sortedClusters.add(1, svt_outerlayrclus);
        sortedClusters.add(2, bmt_Clayrclus);
        sortedClusters.add(3, bmt_Zlayrclus);

        return sortedClusters;

    }
}
