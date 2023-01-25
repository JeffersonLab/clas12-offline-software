/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.dc.sacluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author ziegler
 */
public class Clusterer {
    
    private static List<Hit> getHitsFromBank(DataBank bankDGTZ) {
        List<Hit> hits = new ArrayList<>();
        
        int rows = bankDGTZ.rows();
        for (int i = 0; i < rows; i++) {
            int sector     = bankDGTZ.getByte("sector", i);
            int layer      = bankDGTZ.getByte("layer", i);
            int wire       = bankDGTZ.getShort("component", i);
            int order      = bankDGTZ.getByte("order", i);
            if(isDenoised(order, SAClustering.p) )
                hits.add(new Hit(i+1, sector, layer, wire, order) );
        }
        return hits;
    }
    
    public  static List<Cluster> clusters = new ArrayList<>();
    public static  List<Hit> hits = new ArrayList<>();;
    public static Map<Integer, Integer> indexMap = new HashMap<>();
    static String tdcBank    = "DC::tdc";
    static void getClusters(DataEvent event) {
        indexMap.clear();
        hits.clear();
        clusters.clear();
        DataBank bankDGTZ = event.getBank(tdcBank);
        hits = getHitsFromBank(bankDGTZ);
        List<HitLet> hitlets = HitLet.makeListOfDoublets(hits);
        Map<Integer, List<Cluster> > clusMap = Link.hitletsToClusters(hitlets);
        for (Integer sl : clusMap.keySet()) {
            clusters.addAll(clusMap.get(sl));
        }
        int cid=1;
        for(Cluster cl : clusters) {
            cl.setId(cid);
            for(Hit h : cl ) {
                int index = h.getId()-1;
                h.setCid(cid);
                indexMap.put(index, cid);
            }
            cid++;
        }
        for (Integer idx : indexMap.keySet()) {
            int value = indexMap.get(idx);
            bankDGTZ.setByte("order", idx, (byte) value);
           
        }
        
    }

    private static boolean isDenoised(int order, int[] p) {
        for(int i =0; i<p.length; i++) {
            if(order==p[i]) 
                return true;
        }
        return false;
    }
    
    
}
