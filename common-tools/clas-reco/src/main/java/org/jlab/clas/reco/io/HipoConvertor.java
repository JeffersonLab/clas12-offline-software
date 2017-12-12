/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco.io;

import java.util.List;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataBank;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.jnp.utils.file.FileUtils;

/**
 *
 * @author gavalian
 */
public class HipoConvertor {
    
    
    public static void createRecBanksTOF(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        
        if(evioEvent.hasBank("FTOFRec::rawhits")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTOFRec::rawhits");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("FTOF::rawhits", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setInt("id", i, evioBank.getInt("id", i));
                hipoBank.setShort("status", i, (short) evioBank.getInt("paddle_status", i));                
                hipoBank.setByte("sector", i, (byte) evioBank.getInt("sector", i));
                hipoBank.setByte("layer", i, (byte) evioBank.getInt("panel_id", i));
                hipoBank.setShort("component", i, (short) evioBank.getInt("paddle_id", i));
                hipoBank.setFloat("energy_left", i, evioBank.getFloat("energy_left", i));
                hipoBank.setFloat("energy_right", i, evioBank.getFloat("energy_right", i));
                hipoBank.setFloat("energy_left_unc", i, evioBank.getFloat("energy_left_unc", i));
                hipoBank.setFloat("energy_right_unc", i, evioBank.getFloat("energy_right_unc", i));
                hipoBank.setFloat("time_left", i, evioBank.getFloat("time_left", i));
                hipoBank.setFloat("time_right", i, evioBank.getFloat("time_right", i));
                hipoBank.setFloat("time_left_unc", i, evioBank.getFloat("time_left_unc", i));
                hipoBank.setFloat("time_right_unc", i, evioBank.getFloat("time_right_unc", i));                
            }
            hipoEvent.appendBanks(hipoBank);
        }
        
        if(evioEvent.hasBank("FTOFRec::ftofhits")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTOFRec::ftofhits");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("FTOF::hits", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setShort("id",i,(short) evioBank.getInt("id",i));
                hipoBank.setShort("status",i,(short) evioBank.getInt("paddle_status",i));
                hipoBank.setShort("trackid",i,(short) evioBank.getInt("trkId",i));
                hipoBank.setByte("sector",i,(byte) evioBank.getInt("sector",i));
                hipoBank.setByte("layer",i,(byte) evioBank.getInt("panel_id",i));
                hipoBank.setShort("component",i,(short) evioBank.getInt("paddle_id",i));
                hipoBank.setFloat("energy", i, evioBank.getFloat("energy",i));
                hipoBank.setFloat("time", i, evioBank.getFloat("time",i));
                hipoBank.setFloat("energy_unc", i, evioBank.getFloat("energy_unc",i));
                hipoBank.setFloat("time_unc", i, evioBank.getFloat("time_unc",i));
                hipoBank.setFloat("tx", i, evioBank.getFloat("tx",i));
                hipoBank.setFloat("ty", i, evioBank.getFloat("ty",i));
                hipoBank.setFloat("tz", i, evioBank.getFloat("tz",i));
                hipoBank.setFloat("x", i, evioBank.getFloat("x",i));
                hipoBank.setFloat("y", i, evioBank.getFloat("y",i));
                hipoBank.setFloat("z", i, evioBank.getFloat("z",i));
                hipoBank.setFloat("x_unc", i, evioBank.getFloat("x_unc",i));
                hipoBank.setFloat("y_unc", i, evioBank.getFloat("y_unc",i));
                hipoBank.setFloat("z_unc", i, evioBank.getFloat("z_unc",i));                
            }
            hipoEvent.appendBanks(hipoBank);
        }        
    }
    
    public static void createRecBanksCTOF(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        
        if(evioEvent.hasBank("CTOFRec::rawhits")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("CTOFRec::rawhits");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("CTOF::rawhits", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setInt("id", i, evioBank.getInt("id", i));
                hipoBank.setShort("status", i, (short) evioBank.getInt("paddle_status", i));                
                hipoBank.setShort("component", i, (short) evioBank.getInt("paddle_id", i));
                hipoBank.setFloat("energy_up", i, evioBank.getFloat("energy_up", i));
                hipoBank.setFloat("energy_down", i, evioBank.getFloat("energy_down", i));
                hipoBank.setFloat("energy_up_unc", i, evioBank.getFloat("energy_up_unc", i));
                hipoBank.setFloat("energy_down_unc", i, evioBank.getFloat("energy_down_unc", i));
                hipoBank.setFloat("time_up", i, evioBank.getFloat("time_up", i));
                hipoBank.setFloat("time_down", i, evioBank.getFloat("time_down", i));
                hipoBank.setFloat("time_up_unc", i, evioBank.getFloat("time_up_unc", i));
                hipoBank.setFloat("time_down_unc", i, evioBank.getFloat("time_down_unc", i));                
            }
            hipoEvent.appendBanks(hipoBank);
        }
        
        if(evioEvent.hasBank("CTOFRec::ctofhits")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("CTOFRec::ctofhits");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("CTOF::hits", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setShort("id",i,(short) evioBank.getInt("id",i));
                hipoBank.setShort("status",i,(short) evioBank.getInt("paddle_status",i));
                //hipoBank.setShort("trackid",i,(short) evioBank.getInt("trkId",i));
                hipoBank.setByte("sector",i,(byte) evioBank.getInt("sector",i));
                hipoBank.setByte("layer",i,(byte) evioBank.getInt("panel_id",i));
                hipoBank.setShort("component",i,(short) evioBank.getInt("paddle_id",i));
                hipoBank.setFloat("energy", i, evioBank.getFloat("energy",i));
                hipoBank.setFloat("time", i, evioBank.getFloat("time",i));
                hipoBank.setFloat("energy_unc", i, evioBank.getFloat("energy_unc",i));
                hipoBank.setFloat("time_unc", i, evioBank.getFloat("time_unc",i));
                hipoBank.setFloat("tx", i, evioBank.getFloat("tx",i));
                hipoBank.setFloat("ty", i, evioBank.getFloat("ty",i));
                hipoBank.setFloat("tz", i, evioBank.getFloat("tz",i));
                hipoBank.setFloat("x", i, evioBank.getFloat("x",i));
                hipoBank.setFloat("y", i, evioBank.getFloat("y",i));
                hipoBank.setFloat("z", i, evioBank.getFloat("z",i));
                hipoBank.setFloat("x_unc", i, evioBank.getFloat("x_unc",i));
                hipoBank.setFloat("y_unc", i, evioBank.getFloat("y_unc",i));
                hipoBank.setFloat("z_unc", i, evioBank.getFloat("z_unc",i));                
            }
            hipoEvent.appendBanks(hipoBank);
        }        
    }
    
    public static void main(String[] args){
        
        String outputFile = args[0];
        String inputList  = args[1];
        
        List<String> inputFiles = FileUtils.readFile(inputList);
        
        HipoDataSync writer = new HipoDataSync();
        EvioHipoEvent convertor = new EvioHipoEvent();
        writer.setCompressionType(2);
        writer.open(outputFile);
        int counter = 0;
        for(String file : inputFiles){
            
            System.out.println("processing file ----> " + file);
            EvioSource reader = new EvioSource();
            reader.open(file);
            while(reader.hasEvent()==true){
                counter++;
                EvioDataEvent evioEvent = (EvioDataEvent) reader.getNextEvent();
                HipoDataEvent hipoEvent = convertor.getHipoEvent(writer, evioEvent);
                try {
                    HipoConvertor.createRecBanksTOF(hipoEvent, evioEvent);
                } catch (Exception e){
                    System.out.println("\n>>>   EVENT # " + counter + " error with FTOF banks\n");
                }
                try {
                    HipoConvertor.createRecBanksCTOF(hipoEvent, evioEvent);                
                } catch (Exception e){
                    System.out.println("\n>>>   EVENT # " + counter + " error with CTOF banks\n");                    
                }
                writer.writeEvent(hipoEvent);
            }
        }
        writer.close();
    }
}
