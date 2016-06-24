/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.calib.database;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author gavalian
 */
@XmlRootElement(name = "database")
public class ConstantDatabaseXML {
    
    private ArrayList<ConstantItemInteger> integerList = new 
            ArrayList<ConstantItemInteger>();
    
    private ArrayList<ConstantItemDouble> doubleList = new 
                    ArrayList<ConstantItemDouble>();
    
    public ConstantDatabaseXML(){
        
    }
    
    @XmlElement(name = "integers")
    public ArrayList<ConstantItemInteger> getIntegerList(){
        return integerList;
    }
    
    @XmlElement(name = "doubles")
    public ArrayList<ConstantItemDouble> getDoubleList(){
        return doubleList;
    }
    
    public void addInt(ConstantItemInteger item){
        integerList.add(item);
    }
    
    public void addDouble(ConstantItemDouble item){
        doubleList.add(item);
    }
    
    private String[] getItemNameTokens(String path){
        String[] tokens = path.split("/");
        return tokens;
    }
    
    public void addInt(String name, int sector, int layer, int suplayer, int size){
        String[] tokens = name.split("/");
        integerList.add(new ConstantItemInteger(tokens[0],tokens[1],tokens[2],
        sector,layer,suplayer,size));
    }
    
    public void addDouble(String name, int sector, int layer, int suplayer, int size){
        String[] tokens = name.split("/");
        doubleList.add(new ConstantItemDouble(tokens[0],tokens[1],tokens[2],
        sector,layer,suplayer,size));
    }
    
    public int[] intData(String name, int sector, int layer, int superlayer){
        for(ConstantItemInteger item : integerList){
            if(item.getMapKeyName().compareTo(name)==0&&
                    item.getSector()==sector&&
                    item.getLayer()==layer&&
                    item.getSuperLayer()==superlayer){
                return item.getItemData();
            }
        }
        System.out.println("[intData()] ---> Entry does not exist name = [" + name +"] sector = "
        + sector + " layer = " + layer + " superlayer = " + superlayer);
        return new int[0];
    }
    
    public double[] doubleData(String name, int sector, int layer, int superlayer){
        for(ConstantItemDouble item : doubleList){
            if(item.getMapKeyName().compareTo(name)==0&&
                    item.getSector()==sector&&
                    item.getLayer()==layer&&
                    item.getSuperLayer()==superlayer){
                return item.getItemData();
            }
        }
        System.out.println("[doubleData()] ---> Entry does not exist name = [" + name +"] sector = "
        + sector + " layer = " + layer + " superlayer = " + superlayer);
        return new double[0];
    }
    
    public ConstantItemInteger findInt(String csys, String csub, String ci){
        StringBuilder str = new StringBuilder();
        str.append(csys);
        str.append("/");
        str.append(csub);
        str.append("/");
        str.append(ci);
        String key = str.toString();
        for(ConstantItemInteger item : integerList){
            if(item.getMapKeyName().compareTo(key)==0)
                return item;
        }
        return null;
    }
    
    public ConstantItemDouble findDouble(String csys, String csub, String ci){
        StringBuilder str = new StringBuilder();
        str.append(csys);
        str.append("/");
        str.append(csub);
        str.append("/");
        str.append(ci);
        String key = str.toString();
        for(ConstantItemDouble item : doubleList){
            if(item.getMapKeyName().compareTo(key)==0)
                return item;
        }
        return null;
    }
    
    
    public static ConstantDatabaseXML load(String path){
        String CLAS12DIR = System.getenv("CLAS12DIR");
        String fullpath  = CLAS12DIR + "/lib/database/" + path;
        System.err.println("[CLAS-DATABASE] -----> load file : " + fullpath);
         try {
            //File file = new File(filename);
            File stream = new File(fullpath);
            JAXBContext jaxbContext = JAXBContext.newInstance(ConstantDatabaseXML.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            ConstantDatabaseXML group = (ConstantDatabaseXML) jaxbUnmarshaller.unmarshal(stream);            
            return group;
        } catch (JAXBException ex) {
            Logger.getLogger(ConstantDatabaseXML.class.getName()).log(Level.SEVERE, null, ex);
        }
         return null;
    }
    
    public void save(String filename){
        String CLAS12DIR = System.getenv("CLAS12DIR");
        String fullpath  = CLAS12DIR + "/lib/database/" + filename;
        System.err.println("[CLAS-DATABASE] -----> save file : " + fullpath);
        try {
            File file = new File(fullpath);
            JAXBContext jaxbContext = JAXBContext.newInstance(ConstantDatabaseXML.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(this, file);
        } catch (JAXBException ex) {
            Logger.getLogger(ConstantDatabaseXML.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void show(){
        System.err.println("************ CLAS DATABASE ************");
        for(ConstantItemInteger item : integerList){
            System.err.println(item.toString());
        }
        for(ConstantItemDouble item : doubleList){
            System.err.println(item.toString());
        }
    }
}
