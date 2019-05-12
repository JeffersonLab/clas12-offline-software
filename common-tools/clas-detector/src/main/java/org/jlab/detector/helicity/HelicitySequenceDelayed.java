package org.jlab.detector.helicity;

/**
 *
 * Just adding a delay to HelicitySequence, where delay is the number
 * of "windows".  For example, a quartet contains 4 windows.
 * 
 * @author baltzell
 */
public class HelicitySequenceDelayed extends HelicitySequence {
  
    private int delay;

    public HelicitySequenceDelayed(int delay) {
        this.delay=delay;
    }

    public void setDelay(int delay) {
        this.delay=delay;
    }
    
    @Override
    public HelicityBit get(int n) {
        return super.get(n+delay);
    }
    
    @Override
    public HelicityBit find(long timestamp) {
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
        HelicityState prev=super.getState(0);
        for (int ii=0; ii<this.size(); ii++) {
            if (super.getState(ii).getPatternSync()==HelicityBit.PLUS) continue;
            System.out.println(String.format("%4d %6s %6s %6s",
                    ii,
                    super.getState(ii).getInfo(prev,ii),
                    super.getState(ii).getHelicity(),
                    super.getPrediction(ii)));
            prev=super.getState(ii);
        }
    }
}
