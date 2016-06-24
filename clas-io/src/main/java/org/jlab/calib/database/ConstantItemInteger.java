/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.calib.database;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 *
 * @author gavalian
 */
public class ConstantItemInteger {
    private int[]     itemData;
    private String    itemName;
    private String    itemSubSystem;
    private String    itemSystem;
    private Integer      sector;
    private Integer      layer;
    private Integer      superLayer;
    private String       unit;
    
    public ConstantItemInteger()
    {
        
    }
    
    public ConstantItemInteger(String name, int size)
    {
        itemName = name;
        itemData = new int[size];
    }
    
    public ConstantItemInteger(String csystem, String csub,String name, 
            int _sec, int _l, int _sl, int size)
    {
        itemSubSystem = csub;
        itemSystem    = csystem;
        itemName = name;
        itemData = new int[size];
        sector = _sec;
        layer  = _l;
        superLayer = _sl;
        unit = "undefined";
    }
    
    public ConstantItemInteger(String csystem, String csub,String name, 
            int _sec, int _l, int _sl, String _u,int size)
    {
        itemSubSystem = csub;
        itemSystem    = csystem;
        itemName = name;
        itemData = new int[size];
        sector = _sec;
        layer  = _l;
        superLayer = _sl;
        unit = _u;
    }
    
    public ConstantItemInteger(String csystem, String csub,String name, int size)
    {
        itemSubSystem = csub;
        itemSystem    = csystem;
        itemName = name;
        itemData = new int[size];
        sector = 0;
        layer  = 0;
        superLayer = 0;
        unit = "undefined";
    }
    
    @XmlAttribute(name="sector")
    public Integer getSector(){ return sector;}
    public void setSector(Integer sec){ sector = sec;}
    
    @XmlAttribute(name="layer")
    public Integer getLayer(){ return layer;}  
    public void setLayer(Integer _l){ layer = _l;}
    
    @XmlAttribute(name="superlayer")
    public Integer getSuperLayer(){ return superLayer;}
    public void setSuperLayer(Integer _sl){ superLayer = _sl;}
    
    @XmlAttribute(name="unit")
    public String getUnit(){ return unit;}
    public void setUnit(String _un){ unit = _un;}
    
    @XmlAttribute(name = "mapname")
    public String getMapKeyName(){
        StringBuilder str = new StringBuilder();
        str.append(itemSystem);
        str.append("/");
        str.append(itemSubSystem);
        str.append("/");
        str.append(itemName);
        return str.toString();
    }
    
    public void setMapKeyName(String mkey){
        String[] tokens = mkey.split("/");
        itemSystem = tokens[0];
        itemSubSystem = tokens[1];
        itemName = tokens[2];
    }
    
    @XmlAttribute(name = "name")
    public String getItemName(){
        return itemName;
    }
    
    public void setItemName(String name)
    {
        itemName = name;
    }
    
    //@XmlElement(name = "data")
    @XmlElementWrapper(name = "ints") @XmlElement(name = "int")
    public int[] getItemData(){
        return itemData;
    }
    
    public void setItemData(int[] obj){
        itemData = obj;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(this.getMapKeyName());
        str.append(" : (integer) Length = ");
        str.append(itemData.length);
        str.append(" [ sector = ");
        str.append(sector);
        str.append(" layer = ");
        str.append(layer);
        str.append(" superlayer = ");
        str.append(superLayer);
        str.append(" ]\n");
        int icount = 1;
        for(int v : itemData){
            str.append(String.format(" %12d ", v));
            if(icount%10==0) str.append("\n");
            icount++;
        }
        return str.toString();
    }
}
