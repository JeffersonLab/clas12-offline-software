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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.jlab.groot.data.DataVector;
import org.jlab.groot.data.H1F;
import org.jlab.groot.studio.StudioUI;
import org.jlab.groot.tree.DynamicTree;
import org.jlab.groot.tree.Tree;
import org.jlab.groot.tree.TreeProvider;
import org.jlab.groot.ui.TCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class EventTree extends Tree implements TreeProvider {
    
    private Map<String,EventTreeBranch>  treeBranches = new LinkedHashMap<String,EventTreeBranch>();
    private Tree treeObject = new Tree("Event");
    private GenericKinematicFitter kinFitter = new GenericKinematicFitter(11);
    private HipoDataSource reader = null;
    private int            currentEvent = 0;
    
    public EventTree(){
        super("EventTree");
    }
    
    @Override
    public void reset(){
        currentEvent = 0;
        reader.gotoEvent(0);
    }
    
    @Override
    public int getEntries(){
        return this.reader.getSize();
    }
    
    @Override
    public int readEntry(int entry) {
        DataEvent event = reader.gotoEvent(entry);
        if(event==null){
            System.out.println(" NULL event for entry #" + entry);
        } else {
            this.processEvent(event);
        }
        return 1;
    }
    
    @Override
    public boolean readNext() {
        if(reader.hasEvent()==false) return false;        
        DataEvent event = reader.getNextEvent();
        System.out.println("reading event");
        this.processEvent(event);
        return true;
    }
    
    public void setSource(String filename){
        reader = new HipoDataSource();
        reader.open(filename);
    }
    
    public void addBranch(String name, String filter){
        EventTreeBranch branch = new EventTreeBranch(name);
        branch.setFilter(filter);
        if(treeBranches.containsKey(name)==false){
            treeBranches.put(name, branch);
        } else {
            System.out.println("TREE Branch already exists : " + name);
        }
    }
    
    public void addLeaf(String branch, String name, String expression, String... properties){
        treeBranches.get(branch).addLeaf(name, expression, properties);        
    }
    
    public void initTree(){
        //treeObject = new Tree("EventTree");
        for(Map.Entry<String,EventTreeBranch> entry : treeBranches.entrySet()){
            Map<String,EventTreeLeaf>  leafs = entry.getValue().getLeafs();
            for(Map.Entry<String,EventTreeLeaf> item : leafs.entrySet()){
                List<String> properties = item.getValue().getProperties();
                for(int i = 0; i < properties.size();i++){
                    String branch = item.getValue().getLeafPropertyName(i);
                    this.addBranch(branch, "", "GeV");
                }
            }
        }
        treeObject.print();
    }
    
    public void processEvent(DataEvent event){
        
        RecEvent  recEvent = kinFitter.getRecEvent(event);
        recEvent.doPidMatch();
        this.resetBranches(-1000.0);
        EventFilter  filter = new EventFilter("11:2112:321:Xn:X-:X+");
        for(Map.Entry<String,EventTreeBranch> entry : treeBranches.entrySet()){
            if(entry.getValue().getFilter().isValid(recEvent.getReconstructed())==true
                    &&filter.isValid(recEvent.getGenerated())){
                //System.out.println("passed the filter");
                Map<String,EventTreeLeaf>  leafs = entry.getValue().getLeafs();
                for(Map.Entry<String,EventTreeLeaf> item : leafs.entrySet()){
                    List<String> properties = item.getValue().getProperties();
                    Particle p = recEvent.getReconstructed().getParticle(item.getValue().getExpression());
                    for(int i = 0; i < properties.size(); i++){                        
                        String branch = item.getValue().getLeafPropertyName(i);
                        double value = p.get(properties.get(i));
                        this.getBranch(branch).setValue(value);
                    }
                }
            }
        }
        //treeObject.print();
    }

    public Tree tree() {
        return this;
    }

    public TreeModel getTreeModel() {
        return null;
    }

    @Override
    public DynamicTree getDynamicTree() {
        DynamicTree tree = new DynamicTree(getName());

        //roota.add(new DefaultMutableTreeNode("e-") );
        //tree.addObject(rootb, "e+");        
        //tree.addObject(rootbranch);
        for(Map.Entry<String,EventTreeBranch> entry : this.treeBranches.entrySet()){
            String name = entry.getKey();
            DefaultMutableTreeNode base = tree.addObject( new DefaultMutableTreeNode(name));
            //tree.addObject(base);
            System.out.println("creating base = " + name);
            EventTreeBranch branch = entry.getValue();
            for(Map.Entry<String,EventTreeLeaf> item : branch.getLeafs().entrySet()){
                DefaultMutableTreeNode particle = tree.addObject(base, item.getValue().getName(),true);
                
                System.out.println("adding leaf = " + item.getValue().getName());
                item.getValue().getProperties();
                for(String property : item.getValue().getProperties()){
                    System.out.println("adding property = " + property);
                    tree.addObject(particle, property);
                }
            }
            //root.add(base);
        }
        //tree.addObject(root);
        
        return tree;
    }
    
    public List<DataVector> actionTreeNode(TreePath[] path, int limit) {
        List<DataVector> result = new ArrayList<DataVector>();
        //System.out.println("Got callback to compute something");
        boolean pathOK = true;
        
        for(TreePath p : path){
            if(p.getPathCount()!=4) pathOK = false;
        }
        
        if(pathOK==false) return result;
        
        List<String>  variables = new ArrayList<String>();
        
        for(int i = 0; i < path.length; i++){
             String branch = path[i].getPathComponent(1).toString() + "_" 
                    + path[i].getPathComponent(2).toString() + "_" 
                    + path[i].getPathComponent(3).toString();
             variables.add(branch);
             result.add(new DataVector());
        }
        
        int counter = 0;
        int nevents = reader.getSize();
        for(int i = 0; i < nevents; i++){
                
            DataEvent event = reader.gotoEvent(i);
            if(event!=null){
                counter++;
                this.processEvent(event);
                boolean addEvent = true;
                
                for(int k = 0; k < variables.size(); k++){
                    if(this.getBranch(variables.get(k)).getValue().doubleValue()<-500) addEvent = false;
                }
                if(addEvent==true){
                    for(int k = 0; k < variables.size(); k++){
                        result.get(k).add(this.getBranch(variables.get(k)).getValue().doubleValue());
                    }
                }
            } 
            if(limit>0&&counter>limit) break;
        }
        return result;
    }

    public JDialog treeConfigure() {
        
        String[] options = new String[]{"mass","p","theta","phi","px","py","pz","mass2"};
        JComboBox propertyOne    = new JComboBox(options);
        JComboBox propertyTwo    = new JComboBox(options);
        JComboBox propertyThree  = new JComboBox(options);
        JTextField  textName     = new JTextField();
        JTextField  textFilter   = new JTextField();
        JTextField  textGenFilter   = new JTextField();
        JTextField  textParticle = new JTextField();
        JTextField  textExpression = new JTextField();
        textName.setText("INCLUSIVE");
        textGenFilter.setText("11:X+:X-:Xn");
        textFilter.setText("11:X+:X-:Xn");
        textParticle.setText("electron");
        textExpression.setText("[11]");
        propertyOne.setSelectedIndex(0);
        propertyTwo.setSelectedIndex(1);
        propertyThree.setSelectedIndex(2);
        
        Object[] message = {
            "Name : ", textName,
            "Generated Filter : ", textGenFilter,
            "Event Filter : ", textFilter,
            "Particle : ", textParticle, 
            "Expression : ", textExpression, 
            "property :", propertyOne,
            "property :", propertyTwo,
            "property :", propertyThree
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Add Particle", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String strOne = (String) propertyOne.getSelectedItem();
            String strTwo = (String) propertyTwo.getSelectedItem();
            String strThree = (String) propertyThree.getSelectedItem();
            String name   = textName.getText();
            String filter = textFilter.getText();
            String particle = textFilter.getText();
            String expression = textExpression.getText();
            this.addBranch(name, filter);
            this.addLeaf(name, particle, expression, new String[]{strOne,strTwo,strThree});
            //this.getCanvas().divide(Integer.parseInt(stringCOLS), Integer.parseInt(stringROWS));
            System.out.println(" ADDING BRANCH ----> ");
        }

        return new JDialog();
    }
    
    //public Tree getTree(){return treeObject;}
    
    /**
     * class describing EventTree branch
     */
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
            return String.format("%s_%s_%s", parentBranch,leafName,leafProperties.get(index));
        }
        
        public String getExpression(){return leafExpression;}
        
        public String getParent(){ return parentBranch;}
        public void   setParent(String parent) {parentBranch = parent;}
        public void setName(String name){ leafName = name;}
        public String getName(){return leafName;}
        
        public List<String> getTreeBranches(String prefix){
            List<String> branches = new ArrayList<String>();
            for(String property : leafProperties){
                branches.add(String.format("%s_%s_%s", prefix,leafName,property));
            }
            return branches;
        }
    }
    
    
    public static void main(String[] args){
        
        EventTree  evtTree = new EventTree();

        evtTree.addBranch("LAMBDA", "-211:2212:X+:X-:Xn");  
        evtTree.addLeaf("LAMBDA", "Mppi", "[-211]+[2212]","mass2","mass","theta","phi");
        evtTree.addBranch("LAMBDA_EK", "11:321:X+:X-:Xn");
        evtTree.addLeaf("LAMBDA_EK", "MxeK", "[b]+[t]-[11]-[321]","mass2","mass","theta","phi");
        evtTree.addBranch("LAMBDA_EKG", "11:321:22:X+:X-:Xn");
        evtTree.addLeaf("LAMBDA_EKG", "MxeK", "[b]+[t]-[11]-[321]","mass2","mass","theta","phi");
        evtTree.addBranch("LAMBDA_EKNG", "11:321:X+:X-");
        evtTree.addLeaf("LAMBDA_EKNG", "MxeK", "[b]+[t]-[11]-[321]","mass2","mass","theta","phi");
        //evtTree.addLeaf("DVPI0", "proton", "[2212]","px","py","pz");
        evtTree.initTree();
        //evtTree.treeConfigure();
        evtTree.setSource("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/datasets/reco/reco_eklambda_dst.hipo");
        //evtTree.initTree();
        
        
        /*
        DynamicTree tree = evtTree.getDynamicTree();
        JFrame frame = new JFrame();
        frame.add(tree);
        frame.pack();
        frame.setVisible(true);
        */
        StudioUI studio = new StudioUI(evtTree);
        /*
        HipoDataSource reader = new HipoDataSource();
        //reader.open("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/Debugging/eppi0_rec_central_DST.hipo");        
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
        c1.draw(h1);*/
    }
}
