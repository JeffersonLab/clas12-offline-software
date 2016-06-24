/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.bos;

import java.nio.ByteBuffer;

/**
 *
 * @author gavalian
 */
public class BosBankStructure {
    
    public String bankName = "";    
    public int  BANKNUMBER = 0;
    public int  NROWS    = 0;
    public int  NCOLS    = 0;
    public int  LENGHT   = 0;
    public int  BANKSIZE = 0;
    public int  NWORDS   = 0;
    public byte[]  DATA    = null;
    public int     DATAOFFSET = 0;
    private int    FORMATOFFSET = 12;
    
    public BosBankStructure(){
        
    }
    
    public String getName(){
        return this.bankName;
    }
    
    public static int  dataOffset(ByteBuffer buffer, int start_index){
        byte[] data = buffer.array();
        int offset  = 8*4;
        for(int loop = start_index+offset; loop < data.length; loop++){
            if(data[loop]==' '||data[loop]==')') return (loop+1);
        }
        return -1;
    }
    
    public static BosBankStructure combine(BosBankStructure a, BosBankStructure b){
        BosBankStructure  struct = new BosBankStructure();
        struct.bankName = a.bankName;
        struct.BANKNUMBER = a.BANKNUMBER;
        struct.BANKSIZE   = a.BANKSIZE;
        struct.NCOLS      = a.NCOLS;
        struct.NROWS      = a.NROWS;
        struct.NWORDS     = a.NWORDS + b.NWORDS;
        struct.DATA       = new byte[a.DATA.length + b.DATA.length];
        System.arraycopy(a.DATA, 0, struct.DATA, 0, a.DATA.length);
        System.arraycopy(b.DATA, 0, struct.DATA, a.DATA.length, b.DATA.length);
        return struct;
    }
    
    public boolean isComplete(){
        if(this.NROWS*this.BANKSIZE==4*this.NWORDS) return true;
        return false;
    }
    
    public void updateOffsets(){
        int offset = FORMATOFFSET;
        while(DATA[offset]!='0'||DATA[offset]!='\0'||DATA[offset]!='\n'||
                DATA[offset]!=' '){
            offset++;
        }
        DATAOFFSET = offset;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("[%s] [%d] NCOLS = %3d  NROWS = %3d BANKSIZE = %3d NWORDS = %3d BUFFER LENGTH = %4d OFFSET = %3d",
                this.getName(),this.BANKNUMBER,this.NCOLS,this.NROWS,this.BANKSIZE,this.NWORDS, 
                this.DATA.length,this.DATAOFFSET)
        );
        return str.toString();
    }
}
