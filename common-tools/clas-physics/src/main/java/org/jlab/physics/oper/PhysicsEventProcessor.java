/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.oper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jlab.clas.physics.EventFilter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class PhysicsEventProcessor {
    
    private final TreeMap<String,PhysicsEventOperator>  operators = new TreeMap<String,PhysicsEventOperator>();
    private final TreeMap<String,PhysicsParticleDescriptor>  particleDescriptors = 
            new TreeMap<String,PhysicsParticleDescriptor>();
    private final TreeMap<String,PhysicsCutDescriptor>  cutDescriptors = 
            new TreeMap<String,PhysicsCutDescriptor>();
    
    private final TreeMap<String,PhysicsHistogramDescriptor>  histDescriptors =
            new TreeMap<String,PhysicsHistogramDescriptor>();
    
    private Particle beamParticle = new Particle(11,0.0,0.0,11.0,0.0,0.0,0.0);
    private EventFilter    eventFilter = new EventFilter();
    
    
    public PhysicsEventProcessor(double energy, String filter){
        beamParticle.setVector(11, 0.0,0.0,energy,0.0,0.0,0.0);
        eventFilter.setFilter(filter);
    }
    
    
    public void addParticle(String name,String particle, String variable){
        this.particleDescriptors.put(name, new PhysicsParticleDescriptor(name,particle,variable));
    }
    
    public void addCut(String cutname, String variable, double min, double max){
        this.cutDescriptors.put(cutname, new PhysicsCutDescriptor(cutname,variable,min,max));
    }
    
    public void parseLine(String line){
        String[] tokens = line.split("\\s+");
        
        if(tokens.length>0){
            
            if(tokens[0].compareTo("PARTICLE")==0){
                this.addParticle(tokens[1], tokens[2], tokens[3]);
            }
            
            if(tokens[0].compareTo("CUT")==0){
                this.addCut(tokens[1], tokens[2], 
                        Double.parseDouble(tokens[3]),
                        Double.parseDouble(tokens[4])
                );
            }
            
            if(tokens[0].compareTo("HIST")==0){
                this.addHistogram(tokens[1], Integer.parseInt(tokens[2]), 
                        Double.parseDouble(tokens[3]),
                        Double.parseDouble(tokens[4]),
                        tokens[5],tokens[6]
                );
            }
        }
    }
    
    public void addHistogram(String name, int nbins, double min, double max,
            String var, String cuts){
        String[] tokens = cuts.split("&");
        
        if(this.particleDescriptors.containsKey(var)==false){
            System.out.println("[PHYS-PROC] --->  warning : adding histogram ["
                    + name + "] unsuccessful. no variable [" + var + "] is defined");
            return;
        }
        
        PhysicsHistogramDescriptor  desc = new PhysicsHistogramDescriptor(
                name,nbins,min,max,var,"");
        //System.out.println("ADDING CUTS SIZE TOKENS = " + tokens.length);
        desc.getCuts().clear();
        for(String item : tokens){
            if(this.cutDescriptors.containsKey(item)==true){
                desc.addCut(item);
                //System.out.println(" adding cut ["+item+"]");
            } else {
                System.out.println("[PHYS-PROC] --->  warning : adding "
                + " cut ["+ item+"] to histogram ["+name+"] failed. not cut with that name found");
            }
        }
        this.histDescriptors.put(name, desc);
    }
    
    public void addOperator(PhysicsEventOperator oper){
        this.operators.put(oper.getName(), oper);
    }
    
    public boolean isEventValid(PhysicsEvent event){
        return eventFilter.isValid(event);
    }
    
    public void apply(PhysicsEvent event){
        //for(Map.Entry<String,PhysicsEventOperator> entry : this.operators.entrySet()){
        //    entry.getValue().apply(event);
        //}
        event.setBeamParticle(beamParticle);
        for(Map.Entry<String,PhysicsParticleDescriptor> desc : this.particleDescriptors.entrySet()){
            desc.getValue().applyEvent(event);
        }
        
    }
    
    public PhysicsEventOperator getOperator(String name){
        return this.operators.get(name);
    }
    
    public boolean processCuts(List<String> cutList){
        for(int loop = 0; loop < cutList.size(); loop++){
            if(this.cutDescriptors.get(cutList.get(loop)).isValid(particleDescriptors)==false)
                return false;
        }
        return true;
    }
    
    public List<String>  getOperatorList(){
        ArrayList<String>  opList = new ArrayList<String> ();
        for(Map.Entry<String,PhysicsEventOperator> entry : this.operators.entrySet()){
            opList.add(entry.getKey());
        }
        return opList;
    }
    
    public double[]  getOperatorValues(){
        double[] values = new double[this.operators.size()];
        int counter = 0;
        for(Map.Entry<String,PhysicsEventOperator> entry : this.operators.entrySet()){
            values[counter] = entry.getValue().getValue();
            counter++;
        }
        return values;
    }
    
    public boolean isValid(String oper){
        if(this.operators.containsKey(oper)==false) return false;
        PhysicsEventOperator evOper = this.operators.get(oper);
        for(String cut : evOper.getCuts()){
            if(this.operators.containsKey(cut)==true){
                if(this.operators.get(cut).isValid()==false) return false;
            } else {
                System.out.println("[PhysicsEventProcessor] ERROR : processing operator [" 
                        + oper + "]. Could not find cut named [" + cut + "]" );
            }
        }
        return true;
    }
    public void showData(){
        StringBuilder str = new StringBuilder();
        //for(Map.Entry<String,PhysicsEventOperator> entry : this.operators.entrySet()){
        //    str.append(entry.getValue().stringValue());
        //    str.append("\n");
        //}
        for(Map.Entry<String,PhysicsParticleDescriptor> desc : this.particleDescriptors.entrySet()){
            str.append(String.format("* %-14s * %-12s * %32s * %12.5f *\n", 
                    desc.getValue().getName(),
                    desc.getValue().getVariable(),
                    desc.getValue().getParticle(),desc.getValue().getValue()));
        }
        System.out.println(str.toString());
    }
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        //for(Map.Entry<String,PhysicsEventOperator> entry : this.operators.entrySet()){
        //    str.append(entry.getValue().stringValue());
        //    str.append("\n");
        //}
        for(Map.Entry<String,PhysicsParticleDescriptor> desc : this.particleDescriptors.entrySet()){
            str.append("[VAR ] --> ");
            str.append(desc.getValue().toString());                    
        }
        
        for(Map.Entry<String,PhysicsCutDescriptor> desc : this.cutDescriptors.entrySet()){
            str.append("[CUT ] --> ");
            str.append(desc.getValue().toString());                    
        }
        
        for(Map.Entry<String,PhysicsHistogramDescriptor> desc : this.histDescriptors.entrySet()){
            str.append("[HIST] --> ");
            str.append(desc.getValue().toString());                    
        }
        
        return str.toString();
    }
}
