/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.mltn;

import j4ml.clas12.Clas12TrackClassifier;
import j4ml.clas12.track.ClusterCombinations;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataBank;

/**
 *
 * @author gavalian
 */
public class MLTNEngine extends ReconstructionEngine {
    
    Clas12TrackClassifier classifier = new Clas12TrackClassifier();
    
    public MLTNEngine(){
        super("MLTN","gavalian","1.0");
    }
       
    @Override
    public boolean init() {
        classifier.setEnvDirectory("CLAS12DIR");
        classifier.setEnvPath("etc/nnet/neuroph");
        classifier.load("trackClassifier.nnet", "trackFixer.nnet");
        System.out.println("Loading neural network files done...");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent de) {
        if(de.hasBank("HitBasedTrkg::HBClusters")==true){
            DataBank bank = de.getBank("HitBasedTrkg::HBClusters");
            HipoDataBank hipoBank = (HipoDataBank) bank;
            classifier.processBank(hipoBank.getBank());
            writeBank(de,classifier);
        }
        return true;
    }
    
    public void writeBank(DataEvent event, Clas12TrackClassifier cl){
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
    }
    
}
