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
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.utils.groups.IndexedTable;

/**
 * A class to fill in lists of hits corresponding to DC reconstructed hits
 * characterized by the wire, its location in the detector (superlayer, layer,
 * sector), its reconstructed time. The class also returns a MC hit, which has
 * truth-information (i.e. Left-Right ambiguity)
 *
 * @author ziegler
 *
 */
public class HitReader {

    private List<Hit> _DCHits;

    private List<FittedHit> _HBHits; //hit-based tracking hit information
    private List<FittedHit> _TBHits; //time-based tracking hit information

    /**
     *
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
    public void set_DCHits(List<Hit> _DCHits) {
        this._DCHits = _DCHits;
    }

    /**
     *
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
    public void set_HBHits(List<FittedHit> _HBHits) {
        this._HBHits = _HBHits;
    }
    
     /**
     *
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
    public void set_TBHits(List<FittedHit> _TBHits) {
        this._TBHits = _TBHits;
    }

    /**
     * reads the hits using clas-io methods to get the EvioBank for the DC and
     * fill the values to instantiate the DChit and MChit classes. This methods
     * fills the DChit list of hits.
     *
     * @param event DataEvent
     */
    public void fetch_DCHits(DataEvent event, Clas12NoiseAnalysis noiseAnalysis, NoiseReductionParameters parameters,
            Clas12NoiseResult results, double[][][][] T0, double[][][][] T0ERR, IndexedTable tab, DCGeant4Factory DcDetector) {

        if (event.hasBank("DC::tdc") == false) {
            //System.err.println("there is no dc bank ");
            _DCHits = new ArrayList<Hit>();

            return;
        }
        
        DataBank bankDGTZ = event.getBank("DC::tdc");

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
            _sector = sector[i];
            _layer = layer[i];
            _wire = wire[i];
            
            this.swapWires(event, sector[i], layer[i], wire[i]);
            sector[i] = _sector;
            layer[i] = _layer;
            wire[i] = _wire;
            
        }
        

        if (event.hasBank("DC::doca") == true) {
            DataBank bankD = event.getBank("DC::doca");
            for (int i = 0; i < bankD.rows(); i++) {
                if (bankD.getFloat("stime", i) < 0) {
                    useMChit[i] = -1;
                }
            }
        }
        int size = layer.length;
        int[] layerNum = new int[size];
        int[] superlayerNum = new int[size];
        double[] smearedTime = new double[size];

        List<Hit> hits = new ArrayList<Hit>();

        for (int i = 0; i < size; i++) {

            //if(Constants.isSimulation == false) {
            if (tdc != null && tdc.length > 0) {
                smearedTime[i] = (double) tdc[i];
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
            if (wire[i] != -1 && results.noise[i] == false && useMChit[i] != -1 && !(superlayerNum[i] == 0)) {
                double T_0 = 0;
                if (event.hasBank("MC::Particle") == false && event.getBank("RUN::config").getInt("run", 0)>100)
                    T_0 = this.get_T0(sector[i], superlayerNum[i], layerNum[i], wire[i], T0, T0ERR)[0];
                double T0Sub = smearedTime[i] - T_0; 
                //double TMax = CCDBConstants.getTMAXSUPERLAYER()[sector[i]-1][superlayerNum[i]-1];
 //               double TMax = tab.getDoubleValue("tmax", sector[i], superlayerNum[i] ,0);
                boolean passTimingCut = false;
                int region = (int) (superlayerNum[i] + 1) / 2;
                if(region ==1 && T0Sub>-25 && T0Sub<275)
                    passTimingCut=true;
                if(region ==2) {
                    if(wire[i]>=56) {
                        if(T0Sub>-25 && T0Sub<350+200*(float)(112-wire[i]/56))
                            passTimingCut=true;
                    } else {
                        if(T0Sub>-25 && T0Sub<550+400*(float)(56-wire[i]/56))
                            passTimingCut=true;
                    }
                        
                    passTimingCut=true;
                }
                if(region ==3 && T0Sub>-25 && T0Sub<750)
                    passTimingCut=true;
                if(passTimingCut) { // cut on spurious hits
                    //Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], smearedTime[i], 0, 0, hitno[i]);			
                    Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], smearedTime[i], 0, 0, (i + 1));
                    hit.set_CellSize(DcDetector);
                    double posError = hit.get_CellSize() / Math.sqrt(12.);
                    hit.set_DocaErr(posError);
                    hit.set_Id(i + 1);
                    hits.add(hit); 
                }
            }
        }

        this.set_DCHits(hits);

    }

    /**
     * Reads HB DC hits written to the DC bank
     *
     * @param event
     */
    public void read_HBHits(DataEvent event, IndexedTable constants0, IndexedTable constants1, double[][][][] T0, double[][][][] T0ERR, DCGeant4Factory DcDetector, TimeToDistanceEstimator tde ) {
        /*
        0: this.getConstantsManager().getConstants(newRun, "/calibration/dc/signal_generation/doca_resolution"),
        1: this.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/t2d")
        */
        if (event.hasBank("HitBasedTrkg::HBHits") == false) {
            //System.err.println("there is no HB dc bank ");
            _HBHits = new ArrayList<FittedHit>();
            return;
        }

        DataBank bank = event.getBank("HitBasedTrkg::HBHits");
        int rows = bank.rows();

        int[] id = new int[rows];
        int[] sector = new int[rows];
        int[] slayer = new int[rows];
        int[] layer = new int[rows];
        int[] wire = new int[rows];
        double[] time = new double[rows];
        int[] LR = new int[rows];
        double[] B = new double[rows];
        int[] clusterID = new int[rows];
        int[] trkID = new int[rows];
        double[] tProp = new double[rows];
        double[] tFlight = new double[rows];
        double[] trkDoca = new double[rows];
        
        for (int i = 0; i < rows; i++) {
            sector[i] = bank.getByte("sector", i);
            slayer[i] = bank.getByte("superlayer", i);
            layer[i] = bank.getByte("layer", i);
            wire[i] = bank.getShort("wire", i);
            time[i] = bank.getFloat("time", i);
            id[i] = bank.getShort("id", i);
            LR[i] = bank.getByte("LR", i);
            B[i] = bank.getFloat("B", i);
            trkDoca[i] = bank.getFloat("trkDoca", i);
            clusterID[i] = bank.getShort("clusterID", i);
            trkID[i] = bank.getByte("trkID", i);
            tProp[i] = bank.getFloat("TProp", i);
            tFlight[i] = bank.getFloat("TFlight", i); 
            if (event.hasBank("MC::Particle") == true || event.getBank("RUN::config").getInt("run", 0)<100) {
                tProp[i] = 0;
                tFlight[i] = 0; 
            }
        }

        int size = layer.length;

        List<FittedHit> hits = new ArrayList<FittedHit>();
        for (int i = 0; i < size; i++) {
            //use only hits that have been fit to a track
            if (trkID[i] == -1) {
                continue;
            }
            
            double T_0 = 0;
            if (event.hasBank("MC::Particle") == false && event.getBank("RUN::config").getInt("run", 0)>100)
                T_0 = this.get_T0(sector[i], slayer[i], layer[i], wire[i], T0, T0ERR)[0];
            
            //FittedHit hit = new FittedHit(sector[i], slayer[i], layer[i], wire[i], time[i]-tProp[i]-tFlight[i] - this.get_T0(sector[i], slayer[i], layer[i], wire[i], Constants.getT0())[0], 0, B[i], id[i]);
            FittedHit hit = new FittedHit(sector[i], slayer[i], layer[i], wire[i], time[i]-tProp[i]-tFlight[i] - T_0, 
                    0, B[i], id[i]);          
            hit.set_B(B[i]);
            //hit.setT0SubTime(time[i]- T_0+tProp[i]+tFlight[i]);
            hit.setT0SubTime(time[i]- T_0 - tProp[i] - tFlight[i]); // Temporary fix for triple bands
            hit.setTProp(tProp[i]);
            hit.setTFlight(tFlight[i]);
            hit.set_LeftRightAmb(LR[i]);
            hit.set_TrkgStatus(0);
            hit.set_CellSize( DcDetector) ;
            hit.set_ClusFitDoca(trkDoca[i]);
            hit.set_TimeToDistance(1.0, B[i], constants1, tde);
            
            hit.set_QualityFac(0);
            //hit.set_Doca(hit.get_TimeToDistance());
            //if (hit.get_Doca() > hit.get_CellSize() || hit.get_Time()>CCDBConstants.getTMAXSUPERLAYER()[hit.get_Sector()-1][hit.get_Superlayer()-1] ) {
            if (hit.get_Doca() > hit.get_CellSize() || hit.get_Time() > constants1.getDoubleValue("tmax", hit.get_Sector(), hit.get_Superlayer(),0) ) {
                //this.fix_TimeToDistance(this.get_CellSize());
                hit.set_OutOfTimeFlag(true);
                hit.set_QualityFac(2);
            } 
            if(hit.get_Time()<0)
                hit.set_QualityFac(1);
            
            hit.set_DocaErr(hit.get_PosErr(B[i], constants0, constants1, tde));            
            hit.set_AssociatedClusterID(clusterID[i]);
            hit.set_AssociatedHBTrackID(trkID[i]); 
            hits.add(hit);
            //System.out.println(" using hit for TBT "+hit.printInfo());
        }

        this.set_HBHits(hits);
    }
    public void read_TBHits(DataEvent event, IndexedTable constants0, IndexedTable constants1, TimeToDistanceEstimator tde) {
        /*
        0: this.getConstantsManager().getConstants(newRun, "/calibration/dc/signal_generation/doca_resolution"),
        1: this.getConstantsManager().getConstants(newRun, "/calibration/dc/time_to_distance/t2d")
        */
        if (event.hasBank("TimeBasedTrkg::TBHits") == false) {
            //System.err.println("there is no HB dc bank ");
            _TBHits = new ArrayList<FittedHit>();
            return;
        }

        DataBank bank = event.getBank("TimeBasedTrkg::TBHits");
        int rows = bank.rows();

        int[] id = new int[rows];
        int[] sector = new int[rows];
        int[] slayer = new int[rows];
        int[] layer = new int[rows];
        int[] wire = new int[rows];
        double[] time = new double[rows];
        int[] LR = new int[rows];
        double[] B = new double[rows];
        int[] clusterID = new int[rows];
        int[] trkID = new int[rows];
        double[] tProp = new double[rows];
        double[] tFlight = new double[rows];
        

        for (int i = 0; i < rows; i++) {
            sector[i] = bank.getByte("sector", i);
            slayer[i] = bank.getByte("superlayer", i);
            layer[i] = bank.getByte("layer", i);
            wire[i] = bank.getShort("wire", i);
            time[i] = bank.getFloat("time", i);
            id[i] = bank.getShort("id", i);
            LR[i] = bank.getByte("LR", i);
            B[i] = bank.getFloat("B", i);
            clusterID[i] = bank.getShort("clusterID", i);
            trkID[i] = bank.getByte("trkID", i);
            tProp[i] = bank.getFloat("TProp", i);
            tFlight[i] = bank.getFloat("TFlight", i);
        
            if (event.hasBank("MC::Particle") == true || event.getBank("RUN::config").getInt("run", 0)<100) {
                    tProp[i] = 0;
                    tFlight[i] = 0; 
            }
        }
        int size = layer.length;

        List<FittedHit> hits = new ArrayList<FittedHit>();
        for (int i = 0; i < size; i++) {
            //use only hits that have been fit to a track
            if (trkID[i] == -1) {
                continue;
            }
            
            FittedHit hit = new FittedHit(sector[i], slayer[i], layer[i], wire[i], time[i]-tProp[i]-tFlight[i], 0, B[i], id[i]);
            //hit.setT0SubTime(time[i]+tProp[i]+tFlight[i]); 
            hit.setT0SubTime(time[i] - tProp[i] - tFlight[i]); // Temporary fix for triple bands 
            hit.setTFlight(tFlight[i]);
            hit.setTProp(tProp[i]);
            hit.set_B(B[i]);
// System.out.println("getting the hit time: tdc "+time[i]+" "+Constants.getT0()+" b "+B[i]+" t0 "+this.get_T0(sector[i], slayer[i], layer[i], wire[i], Constants.getT0())[0]);
            hit.set_LeftRightAmb(LR[i]);
            hit.set_TrkgStatus(0);
            
            
            hit.set_DocaErr(hit.get_PosErr(B[i], constants0, constants1, tde));            
            hit.set_AssociatedClusterID(clusterID[i]);
            hit.set_AssociatedTBTrackID(trkID[i]); 
            hit.set_Beta(this.readBeta(event, trkID[i])); 
            //reset the time based on new beta
            double newtFlight = tFlight[i]/hit.get_Beta();
            double newTime = time[i]-tProp[i] - newtFlight;
            hit.set_Time(newTime);
            
            hit.set_TimeToDistance(1.0, B[i], constants1, tde);
            
            hit.set_QualityFac(0);
            //hit.set_Doca(hit.get_TimeToDistance());
            //if (hit.get_Doca() > hit.get_CellSize() || hit.get_Time()>CCDBConstants.getTMAXSUPERLAYER()[hit.get_Sector()-1][hit.get_Superlayer()-1]) {
            if (hit.get_Doca() > hit.get_CellSize() || hit.get_Time()>constants1.getDoubleValue("tmax", hit.get_Sector(), hit.get_Superlayer(),0) ) {   
                //this.fix_TimeToDistance(this.get_CellSize());
                hit.set_OutOfTimeFlag(true);
                hit.set_QualityFac(2);
            } 
            if(hit.get_Time()<0)
                hit.set_QualityFac(1);
            
            hits.add(hit);
            
        }

        this.set_TBHits(hits);
    }

   
    private double[] betaArray = new double[3];
    public double readBeta(DataEvent event, int trkId) {
        double _beta =1.0;
        betaArray[0]=-1;
        betaArray[1]=-1;
        betaArray[2]=-1;
        if (event.hasBank("RECHB::Event") == false) 
            return 1.0;
        DataBank bank = event.getBank("RECHB::Event");
        double startTime = bank.getFloat("STTime", 0);
        
        if (event.hasBank("FTOF::hits") == false) 
            return 1.0;
        
        DataBank bankftof = event.getBank("FTOF::hits");
        int rows = bank.rows();
        for (int i = 0; i < rows; i++) {
            if(bankftof.getShort("trackid", i)==trkId) {
                betaArray[bankftof.getByte("layer", i)-1]= bankftof.getFloat("pathLength", i)/(bankftof.getFloat("time", i)-startTime)/30.0 ;
            }
        }
        if(betaArray[0]==-1 && betaArray[1]==-1 && betaArray[2]!=-1)
            _beta = betaArray[2];
        if(betaArray[0]!=-1 && betaArray[1]==-1)
            _beta = betaArray[0];
        if(betaArray[1]!=-1)
            _beta = betaArray[1];
        if(_beta<0.)
            _beta=0.01;
        if(_beta>1.)
            _beta=1;
        return _beta;
    }
    

    private double[] get_T0(int sector, int superlayer, int layer, int wire, double[][][][] T0, double[][][][] T0ERR) {
        double[] T0Corr = new double[2];

        int cable = this.getCableID1to6(layer, wire);
        int slot = this.getSlotID1to7(wire);
        
        double t0 = T0[sector - 1][superlayer - 1][slot - 1][cable - 1];      //nSec*nSL*nSlots*nCables
        double t0E = T0ERR[sector - 1][superlayer - 1][slot - 1][cable - 1];
        
        T0Corr[0] = t0;
        T0Corr[1] = t0E;
        
        return T0Corr;
    }

    private int getSlotID1to7(int wire1to112) {
        int iSlot = (int) ((wire1to112 - 1) / 16) + 1;
        return iSlot;
    }

    private int getCableID1to6(int layer1to6, int wire1to112) {
        /*96 channels are grouped into 6 groups of 16 channels and each group 
            joins with a connector & a corresponding cable (with IDs 1,2,3,4,& 6)*/
        int wire1to16 = (int) ((wire1to112 - 1) % 16 + 1);
        int cable_id = this.CableID[layer1to6 - 1][wire1to16 - 1];
        return cable_id;
    }
    //Map of Cable ID (1, .., 6) in terms of Layer number (1, ..., 6) and localWire# (1, ..., 16)
    private final int[][] CableID = { //[nLayer][nLocWire] => nLocWire=16, 7 groups of 16 wires in each layer
        {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 1
        {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 2
        {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 3
        {1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6}, //Layer 4
        {1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6}, //Layer 5
        {1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6}, //Layer 6  
    //===> 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 (Local wire ID: 0 for 1st, 16th, 32th, 48th, 64th, 80th, 96th wires)
    };
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
                   // System.out.println(" swapped "+sector+", "+layer+", "+wire);
                    _sector = CableSwaps[i][3];
                    _layer  = CableSwaps[i][4];
                    _wire   = CableSwaps[i][5];
                  //  System.out.println("    to  "+_sector+", "+_layer+", "+_wire);
                }
            }
        }
    }
}
