/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.utils.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author gavalian
 */
public class ClasUtilsFile {
    
    private static String moduleString = "[ClasUtilsFile] --> ";
            
    public static String getName(){ return moduleString; }
    /**
     * prints a log message with the module name included.
     * @param log 
     */
    public static void printLog(String log){
        System.out.println(ClasUtilsFile.getName() + " " + log);
    }
    /**
     * returns package resource directory with given enviromental variable
     * and relative path.
     * @param env
     * @param rpath
     * @return 
     */
    public static String getResourceDir(String env, String rpath){
        
        String envString = System.getenv(env);
        if(envString==null){
            ClasUtilsFile.printLog("Environment variable ["+env+"] is not defined");
            envString = System.getProperty(env);
        }
        
        if(envString == null){
            ClasUtilsFile.printLog("System property ["+env+"] is not defined");
            return null;
        }
        
        StringBuilder str = new StringBuilder();
        int index = envString.length()-1;
        str.append(envString);
        //Char fileSeparator =
        if(envString.charAt(index)!='/' && rpath.startsWith("/")==false) str.append('/');
        str.append(rpath);        
        return str.toString();
    }
    /**
     * returns list of files in the directory. absolute path is given.
     * This function will not exclude ".*" and "*~" files.
     * @param directory
     * @return 
     */
    public static List<String>  getFileList(String directory){        
        List<String> fileList = new ArrayList<String>();
        File[] files = new File(directory).listFiles();
        System.out.println("FILE LIST LENGTH = " + files.length);
        for (File file : files) {
            if (file.isFile()) {
                if(file.getName().startsWith(".")==true||
                        file.getName().endsWith("~")){
                    System.out.println("[FileUtils] ----> skipping file : " + file.getName());
                } else {
                    fileList.add(file.getAbsolutePath());
                }
            }
        }
        return fileList;
    }
    /**
     * returns list of files in the directory defined by environment variable
     * and a relative path.
     * @param env
     * @param rpath
     * @return 
     */
    public static List<String>  getFileList(String env, String rpath){
        String directory = ClasUtilsFile.getResourceDir(env, rpath);
        if(directory==null){
            ClasUtilsFile.printLog("(error) directory does not exist : " + directory);
            return new ArrayList<String>();
        }
        return ClasUtilsFile.getFileList(directory);
    }
    /**
     * returns a file list that contains files with given extension
     * @param env
     * @param rpath
     * @param ext
     * @return 
     */
    public static List<String>  getFileList(String env, String rpath, String ext){
        String directory = ClasUtilsFile.getResourceDir(env, rpath);
        if(directory!=null) return new ArrayList<String>();
        
        List<String> files = ClasUtilsFile.getFileList(directory);
        List<String> selected = new ArrayList<String>();
        for(String item : files){
            if(item.endsWith(ext)==true) selected.add(item);
        }
        return selected;
    }
    
    public static void   writeFile(String filename, List<String> lines){
        System.out.println("writing file --->  " + filename);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            for(String line : lines){
                writer.write (line +"\n");
            }  writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ClasUtilsFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ClasUtilsFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Reads a text file into a list of strings  
     * @param filename
     * @return 
     */
    public static List<String>   readFile(String filename){
        List<String>  lines = new ArrayList<String>();
        String line = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =  new FileReader(filename);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =  new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                lines.add(line);
            }   
            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            ClasUtilsFile.printLog("Unable to open file : '" + filename + "'");             
        }
        catch(IOException ex) {
            ClasUtilsFile.printLog( "Error reading file : '" + filename + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
        return lines;
    }
    /**
     * Reads a text file into one string.
     * @param filename
     * @return 
     */
    public static String readFileString(String filename){
        List<String> lines = ClasUtilsFile.readFile(filename);
        StringBuilder str = new StringBuilder();
        for(String line : lines) str.append(line);
        return str.toString();
    }
    /**
     * Returs relative paths of file names from list of absolute paths.
     * @param files
     * @return 
     */
    public static List<String>  getFileNamesRelative(List<String> files){
        List<String>  newList = new ArrayList<String>();
        for(String file : files){
            int index = file.lastIndexOf('/');
            if(index>=0&&index<file.length()){
                newList.add(file.substring(index+1, file.length()));
            } else {
                newList.add(file);
            }
        }
        return newList;
    }
    
    /**
     * returns a new file name which is composed of the file name given and then by adding
     * given string to it. if flag preservePath is true, then file name will have the same
     * path as the original file name.
     * @param filename
     * @param addition
     * @param preservePath
     * @return 
     */
    public static String createFileName(String filename, String addition, boolean preservePath){
        
        String inputFile = filename;
        
        if(filename.contains("/")==true&&preservePath==false){
            int index_slash = filename.lastIndexOf("/");
            inputFile = filename.substring(index_slash+1,filename.length());
        }
        
        StringBuilder str = new StringBuilder();
        int index = inputFile.lastIndexOf(".");
        //int index = filename.lastIndexOf(".");
        str.append(inputFile.substring(0, index));
        str.append(addition);
        str.append(inputFile.substring(index, inputFile.length()));
        return str.toString();
    }
    
    public static void main(String[] args){
        String output_file = ClasUtilsFile.createFileName("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/coatjava/../datasets/gemc/eklambda/gemc_eklambda_A0001_gen.evio", "_header", true);
        System.out.println(output_file);
    }
}
