package org.jlab.clas.detector;

import java.util.List;
import org.jlab.geom.prim.Line3D;
import org.jlab.detector.base.DetectorType;
import org.jlab.clas.pdg.PhysicsConstants;

/**
 * Override methods to get old POCA-based behavior.
 * 
 */
public class DetectorParticlePOCA extends DetectorParticle {
    
    public DetectorParticlePOCA(DetectorTrack track){
        super(track);
    }

    @Override
    public double getPathLength(DetectorType type, int layer){
        DetectorResponse response = this.getHit(type,layer);
        if(response==null) return -1.0;
        return this.getPathLength(response.getPosition());
    } 
    
    @Override
    public double getBeta(DetectorType type, int layer, double startTime){
        DetectorResponse response = this.getHit(type,layer);
        if(response==null) return -1.0;
        double cpath = this.getPathLength(response.getPosition());
        double ctime = response.getTime() - startTime;
        double beta  = cpath/ctime/PhysicsConstants.speedOfLight();
        return beta;
    }
    
    @Override
    public double getVertexTime(DetectorType type, int layer){
        double vertex_time = this.getTime(type,layer) -
                this.getPathLength(type, layer) /
                (this.getTheoryBeta(this.getPid())*PhysicsConstants.speedOfLight());
        return vertex_time;
    }
    
    @Override
    public double getVertexTime(DetectorType type, int layer, int pid){
        double vertex_time = -9999;
        if(type==DetectorType.CTOF) {
            DetectorResponse res = this.getHit(type);
            vertex_time = this.getTime(type) - res.getPath()/
                    (this.getTheoryBeta(pid)*PhysicsConstants.speedOfLight());
        }
        else {
            vertex_time = this.getTime(type,layer) -
                    this.getPathLength(type, layer) /
                    (this.getTheoryBeta(pid)*PhysicsConstants.speedOfLight());
        }
        return vertex_time;
    }
   
    @Override
    public Line3D  getDistance(DetectorResponse  response){
        Line3D cross = this.detectorTrack.getLastCross();
        Line3D  dist = cross.distanceRay(response.getPosition().toPoint3D());
        return dist;
    }

    @Override
    public void addResponse(DetectorResponse res, boolean match){
        this.responseStore.add(res);
        if(match==true){
            Line3D distance = this.getDistance(res);
            res.getMatchedPosition().setXYZ(
                    distance.midpoint().x(),
                    distance.midpoint().y(),distance.midpoint().z());
            res.setPath(this.getPathLength(res.getPosition()));
        }
    }

    @Override
    public int getCherenkovSignal(List<DetectorResponse> responses, DetectorType type){

        Line3D cross;
        if (type==DetectorType.HTCC) {
            cross=this.detectorTrack.getFirstCross();
        }
        else if (type==DetectorType.LTCC)
            cross=this.detectorTrack.getLastCross();
        else throw new RuntimeException(
                "DetectorParticle:getCheckr5noSignal:  invalid type:  "+type);

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
