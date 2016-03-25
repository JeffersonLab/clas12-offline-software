/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gavalian
 */
public class CoatUtilsJar {
    
    public static List<String>  getClassList(String jarfile){
        ArrayList<String>  classList = new ArrayList<String>();
        try {            
            JarFile jarFile = new JarFile(jarfile);
            Enumeration e = jarFile.entries();
            
            //URL[] urls = { new URL("jar:file:" + jarfile+"!/") };
            //URLClassLoader cl = URLClassLoader.newInstance(urls);
            URL[] urls = {new URL("jar:file:" + jarfile + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls);
             
            while (e.hasMoreElements()) {
                JarEntry je = (JarEntry) e.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class")){
                    continue;
                }
                String className = je.getName().substring(0,je.getName().length()-6);
                className = className.replace('/', '.');
                //System.err.println("CLASS = " + className);
                //Class c = cl.loadClass(className);
                classList.add(className);

            }
        } catch (IOException ex) {
            System.err.println("Warning: file is not a proper jar : " + jarfile);
            //Logger.getLogger(JarPluginLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return classList;
    }
    
    
    public static List<String>  scanJarFile(String jarfile, String superclass){
        
        ArrayList<String>  scanclasses = new ArrayList<String>();
        List<String> classList = CoatUtilsJar.getClassList(jarfile);
        System.out.println("---> scanning file : " + jarfile + " for class [" + 
                superclass + "]");
        //Class interface = Class.forName(superclass);
        try {
            Class ci = Class.forName(superclass);
            for(String itemClass : classList){
                if(itemClass.startsWith("org.jlab.clas")||itemClass.startsWith("org.clas")){
                    try {
                        Class c = Class.forName(itemClass);
                        
                        if(c!=null){
                            //if(c.isAssignableFrom())
                            boolean  flag = ci.isAssignableFrom(c);
                            //System.out.println("class : " + itemClass + "  flag = " + flag);
                            if(flag==true){
                                scanclasses.add(c.getName());
                                //System.out.println("CLASS : [" + itemClass + "] superclass ["
                                //        + c.getSuperclass().getName() + "]");
                                //System.out.println("CLASS " + itemClass);
                            }
                        } else {
                            System.out.println("Warning: unable to process file : " + jarfile);
                        }
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(CoatUtilsJar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (ClassNotFoundException exh){
            
        }
        return scanclasses;
    }
    
    public static Map<String,List<String> >  scanDirectory(String directory, String supeclass){
        Map<String,List<String> >  jarClasses = new TreeMap<String,List<String> >();
        if(directory!=null){
            List<String>  jarFiles = CoatUtilsFile.getFileList(directory);
            for(String item : jarFiles){
                List<String>  cList = CoatUtilsJar.scanJarFile(item, supeclass);
                int index = item.lastIndexOf("/");
                jarClasses.put(item.substring(index+1, item.length()), cList);
            }
        }
        return jarClasses;
    }
}
