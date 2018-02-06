package org.jlab.service.cnd;

import java.io.IOException;
import java.util.ArrayList;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.cnd.constants.CalibrationConstantsLoader;
import org.jlab.rec.cnd.banks.HitReader;
import org.jlab.rec.cnd.banks.RecoBankWriter;
import org.jlab.rec.cnd.hit.CndHit;
import org.jlab.rec.cnd.hit.CvtGetHTrack;
import org.jlab.rec.cnd.hit.HalfHit;
import org.jlab.rec.cnd.hit.CndHitFinder;
/**
 * Service to return reconstructed CND Hits - the output is in Hipo format
 * 
 *
 */

public class CNDCalibrationEngine extends ReconstructionEngine {


	public CNDCalibrationEngine() {
		super("CND", "chatagnon", "1.0");
	
	}

	int Run = -1;
	RecoBankWriter rbc;
	//test
	static int enb =0;
	static int ecnd=0;
	static int hcvt=0;
	static int match=0;
	static int posmatch=0;
	static int ctof=0;
	static int ctoftot=0;

	@Override
	public boolean processDataEvent(DataEvent event) {
		//event.show();
		//System.out.println("in data process ");
		ArrayList<HalfHit> halfhits = new ArrayList<HalfHit>();   
		ArrayList<CndHit> hits = new ArrayList<CndHit>();

		//test
//		if(event.hasBank("CVTRec::Tracks")){
//			hcvt++;
//		}

		halfhits = HitReader.getCndHalfHits(event);		
		//1) exit if halfhit list is empty
		if(halfhits.size()==0 ){
			//			System.out.println("fin de process (0) : ");
			//			event.show();
			return true;
		}
		// update calibration constants based on run number if changed
		setRunConditionsParameters(event);

		//2) find the CND hits from these half-hits
		CndHitFinder hitFinder = new CndHitFinder();
		hits = hitFinder.findHits(halfhits,0);

		CvtGetHTrack cvttry = new CvtGetHTrack();
		cvttry.getCvtHTrack(event); // get the list of helix associated with the event

		//int flag=0;
		for (CndHit hit : hits){ // findlength for charged particles
			double length =hitFinder.findLength(hit, cvttry.getHelices(),0);
			if (length!=0){
				hit.set_tLength(length); // the path length is non zero only when there is a match with cvt track
				//if(flag==0){match++;}
				//flag=1;
			}

		}

		//	   			GetVertex getVertex = new GetVertex();
		//	   			Point3D vertex = getVertex.getVertex(event);
		//	   			for (CndHit hit : hits){ // check findlengthneutral
		//	   				hitFinder.findLengthNeutral( vertex, hit);
		//		   			}
		//	   			

		//		if(hits.size()!=0){
		//
		//			DataBank outbank = RecoBankWriter.fillCndHitBanks(event, hits);
		////			System.out.println("event before process : ");
		////			event.show();
		//			event.appendBanks(outbank);
		//			//System.out.println("event after process : ");
		//			//event.show();
		//			ecnd++;
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

			//          DataBank outbank = RecoBankWriter.fillCndHitBanks(event, hits);
			//          event.appendBanks(outbank);
			// event.show();
		//	System.out.println("in process event ");
			rbc.appendCNDBanks(event,hits);
			//      ecnd++;
			//      if(event.hasBank("CVTRec::Tracks")){
			//              posmatch++;
			//event.getBank("MC::Particle").show();
			//outbank.show();
			//      }
		//	event.show();

		}		return true;
		
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
		CNDCalibrationEngine en = new CNDCalibrationEngine();

		en.init();
		//String input = "/Users/ziegler/Workdir/Files/GEMC/ForwardTracks/pi-.r100.evio";
		//String input = "/projet/nucleon/silvia/test.hipo";
		//String input = "/projet/nucleon/silvia/ctof_pion.rec.hipo";
		//String input = "/projet/nucleon/silvia/out_ep.hipo";
		//String input = "/projet/nucleon/silvia/out_out_bis.hipo";
		//String input = "/projet/nucleon/silvia/out_bis.hipo";
		//String input = "/projet/nucleon/silvia/test.rec.hipo";
		//String input = "/projet/nucleon/pierre/test_out3.hipo";
		//String input = "/projet/nucleon/silvia/test.hipo";
		String input = "/projet/nucleon/pierre/RecCND/clas_002227.evio.18.hipo";
		//String input = "/projet/nucleon/pierre/RecCND/test.hipo";
		//String input = "/projet/nucleon/silvia/CLARA/out_clasdispr_small.00849.hipo";
		HipoDataSource  reader = new HipoDataSource();
		reader.open(input);
		String outputFile="/projet/nucleon/pierre/RecCND/test1.hipo";
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

//				if(event.hasBank("CND::hits")){
//							//event.show();
//				System.out.println("event nb "+enb);
//				event.getBank("CND::hits").show();	
//			event.getBank("CND::adc").show();	
//			event.getBank("CND::tdc").show();	
//				}



			if(enb==30) break;

		}		
		writer.close();

		//some statitics on cvt/cnd matching
		System.out.println("enb "+enb);
		System.out.println("ecnd "+ecnd);
		System.out.println("hcvt "+hcvt);
		System.out.println("posmatch "+posmatch);
		System.out.println("match "+match);
		System.out.println("%match cnd "+100.*match/posmatch);
		System.out.println("Done");


		HipoDataSource  sortie = new HipoDataSource();
		sortie.open(outputFile);

		System.out.println("Fichier de sortie : ");
		while(sortie.hasEvent()) {

			DataEvent event = (DataEvent) sortie.getNextEvent();
			//event.show();

		}		
	}

}


