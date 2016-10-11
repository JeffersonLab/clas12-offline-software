/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.groot.data.H1F;
import org.jlab.groot.tree.Tree;
import org.jlab.groot.ui.TCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class EventTree {
    
    private Map<String,EventTreeBranch>  treeBranches = new LinkedHashMap<String,EventTreeBranch>();
    private Tree treeObject = new Tree("Event");
    private GenericKinematicFitter kinFitter = new GenericKinematicFitter(11);
    
    public EventTree(){
        
    }
    
    public void addBranch(String name, String filter){
        EventTreeBranch branch = new EventTreeBranch(name);
        branch.setFilter(filter);        
        treeBranches.put(name, branch);
    }
    
    public void addLeaf(String branch, String name, String expression, String... properties){
        treeBranches.get(branch).addLeaf(name, expression, properties);        
    }
    
    public void initTree(){
        treeObject = new Tree("EventTree");
        for(Map.Entry<String,EventTreeBranch> entry : treeBranches.entrySet()){
            Map<String,EventTreeLeaf>  leafs = entry.getValue().getLeafs();
            for(Map.Entry<String,EventTreeLeaf> item : leafs.entrySet()){
                List<String> properties = item.getValue().getProperties();
                for(int i = 0; i < properties.size();i++){
                    String branch = item.getValue().getLeafPropertyName(i);
                    treeObject.addBranch(branch, "", "GeV");
                }
            }
        }
        treeObject.print();
    }
    
    public void processEvent(DataEvent event){
        RecEvent  recEvent = kinFitter.getRecEvent(event);
        recEvent.doPidMatch();
        treeObject.resetBranches(-1000.0);
        for(Map.Entry<String,EventTreeBranch> entry : treeBranches.entrySet()){
            if(entry.getValue().getFilter().isValid(recEvent.getReconstructed())==true){
                //System.out.println("passed the filter");
                Map<String,EventTreeLeaf>  leafs = entry.getValue().getLeafs();
                for(Map.Entry<String,EventTreeLeaf> item : leafs.entrySet()){
                    List<String> properties = item.getValue().getProperties();
                    Particle p = recEvent.getReconstructed().getParticle(item.getValue().getExpression());
                    for(int i = 0; i < properties.size(); i++){                        
                        String branch = item.getValue().getLeafPropertyName(i);
                        double value = p.get(properties.get(i));
                        treeObject.getBranch(branch).setValue(value);
                    }
                }
            }
        }
        //treeObject.print();
    }
    
    public Tree getTree(){return treeObject;}
    
    public static class EventTreeBranch {
        
        private String branchName = "";
        private final EventFilter   branchFilter = new EventFilter();        
        private final Map<String,EventTreeLeaf>  branchLeafs = new LinkedHashMap<String,EventTreeLeaf>();
                
        public EventTreeBranch(String name){
            branchName = name;
        }
        
        public EventTreeBranch(String name,String filter){
            branchName = name;
            branchFilter.setFilter(filter);
        }
        
        public EventTreeBranch(){
            
        }
        
        public Map<String,EventTreeLeaf>  getLeafs(){
            return branchLeafs;
        }
        
        public void addLeaf(String name, String expression, String... properties){
            EventTreeLeaf  leaf = new EventTreeLeaf(name,expression);
            leaf.addProperties(properties);
            leaf.setParent(branchName);
            branchLeafs.put(leaf.getName(), leaf);
        }
        
        public void setName(String name){ branchName = name;}
        public String getName(){return branchName;}
        public EventFilter getFilter(){return branchFilter;}
        public void setFilter(String filter) { branchFilter.setFilter(filter);}
        
    }
    
    public static class EventTreeLeaf {
        
        String leafName       = "";
        String leafExpression = "";
        String parentBranch   = "";
        
        List<String>  leafProperties = new ArrayList<String>();
        
        public EventTreeLeaf(String name, String exp){
            leafName = name;
            leafExpression = exp;
        }
        
        public void addProperties(String... properties){
            for(String property : properties){
                leafProperties.add(property);
            }
        }
        
        
        public void addProperty(String property){
            leafProperties.add(property);
        }
        
        public List<String> getProperties(){
            return leafProperties;
        }
        
        public String getLeafPropertyName(int index){
            return String.format("%s:%s.%s", parentBranch,leafName,leafProperties.get(index));
        }
        
        public String getExpression(){return leafExpression;}
        
        public String getParent(){ return parentBranch;}
        public void   setParent(String parent) {parentBranch = parent;}
        public void setName(String name){ leafName = name;}
        public String getName(){return leafName;}
        
        public List<String> getTreeBranches(String prefix){
            List<String> branches = new ArrayList<String>();
            for(String property : leafProperties){
                branches.add(String.format("%s:%s.%s", prefix,leafName,property));
            }
            return branches;
        }
    }
    
    
    public static void main(String[] args){
        
        EventTree  evtTree = new EventTree();
        
        evtTree.addBranch("DVPI0", "11:2212:X+:X-:Xn");
        
        evtTree.addLeaf("DVPI0", "mxEp", "[b]+[t]-[11]-[2212]","mass","theta","phi");
        evtTree.addLeaf("DVPI0", "proton", "[2212]","p","theta","phi");
        
        evtTree.initTree();
        
        HipoDataSource reader = new HipoDataSource();
        
        reader.open("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/Debugging/eppi0_rec_central_DST.hipo");
        H1F h1 = new H1F("h1",80,0.0,0.85);
        for(int i = 0; i < 36000; i++){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            evtTree.processEvent(event);
            double mass = evtTree.getTree().getBranch("DVPI0:mxEp.mass").getValue().doubleValue();
            double theta = evtTree.getTree().getBranch("DVPI0:proton.theta").getValue().doubleValue();
            double p = evtTree.getTree().getBranch("DVPI0:proton.p").getValue().doubleValue();
            if(theta*57.29>45.0&&p>0.8)
                h1.fill(mass);
        }
        
        TCanvas c1 = new TCanvas("c1",500,500);
        c1.draw(h1);
    }
}
