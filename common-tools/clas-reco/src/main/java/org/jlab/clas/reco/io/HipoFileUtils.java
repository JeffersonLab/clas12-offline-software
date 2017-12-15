/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco.io;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clas.physics.EventFilter;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.groot.data.DataVector;
import org.jlab.groot.data.H1F;
import org.jlab.groot.ui.TCanvas;

import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.data.HipoGroup;
import org.jlab.jnp.hipo.data.HipoNode;
import org.jlab.jnp.hipo.data.HipoNodeBuilder;
import org.jlab.jnp.hipo.io.HipoReader;
import org.jlab.jnp.hipo.io.HipoWriter;
import org.jlab.jnp.hipo.schema.Schema;
import org.jlab.jnp.hipo.schema.SchemaFactory;
import org.jlab.physics.io.LundReader;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.CommandLineParser;

/**
 *
 * @author gavalian
 */
public class HipoFileUtils {
    
    
     public static void writeHipoEvents(String name, List<String> inputFiles){
        HipoWriter writer = new HipoWriter();
        writer.defineSchema(new Schema("{20,GenPart::true}[1,pid,INT][2,px,FLOAT][3,py,FLOAT][4,pz,FLOAT][5,vx,FLOAT][6,vy,FLOAT][7,vz,FLOAT]"));
        writer.defineSchema(new Schema("{10,RUN::info}[1,Run,INT][2,Event,INT][3,Type,BYTE][4,Mode,BYTE][5,Torus,FLOAT][6,Solenoid,FLOAT]"));        
        writer.open(name);
        
        
    }
    
     
     public static void writeHipo(String outputName, int compression, String keep, String filter, List<String> files){
         HipoWriter writer = new HipoWriter();
         writer.open(outputName);
         int nFiles = files.size();
         writer.setCompressionType(compression);
         
         String[] keepSchema = keep.split(":");
         SchemaFactory writerFactory = new SchemaFactory();
         ProgressPrintout  progress = new ProgressPrintout();
         
         
         for(int i = 0; i < nFiles; i++){
             HipoReader reader = new HipoReader();
             reader.open(files.get(i));
             if(i==0){
                 SchemaFactory factory = reader.getSchemaFactory();

                 System.out.println(" OPENNING FIRST FILE : " + files.get(i));
                 System.out.println(" Scanning Schema FACTORY");
                 List<Schema> list = factory.getSchemaList();
         
                 for(Schema schema : list){
                     for(String key : keepSchema){
                         if(schema.getName().contains(key)==true||keep.compareTo("ALL")==0){
                             try {
                                 writerFactory.addSchema(schema);
                             } catch (Exception ex) {
                                 Logger.getLogger(HipoFileUtils.class.getName()).log(Level.SEVERE, null, ex);
                             }
                             writerFactory.addFilter(schema.getName());
                             writer.defineSchema(schema);
                             System.out.println("\t >>>>> adding schema to writer : " + schema.getName());
                         }
                     }
                 }
                 writerFactory.show();
                 //writeFactory.getSchemaEvent()
             }
             int nEvents = reader.getEventCount();
             for(int nev = 0; nev < nEvents; nev++){
                 HipoEvent    event = reader.readEvent(nev);
                 boolean flag = false;
                 for(HipoGroup group : event.getGroups()) {
//                     System.out.println(group.getSchema().getName());
                     if(group.getSchema().getName().contains(filter)==true || filter.compareTo("ANY")==0) flag = true;
                 }
                 if(flag){
                 HipoEvent outEvent = writerFactory.getFilteredEvent(event);
                 //outEvent.show();
                 writer.writeEvent(outEvent);
                 progress.updateStatus();
                 }
             }
         }
         writer.close();
     }
     
    public static EvioDataBank getGenPart(PhysicsEvent event){
        EvioDataBank bank = EvioFactory.createBank("GenPart::true", event.count());
        for(int i = 0; i < event.count(); i++){
            bank.setInt("pid", i, event.getParticle(i).pid());
            bank.setDouble("px", i, event.getParticle(i).px()*1000.0);
            bank.setDouble("py", i, event.getParticle(i).py()*1000.0);
            bank.setDouble("pz", i, event.getParticle(i).pz()*1000.0);
            bank.setDouble("vx", i, event.getParticle(i).vertex().x()*10.0);
            bank.setDouble("vy", i, event.getParticle(i).vertex().y()*10.0);
            bank.setDouble("vz", i, event.getParticle(i).vertex().z()*10.0);
        }
        return bank;
    }
    
    public static List<HipoNode> getGeneratedBank(PhysicsEvent event){
        List<HipoNode>  nodes = new ArrayList<HipoNode>();
        HipoNodeBuilder<Float>    vector = new HipoNodeBuilder<Float>();
        //HipoNodeBuilder<Integer>    vector = new HipoNodeBuilder<Integer>();
        HipoNodeBuilder<Short>    vertex = new HipoNodeBuilder<Short>();
        HipoNodeBuilder<Integer>  pid    = new HipoNodeBuilder<Integer>();
        
        for(int i = 0; i < event.count(); i++){
            
            pid.push(event.getParticle(i).pid());
            
            vector.push( (float) event.getParticle(i).px());
            vector.push( (float) event.getParticle(i).py());
            vector.push( (float) event.getParticle(i).pz());            
            
            vertex.push((short) (event.getParticle(i).vertex().x()*100.0));
            vertex.push((short) (event.getParticle(i).vertex().y()*100.0));
            vertex.push((short) (event.getParticle(i).vertex().z()*100.0));
        }
        
        nodes.add(pid.buildNode(20, 1));
        nodes.add(vector.buildNode(20, 2));
        nodes.add(vertex.buildNode(20, 3));
        //System.out.println();
        return nodes;
    }
    
    public static void writeLundFilesHipo(String output, List<String> lundFiles){
        LundReader reader = new LundReader();
        for(int i = 0; i < lundFiles.size(); i++){        
            reader.addFile(lundFiles.get(i));
            System.out.println("LUND READER : adding file ->>> " + lundFiles.get(i));
        }
        reader.open();
        
        HipoWriter writer = new HipoWriter();
        //writer.addHeader("{Reconstruction-File-LUND}");
        
        writer.setCompressionType(2);
        writer.open(output);
        while(reader.next()==true){
            PhysicsEvent  event = reader.getEvent();
            HipoEvent   hipoEvent = new HipoEvent();
            List<HipoNode>  nodes = HipoFileUtils.getGeneratedBank(event);
            //System.out.println("nodes size = " + nodes.size());
            hipoEvent.addNodes(nodes);
            
            writer.writeEvent(hipoEvent);
        }
        writer.close();
    }
    
    public static void writeLundFiles(String output, List<String> lundFiles){
        LundReader reader = new LundReader();
        for(int i = 0; i < lundFiles.size(); i++){        
            reader.addFile(lundFiles.get(i));
            System.out.println("LUND READER : adding file ->>> " + lundFiles.get(i));
        }
        reader.open();
        
        HipoDataSync writer = new HipoDataSync();
        writer.setCompressionType(2);
        writer.open(output);
        int icounter = 0;
        while(reader.next()==true){
            PhysicsEvent  event = reader.getEvent();
            EvioDataEvent evioEvent = EvioFactory.createEvioEvent();
            EvioDataBank  genPart   = HipoFileUtils.getGenPart(event);
            evioEvent.appendGeneratedBank(genPart);
            writer.writeEvent(evioEvent);
            icounter++;
        }
        System.out.println("LUND WRITER : written " + icounter + " events ");
        writer.close();
    }
    
    public static void eventShowHipo(String inputFile,String filter, String particle, String property, int nbins){
        HipoReader reader = new HipoReader();
        reader.open(inputFile);
        EventFilter evtFilter = new EventFilter(filter);
        DataVector  result = new DataVector();
        GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);
        int nentries = reader.getEventCount();
        int icounter = 0;
        
        for(int loop = 0; loop < nentries; loop++){
            //byte[] eventBuffer = reader.readEvent(loop);
            HipoEvent event = reader.readEvent(loop);
            PhysicsEvent physEvent = new PhysicsEvent();//fitter.createEvent(event);
            
            if(evtFilter.checkFinalState(physEvent.mc())==true){
                icounter++;
                Particle p = physEvent.getParticle(particle);
                if(p.p()>0.000001){
                    double value = p.get(property);
                    result.add(value);
                }
                /*System.out.println(" " + physEvent.mc().toLundString());
                System.out.println(" PARTICLE = \n" + p.toLundString());
                System.out.println(" filling value " + value);*/
            }
        }
        System.out.println(" EVENT # " + nentries + "  FILTER PASSED # " + icounter);
        H1F h = H1F.create(property, nbins, result);
        TCanvas c1 = new TCanvas("c1",500,500);
        c1.draw(h);
    }
    
    public static void eventShow(String inputFile,String filter, String particle, String property, int nbins){
        
        GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);
        
        HipoDataSource reader = new HipoDataSource();
        reader.open(inputFile);
        EventFilter evtFilter = new EventFilter(filter);
        DataVector  result = new DataVector();
        
        int icounter = 0;
        int nevents  = 0;
        while(reader.hasEvent()==true){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            PhysicsEvent  physEvent = new PhysicsEvent();//fitter.createEvent(event);
            if(evtFilter.checkFinalState(physEvent.mc())==true){
                icounter++;
                Particle p = physEvent.getParticle(particle);
                if(p.p()>0.000001){
                    double value = p.get(property);
                    result.add(value);
                }
                /*System.out.println(" " + physEvent.mc().toLundString());
                System.out.println(" PARTICLE = \n" + p.toLundString());
                System.out.println(" filling value " + value);*/
            }
            nevents++;
        }
        System.out.println(" EVENT # " + nevents + "  FILTER PASSED # " + icounter);
        H1F h = H1F.create(property, nbins, result);
        TCanvas c1 = new TCanvas("c1",500,500);
        c1.draw(h);
    }
    
    public static void main(String[] args){
        
        OptionParser parser = new OptionParser();
        parser.addRequired("-o");
        parser.addOption("-keep", "ALL", "Selection of banks to keep in the output");
        parser.addOption("-filter", "ANY", "Write only events with the selected bank");
        parser.addOption("-c", "2","Compression algorithm (0-none, 1-gzip, 2-lz4)");
        
        parser.parse(args);
        
        String outputFile = parser.getOption("-o").stringValue();
        List<String> inputFileList = parser.getInputList();
        int  compression    = parser.getOption("-c").intValue();
        String keepBanks    = parser.getOption("-keep").stringValue();
        String filterEvents = parser.getOption("-filter").stringValue();
        
        HipoFileUtils.writeHipo(outputFile, compression, keepBanks, filterEvents, inputFileList);
        /*
        if(parser.getCommand().getCommand().compareTo("-lund")==0){
            String output = parser.getCommand().getAsString("-o");
            List<String> inputList = parser.getCommand().getInputList();
            HipoFileUtils.writeLundFiles(output, inputList);
        }*/
    }
}
