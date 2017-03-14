/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.utils;

import java.util.ArrayList;

/**
 *
 * @author gavalian
 */
public class TablePrintout {
    
    private ArrayList<String>  headerDesc   = new ArrayList<String>();
    private ArrayList<Integer> headerLength = new ArrayList<Integer>();    
    private ArrayList<String[]> tableData   = new ArrayList<String[]>();
       
    private Integer tableIndent = 8;
    
    public TablePrintout(String header){
        this.setHeader(header);
    }
    
    public TablePrintout(String header, String len){
        this.setHeader(header,len);
    }
    
    public final void setHeader(String h){
        headerDesc.clear();
        headerLength.clear();
        String[] tokens = h.split(":");
        for(String item : tokens){
            headerDesc.add(item);
            headerLength.add(item.length()+4);
        }
    }
    
    public final void setHeader(String h, String l){
        headerDesc.clear();
        headerLength.clear();
        String[] th = h.split(":");
        String[] tl = l.split(":");
        for(int loop = 0; loop < th.length; loop++){
            headerDesc.add(th[loop]);
            headerLength.add(Integer.parseInt(tl[loop]));
        }
    }
    
    public static Integer[] positionIndex(String posString)
    {
        String[]  tokens = posString.split(":");
        Integer[] ticks  = new Integer[tokens.length];
        for(int loop = 0; loop < tokens.length; loop++)
        {
            ticks[loop] = Integer.parseInt(tokens[loop]);
        }
        return ticks;
    }
    
    public static String lineWithLength(int len)
    {
        StringBuilder str = new StringBuilder();
        for(int loop = 0; loop < len; loop++) str.append("-");
        return str.toString();
    }
    
    public String lineWithLengthSymbol(int len, Character sym){
        StringBuilder str = new StringBuilder();
        for(int loop = 0; loop < len; loop++) str.append(sym);
        return str.toString();
    }
    
    public String getHeaderLineString(){
        StringBuilder str = new StringBuilder();
        for(int loop = 0; loop < headerLength.size();loop++){
            str.append("+");
            str.append(this.lineWithLengthSymbol(headerLength.get(loop), '-'));
        }
        str.append("+");
        return str.toString();
    }
    public void addData(String[] data){
        if(data.length!=headerLength.size()){
            System.err.println("[Table data] ---> error. data size "
            + data.length + " does not match with header size = " +
                    headerLength.size());
            return;
        }
        tableData.add(data);
    }
    
    public void show(){
        
        String headerLine = this.getHeaderLineString();
        String header     = this.getHeaderString("*");
        String indentString = this.lineWithLengthSymbol(tableIndent, ' ');
        System.err.println();
        System.err.println(indentString + headerLine);
        System.err.println(indentString + header);
        System.err.println(indentString + headerLine);
        
        for(int loop = 0; loop < tableData.size(); loop++){
            System.err.println(indentString + this.getFormattedDataString(loop));
        }
        
        System.err.println(indentString + headerLine);
        System.err.println();
    }
    
    public String getFormattedDataString(int index){
        StringBuilder str = new StringBuilder();
        String[] array = tableData.get(index);
        for(int loop = 0; loop < headerLength.size(); loop++){            
            str.append("|");            
            String format = "%" + headerLength.get(loop).toString() + "s";
            str.append(String.format(format, array[loop]));
        }
        str.append("|");
        return str.toString();
    }
    
    public String getHeaderString(String type)
    {
        StringBuilder str = new StringBuilder();
        for(int loop = 0 ; loop < headerLength.size();loop++){
                str.append("|");
            String format = "%" + headerLength.get(loop).toString() + "s";
            str.append(String.format(format, headerDesc.get(loop)));
        }
        str.append("|");
        return str.toString();
    }
    
    public static String headerTop(String positions)
    {
        StringBuilder str = new StringBuilder();
        //str.append("+");
        Integer[] index = TablePrintout.positionIndex(positions);
        for(int loop = 0; loop < index.length; loop++)
        {
            str.append("+");
            str.append(TablePrintout.lineWithLength(index[loop]));
        }
        str.append("+");
        return str.toString();
    }
    
    public static String tableHeader(String columnNames, String columnWidths)
    {
       StringBuilder str = new StringBuilder();
       str.append(TablePrintout.headerTop(columnWidths));
       str.append("\n");
       Integer[] index  = TablePrintout.positionIndex(columnWidths);
       String[]  column = columnNames.split(":");
       str.append("|");
       for(int loop = 0; loop < column.length; loop++){
           String format = "%" + index[loop].toString() + "s|";
           str.append(String.format(format, column[loop]));       
       }
       str.append("\n");
       str.append(TablePrintout.headerTop(columnWidths));
       str.append("\n");
       return str.toString();
       
    }
}
