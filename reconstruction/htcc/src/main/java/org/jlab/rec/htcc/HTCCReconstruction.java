package org.jlab.rec.htcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.groups.IndexedTable;

/**
 * Performs hit clustering for HTTC reconstruction.
 *
 * @author J. Hankins
 * @author A. Puckett
 * @author G. Gavalian
 */
public class HTCCReconstruction {

    // HTCC geometry parameters
    public IndexedTable gain;
    public IndexedTable time;
    public IndexedTable ring_time;
    public IndexedTable cluster_par;
    public IndexedTable status;
    public IndexedTable geometry;

    // Raw HTCC data from the bank
    private int[] hitnArray;
    private int[] sectorArray;
    private int[] ringArray;
    private int[] halfArray;
    private double[] npheArray;
    private double[] timeArray;
    private int[] ithetaArray;
    private int[] iphiArray;
    private int numHits;

    // Data about the hit in the remaining hit list with the greatest number of
    // photoelections. See findMaximumHit().
    private double maxHitNumPhotoelectrons;
    private int maxHitRemainingIndex;
    private int maxHitRawDataIndex;

    /**
     * Initializes the HTCCReconstruction.
     */
    public HTCCReconstruction() {
    }

    /**
     * Clusters hits in the given event.
     *
     * @param event the event containing hits to cluster
     */
    public void processEvent(DataEvent event) {
        // Load the raw data about the event
        readBankInput(event);
        // Initialize the remaining hits list
        List<Integer> remainingHits = intiRemainingHitList();

        // Place all of the hits into clusters
        List<HTCCCluster> clusters = new ArrayList();
        HTCCCluster cluster;
        while (remainingHits.size() > 0 && (cluster = findCluster(remainingHits)) != null) {
            clusters.add(cluster);
        }

        // Push all of the clusters into the bank and print the results
        fillBankResults(clusters, event);
    }

    /**
     * Reads hit information from the given event out of the bank.
     *
     * @param event the event under analysis
     */
    void readBankInput(DataEvent event) {
        if (!event.hasBank("HTCC::adc")) {
            return;
        }
        DataBank bankDGTZ = event.getBank("HTCC::adc");

        if (bankDGTZ.rows() == 0) {
            return;
        }
        int rows = bankDGTZ.rows();

        hitnArray = new int[rows];
        sectorArray = new int[rows];;
        ringArray = new int[rows];;
        halfArray = new int[rows];;
        npheArray = new double[rows];;
        timeArray = new double[rows];
        ithetaArray = new int[rows];
        iphiArray = new int[rows];
        for (int i = 0; i < bankDGTZ.rows(); i++) {
            //      hitnArray[i]   = bankDGTZ.getInt("hitn", i);
            sectorArray[i] = bankDGTZ.getByte("sector", i);
            ringArray[i] = bankDGTZ.getShort("component", i);
            halfArray[i] = bankDGTZ.getByte("layer", i);
            timeArray[i] = bankDGTZ.getFloat("time", i);

            if (sectorArray[i] > 0) {
                npheArray[i] = ((bankDGTZ.getInt("ADC", i))) / (gain.getDoubleValue("gain", sectorArray[i], halfArray[i], ringArray[i]));
                timeArray[i] = timeArray[i] - (time.getDoubleValue("shift", sectorArray[i], halfArray[i], ringArray[i]));
            }

        }
        numHits = sectorArray.length;
        // Create and fill ithetaArray and iphiArray so that the itheta and iphi
        // values are not calculated more than once
        ithetaArray = new int[numHits];
        iphiArray = new int[numHits];
        for (int hit = 0; hit < numHits; ++hit) {
            ithetaArray[hit] = Math.abs(ringArray[hit]) - 1;
            int iphi = 2 * Math.abs(sectorArray[hit]) + Math.abs(halfArray[hit]) - 3;
            iphi = (iphi == 0 ? iphi + 12 : iphi) - 1;
            iphiArray[hit] = iphi;

        }
    }

    /**
     * Returns a list of of the indexes of the hits whose number of
     * photoelectrons surpasses the minimum number of photoelectrons specified
     * by in <code>parameters</code>.
     *
     * @return a list of hit indexes
     */
    List<Integer> intiRemainingHitList() {
        List<Integer> remainingHits = new ArrayList();

        // Find all hits above the photoelectron threshold
        for (int hit = 0; hit < numHits; ++hit) {
            if(    npheArray[hit] > cluster_par.getDoubleValue("npheminhit", 0,0,0) 
                && sectorArray[hit] > 0
                && status.getIntValue("status", sectorArray[hit],halfArray[hit],ringArray[hit])==0) {
                remainingHits.add(hit);
            }
        }

        return remainingHits;
    }

    /**
     * Returns the next cluster or null if no clusters are left.
     *
     * @param remainingHits the list of remaining hits
     * @return the next cluster or null if no clusters are left
     */
    HTCCCluster findCluster(List<Integer> remainingHits) {
        // Note:
        // maxHitNumPhotoelectrons : the number of photoelectrons for the maximum hit
        // maxHitRawDataIndex : the index of the hit in the raw data
        // maxHitRemainingIndex : the index of the hit in the remaining hits list

        // Find the hit from the list of remaining hits with the largest number 
        // of photoelectrons that also meets the threshold for the minimum 
        // number of photoelectrons specified by cluster_par.npheminmax
        findMaximumHit(remainingHits);

        // If a maximum hit was found:
        if (maxHitNumPhotoelectrons > -1) {

            // Remove the maximum hit from the list of remaining hits
            remainingHits.remove(maxHitRemainingIndex);

            // Get Hit Data:
            // Detector Indicies
            int itheta = ithetaArray[maxHitRawDataIndex];
            int iphi = iphiArray[maxHitRawDataIndex];
            // Numver of Photoelectrons
            double nphe = maxHitNumPhotoelectrons;
            // Hit Time
            double time = timeArray[maxHitRawDataIndex] - ring_time.getDoubleValue("offset", 0,0,itheta+1);
            // Detector Coordinates (polar)
            double theta = Math.toRadians(geometry.getDoubleValue("theta0", 0,0,0)+2*geometry.getDoubleValue("dtheta", 0,0,0)*itheta);
            double phi   = Math.toRadians(geometry.getDoubleValue("phi0", 0,0,0)  +2*geometry.getDoubleValue("dphi", 0,0,0)*iphi);
            // Detector Alignment Errors
            double dtheta = Math.toRadians(geometry.getDoubleValue("dtheta", 0,0,0));
            double dphi   = Math.toRadians(geometry.getDoubleValue("dphi", 0,0,0));
 
            // Create a new cluster and add the maximum hit
            HTCCCluster cluster = new HTCCCluster();
            cluster.addHit(itheta, iphi, nphe, time, theta, phi, dtheta, dphi);
            // Recursively grow the cluster by adding nearby hits
            growCluster(cluster, remainingHits);

            //Check whether this cluster has nphe above threshold, size along theta and phi and total number of hits less than maximum:
            if (cluster.getNPheTot() >= cluster_par.getDoubleValue("npheminclst", 0,0,0)
                    && cluster.getNThetaClust() <= cluster_par.getDoubleValue("nthetamaxclst", 0,0,0)
                    && cluster.getNPhiClust() <= cluster_par.getDoubleValue("nphimaxclst", 0,0,0)
                    && cluster.getNHitClust() <= cluster_par.getDoubleValue("nhitmaxclst", 0,0,0)) {

                // Return the cluster
                return cluster;
            }
        }

        // There are no clusters left, so return null
        return null;
    }

    /**
     * Finds the hit from the list of remaining hits with the largest number of
     * photoelectrons that also meets the threshold for the minimum number of
     * photoelectrons specified in <code>parameters</code>.
     * <p>
     * Side effects: If a maximum hit was found with a number of photo electrons
     * greater than or equal to <code>cluster_par.npheminmax</code>, then:
     * maxHitNumPhotoelectrons = the number of photoelectrons for the max hit
     * maxHitRawDataIndex = the index of the max hit in the bank data
     * maxHitRemainingIndex = the index of the max hit in the remaining hits
     * list
     * <p>
     * If no remaining hit has a number of photoelectrons greater than or equal
     * to <code>cluster_par.npheminmax</code>, then: maxHitNumPhotoelectrons = -1
     * maxHitRawDataIndex = -1 maxHitRemainingIndex = -1
     *
     * @param remainingHits the list of remaining hits
     */
    void findMaximumHit(List<Integer> remainingHits) {
        maxHitNumPhotoelectrons = -100;
        maxHitRemainingIndex = -100;
        maxHitRawDataIndex = -100;
        for (int hit = 0; hit < remainingHits.size(); ++hit) {
            int hitIndex = remainingHits.get(hit);
            double numPhotoElectrons = npheArray[hitIndex];
            if (numPhotoElectrons >= cluster_par.getDoubleValue("npheminmax", 0,0,0)
                    && numPhotoElectrons > maxHitNumPhotoelectrons) {
                maxHitNumPhotoelectrons = numPhotoElectrons;
                maxHitRemainingIndex = hit;
                maxHitRawDataIndex = hitIndex;
            }
        }
    }

    /**
     * Grows the given cluster by adding nearby hits from the remaining hits
     * list. As hits are added to the cluster they are removed from the
     * remaining hits list.
     *
     * @param cluster the cluster to grow
     * @param remainingHits the list of indexes of the remaining hits
     */
    void growCluster(HTCCCluster cluster, List<Integer> remainingHits) {
        // Get the average time of the cluster
        double clusterTime = cluster.getTime();
        // For each hit in the cluster:
        for (int currHit = 0; currHit < cluster.getNHitClust(); ++currHit) {
            // Get the hits coordinates
            int ithetaCurr = cluster.getHitITheta(currHit);
            int iphiCurr = cluster.getHitIPhi(currHit);

            // For each of the remaining hits:
            int hit = 0;
            while (hit < remainingHits.size()) {
                // Get the index of the remaining hit (and call it a test hit)
                int testHit = remainingHits.get(hit);
                // Get the coordinates of the test hit
                int ithetaTest = ithetaArray[testHit];
                int iphiTest = iphiArray[testHit];

                // Find the distance
                int ithetaDiff = Math.abs(ithetaTest - ithetaCurr);
                int iphiDiff = Math.min((12 + iphiTest - iphiCurr) % 12, (12 + iphiCurr - iphiTest) % 12);

                // Find the difference in time
                double time = timeArray[testHit] - ring_time.getDoubleValue("offset", 0,0,ithetaTest+1);
                double timeDiff = Math.abs(time - clusterTime);

                double npheTest = npheArray[testHit];
                // If the test hit is close enough in space and time
                if ((ithetaDiff == 1 || iphiDiff == 1)
                        && (ithetaDiff + iphiDiff <= 2)
                        && (timeDiff <= cluster_par.getDoubleValue("maxtimediff", 0,0,0))) {
                    // Remove the hit from the remaining hits list
                    remainingHits.remove(hit);
                    // Get the Numeber of Photoelectrons
                    npheTest = npheArray[testHit];
                    // Get the Detector Coordinates (polar)
                    double thetaTest = Math.toRadians(geometry.getDoubleValue("theta0", 0,0,0)+2*geometry.getDoubleValue("dtheta", 0,0,0)*ithetaTest);
                    double phiTest   = Math.toRadians(geometry.getDoubleValue("phi0", 0,0,0)  +2*geometry.getDoubleValue("dphi", 0,0,0)*iphiTest);
                    // Detector Alignment Errors
                    double dthetaTest = Math.toRadians(geometry.getDoubleValue("dtheta", 0,0,0));
                    double dphiTest   = Math.toRadians(geometry.getDoubleValue("dphi", 0,0,0));
                    // Add the hit to the cluster
                    cluster.addHit(ithetaTest, iphiTest, npheTest, time, thetaTest, phiTest, dthetaTest, dphiTest);
                    // Get the new average time of the cluster
                    clusterTime = cluster.getTime();
                } else {
                    // Go to the next hit in the remaining hits list
                    hit++;
                }
            }
        }
    }

    /**
     * Pushes
     *
     * @param clusters the output clusters
     * @param event the event under analysis
     */
    void fillBankResults(List<HTCCCluster> clusters, DataEvent event) {
        // Determine the size of the output
        int size = clusters.size();

        if (size == 0) {
            return;
        }

        // Create the output bank
        DataBank bankClusters = event.createBank("HTCC::rec", size);

        // Fill the output bank
        for (int i = 0; i < size; ++i) {
            HTCCCluster cluster = clusters.get(i);
            bankClusters.setShort("id", i, (short) 0);
            bankClusters.setShort("nhits", i, (short) cluster.getNHitClust());
            bankClusters.setShort("ntheta", i, (short) cluster.getNThetaClust());
            bankClusters.setShort("nphi", i, (short) cluster.getNPhiClust());
            bankClusters.setShort("mintheta", i, (short) cluster.getIThetaMin());
            bankClusters.setShort("maxtheta", i, (short) cluster.getIThetaMax());
            bankClusters.setShort("minphi", i, (short) cluster.getIPhiMin());
            bankClusters.setShort("maxphi", i, (short) cluster.getIPhiMax());
            bankClusters.setFloat("nphe", i, (float) cluster.getNPheTot());
            bankClusters.setFloat("time", i, (float) cluster.getTime());
            bankClusters.setFloat("theta", i, (float) cluster.getTheta());
            bankClusters.setFloat("phi", i, (float) cluster.getPhi());
            bankClusters.setFloat("dtheta", i, (float) cluster.getDTheta());
            bankClusters.setFloat("dphi", i, (float) cluster.getDPhi());
            bankClusters.setFloat("x", i, (float) 0.1 * cluster.getX());
            bankClusters.setFloat("y", i, (float) 0.1 * cluster.getY());
            bankClusters.setFloat("z", i, (float) 0.1 * cluster.getZ());

        }

        // Push the results into the bank
        event.appendBanks(bankClusters);
        // Display the results
//        System.out.printSystef("\n[Detector-HTCC] >>>> Input hits %8d Output Clusters %8d\n", numHits, clusters.size());
//        bankClusters.show();
    }

//    /**
//     * Contains the HTCC reconstruction parameters.
//     */
//    class ReconstructionParameters {
//
//        double theta0[];
//        double dtheta0[];
//        double phi0;
//        double dphi0;
//        int npeminclst;
//        int npheminmax;
//        int npheminhit;
//        int nhitmaxclst;
//        int nthetamaxclst;
//        int nphimaxclst;
//        double maxtimediff;
//        double t0[];
//
//        /**
//         * Initialize reconstruction parameters with sensible defaults.
//         */
//        ReconstructionParameters() {
//            theta0 = new double[]{8.75, 16.25, 23.75, 31.25};
//            dtheta0 = new double[]{3.75, 3.75, 3.75, 3.75};
//            for (int i = 0; i < 4; ++i) {
//                theta0[i] = Math.toRadians(theta0[i]);
//                dtheta0[i] = Math.toRadians(dtheta0[i]);
//            }
//            phi0 = Math.toRadians(15.0);
//            dphi0 = Math.toRadians(15.0);
//            npeminclst = 1;
//            npheminmax = 1;
//            npheminhit = 1;
//            nhitmaxclst = 4;
//            nthetamaxclst = 2;
//            nphimaxclst = 2;
//            //defaul value
//            //maxtimediff = 2;
//            maxtimediff = 8;
//
//            t0 = new double[]{11.54, 11.93, 12.33, 12.75};
//        }
//
//        /**
//         * Initialize reconstruction parameters from a packed string.
//         *
//         * @param packed_string the packed string
//         * @throws UnsupportedOperationException
//         */
//        ReconstructionParameters(String packed_string) {
//            // TODO if necessary
//            throw new UnsupportedOperationException();
//        }
//    }

    /**
     * Main routine for testing.
     *
     * The environment variable $CLAS12DIR must be set and point to a directory
     * that contains lib/bankdefs/clas12/<dictionary file name>.xml
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        String inputfile = "out.ev";
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputfile);

        HTCCReconstruction htccRec = new HTCCReconstruction();
        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            htccRec.processEvent(event);
        }
    }
}
