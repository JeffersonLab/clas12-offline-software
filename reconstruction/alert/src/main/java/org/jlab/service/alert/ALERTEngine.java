package org.jlab.service.alert;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;

public class ALERTEngine extends ReconstructionEngine{


    public ALERTEngine() {
        super("ALERT","mpaolone","1.0");
    }

    @Override
    public boolean init() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        
        return true;
    }

    public static void main(String[] args){
        
    }
}
