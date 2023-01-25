/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.dc.sacluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ziegler
 */
public class Link extends ArrayList<HitLet> implements Comparable<Link> {

    public Link() {
    }
    public Link(HitLet hitl0, HitLet hitl1) {
        this.add(hitl0);
        this.add(hitl1);
        _hitlets[0] = hitl0;
        _hitlets[1] = hitl1;
        double deltaLayer = (hitl1.getLayer()-hitl0.getLayer());
        double deltaWire = (hitl1.getWire()-hitl0.getWire());
        _dir = deltaWire/deltaLayer;
        _aveWire = 0.5*(hitl1.getWire()+hitl0.getWire());
        _aveLayer = 0.5*(hitl1.getLayer()+hitl0.getLayer());
        _weight =  (int)Math.sqrt(deltaLayer*deltaLayer + deltaWire*deltaWire);
        
    }   
    public Link(Link inL, Link outL) {
        HitLet hitl0 = inL.getHitlets()[0];
        HitLet hitl1 = outL.getHitlets()[1];
        this.addAll(inL);
        for(int i = 1; i<outL.size(); i++) {
            this.add(outL.get(i));
        }
        _hitlets[0] = hitl0;
        _hitlets[1] = hitl1;
        double deltaLayer = (hitl1.getLayer()-hitl0.getLayer());
        double deltaWire = (hitl1.getWire()-hitl0.getWire());
        _dir = deltaWire/deltaLayer;
        _aveWire = 0.5*(hitl1.getWire()+hitl0.getWire());
        _aveLayer = 0.5*(hitl1.getLayer()+hitl0.getLayer());
        _weight =  (int)Math.sqrt(deltaLayer*deltaLayer + deltaWire*deltaWire);
        inL.inChain=true;
        outL.inChain=true;
    }   
    private HitLet[] _hitlets = new HitLet[2]; 
    public boolean inChain = false;
    /**
     * @return the _hitlets
     */
    public HitLet[] getHitlets() {
        return _hitlets;
    }
   
    /**
     * @param _hitlets the _hitlets to set
     */
    public void setHitlets(HitLet[] _hitlets) {
        this._hitlets = _hitlets;
    }
    
    private double _dir;
    
    /**
     * @return the _dir
     */
    public double getDir() {
        return _dir;
    }

    /**
     * @param _dir the _dir to set
     */
    public void setDir(double _dir) {
        this._dir = _dir;
    }

    
    private double _aveWire;
    
    /**
     * @return the _aveWire
     */
    public double getAveWire() {
        return _aveWire;
    }

    /**
     * @param _aveWire the _aveWire to set
     */
    public void setAveWire(double _aveWire) {
        this._aveWire = _aveWire;
    }

    /**
     * @return the _aveLayer
     */
    public double getAveLayer() {
        return _aveLayer;
    }

    /**
     * @param _aveLayer the _aveLayer to set
     */
    public void setAveLayer(double _aveLayer) {
        this._aveLayer = _aveLayer;
    }

    
    private double _aveLayer;
    
    /**
     * @return the _weight
     */
    public int getWeight() {
        return _weight;
    }

    /**
     * @param _weight the _weight to set
     */
    public void setWeight(int _weight) {
        this._weight = _weight;
    }

    private int _weight;
    
    
    
    public static int nSkipLayers = 3;
    public static int ndeltaWire = 2;
    
    private static void fillHitsMap(List<HitLet> hitlets) {
        Map<Integer, List<HitLet>> hitletsmap = new HashMap<>();  
        for(HitLet h : hitlets) {
            int sl = h.getSecSuperlayer();
            if(!hitletsmap.containsKey(sl)) {
                hitletsmap.put(sl, new ArrayList<HitLet>());
                hitletsmap.get(sl).add(h);
            } else {
                hitletsmap.get(sl).add(h);
            }
        }
        
        for (Integer sl : hitletsmap.keySet()) {
            hitletsmap.get(sl).sort(Comparator.comparing(HitLet::getWire).thenComparing(HitLet::getSecLayer));
        }
    }
    
    private static Map<Integer, List<HitLet>> fillHitsSLMap(List<HitLet> hitlets) {
        Map<Integer, List<HitLet>> hitletsmap = new HashMap<>();
        for(HitLet h : hitlets) {
            int sl = h.getSecSuperlayer();
            if(!hitletsmap.containsKey(sl)) {
                hitletsmap.put(sl, new ArrayList<>());
                hitletsmap.get(sl).add(h); 
            } else {
                hitletsmap.get(sl).add(h);
            }
        }
        
        return hitletsmap;
    }
    private static List<Link> makeLinks(List<HitLet> hitlets) { // meant to run on a list sorted by sector and superlayer
        hitlets.sort(Comparator.comparing(HitLet :: getLayer));
        List<Link> linkslist = new ArrayList<>();
        for(int i = 0; i < hitlets.size(); i++) {
            List<Link> links = new ArrayList<>();
            for(int j = i; j < hitlets.size(); j++) {
                if(hitlets.get(i).isLinkable(hitlets.get(j)))
                    links.add(new Link(hitlets.get(i), hitlets.get(j)));
            }
            links.sort(Comparator.comparing(Link :: getWeight));
             // the ones with the shortest path
            if(links.isEmpty()) 
                continue;
            int w = links.get(0).getWeight();
            for(Link lk : links) {
                if(lk.getWeight()==w) 
                    linkslist.add(lk);
            }
        }
        
        return linkslist;
    }
    private static List<Link> connectLinks(List<Link> ilinks) {// meant to run on a list sorted by sector and superlayer
        List<Link> linkslist = new ArrayList<>();
        List<Link> usedlinks = new ArrayList<>();
        List<Link> links = new ArrayList();
        links.addAll(ilinks);
        for(int i = 0; i < links.size(); i++) {
            for(int j = i; j < links.size(); j++) {
                if(links.get(i).getAveLayer()>links.get(j).getAveLayer()) 
                    continue;
                if(links.get(j).isLinkable(links.get(i))) {
                    linkslist.add(new Link(links.get(i), links.get(j)));
                    usedlinks.add(links.get(i));
                    usedlinks.add(links.get(j));
                }
            }
        }
        links.removeAll(usedlinks);
        linkslist.addAll(links);
        linkslist.sort(Comparator.comparing(Link :: getAveLayer));
        return linkslist;
    }
    
    
    private static List<Link> connectAllLinks(List<Link> ilinks) {// meant to run on a list sorted by sector and superlayer
        List<Link> linkslist = connectLinks(ilinks); //triplets
        for(int i =0; i<3; i++) {
            linkslist=connectLinks(linkslist);
        }
        return linkslist;
    }        
     
    public static Map<Integer, List<Cluster> > hitletsToClusters(List<HitLet> hitls) {
        Map<Integer, List<Cluster>> clsCnds = new HashMap<>();
        Map<Integer, List<HitLet>> hitlSLmap = fillHitsSLMap(hitls); 
        for (Integer sl : hitlSLmap.keySet()) {
            List<HitLet> hitlSL = hitlSLmap.get(sl);
            List<Link> links = makeLinks(hitlSL);
            List<Link> linkedSets = connectAllLinks(links);
            List<Cluster> clusCands = linkstoClusters(linkedSets);
            if(clsCnds.containsKey(sl)) {
                clsCnds.get(sl).addAll(clusCands);
            } else {
                clsCnds.put(sl, clusCands);
            }
        }
        return clsCnds;
    }
    
    
    public static List<Cluster> linkstoClusters(List<Link> links ) {
        List<Cluster> clusters = new ArrayList<>();
        
        for(int i = 0; i < links.size(); i++) { 
            if(links.get(i).size()<3)
                continue;
            clusters.add(links.get(i).toCluster());
        }
        
        
        return clusters;
    }

    
    private boolean isLinkable(Link inLink) {
        // <--inlink-->X<--outlink-->
        //inNode layer = inLink second hitlet
        //outNode lsyer = outLink first hitlet
        //HitLet inLinkLeftHL     = inLink.getHitlets()[0];
        HitLet inLinkRightHL    = inLink.getHitlets()[1];     // <---\ connect these 2
        HitLet outLinkLeftHL    = this.getHitlets()[0];   // <---/
        //HitLet outLinkRightHL   = this.getHitlets()[1];
        
        if(inLinkRightHL.getHits()[0].getId()==outLinkLeftHL.getHits()[0].getId() && 
                inLinkRightHL.getHits()[1].getId()==outLinkLeftHL.getHits()[1].getId()) 
            return true; 
        
        return false;
    }
    
    @Override
    public int compareTo(Link o) {
        
        int CompSecSly = this.getHitlets()[0].getSecSuperlayer() < o.getHitlets()[0].getSecSuperlayer() ? 0 : 1;
        int CompWire = this.getHitlets()[0].getWire() < o.getHitlets()[0].getWire() ? 0 : 1;
        
        return ((CompWire == 0) ? CompSecSly : CompWire);

    }

    private Cluster toCluster() {
        Cluster cl = new Cluster(this);
        
        return cl;
    }

    
}
