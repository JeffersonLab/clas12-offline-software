/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import org.jlab.clas.detector.DetectorData;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.utils.options.OptionParser;


/**
 *
 * @author gavalian
 */
public class EBDebug {
    public static void main(String[] args){
        OptionParser  parser = new OptionParser();
        parser.addRequired("-i", "input.hipo");
        parser.addOption("-d", "0","debug flag if > 0 prints out event bank content");
        parser.parse(args);
        
        String inputFile = parser.getOption("-i").stringValue();
        
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);
        
        int debug = parser.getOption("-d").intValue();
        
        while(reader.hasEvent()==true){
            DataEvent event = reader.getNextEvent();
            //DetectorData.readDetectorEvent(event);
            //if()
            if(event.hasBank("REC::Particle")==true){
                DataBank bank = event.getBank("REC::Particle");
                if(bank!=null){
                    if(bank.rows()>0){
                        if(bank.getInt("pid", 0)==11&&debug>0){
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
                                if(beta>0.2){
                                    System.out.printf("%2d %6d %8.3f %8.3f %8.3f\n",charge, pid,
                                            Math.sqrt(px*px+py*py+pz*pz), beta, mass);
                                }
                            }
                        }
                        
                    }
                }
            }
        }
        
    }
}
