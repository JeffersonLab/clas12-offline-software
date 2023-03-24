/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.vtx;

/**
 *
 * @author veronique
 */
public class Constants {

    // singleton
    private static Constants instance = null;
    
    /**
     * public access to the singleton
     * 
     * @return the constants singleton
     */
    public static Constants getInstance() {
            if (instance == null) {
                    instance = new Constants();
            }
            return instance;
    }
    
    /**
     * @return the DOCACUT
     */
    public static double getDOCACUT() {
        return DOCACUT;
    }

    /**
     * @param aDOCACUT the DOCACUT to set
     */
    public static void setDOCACUT(double aDOCACUT) {
        DOCACUT = aDOCACUT;
    }
    
    public static final double DZ = 10.00;
    private static double DOCACUT = 5.00;
    public static boolean DEBUGMODE = false;
    
}
