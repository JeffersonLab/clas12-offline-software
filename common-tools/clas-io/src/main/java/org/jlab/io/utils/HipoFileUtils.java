/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class HipoFileUtils {
    
    public static void hipoDump(String filename){
        HipoDataSource reader = new HipoDataSource();
        reader.open(filename);
        
    }
    
    public static void main(String[] args){
        String file = args[0];
        HipoFileUtils.hipoDump(file);
    }
}
