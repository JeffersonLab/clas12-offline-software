/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.physics.base;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class ParticleSelector {
    
    private Integer   particleID   = 11;
    private Integer   particleSkip = 0;
    private Integer   particleSign = 1;
    private String    particleType = "11";
    private Boolean   overridePid  = false;
    private Integer   overrideParticleID = 11;
    private String    selectorFormat     = "[11,0]";
    private final  char      bracketsOpen        = '[';
    private final  char      bracketsClose       = ']';
    
    public ParticleSelector(String format){
        this.parse(format);
    }
    
    public ParticleSelector(){
        
    }
    
    public ParticleSelector(int pid, int skip, int sign){
        particleID   = pid;
        particleSkip = skip;
        particleSign = sign;
    }
    
    private boolean parseParticleID(String text){
        //System.err.println("[DEBUG] ---> parsing particle ID = " + text);
        if(text.length()==1&&(text.compareTo("+")==0
                ||text.compareTo("-")==0||text.compareTo("n")==0)){
            particleID   = 0;
            particleType = text;
            return true;
        }
        
        if(text.compareTo("b")==0){
            particleID   = 5000;
            particleType = "beam";
            return true;
        }
        
        if(text.compareTo("t")==0){
            particleID   = 5001;
            particleType = "target";
            return true;
        }
        
        //System.err.println("TEXT = " + text + "  Check = " + text.matches("[0-9]+"));
        
        if (text.matches("-?[0-9]+") == true && text.length() > 0){
            particleID   = Integer.parseInt(text);
            particleType = text;
            return true;
        } else {
            PDGParticle particle = PDGDatabase.getParticleByName(text);
            if(particle!=null){
                particleID   = particle.pid();
                particleType = text; 
                return true;
            } else {
                return false;
            }            
            //System.err.println("Parsing as number : " + text);            
        }
    }
    
    private void reset(){
        this.overridePid = false;
        this.overrideParticleID = 11;
        this.particleID         = 11;
        this.particleType       = "e-";
        this.particleSkip       = 0;
        this.particleSign       = 1;
    }
    
    public final void parse(String options){
        this.reset();
        selectorFormat = options.replaceAll("\\s+", "");
        String trimmed;
        if(selectorFormat.charAt(1)==this.bracketsOpen&&
                selectorFormat.charAt(selectorFormat.length()-1)==this.bracketsClose){
            if(selectorFormat.charAt(0)=='-'){
                this.particleSign = -1;                
            } else {this.particleSign = 1;}
            
            trimmed = selectorFormat.substring(2, selectorFormat.length()-1);
        } else if(selectorFormat.charAt(0)==this.bracketsOpen&&
                selectorFormat.charAt(selectorFormat.length()-1)==this.bracketsClose){
            trimmed = selectorFormat.substring(1, selectorFormat.length()-1);
            this.particleSign = 1;
        } else {
            System.err.println("[ParticleSelector] ---> Syntax error. in string ("
            +this.selectorFormat + ").");
            return;
        }
        
        //trimmed = selectorFormat.substring(2, selectorFormat.length()-1);
        //System.err.println("[DEBUG]--> trimmed string = (" + trimmed + ")");
        String[] tokens = trimmed.split(",");
        if(tokens.length>0){
            this.parseParticleID(tokens[0]);
            this.overrideParticleID = this.particleID;
        }
        
        if(tokens.length>1){
            particleSkip = Integer.parseInt(tokens[1]);
        } else {
            particleSkip = 0;
        }
        
        if(tokens.length>2){
            overrideParticleID = Integer.parseInt(tokens[2]);
            this.overridePid   = true;
        }
        
    }
    
    public int pid(){return particleID;}
    public int skip(){return particleSkip;}
    
    public Particle getParticle(PhysicsEvent event){
        try {
        if(particleID==5000) return event.beamParticle();
        if(particleID==5001) return event.targetParticle();
        
        
        if(particleType.compareTo("-")==0){
            Particle  fromEvent = event.getParticleByCharge(-1,particleSkip);
            Particle npart =  new Particle(this.overrideParticleID,
                    fromEvent.px(),
                    fromEvent.py(),
                    fromEvent.pz(),
                    fromEvent.vertex().x(),                    
                    fromEvent.vertex().y(),
                    fromEvent.vertex().z()
            );
            if(this.particleSign<0) npart.vector().invert();
            return npart;
            
        }
        
        if(particleType.compareTo("+")==0){
            Particle  fromEvent = event.getParticleByCharge(1,particleSkip);
            Particle npart =  new Particle(this.overrideParticleID,
                    fromEvent.px(),
                    fromEvent.py(),
                    fromEvent.pz(),
                    fromEvent.vertex().x(),                    
                    fromEvent.vertex().y(),
                    fromEvent.vertex().z()
            );
            if(this.particleSign<0) npart.vector().invert();
            return npart;
        }
        
        if(particleType.compareTo("n")==0){
            Particle  fromEvent = event.getParticleByCharge(0,particleSkip);
            Particle npart =  new Particle(this.overrideParticleID,
                    fromEvent.px(),
                    fromEvent.py(),
                    fromEvent.pz(),
                    fromEvent.vertex().x(),                    
                    fromEvent.vertex().y(),
                    fromEvent.vertex().z()
            );
            if(this.particleSign<0) npart.vector().invert();
            return npart;
            
        }
        
        if(event.countByPid(particleID)<particleSkip) return null;
        Particle  fromEvent = event.getParticleByPid(particleID, particleSkip);

        if(this.overridePid==false){
            Particle npart =  new Particle(this.particleID,
                    fromEvent.px(),
                    fromEvent.py(),
                    fromEvent.pz(),
                    fromEvent.vertex().x(),                    
                    fromEvent.vertex().y(),
                    fromEvent.vertex().z()
            );
            if(this.particleSign<0) npart.vector().invert();
            return npart;
            
        }
        
        Particle npart =  new Particle(this.overrideParticleID,
                    fromEvent.px(),
                    fromEvent.py(),
                    fromEvent.pz(),
                    fromEvent.vertex().x(),                    
                    fromEvent.vertex().y(),
                    fromEvent.vertex().z()
            );
            if(this.particleSign<0) npart.vector().invert();
            return npart;
            
        } catch (Exception e){
            
        }
        return null;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("SELECTOR : FORMAT = (%32s) (%8d,%8s,%4d,%3d, override = [%6d,%6s] )",
                selectorFormat,particleID,particleType,particleSkip,particleSign,
                this.overrideParticleID,this.overridePid));
        return str.toString();
    }
}
