/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;

/**
 *
 * @author gavalian
 */
public class FADCData {
    
    private DetectorDescriptor desc = new DetectorDescriptor();
    private short[] adcBuffer  = null;
    
    public FADCData(int crate, int slot, int channel){
        desc.setCrateSlotChannel(crate, slot, channel);
    }
    
    public void setBuffer(short[] buffer){
        adcBuffer = buffer;
    }
    
    public int getSize(){
        if (adcBuffer==null) return 0;
        return adcBuffer.length;
    }
    
    public String getPulseString(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < adcBuffer.length; i++){
            str.append(String.format("%6d", adcBuffer[i]));
            if((i+1)%16==0) str.append("\n");
        }
        return str.toString();
    }
    
    public DetectorDescriptor getDescriptor(){ return desc;}
    
    
    public DetectorDataDgtz getDetectorData(){
       List<Short>  decodedList = getDecoded();
       short[]  pulse = new short[decodedList.size()];
       for(int i = 0; i < pulse.length; i++){
           pulse[i] = decodedList.get(i);
       }
       
       DetectorDataDgtz dgtz = new DetectorDataDgtz();
       dgtz.getDescriptor().setCrateSlotChannel(
               desc.getCrate(),desc.getSlot(),desc.getChannel()
               );
       /*dgtz.getDescriptor().setCrateSlotChannel(
               desc.getSector(),desc.getLayer(),desc.getComponent());
       */
       dgtz.addPulse(pulse);
       return dgtz;
    }
    
    public static List<DetectorDataDgtz>  convert(List<FADCData> dataFADC){
        List<DetectorDataDgtz> dgtzList = new ArrayList<DetectorDataDgtz>();
        for(FADCData data : dataFADC){
            DetectorDataDgtz dgtz = data.getDetectorData();
            dgtzList.add(dgtz);
        }
        return dgtzList;
    }
    
    public List<Short> getDecoded(){
        
        List<Short> result = new ArrayList<Short>();
        
        short[]  bucket = new short[16];
        
        int channel;
        int minimum;
        int nwords;
        int nskip;
        int position = 0;
        int headerWord = 0;
        int pedestal ;
        int compressedWord = 0;
        byte[] array = new byte[4];

        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        while(position<this.adcBuffer.length){
        
            short dataH = adcBuffer[position];
            short dataL = adcBuffer[position+1];
            
            buffer.putShort(0, dataH);
            buffer.putShort(2, dataL);

            headerWord = buffer.getInt(0);
            
            //headerWord = (dataL<<16)|(dataH);
            channel  = (headerWord >> 23) & 0x0F;
            nwords   = (headerWord&0x07);
            nskip    = (dataH>>4) &0x0F;
            pedestal = (headerWord>>8)&0x0FFF;
            compressedWord = (headerWord>>27)&0x0F;

            position+=2;
            if(compressedWord==5){
                
                //System.out.println( " position = " + position +  " NWORD = 5  LENGTH = " + adcBuffer.length);
                short value;
                //result.clear();
                for(int i = 0; i < 4; i++){
                    value = (short) (adcBuffer[position+i]&0x000F);                    
                    bucket[i*4] = (short) (value+pedestal);                                        
                    value = (short) ((adcBuffer[position+i] >> 4)&0x000F);                    
                    bucket[i*4+1] = (short) (value+pedestal);                    
                    value = (short) ((adcBuffer[position+i] >> 8)&0x000F);
                    bucket[i*4+2] = (short) (value+pedestal);
                    value = (short) ((adcBuffer[position+i] >> 12)&0x000F);
                    bucket[i*4+3] = (short) (value+pedestal);
                }
                
                position += 4;
                //position += nwords*2;
                
                if(nwords>0){
                    
                    for(int i = 0; i < nwords*2; i++){
                        short  first = (short)  ( (adcBuffer[i+position]&0x00FF) << 4);
                        short second = (short) ( ((adcBuffer[i+position] >>8)&0x00FF)<<4);
                        if( (nskip+i*2)<=15  ) bucket[nskip+i*2]   +=  first;
                        if( (nskip+i*2+1)<=15) bucket[nskip+i*2+1] +=  second;
                    }                    
                    position += nwords*2;
                }
                
                for(int k = 0 ; k < bucket.length; k++){
                    result.add(bucket[k]);
                }
            }
            //position += 4;
            
            //position += nwords*2;
            //System.out.println("position = " + position);
        }
        return result;
    }
    
    public void decode(){
        
        List<Short> result = new ArrayList<Short>();
        
        short[]  bucket = new short[16];
        
        int channel;
        int minimum;
        int nwords;
        int nskip;
        int position = 0;
        int headerWord = 0;
        int pedestal ;
        int compressedWord = 0;
        byte[] array = new byte[4];
        
        System.out.println(desc.toString());
        System.out.println("------------------------------------------------------");
        for(int i = 0 ; i < adcBuffer.length/2; i++){
            System.out.print(String.format("%04X ", adcBuffer[i*2+1]));
            System.out.print(String.format("%04X ", adcBuffer[i*2]));
            if((i+1)%8==0) System.out.println();
        }
        System.out.println();
        System.out.println("------------------------------------------------------");
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        while(position<this.adcBuffer.length){
        
            short dataH = adcBuffer[position];
            short dataL = adcBuffer[position+1];
            
            buffer.putShort(0, dataH);
            buffer.putShort(2, dataL);

            headerWord = buffer.getInt(0);
            
            //headerWord = (dataL<<16)|(dataH);
            channel  = (headerWord >> 23) & 0x0F;
            nwords   = (headerWord&0x07);
            nskip    = (dataH>>4) &0x0F;
            pedestal = (headerWord>>8)&0x0FFF;
            compressedWord = (headerWord>>27)&0x0F;
            //System.out.println(String.format(" %08X %08X %08X ==> ch = %3d, nw = %2d, skip = %2d ped = %4d id = %4d, position = %4d", 
            //        headerWord,dataH, dataL, channel, nwords, nskip, pedestal, compressedWord, position));

            position+=2;
            if(compressedWord==5){
                
                //System.out.println(" NWORD = 5 " );
                short value;
                result.clear();
                for(int i = 0; i < 4; i++){
                    value = (short) (adcBuffer[position+i]&0x000F);
                    
                    bucket[i*4] = (short) (value+pedestal);                    
                    result.add( (short) (value+pedestal));
                    
                    value = (short) ((adcBuffer[position+i] >> 4)&0x000F);
                    
                    bucket[i*4+1] = (short) (value+pedestal);
                    result.add( (short) (value+pedestal));
                    
                    value = (short) ((adcBuffer[position+i] >> 8)&0x000F);
                    bucket[i*4+2] = (short) (value+pedestal);
                    result.add( (short) (value+pedestal));
                    
                    value = (short) ((adcBuffer[position+i] >> 12)&0x000F);
                    bucket[i*4+3] = (short) (value+pedestal);
                    result.add( (short) (value+pedestal));
                }
                
                position += 4;
                //position += nwords*2;
                
                if(nwords>0){
                    
                    for(int i = 0; i < nwords*2; i++){
                        short  first = (short)  ( (adcBuffer[i+position]&0x00FF) << 4);
                        short second = (short) ( ((adcBuffer[i+position] >>8)&0x00FF)<<4);
                        
                        /*System.out.println(String.format(" data (%3d) = %4d %4d  %04X (%04X %04X) %4d %4d (%4d %4d) ", 
                                nskip,
                                first,second, adcBuffer[i+position], first,second, 
                                bucket[nskip+i],bucket[nskip+i+1],
                                bucket[nskip+i]+first,bucket[nskip+i+1]+second));*/
                        if( (nskip+i*2)<=15)
                            bucket[nskip+i*2]   +=  first;
                        if( (nskip+i*2+1)<=15)
                            bucket[nskip+i*2+1] +=  second;
                        //bucket[i+nskip+1] +=  first;
                        //System.out.println(String.format(" data = %4d %4d  %04X (%04X %04X) ", 
                        //        first,second, adcBuffer[i+position], first,second));
                    }
                    
                    position += nwords*2;
                }
                result.clear();
                for(int k = 0 ; k < bucket.length; k++){
                    result.add(bucket[k]);
                }
                for(int i = 0; i < result.size(); i++){
                    System.out.print(String.format("%6d", result.get(i)));
                }
                System.out.println();
            }
            //position += 4;
            
            //position += nwords*2;
            //System.out.println("position = " + position);
        }
    }
    
    public void show(){
        int length = 0;
        if(adcBuffer!=null) length = adcBuffer.length;
        System.out.println( desc.toString() + " ADC LENGTH = " + length);
    }
}
