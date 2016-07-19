/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.calib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.system.ClasUtilsFile;

/**
 *
 * @author gavalian
 */
public class CalibrationConstants extends IndexedTable {   
    
    String constantsName = "default";

    
    public CalibrationConstants(int indexCount) {
        super(indexCount);
    }

    public CalibrationConstants(int indexCount,String format) {
        super(indexCount,format);
    }
    
    
    public void setName(String name){
        this.constantsName = name;
    }
    
    public String getName(){ return this.constantsName;}
    
    public void save(String file){        
        List<String>  linesFile = new ArrayList<String>();        
        Map<Long,IndexedTable.IndexedEntry> map = this.getList().getMap();
        int nindex = this.getList().getIndexSize();
        for(Map.Entry<Long,IndexedTable.IndexedEntry> entry : map.entrySet()){
            StringBuilder str = new StringBuilder();
            for(int i = 0; i < nindex; i++){
                str.append(
                        String.format("%3d ",IndexedList.IndexGenerator.getIndex(entry.getKey(),i)));
            }
            int ncolumns = entry.getValue().getSize();
            for(int i = 0; i < ncolumns; i++){
                str.append(String.format("  %e  ", entry.getValue().getValue(i)));
                //str.append(" ");
            }
            linesFile.add(str.toString());
            //System.out.println(str.toString());
        }
        
        ClasUtilsFile.writeFile(file, linesFile);
    }
    
    public static void main(String[] args){
        CalibrationConstants gain = new CalibrationConstants(3,"Mean/F:Error/I:Sigma/F:Serror/F");
        for(int i = 0; i < 23; i++){
            gain.addEntry(1,1,i+1);
        }
        
        gain.setDoubleValue(0.2, "Mean", 1,1,1);
        gain.setDoubleValue(0.3, "Mean", 1,1,2);
        gain.setDoubleValue(0.4, "Mean", 1,1,3);
        gain.setDoubleValue(0.5, "Mean", 1,1,4);
        gain.setDoubleValue(0.6, "Mean", 1,1,5);
        
        gain.setIntValue(4, "Error", 1,1,4);
        gain.save("filename");
    }
}
