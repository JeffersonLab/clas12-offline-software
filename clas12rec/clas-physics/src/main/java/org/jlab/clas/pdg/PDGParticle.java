/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.pdg;

/**
 *
 * @author gavalian
 */
public class PDGParticle {
    
    String  particleName = "unknown";
    Integer particleId = 0;
    Integer particleIdGeant = 0;
    Double  particleMass = 0.0;
    Integer    particleCharge = -1;
    Double     particleWidth  = 0.0;
    
    public PDGParticle(String partname, int partid, double partmass)
    {
        particleName = partname;
        particleId   = partid;
        particleMass = partmass;
    }
    
    public PDGParticle(String partname, int partid, double partmass, int charge)
    {
        particleName = partname;
        particleId   = partid;
        particleMass = partmass;
        particleCharge = charge;
        particleWidth  = 0.0;
    }
    
    public PDGParticle(String partname, int partid, int geantid, double partmass, int charge)
    {
        particleName = partname;
        particleId   = partid;
        particleIdGeant = geantid;
        particleMass = partmass;
        particleCharge = charge;
        particleWidth  = 0.0;
    }
    
    public PDGParticle(String partname, int partid, double partmass, int charge, double width)
    {
        particleName = partname;
        particleId   = partid;
        particleMass = partmass;
        particleCharge = charge;
        particleWidth  = width;
    }
    
    public int charge()
    { return particleCharge;}
    
    public String name()
    {
        return particleName;
    }
    
    public int gid(){
        return particleIdGeant;
    }
    
    public int    pid()
    {
        return particleId;
    }
    
    public double mass()
    {
        return particleMass;
    }
    
    public double width()
    {
        return particleWidth;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("[%12s] pid/gid %6d [%5d] %3d mass %8.5f width %8.5f", 
                this.particleName,this.particleId,
                this.particleIdGeant, this.charge(), this.particleMass, this.particleWidth));
        return str.toString();
    }
}
