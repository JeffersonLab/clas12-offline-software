/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.utils;

import com.sun.media.jfxmedia.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.control.Accordion;

/**
 *
 * @author gavalian
 */
public class Configuration {
    
    private Map<String,ConfigurationGroup>  cGroups =
            new HashMap<String,ConfigurationGroup>();
    Accordion accordion = new Accordion ();
    
    public Configuration(){
        
    }
    
    public Node  getConfigPane(){
        for(Map.Entry<String,ConfigurationGroup> group : this.cGroups.entrySet()){
            this.accordion.getPanes().add(group.getValue().getTitledPane());
        }
        return this.accordion;
    }
    
    public void addGroup(ConfigurationGroup group){
        this.cGroups.put(group.getName(), group);
    }
}
