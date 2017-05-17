/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;


import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.clas.detector.*;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.clas.detector.CherenkovResponse;





/**
 *
 * @author gavalian
 */
public class EBEngine extends ReconstructionEngine {
    
    public EBEngine(){
        super("EB","gavalian","1.0");
    }
    
    public boolean processDataEvent(DataEvent de) {
        
        
        int eventType = EBio.TRACKS_HB;
        
        String particleBank     = "RECHB::Particle";
        String calorimeterBank  = "RECHB::Calorimeter";
        String scintillatorBank = "RECHB::Scintillator";
        String cherenkovBank    = "RECHB::Cherenkov";
        String eventBank        = "REC::Event";
        
        //System.out.println(" EVENT BUILDER = " + EBio.isTimeBased(de));
        if(EBio.isTimeBased(de)==true){
            eventType = EBio.TRACKS_TB;
            particleBank = "REC::Particle";
            calorimeterBank = "REC::Calorimeter";
            scintillatorBank = "REC::Scintillator";
            cherenkovBank = "REC::Cherenkov";
            eventBank =  "REC::Event";
            //matrixBank    = "REC::TBCovMat";
        }
        
        int run   = 10;
      //  double rf = -100.0;
        
        if(de.hasBank("RUN::config")==true){
            
        }
                
      //  if(de.hasBank("RUN::rf")==true){
     //       DataBank bank = de.getBank("RUN::rf");
     //       if(bank.rows()>0){
     //           rf = bank.getFloat("time", 0);
     //       }
    //    }
        
        
        EventBuilder eb = new EventBuilder();
        
        List<CalorimeterResponse>   responseECAL = CalorimeterResponse.readHipoEvent(de, "ECAL::clusters", DetectorType.EC);
        List<ScintillatorResponse>  responseFTOF = ScintillatorResponse.readHipoEvent(de, "FTOF::hits", DetectorType.FTOF,5.0);
        List<ScintillatorResponse>  responseCTOF = ScintillatorResponse.readHipoEvent(de, "CTOF::hits", DetectorType.CTOF,5.0);
        List<CherenkovResponse>     responseHTCC = CherenkovResponse.readHipoEvent(de,"HTCC::rec",DetectorType.HTCC);
        List<TaggerResponse>           ft_tracks = TaggerResponse.readHipoEvent(de, "FT::particles");

        
//        System.out.println("---------");
//        for(ScintillatorResponse r : responseCTOF ){
//            System.out.println(r);
//        }
        
        eb.addScintillatorResponses(responseFTOF);
        eb.addScintillatorResponses(responseCTOF);
        eb.addCalorimeterResponses(responseECAL);
        eb.addCherenkovResponses(responseHTCC);

        
        List<DetectorTrack> ctracks = DetectorData.readCentralDetectorTracks(de, "CVTRec::Tracks");
        eb.addCentralTracks(ctracks);

        if(eventType==EBio.TRACKS_HB){
            List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(de, "HitBasedTrkg::HBTracks");

            //System.out.println("LOADING HIT BASED TRACKS SIZE = " + tracks.size());
            eb.addHBTracks(tracks);
        } else {
            List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(de, "TimeBasedTrkg::TBTracks");
            //List<double[]> covariant_matrices = DetectorData.readTBCovMat(de, "TimeBasedTrkg::TBCovMat");
            eb.addTBTracks(tracks);
            //System.out.println("LOADING TIME BASED TRACKS SIZE = " + tracks.size());
        }
        
        //System.out.println("Number of particles before matching  " + eb.getEvent().getParticles().size());
        
        eb.processHitMatching();
        eb.addTaggerTracks(ft_tracks);
        eb.processNeutralTracks();        
        eb.assignTrigger();
 
        EBRadioFrequency rf = new EBRadioFrequency();
        eb.getEvent().setRfTime(rf.getTime(de)+EBConstants.RF_OFFSET);
        //eb.getEvent().setRfTime(rf);
        
        EBAnalyzer analyzer = new EBAnalyzer();
        //System.out.println("analyzing");
        analyzer.processEvent(eb.getEvent());

        
       //System.out.println(eb.getEvent().toString());
        
        
        if(eb.getEvent().getParticles().size()>0){
            DataBank bankP = DetectorData.getDetectorParticleBank(eb.getEvent().getParticles(), de, particleBank);
            List<CalorimeterResponse>  calorimeters = eb.getEvent().getCalorimeterResponseList();
            List<ScintillatorResponse> scintillators = eb.getEvent().getScintillatorResponseList();
            List<CherenkovResponse> cherenkovs = eb.getEvent().getCherenkovResponseList();
            DataBank bankCal = DetectorData.getCalorimeterResponseBank(calorimeters, de, calorimeterBank);
            DataBank bankSci = DetectorData.getScintillatorResponseBank(scintillators, de, scintillatorBank);
            DataBank bankChe = DetectorData.getCherenkovResponseBank(cherenkovs, de, cherenkovBank);
            //DataBank bankEve = DetectorData.getEventBank(eb.getEvent(), de, eventBank);
            //DataBank bankMat = DetectorData.getTBCovMatBank(eb.getEvent().getParticles(), de, matrixBank);
            de.appendBanks(bankP);
            if(bankCal.rows()>0) de.appendBanks(bankCal);
            if(bankSci.rows()>0) de.appendBanks(bankSci);
            if(bankChe.rows()>0) de.appendBanks(bankChe);
            if(EBio.isTimeBased(de)==true){
                //de.appendBanks(bankMat);
            }
//            if(eb.getEvent().getParticle(0).getPid()==11){
//               
//                eb.show();
//                DataBank bank = de.getBank("MC::Particle");
//                bank.show();
//            }
        }

            
//       for(int i = 0 ; i < eb.getEvent().getParticles().size() ; i++) {
//            System.out.println("Momentum  " + eb.getEvent().getParticles().get(i).vector().mag());
//        }

/*

List<DetectorParticle>  chargedParticles = EBio.readTracks(de, eventType);
List<DetectorResponse>  ftofResponse     = EBio.readFTOF(de);
List<DetectorResponse>  ecalResponse     = EBio.readECAL(de);
List<CherenkovResponse> htccResponse     = EBio.readHTCC(de);

EBProcessor processor = new EBProcessor(chargedParticles);
processor.addTOF(ftofResponse);
processor.addECAL(ecalResponse);
processor.addHTCC(htccResponse);

processor.matchTimeOfFlight();
processor.matchCalorimeter();
processor.matchHTCC();
processor.matchNeutral();

List<DetectorParticle> centralParticles = EBio.readCentralTracks(de);
*/
/*
int nparticles = processor.getParticles().size();
System.out.println("--------------------------------- ### NPARTICLES " + nparticles);
for(int i = 0; i < nparticles; i++){
if(processor.getParticles().get(i).getCharge()<0){
System.out.println(processor.getParticles().get(i).toString());
}
}
for(int i = 0; i < processor.getResponses().size(); i++){
System.out.println(processor.getResponses().get(i).toString());
}*/

//processor.getParticles().addAll(centralParticles);
/*
if(de.hasBank("RUN::config")==true){
EvioDataBank bankHeader = (EvioDataBank) de.getBank("RUN::config");
System.out.println(String.format("***>>> RUN # %6d   EVENT %6d",
bankHeader.getInt("Run",0), bankHeader.getInt("Event",0)));
}*/
//processor.show();
/*System.out.println("CHARGED PARTICLES = " + chargedParticles.size());
for(DetectorParticle p : chargedParticles){
System.out.println(p);
}*/
/*
DetectorEvent  detectorEvent = new DetectorEvent();

for(int i = 0 ; i < chargedParticles.size() ; i++){
detectorEvent.addParticle(chargedParticles.get(i));

}*/

/*
EventTrigger trigger = new EventTrigger();
//detectorEvent.setEventTrigger(trigger);

EBTrigger triggerinfo = new EBTrigger();
triggerinfo.setEvent(detectorEvent);
triggerinfo.setDataEvent(de);

triggerinfo.RFInformation(); //Obtain RF Time
triggerinfo.Trigger();//Use Trigger Particle Vertex Time and RF Time for Start Time
triggerinfo.CalcBetas(); //Calculate Speeds and Masses of Particles
*/

/*
EBPID pid = new EBPID();
if(EBio.isTimeBased(de)==true){
pid.setEvent(detectorEvent);
pid.PIDAssignment();//PID Assignment
}*/
      

        
//        for(int i = 0 ; i < chargedParticles.size() ; i++){ 
//            System.out.println(chargedParticles.get(i).getPid());
//            System.out.println(chargedParticles.get(i).getPIDResult().getPIDExamination());
//          }
        
        /*
        EvioDataBank pBank = (EvioDataBank) EBio.writeTraks(processor.getParticles(), eventType);
        EvioDataBank dBank = (EvioDataBank) EBio.writeResponses(processor.getResponses(), eventType);
        EvioDataBank cBank = (EvioDataBank) EBio.writeCherenkovResponses(processor.getCherenkovResponses(), eventType);
        EvioDataBank tbank = (EvioDataBank) EBio.writeTrigger(detectorEvent);
        de.appendBanks(pBank,dBank);
        */
        return true;
    }

    @Override
    public boolean init() {
        System.out.println("[EB::] --> event builder is ready....");
        return true;
    }
    
}
