/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.physics.base;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.ParticleNotFoundException;
import org.jlab.clas.physics.PhysicsEvent;

/**
 *
 * @author gavalian
 */
public class EventSelector {
    private final ArrayList<ParticleSelector>  pSelectors = new ArrayList<ParticleSelector>();
    private String  selectorFormat = "";
    private final String  openBracket     = "[";
    private final String  closeBracket    = "]";
    
    public EventSelector(){
        
    }
    
    public EventSelector(String format){
        this.parse(format);        
    }
    
    public final void parse(String format){
        pSelectors.clear();
        ParticleStringParser parser = new ParticleStringParser();
        List<String>  operators = parser.parse(format);
        selectorFormat = format.replaceAll("\\s+", "");
        
        for(String item : operators){
            ParticleSelector selector = new ParticleSelector(item);
            pSelectors.add(selector);
        }
        /*
        selectorFormat = format.replaceAll("\\s+", "");
        int position = selectorFormat.indexOf(this.closeBracket, 0);
        int lastposition = 0;
        while(position>0&&position<selectorFormat.length()){
            ParticleSelector  psel = new ParticleSelector();
            psel.parse(selectorFormat.substring(lastposition, position+1));
            pSelectors.add(psel);
            //System.err.println("SELECTOR : " + selectorFormat.substring(lastposition, position+1));
            lastposition = position+1;
            position = selectorFormat.indexOf(this.closeBracket, position+1);
        }*/
    }
    
    public String getFormat(){
        return selectorFormat;
    }
    
    public Particle get(PhysicsEvent event){
        try {
            List<Particle>  plist = this.getParticleList(event);
            if(plist.isEmpty()) return null;
            
            Particle part = new Particle();
            part.copy(plist.get(0));
            for(int loop =1 ; loop < plist.size(); loop++){
                part.combine(plist.get(loop), 1);
            }
            return part;
        } catch (ParticleNotFoundException e){
            return new Particle();
        }
    }
    
    public List getParticleList(PhysicsEvent event) throws ParticleNotFoundException{
        ArrayList<Particle>  plist = new ArrayList<Particle>();
        for(ParticleSelector sel : pSelectors){
            Particle part = new Particle();
            boolean status = sel.getParticle(event,part);
            if(status==false){
                throw new ParticleNotFoundException("[EventSelector::ERROR] event does no contain "
                + "particle for selector \n" + sel.toString());
                /*System.err.println("[EventSelector::ERROR] event does no contain "
                + "particle for selector \n" + sel.toString());*/
            } else {
                plist.add(part);
            }
        }
        return plist;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("EVENT SELECTOR : (%s)\n", this.selectorFormat));
        for(ParticleSelector psel : pSelectors){
            str.append(psel.toString());
            str.append("\n");
        }
        return str.toString();
    }
    
    public static void main(String[] args){
        EventSelector selector = new EventSelector("[b]+[t]+(211)-{2212,0}-[2212,1]");
        System.out.println(selector);
    }
}
