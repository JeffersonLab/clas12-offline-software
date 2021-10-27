/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    public double[] H(AStateVecs.StateVec stateVec, AStateVecs sv, MeasVec mv, Swim swimmer, int dir) {
        AStateVecs.StateVec SVplus = null;// = new StateVec(stateVec.k);
        AStateVecs.StateVec SVminus = null;// = new StateVec(stateVec.k);
        
        for(int i = 0; i < getHval().length; i++)
            getHval()[i] = 0;
        for(int i = 0; i < getDelta_d_a().length-1; i++) {
            SVplus = this.reset(SVplus, stateVec, sv);
            SVminus = this.reset(SVminus, stateVec, sv);
            if(i ==0) {
                SVplus.x0 = stateVec.x0 + getDelta_d_a()[i] / 2.;
                SVminus.x0 = stateVec.x0 - getDelta_d_a()[i] / 2.;
            }
            if(i ==1) {
                SVplus.z0 = stateVec.z0 + getDelta_d_a()[i] / 2.;
                SVminus.z0 = stateVec.z0 - getDelta_d_a()[i] / 2.;
            }
            if(i ==2) {
                SVplus.tx = stateVec.tx + getDelta_d_a()[i] / 2.;
                SVminus.tz = stateVec.tz - getDelta_d_a()[i] / 2.;
            }
            if(i ==3) {
                SVplus.tz = stateVec.tz + getDelta_d_a()[i] / 2.;
                SVminus.tz = stateVec.tz - getDelta_d_a()[i] / 2.;
            }
            
            SVplus = sv.newStateVecAtMeasSite(stateVec.k, SVplus, mv, swimmer, false);
            SVminus = sv.newStateVecAtMeasSite(stateVec.k, SVminus, mv, swimmer, false);
            Hval[i] = (this.h(stateVec.k, SVplus) - this.h(stateVec.k, SVminus)) / getDelta_d_a()[i] ;
        }
        return getHval();
    }

    @Override
    public AStateVecs.StateVec reset(AStateVecs.StateVec SVplus, AStateVecs.StateVec stateVec, AStateVecs sv) {
        SVplus = sv.new StateVec(stateVec.k);
        SVplus.x0 = stateVec.x0;
        SVplus.z0 = stateVec.z0;
        SVplus.tx = stateVec.tx;
        SVplus.tz = stateVec.tz;
        SVplus.x = stateVec.x;
        SVplus.y = stateVec.y;
        SVplus.z = stateVec.z;
        
        return SVplus;
    }
    
}
