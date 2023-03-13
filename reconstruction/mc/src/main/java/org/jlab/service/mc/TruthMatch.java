package org.jlab.service.mc;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.clas.pdg.PDGDatabase;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafopar
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
        this.chargedPIDs = new ArrayList<>(Arrays.asList(ELECTRON_ID, PROTON_ID, POSITRON_ID, PIPLUS_ID, PIMINUS_ID, KPLUS_ID, KMINUS_ID, MUMINUS_ID, MUPLUS_ID));
        hasNeutral = false;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {

        //System.out.println("========================================= Event "
        //        + event.getBank("RUN::config").getInt("event", 0) + " =========================================");
        // check if the event contains the MC banks
        if (event.hasBank("MC::True") == false) {
//            System.err.print(" [ WARNING, TruthMatching ]: no MC::True bank found");
//            if (event.hasBank("RUN::config")) {
//                System.err.print(" ******* EVENT " + event.getBank("RUN::config").getInt("event", 0));
//            }
//            System.err.println();
            return false;
        }

        if (event.hasBank("MC::Particle") == false) {
//            System.err.println(" [ WARNING, TruthMatching ]: no MC::Particle bank found");
            return false;
        }

        if (event.hasBank("REC::Particle") == false) {
            return false;
        }

        /**
         * ********************************************************
         * The 1st thing, let's load MC particles
         * ********************************************************
         */
        // <index, MCPart>
        Map<Short, MCPart> mcp = getMCparticles(event.getBank("MC::Particle"));

        // <index, RecPart>
        Map<Short, RecPart> recp = getRecparticles(event.getBank("REC::Particle"));

        /**
         * ********************************************************
         * Now let's get True hits from the MC::True bank
         * ********************************************************
         */
        // <Detector, <hitn, MCHit>>
        Map<Byte, Map<Integer, MCHit>> mchits = getMCHits(event.getBank("MC::True"), mcp);

        /**
         * We need a conversion from trkID to the index of a track in the
         * Rec::Track banks It is needed for CVT detectors and DC for two
         * different methods, so in order to avoid multiple times of defining
         * such a map in these function, instead we will make it here, and pass
         * it as an argument to corresponding functions.
         */
        Map<Integer, Map<Short, Integer>> trkID2Index = MaketrkID2IndexMap(event);

        /**
         * Now let's get ECal Rec hits. The RecHit object should, has the -
         * hitID (the same as the hitn in MCHit) - cID : clusterId of the
         * cluster to which the hit belogs to - pindex : pindex of the particle
         * that the current cluster belongs to.
         */
        // <clID, List of RecHits> 
        Map<Short, List<RecHit>> ecalHits = getECalHits(event, mchits.get((byte) DetectorType.ECAL.getDetectorId()), mcp, recp);
        List<RecCluster> ecalClusters = getECalClusters(event);

        /**
         * Getting FT Hits and clusters
         */
        Map< Short, List<RecHit>> ftCalHits = getFTCalHits(event, mchits.get((byte) DetectorType.FTCAL.getDetectorId()), mcp, recp);
        List<RecCluster> ftCalClusters = getFTCalClusters(event);

        Map< Short, List<RecHit>> ftHodoHits = getFTHodoHits(event, mchits.get((byte) DetectorType.FTHODO.getDetectorId()), mcp, recp);
        List<RecCluster> ftHodoClusters = getFTHodoClusters(event);

        /**
         * Getting CND Hits and Clusters
         */
        Map<Short, List<RecHit>> cndHits = getCNDHits(event, mchits.get((byte) DetectorType.CND.getDetectorId()), mcp, recp);
        List<RecCluster> cndClusters = getCNDClusters(event);

//        if (cndClusters != null && cndClusters.size() > 0 && cndHits.isEmpty()) {
//            System.out.println("**** CND clusters have non 0 size, while the number of hits is 0 ****");
//            System.out.println("Size of clusters is " + cndClusters.size());
//        }
        /**
         * Getting CTOF Hits and Clusters
         */
        Map<Short, List<RecHit>> ctofHits = getCTOFHits(event, mchits.get((byte) DetectorType.CTOF.getDetectorId()), mcp, recp);
        List<RecCluster> ctofClusters = getCTOFClusters(event);

        /**
         * Getting BST Hits and Clusters
         */
        Map<Short, List<RecHit>> bstHits = getBSTHits(event, mchits.get((byte) DetectorType.BST.getDetectorId()), trkID2Index, mcp, recp);
        List<RecCluster> bstClusters = getBSTClusters(event, trkID2Index);

        /**
         * Getting BMT Hits and Clusters
         */
        Map<Short, List<RecHit>> bmtHits = getBMTHits(event, mchits.get((byte) DetectorType.BMT.getDetectorId()), trkID2Index, mcp, recp);
        List<RecCluster> bmtClusters = getBMTClusters(event, trkID2Index);

        /**
         * Getting DC Hits and Clusters
         */
        Map<Short, List<RecHit>> DCHits = getDCHits(event, mchits.get((byte) DetectorType.DC.getDetectorId()), mcp, recp);
        List<RecCluster> dcClusters = getDCClusters(event);

        /**
         * Matchingg clusters to MCParticles
         */
        MatchClasters(ecalClusters, ecalHits, mchits.get((byte) DetectorType.ECAL.getDetectorId()));
        MatchClasters(ftCalClusters, ftCalHits, mchits.get((byte) DetectorType.FTCAL.getDetectorId()));
        MatchClasters(ftHodoClusters, ftHodoHits, mchits.get((byte) DetectorType.FTHODO.getDetectorId()));
        MatchClasters(cndClusters, cndHits, mchits.get((byte) DetectorType.CND.getDetectorId()));
        MatchClasters(ctofClusters, ctofHits, mchits.get((byte) DetectorType.CTOF.getDetectorId()));
        MatchClasters(bstClusters, bstHits, mchits.get((byte) DetectorType.BST.getDetectorId()));
        MatchClasters(bmtClusters, bmtHits, mchits.get((byte) DetectorType.BMT.getDetectorId()));
        MatchClasters(dcClusters, DCHits, mchits.get((byte) DetectorType.DC.getDetectorId()));

        /**
         * Adding all clusters together
         */
        List<RecCluster> allCls = new ArrayList<>();

        /**
         * Adding ECal clusters
         */
        if (ecalClusters != null) {
            allCls.addAll(ecalClusters);
        }
        /**
         * Adding FTCal clusters
         */
        if (ftCalClusters != null) {
            allCls.addAll(ftCalClusters);
        }

        /**
         * Adding FTHodo clusters
         */
        if (ftHodoClusters != null) {
            allCls.addAll(ftHodoClusters);
        }

        /**
         * Adding CND clusters
         */
        if (cndClusters != null) {
            allCls.addAll(cndClusters);
        }

        /**
         * Adding CTOF clusters
         */
        if (ctofClusters != null) {
            allCls.addAll(ctofClusters);
        }

        /**
         * Adding BST clusters
         */
        if (bstClusters != null) {
            allCls.addAll(bstClusters);
        }

        /**
         * Adding BMT clusters
         */
        if (bmtClusters != null) {
            allCls.addAll(bmtClusters);
        }

        /**
         * Adding DC clusters
         */
        if (dcClusters != null) {
            allCls.addAll(dcClusters);
        }

        try {
            /**
             * Mapping Clusters to MCParticle
             */
            Map<Short, List<RecCluster>> clsPerMCp = mapClustersToParticles(mcp.keySet(), allCls, "MC");
            /**
             * Mapping Clusters to RecParticle
             */
            Map<Short, List<RecCluster>> clsPerRecp = mapClustersToParticles(recp.keySet(), allCls, "Rec");
            //PrintClsPerMc(clsPerMCp);

            List<MCRecMatch> MCRecMatches = MakeMCRecMatch(mcp, clsPerMCp);
            List<MCRecMatch> RecMCMatches = MakeRecMCMatch(recp, clsPerRecp);
            CalculateMatchCuality(event, MCRecMatches, RecMCMatches);
            bankWriter(event, MCRecMatches, RecMCMatches);

            /*
                Some debugging
             */
//            DataBank dbRecMatch = event.getBank("MC::RecMatch");
//            DataBank dbRecPart = event.getBank("REC::Particle");
//            DataBank dbMCPart = event.getBank("MC::Particle");
//            DataBank dbtrkBank = event.getBank("TimeBasedTrkg::TBTracks");
//            DataBank dbrectrkBank = event.getBank("REC::Track");
//            DataBank dbdcBank = event.getBank("DC::tdc");
//            DataBank dbHitsBank = event.getBank("TimeBasedTrkg::TBHits");
//            DataBank dbMCTrue = event.getBank("MC::True");

//            System.out.println("=========                     DEBUGGING                      =========");
//
//            System.out.println("========= MC::Particle Bank =========");
//            dbMCPart.show();
//            System.out.println("========= REC::Particle Bank =========");
//            dbRecPart.show();
//            System.out.println("========= MC::RecMatch Bank =========");
//            dbRecMatch.show();
//            System.out.println("========= TimeBasedTrkg::TBTracks =========");
//            dbtrkBank.show();
//            System.out.println("========= REC::Track =========");
//            dbrectrkBank.show();
//            System.out.println("========= DC::tdc =========");
//            dbdcBank.show();
//            System.out.println("========= TimeBasedTrkg::TBHits =========");
//            dbHitsBank.show();
//            System.out.println("========= MC::True =========");
//            dbMCTrue.show();
//
//            for( Short pind : clsPerRecp.keySet() ){
//                System.out.println( " ===== Clusters for pind " + pind + " are =====" );
//                List<RecCluster> clusters = clsPerRecp.get(pind);
//                
//                for( RecCluster curCl : clusters ){
//                
//                    System.out.println("== " + curCl.toString() );
//                }
//            }
        } catch (Exception ex) {
            System.err.println("The DataSet in the  mapClustersToParticles should be either 'Rec' or 'MC' ");
            Logger.getLogger(TruthMatch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;

    }

    /**
     * ************************************************************************
     * Defining objects that will be needed int the matching process
     * ************************************************************************
     */
    // MCParticle from MC::Particle bank
    class MCPart {

        public MCPart() {
            MCLayersTrk = 0;
            MCLayersNeut = 0;
            RecLayersTrk = new HashMap<>();
            RecLayersNeut = new HashMap<>();
        }

        public int id;      // index of the MC particle (it should correspond of tid/otid
        // (still needs to be defined which one) in MC::True)

        public int pid;     // PDG ID code
        public long MCLayersTrk;    // This is not really MCParticle property, but we know that each MCParticle should have this so, attaching this to MCPart object
        // *********************************************** Description of "LayersTrk" ************************************************************
        //**** BMT Layer ****|*** BST Layer **** | ******************************************* DC layers *******************************************
        // 47 46 45 44 43 42 | 41 40 39 38 37 36 | 35 34 33 32 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 3 2 0

        public long MCLayersNeut;   // This is ntot really MCParticle property, bu we know that each MCParticle should have this so, attaching this to MCPart object

        // In the following map, the key is the pindex of the matched particle, and the value is the    
        public Map<Integer, Long> RecLayersTrk;
        // In the following map, the key is the pindex of the matched particle, and the value is the    
        public Map<Integer, Long> RecLayersNeut;
    }

    // MCParticle from MC::Particle bank
    class RecPart {

        public RecPart() {
            RecLayersTrk = 0;
            RecLayersNeut = 0;

            MCLayersTrk = new HashMap<>();
            MCLayersNeut = new HashMap<>();
        }

        public int id;      // index of the MC particle (it should correspond of tid/otid
        // (still needs to be defined which one) in MC::True)

        public int pid;     // PDG ID code
        public long RecLayersTrk;    // This is not really RecParticle property, but we know that each RecParticle should have this so, attaching this to RecPart object
        public long RecLayersNeut;    // This is not really RecParticle property, but we know that each RecParticle should have this so, attaching this to RecPart object
        // *********************************************** Description of "LayersTrk" ************************************************************
        //**** BMT Layer ****|*** BST Layer **** | ******************************************* DC layers *******************************************
        // 47 46 45 44 43 42 | 41 40 39 38 37 36 | 35 34 33 32 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 3 2 0

        public Map<Integer, Long> MCLayersTrk;
        public Map<Integer, Long> MCLayersNeut;
    }

// True hit information from the MC::True banl
    class MCHit {

        /**
         * TODO: pid should be removed, it is not needed anymore.
         */
        public int pid;      // MC particle id (pdg code)
        public int otid;     // id of the original (gernerated) particle that eventually caused the hit
        public int hitn;     // Hit id: it corresponds to the position of the corresponding dgtized hit in the adc bank
        public byte detector; // Detector code descriptor

        @Override
        public String toString() {
            String str = "***** MCHit object ******\n";

            str += "* pid = " + String.valueOf(pid) + "\n";
            str += "* otid = " + String.valueOf(otid) + "\n";
            str += "* hitn = " + String.valueOf(hitn) + "\n";
            str += "* detector = " + String.valueOf(detector) + "\n";

            return str;
        }
    }

// RecHit object
    class RecHit {

        public int id;          // ID: This is corresponding ADC row number in the ADC bank.
        public short pindex;      // pindex
        public short cid;         // clusterID
        public byte detector;     // Detector specifier

        @Override
        public String toString() {
            String str = "***** RecHit object ******\n";

            str += "* id = " + String.valueOf(id) + "\n";
            str += "* pindex = " + String.valueOf(pindex) + "\n";
            str += "* cid = " + String.valueOf(cid) + "\n";
            str += "* detector = " + String.valueOf(cid) + "\n";
            str += "***** End of RecHit object ******\n";
            return str;
        }

    }

    class RecCluster {

        public RecCluster() {
            nHitMatched = 0;
            mcotid = -1;
        }
        public short id;            // cluster id
        public short mcotid;        // id of the original MC particls
        public short rectid;        // rec track id
        public short pindex;        // index of the rec particle
        public int nHitMatched;     // number of hits MC matched
        public short size;          // number of hits
        public byte detector;       // The detector
        public byte layer;          // layer
        public byte superlayer;     // Superlayer
        public byte sector;         // Sector
        public float energy;        // energy

        @Override
        public String toString() {

            String str = "***** RecCluster Object *****\n";

            str += "id = " + String.valueOf(id) + "\n";
            str += "mcotid = " + String.valueOf(mcotid) + "\n";
            str += "rectid = " + String.valueOf(rectid) + "\n";
            str += "pindex = " + String.valueOf(pindex) + "\n";
            str += "nHitMatched = " + String.valueOf(nHitMatched) + "\n";
            str += "size = " + String.valueOf(size) + "\n";
            str += "detector = " + String.valueOf(detector) + "\n";
            str += "layer = " + String.valueOf(layer) + "\n";
            str += "superlayer = " + String.valueOf(superlayer) + "\n";
            str += "sector = " + String.valueOf(sector) + "\n";
            str += " ***** All info about the RecCluster Object is printed \n \n";

            return str;
        }
    }

    class MCRecMatch {

        public MCRecMatch() {
            id = -1;
            pindex = -1;
            MCLayersTrk = 0L;
            RecLayersTrk = 0L;
            quality = 0F;
        }
        public short id;            // MC particle id
        public short pindex;        // pindex index of the rec particle in the REC::Particle bank
        public long MCLayersTrk;    // See description below
        public long MCLayersNeut;    // See description below
        public long RecLayersTrk;    // See description below
        public long RecLayersNeut;   // See description below

        public float quality;

        // *********************************************** Description of "MCLayersTrk" ************************************************************
        //**** BMT Layer ****|*** BST Layer **** | ******************************************* DC layers *******************************************
        // 47 46 45 44 43 42 | 41 40 39 38 37 36 | 35 34 33 32 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 3 2 0
        // *********************************************** Description of "RecLayersTrk" ***********************************************************
        //**** BMT Layer ****|*** BST Layer **** | ******************************************* DC layers *******************************************
        // 47 46 45 44 43 42 | 41 40 39 38 37 36 | 35 34 33 32 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 3 2 0
    }

    /**
     * Some constatnts
     */
    private final int BMTID = 1;
    private final int BSTID = 2;
    private final int DCID = 6;
    private final int ECALID = 7;
    private final int CNDID = 3;
    private final int CTOFID = 4;
    private final int FTCALID = 10;

    private final int PHOTON_ID = 22;
    private final int NEUTRON_ID = 2112;

    private final int ELECTRON_ID = 11;
    private final int PROTON_ID = 2212;
    private final int POSITRON_ID = -11;
    private final int PIPLUS_ID = 211;
    private final int PIMINUS_ID = -211;
    private final int KPLUS_ID = 321;
    private final int KMINUS_ID = -321;
    private final int MUMINUS_ID = 13;
    private final int MUPLUS_ID = -13;

    private final int BSTStartBit = 36;
    private final int BMTStartBit = 42;
    private final int DCStartBit = 0;
    private final int ECalStartBit = 0;
    private final int CTOFBit = 6;
    private final int CNDStartBit = 3;
    private final int FTCalBit = 14;
    private final int FTHodoStartBit = 12;

    private final List<Integer> chargedPIDs;

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

    /**
     *
     * @param REC::Particle bank
     *
     * @return map of MCpart objects, where the Key is the index of the MC
     * particle in the MC::Particle bank
     */
    Map<Short, RecPart> getRecparticles(DataBank recpart) {

        Map<Short, RecPart> recp = new HashMap<>();

        for (int i = 0; i < recpart.rows(); i++) {

            RecPart curPart = new RecPart();

            curPart.id = i;
            curPart.pid = recpart.getInt("pid", i);

            /**
             * Check if there is a neutral particle in the MC::Particle
             */
            if (curPart.pid == 22 /*photon*/ || curPart.pid == 2112/*Neutron*/) {
                hasNeutral = true;
            }

            recp.put((short) (i), curPart);
        }
        return recp;
    }

    Map<Byte, Map<Integer, MCHit>> getMCHits(DataBank mctrue, Map<Short, MCPart> mcp) {

        /**
         * TODO: The 2nd argument is not needed anymore, should be cleaned up
         */
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
//            if (hit.detector != (byte) DetectorType.ECAL.getDetectorId() && hit.detector != (byte) DetectorType.FTCAL.getDetectorId()
//                    && hit.detector != (byte) DetectorType.CND.getDetectorId() && hit.detector != (byte) DetectorType.CTOF.getDetectorId()
//                    && mcp.get((short) tid) == null) {
//                continue;
//            }
            /*else if (mcp.containsKey((short) (hit.otid)) && mcp.get((short) (hit.otid)).pid != 22 && mcp.get((short) (hit.otid)).pid != 2112 &&  mcp.get((short) (hit.otid)).pid != -11 ) {
                continue;
            }*/
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

    Map< Integer, Map<Short, Integer>> MaketrkID2IndexMap(DataEvent event) {
        Map< Integer, Map<Short, Integer>> trkID2Index = new HashMap<>();

        trkID2Index.put(DetectorType.CVT.getDetectorId(), new HashMap<>());
        trkID2Index.put(DetectorType.DC.getDetectorId(), new HashMap<>());

        /**
         * Check if the REC::Track bank exists
         */
        if (!event.hasBank("REC::Track")) {
            return trkID2Index;
        }

        DataBank trkBank = event.getBank("REC::Track");

        for (int iTrk = 0; iTrk < trkBank.rows(); iTrk++) {
            int det = trkBank.getInt("detector", iTrk);

            if (det == DetectorType.CVT.getDetectorId() || det == DetectorType.DC.getDetectorId()) {
                trkID2Index.get(det).put(trkBank.getShort("index", iTrk), iTrk);
            }
        }

        return trkID2Index;
    }

    /**
     *
     * @param event DataEvent
     * @param mchitsInECal MCHits in ECal
     * @return Map<clusterID, List<RecHit>>, Map, where the Key is the
     * clusterID, and the value is a list of hits having the same clusterID
     */
    Map< Short, List<RecHit>> getECalHits(DataEvent event, Map<Integer, MCHit> mchitsInECal, Map<Short, MCPart> mcp, Map<Short, RecPart> recp) {

        /**
         * We need two banks to be present in the event: ECAL::hits and
         * REC::Calorimeter "id" and "cid (clusterId)" will be obtained from the
         * ECAL::hits bank, but in order to find the pindex of particle we need
         * to loop over the REC::Calorimeter bank and create a map
         * <cId, pindex>.
         */
        Map< Short, List<RecHit>> recHits = new HashMap<>();
        if (mchitsInECal == null) {
            /**
             * In case if no MC hit present in the ECal, then don't proceed, as
             * we need only hits that are associated to an MC hit
             */
            return recHits;
        }

        /**
         * Check if two necessary banks exist otherwise will return null
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

        for (int ihit = 0; ihit < hitsBank.rows(); ihit++) {
            RecHit curHit = new RecHit();

            curHit.id = hitsBank.getShort("id", ihit) - 1;   // -1 for starting from 0
            curHit.cid = (short) (hitsBank.getShort("clusterId", ihit) - 1);  // -1 for starting from 0

            int layer = (hitsBank.getInt("layer", ihit) - 1) / 3;  // PCAL 1(U)2(V)3(W), ECIN 4(U)5(V)6(W). ECOut 7(U)8(V)9(W)

            int ECalLayerBit = ECalStartBit + layer;

            mcp.get((short) mchitsInECal.get(curHit.id).otid).MCLayersNeut |= 1L << ECalLayerBit;

            if (curHit.cid == -2 || !mchitsInECal.containsKey(curHit.id)) {
                continue; // The hit is not part of any cluster, or the hit it's corresponding MC hit is ignored
            }
            curHit.pindex = clId2Pindex.get(curHit.cid);
            curHit.detector = (byte) DetectorType.ECAL.getDetectorId();

            if (curHit.pindex >= 0) {

                recp.get(curHit.pindex).RecLayersNeut |= 1L << ECalLayerBit;

                if (!recp.get(curHit.pindex).MCLayersNeut.containsKey(mchitsInECal.get(curHit.id).otid)) {
                    recp.get(curHit.pindex).MCLayersNeut.put(mchitsInECal.get(curHit.id).otid, 0L);
                }

                Long tmpMCWord = recp.get(curHit.pindex).MCLayersNeut.get(mchitsInECal.get(curHit.id).otid);
                tmpMCWord |= 1L << ECalLayerBit;
                recp.get(curHit.pindex).MCLayersNeut.put(mchitsInECal.get(curHit.id).otid, tmpMCWord);

                if (!mcp.get((short) mchitsInECal.get(curHit.id).otid).RecLayersNeut.containsKey((int) curHit.pindex)) {
                    mcp.get((short) mchitsInECal.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, 0L);
                }

                Long tmp = mcp.get((short) mchitsInECal.get(curHit.id).otid).RecLayersNeut.get((int) curHit.pindex);
                tmp |= 1L << ECalLayerBit;
                mcp.get((short) mchitsInECal.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, tmp);
            }

            if (recHits.get(curHit.cid) == null) {
                recHits.put(curHit.cid, new ArrayList<>());
            }

            recHits.get(curHit.cid).add(curHit);
        }

        return recHits;
    }

    Map< Short, List<RecHit>> getFTCalHits(DataEvent event, Map<Integer, MCHit> mchitsInFTCal, Map<Short, MCPart> mcp, Map<Short, RecPart> recp) {
        Map< Short, List<RecHit>> recHits = new HashMap<>();

        if (mchitsInFTCal == null) {
            /**
             * In case if no MC hit present in the FTCal, then don't proceed, as
             * we need only hits that are associated to an MC hit
             */
            return recHits;
        }

        /**
         * Check if two necessary banks exist otherwise will return null
         */
        if (event.hasBank("FTCAL::hits") == false) {
            return null;
        }

        if (event.hasBank("FTCAL::clusters") == false) {
            return null;
        }

        Map<Short, Short> clId2Pindex = new HashMap<>();
        DataBank RecFTBank = event.getBank("REC::ForwardTagger");

        for (int iFT = 0; iFT < RecFTBank.rows(); iFT++) {

            Short pindex = RecFTBank.getShort("pindex", iFT);
            Short index = RecFTBank.getShort("index", iFT);
            Byte detector = RecFTBank.getByte("detector", iFT);

            if (detector == (byte) DetectorType.FTCAL.getDetectorId()) {
                clId2Pindex.put(index, pindex);
            }
        }

        DataBank hitsBank = event.getBank("FTCAL::hits");

        for (int ihit = 0; ihit < hitsBank.rows(); ihit++) {
            RecHit curHit = new RecHit();

            curHit.id = hitsBank.getShort("hitID", ihit);   // Not removing 1, as hitID start from 0
            curHit.cid = (short) (hitsBank.getShort("clusterID", ihit) - 1);  // -1 for starting from 0

            mcp.get((short) mchitsInFTCal.get(curHit.id).otid).MCLayersNeut |= 1L << FTCalBit;

            if (curHit.cid == -2 || !mchitsInFTCal.containsKey(curHit.id)) {
                continue; // The hit is not part of any cluster, or the hit it's corresponding MC hit is ignored
            }

            /**
             * For FTCal not necessarily all clusters are associated to a rec
             * particle, that is why we will check, if the clId2Pindex contains
             * the given cluster
             */
            if (!clId2Pindex.containsKey(curHit.cid)) {
                continue;
            }

            curHit.pindex = clId2Pindex.get(curHit.cid);
            //curHit.detector = (byte) DetectorType.ECAL.getDetectorId(); // Seems Wrong 10/03/2020, Should be looked at
            curHit.detector = (byte) DetectorType.FTCAL.getDetectorId();

            // Although the "if" statement above should ensure pindex is not negative, 
            if (curHit.pindex >= 0) {

                recp.get(curHit.pindex).RecLayersNeut |= 1L << FTCalBit;

                if (!recp.get(curHit.pindex).MCLayersNeut.containsKey(mchitsInFTCal.get(curHit.id).otid)) {
                    recp.get(curHit.pindex).MCLayersNeut.put(mchitsInFTCal.get(curHit.id).otid, 0L);
                }

                Long tmpMCWord = recp.get(curHit.pindex).MCLayersNeut.get(mchitsInFTCal.get(curHit.id).otid);
                tmpMCWord |= 1L << FTCalBit;
                recp.get(curHit.pindex).MCLayersNeut.put(mchitsInFTCal.get(curHit.id).otid, tmpMCWord);

                if (!mcp.get((short) mchitsInFTCal.get(curHit.id).otid).RecLayersNeut.containsKey((int) curHit.pindex)) {
                    mcp.get((short) mchitsInFTCal.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, 0L);
                }

                Long tmp = mcp.get((short) mchitsInFTCal.get(curHit.id).otid).RecLayersNeut.get((int) curHit.pindex);
                tmp |= 1L << FTCalBit;
                mcp.get((short) mchitsInFTCal.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, tmp);
            }

            if (recHits.get(curHit.cid) == null) {
                recHits.put(curHit.cid, new ArrayList<>());
            }

            recHits.get(curHit.cid).add(curHit);
        }

        return recHits;
    }

    Map< Short, List<RecHit>> getFTHodoHits(DataEvent event, Map<Integer, MCHit> mchitsInFTHodo, Map<Short, MCPart> mcp, Map<Short, RecPart> recp) {
        Map< Short, List<RecHit>> recHits = new HashMap<>();

        if (mchitsInFTHodo == null) {
            /**
             * In case if no MC hit present in the FTHodo, then don't proceed,
             * as we need only hits that are associated to an MC hit
             */
            return recHits;
        }

        /**
         * Check if two necessary banks exist otherwise will return null
         */
        if (event.hasBank("FTHODO::hits") == false) {
            return null;
        }

        if (event.hasBank("FTHODO::clusters") == false) {
            return null;
        }

        Map<Short, Short> clId2Pindex = new HashMap<>();
        DataBank RecFTBank = event.getBank("REC::ForwardTagger");

        for (int iFT = 0; iFT < RecFTBank.rows(); iFT++) {

            Short pindex = RecFTBank.getShort("pindex", iFT);
            Short index = RecFTBank.getShort("index", iFT);
            Byte detector = RecFTBank.getByte("detector", iFT);

            if (detector == (byte) DetectorType.FTHODO.getDetectorId()) {
                clId2Pindex.put(index, pindex);
            }
        }

        DataBank hitsBank = event.getBank("FTHODO::hits");

        for (int ihit = 0; ihit < hitsBank.rows(); ihit++) {
            RecHit curHit = new RecHit();

            curHit.id = hitsBank.getShort("hitID", ihit);   // Not removing 1, as hitID start from 0
            curHit.cid = (short) (hitsBank.getShort("clusterID", ihit) - 1);  // -1 for starting from 0
            int layer = (int) hitsBank.getByte("layer", ihit) - 1; // 0 would correspond to the layer 1, and 1 would correspond to the layer 2
            int HodoLayerBit = FTHodoStartBit + layer;

            mcp.get((short) mchitsInFTHodo.get(curHit.id).otid).MCLayersNeut |= 1L << HodoLayerBit;

            if (curHit.cid == -2 || !mchitsInFTHodo.containsKey(curHit.id)) {
                continue; // The hit is not part of any cluster, or the hit it's corresponding MC hit is ignored
            }

            /**
             * For FTHodo not necessarily all clusters are associated to a rec
             * particle, that is why we will check, if the clId2Pindex contains
             * the given cluster
             */
            if (!clId2Pindex.containsKey(curHit.cid)) {
                continue;
            }

            curHit.pindex = clId2Pindex.get(curHit.cid);
            //curHit.detector = (byte) DetectorType.ECAL.getDetectorId(); // Seems Wrong 10/03/2020, Should be looked at
            curHit.detector = (byte) DetectorType.FTHODO.getDetectorId();

            // Although the "if" statement above should ensure pindex is not negative, 
            if (curHit.pindex >= 0) {

                recp.get(curHit.pindex).RecLayersNeut |= 1L << HodoLayerBit;

                if (!recp.get(curHit.pindex).MCLayersNeut.containsKey(mchitsInFTHodo.get(curHit.id).otid)) {
                    recp.get(curHit.pindex).MCLayersNeut.put(mchitsInFTHodo.get(curHit.id).otid, 0L);
                }

                Long tmpMCWord = recp.get(curHit.pindex).MCLayersNeut.get(mchitsInFTHodo.get(curHit.id).otid);
                tmpMCWord |= 1L << HodoLayerBit;
                recp.get(curHit.pindex).MCLayersNeut.put(mchitsInFTHodo.get(curHit.id).otid, tmpMCWord);

                if (!mcp.get((short) mchitsInFTHodo.get(curHit.id).otid).RecLayersNeut.containsKey((int) curHit.pindex)) {
                    mcp.get((short) mchitsInFTHodo.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, 0L);
                }

                Long tmp = mcp.get((short) mchitsInFTHodo.get(curHit.id).otid).RecLayersNeut.get((int) curHit.pindex);
                tmp |= 1L << HodoLayerBit;
                mcp.get((short) mchitsInFTHodo.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, tmp);
            }

            if (recHits.get(curHit.cid) == null) {
                recHits.put(curHit.cid, new ArrayList<>());
            }

            recHits.get(curHit.cid).add(curHit);
        }

        return recHits;
    }

    Map< Short, List<RecHit>> getCNDHits(DataEvent event, Map<Integer, MCHit> mchitsInCND, Map<Short, MCPart> mcp, Map<Short, RecPart> recp) {
        Map< Short, List<RecHit>> recHits = new HashMap<>();

        if (mchitsInCND == null) {
            /**
             * If no MC hits present in the CND, then we stop here! no need to
             * collect hits, as wee need only hits that are matched to an MChit
             */
            //System.out.println("No MC hits in CND");
            return recHits;
        }

        /**
         * Check if three necessary banks exist otherwise will return null
         */
        if ((event.hasBank("CND::hits") == false) || (event.hasBank("CND::clusters") == false)
                || (event.hasBank("REC::Scintillator") == false)) {
            // System.out.println("There is No CND cluster or there is No CND::hit or there is no REC::Scintillator bank present");
            return null;
        }

        Map<Short, Short> clId2Pindex = new HashMap<>();
        DataBank RecScintil = event.getBank("REC::Scintillator");

        for (int iSC = 0; iSC < RecScintil.rows(); iSC++) {

            // Rec scintillator has different detectors in it, so we want only CND responces in this case
            if (RecScintil.getByte("detector", iSC) != (byte) DetectorType.CND.getDetectorId()) {
                continue;
            }

            Short pindex = RecScintil.getShort("pindex", iSC);
            Short index = RecScintil.getShort("index", iSC);

            clId2Pindex.put(index, pindex);
        }

        DataBank hitsBank = event.getBank("CND::hits");

        for (int ihit = 0; ihit < hitsBank.rows(); ihit++) {
            RecHit curHit = new RecHit();

            /**
             * The following line is *WRONG*, variable *id* is not correct,
             * instead we should use indexLadc(tdc) *** curHit.id =
             * hitsBank.getShort("id", ihit) - 1; // -1, as id start from 1***
             */
            curHit.id = (int) hitsBank.getShort("indexLtdc", ihit) / 2;   // We should devide to 2, as each MC::True hit is digitized into two ADC/TDC hits.
            curHit.cid = (short) (hitsBank.getShort("clusterid", ihit) - 1);  // -1 for starting from 0

            int layer = hitsBank.getInt("layer", ihit) - 1;

            int CNDLayerBit = CNDStartBit + layer;
            mcp.get((short) mchitsInCND.get(curHit.id).otid).MCLayersNeut |= 1L << CNDLayerBit;

            if (curHit.cid == -2 || !mchitsInCND.containsKey(curHit.id)) {
                continue; // The hit is not part of any cluster, or the hit it's corresponding MC hit is ignored
            }

            curHit.pindex = clId2Pindex.get(curHit.cid);
            curHit.detector = (byte) DetectorType.CND.getDetectorId();

            if (curHit.pindex >= 0) {
                recp.get(curHit.pindex).RecLayersNeut |= 1L << CNDLayerBit;

                if (!recp.get(curHit.pindex).MCLayersNeut.containsKey(mchitsInCND.get(curHit.id).otid)) {
                    recp.get(curHit.pindex).MCLayersNeut.put(mchitsInCND.get(curHit.id).otid, 0L);
                }

                Long tmpMCWord = recp.get(curHit.pindex).MCLayersNeut.get(mchitsInCND.get(curHit.id).otid);
                tmpMCWord |= 1L << CNDLayerBit;
                recp.get(curHit.pindex).MCLayersNeut.put(mchitsInCND.get(curHit.id).otid, tmpMCWord);

                if (!mcp.get((short) mchitsInCND.get(curHit.id).otid).RecLayersNeut.containsKey((int) curHit.pindex)) {
                    mcp.get((short) mchitsInCND.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, 0L);
                }

                Long tmp = mcp.get((short) mchitsInCND.get(curHit.id).otid).RecLayersNeut.get((int) curHit.pindex);
                tmp |= 1L << CNDLayerBit;
                mcp.get((short) mchitsInCND.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, tmp);

            }

            if (recHits.get(curHit.cid) == null) {
                recHits.put(curHit.cid, new ArrayList<>());
            }

            recHits.get(curHit.cid).add(curHit);
        }

        //System.out.println("The size of CNDHits is " + recHits.keySet().size());
        return recHits;
    }

    Map< Short, List<RecHit>> getCTOFHits(DataEvent event, Map<Integer, MCHit> mchitsInCTOF, Map<Short, MCPart> mcp, Map<Short, RecPart> recp) {
        Map< Short, List<RecHit>> recHits = new HashMap<>();

        if (mchitsInCTOF == null) {
            /**
             * If no MC hits present in the CTOF, then we stop here! no need to
             * collect hits, as wee need only hits that are matched to an MChit
             */
            //System.out.println("No MC hits in CTOF");
            return recHits;
        }

        /**
         * Check if three necessary banks exist otherwise will return null
         */
        if ((event.hasBank("CTOF::hits") == false) || (event.hasBank("CTOF::clusters") == false)
                || (event.hasBank("REC::Scintillator") == false)) {
            //System.out.println("There is No CTOF::cluster or there is No CTOF::hit or there is no REC::Scintillator bank present");
            return null;
        }

        Map<Short, Short> clId2Pindex = new HashMap<>();
        DataBank RecScintil = event.getBank("REC::Scintillator");

        for (int iSC = 0; iSC < RecScintil.rows(); iSC++) {

            // Rec scintillator has different detectors in it, so we want only CTOF responces in this case
            if (RecScintil.getByte("detector", iSC) != (byte) DetectorType.CTOF.getDetectorId()) {
                continue;
            }

            Short pindex = RecScintil.getShort("pindex", iSC);
            Short index = RecScintil.getShort("index", iSC);

            clId2Pindex.put(index, pindex);
        }

        DataBank hitsBank = event.getBank("CTOF::hits");

        for (int ihit = 0; ihit < hitsBank.rows(); ihit++) {
            RecHit curHit = new RecHit();

            curHit.id = (int) hitsBank.getShort("tdc_idx1", ihit);

            mcp.get((short) mchitsInCTOF.get(curHit.id).otid).MCLayersNeut |= 1L << CTOFBit;

            curHit.cid = (short) (hitsBank.getShort("clusterid", ihit) - 1);  // -1 for starting from 0
            if (curHit.cid == -1 || !mchitsInCTOF.containsKey(curHit.id)) {

                //System.out.println("Continuing!!!! The hit id is " + curHit.id);
                continue; // The hit is not part of any cluster, or the hit it's corresponding MC hit is ignored
            }

            curHit.pindex = clId2Pindex.get(curHit.cid);
            curHit.detector = (byte) DetectorType.CTOF.getDetectorId();

            if (curHit.pindex >= 0) {

                recp.get(curHit.pindex).RecLayersNeut |= 1L << CTOFBit;

                if (!recp.get(curHit.pindex).MCLayersNeut.containsKey(mchitsInCTOF.get(curHit.id).otid)) {
                    recp.get(curHit.pindex).MCLayersNeut.put(mchitsInCTOF.get(curHit.id).otid, 0L);
                }

                Long tmpMCWord = recp.get(curHit.pindex).MCLayersNeut.get(mchitsInCTOF.get(curHit.id).otid);
                tmpMCWord |= 1L << CTOFBit;
                recp.get(curHit.pindex).MCLayersNeut.put(mchitsInCTOF.get(curHit.id).otid, tmpMCWord);

                if (!mcp.get((short) mchitsInCTOF.get(curHit.id).otid).RecLayersNeut.containsKey((int) curHit.pindex)) {
                    mcp.get((short) mchitsInCTOF.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, 0L);
                }

                Long tmp = mcp.get((short) mchitsInCTOF.get(curHit.id).otid).RecLayersNeut.get((int) curHit.pindex);
                tmp |= 1L << CTOFBit;
                mcp.get((short) mchitsInCTOF.get(curHit.id).otid).RecLayersNeut.put((int) curHit.pindex, tmp);
            }

            if (recHits.get(curHit.cid) == null) {
                recHits.put(curHit.cid, new ArrayList<>());
            }

            recHits.get(curHit.cid).add(curHit);
        }

        //System.out.println("The size of CTOFHits is " + recHits.keySet().size());
        return recHits;
    }

    Map< Short, List<RecHit>> getBSTHits(DataEvent event, Map<Integer, MCHit> mchitsInBST, Map<Integer, Map<Short, Integer>> trkID2Index, Map<Short, MCPart> mcp, Map<Short, RecPart> recp) {

        Map< Short, List<RecHit>> recHits = new HashMap<>();

        if (mchitsInBST == null) {
            /**
             * If no MC hits present in the BST, then we stop here! no need to
             * collect hits, as wee need only hits that are matched to an MChit
             */
            //System.out.println("No MC hits in BST");
            return recHits;
        }

        /**
         * Check if three necessary banks exist otherwise will return null
         */
        if ((event.hasBank("BST::adc") == false) || (event.hasBank("BSTRec::Clusters") == false)) {
            //System.out.println("There is No BSTRec::clusters bank, or there is No BSTRec::Hits bank, or there is no REC::Track bank present");
            return null;
        }

//        System.out.print("KeySet: ");
//        for (int curKey : mchitsInBST.keySet()) {
//            System.out.print(curKey + " ");
//        }
//        System.out.println(" ");
        DataBank clBank = event.getBank("BSTRec::Clusters");
        DataBank trkBank = event.getBank("REC::Track");
        DataBank adcBank = event.getBank("BST::adc");
        //System.out.println("The Keyset of recp is" + recp.keySet());
//        System.out.println("The number of BSTRec::Clusters rows is " + clBank.rows());

        //System.out.println("*** The content of the BSTRec::Clusters bank ");
        for (int iCL = 0; iCL < clBank.rows(); iCL++) {

            short clID = (short) iCL;
            short trkID = (short) (clBank.getShort("trkID", iCL) - 1);

            /**
             * The following line sometimes throws exception. The cluster's
             * associated track doesn't exist in the Rec::Track bank. Yuri knows
             * this they were working on this.
             */
            short pindex = trkID >= 0 ? trkBank.getShort("pindex", trkID2Index.get(DetectorType.CVT.getDetectorId()).get(trkID)) : -1;

            // The hardcoded 5 is the Max number of hits per Cl
            for (int iHit = 0; iHit < 5; iHit++) {

                int hitID = clBank.getInt(String.format("Hit%d_ID", iHit + 1), iCL) - 1;

                if (hitID < 0) {
                    break;
                }

                int layerBit = BSTStartBit + adcBank.getInt("layer", hitID) - 1;
                mcp.get((short) mchitsInBST.get(hitID).otid).MCLayersTrk |= 1L << layerBit;
                if (pindex >= 0) {

                    recp.get(pindex).RecLayersTrk |= 1L << layerBit;

                    if (!recp.get(pindex).MCLayersTrk.containsKey(mchitsInBST.get(hitID).otid)) {
                        recp.get(pindex).MCLayersTrk.put(mchitsInBST.get(hitID).otid, 0L);
                    }

                    Long tmpMCWord = recp.get(pindex).MCLayersTrk.get(mchitsInBST.get(hitID).otid);
                    tmpMCWord |= 1L << layerBit;
                    recp.get(pindex).MCLayersTrk.put(mchitsInBST.get(hitID).otid, tmpMCWord);

                    if (!mcp.get((short) mchitsInBST.get(hitID).otid).RecLayersTrk.containsKey((int) pindex)) {
                        mcp.get((short) mchitsInBST.get(hitID).otid).RecLayersTrk.put((int) pindex, 0L);
                    }

                    Long tmp = mcp.get((short) mchitsInBST.get(hitID).otid).RecLayersTrk.get((int) pindex);
                    tmp |= 1L << layerBit;
                    mcp.get((short) mchitsInBST.get(hitID).otid).RecLayersTrk.put((int) pindex, tmp);
                }
                if (!mchitsInBST.containsKey(hitID)) {
                    // We need only hits that correspond to an MCHit
                    continue;
                }

                RecHit curHit = new RecHit();

                curHit.id = hitID;
                curHit.cid = (short) iCL;
                curHit.pindex = pindex;
                curHit.detector = (byte) DetectorType.BST.getDetectorId();

                if (iHit == 0) {
                    recHits.put(curHit.cid, new ArrayList<>());
                }

                recHits.get(curHit.cid).add(curHit);

            }

        }

        return recHits;
    }

    Map< Short, List<RecHit>> getBMTHits(DataEvent event, Map<Integer, MCHit> mchitsInBMT, Map<Integer, Map<Short, Integer>> trkID2Index, Map<Short, MCPart> mcp, Map<Short, RecPart> recp) {
        Map< Short, List<RecHit>> recHits = new HashMap<>();

        /**
         * Check if three necessary banks exist otherwise will return null
         */
        if ((event.hasBank("BMT::adc") == false) || (event.hasBank("BMTRec::Clusters") == false)) {
            //System.out.println("There is No BMTRec::clusters bank, or there is No BMTRec::Hits bank, or there is no REC::Track bank present");
            return null;
        }

        DataBank clBank = event.getBank("BMTRec::Clusters");
        DataBank trkBank = event.getBank("REC::Track");
        DataBank adcBank = event.getBank("BMT::adc");

        for (int iCL = 0; iCL < clBank.rows(); iCL++) {

            short clID = (short) iCL;
            short trkID = (short) (clBank.getShort("trkID", iCL) - 1);

            //short pindex = trkID >= 0 ? trkBank.getShort("pindex", trkID) : -1;   // THIS IS WRONG, see following line
            short pindex = trkID >= 0 ? trkBank.getShort("pindex", trkID2Index.get(DetectorType.CVT.getDetectorId()).get(trkID)) : -1;

            for (int iHit = 0; iHit < 5; iHit++) {

                int hitID = clBank.getInt(String.format("Hit%d_ID", iHit + 1), iCL) - 1;

                if (hitID < 0) {
                    break;
                }

                int layerBit = BMTStartBit + adcBank.getInt("layer", hitID) - 1;
                mcp.get((short) mchitsInBMT.get(hitID).otid).MCLayersTrk |= 1L << layerBit;
                if (pindex >= 0) {

                    recp.get(pindex).RecLayersTrk |= 1L << layerBit;

                    if (!recp.get(pindex).MCLayersTrk.containsKey(mchitsInBMT.get(hitID).otid)) {
                        recp.get(pindex).MCLayersTrk.put(mchitsInBMT.get(hitID).otid, 0L);
                    }

                    Long tmpMCWord = recp.get(pindex).MCLayersTrk.get(mchitsInBMT.get(hitID).otid);
                    tmpMCWord |= 1L << layerBit;
                    recp.get(pindex).MCLayersTrk.put(mchitsInBMT.get(hitID).otid, tmpMCWord);

                    if (!mcp.get((short) mchitsInBMT.get(hitID).otid).RecLayersTrk.containsKey((int) pindex)) {
                        mcp.get((short) mchitsInBMT.get(hitID).otid).RecLayersTrk.put((int) pindex, 0L);
                    }

                    Long tmp = mcp.get((short) mchitsInBMT.get(hitID).otid).RecLayersTrk.get((int) pindex);
                    tmp |= 1L << layerBit;
                    mcp.get((short) mchitsInBMT.get(hitID).otid).RecLayersTrk.put((int) pindex, tmp);
                }

                //System.out.println("Bitwise representation of LayersTrk is " + Long.toBinaryString(mcp.get((short) mchitsInBMT.get(hitID).otid).MCLayersTrk));
                if (!mchitsInBMT.containsKey(hitID)) {
                    // We need only hits that correspond to an MCHit

                    System.out.println("*********** This BMT hit is not in MCHit!!  " + hitID);

                    continue;
                }

                RecHit curHit = new RecHit();

                curHit.id = hitID;
                curHit.cid = (short) iCL;
                curHit.pindex = pindex;
                curHit.detector = (byte) DetectorType.BMT.getDetectorId();

                if (iHit == 0) {
                    recHits.put(curHit.cid, new ArrayList<>());
                }

                recHits.get(curHit.cid).add(curHit);

            }

        }

        return recHits;
    }

    Map< Short, List<RecHit>> getDCHits(DataEvent event, Map<Integer, MCHit> mchitsInDC, Map<Short, MCPart> mcp, Map<Short, RecPart> recp) {
        Map< Short, List<RecHit>> recHits = new HashMap<>();

        /**
         * Check if three necessary banks exist otherwise will return null
         */
        if (event.hasBank("DC::tdc") == false) {
            return null;
        }

        DataBank tbtrkBank = event.getBank("TimeBasedTrkg::TBTracks");
        DataBank trkBank = event.getBank("REC::Track");
        DataBank tdcBank = event.getBank("DC::tdc");
        DataBank tbHitsBank = event.getBank("TimeBasedTrkg::TBHits");
        DataBank tbMCTrue = event.getBank("MC::True");

        /**
         * We need to link the hit to a pindex, if the the hit is part of a
         * track, so will read TBHits banks and collect all hit IDs that were
         * part of any track
         */
        //List<Integer> tbHitIDs = new ArrayList<>();
        Map<Short, Short> tbTrkID2RecTrInd = new HashMap();
        for (short iRec = 0; iRec < trkBank.rows(); iRec++) {
            Byte det = trkBank.getByte("detector", iRec);
            Short index = trkBank.getShort("index", iRec);

            if (det == DetectorType.DC.getDetectorId()) {
                tbTrkID2RecTrInd.put(tbtrkBank.getShort("id", index), iRec);
            }
        }

        Map<Integer, Short> tbHitIDs = new HashMap<>();
        for (int itbHit = 0; itbHit < tbHitsBank.rows(); itbHit++) {
            //tbHitIDs.put(tbHitsBank.getInt("id", itbHit) - 1, (short)tbHitsBank.getShort("trkID", itbHit) - 1);
            //tbHitIDs.put(tbHitsBank.getInt("id", itbHit) - 1, (short)(tbHitsBank.getByte("trkID", itbHit) - 1));

            if (tbTrkID2RecTrInd.containsKey((short) tbHitsBank.getByte("trkID", itbHit) ) ) {
                tbHitIDs.put(tbHitsBank.getInt("id", itbHit) - 1, (short) tbTrkID2RecTrInd.get((short) tbHitsBank.getByte("trkID", itbHit) ));
            }
        }

//        System.out.println(" ======== Beginning ========");
//        tbMCTrue.show();
//        tdcBank.show();
//        tbHitsBank.show();
//        tbtrkBank.show();
//        trkBank.show();
//        for( Map.Entry<Integer, MCHit> entry: mchitsInDC.entrySet() ){
//            System.out.println( "The Key is " + entry.getKey() + "   The value is " + entry.getValue().toString() );
//        }
        for (int iHit = 0; iHit < tdcBank.rows(); iHit++) {
            RecHit curHit = new RecHit();

            int layer = tdcBank.getInt("layer", iHit);
            int layerBit = DCStartBit + layer - 1;

            curHit.id = iHit;
            curHit.detector = (byte) DetectorType.DC.getDetectorId();
            curHit.cid = (short) iHit;

            if (tbHitIDs.containsKey(curHit.id)) {
                //System.out.println("======== siz of trkBanks is " + trkBank.rows() + "  hitid is " + curHit.id + "   tbHitIDs.get(curHit.id) is " + tbHitIDs.get(curHit.id) + "   the Pindex is " + trkBank.getShort("pindex", tbHitIDs.get(curHit.id)));

                curHit.pindex = trkBank.getShort("pindex", tbHitIDs.get(curHit.id));
                //System.out.println( "iHit = " + iHit + "   pindex = " + curHit.pindex);
            } else {
                curHit.pindex = -1;
            }

//            System.out.println(" layerBit = " + layerBit + "   The size of mcp is " + mcp.size() );
//            System.out.println(" curHit.id = " + curHit.id);
//            System.out.println(" otid is " + mchitsInDC.get(curHit.id).otid);
//            System.out.println(" MCLayersTrk is " + mcp.get( (short) mchitsInDC.get(curHit.id).otid).MCLayersTrk + "   layerBit = " + layerBit );
            mcp.get((short) mchitsInDC.get(curHit.id).otid).MCLayersTrk |= 1L << layerBit;

            if (curHit.pindex >= 0) {

                //System.out.println("N_Rec_Part = " + recp.size() + "    pindex = " + curHit.pindex);
                recp.get(curHit.pindex).RecLayersTrk |= 1L << layerBit;

                if (!recp.get(curHit.pindex).MCLayersTrk.containsKey(mchitsInDC.get(curHit.id).otid)) {
                    recp.get(curHit.pindex).MCLayersTrk.put(mchitsInDC.get(curHit.id).otid, 0L);
                }

                Long tmpMCWord = recp.get(curHit.pindex).MCLayersTrk.get(mchitsInDC.get(curHit.id).otid);
                tmpMCWord |= 1L << layerBit;
                recp.get(curHit.pindex).MCLayersTrk.put(mchitsInDC.get(curHit.id).otid, tmpMCWord);

                if (!mcp.get((short) mchitsInDC.get(curHit.id).otid).RecLayersTrk.containsKey((int) curHit.pindex)) {
                    mcp.get((short) mchitsInDC.get(curHit.id).otid).RecLayersTrk.put((int) curHit.pindex, 0L);
                }

                Long tmp = mcp.get((short) mchitsInDC.get(curHit.id).otid).RecLayersTrk.get((int) curHit.pindex);
                tmp |= 1L << layerBit;
                mcp.get((short) mchitsInDC.get(curHit.id).otid).RecLayersTrk.put((int) curHit.pindex, tmp);

                //recp.get(curHit.pindex).RecLayersTrk |= 1L << layerBit;
            }

            recHits.put(curHit.cid, new ArrayList<>());
            recHits.get(curHit.cid).add(curHit);
        }

        //System.out.println(" ========    End    ========");
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
            curCl.energy = recCal.getFloat("energy", iCl);

            curCl.size = -1;    // For ECal clusters this is not particularly important, // We don't have this in the bank
            curCl.rectid = -1;  // We will not use ECal clusters for tracks.
            curCl.superlayer = -1; // Not applicable for ECal clusters

            cls.add(curCl);
        }

        return cls;
    }

    List<RecCluster> getFTCalClusters(DataEvent event) {
        List<RecCluster> cls = new ArrayList<>();

        /**
         * We need the bank REC::ForwardTagger, so as a first thing we will
         * check if the bank exist
         */
        if (event.hasBank("REC::ForwardTagger") == false) {
            return cls;
        }

        DataBank recFTCal = event.getBank("REC::ForwardTagger");

        for (int iCl = 0; iCl < recFTCal.rows(); iCl++) {

            /**
             * Both FT clusters and FT hodo hits are in the same
             * REC::ForwardTagger bank
             */
            if (recFTCal.getByte("detector", iCl) != DetectorType.FTCAL.getDetectorId()) {
                continue;
            }

            RecCluster curCl = new RecCluster();

            curCl.id = recFTCal.getShort("index", iCl);
            curCl.pindex = recFTCal.getShort("pindex", iCl);
            curCl.detector = recFTCal.getByte("detector", iCl);
            curCl.layer = recFTCal.getByte("layer", iCl);
            curCl.sector = -1; // No concept of sector for FT
            curCl.energy = recFTCal.getFloat("energy", iCl);
            curCl.size = recFTCal.getShort("size", iCl);

            curCl.rectid = -1;  // We will not use ECal clusters for tracks.
            curCl.superlayer = -1; // Not applicable for ECal clusters

            cls.add(curCl);
        }

        return cls;
    }

    List<RecCluster> getFTHodoClusters(DataEvent event) {
        List<RecCluster> cls = new ArrayList<>();

        /**
         * We need the bank REC::ForwardTagger, so as a first thing we will
         * check if the bank exist
         */
        if (event.hasBank("REC::ForwardTagger") == false) {
            return cls;
        }

        DataBank recFT = event.getBank("REC::ForwardTagger");

        for (int iCl = 0; iCl < recFT.rows(); iCl++) {

            /**
             * Both FT clusters and FT hodo hits are in the same
             * REC::ForwardTagger bank
             */
            if (recFT.getByte("detector", iCl) != DetectorType.FTHODO.getDetectorId()) {
                continue;
            }

            RecCluster curCl = new RecCluster();

            curCl.id = recFT.getShort("index", iCl);
            curCl.pindex = recFT.getShort("pindex", iCl);
            curCl.detector = recFT.getByte("detector", iCl);
            curCl.layer = recFT.getByte("layer", iCl);
            curCl.sector = -1; // No concept of sector for FT
            curCl.energy = recFT.getFloat("energy", iCl);
            curCl.size = recFT.getShort("size", iCl);

            curCl.rectid = -1;
            curCl.superlayer = -1;

            cls.add(curCl);
        }

        return cls;
    }

    List<RecCluster> getCNDClusters(DataEvent event) {
        List<RecCluster> cls = new ArrayList<>();

        /**
         * Of course we need the REC::Scintillator bank. Though we don't need
         * directly the CND::cluster bank, however without it REC::scintillator
         * will not have entry with CND detector
         */
        if ((event.hasBank("REC::Scintillator") == false) || (event.hasBank("CND::clusters") == false)) {
            return cls;
        }

        DataBank recSC = event.getBank("REC::Scintillator");

        for (int iSC = 0; iSC < recSC.rows(); iSC++) {

            if (recSC.getByte("detector", iSC) != DetectorType.CND.getDetectorId()) {
                continue;
            }

            RecCluster curCl = new RecCluster();
            curCl.id = recSC.getShort("index", iSC);
            curCl.pindex = recSC.getShort("pindex", iSC);
            curCl.detector = recSC.getByte("detector", iSC);
            curCl.layer = recSC.getByte("layer", iSC);
            curCl.sector = recSC.getByte("sector", iSC);
            curCl.energy = recSC.getFloat("energy", iSC);
            curCl.size = -1; // For CND clusters this is not a relevant variable
            curCl.rectid = -1; // CND is not used for tracks
            curCl.superlayer = -1; // not applicable

            cls.add(curCl);
        }

        return cls;
    }

    List<RecCluster> getCTOFClusters(DataEvent event) {
        List<RecCluster> cls = new ArrayList<>();

        /**
         * Of course we need the REC::Scintillator bank. Though we don't need
         * directly the CTOF::cluster bank, however without it REC::scintillator
         * will not have entry with CTOF detector
         */
        if ((event.hasBank("REC::Scintillator") == false) || (event.hasBank("CTOF::clusters") == false)) {
            return cls;
        }

        DataBank recSC = event.getBank("REC::Scintillator");

        for (int iSC = 0; iSC < recSC.rows(); iSC++) {

            if (recSC.getByte("detector", iSC) != DetectorType.CTOF.getDetectorId()) {
                continue;
            }

            RecCluster curCl = new RecCluster();
            curCl.id = recSC.getShort("index", iSC);
            curCl.pindex = recSC.getShort("pindex", iSC);
            curCl.detector = recSC.getByte("detector", iSC);
            curCl.layer = recSC.getByte("layer", iSC);
            curCl.sector = recSC.getByte("sector", iSC);
            curCl.energy = recSC.getFloat("energy", iSC);
            curCl.size = -1; // For CTOF clusters this is not a relevant variable
            curCl.rectid = -1; // CTOF is not used for tracks
            curCl.superlayer = -1; // not applicable

            cls.add(curCl);
        }

        return cls;
    }

    List<RecCluster> getBSTClusters(DataEvent event, Map<Integer, Map<Short, Integer>> trkID2Index) {
        List<RecCluster> cls = new ArrayList<>();

        /**
         * Check if three necessary banks exist otherwise will return null
         */
        if ((event.hasBank("BSTRec::Clusters") == false) || (event.hasBank("REC::Track") == false)) {
            //System.out.println("There is No BSTRec::clusters bank, or there is No BSTRec::Hits bank, or there is no REC::Track bank present");
            return null;
        }

        DataBank clBank = event.getBank("BSTRec::Clusters");
        DataBank trkBank = event.getBank("REC::Track");

        for (int iCL = 0; iCL < clBank.rows(); iCL++) {

            short clID = (short) iCL;
            short trkID = (short) (clBank.getShort("trkID", iCL) - 1);

            if (trkID < 0) {
                // We need only hits that contribute to a track
                continue;
            }

            //short pindex = trkBank.getShort("pindex", trkID);
            short pindex = trkBank.getShort("pindex", trkID2Index.get(DetectorType.CVT.getDetectorId()).get(trkID));

            RecCluster curCl = new RecCluster();
            curCl.id = (short) iCL;
            curCl.detector = (byte) DetectorType.BST.getDetectorId();
            curCl.energy = clBank.getFloat("ETot", iCL);
            curCl.rectid = trkID;
            curCl.layer = clBank.getByte("layer", iCL);
            curCl.sector = clBank.getByte("sector", iCL);
            curCl.pindex = pindex;
            curCl.size = clBank.getShort("size", iCL);
            curCl.superlayer = -1; // NA for this detector
            cls.add(curCl);
        }

        return cls;
    }

    List<RecCluster> getBMTClusters(DataEvent event, Map<Integer, Map<Short, Integer>> trkID2Index) {
        List<RecCluster> cls = new ArrayList<>();

        /**
         * Check if three necessary banks exist otherwise will return null
         */
        if ((event.hasBank("BMTRec::Clusters") == false) || (event.hasBank("REC::Track") == false)) {
            //System.out.println("There is No BMTRec::clusters bank, or there is No BMTRec::Hits bank, or there is no REC::Track bank present");
            return null;
        }

        DataBank clBank = event.getBank("BMTRec::Clusters");
        DataBank trkBank = event.getBank("REC::Track");

        for (int iCL = 0; iCL < clBank.rows(); iCL++) {

            short clID = (short) iCL;
            short trkID = (short) (clBank.getShort("trkID", iCL) - 1);

            if (trkID < 0) {
                // We need only hits/clusters that contribute to a track
                continue;
            }

            //short pindex = trkBank.getShort("pindex", trkID);
            short pindex = trkBank.getShort("pindex", trkID2Index.get(DetectorType.CVT.getDetectorId()).get(trkID));

            RecCluster curCl = new RecCluster();
            curCl.id = (short) iCL;
            curCl.detector = (byte) DetectorType.BMT.getDetectorId();
            curCl.energy = clBank.getFloat("ETot", iCL);
            curCl.rectid = trkID;
            curCl.layer = clBank.getByte("layer", iCL);
            curCl.sector = clBank.getByte("sector", iCL);
            curCl.pindex = pindex;
            curCl.size = clBank.getShort("size", iCL);
            curCl.superlayer = -1; // NA for this detector
            cls.add(curCl);
        }

        return cls;
    }

    List<RecCluster> getDCClusters(DataEvent event) {
        List<RecCluster> cls = new ArrayList<>();

        /**
         * Check if three necessary banks exist otherwise will return null
         */
        if ((event.hasBank("TimeBasedTrkg::TBHits") == false) || (event.hasBank("REC::Track") == false)) {
            return null;
        }
        
        DataBank trkBank = event.getBank("REC::Track");

        /**
         * In the case of DC, a cluster is a single DC hit.
         */
        DataBank clBank = event.getBank("TimeBasedTrkg::TBHits");
        DataBank tbtrkBank = event.getBank("TimeBasedTrkg::TBTracks");
        
        /*
        * We need to get a conversion from HitID to Rec::Track Ind
        */
        Map<Short, Short> tbTrkID2RecTrInd = new HashMap();
        for (short iRec = 0; iRec < trkBank.rows(); iRec++) {
            Byte det = trkBank.getByte("detector", iRec);
            Short index = trkBank.getShort("index", iRec);

            if (det == DetectorType.DC.getDetectorId()) {
                tbTrkID2RecTrInd.put(tbtrkBank.getShort("id", index), iRec);
            }
        }
        
        

        for (int iCL = 0; iCL < clBank.rows(); iCL++) {

            short clID = (short) iCL;
            short trkID = (short) (clBank.getByte("trkID", iCL) );

            if (trkID < 0) {
                // This should not happen, just in case
                continue;
            }
            //System.out.println("trkID = " + trkID);
            int trInd = tbTrkID2RecTrInd.get(trkID );
            short pindex = trkBank.getShort("pindex", trInd);
            RecCluster curCl = new RecCluster();
            curCl.id = (short) (clBank.getShort("id", iCL) - 1);
            curCl.detector = (byte) DetectorType.DC.getDetectorId();
            curCl.energy = -1;
            curCl.rectid = 0;
            curCl.rectid = trkID;
            curCl.layer = clBank.getByte("layer", iCL);
            curCl.sector = clBank.getByte("sector", iCL);
            curCl.pindex = pindex;
            //System.out.println("id = " + curCl.id + "   pindex = " + pindex);
            curCl.size = 1;
            curCl.superlayer = clBank.getByte("superlayer", iCL);
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

        for (RecCluster cl : cls) {

            /**
             * The Key of the map is the MCParticleindex, that created a given
             * hit in a cluster, while the value of the map shows # of hits in
             * the cluster from that MCParticle
             */
            Map<Integer, Integer> matchMCParts = new HashMap<>();

            List<RecHit> recHits = Rechits_a.get(cl.id);

            if (recHits == null) {
                /**
                 * ************* AA TT EE NN TT II OO NN *******************
                 * This needs to be resolved!! There should not be cases where
                 * there is a cluster, but non of hits has clusterId pointing to
                 * that cluster. A possibility is that because of shared hits
                 * all hits of a given cluster is shared wit other cluster(s).
                 *
                 * Needs to be investigated
                 */
//                System.out.println("Oo, recHits is Null. Tot # of cluster is " + cls.size() + "    The energy of the cluster is " + cl.energy);
//                System.out.println("Cluster is " + cl.toString());
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
                } else {
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
                cl.mcotid = maxEntry.getKey().shortValue();
            }

        }

    }

    /**
     *
     * @param pKeys: Set of Particle iDs, // For MCParticle it is id, and for
     * RecParticle it is pindex
     * @param cls : List of clusters
     * @return : Map<MCPId, List<Clusters>>, returns list of RecClusters for
     * each MC particle
     */
    Map<Short, List<RecCluster>> mapClustersToParticles(Set<Short> pKeys, List<RecCluster> cls, String DataSet) throws Exception {
        Map<Short, List<RecCluster>> map = new HashMap<>();

        for (short theKey : pKeys) {

            map.put(theKey, new ArrayList<>());
        }

        for (RecCluster curCl : cls) {

            short partId;
            if (DataSet == "MC") {
                partId = curCl.mcotid;
            } else if (DataSet == "Rec") {
                partId = curCl.pindex;
            } else {
                System.out.println("The dataSet provided is " + DataSet);
                throw new RuntimeException("The Data set should be 'MC' or 'Rec' ");
            }

            if (map.get(partId) == null) {
                // Should not happen, but just in case
                map.put(partId, new ArrayList<>());
            }

            map.get(partId).add(curCl);

        }

        return map;
    }

    /**
     * @param mcp: the Key is the index of the MCparticle in the MC::Particle
     * bank, and the value is MCPart object
     * @param clsPerMCp: The key is MCParticle index in the MC::Particle bank
     * and the value is the list of clusters for that MC::Particle
     * @return
     */
    List<MCRecMatch> MakeMCRecMatch(Map<Short, MCPart> mcp, Map<Short, List<RecCluster>> clsPerMCp) {

        List<MCRecMatch> recMatch = new ArrayList<>();

        for (short imc : mcp.keySet()) {

            MCRecMatch match = new MCRecMatch();

            match.id = imc;
            match.MCLayersTrk = mcp.get(imc).MCLayersTrk;
            match.MCLayersNeut = mcp.get(imc).MCLayersNeut;

            /**
             * Generally speaking it is possible that all clusters of a given MC
             * particles will not have the same Rec::Particle. So we will make a
             * map here Map<pindex, count>, and the matched Rec::Particle will
             * be the one with highest count.
             */
            Map<Short, Integer> matched_counts = new HashMap<>();

            Map<Short, Integer> matched_PCalcounts = new HashMap<>();
            Map<Short, Integer> matched_ECcounts = new HashMap<>();
            Map<Short, Integer> matched_FTCalcounts = new HashMap<>();
            Map<Short, Integer> matched_CNDcounts = new HashMap<>();
            Map<Short, Integer> matched_CTOFcounts = new HashMap<>();
            Map<Short, Integer> matched_BSTcounts = new HashMap<>();
            Map<Short, Integer> matched_BMTcounts = new HashMap<>();

            int nPCal = 0;
            int nEC = 0;
            int nFTCal = 0;
            int nCND = 0;
            int nCTOF = 0;
            int nBST = 0;
            int nBMT = 0;

            /**
             * Making sure there are clusters created from the given MCParticle
             */
            if (!clsPerMCp.get(imc).isEmpty()) {

                for (RecCluster curCl : clsPerMCp.get(imc)) {

                    incrementMap(matched_counts, curCl.pindex);

                    final int det = (int) curCl.detector;

                    /**
                     * Can not use swith with with det.getdetectorID() so will
                     * make
                     */
                    switch (det) {

                        case ECALID:

                            if (curCl.layer == 1) {
                                // PCal
                                incrementMap(matched_PCalcounts, curCl.pindex);
                            } else if (curCl.layer == 4 || curCl.layer == 7) {
                                // EC
                                incrementMap(matched_ECcounts, curCl.pindex);
                            }
                            break;
                        case FTCALID:
                            incrementMap(matched_FTCalcounts, curCl.pindex);

                        case BMTID:
                            incrementMap(matched_BMTcounts, curCl.pindex);
                            break;
                        case BSTID:
                            incrementMap(matched_BSTcounts, curCl.pindex);
                            break;
                        case CNDID:
                            incrementMap(matched_CNDcounts, curCl.pindex);
                        case CTOFID:
                            incrementMap(matched_CTOFcounts, curCl.pindex);
                            break;
                    }

                }

                match.pindex = getMaxEntryKey(matched_counts);
                match.RecLayersTrk = 0L;
                match.RecLayersNeut = 0L;
                if (mcp.get(imc).RecLayersTrk.containsKey((int) match.pindex)) {
                    match.RecLayersTrk = mcp.get(imc).RecLayersTrk.get((int) match.pindex);
                }
                if (mcp.get(imc).RecLayersNeut.containsKey((int) match.pindex)) {
                    match.RecLayersNeut = mcp.get(imc).RecLayersNeut.get((int) match.pindex);
                }
                //match.RecLayersTrk = recp.get(match.pindex).RecLayersTrk;

            } else {
                match.pindex = -1;
                match.RecLayersTrk = 0L;
                match.RecLayersNeut = 0L;
            }
            recMatch.add(match);
        }

        return recMatch;
    }

    List<MCRecMatch> MakeRecMCMatch(Map<Short, RecPart> recp, Map<Short, List<RecCluster>> clsPerRecP) {

        List<MCRecMatch> recMatch = new ArrayList<>();

        for (short iRec : recp.keySet()) {

            MCRecMatch match = new MCRecMatch();

            match.pindex = iRec;
            match.RecLayersTrk = recp.get(iRec).RecLayersTrk;
            match.RecLayersNeut = recp.get(iRec).RecLayersNeut;

            /**
             * Generally speaking it is possible that all clusters of a given MC
             * particles will not have the same Rec::Particle. So we will make a
             * map here Map<pindex, count>, and the matched Rec::Particle will
             * be the one with highest count.
             */
            Map<Short, Integer> matched_counts = new HashMap<>();

            /**
             * Making sure there are clusters created from the given MCParticle
             */
            if (!clsPerRecP.get(iRec).isEmpty()) {

                for (RecCluster curCl : clsPerRecP.get(iRec)) {
                    incrementMap(matched_counts, curCl.mcotid);
                }

                match.id = getMaxEntryKey(matched_counts);
                match.MCLayersTrk = 0L;
                match.MCLayersNeut = 0L;
                if (recp.get(iRec).MCLayersTrk.containsKey((int) match.id)) {
                    match.MCLayersTrk = recp.get(iRec).MCLayersTrk.get((int) match.id);
                }

                if (recp.get(iRec).MCLayersNeut.containsKey((int) match.id)) {
                    match.MCLayersNeut = recp.get(iRec).MCLayersNeut.get((int) match.id);
                }

            } else {
                match.id = -1;
                match.MCLayersTrk = 0L;
                match.MCLayersNeut = 0L;
            }

            recMatch.add(match);
        }

        return recMatch;
    }

    void CalculateMatchCuality(DataEvent event, List<MCRecMatch> mcp, List<MCRecMatch> recp) {

        // At this stage we know that MC::Particle and REC::Particle must exist, so will not additionaly check for them
        DataBank mcPartBank = event.getBank("MC::Particle");

        for (int j = 0; j < mcp.size(); j++) {
            MCRecMatch mcMatch = mcp.get(j);
            Integer pid = mcPartBank.getInt("pid", j);
            Integer charge = PDGDatabase.getParticleById(pid).charge();
            //System.out.println("pid = " + mcPartBank.getInt("pid", j) + "    Charge = " + charge);
            Float quality = 0.F;

            if (charge != 0) {
                if (CheckDCAcceptance(mcMatch.MCLayersTrk, (short) 5, (short) 4) || CheckCVTAcceptance(mcMatch.MCLayersTrk)) {
                    quality = 0.95F;

                    if (CheckDCAcceptance(mcMatch.RecLayersTrk, (short) 5, (short) 4) || CheckCVTAcceptance(mcMatch.RecLayersTrk)) {
                        quality = 0.98F;
                    }
                }
            } else {

                if (pid == 2112 || pid == -2112) {

                    if (CountNSetBits(mcMatch.MCLayersNeut, (short) 0, (short) 6) > 0) {
                        quality = 0.91F;

                        if (CountNSetBits(mcMatch.RecLayersNeut, (short) 0, (short) 6) > 0) {
                            quality = 0.92F;
                        }
                    }
                } else if (pid == 22) {
                    if (CountNSetBits(mcMatch.RecLayersNeut, (short) 0, (short) 0) > 0 || CountNSetBits(mcMatch.RecLayersNeut, (short) 3, (short) 6) > 0) {
                        quality = 0.93F;
                        if (CountNSetBits(mcMatch.RecLayersNeut, (short) 0, (short) 0) > 0 || CountNSetBits(mcMatch.RecLayersNeut, (short) 3, (short) 6) > 0) {
                            quality = 0.94F;
                        }
                    }
                }

            }

            mcMatch.quality = quality;
        }

        // At this stage we know that MC::Particle and REC::Particle must exist, so will not additionaly check for them
        DataBank recPartBank = event.getBank("REC::Particle");

        for (int j = 0; j < recp.size(); j++) {
            MCRecMatch recMatch = recp.get(j);
            Integer pid = recPartBank.getInt("pid", j);
            Integer charge = recPartBank.getInt("charge", j);

            Float quality = 0.F;

            //Integer charge = PDGDatabase.getParticleById(pid).charge();
            if (charge != 0) {
                if (CheckDCAcceptance(recMatch.RecLayersTrk, (short) 5, (short) 4) || CheckCVTAcceptance(recMatch.RecLayersTrk)) {
                    quality = 0.95F;

                    if (CheckDCAcceptance(recMatch.MCLayersTrk, (short) 5, (short) 4) || CheckCVTAcceptance(recMatch.MCLayersTrk)) {
                        quality = 0.98F;
                    }
                }
            } else {

                if (pid == 2112 || pid == -2112) {

                    if (CountNSetBits(recMatch.RecLayersNeut, (short) 0, (short) 6) > 0) {
                        quality = 0.91F;

                        if (CountNSetBits(recMatch.MCLayersNeut, (short) 0, (short) 6) > 0) {
                            quality = 0.92F;
                        }
                    }
                } else if (pid == 22) {
                    if (CountNSetBits(recMatch.RecLayersNeut, (short) 0, (short) 0) > 0 || CountNSetBits(recMatch.RecLayersNeut, (short) 3, (short) 6) > 0) {
                        quality = 0.93F;
                        if (CountNSetBits(recMatch.RecLayersNeut, (short) 0, (short) 0) > 0 || CountNSetBits(recMatch.RecLayersNeut, (short) 3, (short) 6) > 0) {
                            quality = 0.94F;
                        }
                    }
                }

            }

            recMatch.quality = quality;
        }

    }

    void bankWriter(DataEvent event, List<MCRecMatch> mcp, List<MCRecMatch> recp) {

        DataBank bank = event.createBank("MC::GenMatch", mcp.size());
        for (int j = 0; j < mcp.size(); j++) {
            MCRecMatch p = mcp.get(j);
            bank.setShort("mcindex", j, p.id);
            bank.setShort("pindex", j, p.pindex);
            bank.setLong("mclayer1", j, p.MCLayersTrk);
            bank.setLong("mclayer2", j, p.MCLayersNeut);
            bank.setLong("player1", j, p.RecLayersTrk);
            bank.setLong("player2", j, p.RecLayersNeut);
            bank.setFloat("quality", j, p.quality);
        }

        event.appendBanks(bank);

        DataBank bankRecMatch = event.createBank("MC::RecMatch", recp.size());

        for (int j = 0; j < recp.size(); j++) {
            MCRecMatch p = recp.get(j);

            //Long.toBinaryString(mcp.get((short) mchitsInBMT.get(hitID).otid).MCLayersTrk)
            //System.out.println( Long.toBinaryString(p.RecLayersTrk) + "     " +  Long.toBinaryString(p.MCLayersTrk));
            bankRecMatch.setShort("pindex", j, p.pindex);
            bankRecMatch.setShort("mcindex", j, p.id);
            bankRecMatch.setLong("player1", j, p.RecLayersTrk);
            bankRecMatch.setLong("player2", j, p.RecLayersNeut);
            bankRecMatch.setLong("mclayer1", j, p.MCLayersTrk);
            bankRecMatch.setLong("mclayer2", j, p.MCLayersNeut);
            bankRecMatch.setFloat("quality", j, p.quality);
        }

        event.appendBank(bankRecMatch);

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

    void PrintRecMatches(List<MCRecMatch> matches) {

        System.out.println("** ************  MATCHED Objects *********************");
        for (int i = 0; i < matches.size(); i++) {

            String strIndex;
            if (i == 0) {
                strIndex = "1st";
            } else if (i == 1) {
                strIndex = "2nd";
            } else if (i == 2) {
                strIndex = "3rd";
            } else {
                strIndex = String.valueOf(i + 1) + "th";
            }

            System.out.println("** ********" + strIndex + "Particle *********");
            System.out.println("** id = " + matches.get(i).id);
            System.out.println("** pindex = " + matches.get(i).pindex);
            System.out.println("");

        }
        System.out.println("** *********  End of MATCHED Objects *****************");

    }

    void PrintClsPerMc(Map<Short, List<RecCluster>> map) {

        System.out.println("** ******** Map of Clusters per MC particle **************");

        if (!map.isEmpty()) {

            for (Short curKey : map.keySet()) {

                int nCl = map.get(curKey).size();
                System.out.println("mcotid  = " + curKey + "     # of clusters is " + nCl);

                for (int ii = 0; ii < nCl; ii++) {

                    System.out.println("                ***** printing the cluster #" + ii + " *****");
                    System.out.print(map.get(curKey).get(ii).toString());
                }
            }

        } else {
            System.out.println("The Map is empty");
        }

        System.out.println("** ***** End of Map of Clusters per MC particle **********");

    }

    /**
     * This function as an argument expects counter maps, i.e. maps, which have
     * the value as a counter of Keys. I will increment the value of the map
     * each time it is called, and if the key doesn't exist, it will make an
     * entry with that key and will assign value = 1
     *
     * @param <T> : the type of the make Key
     * @param map : the map
     * @param var : is the key of the map
     */
    private <T> void incrementMap(Map<T, Integer> map, T var) {
        if (map.get(var) == null) {
            map.put(var, 1);
        } else {
            map.put(var, map.get(var) + 1);
        }
    }

    /**
     * Returns the Key of the map that has the highest counts
     *
     * @param <T> Type of the Map keys
     * @param map The actuall map
     * @return The Key, which has highest counts
     */
    private <T> T getMaxEntryKey(Map<T, Integer> map) {

        if (map.isEmpty()) {
            System.out.println("Oho Map is empty. Returning null");
            return null;
        }

        Map.Entry<T, Integer> maxEntry = null;
        for (Map.Entry<T, Integer> entry : map.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }

        return maxEntry.getKey();
    }

    /**
     * This function returns the number of "set" bits in between bit1 (included)
     * and bit2 (included) of the variable type Long
     *
     * @param word : The word we want to check
     * @param bit1 : index of bit1
     * @param bit2 : index of bit1
     * @return : The number of set bits between bit1 and bit2
     */
    public Integer CountNSetBits(Long word, short bit1, short bit2) {
        int count = 0;

        for (int i = bit1; i <= bit2; i++) {
            count += (word >> i) & 1;
        }

        return count;
    }

    /**
     * Check whether enough layers and SLs are hit to make the track
     * reconstractable in DC
     *
     * @param word to be checked
     * @param nMinSL : Minimum number of SLayers
     * @param nMinLayerPerSL : minimum number of layer per SL, in order a SL to
     * be counted as hit
     * @return Whether the the DC track is reconstractable
     */
    public Boolean CheckDCAcceptance(Long word, short nMinSL, short nMinLayerPerSL) {
        short nSL = 0;

        for (short iSL = 0; iSL < 6; iSL++) {

            short layersPerSL = 0;

            for (short ilayer = 0; ilayer < 6; ilayer++) {
                layersPerSL += (word >> (iSL * 6 + ilayer)) & 1;
            }

            if (layersPerSL >= nMinLayerPerSL) {
                nSL = (short) (nSL + 1);
            }
        }

        return nSL >= nMinSL;
    }

    /**
     * Check whether the track is reconstructable in CVT reconstractable in DC
     *
     * @param word to be checked
     * @return Whether the the CVT track is reconstructable
     */
    public Boolean CheckCVTAcceptance(Long word) {

        //std::cout<<"Word is "<<word<<std::endl;
        int bstR1 = CountNSetBits(word, (short) BSTStartBit, (short) (BSTStartBit + 1)) == 2 ? 1 : 0;
        int bstR2 = CountNSetBits(word, (short) (BSTStartBit + 2), (short) (BSTStartBit + 3)) == 2 ? 1 : 0;
        int bstR3 = CountNSetBits(word, (short) (BSTStartBit + 4), (short) (BSTStartBit + 5)) == 2 ? 1 : 0;
        int nSVTCross = (int) bstR1 + (int) bstR2 + (int) bstR3;

        int nBMT_Z = CountNSetBits(word, (short) (BMTStartBit + 1), (short) (BMTStartBit + 1))
                + CountNSetBits(word, (short) (BMTStartBit + 2), (short) (BMTStartBit + 2))
                + CountNSetBits(word, (short) (BMTStartBit + 4), (short) (BMTStartBit + 4));
        int nBMT_C = CountNSetBits(word, (short) (BMTStartBit), (short) BMTStartBit)
                + CountNSetBits(word, (short) (BMTStartBit + 3), (short) (BMTStartBit + 3))
                + CountNSetBits(word, (short) (BMTStartBit + 5), (short) (BMTStartBit + 5));

        //  1 BMT Z-detector cross + 2 SVT crosses;
        //  2 BMT Z-detector cross +1 SVT cross + 1 BMT C-detector cross ;
        return ((nBMT_Z >= 1) && (nSVTCross >= 2)) || ((nBMT_Z >= 2) && (nSVTCross >= 1) && (nBMT_C >= 1));

    }

    /**
     *
     * @param word // The status word to be checked
     * @param EC_part // ECal part: 0 for PCal, 1 for EC_in and 2 for EC_out
     * @return // true if the corresponding ECal layer bit is set
     */
    public Boolean CheckECalLayers(Long word, short EC_part) {

        if (EC_part != 0 || EC_part != 1 || EC_part != 2) {
            throw new RuntimeException(" The 2nd argument value should be 0,1 or 2 // representing PCal, EC_in and EC_out respectively");
        }

        return CountNSetBits(word, (short) (EC_part + ECalStartBit), (short) (EC_part + ECalStartBit)) > 0;
    }

    /**
     *
     * @param word // The status word to be checked
     * @param layer // CND layer: 0 for layer 1, 1 for layer 2, 2 for layer 3
     * @return // true if the corresponding CND layer bit is set
     */
    public Boolean CheckCNDLayers(Long word, short layer) {

        if (layer != 0 || layer != 1 || layer != 2) {
            throw new RuntimeException(" The 2nd argument value should be 0,1 or 2 // representing CND layers, 1, 2 and 3 respectively");
        }

        return CountNSetBits(word, (short) (layer + CNDStartBit), (short) (layer + CNDStartBit)) > 0;
    }

    /**
     *
     * @param word // The status word to be checked
     * @return // true if CTOF bit is set
     */
    public Boolean CheckCTOF(Long word) {

        return CountNSetBits(word, (short) CTOFBit, (short) CTOFBit) > 0;
    }

    /**
     *
     * @param word // The status word to be checked
     * @param layer // FTCalHodo: 0 for Hodo layer 1, 1 for Hodo layer 2, 2 for
     * FTCal
     * @return // true if the corresponding FTCalHodo bit is set
     */
    public Boolean CheckFTHodoLayers(Long word, short layer) {

        if (layer != 0 || layer != 1) {
            throw new RuntimeException(" The 2nd argument value should be 0 or 1 // representing FTHodo layers, 1, 2  respectively");
        }

        return CountNSetBits(word, (short) (layer + FTHodoStartBit), (short) (layer + FTHodoStartBit)) > 0;
    }

    /**
     *
     * @param word // The status word to be checked
     * @return // true if CTOF bit is set
     */
    public Boolean CheckFTCal(Long word) {

        return CountNSetBits(word, (short) FTCalBit, (short) FTCalBit) > 0;
    }

}
