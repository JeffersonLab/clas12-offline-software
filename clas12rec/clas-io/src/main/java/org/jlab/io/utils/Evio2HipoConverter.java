/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataSync;

/**
 *
 * @author gavalian
 */
public class Evio2HipoConverter {
    
    public int COMPRESSION_TYPE = 0;
    private Map<String,Integer>   excludedBanks = new LinkedHashMap<String,Integer>();
    
    public Evio2HipoConverter(){
        this.exclude("DC::true");
        this.exclude("FTOF1A::true");
        this.exclude("FTOF1B::true");
        this.exclude("FTOF2B::true");
        this.exclude("PCAL::true");
        this.exclude("EC::true");
        this.exclude("HTCC::true");
        this.exclude("BST::true");
    }
    
    public final void exclude(String name){
        this.excludedBanks.put(name, 1);
    }
    /**
     * returns list of banks from the dictionary that match the name.
     * @param name
     * @return 
     */
    public List<String> getBanksWithName(String name){
        String[] system = EvioFactory.getDictionary().getDescriptorList();
        
        List<String>  bankNames = new ArrayList<String>();
        for(String item : system){
            if(item.startsWith(name)==true){
                bankNames.add(item);
            }
        }
        return bankNames;
    }
    /**
     * Returns bank names that match the name, and exist in the event
     * @param event
     * @param name
     * @return 
     */
    public List<String> getExistingBanksWithName(DataEvent event, String name){
        List<String> banks      = this.getBanksWithName(name);
        List<String> banksExist = new ArrayList<String>();
        
        for(String item : banks){
            if(event.hasBank(item)==true){
                if(this.excludedBanks.containsKey(item)==false){
                    banksExist.add(item);
                }
            }
        }
        return banksExist;
    }
    
    public Map<String,Integer>  getExcluded(){
        return this.excludedBanks;
    }
    
    public DataBank[]  getDataBanksWithName(DataEvent event, String name){
        List<String> banksExist = this.getExistingBanksWithName(event, name);
        DataBank[]   banks = new DataBank[banksExist.size()];
        int icounter = 0;
        for(int i = 0; i < banksExist.size(); i++){
            banks[i] = event.getBank(banksExist.get(i));
        }
        return banks;
    }
    
    public void convertEvio(String output, List<String> input, List<String> banks){
        EvioDataSync  writer = new EvioDataSync();
        writer.open(output);
        //writer.setCompressionType(this.COMPRESSION_TYPE);
        
        for(int icount = 0; icount < input.size(); icount++){
            EvioSource  reader = new EvioSource();
            reader.open(input.get(icount));
            System.out.println("[convertor] ---> openning file # " + icount
                    + " : " + input.get(icount));
            while(reader.hasEvent()==true){
                DataEvent  inEevent = reader.getNextEvent();
                DataEvent  outEvent = EvioFactory.createEvioEvent();
                for(String entries : banks){
                    DataBank[]  dataBanks = this.getDataBanksWithName(inEevent, entries);
                    if(dataBanks.length!=0){
                        outEvent.appendBanks(dataBanks);
                    }
                }
                writer.writeEvent(outEvent);
            }
        }
    }
    
    public void convertHipo(String output, List<String> input, List<String> banks){
        if(this.COMPRESSION_TYPE>2){
            this.convertEvio(output, input, banks);
        }
        HipoDataSync  writer = new HipoDataSync();
        writer.open(output);
        writer.setCompressionType(this.COMPRESSION_TYPE);
        
        for(int icount = 0; icount < input.size(); icount++){
            EvioSource  reader = new EvioSource();
            reader.open(input.get(icount));
            System.out.println("[convertor] ---> openning file # " + icount
                    + " : " + input.get(icount));
            int counter = 0;
            while(reader.hasEvent()==true){
                DataEvent  inEvent = reader.getNextEvent();
                DataEvent  outEvent = EvioFactory.createEvioEvent();
                counter++;
                if(banks.isEmpty()){
                    writer.writeEvent(inEvent);
                } else {
                
                    for(String entries : banks){
                        DataBank[]  dataBanks = this.getDataBanksWithName(inEvent, entries);
                        if(dataBanks.length!=0){
                            outEvent.appendBanks(dataBanks);
                        }
                    }
                    
                    if(inEvent.hasBank("GenPart::true")==true){
                        EvioDataBank  bankGen = (EvioDataBank) inEvent.getBank("GenPart::true");
                        //System.out.println("writing generated event " + counter);
                        //bankGen.show();
                        ((EvioDataEvent) outEvent).appendGeneratedBank(bankGen);
                        //outEvent.appendBank(bankGen);
                    }
                    writer.writeEvent(outEvent);
                }
            }
        }
        writer.close();
    }
    
    public static void printUsage(){
        System.out.println("\tUsage: convert -[compression] -b [bank1:bank2] output.hipo input.evio [input2.evio] [input3.evio]");
            System.out.println("\n\t Options :");
            System.out.println("\t\t -u    : uncompressed");
            System.out.println("\t\t -gzip : gzip compression");
            System.out.println("\t\t -lz4  : lz4 compression");
            System.out.println("\n");
    }
    
    public static void main(String[] args){
        
        
        if(args.length>0){
            if(args[0].compareTo("-list")==0){
                EvioFactory.getDictionary().show();
            }
        }
        /*
        if(args.length<5){
            HipoDataSync.printUsage();
            System.exit(0);
        }*/
         
        if(args[0].startsWith("-")==false){
            System.out.println("\n\n--> please provide compression type");
            HipoDataSync.printUsage();
            System.exit(0);
        }
        
        int compressionType = -1;
        
        if(args[0].compareTo("-u")==0){
            compressionType = 0;
        }
        
        if(args[0].compareTo("-gzip")==0){
            compressionType = 1;
        }
        
        if(args[0].compareTo("-lz4")==0){
            compressionType = 2;
        }
        
        if(args[0].compareTo("-evio")==0){
            compressionType = 3;
        }
        
        if(compressionType<0){
            HipoDataSync.printUsage();
            System.out.println("[error] ---> compression type string is invalid.");
            System.exit(0);
        }
        
        if(args[1].startsWith("-b")==false){
            System.out.println("\n\n--> please provide bank names in the output");
            HipoDataSync.printUsage();
            System.exit(0);
        }
        
        String bankNames = args[2];
        List<String> bankList = new ArrayList<String>();
        
        boolean writeTrueBanks = false;
        
        if(bankNames.compareTo("ALLTRUE")==0){
            System.out.println("[convertor] ---> outputing ALL banks with TRUE");
            writeTrueBanks = true;
        } else {
            if(bankNames.compareTo("ALL")==0){
                System.out.println("[convertor] ---> outputing ALL banks");
            } else {
                String[] tokens = bankNames.split(":");
                for(String token : tokens){
                    bankList.add(token);
                }
            }
        }
        
        String outputFile = args[3];
        
        File outFile = new File(outputFile);
        
        if(outFile.exists()==true){
            System.out.println("\n[error] ---> can not overwrite existing file.\n\n");
            System.exit(0);
        }

        List<String> inputFiles = new ArrayList<String>();
        
        for(int i = 4; i < args.length; i++){
            inputFiles.add(args[i]);
        }
        
        
        Evio2HipoConverter converter = new Evio2HipoConverter();
        converter.COMPRESSION_TYPE = compressionType;
        if(writeTrueBanks==true){
            converter.getExcluded().clear();
        }
        
        converter.convertHipo(outputFile, inputFiles, bankList);
        //String bank = args[0];
        //converter.getBanksWithName(bank);
        
    }
}
