package org.jlab.service.pid;




/**
 *
 * @author jnewton
 */
public class TIDResult {
    

   private int score;
   private TIDExamination tidexam = new TIDExamination();


    
    public TIDResult(){
        
    }
    

    public void   setTIDExamination(TIDExamination exam){this.tidexam = exam;}
    public void   setScore(int x ){this.score = x;} 

    
    public int getScore(){return this.score;}
    public TIDExamination getTIDExamination(){return this.tidexam;}

    
}
