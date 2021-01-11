/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo3;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSync;


import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.io.HipoWriter;
import org.jlab.jnp.hipo.schema.SchemaFactory;

/**
 *
 * @author gavalian
 */
public class Hipo3DataSync implements DataSync {
    
    HipoWriter writer = null;
    
    public Hipo3DataSync(){
        this.writer = new HipoWriter();
        this.writer.setCompressionType(2);
        writer.appendSchemaFactoryFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        System.out.println("[HipoDataSync] ---> dictionary size = " + writer.getSchemaFactory().getSchemaList().size());
        //this.writer.getSchemaFactory().initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        //this.writer.getSchemaFactory().show();
    }
    
    public Hipo3DataSync(SchemaFactory factory){
        this.writer = new HipoWriter();
        this.writer.setCompressionType(2);
        writer.appendSchemaFactory(factory);
    }
    
    @Override
    public void open(String file) {
        /*
        EvioDataDictionary  dict = EvioFactory.getDictionary();
        String[] descList = dict.getDescriptorList();
        for(String desc : descList){
            String descString = dict.getDescriptor(desc).toString();
            this.writer.addHeader(descString);
        }*/
        this.writer.open(file);
    }

    @Override
    public void writeEvent(DataEvent event) {
        //EvioDataEvent  evioEvent = (EvioDataEvent) event;
        if(event instanceof Hipo3DataEvent) {
            Hipo3DataEvent hipoEvent = (Hipo3DataEvent) event;
            this.writer.writeEvent(hipoEvent.getHipoEvent());
        }
    }

    public void close() {
        this.writer.close();
    }
    
    public void setCompressionType(int type){
        this.writer.setCompressionType(type);
    }
    
    public DataEvent createEvent() {
        HipoEvent event = this.writer.createEvent();
        return new Hipo3DataEvent(event);
    }
    
    public static void printUsage(){
        System.out.println("\tUsage: convert -[option] output.hipo input.evio [input2.evio] [input3.evio]");
            System.out.println("\n\t Options :");
            System.out.println("\t\t -u    : uncompressed");
            System.out.println("\t\t -gzip : gzip compression");
            System.out.println("\t\t -lz4  : lz4 compression");
            System.out.println("\n");
    }
    
    public static void main(String[] args){
        
        Hipo3DataSync writer = new Hipo3DataSync();
        writer.open("test_hipoio.hipo");
        for(int i = 0; i < 20; i++){
            DataEvent event = writer.createEvent();
            DataBank   bank = event.createBank("FTOF::dgtz", 12);
            DataBank   bankDC = event.createBank("DC::dgtz",  7);
            for(int k = 0; k < 5; k++){
                bank.setByte("sector", k, (byte) (1+k));
                bank.setByte("layer", k, (byte) (2+k));
                bank.setShort("component", k, (short) (2+k*5));
                bank.setInt("ADCL", k, (int) (Math.random()*3000) );
                bank.setInt("ADCR", k, (int) (Math.random()*3000) );
                bank.setInt("TDCL", k, (int) (Math.random()*3000) );
                bank.setInt("TDCR", k, (int) (Math.random()*3000) );
            }
            //bank.show();
            event.appendBanks(bank,bankDC);
            writer.writeEvent(event);
        }
        writer.close();
        /*
        if(args.length<3){
            HipoDataSync.printUsage();
            System.exit(0);
        }
        
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
        
        if(compressionType<0){
            HipoDataSync.printUsage();
            System.out.println("[error] ---> compression type string is invalid.");
            System.exit(0);
        }
        
        String outputFile       = args[1];
        List<String> inputFiles = new ArrayList<String>();
        
        for(int i = 2; i < args.length; i++){
            inputFiles.add(args[i]);
        }
        
        File outFile = new File(outputFile);
        
        if(outFile.exists()==true){
            System.out.println("\n[error] ---> can not overwrite existing file.\n\n");
            System.exit(0);
        }
        
        HipoDataSync  writer = new HipoDataSync();
        writer.setCompressionType(compressionType);
        writer.open(outputFile);
        for(String inFile : inputFiles){
            EvioSource reader = new EvioSource();
            reader.open(inFile);
            while(reader.hasEvent()){
                EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
                
                EvioDataBank  bankFTOF = EvioHipoEvent.getBankFTOF(event);
                EvioDataEvent cevent   = EvioFactory.createEvioEvent();
                cevent.appendBank(bankFTOF);
                if(event.hasBank("TimeBasedTrkg::TBTracks")==true){
                    EvioDataBank bankTRK = (EvioDataBank) event.getBank("TimeBasedTrkg::TBTracks");
                    cevent.appendBanks(bankTRK);
                }
                
                writer.writeEvent(cevent);
                //writer.writeEvent(event);
            }
        }
        writer.close();*/
    }

}
