package org.jlab.rec.cvt.cross;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.cvt.bmt.Geometry;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.svt.Constants;

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
    public ArrayList<ArrayList<Cross>> findCrosses(List<Cluster> clusters, org.jlab.rec.cvt.svt.Geometry svt_geo,
            org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
        // instantiate array of clusters that are sorted by detector (SVT, BMT [C, Z]) and inner/outer layers
        ArrayList<ArrayList<Cluster>> sortedClusters = new ArrayList<ArrayList<Cluster>>();
        // fill the sorted list
        sortedClusters = this.sortClusterByDetectorAndIO(clusters);
        // array indexes: array index 0 (1) = svt inner (outer) layer clusters, 2 (3) = bmt inner (outer) layers
        ArrayList<Cluster> svt_innerlayrclus = sortedClusters.get(0);
        ArrayList<Cluster> svt_outerlayrclus = sortedClusters.get(1);
        ArrayList<Cluster> bmt_Clayrclus = sortedClusters.get(2);
        ArrayList<Cluster> bmt_Zlayrclus = sortedClusters.get(3);
        // arrays of BMT and SVT crosses
        ArrayList<Cross> BMTCrosses = this.findBMTCrosses(bmt_Clayrclus, bmt_Zlayrclus, bmt_geo);
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
            org.jlab.rec.cvt.svt.Geometry svt_geo) {
        // instantiate the list of crosses
        ArrayList<Cross> crosses = new ArrayList<Cross>();
        int rid = 0; // cross id
        //loop over the clusters
        // inner clusters
        for (Cluster inlayerclus : svt_innerlayrclus) {
            if(inlayerclus.get_TotalEnergy()<org.jlab.rec.cvt.svt.Constants.ETOTCUT)
                continue;
            // outer clusters
            for (Cluster outlayerclus : svt_outerlayrclus) {
                if(outlayerclus.get_TotalEnergy()<org.jlab.rec.cvt.svt.Constants.ETOTCUT)
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
                if ((inlayerclus.get_MinStrip() + outlayerclus.get_MinStrip() > Constants.sumStpNumMin)
                        && (inlayerclus.get_MaxStrip() + outlayerclus.get_MaxStrip() < Constants.sumStpNumMax)) { // the intersection is valid

                    // define new cross 
                    Cross this_cross = new Cross("SVT", "", inlayerclus.get_Sector(), inlayerclus.get_Region(), rid++);
                    // cluster1 is the inner layer cluster
                    this_cross.set_Cluster1(inlayerclus);
                    // cluster2 is the outer layer cluster
                    this_cross.set_Cluster2(outlayerclus);
                    this_cross.set_Id(rid);
                    // sets the cross parameters (point3D and associated error) from the SVT geometry
                    this_cross.set_CrossParamsSVT(null, svt_geo); 
                    // the uncorrected point obtained from default estimate that the track is at 90 deg wrt the module should not be null
                    if (this_cross.get_Point0() != null) {
                        //pass the cross to the arraylist of crosses
                        this_cross.set_Id(crosses.size() + 1);
                        this_cross.set_Detector("SVT");
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

    private void calcCentErr(Cross c, Cluster Cluster1, org.jlab.rec.cvt.svt.Geometry svt_geo) {
        double Z = svt_geo.transformToFrame(Cluster1.get_Sector(), Cluster1.get_Layer(), c.get_Point().x(), c.get_Point().y(), c.get_Point().z(), "local", "").z();
        if(Z<0)
            Z=0;
        if(Z>Constants.ACTIVESENLEN)
            Z=Constants.ACTIVESENLEN;
        Cluster1.set_CentroidError(Cluster1.get_ResolutionAlongZ(Z, svt_geo) / (Constants.PITCH / Math.sqrt(12.)));
        Cluster1.set_Error(Cluster1.get_ResolutionAlongZ(Z, svt_geo) );
    }
    /**
     *
     * @param Clayrclus C layer BMT clusters
     * @param Zlayrclus Z layer BMT clusters
     * @return list of reconstructed peudocrosses for the BMT which contain
     * measured x,y position for Z and measured z position for C detectors.
     */
    private ArrayList<Cross> findBMTCrosses(
            ArrayList<Cluster> Clayrclus,
            ArrayList<Cluster> Zlayrclus, 
            org.jlab.rec.cvt.bmt.Geometry bmt_geo) {
        //instanciates the list of crosses
        ArrayList<Cross> crosses = new ArrayList<Cross>();

        // For BMT start id at 1000
        int pid = 1000;
        for (Cluster Zlayerclus : Zlayrclus) {
            if (Zlayerclus.get_TotalEnergy() < org.jlab.rec.cvt.bmt.Constants.ETOTCUT) {
                continue;
            }
            // Z detector --> meas phi
            // define new cross 
            Cross this_cross = new Cross("BMT", "Z", Zlayerclus.get_Sector(), Zlayerclus.get_Region(), pid++);
            this_cross.set_Id(pid);
            this_cross.set_Cluster1(Zlayerclus); // this is the inner shell
            //the uncorrected x,y position of the Z detector cluster centroid.  This is calculated from the measured strips 
            // in the cluster prior to taking Lorentz angle correction into account
            double x0 = (org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[Zlayerclus.get_Region() - 1] + org.jlab.rec.cvt.bmt.Constants.hStrip2Det) * Math.cos(Zlayerclus.get_Phi0());
            double y0 = (org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[Zlayerclus.get_Region() - 1] + org.jlab.rec.cvt.bmt.Constants.hStrip2Det) * Math.sin(Zlayerclus.get_Phi0());
            double x0Er = -org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[Zlayerclus.get_Region() - 1] * Math.sin(Zlayerclus.get_Phi0()) * Zlayerclus.get_PhiErr0();
            double y0Er = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[Zlayerclus.get_Region() - 1] * Math.cos(Zlayerclus.get_Phi0()) * Zlayerclus.get_PhiErr0();
            // set only the coordinates for which there is a measurement
            this_cross.set_Point0(new Point3D(x0, y0, Double.NaN));
            this_cross.set_PointErr0(new Point3D(x0Er, y0Er, Double.NaN));
            //the x,y position of the Z detector cluster centroid.  This is calculated from the Lorentz angle corrected strips 
            double x = (org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[Zlayerclus.get_Region() - 1] + org.jlab.rec.cvt.bmt.Constants.hStrip2Det) * Math.cos(Zlayerclus.get_Phi());
            double y = (org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[Zlayerclus.get_Region() - 1] + org.jlab.rec.cvt.bmt.Constants.hStrip2Det) * Math.sin(Zlayerclus.get_Phi());
            double xEr = -org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[Zlayerclus.get_Region() - 1] * Math.sin(Zlayerclus.get_Phi()) * Zlayerclus.get_PhiErr();
            double yEr = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[Zlayerclus.get_Region() - 1] * Math.cos(Zlayerclus.get_Phi()) * Zlayerclus.get_PhiErr();

            // set only the coordinates for which there is a measurement (x,y)
            this_cross.set_Point(new Point3D(x, y, Double.NaN));
            this_cross.set_PointErr(new Point3D(Math.abs(xEr), Math.abs(yEr), Double.NaN));
            this_cross.set_Cluster1(Zlayerclus);
            if (this_cross.get_Point0() != null) {
                //make arraylist
                crosses.add(this_cross);
            }

        }

        for (Cluster Clayerclus : Clayrclus) {
            if (Clayerclus.get_TotalEnergy() < org.jlab.rec.cvt.bmt.Constants.ETOTCUT) {
                continue;
            }
            // C detector --> meas z
            // define new cross 
            Cross this_cross = new Cross("BMT", "C", Clayerclus.get_Sector(), Clayerclus.get_Region(), pid++);
            this_cross.set_Id(pid);

            // measurement of z
            double z = Clayerclus.get_Z();
            double zErr = Clayerclus.get_ZErr();
            // there is no measurement of x,y, hence only the z component is set
            this_cross.set_Point0(new Point3D(Double.NaN, Double.NaN, z));
            this_cross.set_PointErr0(new Point3D(Double.NaN, Double.NaN, zErr));
            // at this stage there is no additional correction to the measured centroid
            this_cross.set_Point(new Point3D(Double.NaN, Double.NaN, z));
            this_cross.set_PointErr(new Point3D(Double.NaN, Double.NaN, zErr));
            this_cross.set_Cluster1(Clayerclus);
            if (this_cross.get_Point0() != null) {
                //make arraylist
                crosses.add(this_cross);
            }
        }
        //for (Cross c : crosses) {
        //    int rg  = (c.get_Detector().equalsIgnoreCase("BMT"))  ? 3 + 
        //            bmt_geo.getLayer( c.get_Region(), c.get_DetectorType()) : c.get_Region();
        //    c.setOrderedRegion(rg);
        //}
        for (Cross c : crosses) {
            int rg  =  3 + 
                    bmt_geo.getLayer( c.get_Region(), c.get_DetectorType()) ;
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
            if (theclus.get_Detector() == 1) {
                if (Geometry.getZorC(theclus.get_Layer()) == 1) {
                    bmt_Zlayrclus.add(theclus);
                }

                if (Geometry.getZorC(theclus.get_Layer()) == 0) {
                    bmt_Clayrclus.add(theclus);
                }

            }
            if (theclus.get_Detector() == 0) {
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
        int nlayr = Constants.NLAYR;
        // list of crosses in a sector
        ArrayList<ArrayList<ArrayList<Cross>>> secList = new ArrayList<ArrayList<ArrayList<Cross>>>();

        //initialize
        for (int i = 0; i < nlayr; i++) {
            secList.add(i, new ArrayList<ArrayList<Cross>>());
            for (int j = 0; j < Constants.NSECT[i]; j++) {
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
            for (int j = 0; j < Constants.NSECT[i]; j++) {
                //System.out.println(" number of crosses in sector "+(j+1)+" = "+secList.get(i).get(j).size());
                if (secList.get(i).get(j).size() > Constants.MAXNUMCROSSESINMODULE) {
                    listOfCrossesToRm.addAll(secList.get(i).get(j));
                }
            }

        }
        //System.out.println(" number of crosses to remove "+listOfCrossesToRm.size());
        return listOfCrossesToRm;

    }

}
