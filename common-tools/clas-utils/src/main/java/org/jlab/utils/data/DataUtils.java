/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils.data;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public class DataUtils {
    
    public static TreeMap<Integer,Integer> bitMap = DataUtils.createBitMap();
    
    public static TreeMap<Integer,Integer>  createBitMap(){
        TreeMap<Integer,Integer> map = new TreeMap<Integer,Integer>();
        for(int loop = 0; loop < 25; loop++){
            int  integer_value = 0;
            for(int hb = 0 ; hb < loop; hb++){
                integer_value = integer_value | (1<<hb);
            }
            map.put(loop, integer_value);
        }
        return map;
    }
    
    public static void printBitMap(){
        for(Map.Entry<Integer,Integer> entry : bitMap.entrySet()){
            System.out.println(String.format("%4d : ", entry.getKey()) 
                    + String.format("%32s", Integer.toBinaryString(entry.getValue())).replace(' ', '0'));
        }
    }
        
    public static int getInteger(int data, int bitstart, int bitend){
        int length = bitend - bitstart + 1;
        if(DataUtils.bitMap.containsKey(length)==true){
            int value = ((data>>bitstart)&DataUtils.bitMap.get(length));
            return value;
        } else {
            System.out.println("[DataUtilities] : ERROR length = " + length);
        }
        return 0;
    }
    
    public static short getShortFromByte(byte data){
        short short_data = 0;
        return (short) ((short_data|data)&0x00FF);
    }
    
    public static int  getIntFromShort(short data){
        int int_data = 0;
        return (int) ( (int_data|data)&0x0000FFFF);
    }
    
    public static int  getIntFromByte(byte data){
        int int_data = 0;
        return (int) ( (int_data|data)&0x0000FFFF);
    }
    
    public static short  getShortFromInt(int data){
        short int_data = 0;
        return (short) ( (int_data|data)&0xFFFF);
    }
    
    public static byte   getByteFromInt(int data){
        byte byte_data = 0;
        return (byte) ((byte_data|data)&0xFF);
    }
    
    public static byte   getByteFromShort(short data){
        byte byte_data = 0;
        return (byte) ((byte_data|data)&0xFF);
    }

    public static long  getLongFromInt(int data){
        long long_data = 0;
        return (long) ( (long_data|data)&0x00000000FFFFFFFFL);
    }
    
}
