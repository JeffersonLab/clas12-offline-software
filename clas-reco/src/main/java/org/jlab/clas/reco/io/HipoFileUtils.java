/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco.io;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.EventFilter;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.groot.data.DataVector;
import org.jlab.groot.data.H1F;
import org.jlab.groot.ui.TCanvas;
import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.data.HipoNode;
import org.jlab.hipo.data.HipoNodeBuilder;
import org.jlab.hipo.io.HipoReader;
import org.jlab.hipo.io.HipoWriter;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.physics.io.LundReader;
import org.jlab.utils.system.CommandLineParser;

/**
 *
 * @author gavalian
 */
public class HipoFileUtils {
    
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
        writer.addHeader("{Reconstruction-File-LUND}");
        writer.setCompressionType(2);
        writer.open(output);
        while(reader.next()==true){
            PhysicsEvent  event = reader.getEvent();
            HipoEvent   hipoEvent = new HipoEvent();
            List<HipoNode>  nodes = HipoFileUtils.getGeneratedBank(event);
            //System.out.println("nodes size = " + nodes.size());
            hipoEvent.addNodes(nodes);
            
            writer.writeEvent(hipoEvent.getDataBuffer());
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
            byte[] eventBuffer = reader.readEvent(loop);
            HipoEvent event = new HipoEvent(eventBuffer);
            PhysicsEvent physEvent = fitter.createEvent(event);
            
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
            PhysicsEvent  physEvent = fitter.createEvent(event);
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
        
        CommandLineParser parser = new CommandLineParser();
        
        parser.addCommand("-lund");
        parser.addCommand("-show");
                
        parser.getCommand("-lund").addRequiredParameter("-o", "Output HIPO file");
        
        
        
        parser.getCommand("-show").addRequiredParameter("-filter", "Event filter (example 11:2212:X+:X-:Xn)");
        parser.getCommand("-show").addRequiredParameter("-i", "Input HIPO file");
        parser.getCommand("-show").addRequiredParameter("-p", "particle to from the event (example \"[22,0]+[22,1]\")");
        parser.getCommand("-show").addRequiredParameter("-v", "property of the particle to display (example mass, px, theta)");
        parser.getCommand("-show").addOptionalParameter("-b","100", "number of bins on the plot");
        
        //parser.getCommand("-lund").addRequiredParameter("-o", "Output hipo File");
        //parser.getCommand("-lund").printUsage("hipo-utils");
        
        parser.parse(args);
        
        if(parser.getCommand().getCommand().compareTo("-lund")==0){
            if(parser.getCommand().containsRequired()==false){
                parser.getCommand().explainMissing();
                parser.getCommand().printUsage("hipo-utils");
            } else {
                String output = parser.getCommand().getAsString("-o");
                System.out.println(" output file = " + output);
                List<String> inputList = parser.getCommand().getInputList();
                //HipoFileUtils.writeLundFiles(output, inputList);
                HipoFileUtils.writeLundFilesHipo(output, inputList);
            }
        }
        
        if(parser.getCommand().getCommand().compareTo("-show")==0){
            if(parser.getCommand().containsRequired()==false){
                parser.getCommand().explainMissing();
                parser.getCommand().printUsage("hipo-utils");
            } else {
                //System.out.println(" Lanching display command");
                String filter = parser.getCommand().getAsString("-filter");
                String  input = parser.getCommand().getAsString("-i");
                String  particle = parser.getCommand().getAsString("-p");
                String  property = parser.getCommand().getAsString("-v");
                Integer  nbins  = parser.getCommand().getAsInt("-b");
                System.out.println("NUMBER OF BINS = " + nbins);
                HipoFileUtils.eventShowHipo(input, filter, particle, property, nbins);
            }
        }
        parser.getCommand().show();
        /*
        if(parser.getCommand().getCommand().compareTo("-lund")==0){
            String output = parser.getCommand().getAsString("-o");
            List<String> inputList = parser.getCommand().getInputList();
            HipoFileUtils.writeLundFiles(output, inputList);
        }*/
    }
}
