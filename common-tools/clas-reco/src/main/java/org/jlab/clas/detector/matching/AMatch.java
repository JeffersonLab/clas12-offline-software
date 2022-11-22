package org.jlab.clas.detector.matching;

import java.util.List;
import javafx.util.Pair;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author baltzell
 */
public abstract class AMatch implements IMatch {

    private boolean sharing = false;

    public final void setSharing(boolean sharing) {
        this.sharing = sharing;
    }

    public final boolean getSharing() {
        return this.sharing;
    }

    public static double getDeltaPhi(double phi1, double phi2) {
        return Math.IEEEremainder(phi1-phi2,2.*Math.PI);
    }

    public static double getDeltaPhi(Point3D p1, Point3D p2) {
        return getDeltaPhi(Math.atan2(p1.y(),p1.x()),Math.atan2(p2.y(),p2.x()));
    }

    private int findMatch(DetectorParticle p, List<DetectorResponse> r,
            DetectorType type, final int layer, final boolean first) {
        int bestIndex = -1;
        double bestQuality = Double.POSITIVE_INFINITY;
        for (int i=0; i<r.size(); i++) {
            if (r.get(i).getAssociation()>=0 && !sharing) {
                continue;
            }
            if (type != null && r.get(i).getDescriptor().getType() != type) {
                continue;
            }
            if (layer >= 0 && r.get(i).getDescriptor().getLayer() != layer) {
                continue;
            }
            if (this.matches(p,r.get(i))) {
                if (first) {
                    return i;
                }
                else if (this.quality(p,r.get(i)) < bestQuality) {
                    bestQuality = this.quality(p,r.get(i));
                    bestIndex = i;
                }
            }
        }
        return bestIndex;
    }

    @Override
    public final int firstMatch(DetectorParticle p, List<DetectorResponse> r,
            DetectorType type, final int layer) {
        return this.findMatch(p, r, type, layer, true);
    }
    
    @Override
    public final int firstMatch(DetectorParticle p, List<DetectorResponse> r, DetectorType t) {
        return this.firstMatch(p, r, t, -1);
    }

    @Override
    public final int bestMatch(DetectorParticle p, List<DetectorResponse> r,
            DetectorType type, final int layer) {
        return this.findMatch(p, r, type, layer, false);
    }
    
    @Override
    public final int bestMatch(DetectorParticle p, List<DetectorResponse> r, DetectorType t) {
        return this.bestMatch(p, r, t, -1);
    }

    @Override
    public int compare(Pair<DetectorParticle,DetectorResponse> a,
                       Pair<DetectorParticle,DetectorResponse> b) {
        if (this.quality(a.getKey(),a.getValue()) <
            this.quality(b.getKey(),b.getValue())) {
            return 1;
        }
        if (this.quality(a.getKey(),a.getValue()) >
            this.quality(b.getKey(),b.getValue())) {
            return -1;
        }
        return 0;
    }
    
    /*
    public DetectorResponse findMatch2(DetectorParticle p, List<DetectorResponse> r) {
        List<Pair<DetectorParticle,DetectorResponse>> l = new ArrayList<>();
        for (DetectorResponse r1 : r) {
            l.add(new Pair<>(p,r1));
        }
        Collections.sort(l, this);
        return l.get(0).getValue();
    }
    */

}
