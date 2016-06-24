/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.geom.gui;

import org.jlab.geom.detector.ec.ECFactory;

/**
 *
 * @author gavalian
 */
public class GeometryProfiler {
    private static final double BYTE_TO_MB = 1024*1024; 
    public static void showHeapUsage(String unit){
        Runtime runtime = Runtime.getRuntime();
        String stats = String.format("MEMORY [%12s] MAX = %15.2f MB   TOTAL = %15.2f MB   FREE = %15.2f MB", unit,
                runtime.maxMemory()/BYTE_TO_MB,runtime.totalMemory()/BYTE_TO_MB,runtime.freeMemory()/BYTE_TO_MB);
        System.err.println(stats);
    }
    public static void main(String[] args){
        GeometryProfiler.showHeapUsage("START");
        ECFactory ecFactory = new ECFactory();
        //Detector ecDetector = ecFactory.createDetectorCLAS();
    }
}
