package org.jlab.clas.detector.matching;

import java.util.Comparator;
import java.util.List;
import javafx.util.Pair;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;

/**
 *
 * @author baltzell
 */
public interface IMatch extends Comparator<Pair<DetectorParticle,DetectorResponse>> {
    
    public abstract boolean matches(DetectorParticle p, DetectorResponse r);

    public abstract double quality(DetectorParticle p, DetectorResponse r);

    public abstract int firstMatch(DetectorParticle p, List<DetectorResponse> r,
            DetectorType type, final int layer);

    public abstract int firstMatch(DetectorParticle p, List<DetectorResponse> r,
            DetectorType t);

    public abstract int bestMatch(DetectorParticle p, List<DetectorResponse> r,
            DetectorType type, final int layer);
    
    public abstract int bestMatch(DetectorParticle p, List<DetectorResponse> r,
            DetectorType type);

}
