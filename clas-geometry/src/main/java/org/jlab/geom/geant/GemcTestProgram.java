/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.geant;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.base.ConstantProvider;

/**
 *
 * @author gavalian
 */
public class GemcTestProgram {
    
    public static String numberString(Integer num, int spaces){
        String ns = num.toString();
        StringBuilder str = new StringBuilder();
        int len = spaces - ns.length();
        for(int b = 0; b < len; b++){
            str.append("0");
        }
        str.append(ns);
        return str.toString();
    }
    
    public static List<G4BaseVolume>  getList(ConstantProvider cp){
        List<G4BaseVolume>  volumes = new ArrayList<G4BaseVolume>();
        
        return volumes;
    }
    /**
     * This is a test program
     * @param args 
     */
    public static void main(String[] args){               
        
        for(int loop = 0; loop < 23; loop++){
            double length = 50 + loop*4;
            double width  = 5.0;
            
            G4Trd trd = new G4Trd("paddle_"+loop,5,10,10,10,width);
            trd.addTranslation(0.0, width, 0.0);
            trd.addRoration("z", 0.0, "y", 0.0, "x", 20.0);
            
            trd.setDescription("FTOF-sector-2-paddle-" + GemcTestProgram.numberString(loop, 3));
            
            trd.getDetector().put("mother", "ftof-panel-1-sector-1");
            //System.out.println("created paddle # "+ loop);
            System.out.println(trd.getDetector().toPaddedString(trd.getDetector().getWidths(1), "|"));
        }
    }
}
