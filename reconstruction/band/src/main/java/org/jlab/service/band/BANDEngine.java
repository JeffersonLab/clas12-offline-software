package org.jlab.service.band;


import java.io.IOException;
import java.util.ArrayList;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.band.constants.CalibrationConstantsLoader;
import org.jlab.rec.band.banks.HitReader;
import org.jlab.rec.band.banks.RecoBankWriter;
import org.jlab.rec.band.hit.BandHit;
import org.jlab.rec.band.hit.CvtGetHTrack;
import org.jlab.rec.band.hit.HalfHit;
import org.jlab.rec.band.hit.BandHitFinder;

import org.jlab.rec.band.cluster.BANDCluster;
import org.jlab.rec.band.cluster.BANDClusterFinder;

/**
 * Service to return reconstructed BAND Hits - the output is in Hipo format
 * 
 *
 */

public class BANDEngine extends ReconstructionEngine {


	public BANDEngine() {
		super("BAND", "hauenstein", "1.0");
	}

	int Run = -1;
	RecoBankWriter rbc;
	//test
	static int enb =0;
	static int eband=0;
	static int hcvt=0;
	static int match=0;
	static int posmatch=0;

	@Override
	public boolean processDataEvent(DataEvent event) {
		// update calibration constants based on run number if changed
		setRunConditionsParameters(event);
		
		ArrayList<HalfHit> halfhits = new ArrayList<HalfHit>();   
		ArrayList<BandHit> hits = new ArrayList<BandHit>();
	    
		//test
		if(event.hasBank("CVTRec::Tracks")){
			hcvt++;
		}

		halfhits = HitReader.getBandHalfHits(event);		
		//1) exit if halfhit list is empty
		if(halfhits.size()==0 )
			return true;

		//2) find the BAND hits from these half-hits
		BandHitFinder hitFinder = new BandHitFinder();
		hits = hitFinder.findHits(halfhits,1);

		CvtGetHTrack cvttry = new CvtGetHTrack();
		cvttry.getCvtHTrack(event); // get the list of helix associated with the event
		
		int flag=0;
		for (BandHit hit : hits){ // findlength for charged particles
			double length =hitFinder.findLength(hit, cvttry.getHelices(),1);
			if (length!=0){
				hit.set_tLength(length); // the path length is non zero only when there is a match with cvt track
				if(flag==0){match++;}
				flag=1;
			}
			
		}

		//	   			GetVertex getVertex = new GetVertex();
		//	   			Point3D vertex = getVertex.getVertex(event);
		//	   			for (bandHit hit : hits){ // check findlengthneutral
		//	   				hitFinder.findLengthNeutral( vertex, hit);
		//		   			}
		//	   			

		if(hits.size()!=0){

				//          DataBank outbank = RecoBankWriter.fillbandHitBanks(event, hits);
			//          event.appendBanks(outbank);
			// event.show();
			System.out.println("in process event ");
			rbc.appendBANDBanks(event,hits);
			//      eband++;
			//      if(event.hasBank("CVTRec::Tracks")){
			//              posmatch++;
			//event.getBank("MC::Particle").show();
			//outbank.show();
			//      }
		}

		//// clustering of the BAND hits
		BANDClusterFinder bandclusterFinder = new BANDClusterFinder();
		ArrayList<BANDCluster> bandclusters = bandclusterFinder.findClusters(hits);

	        /// Filling the banks of BAND clusters
	        int size = bandclusters.size();
	        if(size>0){
	                DataBank bank2 =  event.createBank("BAND::clusters", size);
	                if (bank2 == null) {
	                        System.err.println("COULD NOT CREATE A BAND::clusters BANK!!!!!!");
	                        return false;
	                }
	                for(int i =0; i< size; i++) {
	                        bank2.setInt("id",i, bandclusters.get(i).get_id() );
	                        bank2.setInt("nhits",i, bandclusters.get(i).get_nhits() );
				bank2.setByte("sector",i,  (byte)(1* bandclusters.get(i).get_sector()) );
				bank2.setByte("layer",i,  (byte)(1*  bandclusters.get(i).get_layer()) );
				bank2.setInt("component",i,  bandclusters.get(i).get_component() );
	                        bank2.setFloat("energy",i,   (float)(1.0* bandclusters.get(i).get_energysum()) );
	                        bank2.setFloat("x",i,   (float)(1.0* bandclusters.get(i).get_x()) );
	                        bank2.setFloat("y",i,   (float)(1.0* bandclusters.get(i).get_y()) );
	                        bank2.setFloat("z",i,   (float)(1.0* bandclusters.get(i).get_z()) );
	                        bank2.setFloat("time",i,   (float)(1.0*  bandclusters.get(i).get_time()) );
				bank2.setInt("status",i,   bandclusters.get(i).get_status());
	                }
	                event.appendBanks(bank2);
	        }


		return true;
	}

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return true;
	}

	public void setRunConditionsParameters(DataEvent event) {
		if(event.hasBank("RUN::config")==false) {
			System.err.println("RUN CONDITIONS NOT READ!");
		}
		else {
			int newRun = Run;        

			DataBank bank = event.getBank("RUN::config");
			newRun = bank.getInt("run", 0);  
			// Load the constants
			//-------------------
			if(Run!=newRun) {
				CalibrationConstantsLoader.Load(newRun,"default"); 
				Run = newRun;
			}
		}

	}

	public static void main (String arg[]) {
		BANDEngine en = new BANDEngine();
		en.init();
		//String input = "/Users/ziegler/Workdir/Files/GEMC/ForwardTracks/pi-.r100.evio";
		
		String input = "/band_2052_2053.hipo";
		
		HipoDataSource  reader = new HipoDataSource();
		reader.open(input);
		String outputFile="/projet/nucleon/hauenst/band_run2052_2053/test.hipo";
		HipoDataSync  writer = new HipoDataSync();
		writer.open(outputFile);

		
		while(reader.hasEvent()) {
			enb++;		
			DataEvent event = (DataEvent) reader.getNextEvent();
			
			//event.show();
			if (event.hasBank("band::adc") && event.hasBank("band::tdc")){
			event.getBank("band::adc").show();
			event.getBank("band::tdc").show();
			}
			en.processDataEvent(event);
			writer.writeEvent(event);
			//event.getBank("band::hits").show();
			System.out.println("event nb "+enb);
			//event.getBank("band::hits").show();
			//System.out.println();
			if(enb>1000)
				break;
		}		
		writer.close();
		
		System.out.println("enb "+enb);
		System.out.println("eband "+eband);
		System.out.println("hcvt "+hcvt);
		System.out.println("posmatch "+posmatch);
		System.out.println("match "+match);
		System.out.println("%match "+100.*match/posmatch);
		System.out.println("Done");
	}

}
