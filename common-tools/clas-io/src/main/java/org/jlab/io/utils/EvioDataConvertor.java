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
public class EvioDataConvertor {
    public static short getShortFromByte(byte data){
        short short_data = 0;
        return (short) ((short_data|data)&0x00FF);
    }
    
    public static int  getIntFromShort(short data){
        int int_data = 0;
        return (int) ( (int_data|data)&0x0000FFFF);
    }
    
    public static void main(String[] args){
        byte data = -117;
        System.err.println(String.format("byte = %d   short = %d , B=%X  S=%X", 
                data,EvioDataConvertor.getShortFromByte(data),data,
                EvioDataConvertor.getShortFromByte(data)));
        
        short sdata = -25678;
        System.err.println(String.format("short = %d   int = %d , S=%X  I=%X", 
                sdata,EvioDataConvertor.getIntFromShort(sdata),sdata,
                EvioDataConvertor.getIntFromShort(sdata)));
    }
}
