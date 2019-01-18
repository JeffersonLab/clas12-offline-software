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

import java.lang.String;
import java.lang.Double;
import java.lang.Integer;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import org.jlab.clas.physics.LorentzVector;

import org.jlab.rec.band.cluster.BANDCluster;
import org.jlab.rec.band.cluster.BANDClusterFinder;

/**
 * Service to return reconstructed BAND Hits - the output is in Hipo format
 * doing clustering job at the end, provide the cluster infos for PID ("rwangcn8@gmail.com")
 *
 *
 */

public class BANDCalibrationEngine extends ReconstructionEngine {


	public BANDCalibrationEngine() {
		super("BAND", "hauenstein & segarra", "1.0");
	
	}

	int Run = -1;
	RecoBankWriter rbc;
	//test
	static int enb =0;
	static int eband=0;
	static int hcvt=0;
	static int match=0;
	static int posmatch=0;
	static int ctof=0;
	static int ctoftot=0;

	@Override
	public boolean processDataEvent(DataEvent event) {

		//event.show();
		//System.out.println("in data process ");
            
		// update calibration constants based on run number if changed
		setRunConditionsParameters(event);


                ArrayList<HalfHit> halfhits = new ArrayList<HalfHit>();   
		ArrayList<BandHit> hits = new ArrayList<BandHit>();

		//test
//		if(event.hasBank("CVTRec::Tracks")){
//			hcvt++;
//		}

		halfhits = HitReader.getBandHalfHits(event);		
		//1) exit if halfhit list is empty
		if(halfhits.size()==0 ){
			//			System.out.println("fin de process (0) : ");
			//			event.show();
			return true;
		}

		//2) find the BAND hits from these half-hits
		BandHitFinder hitFinder = new BandHitFinder();
		hits = hitFinder.findHits(halfhits,0);

		CvtGetHTrack cvttry = new CvtGetHTrack();
		cvttry.getCvtHTrack(event); // get the list of helix associated with the event

		//int flag=0;
		for (BandHit hit : hits){ // findlength for charged particles
			double length =hitFinder.findLength(hit, cvttry.getHelices(),0);
			if (length!=0){
				hit.set_tLength(length); // the path length is non zero only when there is a match with cvt track
				//if(flag==0){match++;}
				//flag=1;
			}

		}

		//	   			GetVertex getVertex = new GetVertex();
		//	   			Point3D vertex = getVertex.getVertex(event);
		//	   			for (bandHit hit : hits){ // check findlengthneutral
		//	   				hitFinder.findLengthNeutral( vertex, hit);
		//		   			}
		//	   			

		//		if(hits.size()!=0){
		//
		//			DataBank outbank = RecoBankWriter.fillbandHitBanks(event, hits);
		////			System.out.println("event before process : ");
		////			event.show();
		//			event.appendBanks(outbank);
		//			//System.out.println("event after process : ");
		//			//event.show();
		//			eband++;
		//			if(event.hasBank("CVTRec::Tracks")){
		//				posmatch++;
		//				//event.getBank("MC::Particle").show();
		//				//outbank.show();
		//			}
		//			
		//		}
		////		System.out.println("fin de process : ");
		////		event.show();
		//		return true;
		//	}
		if(hits.size()!=0){

			//          DataBank outbank = RecoBankWriter.fillbandHitBanks(event, hits);
			//          event.appendBanks(outbank);
			// event.show();
		//	System.out.println("in process event ");
			rbc.appendBANDBanks(event,hits);
			//      eband++;
			//      if(event.hasBank("CVTRec::Tracks")){
			//              posmatch++;
			//event.getBank("MC::Particle").show();
			//outbank.show();
			//      }
		//	event.show();

		}



		//// clustering of the BAND hits
		BANDClusterFinder bandclusterFinder = new BANDClusterFinder();
		ArrayList<BANDCluster> bandclusters = bandclusterFinder.findClusters(hits);
	        

	        /// Filling the banks of band clusters
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
		rbc = new RecoBankWriter();
		System.out.println("in init ");
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
		BANDCalibrationEngine en = new BANDCalibrationEngine();

		en.init();
		
		String input = "/projet/nucleon/hauenst/Recband/clas_002227.evio.18.hipo";
		//String input = "/projet/nucleon/pierre/Recband/test.hipo";
		//String input = "/projet/nucleon/silvia/CLARA/out_clasdispr_small.00849.hipo";
		HipoDataSource  reader = new HipoDataSource();
		reader.open(input);
		String outputFile="/projet/nucleon/hauenst/Recband/test1.hipo";
		HipoDataSync  writer = new HipoDataSync();
		writer.open(outputFile);


		while(reader.hasEvent()) {
			enb++;		
			DataEvent event = (DataEvent) reader.getNextEvent();
			//event.show();
			//System.out.println("event nb "+enb);

			//			System.out.println("event avant process ");
			//			event.show();

			//event.getBank("MC::Particle").show();
			//if(event.hasBank("CVTRec::Tracks")){event.getBank("CVTRec::Tracks").show();};
			en.processDataEvent(event);

			//			System.out.println("event après process ");
			//			event.show();

			//System.out.println("avant write ");
			writer.writeEvent(event);
			//System.out.println("après write ");

//				if(event.hasBank("band::hits")){
//							//event.show();
//				System.out.println("event nb "+enb);
//				event.getBank("band::hits").show();	
//			event.getBank("band::adc").show();	
//			event.getBank("band::tdc").show();	
//				}



			if(enb==30) break;

		}		
		writer.close();

		//some statitics on cvt/band matching
		System.out.println("enb "+enb);
		System.out.println("eband "+eband);
		System.out.println("hcvt "+hcvt);
		System.out.println("posmatch "+posmatch);
		System.out.println("match "+match);
		System.out.println("%match band "+100.*match/posmatch);
		System.out.println("Done");


		HipoDataSource  sortie = new HipoDataSource();
		sortie.open(outputFile);

		
		while(sortie.hasEvent()) {

			DataEvent event = (DataEvent) sortie.getNextEvent();
			//event.show();

		}		
	}

}


