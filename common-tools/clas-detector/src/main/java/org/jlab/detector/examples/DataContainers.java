/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.examples;

import org.jlab.groot.data.H2F;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author gavalian
 */
public class DataContainers {
    public static void main(String[] args){
        IndexedList<H2F>  h2TOF = new IndexedList<H2F>(3);
        
        for(int sector = 1; sector < 6; sector++){
            for(int paddle = 1; paddle < 23; paddle++){
                h2TOF.add(new H2F(), sector,1,paddle);
            }
        }
        
        
        boolean flag = h2TOF.hasItem(2,1,13);
        H2F     h2P  = h2TOF.getItem(2,1,13);
    }
}
