/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.analysis.eventmerger;

import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;

/**
 * Utility methods for the HipoWriterSorted
 * 
 * @author devita
 */
public class SortedWriterUtils {
    
    public static final int SCALERTAG = 1;    
    public static final int CONFIGTAG = 2;
    
    /**
     * Write all selected tag events from inputfile, requires writer file to be already open
     * @param writer
     * @param tag
     * @param inputfile
     */
    public void writeTag(HipoWriterSorted writer, int tag, String inputfile){
        if(writer==null) return;
        HipoReader reader = new HipoReader();
        reader.setTags(tag);
        reader.open(inputfile);
        while(reader.hasNext()) {
            Event eventData = new Event();
            reader.nextEvent(eventData);
            writer.addEvent(eventData, eventData.getEventTag());
        }
        reader.close();
        System.out.println("\nAdding tag-" + tag + " events from file " + inputfile);
    }


}
