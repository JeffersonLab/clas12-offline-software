package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


/**
 *
 * @author baltzell
 */
public class CherenkovResponse extends DetectorResponse {

    public static class TrackResidual implements Comparable {
        private final double coneAngle;
        private final double dTheta;
        private final double dPhi;
        public double getDeltaTheta() { return this.dTheta; }
        public double getDeltaPhi()   { return this.dPhi; }
        public double getConeAngle()  { return this.coneAngle; }
        public TrackResidual(CherenkovResponse cher, Line3D track) {
            Point3D intersection = cher.getIntersection(track);
            Vector3D vecTrk = intersection.toVector3D();
            Vector3D vecHit = cher.hitPosition.toVector3D();
            this.dTheta = vecHit.theta()-vecTrk.theta();
            this.dPhi = cher.getDeltaPhi(vecHit.phi(),vecTrk.phi());
            this.coneAngle = vecHit.angle(vecTrk);
        }
        @Override
        public int compareTo(Object o) {
            TrackResidual other = (TrackResidual)o;
            if (this.coneAngle < other.getConeAngle()) return -1;
            if (this.coneAngle > other.getConeAngle()) return  1;
            return 0;
        }
    }

    // theta/phi resolution for this response:
    private double  hitDeltaPhi   = 0.0;
    private double  hitDeltaTheta = 0.0;
    
    // FIXME:  remove this, use DetectorResponse's hitPosition instead:
    private final Point3D hitPosition = new Point3D();

    public CherenkovResponse() {
        super();
    }

    public CherenkovResponse(CherenkovResponse r) {
        super();
        this.copy(r);
    }

    public CherenkovResponse(double dtheta, double dphi){
        hitDeltaTheta  = dtheta;
        hitDeltaPhi    = dphi;
    }

    public void copy(CherenkovResponse r) {
        super.copy(r);
        hitDeltaPhi = r.hitDeltaPhi;
        hitDeltaTheta = r.hitDeltaTheta;
        hitPosition.copy(r.hitPosition);
    }

    public double getNphe() {return this.getEnergy(); }
    public double getDeltaTheta(){ return this.hitDeltaTheta;}
    public double getDeltaPhi() {return this.hitDeltaPhi;}

    public Point3D getHitPosition(){
        return this.hitPosition;
    }

    public void setHitPosition(double x, double y, double z){
        this.hitPosition.set(x, y, z);
    }

    /**
     * Wrap delta-phi into -pi/pi:
     */
    public double getDeltaPhi(double phi1, double phi2) {
        return Math.IEEEremainder(phi1-phi2,2.*Math.PI);
    }

    public Point3D getIntersection(Line3D line){
        Vector3D vec = new Vector3D(this.hitPosition.x(),this.hitPosition.y(),this.hitPosition.z());
        vec.unit();        
        Plane3D plane = new Plane3D(this.hitPosition,vec);
        Point3D intersect = new Point3D();
        plane.intersection(line, intersect);
        return intersect;
    }
    
    public TrackResidual getTrackResidual(Line3D particleTrack) {
        return new TrackResidual(this,particleTrack);
    }

    public TrackResidual getTrackResidual(DetectorParticle particle) {
        return new TrackResidual(this,this.getCross(particle));
    }

    public Line3D getCross(DetectorParticle part) {
        if (this.getDescriptor().getType() == DetectorType.HTCC)
            return part.getFirstCross();
        else if (this.getDescriptor().getType() == DetectorType.LTCC)
            return part.getLastCross();
        else
            throw new RuntimeException("DetectorParticle:getCheckr5noSignal:  invalid type:  "+this.getDescriptor().getType());
    }

    public int findClosestTrack(List<DetectorParticle> parts) {
        int best=-1;
        TrackResidual trBest=null;
        for (int ipart=0; ipart<parts.size(); ipart++) {
            if (parts.get(ipart).getCharge()==0) continue;
            if (parts.get(ipart).getTrackDetectorID()!=DetectorType.DC.getDetectorId()) continue;
            if (parts.get(ipart).countResponses(this.getDescriptor().getType()) > 0) continue;
            TrackResidual tr=getTrackResidual(parts.get(ipart));
            if (trBest == null || tr.compareTo(trBest)<0) {//getConeAngle() < trBest.getConeAngle()) {
                if (tr.getDeltaTheta() < this.getDeltaTheta() &&
                    tr.getDeltaPhi() < this.getDeltaPhi()) {
                    trBest=tr;
                    best=ipart;
                }
            }
        }
        return best;
    }

    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type){        
        List<DetectorResponse> responseList = new ArrayList<>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){

                double x = bank.getFloat("x",row);
                double y = bank.getFloat("y",row);
                double z = bank.getFloat("z",row);
                double time = bank.getFloat("time",row);
                double nphe = bank.getFloat("nphe", row);
                double theta = Math.atan2(Math.sqrt(x*x+y*y),z);
                double phi   = Math.atan2(y,x);
                int sector = 0;

                // FIXME:  move these constants to CCDB
                double dtheta=0,dphi=0;
                if (type==DetectorType.HTCC) {
                    dtheta = 10*3.14159/180; // based on MC
                    dphi   = 18*3.14159/180; // based on MC
                    // HTCC reconstruction does not provide a sector,
                    // so we calculate it based on hit position:
                    sector = DetectorResponse.getSector(phi);
                }
                else if (type==DetectorType.LTCC) {
                    dtheta = (35-5)/18*2 * 3.14159/180; // +/- 2 mirrors
                    dphi   = 10*3.14159/180;
                    sector = bank.getByte("sector",row);
                }
                else {
                    throw new RuntimeException(
                            "CherenkovResponse::readHipoEvent:  invalid DetectorType: "+type);
                }

                CherenkovResponse che = new CherenkovResponse(dtheta,dphi);
                che.setHitPosition(x, y, z);
                che.setHitIndex(row);
                che.setEnergy(nphe);
                che.setTime(time);
                che.getDescriptor().setSector(sector);
                che.getDescriptor().setType(type);
                che.getDescriptor().setLayer(1);

                // only LTCC currently reports status:
                if (type==DetectorType.LTCC) che.setStatus(bank.getInt("status",row));
                
                responseList.add((DetectorResponse)che);
            }
        }
        return responseList;
    }
    
}

