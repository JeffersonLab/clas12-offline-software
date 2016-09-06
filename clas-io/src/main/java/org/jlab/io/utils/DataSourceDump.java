/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

import java.io.Console;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class DataSourceDump {
    public static String waitForEnter() {
        String line = "";
        Console c = System.console();
        if (c != null) {
            // printf-like arguments
            //c.format(message, args);
            c.format("\nPress Enter for Next Event or Bank Name: ");
            line = c.readLine();
        }
        return line;
    }
    public static void main(String[] args){
                
        //CommandLineTools  parser = new CommandLineTools();
        
        //parser.addRequired("-i");
        
        //parser.addDescription("-i", "input file name");
        //parser.addDescription("-b", "bank name to display (eg. -b PCAL::dgtz )");

        
        //parser.setMultiOption("-b");
        //parser.parse(args);
        
        if(args.length==0){
            System.out.println("\n\n\t Usage : eviodump [filename]");
            System.exit(0);
        }
        
        String inputFile = args[0];
        
        DataSource  reader = null;
        
        if(inputFile.contains("hipo")==true){
            reader = new HipoDataSource();
        } else {
            reader = new EvioSource();
        }
        
        reader.open(inputFile);

        String command = "";
        EvioDataEvent event = null;
        int icounter = 0;
        while(reader.hasEvent()==true){
            
            if(command.length()<4){
                event = (EvioDataEvent) reader.getNextEvent();
                icounter++;
                System.out.println("*********************** EVENT # " + icounter 
                        + "  ***********************");
                event.show();
            }
            command = DataSourceDump.waitForEnter();
            if(command.length()>4){
                if(event.hasBank(command)==true){
                    EvioDataBank bank = (EvioDataBank) event.getBank(command);
                    bank.show();
                }
            }
            
            if(command.compareTo("q")==0){
                reader.close();
                System.exit(0);
            }
            /*
            for(String bankName : banks){
                if(event.hasBank(bankName)==true){
                    EvioDataBank bank = (EvioDataBank) event.getBank(bankName);
                    bank.show();
                }
            }*/
        }         
    }   

}
