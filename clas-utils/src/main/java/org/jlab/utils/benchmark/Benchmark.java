/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.utils.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author gavalian
 */
public class Benchmark {
    private final TreeMap<String,BenchmarkTimer> timerStore = new TreeMap<String,BenchmarkTimer>();
    public Benchmark(){
        
    }
    
    public void addTimer(String name){
        if(timerStore.containsKey(name)==true){
            System.err.println("[Benchmark] -----> error. timer with name ("
            + name + ") already exists");
        } else {
            BenchmarkTimer timer = new BenchmarkTimer(name);
            timerStore.put(timer.getName(), timer);
        }
    }
    
    public void pause(String name){
        if(timerStore.containsKey(name)==false){
            System.err.println("[Benchmark] -----> error. no timer defined with name ("
            + name + ")");
        } else {
            timerStore.get(name).pause();
        }
    }
    
    public void resume(String name){
        if(timerStore.containsKey(name)==false){
            System.err.println("[Benchmark] -----> error. no timer defined with name ("
            + name + ")");
        } else {
            timerStore.get(name).resume();
        }
    }
    public BenchmarkTimer  getTimer(String name){
        if(timerStore.containsKey(name)==true){
            return timerStore.get(name);
        }
        return null;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        ArrayList<String>  timerStrings = new ArrayList<String>();
        for(Map.Entry<String,BenchmarkTimer> timer : timerStore.entrySet()){
            timerStrings.add(timer.getValue().toString());
            //str.append(timer.getValue().toString());
            //str.append("\n");
        }
        
        if(timerStrings.size()>0){
            int len = timerStrings.get(0).length();
            char[]  asterix = new char[len+8];
            Arrays.fill(asterix,'*');
            String margins = new String(asterix);
            str.append(margins);
            str.append("\n");
            str.append("*     BENCHMARK  RESULTS \n");
            str.append(margins);
            str.append("\n");
            for(String lines : timerStrings){
                str.append("*   ");
                str.append(lines);
                str.append("   *\n");
            }
            str.append(margins);
            str.append("\n");
        }
        
        return str.toString();
    }
}
