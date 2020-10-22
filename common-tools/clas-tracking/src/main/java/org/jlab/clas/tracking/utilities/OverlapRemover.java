/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jlab.clas.tracking.objects.TObject;
import org.jlab.clas.tracking.trackrep.Seed;

/**
 *
 * @author ziegler
 */
public class OverlapRemover {
    
    private boolean contains(Seed s0, List<Seed> set) {        
        boolean isc = false;
        List<TObject> c0 = s0.getTObjects();        
        c0.sort(Comparator.comparing(TObject::getLayer).thenComparing(TObject::getR).thenComparing(TObject::getZ));
        
        for(int i = 0; i < set.size(); i++) {
            List<TObject> c1 = set.get(i).getTObjects();
            c1.sort(Comparator.comparing(TObject::getLayer).thenComparing(TObject::getR).thenComparing(TObject::getZ));
            
            if(c1.size() == c0.size()) {
                int n = 0;
                for(int k =0; k < c1.size(); k++) {
                    if(c1.get(k).equals(c0.get(k)))
                        n++;
                }
                if(n == c1.size())
                    isc = true;
            }
        }
        
        return isc;
    }
    
    private boolean equals(List<TObject> c0, List<TObject> c1) {
        boolean isc = false;
        
        c0.sort(Comparator.comparing(TObject::getLayer).thenComparing(TObject::getR).thenComparing(TObject::getZ));
        c1.sort(Comparator.comparing(TObject::getLayer).thenComparing(TObject::getR).thenComparing(TObject::getZ));
        if(c1.size() == c0.size()) {
            int n = 0;
            for(int k =0; k < c1.size(); k++) {
                if(c1.get(k).equals(c0.get(k)))
                    n++;
            }
            if(n == c1.size())
                isc = true;
        }
        
        return isc;
    }
    
    private List<TObject> findOverlapObjects(List<TObject> c0, List<TObject> c1) {
        List<TObject> covrl = new ArrayList<TObject>();
        c0.sort(Comparator.comparing(TObject::getLayer).thenComparing(TObject::getR).thenComparing(TObject::getZ));
        c1.sort(Comparator.comparing(TObject::getLayer).thenComparing(TObject::getR).thenComparing(TObject::getZ));
        
        //List<Cross> largerCList = ((c0.size()>c1.size()) ? c0 : c1);
        for(TObject ci : c0) {
            for(TObject cj : c1) {
                if(ci.equals(cj)) {
                    covrl.add(cj);
                }
            }
        }
        System.out.println("Comparing lists ");
        for(TObject c : c0) {
            System.out.println("c0: "+c.getX());
        }
        for(TObject c : c1) {
            System.out.println("c1: "+c.getX());
        }
        for(TObject c : covrl) {
            System.out.println("covrl: "+c.getX());
        }
        return covrl;
    }
    private boolean equalLists(List<TObject> c0, List<TObject> c1) {      
        if(c0.size()!=c1.size()) {
            return false;
        }
        boolean eq = true;
        for(int i = 0; i < c0.size(); i++) {
            if(c0.get(i).equals(c1.get(i))) { //un-matched objects
                eq = false;
            }
        }
        return eq;
    }
    
    public void flagOverlap(Seed s0, Seed s1) {      
        List<TObject> ovrl = this.findOverlapObjects(s0.getTObjects(), s1.getTObjects());
        if(ovrl == null) {
            return;
        }
        if(ovrl.size()==s0.size()) {
            s0.setOverlapFlag(1);
        } else {
            s1.setOverlapFlag(1);
        }
    }
    
    public void removeDuplicates(List<Seed> seeds) {
        List<Seed> dupl = new ArrayList<Seed>();
        for(int i = 0; i < seeds.size(); i++) {
            Seed ci = seeds.get(i);
            seeds.remove(i); //exlude it from list
            if(this.contains(ci, seeds)) {
                dupl.add(ci); 
            }
            seeds.add(ci);  //put it back
        }
        seeds.removeAll(dupl);
    }
    
    public void flagOverlaps(List<Seed> seeds) {
        //clean up the list
        this.removeDuplicates(seeds);
        //flag overlaps
        for(int i = 0; i < seeds.size()-1; i++) {
            for(int j = i+1; j < seeds.size(); j++) {
                this.flagOverlap(seeds.get(i), seeds.get(j));
            }
        }
    }
}
