package org.jlab.rec.dc.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.CCDBConstants;
import org.jlab.rec.dc.CalibrationConstantsLoader;
import org.jlab.rec.dc.Constants;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

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
     * reads the hits using clas-io methods to get the EvioBank for the DC and
     * fill the values to instantiate the DChit and MChit classes. This methods
     * fills the DChit list of hits.
     *
     * @param event DataEvent
     */
    public void fetch_DCHits(DataEvent event, Clas12NoiseAnalysis noiseAnalysis, NoiseReductionParameters parameters,
            Clas12NoiseResult results) {

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
                //Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], smearedTime[i], 0, 0, hitno[i]);			
                Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], smearedTime[i], 0, 0, (i + 1));
                double posError = hit.get_CellSize() / Math.sqrt(12.);
                hit.set_DocaErr(posError);
                hit.set_Id(i + 1);
                hits.add(hit);
            }
        }

        this.set_DCHits(hits);

    }

    /**
     * Reads HB DC hits written to the DC bank
     *
     * @param event
     */
    public void read_HBHits(DataEvent event) {

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
        }

        int size = layer.length;

        List<FittedHit> hits = new ArrayList<FittedHit>();
        for (int i = 0; i < size; i++) {
            //use only hits that have been fit to a track
            if (clusterID[i] == -1) {
                continue;
            }

            FittedHit hit = new FittedHit(sector[i], slayer[i], layer[i], wire[i], time[i] - this.get_T0(sector[i], slayer[i], layer[i], wire[i], Constants.getT0())[0], 0, B[i], id[i]);
            hit.set_B(B[i]);
// System.out.println("getting the hit time: tdc "+time[i]+" "+Constants.getT0()+" b "+B[i]+" t0 "+this.get_T0(sector[i], slayer[i], layer[i], wire[i], Constants.getT0())[0]);
            hit.set_LeftRightAmb(LR[i]);
            hit.set_TrkgStatus(0);
            hit.set_TimeToDistance(1.0, B[i]);
            hit.set_QualityFac(0);
            //hit.set_Doca(hit.get_TimeToDistance());
            if (hit.get_Doca() > hit.get_CellSize() || hit.get_Time()>CCDBConstants.getTMAXSUPERLAYER()[hit.get_Sector()-1][hit.get_Superlayer()-1]) {
                //this.fix_TimeToDistance(this.get_CellSize());
                hit.set_OutOfTimeFlag(true);
                hit.set_QualityFac(2);
            } 
            if(hit.get_Time()<0)
                hit.set_QualityFac(1);
            
            hit.set_DocaErr(hit.get_PosErr(B[i]));            
            hit.set_AssociatedClusterID(clusterID[i]);
            hit.set_AssociatedHBTrackID(trkID[i]); 
            hit.set_Beta(this.readBeta(event, trkID[i])); 
            hits.add(hit);
            
        }

        this.set_HBHits(hits);
    }

   
    private double[] betaArray = new double[3];
    public double readBeta(DataEvent event, int trkId) {
        double _beta =1.0;
        betaArray[0]=-1;
        betaArray[1]=-1;
        betaArray[2]=-1;
        if (event.hasBank("RUN::config") == false) 
            return 1.0;
        DataBank bank = event.getBank("RUN::config");
        double startTime = bank.getFloat("startTime", 0);
        
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
        
        return _beta;
    }
    

    private double[] get_T0(int sector, int superlayer, int layer, int wire, boolean applyCorr) {
        double[] T0Corr = new double[2];

        if (applyCorr == false) {
            T0Corr[0] = 0;
            T0Corr[1] = 0;

        } else {

            double t0 = 0;
            double t0E = 0;
            int cable = this.getCableID1to6(layer, wire);
            int slot = this.getSlotID1to7(wire);

            t0 = CCDBConstants.getT0()[sector - 1][superlayer - 1][slot - 1][cable - 1];      //nSec*nSL*nSlots*nCables
            t0E = CCDBConstants.getT0ERR()[sector - 1][superlayer - 1][slot - 1][cable - 1];

            T0Corr[0] = t0;
            T0Corr[1] = t0E;
        }
        //System.out.println(" t0 correction "+T0Corr[0]);
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
        int cable_id = CalibrationConstantsLoader.CableID[layer1to6 - 1][wire1to16 - 1];
        return cable_id;
    }

}
