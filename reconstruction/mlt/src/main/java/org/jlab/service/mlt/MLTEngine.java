/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.mlt;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataBank;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.tracking.clas.DataReader;
import org.jlab.jnp.tracking.clas.Track;
import org.jlab.jnp.tracking.clas.TrackList;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author gavalian
 */
public class MLTEngine extends ReconstructionEngine {

    private MultiLayerNetwork networkNegative = null;
    private MultiLayerNetwork networkPositive = null;
    
    public MLTEngine(){
        super("MLT","gavalian","1.0");
    }
    
    @Override
    public boolean processDataEvent(DataEvent de) {
        
        if(de.hasBank("HitBasedTrkg::HBClusters")==true){
            DataBank bank = de.getBank("HitBasedTrkg::HBClusters");
            DataReader dataReader = new DataReader();
            HipoDataBank hipoBank = (HipoDataBank) bank;
            dataReader.read( hipoBank.getBank() );
            
            TrackList tracks = dataReader.getTrackList();
            
            if(tracks.getTracks().size()>0){
                INDArray input  = tracks.getFeatures();
                INDArray output =  networkNegative.output(input);
                tracks.setProbability(output);
                
                tracks.analize();
                
                List<Track> identifiedNeg = tracks.getSelected();            
                tracks.removeSelected();
                int npos = 0;
                
                if(tracks.getTracks().size()>0){
                    INDArray inputPos  = tracks.getFeatures();
                    INDArray outputPos = networkPositive.output(inputPos);
                    
                    tracks.setProbability(outputPos);
                    tracks.analize();
                    
                    List<Track> identifiedPos = tracks.getSelected();
                    
                    npos = identifiedPos.size();
                    if(npos>0) identifiedNeg.addAll(identifiedPos);
                }
                                
                if(identifiedNeg.size()>0){                
                writeBank(de, identifiedNeg);
            }
            }
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }

    
    public void writeBank(DataEvent event, List<Track> tracks){
                
        DataBank bank = event.createBank("ai::tracks", tracks.size());
//        Bank bank = new Bank(writer.getSchemaFactory().getSchema("ai::tracks"),tracks.size());
        for(int i = 0; i < tracks.size(); i++){
            bank.setByte("id", i, (byte) (i+1));
            bank.setByte("sector", i, (byte) tracks.get(i).getSector());
            for(int c = 0; c < 6; c++){
                int order = c+1;
                bank.setShort("c"+order, i, (short) tracks.get(i).getCluster(c));
            }
        }
        event.appendBank(bank);
    }
    
    public String getEnvironment(){
        String result = System.getenv("CLAS12DIR");
        if(result==null){
            result = System.getProperty("CLAS12DIR");
        }
        return result;
    }
    
    @Override
    public boolean init() {
        String dir = this.getEnvironment();
        if(dir!=null){
            String fileNegative = dir + "/etc/nnet/dc_negative.nnet"; 
            String filePositive = dir + "/etc/nnet/dc_positive.nnet"; 

            try {
                networkNegative = MultiLayerNetwork.load(new File(fileNegative), false);
                System.out.println("LOADING AI : " + fileNegative);
                networkPositive = MultiLayerNetwork.load(new File(filePositive), false);
                System.out.println("LOADING AI : " + filePositive);
            } catch (IOException ex) {
                Logger.getLogger(MLTEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }
    
}
