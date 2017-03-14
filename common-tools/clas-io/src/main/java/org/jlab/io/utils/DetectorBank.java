/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

/**
 *
 * @author gavalian
 */
public class DetectorBank {
    public float X = 0;
    public float Y = 0;
    public float Z = 0;
    public float dX = 0;
    public float dY = 0;
    public float dZ = 0;
    public float time      = (float) 0.0;
    public float energy    = (float) 0.0;
    public float path      = (float) 0.0;
    public short pindex    = (short) 0;
    public byte  dindex    = (byte)  0;
    public byte  detector   = (byte) 1;
    public byte  sector     = (byte) 1;
    public byte  superlayer = (byte) 1;
    public short component  = (short) 1;    
}
