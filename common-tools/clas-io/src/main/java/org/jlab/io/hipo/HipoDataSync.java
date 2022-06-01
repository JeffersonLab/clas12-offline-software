package org.jlab.io.hipo;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSync;


import org.jlab.io.base.DataBank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Schema;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.io.HipoWriter;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;


/**
 *
 * @author gavalian
 */
public class HipoDataSync implements DataSync {

    public static Logger LOGGER = Logger.getLogger(HipoDataSync.class.getName());
    
    HipoWriterSorted writer = null;
    
    public HipoDataSync(){
        this.writer = new HipoWriterSorted();
        this.writer.setCompressionType(2);
        String env = System.getenv("CLAS12DIR");
        writer.getSchemaFactory().initFromDirectory(env + "/etc/bankdefs/hipo4");
        LOGGER.log(Level.INFO,"[HipoDataSync] ---> dictionary size = " + writer.getSchemaFactory().getSchemaList().size());
        //this.writer.getSchemaFactory().initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        //this.writer.getSchemaFactory().show();
    }
    
    public HipoDataSync(SchemaFactory factory){
        this.writer = new HipoWriterSorted();
        this.writer.setCompressionType(2);
        List<Schema>  schemas = factory.getSchemaList();
        for(Schema schema : schemas){
            writer.getSchemaFactory().addSchema(schema);
        }
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

    public void addSchema(Schema schema){
        writer.getSchemaFactory().addSchema(schema);
    }
    
    public void addSchemaList(List<Schema> schemaList){
        for(Schema schema : schemaList) addSchema(schema);
    }
    
    @Override
    public void writeEvent(DataEvent event) {
        //EvioDataEvent  evioEvent = (EvioDataEvent) event;
        if(event instanceof HipoDataEvent) {
            HipoDataEvent hipoEvent = (HipoDataEvent) event;
            
            this.writer.addEvent(hipoEvent.getHipoEvent(),hipoEvent.getHipoEvent().getEventTag());
        }
    }
    public HipoWriterSorted getWriter(){ return writer;}
    
    public void close() {
        this.writer.close();
    }
    
    public void setCompressionType(int type){
        this.writer.setCompressionType(type);
    }
    
    @Override
    public DataEvent createEvent() {
        Event event = new Event();
        return new HipoDataEvent(event,writer.getSchemaFactory());
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
        
        HipoDataSync writer = new HipoDataSync();
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
