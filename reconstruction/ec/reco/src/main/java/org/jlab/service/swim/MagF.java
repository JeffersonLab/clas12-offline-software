/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.swim;

import java.nio.ByteBuffer;

/**
 *
 * @author gavalian
 */
public class MagF {
    
    ByteBuffer  field;
    private final double scale;
    
    
    public MagF(String file){
        scale = 0.2;
        field = MagF.readFromFile(file);        
    }
    
    private  void  initalize(){
        byte[] data = new byte[4096];
        //field = ByteBuffer.wrap(data);
    }
    
    public static  ByteBuffer  readFromFile(String file){
        int size = 1024*1024*8;
        byte[] data = new byte[size];
        ByteBuffer f = ByteBuffer.wrap(data);
        for(int i = 0; i < size/4; i++){
            float value = (float) Math.random();
            f.putFloat(i*4, value);
        }
        return f;
    }
    
    public float filed(int x, int y, int z){
        float xv = field.getFloat(x*4);
        float yv = field.getFloat(y*4);
        float zv = field.getFloat(z*4);
        return (xv*xv + yv*yv + zv*zv);
    }
}
