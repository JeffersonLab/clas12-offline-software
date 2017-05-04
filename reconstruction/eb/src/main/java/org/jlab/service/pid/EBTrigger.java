package org.jlab.service.pid;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.detector.*;




/**
 *
 * @author jnewton
 */
public class EventTrigger {
    
 private double zt=0.0;
 private double rftime=0.0;
 private double starttime=0.0;
 private double vertextime=0.0;
 private double correctionterm=0.0;
 public int     triggerid=-1;

 private DetectorParticle triggerparticle = new DetectorParticle();
 private HashMap<Integer,DetectorParticle> ElectronCandidates = new HashMap<Integer,DetectorParticle>();
 private HashMap<Integer,DetectorParticle> PositronCandidates = new HashMap<Integer,DetectorParticle>();
 private HashMap<Integer,DetectorParticle> NegativePionCandidates = new HashMap<Integer,DetectorParticle>();

    
    public EventTrigger(){
        
    }
    
    public void   setzt(double z_t){ this.zt = z_t;}
    public void   setRFTime(double rf){this.rftime = rf;}
    public void   setVertexTime(double t){this.vertextime = t;}
    public void   setStartTime(double start){this.starttime = start;}
    public void   setCorrectionTerm(double corr){this.correctionterm = corr;}
    public void   setTriggerParticle(DetectorParticle particle){this.triggerparticle=particle;}
    public void   setElectronCandidates(HashMap<Integer,DetectorParticle> ecandidates){this.ElectronCandidates = ecandidates;}
    public void   setPositronCandidates(HashMap<Integer,DetectorParticle> epluscandidates){this.PositronCandidates = epluscandidates;}
    public void   setNegativePionCandidates(HashMap<Integer,DetectorParticle> piminus){this.NegativePionCandidates = piminus;}
    public void   setTriggerID(int i){this.triggerid = i;}


    public DetectorParticle GetBestTriggerParticle(HashMap<Integer,DetectorParticle> TriggerCandidates) {
            DetectorParticle BestTrigger = new DetectorParticle();
            int SizeOfMap = TriggerCandidates.size();
            HashMap<Integer,Integer> Scores = new HashMap<Integer,Integer>();

            for(int i = 0 ; i < SizeOfMap; i++){
              Scores.put(i,TriggerCandidates.get(i).getScore()); 
            }
            int HighestScore = GetHighestScore(Scores);
            int SizeofMap = TriggerCandidates.size();
            List<Integer> IndicesThatMatchWithHighestScore = new ArrayList<Integer>();
            for(int i = 0 ; i < SizeofMap; i++){ 
                if(Scores.get(i)==HighestScore){
                    IndicesThatMatchWithHighestScore.add(i);
                }
            }
            HashMap<Integer,Double> Momenta = new HashMap<Integer,Double>();
            HashMap<Integer,Integer> IndexTranslation = new HashMap<Integer,Integer>();
            for(int i = 0 ; i < IndicesThatMatchWithHighestScore.size() ; i++){
                int index = IndicesThatMatchWithHighestScore.get(i);
                Momenta.put(i,TriggerCandidates.get(index).vector().mag());
                IndexTranslation.put(i,index);
            }
            int HighestMomentumIndex = GetHighestMomentumIndex(Momenta);
            int CorrectHighestMomentumIndex = IndexTranslation.get(HighestMomentumIndex);
            BestTrigger = TriggerCandidates.get(CorrectHighestMomentumIndex);
            
            return BestTrigger;
        }
        
    public DetectorParticle GetFastestTrack(HashMap<Integer,DetectorParticle> TriggerCandidates) {
            DetectorParticle FastestParticle = new DetectorParticle();
            HashMap<Integer,Double> momenta = new HashMap<Integer,Double>();
            int SizeOfMap = TriggerCandidates.size();
            for(int i = 0 ; i < SizeOfMap ; i++){
                momenta.put(i,TriggerCandidates.get(i).vector().mag());
            }
            FastestParticle = TriggerCandidates.get(GetHighestMomentumIndex(momenta));
            return FastestParticle;
        }
            
    public int GetHighestScore(HashMap<Integer,Integer> Sc) {
           int max = Sc.get(0);

            for (int i = 1; i < Sc.size(); i++) {
                if (Sc.get(i) > max) {
                max = Sc.get(i);
                }
            }
            
            return max;
        }
        
    public int GetHighestMomentumIndex(HashMap<Integer,Double> momentum) {
            Double max = momentum.get(0);
            int MaximumIndex = 0;
            for (int i = 1; i < momentum.size(); i++) {
                if (momentum.get(i) > max) {
                max = momentum.get(i);
                MaximumIndex = i;
                }
            }
            
            return MaximumIndex;
        }
    
       public double VertexTime(DetectorParticle particle, int usertriggerid) {
            double t_0r = 0.0;
            
            if(particle.hasHit(DetectorType.FTOF, 2)==true){
           
            double beta = 0.0;
            
            if(abs(usertriggerid)==11){
                beta = 1;//We assign electron beta if at least one track has responses in FTOF/HTCC
            }
            if(abs(usertriggerid)==211){
                beta = particle.getTheoryBeta(211); //We Assign Pion mass to the fastest negative track
            }
            if(abs(usertriggerid)==22){
                beta = particle.getTheoryBeta(22);
            }

            
            t_0r = particle.getTime(DetectorType.FTOF) - (particle.getPathLength(DetectorType.FTOF))/(29.9792*beta);//vertex time

            }
   
      
            return t_0r;
       }
    
       public double StartTime(DetectorParticle particle, int usertriggerid) {

            double deltatr = this.getVertexTime() - this.getRFTime() - (this.getZt() - (-4.5))/(29.9792)+800*2.004 + 1.3;
            //+800*2.0004+1.002
            double t_0corr = deltatr%2.004 - 2.004/2;//RF correction term
            this.setCorrectionTerm(t_0corr);
            double t_0 = this.getVertexTime() + t_0corr;//RF-Corrected Start Time
            return t_0;
            }
       
public int TriggerScenario() {
    int i = 0;
    int j = 1;
    boolean stop = false;
    while(stop!=true){
        if(j==1 && ElectronCandidates.size()>0){
            i = 1;
            stop = true;
          }
        if(j==2 && PositronCandidates.size()>0){
            i = 2;
            stop = true;
        }
        if(j==3 && NegativePionCandidates.size()>0){
            i = 3;
            stop = true;
        }
        if(j==4){
            stop = true;
        }
        j=j+1;
       }
    
      return i;
    }

    public double getZt(){ return this.zt;}
    public double getRFTime(){ return this.rftime; }
    public double getVertexTime(){return this.vertextime;}
    public double getStartTime(){ return this.starttime;}
    public double getCorrectionTerm() {return this.correctionterm;}
    public int    getTriggerID() {return this.triggerid;}
    public DetectorParticle getTriggerParticle(){return this.triggerparticle;}
    public HashMap<Integer,DetectorParticle> getElectronCandidates(){return this.ElectronCandidates;}
    public HashMap<Integer,DetectorParticle> getPositronCandidates(){return this.PositronCandidates;}
    public HashMap<Integer,DetectorParticle> getNegativePionCandidates(){return this.NegativePionCandidates;}


    @Override
	public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("\t [RF Time/Start Time/Vertex Time/Vertex Position] [%8f %3f %3f %3f] ", 
				 this.rftime,
				 this.starttime,
				 this.vertextime,
				 this.zt
				 ));

        return str.toString();
    }
        
    public void Print(){
            System.out.println("RF Time = " + this.rftime);
            System.out.println("Event Start Time = " + this.starttime);
            System.out.println("Trigger Vertex Time" + this.vertextime);
            System.out.println("Z Position of Vertex = " + this.zt);
    }

}


