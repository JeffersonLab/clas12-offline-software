/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.calib.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.system.ClasUtilsFile;

/**
 *
 * @author gavalian
 */
public class CalibrationEngineTask {
    
    private List<CalibrationEngine>  calibrationEngines = new ArrayList<CalibrationEngine>();
    
    public CalibrationEngineTask(){
        
    }
    
    public void addEngine(String name)  {
        try {
            Class engineInterface = Class.forName("org.jlab.detector.calib.tasks.CalibrationEngine");
            Class engine = Class.forName(name);
            boolean flag = engineInterface.isAssignableFrom(engine);
            if(flag == true){
                CalibrationEngine listener = (CalibrationEngine) engine.newInstance();
                this.calibrationEngines.add(listener);                
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CalibrationEngineTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(CalibrationEngineTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CalibrationEngineTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void processDir(String directory, int maxFiles){
        
        System.out.println("[Processing directory]");
        System.out.println("[Data Listener size = ] " + this.calibrationEngines.size());
        
        List<String>  fileList = ClasUtilsFile.getFileList(directory);
        
        int evCounter = 0;
        int nFilesToProcess = fileList.size();
        if(nFilesToProcess>maxFiles) nFilesToProcess = maxFiles;
        
        for(int i = 0; i < nFilesToProcess; i++){
            EvioSource dataSource = new EvioSource();
            dataSource.open(fileList.get(i));
            System.out.println("[oppening file] --->  opened file # " + i + " file : " + fileList.get(i));
            while(dataSource.hasEvent()){
                DataEvent event = dataSource.getNextEvent();
                if(evCounter==0){
                    event.setType(DataEventType.EVENT_START);
                } else {
                    event.setType(DataEventType.EVENT_ACCUMULATE);
                }
                evCounter++;
                for(IDataEventListener listener : this.calibrationEngines){
                    listener.dataEventAction(event);
                    if(evCounter%15000==0){
                        System.out.println("-------> updating timers at EVENT # " + evCounter);
                        listener.timerUpdate();
                    }
                }                
            }            
        }
        
        DataEvent event = EvioFactory.createEvioEvent();
        event.setType(DataEventType.EVENT_STOP);
        for(IDataEventListener listener : this.calibrationEngines){
            listener.dataEventAction(event);
        }
    }
    
    
    public void showResults(){
        JFrame frame = new JFrame();
        //CalibrationConstantsView view = new CalibrationConstantsView();
        CalibrationEngineView  view = new CalibrationEngineView(calibrationEngines.get(0));
        frame.add(view);
        /*for(CalibrationEngine engine : this.calibrationEngines){
            List<CalibrationConstants> calibConsts = engine.getCalibrationConstants();
            view.addConstants(calibConsts);
        }*/
        
        frame.setSize(600, 600);
        frame.setVisible(true);
    }
    
    public static void main(String[] args){
        String directory = args[0];
        String nfilesstr = args[1];
        
        Integer nfiles = Integer.parseInt(nfilesstr);
        List<String>  engineNames = new ArrayList<String>();
        for(int i = 2; i < args.length; i++){
            engineNames.add(args[i]);
        }
        
        CalibrationEngineTask task = new CalibrationEngineTask();
        for(int i = 0 ; i < engineNames.size(); i++){
            task.addEngine(engineNames.get(i));
        }
        task.processDir(directory, nfiles);
        task.showResults();
    }
}
