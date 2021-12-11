package org.jlab.rec.dc.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.timetodistance.TimeToDistanceEstimator;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.Constants;
import org.jlab.utils.groups.IndexedTable;

/**
 * A class to fill in lists of hits corresponding to DC reconstructed hits
 * characterized by the wire, its location in the detector (superlayer, layer,
 * sector), its reconstructed time. The class also returns a MC hit, which has
 * truth-information (i.e. Left-Right ambiguity)
 *
 * @author ziegler
 */
public class HitReader {

    private Banks bankNames = null;
    
    public HitReader(Banks names) {
        this.bankNames= names;
            
    }
    
    private static final Logger LOGGER = Logger.getLogger(HitReader.class.getName());
    
    private List<Hit> _DCHits;

    private List<FittedHit> _HBHits; //hit-based tracking hit information
    private List<FittedHit> _TBHits; //time-based tracking hit information

    /**
     * @return a list of DC hits
     */
    public List<Hit> get_DCHits() {
        return _DCHits;
    }

    /**
     * sets the list of DC hits
     *
     * @param _DCHits list of DC hits
     */
    private void set_DCHits(List<Hit> _DCHits) {
        this._DCHits = _DCHits;
    }

    /**
     * @return list of DCHB hits
     */
    public List<FittedHit> get_HBHits() {
        return _HBHits;
    }

    /**
     * sets the list of HB DC hits
     *
     * @param _HBHits list of DC hits
     */
    private void set_HBHits(List<FittedHit> _HBHits) {
        this._HBHits = _HBHits;
    }

    /**
     * @return list of DCTB hits
     */
    public List<FittedHit> get_TBHits() {
        return _TBHits;
    }

    /**
     * sets the list of HB DC hits
     *
     * @param _TBHits list of DC hits
     */
    private void set_TBHits(List<FittedHit> _TBHits) {
        this._TBHits = _TBHits;
    }
    private final double timeBuf = 25.0;
    /**
     * reads the hits using clas-io methods to get the EvioBank for the DC and
     * fill the values to instantiate the DChit and MChit classes.This methods
 fills the DChit list of hits.
     *
     * @param event DataEvent
     * @param noiseAnalysis
     * @param parameters
     * @param results
     * @param tab
     * @param tab2
     * @param tab3
     * @param DcDetector
     * @param triggerPhase
     */
    public void fetch_DCHits(DataEvent event, Clas12NoiseAnalysis noiseAnalysis,
                             NoiseReductionParameters parameters,
                             Clas12NoiseResult results, IndexedTable tab,
                             IndexedTable tab2, IndexedTable tab3,
                             DCGeant4Factory DcDetector,
                             double triggerPhase) {

        if (!event.hasBank(bankNames.getTdcBank())) {
            _DCHits = new ArrayList<>();

            return;
        }

        //cut on max number of hits
        if (event.getBank(bankNames.getTdcBank()).rows()>Constants.MAXHITS) {
            _DCHits = new ArrayList<>();

            return;
        }
//        if(true)return;// DDD BREAK BREAK BREAK

        DataBank bankDGTZ = event.getBank(bankNames.getTdcBank());

        int rows = bankDGTZ.rows();
        int[] sector = new int[rows];
        int[] layer = new int[rows];
        int[] wire = new int[rows];
        int[] tdc = new int[rows];
        int[] useMChit = new int[rows];

        for (int i = 0; i < rows; i++) {
            sector[i] = bankDGTZ.getByte("sector", i);
            layer[i] = bankDGTZ.getByte("layer", i);
            wire[i] = bankDGTZ.getShort("component", i);
            tdc[i] = bankDGTZ.getInt("TDC", i);

        }


        if (event.hasBank(bankNames.getDocaBank())) {
            DataBank bankD = event.getBank(bankNames.getDocaBank());
            int bd_rows = bankD.rows();
            for (int i = 0; i < bd_rows; i++) {
                if (bankD.getFloat("stime", i) < 0) {
                    useMChit[i] = -1;
                }
            }
        }
        int size = layer.length;
        int[] layerNum = new int[size];
        int[] superlayerNum = new int[size];
        double[] smearedTime = new double[size];

        List<Hit> hits = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (tdc.length > 0) {
                smearedTime[i] = (double) tdc[i] - triggerPhase;
                if (smearedTime[i] < 0) {
                    smearedTime[i] = 1;
                }
            }

            superlayerNum[i] = (layer[i] - 1) / 6 + 1;
            layerNum[i] = layer[i] - (superlayerNum[i] - 1) * 6;

        }
        results.clear();
        noiseAnalysis.clear();


         noiseAnalysis.findNoise(sector, superlayerNum, layerNum, wire, results);

        for (int i = 0; i < size; i++) {
            boolean passHit = true;
            if (tab3 != null) {
                if (tab3.getIntValue("status", sector[i], layer[i], wire[i]) != 0)
                    passHit = false;
            }
            if (passHit && wire[i] != -1 && !results.noise[i] && useMChit[i] != -1 && !(superlayerNum[i] == 0)) {

                double timeCutMin = 0;
                double timeCutMax = 0;
                double timeCutLC = 0;

                int region = ((superlayerNum[i] + 1) / 2);

                switch (region) {
                    case 1:
                        timeCutMin = tab2.getIntValue("MinEdge", 0, region, 0);
                        timeCutMax = tab2.getIntValue("MaxEdge", 0, region, 0);
                        break;
                    case 2:
                        if (wire[i] <= 56) {
                            timeCutLC = tab2.getIntValue("LinearCoeff", 0, region, 1);
                            timeCutMin = tab2.getIntValue("MinEdge", 0, region, 1);
                            timeCutMax = tab2.getIntValue("MaxEdge", 0, region, 1);
                        }
                        if (wire[i] > 56) {
                            timeCutLC = tab2.getIntValue("LinearCoeff", 0, region, 56);
                            timeCutMin = tab2.getIntValue("MinEdge", 0, region, 56);
                            timeCutMax = tab2.getIntValue("MaxEdge", 0, region, 56);
                        }
                        break;
                    case 3:
                        timeCutMin = tab2.getIntValue("MinEdge", 0, region, 0);
                        timeCutMax = tab2.getIntValue("MaxEdge", 0, region, 0)+timeBuf;
                        break;
                }
                boolean passTimingCut = false;

                if (region == 1 && smearedTime[i] > timeCutMin && smearedTime[i] < timeCutMax)
                    passTimingCut = true;
                if (region == 2) {
                    double Bscale = Swimmer.getTorScale() * Swimmer.getTorScale();
                    if (wire[i] >= 56) {
                        if (smearedTime[i] > timeCutMin &&
                                smearedTime[i] < timeCutMax + timeCutLC * (double) (112 - wire[i] / 56) * Bscale)
                            passTimingCut = true;
                    } else {
                        if (smearedTime[i] > timeCutMin &&
                                smearedTime[i] < timeCutMax + timeCutLC * (double) (56 - wire[i] / 56) * Bscale)
                            passTimingCut = true;
                    }
                }
                if (region == 3 && smearedTime[i] > timeCutMin && smearedTime[i] < timeCutMax)
                    passTimingCut = true;

                if (passTimingCut) { // cut on spurious hits
                    //Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], smearedTime[i], 0, 0, hitno[i]);			
                    Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], tdc[i], (i + 1));
                    hit.set_Id(i + 1);
                    hit.calc_CellSize(DcDetector);
                    double posError = hit.get_CellSize() / Math.sqrt(12.);
                    hit.set_DocaErr(posError);
                    hits.add(hit);
                }
            }
        }

        this.set_DCHits(hits);

    }
    
    public Map<Integer, ArrayList<FittedHit>> read_Hits(DataEvent event, DCGeant4Factory dcDetector) {
        Map<Integer, ArrayList<FittedHit>> grpHits = new HashMap<>();
        
        if (!event.hasBank(bankNames.getInputHitsBank())) {
            return null;
        }
        
        DataBank bank = event.getBank(bankNames.getInputHitsBank());
        int rows = bank.rows();

        List<FittedHit> hits = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            int id          = bank.getShort("id", i);
            int sector      = bank.getByte("sector", i);
            int slayer      = bank.getByte("superlayer", i);
            int layer       = bank.getByte("layer", i);
            int wire        = bank.getShort("wire", i);
            int tdc         = bank.getInt("TDC", i);
            int LR          = bank.getByte("LR", i);
            int clusterID   = bank.getShort("clusterID", i);
            
        
            //use only hits that have been fit to a track
            if (clusterID == -1) {
                continue;
            }
            
            FittedHit hit = new FittedHit(sector, slayer, layer, wire, tdc, id);
            hit.set_Id(id);
            hit.set_AssociatedClusterID(clusterID);
            hit.set_TrkgStatus(0);
            hit.calc_CellSize(dcDetector);
            hit.calc_GeomCorr(dcDetector, 0);
            double posError = hit.get_CellSize() / Math.sqrt(12.);
            hit.set_DocaErr(posError);
            hits.add(hit);   
        }
        for (FittedHit hit : hits) {
            
            if (hit.get_AssociatedClusterID() == -1) {
                continue;
            }
            if (hit.get_AssociatedClusterID() != -1 ) {
                
                int index = hit.get_AssociatedClusterID();
                if(grpHits.get(index)==null) { // if the list not yet created make it
                    grpHits.put(index, new ArrayList<>()); 
                    grpHits.get(index).add(hit); // append hit
                } else {
                    grpHits.get(index).add(hit); // append hit
                }
            }
        }
        return grpHits;
    }

    private final Map<Integer, Integer> id2tid = new HashMap<Integer, Integer>();
    private final Map<Integer, Double> id2tidB = new HashMap<Integer, Double>();
    private final Map<Integer, Double> id2tidtProp = new HashMap<Integer, Double>();
    private final Map<Integer, Double> id2tidtFlight = new HashMap<Integer, Double>();
    
    private final Map<Integer, double[]> aimatch = new HashMap<Integer, double[]>();
    /**
     * Reads HB DC hits written to the DC bankAI
     *
     * @param event      .
     * @param constants0 .
     * @param constants1 .
     * @param t0Table
     * @param DcDetector .
     * @param tde        .
     */
    public void read_HBHits(DataEvent event, IndexedTable constants0, IndexedTable constants1, IndexedTable t0Table, 
            DCGeant4Factory DcDetector, TimeToDistanceEstimator tde) {
        /*
        0: this.getConstantsManager().getConstants(newRun, "/calibration/dc/signal_generation/doca_resolution"),
        1: this.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/t2d")
        */
        String bankName    = bankNames.getInputHitsBank();
        String pointName   = bankNames.getInputIdsBank();
        String recBankName = bankNames.getRecEventBank();
        
        LOGGER.log(Level.FINE,"Reading hb banks for "+ bankName + ", " + pointName + " " + recBankName);
        
        if (!event.hasBank(bankName) || !event.hasBank(pointName) || event.getBank(pointName).rows()==0) {
            //    System.err.println("there is no HB dc bankAI for "+_names[0]);
            _HBHits = new ArrayList<>();
            return;
        }
        id2tid.clear();
        id2tidB.clear();
        id2tidtFlight.clear();
        id2tidtProp.clear();
        
        DataBank pbank = event.getBank(pointName);
        for (int i = 0; i < pbank.rows(); i++) {
            id2tid.put((int)pbank.getShort("id", i), (int)pbank.getShort("tid", i));
            id2tidB.put((int)pbank.getShort("id", i), (double)pbank.getFloat("B", i));
            id2tidtFlight.put((int)pbank.getShort("id", i), (double)pbank.getFloat("TFlight", i));
            id2tidtProp.put((int)pbank.getShort("id", i), (double)pbank.getFloat("TProp", i));
        }
        
        DataBank bank = event.getBank(bankName);
        int rows = bank.rows();

        int[] id = new int[rows];
        int[] status = new int[rows];
        int[] sector = new int[rows];
        int[] slayer = new int[rows];
        int[] layer = new int[rows];
        int[] wire = new int[rows];
        int[] tdc = new int[rows];
        int[] LR = new int[rows];
        double[] B = new double[rows];
        int[] clusterID = new int[rows];
        int[] trkID = new int[rows];
        double[] tProp = new double[rows];
        double[] tFlight = new double[rows];
        double[] trkDoca = new double[rows];

        for (int i = 0; i < rows; i++) {
            id[i] = bank.getShort("id", i);
            status[i] = bank.getShort("status", i);
            sector[i] = bank.getByte("sector", i);
            slayer[i] = bank.getByte("superlayer", i);
            layer[i] = bank.getByte("layer", i);
            wire[i] = bank.getShort("wire", i);
            tdc[i] = bank.getInt("TDC", i);
            id[i] = bank.getShort("id", i);
            LR[i] = bank.getByte("LR", i);
           
            trkDoca[i] = bank.getFloat("trkDoca", i);
            clusterID[i] = bank.getShort("clusterID", i);
            trkID[i] = -1;
            if(this.id2tid.containsKey(id[i]) ){
                trkID[i]    = this.id2tid.get(id[i]);
                 B[i]       = this.id2tidB.get(id[i]);
                 tProp[i]   = this.id2tidtProp.get(id[i]);
                 tFlight[i] = this.id2tidtFlight.get(id[i]);
            }
            
            if (event.hasBank("MC::Particle") ||
                    event.getBank("RUN::config").getInt("run", 0) < 100) {
                tProp[i] = 0;
                tFlight[i] = 0;
            }
        }

        int size = layer.length;

        List<FittedHit> hits = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            //use only hits that have been fit to a track
            if (trkID[i] == -1) {
                continue;
            }
            
            double T_0 = 0;
            double T_Start = 0;
            
            if (!event.hasBank(recBankName)) {
                continue;
            }
            
            if (event.hasBank(recBankName) && 
                    event.getBank(recBankName).getFloat("startTime", 0)==-1000) {
                continue;
            } 
            
            if (!event.hasBank("MC::Particle") &&
                    event.getBank("RUN::config").getInt("run", 0) > 100) {
                //T_0 = this.get_T0(sector[i], slayer[i], layer[i], wire[i], T0, T0ERR)[0];
                if (event.hasBank(recBankName))
                    T_Start = event.getBank(recBankName).getFloat("startTime", 0);
            }  
            
            T_0 = this.get_T0(sector[i], slayer[i], layer[i], wire[i], t0Table)[0];
            FittedHit hit = new FittedHit(sector[i], slayer[i], layer[i], wire[i], tdc[i], id[i]);
            hit.set_Id(id[i]);
            hit.setB(B[i]);
            hit.setT0(T_0);
            hit.setTStart(T_Start);
            hit.setTProp(tProp[i]);
            //hit.setTFlight(tFlight[i]);
            hit.set_Beta(this.readBeta(event, trkID[i]));
            this.set_BetaFlag(event, trkID[i], hit, hit.get_Beta());//reset beta for out of range assuming the pion hypothesis and setting a flag
            hit.setTFlight(tFlight[i]/hit.get_Beta0to1());
            //resetting TFlight after beta has been obtained
            //hit.setSignalTimeOfFlight(); 
            double T0Sub = (tdc[i] - tProp[i] - tFlight[i] - T_0);

            if (Constants.getInstance().isUSETSTART()) {
                T0Sub -= T_Start;
            }
            hit.set_Time(T0Sub);
            hit.set_LeftRightAmb(LR[i]);
            hit.set_TrkgStatus(0);
            hit.calc_CellSize(DcDetector);
            hit.calc_GeomCorr(DcDetector, 0);
            hit.set_ClusFitDoca(trkDoca[i]);
            hit.set_TimeToDistance(event, 0.0, B[i], constants1, tde);

            hit.set_QualityFac(status[i]);
            if (hit.get_Doca() > hit.get_CellSize()) {
                hit.set_OutOfTimeFlag(true);
                hit.set_QualityFac(3);
            }
            if (hit.get_Time() < 0)
                hit.set_QualityFac(2);
            
            hit.set_DocaErr(hit.get_PosErr(event, B[i], constants0, constants1, tde));
            hit.set_AssociatedClusterID(clusterID[i]);
            hit.set_AssociatedHBTrackID(trkID[i]); 
            
            //if(hit.betaFlag == 0)
            if(passHit(hit.betaFlag)) {
                hits.add(hit);        
                LOGGER.log(Level.FINE, "Passing "+hit.printInfo()+" for "+ bankNames.getHitsBank());            
            }
        }

        this.set_HBHits(hits);
    }
    
    private boolean passHit(int betaFlag) {
        boolean pass = true;
        if(Constants.getInstance().USEBETACUT()) {
            //if(betaFlag != 0) { //all beta cuts
            //    pass = false;
            //}
            if(Math.abs(betaFlag) == 1) { // beta cut: beta >0.15
                pass = false;
            }
        }
        return pass;
    }
    //new way of fetching ai id'ed hits
    public void read_NNHits(DataEvent event, DCGeant4Factory DcDetector) {
        
        if (!(event.hasBank(bankNames.getInputHitsBank()) 
           && event.hasBank(bankNames.getInputClustersBank())
           && event.hasBank(bankNames.getAiBank())  )) {
            _DCHits = new ArrayList<>();
            return;
        }
        List<Hit> hits = new ArrayList<>();
        
        DataBank bankAI = event.getBank(bankNames.getAiBank());
        DataBank bank = event.getBank(bankNames.getInputHitsBank());

        int[] Ids  ;     //  1-6 = cluster ids for slyrs 1 - 6
        double[] tPars ; // NN trk pars p, theta, phi ; last idx = track id;
        for (int j = 0; j < bankAI.rows(); j++) {
            Ids  = new int[6];
            tPars = new double[4];
            Ids[0] = (int)bankAI.getShort("c1", j); // clusId in superlayer 1
            Ids[1] = (int)bankAI.getShort("c2", j);
            Ids[2] = (int)bankAI.getShort("c3", j);
            Ids[3] = (int)bankAI.getShort("c4", j);
            Ids[4] = (int)bankAI.getShort("c5", j);
            Ids[5] = (int)bankAI.getShort("c6", j); // clusId in superlayer 6
            
            tPars[0] = (double)bankAI.getFloat("p", j);
            tPars[1] = (double)bankAI.getFloat("theta", j);
            tPars[2] = (double)bankAI.getFloat("phi", j);
            tPars[3] = (double)bankAI.getByte("id", j);
            
            for (int k = 0; k < 6; k++) {
                aimatch.put(Ids[k], tPars); 
            }
        
            for (int i = 0; i < bank.rows(); i++) {
                int clusterID = bank.getShort("clusterID", i);

                if(clusterID>0) {
                    if(this.aimatch.containsKey(clusterID)) { 
                        Hit hit = new Hit(bank.getByte("sector", i), bank.getByte("superlayer", i), 
                            bank.getByte("layer", i), bank.getShort("wire", i), bank.getInt("TDC", i), bank.getShort("id", i));
                        hit.set_Id(bank.getShort("id", i));
                        hit.calc_CellSize(DcDetector);
                        double posError = hit.get_CellSize() / Math.sqrt(12.);
                        hit.set_DocaErr(posError);
                        hit.NNTrkId  = (int) this.aimatch.get(clusterID)[3];
                        hit.NNClusId = clusterID;
                        hit.NNTrkP      = this.aimatch.get(clusterID)[0];
                        hit.NNTrkTheta  = this.aimatch.get(clusterID)[1];
                        hit.NNTrkPhi    = this.aimatch.get(clusterID)[2];
                        LOGGER.log(Level.FINE, "NN"+hit.printInfo());
                        hits.add(hit);
                    }
                }
            }
        }
        this.set_DCHits(hits);
    }

//    public void read_NNHits(DataEvent event, DCGeant4Factory DcDetector,
//                             double triggerPhase) {
//        if (!(event.hasBank("DC::tdc") && event.hasBank("nn::dchits")  )) {
//            _DCHits = new ArrayList<>();
//
//            return;
//        }
//        DataBank bankAI = event.getBank("nn::dchits");
//        DataBank bankHits = event.getBank("DC::tdc");
//
//        int rows = bankHits.rows();
//        int[] sector    = new int[rows];
//        int[] layer     = new int[rows];
//        int[] wire      = new int[rows];
//        int[] tdc       = new int[rows];
//
//        for (int i = 0; i < rows; i++) {
//            sector[i] = bankHits.getByte("sector", i);
//            layer[i] = bankHits.getByte("layer", i);
//            wire[i] = bankHits.getShort("component", i);
//            tdc[i] = bankHits.getInt("TDC", i);
//
//        }
//
//        int[] layerNum = new int[rows];
//        int[] superlayerNum = new int[rows];
//        double[] smearedTime = new double[rows];
//
//        List<Hit> hits = new ArrayList<>();
//
//        for (int i = 0; i < rows; i++) {
//            
//            smearedTime[i] = (double) tdc[i] - triggerPhase;
//            if (smearedTime[i] < 0) {
//                smearedTime[i] = 1;
//            }
//            
//
//            superlayerNum[i] = (layer[i] - 1) / 6 + 1;
//            layerNum[i] = layer[i] - (superlayerNum[i] - 1) * 6;
//
//        }
//        
//        for (int j = 0; j < bankAI.rows(); j++) {
//            int i = bankAI.getInt("index", j);
//            int tid = (int)bankAI.getByte("id", j);
//            Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], tdc[i], (i + 1));
//            hit.set_Id(i+1);
//            hit.calc_CellSize(DcDetector);
//            double posError = hit.get_CellSize() / Math.sqrt(12.);
//            hit.set_DocaErr(posError);
//            hit.NNTrkId = tid;
//            hits.add(hit);
//        }
//
//        this.set_DCHits(hits);
//    }

    //betaFlag:0 = OK; -1 = negative; 1 = less than lower cut (0.15); 2 = greater than 1.15 (from HBEB beta vs p plots for data)
    private void set_BetaFlag(DataEvent event, int trkId, FittedHit hit, double beta) {
        if(beta<0.15) {
            if(beta<0) {
                hit.betaFlag = -1;
                this.set_ToPionHypothesis(event, trkId, hit);
            } else {
                hit.betaFlag = 1;
            }
        } 
        if(beta>1.15) {
            hit.betaFlag = 2;
        }
    }
    private void set_ToPionHypothesis(DataEvent event, int trkId, FittedHit hit) {
        double piMass = 0.13957018;
        String partBankName = bankNames.getRecPartBank();
        String trackBankName = bankNames.getRecTrackBank();
        double px=0;
        double py=0;
        double pz=0;
        if (!event.hasBank(partBankName) || !event.hasBank(trackBankName))
            return ;
        DataBank bank = event.getBank(trackBankName);

        int rows = bank.rows();
        for (int i = 0; i < rows; i++) {
            if (bank.getByte("detector", i) == 6 &&
                    bank.getShort("index", i) == trkId - 1) {
                px = event.getBank(partBankName).getFloat("px",
                        bank.getShort("pindex", i));
                py = event.getBank(partBankName).getFloat("py",
                        bank.getShort("pindex", i));
                pz = event.getBank(partBankName).getFloat("pz",
                        bank.getShort("pindex", i));
            }
        }
        
        double p = Math.sqrt(px*px+py*py+pz*pz);
        if(p == 0) {
            //System.err.println("DC Track not matched in EB");
            return;
        }
        
        double beta = p/Math.sqrt(p*p + piMass*piMass);
        hit.set_Beta(beta);
    }
    
    private double readBeta(DataEvent event, int trkId) {
        double _beta = 1.0;
        String partBankName = bankNames.getRecPartBank();
        String trackBankName = bankNames.getRecTrackBank();
        if (!event.hasBank(partBankName) || !event.hasBank(trackBankName))
            return _beta;
        DataBank bank = event.getBank(trackBankName);

        int rows = bank.rows();
        for (int i = 0; i < rows; i++) {
            if (bank.getByte("detector", i) == 6 &&
                    bank.getShort("index", i) == trkId - 1) {
                _beta = event.getBank(partBankName).getFloat("beta",
                        bank.getShort("pindex", i));
            }
        }
        //if(_beta>1.0)
        //    _beta=1.0;
        return _beta;
    }


    private double[] get_T0(int sector, int superlayer,
                            int layer, int wire, IndexedTable t0Table) {
        double[] T0Corr = new double[2];

        int cable = this.getCableID1to6(layer, wire);
        int slot = this.getSlotID1to7(wire);

        double t0  = t0Table.getDoubleValue("T0Correction", sector, superlayer, slot, cable);
        double t0E = t0Table.getDoubleValue("T0Error", sector, superlayer, slot, cable);

        T0Corr[0] = t0;
        T0Corr[1] = t0E;

        return T0Corr;
    }

    private int getSlotID1to7(int wire1to112) {
        return ((wire1to112 - 1) / 16) + 1;
    }

    private int getCableID1to6(int layer1to6, int wire1to112) {
        /*96 channels are grouped into 6 groups of 16 channels and each group 
            joins with a connector & a corresponding cable (with IDs 1,2,3,4,& 6)*/
        int wire1to16 = ((wire1to112 - 1) % 16 + 1);
        return this.CableID[layer1to6 - 1][wire1to16 - 1];
    }

    //Map of Cable ID (1, .., 6) in terms of Layer number (1, ..., 6) and localWire# (1, ..., 16)
    private final int[][] CableID = {
            //[nLayer][nLocWire] => nLocWire=16, 7 groups of 16 wires in each layer
            {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 1
            {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 2
            {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 3
            {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 4
            {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 5
            {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 6
            //===> 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
            // (Local wire ID: 0 for 1st, 16th, 32th, 48th, 64th, 80th, 96th wires)
    };

    /*
    private final int[][] CableSwaps ={
     //[nswaps][swap]
        //CableSwaps[0]:from sector CableSwaps[1]: from layer(1...36) CableSwaps[2]: from wire
        //CableSwaps[3]:  to sector CableSwaps[4]:   to layer(1...36) CableSwaps[5]:   to wire
        {4,    18,    22,     4,    14,    25},
        {4,    13,    22,     4,    16,    25},
        {4,    15,    22,     4,    18,    25},
        {4,    17,    22,     4,    13,    25},
        {4,    14,    23,     4,    15,    25},
        {4,    16,    23,     4,    17,    25},
        {4,    18,    23,     4,    14,    26},
        {4,    13,    23,     4,    16,    26},
        {4,    15,    23,     4,    18,    26},
        {4,    17,    23,     4,    13,    26},
        {4,    14,    24,     4,    15,    26},
        {4,    16,    24,     4,    17,    26},
        {4,    18,    24,     4,    14,    27},
        {4,    13,    24,     4,    16,    27},
        {4,    15,    24,     4,    18,    27},
        {4,    17,    24,     4,    13,    27},
        {4,    14,    25,     4,    18,    22},
        {4,    16,    25,     4,    13,    22},
        {4,    18,    25,     4,    15,    22},
        {4,    13,    25,     4,    17,    22},
        {4,    15,    25,     4,    14,    23},
        {4,    17,    25,     4,    16,    23},
        {4,    14,    26,     4,    18,    23},
        {4,    16,    26,     4,    13,    23},
        {4,    18,    26,     4,    15,    23},
        {4,    13,    26,     4,    17,    23},
        {4,    15,    26,     4,    14,    24},
        {4,    17,    26,     4,    16,    24},
        {4,    14,    27,     4,    18,    24},
        {4,    16,    27,     4,    13,    24},
        {4,    18,    27,     4,    15,    24},
        {4,    13,    27,     4,    17,    24},
    };
    private int _sector;
    private int _layer;
    private int _wire;


    private void swapWires(DataEvent event, int sector, int layer, int wire) {
        // don't swap in MC
        if (event.hasBank("MC::Particle") == true || event.getBank("RUN::config").getInt("run", 0)<100) {
            return ; 
        } else {
            for(int i = 0; i<CableSwaps.length; i++) {
                if(CableSwaps[i][0]==sector && CableSwaps[i][1]==layer && CableSwaps[i][2]==wire) {
                   // LOGGER.log(Level.FINE, " swapped "+sector+", "+layer+", "+wire);
                    _sector = CableSwaps[i][3];
                    _layer  = CableSwaps[i][4];
                    _wire   = CableSwaps[i][5];
                  //  LOGGER.log(Level.FINE, "    to  "+_sector+", "+_layer+", "+_wire);
                }
            }
        }
    }
    
    public void getTriggerBits(){
        // Decoding Trigger Bits
     /*   boolean[] trigger_bits = new boolean[32];

        if (event.hasBank("RUN::config")) {
            DataBank bankAI = event.getBank("RUN::config");
            TriggerWord = bankAI.getLong("trigger",0);
            for (int i=31; i>=0; i--) {
                trigger_bits[i] = (TriggerWord & (1 << i)) != 0;
            }
        }

        for (int s=1; s<7; s++) {
           if (trigger_bits[s]) {
               LOGGER.log(Level.FINE, "Trigger bit set for electron in sector "+s);
           }
          if (trigger_bits[31])LOGGER.log(Level.FINE, "Trigger bit set from random pulser");
        }
}
    }
*/

    public List<Hit> get_DCHits(int sectorSelect) {
        if(sectorSelect==0) {
            return this._DCHits;
        } else {
            List<Hit> list = new ArrayList<Hit>();
            for (int i = 0; i < this._DCHits.size(); i++) {
                if(this._DCHits.get(i).get_Sector()==sectorSelect) {
                   list.add(this._DCHits.get(i)); 
                }
            }
            return list;
        }
    }
}
