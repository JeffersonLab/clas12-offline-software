/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import org.jlab.groot.tree.Tree;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.physics.analysis.PhysicsAnalysis;

/**
 *
 * @author gavalian
 */
public class AnalysisTree extends Tree {

    private PhysicsAnalysis  physAnalysis = new PhysicsAnalysis();
    private HipoDataSource   hipoReader   = null;
    private GenericKinematicFitter fitter = new GenericKinematicFitter(11.0);
    
    public AnalysisTree(){
        super("CLAS12AnalysisTree");
    }
    
    public void setSource(String filename){
        hipoReader = new HipoDataSource();
        hipoReader.open(filename);
    }
    
    
    @Override
    public boolean readNext(){
        if(hipoReader.hasEvent()==false) return false;
    
        EvioDataEvent event     = (EvioDataEvent) hipoReader.getNextEvent();
        PhysicsEvent  physEvent = fitter.getPhysicsEvent(event);
        
        physAnalysis.processEvent(physEvent);
        System.out.println(physAnalysis.toString());
        return true;
    }
    
    
}
