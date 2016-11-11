/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;

/**
 *
 * @author gavalian
 */
public class EventFilter {
    
    HashMap<Integer,Integer> pParticleIDS;
    //ArrayList<ParticleOperation> pOperationCuts;
    
    int nNegativeParticlesWithPid;
    int nPositiveParticlesWithPid;
    int nNeutralParticlesWithPid;
    
    int nNegativeParticlesCount;
    int nPositiveParticlesCount;
    int nNeutralParticlesCount;
    
    int nNegativeParticlesAny;
    int nPositiveParticlesAny;
    int nNeutralParticlesAny;
    
    int nPositiveParticles;
    int nNegativeParticles;
    int nNeutralParticles;
    
    
    long numberOfChecks = 0;
    long numberOfChecksPassed = 0;
    private String filterString = "";
    
    public EventFilter(String filter){
        pParticleIDS = new HashMap<Integer,Integer>();
        //pOperationCuts = new ArrayList<ParticleOperation>();
        this.setFilter(filter);
    }
    
    public EventFilter()
    {
        pParticleIDS = new HashMap<Integer,Integer>();
        //pOperationCuts = new ArrayList<ParticleOperation>();
    }
    
    
    public void addCut(String name, String particle, double min, double max)
    {
        //pOperationCuts.add(new ParticleOperation(name,particle,min,max));                
    }
    
    public boolean checkFinalState(PhysicsEvent event)
    {
        boolean filter = true;
        
        int pc = event.countByCharge(+1);
        int pm = event.countByCharge(-1);
        int pn = event.countByCharge( 0);
        
        if(pc<this.getMinimumByCharge(+1)||pc>this.getMaximumByCharge(+1))
            return false;
        if(pm<this.getMinimumByCharge(-1)||pm>this.getMaximumByCharge(-1))
            return false;       
        if(pn<this.getMinimumByCharge( 0)||pn>this.getMaximumByCharge( 0))
            return false;
        
        Set<Integer> pKeys = this.getIdKeys();
        Iterator it = pKeys.iterator();
        while(it.hasNext())
        {
            Integer key = (Integer) it.next();
            int idcount = this.getIdCount(key);
            if(event.countByPid(key)<idcount) return false;
        }
        return filter;
    }
    
    
    public boolean checkFinalState(ParticleList plist)
    {
        boolean filter = true;
        
        int pc = plist.countByCharge(+1);
        int pm = plist.countByCharge(-1);
        int pn = plist.countByCharge( 0);
        
        if(pc<this.getMinimumByCharge(+1)||pc>this.getMaximumByCharge(+1))
            return false;
        if(pm<this.getMinimumByCharge(-1)||pm>this.getMaximumByCharge(-1))
            return false;       
        if(pn<this.getMinimumByCharge( 0)||pn>this.getMaximumByCharge( 0))
            return false;
        
        Set<Integer> pKeys = this.getIdKeys();
        Iterator it = pKeys.iterator();
        while(it.hasNext())
        {
            Integer key = (Integer) it.next();
            int idcount = this.getIdCount(key);
            if(plist.countByPid(key)<idcount) return false;
        }
        return filter;
    }
    
    public boolean isValid(PhysicsEvent event)
    {
        this.numberOfChecks++;
        boolean filter = true;
        if(checkFinalState(event)==false) return false;
        /*
        int noper = pOperationCuts.size();
        for(int loop = 0; loop < noper; loop++)
        {
            if(pOperationCuts.get(loop).isValid(event)==false) return false;
        } */
        this.numberOfChecksPassed++;
        return filter;
    }
    
    public String summary(){
        StringBuilder str = new StringBuilder();
        double ratio = 0.0;
        if(this.numberOfChecks!=0){
            ratio = ((double) this.numberOfChecksPassed)/((double)this.numberOfChecks);
        }
        str.append(String.format("*   %24s | Checks  %12d | Passed %12d | Ratio %8.3f   *",
                this.filterString,this.numberOfChecks,this.numberOfChecksPassed,
                ratio));
        return str.toString();
    }
    
    public void clear()
    {
        pParticleIDS.clear();
        nPositiveParticles = 0;
        nNegativeParticles = 0;
        nNeutralParticles  = 0;
        
        nNegativeParticlesWithPid = 0;
        nPositiveParticlesWithPid = 0;
        nNeutralParticlesWithPid  = 0;
    
        nNegativeParticlesCount = 0;
        nPositiveParticlesCount = 0;
        nNeutralParticlesCount  = 0;
    
        nNegativeParticlesAny  = 0;
        nPositiveParticlesAny  = 0;
        nNeutralParticlesAny  = 0;
    }
    
    public void addPid(String pid)
    {
        Integer partID = Integer.parseInt(pid);
        PDGParticle pdgParticle = PDGDatabase.getParticleById(partID);
        if(pdgParticle!=null)
        {
            if(pParticleIDS.containsKey(partID)==true)
            {
                Integer count = pParticleIDS.get(partID);
                count++;
                pParticleIDS.put(partID, count);
            } else {pParticleIDS.put(partID, 1);}
            int charge = pdgParticle.charge();
            if(charge==0)
            {
                nNeutralParticlesWithPid += 1;
            } else if(charge>0){
                    nPositiveParticlesWithPid += 1;
                } else {
                    nNegativeParticlesWithPid += 1;
                }
            //increaseCharge(charge,1);
        }
    }
    
    public void parseChargedSign(String charge)
    {
        int  clen    = charge.length();
        char ctrail  = charge.charAt(clen-1);
        int  count  = 0;
        
        if(clen==1)
        {
            count =1;
        } else {
            char pctrail = charge.charAt(clen-2);
            if(pctrail=='X'){
                setInclusive(ctrail);
                return;
            }
            count = Integer.parseInt(charge.substring(0, clen-1));
        }
        
        increaseChargeString(ctrail,count);
    }
    
    public int getIdCount(Integer pidKey)
    {
        return pParticleIDS.get(pidKey);
    }
    
    public Set<Integer> getIdKeys()
    {
        return pParticleIDS.keySet();
    }
    
    public void setInclusive(char charge)
    {
        switch (charge)
        {
            case '+': nPositiveParticlesAny = -1;
                break;
            case '-': nNegativeParticlesAny = -1;
                break;
            case 'n': nNeutralParticlesAny  = -1;
                break;
            default: break;
        }
    }
    
    public void increaseChargeString(char charge, int weight)
    {
        switch (charge)
        {
            case '+': nPositiveParticlesCount += weight;//increaseCharge(1,weight);
                break;
            case '-': nNegativeParticlesCount += weight;//increaseCharge(-1,weight);
                break;
            case 'n': nNeutralParticlesCount  += weight;//increaseCharge(0,weight);
                break;
            default: break;
        }
    }
    
    public void increaseCharge(int charge, int weight)
    {
        switch (charge){
            case 1: if(nPositiveParticles>=0) nPositiveParticles+=weight;
            break;
            case -1: if(nNegativeParticles>=0) nNegativeParticles+=weight;
            break;
            case 0: if(nNeutralParticles>=0) nNeutralParticles+=weight;
            break;
            default: break;
        }
    }
    
    public final void setFilter(String opt)
    {
        this.filterString = opt;
        clear();
        String[] tokens = opt.split(":");
        for(int loop = 0; loop < tokens.length; loop++)
        {
            int tokenLen = tokens[loop].length();
            if(tokenLen>0)
            {
                char trailingChar = tokens[loop].charAt(tokenLen-1);
                if(trailingChar=='+'||trailingChar=='-'||trailingChar=='n')
                {
                    parseChargedSign(tokens[loop]);
                } else {
                    addPid(tokens[loop]);
                }
            }
        }
        this.numberOfChecks = 0;
        this.numberOfChecksPassed = 0;
    }
    
    public int getMinimumByCharge(int charge)
    {
        if(charge==0)
        {
            return (nNeutralParticlesWithPid + nNeutralParticlesCount);
        } else if(charge > 0) {
            return (nPositiveParticlesWithPid + nPositiveParticlesCount);
        }
        
        return (nNegativeParticlesWithPid + nNegativeParticlesCount);            
    }
    
    public int getMaximumByCharge(int charge)
    {
        if(charge==0)
        {
            if(nNeutralParticlesAny<0){
                return 100;
            } else {
                return getMinimumByCharge(0);
            }
        }
        
        if(charge>0){
            if(nPositiveParticlesAny<0)
            {
                return 100;
            } else {
                return getMinimumByCharge(1);
            }
        }
        
        if(nNegativeParticlesAny<0)
        {
            return 100;
        } else {
            return getMinimumByCharge(-1);
        }
    }
    
    public int getCountByCharge(int charge)
    {
        if(charge==0)
        {
            return nNeutralParticles;
        } else if(charge>0) return nPositiveParticles;
        return nNegativeParticles;
    }
    
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append(String.format("FILTER:---> POSITIVE : %6d %6d, NEGATIVE : %6d %6d , "
                + "NEUTRAL : %6d %6d",
                getMinimumByCharge( 1),getMaximumByCharge( 1),
                getMinimumByCharge(-1),getMaximumByCharge(-1),
                getMinimumByCharge( 0),getMaximumByCharge( 0)));               
                //nPositiveParticles, nNegativeParticles, nNeutralParticles));
        return str.toString();
    }
    
}
