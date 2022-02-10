package org.jlab.clas.tracking.kalmanfilter.straight;

import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;

/**
 *
 * @author ziegler
 */
public class MeasVecs extends AMeasVecs {

    @Override
    public double[] H(AStateVecs.StateVec stateVec, AStateVecs sv, MeasVec mv, Swim swimmer) {
        AStateVecs.StateVec SVplus = sv.new StateVec(stateVec);
        AStateVecs.StateVec SVminus = sv.new StateVec(stateVec);
        
        for(int i = 0; i < getHval().length; i++)
            getHval()[i] = 0;
        for(int i = 0; i < getDelta_d_a().length-1; i++) {
            SVplus.copy(stateVec);
            SVminus.copy(stateVec);
            if(i ==0) {
                SVplus.x0  = stateVec.x0 + getDelta_d_a()[i] / 2.;
                SVminus.x0 = stateVec.x0 - getDelta_d_a()[i] / 2.;
            }
            if(i ==1) {
                SVplus.z0  = stateVec.z0 + getDelta_d_a()[i] / 2.;
                SVminus.z0 = stateVec.z0 - getDelta_d_a()[i] / 2.;
            }
            if(i ==2) {
                SVplus.tx  = stateVec.tx + getDelta_d_a()[i] / 2.;
                SVminus.tx = stateVec.tx - getDelta_d_a()[i] / 2.;
            }
            if(i ==3) {
                SVplus.tz  = stateVec.tz + getDelta_d_a()[i] / 2.;
                SVminus.tz = stateVec.tz - getDelta_d_a()[i] / 2.;
            }
            SVplus.updateFromRay();
            SVminus.updateFromRay();
            sv.setStateVecPosAtMeasSite(SVplus, mv, swimmer);
            sv.setStateVecPosAtMeasSite(SVminus, mv, swimmer);
            Hval[i] = (this.h(stateVec.k, SVplus) - this.h(stateVec.k, SVminus)) / getDelta_d_a()[i] ;
        }
        return getHval();
    }
    
}
