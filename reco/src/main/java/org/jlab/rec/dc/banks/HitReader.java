package org.jlab.rec.dc.banks;

import java.util.ArrayList;
import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.Constants;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

/**
 * A class to fill in lists of hits  corresponding to DC reconstructed hits characterized by the wire, its location in the detector (superlayer,
 * layer, sector), its reconstructed time.  The class also returns a MC hit, which has truth-information (i.e. Left-Right ambiguity)
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
	 *  sets the list of DC hits
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
	 * @param _HBHits list of DC hits 
	 */
	public void set_HBHits(List<FittedHit> _HBHits) {
		this._HBHits = _HBHits;
	}
	/**
	 * reads the hits using clas-io methods to get the EvioBank for the DC and fill the values to instantiate the DChit and MChit classes.
	 * This methods fills the DChit list of hits.  
	 * @param event DataEvent
	 */
	public void fetch_DCHits(DataEvent event, Clas12NoiseAnalysis noiseAnalysis,NoiseReductionParameters parameters,
			Clas12NoiseResult results) {
		
		if(event.hasBank("DC::dgtz")==false) {
			//System.err.println("there is no dc bank ");
			_DCHits= new ArrayList<Hit>();
			
			return;
		}
 
		DataBank bankDGTZ = event.getBank("DC::tdc");
		
		int rows = bankDGTZ.rows();
		int[] sector = new int[rows];
		int[] layer = new int[rows];
		int[] wire = new int[rows];
		int[] tdc = new int[rows];
		
		for(int i = 0; i< rows; i++) {
			sector[i] = bankDGTZ.getByte("sector", i);
			layer[i] = bankDGTZ.getByte("layer", i);
			wire[i] = bankDGTZ.getShort("component", i);
			tdc[i] = bankDGTZ.getInt("TDC", i);		
		}
		
		
		int size = layer.length;
		int[] layerNum = new int[size];
		int[] superlayerNum =new int[size];
		double[] smearedTime = new double[size];
		
		List<Hit> hits = new ArrayList<Hit>();
		
		for(int i = 0; i<size; i++) {
			
			//if(Constants.isSimulation == false) {
			if(tdc!=null && tdc.length>0) {
					smearedTime[i] = (double) tdc[i];
			} 
			//if(stime!=null && stime.length>0) {
			//	smearedTime[i] = stime[i];
			//}
			
			//if(slayer!=null && slayer.length>0) {
			//	layerNum[i] = layer[i];
			//	superlayerNum[i] = slayer[i]; 
			//} else {
				superlayerNum[i]=(layer[i]-1)/6 + 1;
				layerNum[i] = layer[i] - (superlayerNum[i] - 1)*6; 
			//}
	
		}
		results.clear();
		noiseAnalysis.clear();
		
		noiseAnalysis.findNoise(sector, superlayerNum, layerNum, wire, results);
		
		for(int i = 0; i<size; i++) {	
			if(wire[i]!=-1 && results.noise[i]==false && smearedTime[i]>=0.0){		
				//Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], smearedTime[i], 0, 0, hitno[i]);			
				Hit hit = new Hit(sector[i], superlayerNum[i], layerNum[i], wire[i], smearedTime[i], 0, 0, (i+1));			
				double posError = hit.get_CellSize()/Math.sqrt(12.);
				hit.set_DocaErr(posError);
				hit.set_Id(i+1); 
				hits.add(hit); 
			}	
		}
			
			this.set_DCHits(hits);

		}
		

	/**
	 * Reads HB DC hits written to the DC bank
	 * @param event
	 */

	public void read_HBHits(DataEvent event) {
		
		if(event.hasBank("HitBasedTrkg::HBHits")==false) {
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
		
		
		for(int i = 0; i< rows; i++) {
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
		for(int i = 0; i<size; i++) {
			//use only hits that have been fit to a track
			if(clusterID[i]==-1)
				continue;
			
			FittedHit hit = new FittedHit(sector[i], slayer[i], layer[i], wire[i], time[i]-Constants.T0, 0, B[i], id[i]); 
			hit.set_LeftRightAmb(LR[i]);
			hit.set_TrkgStatus(0);
			hit.set_TimeToDistance(1.0, B[i]);
			hit.set_Doca(hit.get_TimeToDistance()); 
			if(hit.get_Doca()>hit.get_CellSize()) {
				//this.fix_TimeToDistance(this.get_CellSize());
				hit.set_OutOfTimeFlag(true); 
			}
			hit.set_DocaErr(hit.get_PosErr(B[i]));
			hit.set_AssociatedClusterID(clusterID[i]);
			hit.set_AssociatedHBTrackID(trkID[i]);
			hits.add(hit);
			
		}
		
		
		
		this.set_HBHits(hits);
	}
	

   
}
