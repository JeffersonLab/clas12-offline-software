package org.jlab.service.mltn;

import j4ml.clas12.ejml.ArchiveProvider;
import j4ml.clas12.ejml.EJMLTrackNeuralNetwork;
import j4ml.clas12.network.Clas12TrackFinder;
import j4ml.clas12.tracking.ClusterCombinations;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataBank;
import org.jlab.utils.CLASResources;

/**
 *
 * @author gavalian
 */
public class MLTDEngine extends ReconstructionEngine {
    

    EJMLTrackNeuralNetwork       network = null;
    private String         networkFlavor = "default";
    private Integer           networkRun = 5038;
    
    public MLTDEngine(){
        super("MLTD","gavalian","1.0");
    }
    
    @Override
    public boolean init() {
        
        networkFlavor = Optional.ofNullable(this.getEngineConfigString("flavor")).orElse("default");
        String runNumber = Optional.ofNullable(this.getEngineConfigString("run")).orElse("5038");
        networkRun = Integer.parseInt(runNumber);
        
        String path = CLASResources.getResourcePath("etc/ejml/ejmlclas12.network"); 
        if(this.getEngineConfigString("network")!=null) 
            path = this.getEngineConfigString("network");
        System.out.println("[neural-network] info : Loading neural network from " + path);
        
        network = new EJMLTrackNeuralNetwork();        
        Map<String,String>  files = new HashMap<String,String>();
        files.put("classifier", "trackClassifier.network");
        files.put("fixer", "trackFixer.network");        
        
        ArchiveProvider provider = new ArchiveProvider(path);
        
        //----- This will find in the archive the last run number closest
        //----- to provided run number that contains trained network.
        //----- it works similar to CCDB, but not exatly, for provided 
        //----- run number it looks for run that has smaller number,
        //----- however it the provided run # it lower than anything 
        //----- existing in the arhive, it will return the closest run 
        //----- number entry.
        int adjustedRun = provider.findEntry(networkRun);
        
        String directory = String.format("network/%d/%s", adjustedRun, networkFlavor);
        network.initZip(path,directory, files);
        
        //trackFinder = Clas12TrackFinder.createEJML("CLAS12DIR","etc/ejml/ejmlclas12.network");
        //classifier.setEnvDirectory("CLAS12DIR");
        //classifier.setEnvPath("etc/nnet/neuroph");
        //classifier.load("trackClassifier.nnet", "trackFixer.nnet");
        System.out.println("[neural-network] info : Loading neural network files done...");
        System.out.println("[neural-network] info : Only network is initialized...");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent de) {
        if(de.hasBank("HitBasedTrkg::Clusters")==true){
            DataBank bank = de.getBank("HitBasedTrkg::Clusters");
                        
            HipoDataBank hipoBank = (HipoDataBank) bank;
                        
            //classifier.processBank(hipoBank.getBank());
            
            /*Clas12TrackAnalyzer analyzer = new Clas12TrackAnalyzer();
            for(int sector = 1; sector <=6; sector++){
                analyzer.readBank(hipoBank.getBank(), sector);
                classifier.evaluate(analyzer.getCombinations());
                //analyzer.getCombinations().analyze();
                //System.out.println(analyzer.getCombinations());
                classifier.evaluate5(analyzer.getCombinationsPartial());
                analyzer.analyze();
            }*/            
            Clas12TrackFinder trackFinder = new Clas12TrackFinder();
            trackFinder.setTrackingNetwork(network);
            trackFinder.process(hipoBank.getBank());            
            writeBank(de,trackFinder.getResults());            
        }
        return true;
    }
    
    public void writeBank(DataEvent event, ClusterCombinations combi){
        //ClusterCombinations combi = cl.getTracks();
        //System.out.println(">>> writing ai bank with entries = " + combi.getSize());        
        DataBank bank = event.createBank("ai::tracks", combi.getSize());
        for(int i = 0; i < combi.getSize(); i++){
            bank.setByte("id", i, (byte) (i+1));
            bank.setByte("sector", i, (byte) 1);
            bank.setByte("charge", i, (byte) combi.setRow(i).getStatus());
            bank.setFloat("prob", i, (float) combi.setRow(i).getProbability());
            int[] ids = combi.getLabels(i);
            for(int c = 0; c < 6; c++){
                int order = c+1;
                bank.setShort("c"+order, i, (short) ids[c]);
            }
        }
        //System.out.println("appending bank");
        event.removeBank("ai::tracks");
        event.appendBank(bank);
    }

    /*public void writeBank(DataEvent event, Clas12TrackClassifier cl){
        ClusterCombinations combi = cl.getTracks();
        //System.out.println(">>> writing ai bank with entries = " + combi.getSize());
        DataBank bank = event.createBank("ai::tracks", combi.getSize());
        for(int i = 0; i < combi.getSize(); i++){
            bank.setByte("id", i, (byte) (i+1));
            bank.setByte("sector", i, (byte) 1);
            int[] ids = combi.getLabels(i);
            for(int c = 0; c < 6; c++){
                int order = c+1;
                bank.setShort("c"+order, i, (short) ids[c]);
            }
        }
        //System.out.println("appending bank");
        event.appendBank(bank);
    }*/
    
}
