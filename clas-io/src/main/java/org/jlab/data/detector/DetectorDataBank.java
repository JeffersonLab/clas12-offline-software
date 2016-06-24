/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.detector;

import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public class DetectorDataBank {
    
    private DetectorType  detectorType = DetectorType.UNDEFINED;
    private TreeMap<Integer,Object>  bankData = new TreeMap<Integer,Object>();
    
    public DetectorDataBank(){
        
    }
    
    public DetectorDataBank(DetectorType type){
        this.detectorType = type;
    }
    
    public DetectorDataBank(DetectorType type, int size){
        this.detectorType = type;
        this.allocate(size);
    }
    
    public final void allocate(int rows){
        bankData.clear();
        bankData.put(1, new byte[rows]);
        bankData.put(2, new byte[rows]);
        bankData.put(3, new byte[rows]);
        
        if(detectorType==DetectorType.DC){
            bankData.put(4, new short[rows]);
        }
        
        if(detectorType==DetectorType.BST){
            bankData.put(4, new short[rows]);
            bankData.put(5, new short[rows]);
        }
        
        if(detectorType==DetectorType.EC){
            bankData.put(4, new short[rows]);
            bankData.put(5, new short[rows]);
        }
        
        if(detectorType==DetectorType.FTOF){
            bankData.put(4, new short[rows]);
            bankData.put(5, new short[rows]);
            bankData.put(6, new short[rows]);
            bankData.put(7, new short[rows]);
        }                
    }
    
    public void setSectorLayerComponent(int sector, int layer, int component, int index){
        ((byte[]) this.bankData.get(1))[index] = (byte) sector;
        ((byte[]) this.bankData.get(2))[index] = (byte) layer;
        ((byte[]) this.bankData.get(3))[index] = (byte) component;
    }
    
    public void setTDC(short tdc, int index){
        //System.out.println("SETTING TDC = " + index + " " + tdc);
       ((short[]) this.bankData.get(4))[index] = tdc;
    }
    
    public void setADC(short adc, int index){
       ((short[]) this.bankData.get(5))[index] = adc;
    }
    public void setTDCL(short adc, int index){
       ((short[]) this.bankData.get(4))[index] = adc;
    }
    
    public void setADCL(short adc, int index){
       ((short[]) this.bankData.get(5))[index] = adc;
    }
    
    public void setTDCR(short adc, int index){
       ((short[]) this.bankData.get(6))[index] = adc;
    }
    
    public void setADCR(short adc, int index){
       ((short[]) this.bankData.get(7))[index] = adc;
    }
    
    public int getSector(int index){
       return this.getByteAsInteger(1, index);
    }
    
    public int getLayer(int index){
        return this.getByteAsInteger(2, index);
    }
    
    public int getComponent(int index){
        return this.getByteAsInteger(3, index);
    }
    
    public int getTag(){
        return this.detectorType.getDetectorId()*100;
    }
    
    public int getTDC(int index){
        if(this.hasEntryShort(4)==true){
            return this.getShortAsInteger(4, index);
        }
        return 0;
    }
    
    public int getADC(int index){
        if(this.hasEntryShort(5)==true){
            return this.getShortAsInteger(5, index);
        }
        return 0;
    }
    
    public int getTDCL(int index){
       return this.getTDC(index);
    }
    
    public int getADCL(int index){
       return this.getADC(index);
    }
    
    public int getTDCR(int index){
        if(this.hasEntryShort(6)==true){
            return this.getShortAsInteger(6, index);
        }
        return 0;
    }
    
    public int getADCR(int index){
        if(this.hasEntryShort(7)==true){
            return this.getShortAsInteger(7, index);
        }
        return 0;
    }
    
    public  void  setTree(TreeMap<Integer,Object> data){ this.bankData = data;}    
    public  TreeMap<Integer,Object>  getTree(){return this.bankData;}
        
    private boolean hasEntryShort(int key){
        if(this.bankData.containsKey(key)==true){
            if(this.bankData.get(key) instanceof short[]) return true;
        }
        return false;
    }
    
    private boolean hasEntryByte(int key){
        if(this.bankData.containsKey(key)==true){
            if(this.bankData.get(key) instanceof byte[]) return true;
        }
        return false;
    }
    
    private int getShortAsInteger(int key, int index){
        if(bankData.containsKey(key)==true){
            if(bankData.get(key) instanceof short[]){
                short[] buffer = (short[]) bankData.get(key);
                if(index>=0 && index<buffer.length){
                    return (int) buffer[index];
                }
            }
        }
        return 0;
    }
    
    private int getByteAsInteger(int key, int index){
        if(bankData.containsKey(key)==true){
            if(bankData.get(key) instanceof byte[]){
                byte[] buffer = (byte[]) bankData.get(key);
                if(index>=0 && index<buffer.length){
                    return (int) buffer[index];
                }
            }
        }
        return 0;
    }
    
    public int getRows(){
        if(this.hasEntryByte(1)==true){
            byte[] sector = (byte[]) this.bankData.get(1);
            return sector.length;
        }
        return 0;
    }
    
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        int rows = this.getRows();
        for(int loop = 0; loop < rows; loop++){
            str.append(String.format(" S/L/C [ %4d %4d %4d ]   ", this.getSector(loop),
                    this.getLayer(loop),this.getComponent(loop)));
            
            if(detectorType==DetectorType.BST){
                str.append(String.format("ADC/TDC [%8d %8d]", this.getADC(loop),this.getTDC(loop)));
            }
            
            if(detectorType==DetectorType.FTOF){
                str.append(String.format("ADCL/ADCR [%8d %8d] TDCL/TDCR [%8d %8d]", 
                        this.getADCL(loop),this.getADCR(loop),
                        this.getTDCL(loop),this.getTDCR(loop)
                        ));
            }
            
            if(detectorType==DetectorType.DC){
                str.append(String.format("TDCL [%8d ]", 
                        this.getTDCL(loop)
                        ));
            }
            
            if(detectorType==DetectorType.EC){
                str.append(String.format("ADC/TDC [%8d %8d] ", this.getADC(loop),this.getTDC(loop)));
            }
            
            str.append("\n");
        }
        return str.toString();
    }
}
