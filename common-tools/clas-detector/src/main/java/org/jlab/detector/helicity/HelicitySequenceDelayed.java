package org.jlab.detector.helicity;

/**
 *
 * Just adding a delay to HelicitySequence, where delay is the number
 * of "windows".  For example, a quartet pattern contains 4 windows, and
 * if the helicity clock is the usual 29.56 Hz, a window lasts 33.829 ms.
 * 
 * @author baltzell
 */
public class HelicitySequenceDelayed extends HelicitySequence {
  
    private int delay;

    public HelicitySequenceDelayed(int delay) {
        this.delay=delay;
    }

    /**
     * Set the number of windows delayed.
     * 
     * @param delay 
     */
    public void setDelay(int delay) {
        this.delay=delay;
    }
    
    /**
     * Get the delay-corrected nth state in the measured sequence.
     * 
     * @param n the index of the state, where 0 corresponds to the first state
     * @return the helicity state, null if outside the mesaured range
     */
    @Override
    public HelicityBit get(int n) {
        return super.get(n+delay);
    }
    
    /**
     * Find the delay-corrected state corresponding to a given timestamp in the
     * measured sequence.
     * 
     * @param timestamp TI-timestamp, i.e. RUN::config.timestamp
     * @return the helicity state, null if timestamp is outside of measured range
     */
    @Override
    public HelicityBit find(long timestamp) {
        return this.get(super.findIndex(timestamp));
    }

    
    /**
     * Predict the delay-correct nth state in the sequence.
     * 
     * This uses the pseudo-random sequence of the helicity hardware to
     * generate the sequence into the infinite future and requires that enough
     * states were provided to initialize it.  Returns null if generator cannot
     * be initialized or the state is before the measured ones (i.e. negative).
     *
     * @param n the index of the state
     * @return the helicity bit
     */
    @Override
    public HelicityBit getPrediction(int n) {
        return super.getPrediction(n+delay);
    }

    /**
     * Predict the delay-corrected state of a TI timestamp.
     * 
     * This uses the pseudo-random sequence of the helicity hardware to
     * generate the sequence into the infinite future and requires that enough
     * states were provided to initialize it.  Returns null if generator cannot
     * be initialized or timestamp is before the measured ones.
     * 
     * @param timestamp TI-timestamp (i.e. RUN::config.timestamp)
     * @return the helicity bit
     */
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
