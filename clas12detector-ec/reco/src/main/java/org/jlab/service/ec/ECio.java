/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ec;

import java.util.List;
import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */
public class ECio {
    
    public static DataBank createBankHits(List<ECStrip> strips){
        EvioDataBank bankStrips = EvioFactory.createBank("ECREC::hits", strips.size());
        return bankStrips;
    }
    
    public static DataBank createBankPeaks(List<ECPeak> peaks){
        EvioDataBank bankStrips = EvioFactory.createBank("ECREC::hits", peaks.size());
        
        return bankStrips;
    }
    
    public static DataBank createBankClusters(List<ECCluster> clusters){
        EvioDataBank bankCL = EvioFactory.createBank("ECDetector::clusters", clusters.size());
        int nrows = clusters.size();
        for(int i = 0; i < nrows; i++){
            ECCluster c = clusters.get(i);
            bankCL.setByte("sector", i, (byte) c.getPeak(0).getDescriptor().getSector());
            bankCL.setByte("layer",  i, (byte) c.getPeak(0).getDescriptor().getLayer());
            bankCL.setFloat("energy", i, (float) c.getEnergy());
            bankCL.setFloat("time", i, (float) c.getTime());
            bankCL.setFloat("X", i, (float) c.getHitPosition().x());
            bankCL.setFloat("Y", i, (float) c.getHitPosition().y());
            bankCL.setFloat("Z", i, (float) c.getHitPosition().z());
        }
        return bankCL;
    }
    
}
