package org.jlab.rec.rich;

/**
 * @author mcontalb
 */
public class RICHTime{

    private final static int NTIME = 10;
    private long RICH_START_TIME = (long) 0;
    private double richprocess_time[] = new double[NTIME];
    private int richprocess_ntimes[] = new int[NTIME];

    private double  SHOW_PROGRESS_INTERVAL = 1.e9;    // as default, do not print anything



    //------------------------------
    public RICHTime() {
    //------------------------------
    }


    // ----------------
    public void init_ProcessTime(){
    // ----------------

        int debugMode = 0;

        for(int i=0; i<NTIME; i++){richprocess_time[i] = 0.0; richprocess_ntimes[i]=0;}

        RICH_START_TIME = System.nanoTime();

        if(debugMode==1)System.out.format("RICH_INIT_TIME %d \n",RICH_START_TIME);
    }


    // ----------------
    public void save_ProcessTime(int iphase, long reftime){
    // ----------------

        int debugMode = 0;

        if(iphase>-1 && iphase<NTIME){

            long newtime = System.nanoTime();
            double dtime = (double) (newtime - reftime) * 1.0e-6;
            double stime = (double) reftime * 1.0e-9;

            richprocess_time[iphase] += dtime;
            richprocess_ntimes[iphase] += 1;

            if(debugMode==1)System.out.format("Phase %3d: Save time %3d  %10.4f vs %10.4f \n", iphase, richprocess_ntimes[iphase], dtime, stime);

        }

    }


    // ----------------
    public void dump_ProcessTime(){
    // ----------------

        String str[] = {" INIT     "," CCDB     ", " RAW-RICH " ," DC-RICH  ", " HADRONS  ", " ANALYTIC ", " TRACED   ", " WRITE    ", " CLOSE    "};

        for(int i=0; i<NTIME; i++){
            double time = 0.0;
            if(richprocess_ntimes[i]>0){
                int found=-1;
                for(int j=i-1; j>-1; j--){
                    if(richprocess_ntimes[j]>0){found=j; break;}
                }
                if(found>-1){
                    time = (richprocess_time[i]/richprocess_ntimes[i]-richprocess_time[found]/richprocess_ntimes[found]);
                }else{
                    time = richprocess_time[i]/richprocess_ntimes[i];
                }
                System.out.format(" PHASE %3d: %s  average over %6d  time %10.4f ms \n", i, str[i], richprocess_ntimes[i], time);
            }
        }

        /*for(int i=NTIME-1; i>-1; i--){
            double time = 0.0;
            if(richprocess_ntimes[i]>0){
                time = richprocess_time[i]/richprocess_ntimes[i];
                System.out.format(" PHASE %3d:  TOTAL      average over %6d  time %10.4f ms \n", NTIME, richprocess_ntimes[i], time);
                break;
            }
        }*/
    }

}
