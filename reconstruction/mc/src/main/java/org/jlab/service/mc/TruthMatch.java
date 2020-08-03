package org.jlab.service.mc;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import java.util.*;

/**
 *
 * @author rafopar
 *
 *
 */
/**
 *
 * Another approach for making truth matching that will include neutral
 * particles as well The "TruthMatching" class works well for charged particles,
 * but for neutrals it requires to introduce some ad-hoc functions. So this is
 * an attempt to make it to look more general.
 */
public class TruthMatch extends ReconstructionEngine {

    /**
     * We introduce this variable which will show whether there is a neutral
     * particle in the MC::Particle bank. In case all particles are charged then
     * we don't need to use all secondary hits, but instead we will use hits
     * that are created directly from the MC particle
     */
    private boolean hasNeutral;

    public TruthMatch() {
        super("TruthMatch", "Rafo", "0.0");
        hasNeutral = false;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {

        // check if the event contains the MC banks
        if (event.hasBank("MC::True") == false) {
            System.err.print(" [ WARNING, TruthMatching ]: no MC::True bank found");
            if (event.hasBank("RUN::config")) {
                System.err.print(" ******* EVENT " + event.getBank("RUN::config").getInt("event", 0));
            }
            System.err.println();
            return false;
        }

        if (event.hasBank("MC::Particle") == false) {
            System.err.println(" [ WARNING, TruthMatching ]: no MC::Particle bank found");
            return false;
        }

        /**
         * ********************************************************
         * The 1st thing, let's load MC particles
         * ********************************************************
         */
        Map<Short, MCPart> mcp = getMCparticles(event.getBank("MC::Particle"));

        /**
         * ********************************************************
         * Now let's get True hits from the MC::True bank
         * ********************************************************
         */
        Map<Byte, Map<Integer, MCHit>> mchits = getMCHits(event.getBank("MC::True"), mcp);

        /**
         * Now let's get ECal Rec hits. The RecHit object should, has the -
         * hitID (the same as the hitn in MCHit) - cID : clusterId of the
         * cluster to which the hit belogs to - pindex : pindex of the particle
         * that the current cluster belongs to.
         */
        Map< Short, List<RecHit>> ecalHits = getECalHits(event);

        List<RecCluster> ecalClusters = getECalClusters(event);

        /**
         * Matchingg clusters to MCParticles
         */
        MatchClasters(ecalClusters, ecalHits, mchits.get((byte) DetectorType.ECAL.getDetectorId()));

        /**
         *
         *
         */
        return true;
    }

    /**
     * ************************************************************************
     * Defining objects that will be needed int the matching process
     * ************************************************************************
     */
    // MCParticle from MC::Particle bank
    class MCPart {

        public int id;      // index of the MC particle (it should correspond of tid/otid
        // (still needs to be defined which one) in MC::True)

        public int pid;     // PDG ID code
    }

    // True hit information from the MC::True banl
    class MCHit {

        public int pid;      // MC particle id (pdg code)
        public int otid;     // id of the original (gernerated) particle that eventually caused the hit
        public int hitn;     // Hit id: it corresponds to the position of rec hits.
        public byte detector; // Detector code descriptor
    }

    // RecHit object
    class RecHit {

        public int id;          // ID: This is corresponding ADC row number in the ADC bank.
        public short pindex;      // pindex
        public short cid;         // clusterID
        public byte detector;     // Detector specifier
    }

    class RecCluster {

        public RecCluster() {
            nHitMatched = 0;
            mcotid = -1;
        }
        public short id;            // cluster id
        public short mcotid;         // mc track id
        public short rectid;        // rec track id
        public short pindex;        // index of the rec particle
        public int nHitMatched;   // number of hits MC matched
        public short size;          // number of hits
        public byte detector;
        public byte layer;
        public byte superlayer;
        public byte sector;
    }

    /**
     * ************************************************************************
     * Defining utility functions
     * ************************************************************************
     */
    /**
     *
     * @param MC::Particle bank
     *
     * @return map of MCpart objects, where the Key is the index of the MC
     * particle in the MC::Particle bank
     */
    Map<Short, MCPart> getMCparticles(DataBank mcpart) {

        Map<Short, MCPart> mcp = new HashMap<>();

        for (int i = 0; i < mcpart.rows(); i++) {

            MCPart curPart = new MCPart();

            curPart.id = i;
            curPart.pid = mcpart.getInt("pid", i);

            /**
             * Check if there is a neutral particle in the MC::Particle
             */
            if (curPart.pid == 22 /*photon*/ || curPart.pid == 2112/*Neutron*/) {
                hasNeutral = true;
            }

            mcp.put((short) (i), curPart);
        }
        return mcp;

    }

    Map<Byte, Map<Integer, MCHit>> getMCHits(DataBank mctrue, Map<Short, MCPart> mcp) {

        Map<Byte, Map<Integer, MCHit>> dmchits = new HashMap<>();

        for (int i = 0; i < mctrue.rows(); i++) {
            MCHit hit = new MCHit();
            hit.pid = mctrue.getInt("pid", i);
            hit.otid = mctrue.getInt("otid", i) - 1;
            hit.hitn = mctrue.getInt("hitn", i) - 1;
            hit.detector = mctrue.getByte("detector", i);

            int tid = mctrue.getInt("tid", i) - 1;  // in MC::True tid starts 
            //  from one, so subtracting 1 to match to the row number in the MC::Particle bank
            int mtid = mctrue.getInt("mtid", i);

            /**
             * In the original version of Truth Matching, before adding the
             * MC::True hit into the returned hit list, it checks whether the
             * tid (track id) of a hit is the id of one of MCPartciles
             * (generated particles), i.e. whether the hit is directly produced
             * from the originally generated MCParticle. This is good for
             * tracking detectors, as many hits unrelated (not directly related)
             * to the track, will not be followed.
             *
             * In calorimeters however lots of particle creation and killing is
             * ongoing, and only small fraction of hits (if any) is actually
             * created directly from the original particle, but rather are
             * created from daughters/granddaughters of the MCParticle.
             *
             * So for this purpose we have to keep all hits for calorimeters, if
             * the original particle is either photon or neutron, otherwise hits
             * in the ECAL or FTCak will not be kept.
             *
             * For tracking detectors we will throw non-direct hits from MC
             * particle
             *
             */
            if (hit.detector != (byte) DetectorType.ECAL.getDetectorId() && hit.detector != (byte) DetectorType.FTCAL.getDetectorId()
                    && mcp.get((short) tid) == null) {
                continue;
            } else if (mcp.get((short) (hit.otid)).pid != 22 && mcp.get((short) (hit.otid)).pid != 2112) {
                continue;
            }

            /**
             * We can check whether the particle or it's mother is original
             * particle. This can reduce the number of hits. Subject further
             * studies... if( mcp.get( (short) tid ) == null && mcp.get( (short)
             * mtid ) == null ) continue;
             */
            if (dmchits.get(hit.detector) == null) {
                dmchits.put(hit.detector, new HashMap<>());
            }
            dmchits.get(hit.detector).put(hit.hitn, hit);

        }
        return dmchits;
    }

    Map< Short, List<RecHit>> getECalHits(DataEvent event) {

        /**
         * We need two banks to be present in the event: ECAL::hits and
         * REC::Calorimeter "id" and "cid (clusterId)" will be obtained from the
         * ECAL::hits bank, but in order to find the pindex of particle we need
         * to loop over the REC::Calorimeter bank and create a map
         * <cId, pindex>.
         */
        Map< Short, List<RecHit>> recHits = new HashMap<>();

        /**
         * Check if two necessary banks existm otherwise will return null
         */
        if (event.hasBank("ECAL::hits") == false) {
            return null;
        }

        if (event.hasBank("REC::Calorimeter") == false) {
            return null;
        }

        Map<Short, Short> clId2Pindex = new HashMap<>();
        DataBank RecCalBank = event.getBank("REC::Calorimeter");

        for (int ical = 0; ical < RecCalBank.rows(); ical++) {

            Short pindex = RecCalBank.getShort("pindex", ical);
            Short index = RecCalBank.getShort("index", ical);

            clId2Pindex.put(index, pindex);
        }

        DataBank hitsBank = event.getBank("ECAL::hits");

//        for (Short theKey : clId2Pindex.keySet()) {
//            System.out.println("The Key of clId2Pindex is " + theKey + "The value is " + clId2Pindex.get(theKey));
//        }
        for (int ihit = 0; ihit < hitsBank.rows(); ihit++) {
            RecHit curHit = new RecHit();

            curHit.id = hitsBank.getShort("id", ihit);
            curHit.cid = (short) (hitsBank.getShort("clusterId", ihit) - 1);  // -1 for starting from 0
            if (curHit.cid == -2) {
                continue; // The hit is not part of any cluster, 
                // and hence is not related to any rec particle
            }
            //System.out.println("Inside the hit loop:  the cid of the hit is " + curHit.cid);
            curHit.pindex = clId2Pindex.get(curHit.cid);
            curHit.detector = (byte) DetectorType.ECAL.getDetectorId();

            if (recHits.get(curHit.cid) == null) {
                recHits.put(curHit.cid, new ArrayList<>());
            }

            recHits.get(curHit.cid).add(curHit);
        }

        return recHits;
    }

    List<RecCluster> getECalClusters(DataEvent event) {

        List<RecCluster> cls = new ArrayList<>();

        /**
         * We need the bank REC::Calorimeter, so as a first thing we will check
         * if the bank exist
         */
        if (event.hasBank("REC::Calorimeter") == false) {
            return cls;
        }

        DataBank recCal = event.getBank("REC::Calorimeter");

        for (int iCl = 0; iCl < recCal.rows(); iCl++) {
            RecCluster curCl = new RecCluster();

            curCl.id = recCal.getShort("index", iCl);
            curCl.pindex = recCal.getShort("pindex", iCl);
            curCl.detector = recCal.getByte("detector", iCl);
            curCl.layer = recCal.getByte("layer", iCl);
            curCl.sector = recCal.getByte("sector", iCl);

            curCl.size = -1;    // For ECal clusters this is not particularly important, // We don't have this in the bank
            curCl.rectid = -1;  // We will not use ECal clusters for tracks.
            curCl.superlayer = -1; // Not applicable for ECal clusters

            cls.add(curCl);
        }

        return cls;
    }

    /**
     *
     * @param cls: List of clusters for a given detector
     * @param Rechits_a : Map<clId, ListRecHis>, i.e. list of hits for each
     * cluster
     * @param mchits : Map<hitn, mchit>, map of mc hits, where the Key is the
     * "hitn"
     */
    void MatchClasters(List<RecCluster> cls, Map< Short, List<RecHit>> Rechits_a, Map<Integer, MCHit> mchits) {

        if (cls == null) {
            return;
        }

        System.out.println("Size of Clusters is " + cls.size());
        if (cls.size() >= 1) {
            PrintRecHits(Rechits_a);
        }

        int ind = 0;
        for (RecCluster cl : cls) {

            /**
             * The Key of the map is the MCParticleindex, that created a given
             * hit in a cluster, while the value of the map shows # of hits in
             * the cluster from that MCParticle
             */
            Map<Integer, Integer> matchMCParts = new HashMap<>();

            List<RecHit> recHits = Rechits_a.get(cl.id);

            System.out.println(" ====== Printing the " + ind + "-th cluster ==========");
            ind = ind + 1;
            PrintRecCluster(cl);

            if (recHits == null) {
                System.out.println("Oo, recHits is Null");
                continue;
                //System.out.println(" ====== # of hit in a cluster " + cl.id + " is " + recHits.size() + " =========");
            }

            for (RecHit curRecHit : recHits) {

                MCHit mchit = mchits.get(curRecHit.id);

                if (mchit != null) {
                    //cl.nHitMatched = (short) (cl.nHitMatched + (short) 1);

                    if (matchMCParts.get(mchit.otid) == null) {
                        matchMCParts.put(mchit.otid, 1);
                    } else {
                        matchMCParts.put(mchit.otid, matchMCParts.get(mchit.otid) + 1);
                    }
                }

            }

            /**
             * Loop over all hits is finished, so let's check, if there are more
             * than one MCParticle is associated with the cluster, chose the one
             * which has largest # of hits.
             */
            Map.Entry<Integer, Integer> maxEntry = null;
            for (Map.Entry<Integer, Integer> entry : matchMCParts.entrySet()) {
                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                    maxEntry = entry;
                }
            }

            if (maxEntry != null) {
                cl.nHitMatched = maxEntry.getValue();
                System.out.println("Final otid is  " + maxEntry.getKey() + "and # of matched hits is " + maxEntry.getValue());
            }

        }

    }

    /**
     * Some Utility functions
     */
    void PrintRecHits(Map< Short, List<RecHit>> Rechits_a) {

        System.out.println("******************* Map of Rec Hits ******************************");

        for (Map.Entry<Short, List<RecHit>> entry : Rechits_a.entrySet()) {
            System.out.println("**  ======= The key (clusterID) : is " + entry.getKey() + " ======= ");

            for (int ilist = 0; ilist < entry.getValue().size(); ilist++) {

                System.out.println("**  *id* of the " + ilist + "-th RecHit is " + entry.getValue().get(ilist).id);
                System.out.println("**  *pindex of the " + ilist + "-th RecHit is " + entry.getValue().get(ilist).pindex);
                System.out.println("**  *cid* of the " + ilist + "-th RecHit is " + entry.getValue().get(ilist).cid);
                System.out.println("**  *detector* of the " + ilist + "-th RecHit is " + entry.getValue().get(ilist).detector);
                System.out.println("**");
            }
        }

        System.out.println("******************* Map of Rec Hits ******************************");

    }

    void PrintRecCluster(RecCluster cl) {

        System.out.println("******************* RecCluster ******************************");
        System.out.println("** id is                " + cl.id);
        System.out.println("** mctid is             " + cl.mcotid);
        System.out.println("** rectid is            " + cl.rectid);
        System.out.println("** pindex is            " + cl.pindex);
        System.out.println("** nHittMatched is      " + cl.nHitMatched);
        System.out.println("** size is              " + cl.size);
        System.out.println("** detector is          " + cl.detector);
        System.out.println("** layer is             " + cl.layer);
        System.out.println("** superlayer is        " + cl.superlayer);
        System.out.println("** sector is            " + cl.sector);
        System.out.println("******************* RecCluster ******************************");
    }
}
