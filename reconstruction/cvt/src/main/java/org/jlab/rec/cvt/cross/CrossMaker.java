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
     * @param svt_geo svt geometry
     * @return list of crosses for the SVT and BMT
     */
    public ArrayList<ArrayList<Cross>> findCrosses(List<Cluster> clusters, SVTGeometry svt_geo,
            BMTGeometry bmt_geo) {
        // instantiate array of clusters that are sorted by detector (SVT, BMT [C, Z]) and inner/outer layers
        ArrayList<ArrayList<Cluster>> sortedClusters = new ArrayList<ArrayList<Cluster>>();
        // fill the sorted list
        sortedClusters = this.sortClusterByDetectorAndIO(clusters);
        // array indexes: array index 0 (1) = svt inner (outer) layer clusters, 2 (3) = bmt inner (outer) layers
        ArrayList<Cluster> svt_innerlayrclus = sortedClusters.get(0);
        ArrayList<Cluster> svt_outerlayrclus = sortedClusters.get(1);
        ArrayList<Cluster> bmt_Clayrclus = sortedClusters.get(2);
        ArrayList<Cluster> bmt_Zlayrclus = sortedClusters.get(3);
        ArrayList<Cluster> rbmt_Clayrclus = new ArrayList<Cluster>();
        ArrayList<Cluster> rbmt_Zlayrclus = new ArrayList<Cluster>();
        
        for(Cluster cl : bmt_Zlayrclus) { 
            if(cl.get_Layer()==Constants.getBMTLayerExcld()
                    && cl.get_Phi0()>Math.toRadians(Constants.getBMTPhiZRangeExcld()[0][0])
                    && cl.get_Phi0()<=Math.toRadians(Constants.getBMTPhiZRangeExcld()[0][1]) ) {
                cl.flagForExclusion = true;
                rbmt_Zlayrclus.add(cl); 
            }
        }
        if(bmt_Zlayrclus.size()>0) {
            bmt_Zlayrclus.removeAll(rbmt_Zlayrclus);
        }
        for(Cluster cl : bmt_Clayrclus) { 
            if(cl.get_Layer()==Constants.getBMTLayerExcld()
                    && cl.get_Z()>Constants.getBMTPhiZRangeExcld()[1][0]
                    && cl.get_Z()<=Constants.getBMTPhiZRangeExcld()[1][1]) {
                cl.flagForExclusion = true;
                rbmt_Clayrclus.add(cl); 
            }
        }
        if(bmt_Clayrclus.size()>0) {
            bmt_Clayrclus.removeAll(rbmt_Clayrclus);
        }
        // arrays of BMT and SVT crosses
        ArrayList<Cross> BMTCrosses = this.findBMTCrosses(bmt_Clayrclus, bmt_Zlayrclus, bmt_geo,1000);
        ArrayList<Cross> SVTCrosses = this.findSVTCrosses(svt_innerlayrclus, svt_outerlayrclus, svt_geo);
        
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
     * @param svt_geo svt geometry
     * @return the list of SVT crosses reconstructed from clusters in the inner
     * and outer layers in a module
     */
    public ArrayList<Cross> findSVTCrosses(
            List<Cluster> svt_innerlayrclus,
            List<Cluster> svt_outerlayrclus,
            SVTGeometry svt_geo) {
        // instantiate the list of crosses
        ArrayList<Cross> crosses = new ArrayList<Cross>();
        int rid = 0; // cross id
        //loop over the clusters
        // inner clusters
        for (Cluster inlayerclus : svt_innerlayrclus) {
            if(inlayerclus.get_TotalEnergy()<SVTParameters.ETOTCUT)
                continue;
            // outer clusters
            for (Cluster outlayerclus : svt_outerlayrclus) {
                if(outlayerclus.get_TotalEnergy()<SVTParameters.ETOTCUT)
                    continue;
                // the diffence in layers between outer and inner is 1 for a double layer
                if (outlayerclus.get_Layer() - inlayerclus.get_Layer() != 1) {
                    continue;
                }
                // the sectors must be the same
                if (outlayerclus.get_Sector() != inlayerclus.get_Sector()) {
                    continue;
                }
                
                    // define new cross ))
                // a cut to avoid looping over all strips - from geometry there is a minimum (maximum) strip sum of inner and outer layers that can give a strip intersection
                if ((inlayerclus.get_MinStrip() + outlayerclus.get_MinStrip() > SVTParameters.sumStpNumMin)
                        && (inlayerclus.get_MaxStrip() + outlayerclus.get_MaxStrip() < SVTParameters.sumStpNumMax)) { // the intersection is valid

                    // define new cross 
                    Cross this_cross = new Cross(DetectorType.BST, BMTType.UNDEFINED, inlayerclus.get_Sector(), inlayerclus.get_Region(), rid++);
                    // cluster1 is the inner layer cluster
                    this_cross.set_Cluster1(inlayerclus);
                    // cluster2 is the outer layer cluster
                    this_cross.set_Cluster2(outlayerclus);
                    this_cross.set_Id(rid);
                    // sets the cross parameters (point3D and associated error) from the SVT geometry
                    this_cross.updateSVTCross(null, svt_geo); 
                    // the uncorrected point obtained from default estimate that the track is at 90 deg wrt the module should not be null
                    if (this_cross.get_Point0() != null) {
                        //pass the cross to the arraylist of crosses
                        this_cross.set_Id(crosses.size() + 1);
                        this_cross.set_Detector(DetectorType.BST);
                        calcCentErr(this_cross, this_cross.get_Cluster1(), svt_geo);
                        calcCentErr(this_cross, this_cross.get_Cluster2(), svt_geo);
                        crosses.add(this_cross);
                    }

                }
            }
        }
        for (Cross c : crosses) {
            int rg  = c.get_Region();
            c.setOrderedRegion(rg);
        }
        return crosses;
    }

    private void calcCentErr(Cross c, Cluster Cluster1, SVTGeometry svt_geo) {
        double Z = svt_geo.toLocal(Cluster1.get_Layer(),
                                   Cluster1.get_Sector(),
                                   c.get_Point()).z();        
        if(Z>SVTGeometry.getModuleLength()) Z=SVTGeometry.getModuleLength();
        else if(Z<0) Z=0;
        Cluster1.set_CentroidError(Cluster1.get_ResolutionAlongZ(Z, svt_geo) /(SVTGeometry.getPitch() / Math.sqrt(12.)));
        Cluster1.set_Resolution(Cluster1.get_ResolutionAlongZ(Z, svt_geo) );
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
            BMTGeometry bmt_geo, int idx) {
        //instanciates the list of crosses
        ArrayList<Cross> crosses = new ArrayList<Cross>();

        // For BMT start id at last id from existing list
        int pid = idx;
        for (Cluster Zlayerclus : Zlayrclus) {
            if (Zlayerclus.get_TotalEnergy() < BMTConstants.ETOTCUT) {
                continue;
            }
            // Z detector --> meas phi
            // define new cross 
            Cross this_cross = new Cross(DetectorType.BMT, BMTType.Z, Zlayerclus.get_Sector(), Zlayerclus.get_Region(), pid++);
            this_cross.set_Id(pid);
            this_cross.set_Cluster1(Zlayerclus); 
            this_cross.updateBMTCross(null, null);
            if (this_cross.get_Point0() != null) {
                //make arraylist
                crosses.add(this_cross);
            }

        }

        for (Cluster Clayerclus : Clayrclus) {
            if (Clayerclus.get_TotalEnergy() < BMTConstants.ETOTCUT) {
                continue;
            }
            // C detector --> meas z
            // define new cross 
            Cross this_cross = new Cross(DetectorType.BMT, BMTType.C, Clayerclus.get_Sector(), Clayerclus.get_Region(), pid++);
            this_cross.set_Id(pid);
            this_cross.set_Cluster1(Clayerclus);
            this_cross.updateBMTCross(null, null);
            if (this_cross.get_Point0() != null) {
                //make arraylist
                crosses.add(this_cross);
            }
        }

        for (Cross c : crosses) {
            int rg  =  3 + 
                    bmt_geo.getLayer( c.get_Region(), c.get_Type()) ;
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

        ArrayList<ArrayList<Cluster>> sortedClusters = new ArrayList<ArrayList<Cluster>>();
        // svt
        ArrayList<Cluster> svt_innerlayrclus = new ArrayList<Cluster>();
        ArrayList<Cluster> svt_outerlayrclus = new ArrayList<Cluster>();

        // bmt
        ArrayList<Cluster> bmt_Clayrclus = new ArrayList<Cluster>();
        ArrayList<Cluster> bmt_Zlayrclus = new ArrayList<Cluster>();

        // Sorting by layer first:
        for (Cluster theclus : clusters) {
            if (theclus.get_Detector() == DetectorType.BMT) {
                if (BMTGeometry.getDetectorType(theclus.get_Layer()) == BMTType.Z) {
                    bmt_Zlayrclus.add(theclus);
                }

                if (BMTGeometry.getDetectorType(theclus.get_Layer()) == BMTType.C) {
                    bmt_Clayrclus.add(theclus); 
                }

            }
            if (theclus.get_Detector() == DetectorType.BST) {
                if (theclus.get_Layer() % 2 == 0) {
                    svt_outerlayrclus.add(theclus);
                }

                if (theclus.get_Layer() % 2 == 1) {
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

    /**
     *
     * @param crosses the crosses in the list
     * @return the crosses that have been flagged as being part of a looper
     * candidate in the SVT
     */
    public List<Cross> crossLooperCands(List<ArrayList<Cross>> crosses) {
        // nb SVT layers
        int nlayr = SVTGeometry.NLAYERS;
        // list of crosses in a sector
        ArrayList<ArrayList<ArrayList<Cross>>> secList = new ArrayList<ArrayList<ArrayList<Cross>>>();

        //initialize
        for (int i = 0; i < nlayr; i++) {
            secList.add(i, new ArrayList<ArrayList<Cross>>());
            for (int j = 0; j < SVTGeometry.NSECTORS[i]; j++) {
                secList.get(i).add(j, new ArrayList<Cross>());
            }
        }

        // loop over the crosses in the SVT and use a simple counting algorithm for the number of crosses in a given sector
        // a looper will have a pattern of multiple hits in the same sector
        for (Cross c : crosses.get(0)) { // svt crosses

            int l = c.get_Region() * 2;
            int s = c.get_Sector();

            secList.get(l - 1).get(s - 1).add(c);
        }
        ArrayList<Cross> listOfCrossesToRm = new ArrayList<Cross>();

        for (int i = 0; i < nlayr; i++) {
            for (int j = 0; j < SVTGeometry.NSECTORS[i]; j++) {
                //System.out.println(" number of crosses in sector "+(j+1)+" = "+secList.get(i).get(j).size());
                if (secList.get(i).get(j).size() > SVTParameters.MAXNUMCROSSESINMODULE) {
                    listOfCrossesToRm.addAll(secList.get(i).get(j));
                }
            }

        }
        //System.out.println(" number of crosses to remove "+listOfCrossesToRm.size());
        return listOfCrossesToRm;

    }

}
