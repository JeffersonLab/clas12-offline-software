package org.jlab.service.pid;

import java.util.HashMap;




/**
 *
 * @author jnewton
 */
public class PIDResult {
    
   private int finalid=-1;
   private PIDExamination pidexam = new PIDExamination();
   private double timingquality = 0.0;
   HashMap<Integer,Double> ECTimingCheck = new HashMap<Integer,Double>();
   HashMap<Integer,Double> FTOFTimingCheck = new HashMap<Integer,Double>();
    
    public PIDResult(){
        
    }
    
    public void   setFinalID(int id){ this.finalid = id;}
    public void   setPIDExamination(PIDExamination exam){this.pidexam = exam;}
    public void   setTimingQuality(double time){this.timingquality = time;}
    public void   setECTimingCheck(HashMap<Integer,Double> ec){this.ECTimingCheck = ec;}
    public void   setFTOFTimingCheck(HashMap<Integer,Double> ftof){this.FTOFTimingCheck = ftof;}
    
    public int getFinalID(){ return this.finalid;}
    public double getTimingQuality() {return this.timingquality;}
    public PIDExamination getPIDExamination(){return this.pidexam;}
    public HashMap<Integer,Double> getECTimingCheck() {return this.ECTimingCheck;}
    public HashMap<Integer,Double> getFTOFTimingCheck() {return this.FTOFTimingCheck;}
    
    

}
