/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import java.util.List;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.detector.DetectorTrack;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.options.OptionParser;


/**
 *
 * @author gavalian
 */
public class EBDebug {
    
    public void printSingle(DataEvent de){
        DetectorEvent event = DetectorEvent.readDetectorEvent(de);
        List<DetectorTrack> tracks = DetectorData.readDetectorTracks(de, "TimeBasedTrkg::TBTracks");
        if(event.getParticles().size()>0&&tracks.size()==1){
            if(event.getParticle(0).hasHit(DetectorType.FTOF, 2)){
               DetectorResponse resp = event.getParticle(0).getHit(DetectorType.FTOF, 2);
               DetectorParticle part = event.getParticle(0);
               double distance = tracks.get(0).getLastCross().origin().distance(resp.getPosition().toPoint3D());
              
               double path = tracks.get(0).getPath() + distance;
               double time = resp.getTime()-123.5;
               double    p = tracks.get(0).getP();
               double beta = path/time/30.0;
               double betath = p/Math.sqrt(p*p+0.938*0.938);
               double mass = p*p/(beta*beta) - p*p;
               System.out.println(String.format("%8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f", 
                       tracks.get(0).getPath(),tracks.get(0).getP(),
                       part.vector().mag(), resp.getTime(),path,beta, betath, mass));
            }
        }
    }
    
    public static void main(String[] args){
        OptionParser  parser = new OptionParser();
        parser.addRequired("-i", "input.hipo");
        parser.addOption("-d", "0","debug flag if > 0 prints out event bank content");
        parser.parse(args);
        
        String inputFile = parser.getOption("-i").stringValue();
        
        EBDebug eb = new EBDebug();
        
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);
        
        int debug = parser.getOption("-d").intValue();
        
        while(reader.hasEvent()==true){
            DataEvent event = reader.getNextEvent();
            
            if(debug>4){
                eb.printSingle(event);
                continue;
            }
            //DetectorData.readDetectorEvent(event);
            //if()
            if(event.hasBank("REC::Particle")==true){
                DataBank bank = event.getBank("REC::Particle");
                DetectorEvent  detEvent = DetectorEvent.readDetectorEvent(event);
                if(debug>2){
                    System.out.println(detEvent.toString());
                }
                if(bank!=null){
                    if(bank.rows()>0){
                        if(bank.getInt("pid", 0)==11&&debug>0&&debug<3){
                            bank.show();
                        }
                        if(bank.getInt("pid", 0)==11){
                            int nrows = bank.rows();
                            for(int i = 0; i < nrows; i++){
                                
                                int charge = bank.getByte("charge", i);
                                float beta = bank.getFloat("beta", i);
                                float mass = bank.getFloat("mass", i);                                
                                int pid    = bank.getInt("pid", i);
                                
                                float px   = bank.getFloat("px", i);
                                float py   = bank.getFloat("py", i);
                                float pz   = bank.getFloat("pz", i);
                                double p   = Math.sqrt(px*px+py*py+pz*pz);
                                double betaTheory = 0.0;
                                if(pid==2212){
                                    betaTheory =  p / Math.sqrt(p*p+0.938*0.938);
                                }
                                if(pid==321){
                                    betaTheory =  p / Math.sqrt(p*p+0.495*0.495);
                                }
                                if(beta>0.2){
                                    System.out.printf("%2d %6d %8.3f %8.3f %8.3f %8.3f\n",charge, pid,
                                            Math.sqrt(px*px+py*py+pz*pz), beta, betaTheory, mass);
                                }
                            }
                        }
                        
                    }
                }
            }
        }
        
    }
}
