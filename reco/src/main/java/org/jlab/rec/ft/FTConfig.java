package org.jlab.rec.ft;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

public class FTConfig {

    public FTConfig() {
            // TODO Auto-generated constructor stub
    }
    private int Run;
    private double Solenoid;

    public int setRunConditionsParameters(DataEvent event, String Detector, int iRun) {
        if(event.hasBank("RUN::config")==false) {
                System.err.println("RUN CONDITIONS NOT READ!");
                return -1;
        }

        int Run    = iRun;
        int newRun = Run;        
        double fieldScale = 0;
        
        boolean isMC = false;
        boolean isCosmics = false;

        if(event instanceof EvioDataEvent) {
            EvioDataBank bank = (EvioDataBank) event.getBank("RUN::config");
            if(bank.getByte("Type")[0]==0)
                isMC = true;
            if(bank.getByte("Mode")[0]==1)
                isCosmics = true;
            newRun = bank.getInt("Run")[0];
            fieldScale = bank.getFloat("Solenoid")[0];
        }
        else {
            DataBank bank = event.getBank("RUN::config");
            if(bank.getByte("type")[0]==0)
                isMC = true;
            if(bank.getByte("mode")[0]==1)
                isCosmics = true;
            newRun = bank.getInt("run")[0];
            fieldScale = bank.getFloat("solenoid")[0];
        }
		
        Run = newRun;
        this.setRun(Run);
        this.setSolenoid(fieldScale);
        return Run;
    }

    public  int getRun() {
            return Run;
    }

    public  void setRun(int run) {
            Run = run;
    }

    public double getSolenoid() {
        return Solenoid;
    }

    public void setSolenoid(double Solenoid) {
        this.Solenoid = Solenoid;
    }

	
}
