/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco.io;

import java.util.List;
import org.jlab.clas.physics.EventFilter;
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.groot.data.DataVector;
import org.jlab.groot.data.H1F;
import org.jlab.groot.ui.TCanvas;
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
                HipoFileUtils.writeLundFiles(output, inputList);
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
                HipoFileUtils.eventShow(input, filter, particle, property, nbins);
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
