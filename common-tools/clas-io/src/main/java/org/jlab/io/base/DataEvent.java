package org.jlab.io.base;

import java.nio.ByteBuffer;

public interface DataEvent {
    
    String[] getBankList();
    String[] getColumnList(String bank_name);
    DataDictionary getDictionary();
    ByteBuffer getEventBuffer();
    
    void     appendBank(DataBank bank);
    void     appendBanks(DataBank... bank);
    void     removeBank(String bankName);
    void     removeBanks(String... bankNames);
    
    boolean  hasBank(String name);
    
    DataBank createBank(String bank_name, int rows);    
    DataBank getBank(String bank_name);
    void     show();
    
    void getBank(String bank_name, DataBank bank);
    void setProperty(String property, String value);
    String getProperty(String property);
    
    double[] getDouble(String path);
    void setDouble(String path, double[] arr);
    void appendDouble(String path, double[] arr);
    
    float[] getFloat(String path);
    void setFloat(String path, float[] arr);
    void appendFloat(String path, float[] arr);
    
    int[] getInt(String path);
    void setInt(String path, int[] arr);
    void appendInt(String path, int[] arr);

    long[] getLong(String path);
    void setLong(String path, long[] arr);
    void appendLong(String path, long[] arr);
    
    short[] getShort(String path);
    void setShort(String path, short[] arr);
    void appendShort(String path, short[] arr);
    
    byte[] getByte(String path);
    void setByte(String path, byte[] arr);
    void appendByte(String path, byte[] arr);
    
    void setType(DataEventType type);
    DataEventType getType();
}
