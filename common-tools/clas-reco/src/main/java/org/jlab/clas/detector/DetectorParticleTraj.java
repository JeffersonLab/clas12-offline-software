package org.jlab.clas.detector;

import java.util.List;
import org.jlab.clas.detector.DetectorTrack.TrajectoryPoint;
import org.jlab.geom.prim.Line3D;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.pdg.PhysicsConstants;

/**
 * Override methods to use new tracking trajectory surfaces.
 * (Currently unavailable for hit-based tracking.)
 * 
 */
public class DetectorParticleTraj extends DetectorParticle {
    
    public DetectorParticleTraj(DetectorTrack track){
        super(track);
    }

    @Override
    public int getDetectorHit(List<DetectorResponse>  hitList, DetectorType type,
            int detectorLayer,
            double distanceThreshold){
        
        // Protect against odd tracks that don't have trajectory intersection:
        if (detectorLayer<1) {
            if (!detectorTrack.getTrajectory().hasDetector(type.getDetectorId())) {
                return -1;
            }
        }
        else if (detectorTrack.getTrajectoryPoint(type.getDetectorId(),detectorLayer)==null) {
            return -1;
        }

        // FIXME:  replace with trajectory-based matching:
        return super.getDetectorHit(hitList, type, detectorLayer, distanceThreshold);
    }

    @Override
    public double getPathLength(DetectorType type,int layId) {
        return this.detectorTrack.getPathLength(type,layId);
    }
    
    @Override
    public double getBeta(DetectorType type, int layer, double startTime){
        DetectorResponse response = this.getHit(type,layer);
        if(response==null) return -1.0;
        final double cpath = this.detectorTrack.getPathLength(type,layer);
        final double ctime = response.getTime() - startTime;
        return cpath/ctime/PhysicsConstants.speedOfLight();
    }
    
    @Override
    public double getVertexTime(DetectorType type, int layer){
        return this.getVertexTime(type,layer,this.getPid());
    }

    @Override
    public double getVertexTime(DetectorType type, int layer, int pid){
        return this.getTime(type,layer) -
               this.getPathLength(type, layer) /
               (this.getTheoryBeta(pid)*PhysicsConstants.speedOfLight());
    }
  
    @Override
    public Line3D  getDistance(DetectorResponse  response){
        TrajectoryPoint tp=detectorTrack.getTrajectoryPoint(response.getDescriptor());
        if (tp!=null) {
            return new Line3D(tp.getCross().origin(), response.getPosition().toPoint3D());
        }
        return null;
    }

    @Override
    public void addResponse(DetectorResponse res, boolean match){
        this.responseStore.add(res);
        if(match==true){
            TrajectoryPoint tp=this.detectorTrack.getTrajectoryPoint(res.getDescriptor());
            if (tp!=null) {
                res.getMatchedPosition().setXYZ(tp.getCross().origin().x(),
                                                tp.getCross().origin().y(),
                                                tp.getCross().origin().z());
                res.setPath(tp.getPathLength());
            }
        }
    }

    @Override
    public int getCherenkovSignal(List<DetectorResponse> responses, DetectorType type){

        TrajectoryPoint tp=detectorTrack.getTrajectoryPoint(type.getDetectorId(), 1);
        if (tp==null) return -1;
        Line3D cross=tp.getCross();
       
        // find the best match:
        int bestIndex = -1;
        double bestConeAngle = Double.POSITIVE_INFINITY;
        if(responses.size()>0){
            for(int loop = 0; loop < responses.size(); loop++) {
                if (responses.get(loop).getDescriptor().getType() != type) continue;
                if (responses.get(loop).getAssociation()>=0) continue;
                CherenkovResponse cher = (CherenkovResponse)responses.get(loop);
                // FIXME:  use normalized distance/angle instead of box cut?
                // unify with non-Cherenkov?
                CherenkovResponse.TrackResidual tres = cher.getTrackResidual(cross);
                if (Math.abs(tres.getDeltaTheta()) < cher.getDeltaTheta() &&
                    Math.abs(tres.getDeltaPhi())   < cher.getDeltaPhi()) {
                    if (tres.getConeAngle() < bestConeAngle) {
                        bestIndex = loop;
                        bestConeAngle = tres.getConeAngle();
                    }
                }
            }
        }
        return bestIndex;
    } 
}
