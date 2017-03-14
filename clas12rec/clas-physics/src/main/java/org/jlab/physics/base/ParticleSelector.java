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
    
    public static int TYPE_GENERATED    = 1;
    public static int TYPE_RECONSTRUCED = 2;
    public static int TYPE_MATCHED      = 3;
    public static int TYPE_DETECTOR_PARTICLE = 4;
    
    private Integer   particleID   = 11;
    private Integer   particleSkip = 0;
    private Integer   particleSign = 1;
    private String    particleType = "11";
    private Integer   particleSelectionType = ParticleSelector.TYPE_RECONSTRUCED;
    private Boolean   overridePid  = false;
    private Integer   overrideParticleID = 11;
    private String    selectorFormat     = "[11,0]";
    
    private char      bracketsOpen        = '(';
    private char      bracketsClose       = ')';
    
    private final char[]    bracketTypes  = new char[]{'(',')','[',']','{','}'};
    
    public ParticleSelector(String format, int type){
        
        if(type>0&&type<4){
            bracketsOpen  = bracketTypes[ (type-1)*2];
            bracketsClose = bracketTypes[ (type-1)*2 + 1];
            this.particleSelectionType = type;
            this.parse(format);
        }
    }
    
    
    public ParticleSelector(String format){
        
        int type = 1;
        if(format.charAt(format.length()-1)==')') type = 1;
        if(format.charAt(format.length()-1)==']') type = 2;
        if(format.charAt(format.length()-1)=='}') type = 3;
        
        if(type>0&&type<4){
            bracketsOpen  = bracketTypes[ (type-1)*2];
            bracketsClose = bracketTypes[ (type-1)*2 + 1];
            this.particleSelectionType = type;
            this.parse(format);
        }
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
    
    public boolean getParticle(PhysicsEvent event, Particle p){                
        
        p.setParticleWithMass(0.0,(byte) 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        try {
            
            if(particleID==5000){
                p.copyParticle(event.beamParticle());
                return true;
            }
            if(particleID==5001){
                p.copyParticle(event.targetParticle());
                return true;
            }        
            
        } catch (Exception ex){
            System.out.println("ERROR: getting particle beam or target has failed");
        }
        
        
        if(this.particleSelectionType==ParticleSelector.TYPE_RECONSTRUCED&&event.countByPid(particleID)<particleSkip) {
            //System.out.println("debug 1");
            return false;
        }
        if(this.particleSelectionType==ParticleSelector.TYPE_GENERATED&&event.mc().countByPid(particleID)<particleSkip){ 
            //System.out.println("debug 2");
            return false;
        }
        
        //System.out.println("debug 3");
        try {
            
            Particle  fromEvent = event.getParticleByPid(particleID, particleSkip);
            
            if(this.particleSelectionType==ParticleSelector.TYPE_GENERATED){
                //System.out.println("debug 4");
                fromEvent = event.mc().getByPid(particleID, particleSkip);
            }
            
            if(this.particleSelectionType==ParticleSelector.TYPE_MATCHED){
                //System.out.println("debug 5");
                fromEvent = event.getParticleMatchByPid(particleID,particleSkip);                
            }
            
            if(fromEvent.p()<0.0000001){
                //System.out.println(" momentum is toooooo small");
                return false;
            }
            
            if(this.overridePid==false){
                p.copyParticle(fromEvent);
                p.changePid(this.particleID);
                //System.out.println("----------->>>>>> COPY IS DONE ON " + fromEvent.toString());
                if(this.particleSign<0) p.vector().invert();
                return true;                
            }            
            p.initParticle(this.overrideParticleID,
                    fromEvent.px(),
                    fromEvent.py(),
                    fromEvent.pz(),
                    fromEvent.vertex().x(),                    
                    fromEvent.vertex().y(),
                    fromEvent.vertex().z()
            );
            if(this.particleSign<0) p.vector().invert();
            return true;            
        } catch (Exception e){
            
        }
        return false;
    }
    
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
        str.append(String.format("SELECTOR : FORMAT = (%s) ( id = %8d, name = %8s, order = %4d, sign = %3d, type = %2d, override = [%6d,%6s] )",
                selectorFormat,particleID,particleType,particleSkip,particleSign,
                this.particleSelectionType,this.overrideParticleID,this.overridePid));
        return str.toString();
    }
    
    public static void main(String[] args){
        ParticleSelector selector = new ParticleSelector("-{211,2,2212}",3);
        //selector.parse("-(211,2,2212)");
        System.out.println(selector.toString());
    }
}
