package org.jlab.io.evio;


import org.jlab.io.base.BasicDataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataEntryType;


public class EvioDataBank extends BasicDataBank {
    
   public EvioDataBank(DataDescriptor desc){
       super(desc);
   }
   
   @Override
   public void allocate(int rows) {
       reset();
       EvioDataDescriptor desc = (EvioDataDescriptor) this.getDescriptor();
       String[] entries = desc.getEntryList();
       for(String entry : entries){
           DataEntryType type = DataEntryType.getType(desc.getProperty("type", entry));
           
           if(type==DataEntryType.BYTE){
               this.setByte(entry, new byte[rows]);
           }
           
           if(type==DataEntryType.SHORT){
               this.setShort(entry, new short[rows]);
           }
           
           if(type==DataEntryType.INTEGER){
               this.setInt(entry, new int[rows]);
           }
           
           if(type==DataEntryType.DOUBLE){
               this.setDouble(entry, new double[rows]);
           }
           
           if(type==DataEntryType.FLOAT){
               this.setFloat(entry, new float[rows]);
           }
       }
   }
}
