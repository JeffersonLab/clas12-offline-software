package org.jlab.service.pid;

import static java.lang.Math.abs;
import java.util.HashMap;




/**
 *
 * @author jnewton
 */
public class PIDExamination {
    
 

    private Integer CandidateID = -1;
    
    private Boolean closestBeta = null;
    private Boolean hasHTCC = null;
    private Boolean hasLTCC = null;
    private Boolean hasFTOF = null;
    private Boolean hasCND = null;
    private Boolean correctSF = null;
    private Boolean HTCCThreshold = null;
    private Boolean LTCCThreshold = null;

    public PIDExamination(){
        
    }
    

    public void   setCandidateID(int x){this.CandidateID = x;}
    public void   setClosestBeta(Boolean beta){this.closestBeta = beta;}
    public void   setHTCC(Boolean htcc){this.hasHTCC = htcc;}
    public void   setLTCC(Boolean ltcc){this.hasLTCC = ltcc;}
    public void   setFTOF(Boolean ftof){this.hasFTOF = ftof;}
    public void   setCorrectSF(Boolean sf){this.correctSF = sf;}
    public void   setHTCCThreshold(Boolean htcc){this.HTCCThreshold = htcc;}
    public void   setLTCCThreshold(Boolean ltcc){this.LTCCThreshold = ltcc;}
    
    public Boolean compareExams(PIDExamination mapexam){
        Boolean truth = true;
        if(mapexam.closestBeta!=null){
            if(this.closestBeta!=mapexam.closestBeta){
                truth = false;
            }
        }
        
        if(mapexam.hasHTCC!=null){
            if(this.hasHTCC!=mapexam.hasHTCC){
                truth = false;
            }
        }
        
        if(mapexam.HTCCThreshold!=null){
            if(mapexam.HTCCThreshold!=this.HTCCThreshold){
                truth = false;
            }
        }
        
        if(mapexam.hasLTCC!=null){
            if(this.hasLTCC!=mapexam.hasLTCC){
                truth = false;
            }
        }
        
        if(mapexam.LTCCThreshold!=null){
            if(this.LTCCThreshold!=mapexam.LTCCThreshold){
                truth = false;
            }
        }
        
        if(mapexam.correctSF!=null){
            if(this.correctSF!=mapexam.correctSF){
                truth = false;
            }
        }

        return truth;
    }

    public Boolean SamplingFractionCheck(DetectorParticle particle){ //Checks if particle falls within electron sampling fraction
        double p =  particle.vector().mag();
        Boolean check = false;
        double sf_calc =  particle.CalculatedSF();
        double sf_expect =  particle.ParametrizedSF();
        double sf_sigma = particle.ParametrizedSigma();
        if(sf_calc<=(sf_expect+5*sf_sigma) && sf_calc>=(sf_expect-5*sf_sigma) && p!=0.0){ //Is the calculated sampling fraction within 5 sigma
              check = true;
              }
           return check;
        }

    public Boolean HTCCSignal(DetectorParticle particle) {
        String str = "htcc";
        Boolean truth = false;
        if(particle.getNphe(str)>0){
            truth = true;
        }
        return truth;
    }
    
    public Boolean LTCCSignal(DetectorParticle particle) {
        String str = "ltcc";
        Boolean truth = false;
        if(particle.getNphe(str)>0){
            truth = true;
        }
        return truth;
    }
    
    public Boolean HTCCThreshold(DetectorParticle particle) {
        Boolean truth = false;
        if(particle.vector().mag()>4.9){
            truth = true;
        }
         return truth;
    }
    
   public Boolean LTCCThreshold(DetectorParticle particle) {
        Boolean truth = false;
        if(particle.vector().mag()>3 && particle.vector().mag()<5){
            truth = true;
        }
         return truth;
    }
    
    public Boolean GetBeta(DetectorParticle particle , int pid){
            Boolean truth = false;
            HashMap<Integer,Double> dBetas= new HashMap<Integer,Double>();
            dBetas.put(0,abs(particle.getTheoryBeta(11) - particle.getBeta()));
            dBetas.put(1,abs(particle.getTheoryBeta(2212) - particle.getBeta()));
            dBetas.put(2,abs(particle.getTheoryBeta(211) - particle.getBeta()));
            dBetas.put(3,abs(particle.getTheoryBeta(321) - particle.getBeta()));
            double min = dBetas.get(0);
            int index = 0,id=0;
            for (int i = 0; i <= 3; i++) {
             //   System.out.println(dBetas.get(i));
                if (dBetas.get(i) < min) {
                min = dBetas.get(i);
                index = i;
                }
            }
            if(index==0){
                id=11;
            }
            if(index==1){
                id=2212;
            }
            if(index==2){
                id=211;
            }
            if(index==3){
                id=321;
            }
            if(id==pid){
                truth = true;
            }
            if(particle.getBeta()==0.0){
                truth = false;
            }
            return truth;
    }

     public HashMap<Integer,PIDExamination> getElectronTests(DetectorParticle particle) {
                
                HashMap<Integer,PIDExamination>  electrontests = new HashMap<Integer, PIDExamination>();
               
                PIDExamination test1 = new PIDExamination();
                test1.setClosestBeta(true);
                test1.setHTCC(true);
                test1.setCorrectSF(true);
         
                
                PIDExamination test2 = new PIDExamination();
                test2.setClosestBeta(false);
                test2.setHTCC(true);
                test2.setCorrectSF(true);
                
                PIDExamination test3 = new PIDExamination();
                test3.setClosestBeta(true);
                test3.setHTCC(false);
                test3.setLTCC(false);
                
                PIDExamination test4 = new PIDExamination();
                test4.setClosestBeta(true);
                test4.setHTCC(true);
                test4.setCorrectSF(false);
                test4.setHTCCThreshold(false);
                
                PIDExamination test5 = new PIDExamination();
                test5.setClosestBeta(false);
                test5.setHTCC(true);
                test5.setCorrectSF(false);
                test5.setHTCCThreshold(false);
                
                electrontests.put(0,test1);
                electrontests.put(1,test2);
                electrontests.put(2,test3);
                electrontests.put(3,test4);
                electrontests.put(4,test5);
   
                return electrontests;
            }
    
      public HashMap<Integer,PIDExamination> getPionTests(DetectorParticle particle) {
                
                HashMap<Integer,PIDExamination>  piontests = new HashMap<Integer, PIDExamination>();
               
                PIDExamination test1 = new PIDExamination();
                test1.setClosestBeta(true);
                test1.setHTCC(false);
                test1.setCorrectSF(false);
              
                
                PIDExamination test2 = new PIDExamination();
                test2.setClosestBeta(true);
                test2.setHTCC(true);
                test2.setCorrectSF(false);
                test2.setHTCCThreshold(true);
                
                PIDExamination test3 = new PIDExamination();
                test3.setClosestBeta(false);
                test3.setHTCC(true);
                test3.setCorrectSF(false);
                test3.setHTCCThreshold(true);
                
                PIDExamination test4 = new PIDExamination();
                test4.setClosestBeta(true);
                test4.setHTCC(false);
                test4.setLTCC(true);
                test4.setLTCCThreshold(true);
                
                PIDExamination test5 = new PIDExamination();
                test5.setClosestBeta(false);
                test5.setHTCC(true);
                test5.setLTCC(false);
                test5.setLTCCThreshold(false);
                
                piontests.put(0,test1);
                piontests.put(1,test2);
                piontests.put(2,test3);
                piontests.put(3,test4);
                piontests.put(4,test5);

                return piontests;
            }                

                
      public HashMap<Integer,PIDExamination> getProtonTests(DetectorParticle particle) {
                
                HashMap<Integer,PIDExamination>  protontests = new HashMap<Integer, PIDExamination>();
               
                PIDExamination test1 = new PIDExamination();
                test1.setClosestBeta(true);
                test1.setHTCC(false);
                //test1.setLTCC(false);

                //PIDExamination test2 = new PIDExamination();
                //test2.setClosestBeta(true);
                //test2.setHTCC(false);
                //test2.setLTCC(true);
                //test2.setLTCCThreshold(false);
                
                protontests.put(0, test1);
                //protontests.put(1, test2);
                
                return protontests;
            }
    
        public HashMap<Integer,PIDExamination> getKaonTests(DetectorParticle particle) {
                
                HashMap<Integer,PIDExamination>  kaontests = new HashMap<Integer, PIDExamination>();
               
                PIDExamination test1 = new PIDExamination();
           
                test1.setClosestBeta(true);
                test1.setHTCC(false);
                //test1.setLTCC(false);
                
                //PIDExamination test2 = new PIDExamination();
                //test2.setClosestBeta(true);
                //test2.setHTCC(false);
                //test2.setLTCC(true);
                //test2.setLTCCThreshold(false);
                    
                kaontests.put(0, test1);

                return kaontests;
            }     
        
        public HashMap<Integer,PIDExamination> getPhotonTests(DetectorParticle particle) {
             HashMap<Integer, PIDExamination> photontests = new HashMap<Integer, PIDExamination>();
             return photontests;
        }
    
    public Integer getCandidateID(){return this.CandidateID;}
    public Boolean getClosestBeta(){return this.closestBeta;}
    public Boolean getHTCC(){ return this.hasHTCC; }
    public Boolean getLTCC(){ return this.hasLTCC;}
    public Boolean getFTOF(){return this.hasFTOF;}
    public Boolean getCorrectSF() {return this.correctSF;}
    public Boolean getHTCCThreshold() {return this.HTCCThreshold;}
    public Boolean getLTCCThreshold() {return this.LTCCThreshold;}

        @Override
	public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("\t [HTCC?/Sampling Fraction?/HTCC Threshold?/Beta?] [%8b %3b %3b %3b] ", 
				 this.hasHTCC,
				 this.correctSF,
				 this.HTCCThreshold,
				 this.closestBeta
        ));

        return str.toString();
    }


}
