/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;

/**
 *
 * @author gavalian
 */
public class EvioObjectReader {
    public static void readObject(EvioDataEvent event, Object obj){
        if (obj.getClass().isAnnotationPresent((Class<? extends Annotation>) obj.getClass())) {
            Annotation annotation = obj.getClass().getAnnotation( (Class<? extends Annotation>) obj.getClass());
            //System.out.println(annotation);
        }
        
        for(Method method : obj.getClass().getDeclaredMethods()){
            //System.out.println(method);
            Annotation annotation = method.getAnnotation(EvioDataType.class);
            //System.out.println((EvioDataType) annotation);
            
            if(annotation==null) continue;
            try {
                EvioDataType type = (EvioDataType) annotation;
                System.out.println(" tag = " + type.tag() + " num " + type.num());
                

                int  container_tag = type.parent();
                int  leaf_tag      = type.tag();
                int  leaf_num      = type.num();
                String typeString  = type.type();
                /**
                 * parsing integer from the bank
                 */
                if(typeString.compareTo("int32")==0){
                    System.out.println("[] ---> Getting the integer " + leaf_tag
                            + "  " + leaf_num);
                    List<Integer> result = (List<Integer>) method.invoke(obj, null);
                    
                    int[]  array = event.getInt(leaf_tag, leaf_num);
                    if(array!=null){
                        System.out.println("[] ---> extracted array size = " + array.length);
                        result.clear();
                        for(int value : array){
                            result.add(value);
                        }
                    }
                }
                //method.invoke(obj, null);
                /**
                 * parsing double from the bank
                 */
                if(typeString.compareTo("double")==0){
                    System.out.println("[] ---> Getting the integer " + leaf_tag
                            + "  " + leaf_num);
                    List<Double> result = (List<Double>) method.invoke(obj, null);
                    
                    double[]  array = event.getDouble(leaf_tag, leaf_num);
                    if(array!=null){
                        System.out.println("[] ---> extracted array size = " + array.length);
                        result.clear();
                        for(double value : array){
                            result.add(value);
                        }
                    }
                }
                
            } catch (IllegalAccessException ex) {
                Logger.getLogger(EvioObjectReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(EvioObjectReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(EvioObjectReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String[] args){
        
        String filename = "/Users/gavalian/Work/Software/Release-8.0/COATJAVA/coatjava/../gemc_proton_10deg.evio";
        EvioSource reader = new EvioSource();
        reader.open(filename);
        DetectorBank ftof = new DetectorBank();
        
        int counter = 0;
        while(reader.hasEvent()==true){
            EvioDataEvent  event = (EvioDataEvent) reader.getNextEvent();
            EvioObjectReader.readObject(event, ftof);
        }
        System.out.println(ftof);
    }
}
