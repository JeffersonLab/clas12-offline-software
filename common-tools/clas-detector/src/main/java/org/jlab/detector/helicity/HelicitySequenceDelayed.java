package org.jlab.detector.helicity;

import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.io.hipo.HipoDataSource;

// this will allow access to tagged events:
//import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * Just adding a delay to HelicitySequence, where delay is the number
 * of "windows".  For example, a quartet contains 4 windows.
 * 
 * @author baltzell
 */
public class HelicitySequenceDelayed extends HelicitySequence {
  
    private final int delay;

    public HelicitySequenceDelayed(int delay) {
        this.delay=delay;
    }
    
    @Override
    public HelicityState get(int n) {
        return super.get(n+delay);
    }
    
    @Override
    public HelicityState find(long timestamp) {
        return this.get(super.findIndex(timestamp));
    }

    @Override
    public HelicityBit getPrediction(int n) {
        return super.getPrediction(n+delay);
    }

    @Override
    public HelicityBit findPrediction(long timestamp) {
        return this.getPrediction(super.findIndex(timestamp));
    }
    
    @Override
    public void show() {
        HelicityState prev=super.get(0);
        for (int ii=0; ii<this.size(); ii++) {
            if (super.get(ii).getQuartet()==HelicityBit.PLUS) continue;
            System.out.println(String.format("%4d %6s %6s %6s",
                    ii,
                    super.get(ii).getInfo(prev,ii),
                    super.get(ii).getHelicity(),
                    super.getPrediction(ii)));
            prev=super.get(ii);
        }
    }
    
    public static void main(String[] args) {

        HelicitySequenceDelayed seq=new HelicitySequenceDelayed(8);

        final String dir="/Users/baltzell/data/CLAS12/rg-b/decoded/";
        final String file="clas_006432.evio.00041-00042.hipo";

        HipoDataSource reader=new HipoDataSource();
        reader.open(dir+file);

        while (reader.hasEvent()) {
            DataEvent event = reader.getNextEvent();
            if (!event.hasBank("HEL::flip")) continue;
            DataBank bank=event.getBank("HEL::flip");
            HelicityState state=HelicityState.createFromFlipBank(bank);
            seq.addState(state);
        }

        seq.show();

        reader.close();
        
        HipoDataSource reader2=new HipoDataSource();
        reader2.open(dir+file);
        
        // just use this to count unique flips to curtail printouts:
        HelicitySequence seq2=new HelicitySequence();

        int nevents=0;
        int nflips=0;
        while (reader2.hasEvent()) {
            nevents++;
            DataEvent event = reader2.getNextEvent();
            if (!event.hasBank("RUN::config")) continue;
            if (event.hasBank("HEL::flip")) {
                if (seq2.addState(HelicityState.createFromFlipBank(event.getBank("HEL::flip")))) {
                    nflips++;
                }
            }
            if (nflips<310) continue;
            if (nevents%100!=0) continue;
            //if (nflips>200) break;
            final long timestamp = event.getBank("RUN::config").getLong("timestamp",0);
            //final byte l3 = event.getBank("RUN::config").getByte("helicityL3",0);
            
            final byte l3 = event.getBank("HEL::online").getByte("helicity",0);
            
            final byte predicted = seq.findPrediction(timestamp)==null ? -9 : seq.findPrediction(timestamp).value();
            
            final byte measured = seq.find(timestamp)==null ? -9 : seq.find(timestamp).getHelicity().value();
            
            System.out.println(String.format("%d %5d l3=%+d l4b=%+d l4c=%+d",
                    timestamp,nflips,l3,predicted,measured));
        }

    }
}
