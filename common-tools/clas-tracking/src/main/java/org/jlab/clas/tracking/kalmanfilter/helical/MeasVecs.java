/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.kalmanfilter.helical;

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
        delta_d_a[0]=2*sqrt_epsilon*(stateVec.d_rho+1);
        delta_d_a[1]=2*sqrt_epsilon*(stateVec.phi0+1);
        delta_d_a[2]=2*sqrt_epsilon*(stateVec.kappa+1);
        delta_d_a[3]=2*sqrt_epsilon*(stateVec.dz+1);
        delta_d_a[4]=2*sqrt_epsilon*(stateVec.tanL+1);
        
        for(int i = 0; i < getHval().length; i++)
            getHval()[i] = 0;
        for(int i = 0; i < getDelta_d_a().length; i++) {
            SVplus = this.reset(SVplus, stateVec, sv);
            SVminus = this.reset(SVminus, stateVec, sv);
            if(i ==0) {
                SVplus.d_rho = stateVec.d_rho + getDelta_d_a()[i] / 2.;
                SVminus.d_rho = stateVec.d_rho - getDelta_d_a()[i] / 2.;
            }
            if(i ==1) {
                SVplus.phi0 = stateVec.phi0 + getDelta_d_a()[i] / 2.;
                SVminus.phi0 = stateVec.phi0 - getDelta_d_a()[i] / 2.;
            }
            if(i ==2) {
                SVplus.kappa = stateVec.kappa + getDelta_d_a()[i] / 2.;
                SVminus.kappa = stateVec.kappa - getDelta_d_a()[i] / 2.;
            }
            if(i ==3) {
                SVplus.dz = stateVec.dz + getDelta_d_a()[i] / 2.;
                SVminus.dz = stateVec.dz - getDelta_d_a()[i] / 2.;
            }
            if(i ==4) {
                SVplus.tanL = stateVec.tanL + getDelta_d_a()[i] / 2.;
                SVminus.tanL = stateVec.tanL - getDelta_d_a()[i] / 2.;
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
        SVplus.d_rho = stateVec.d_rho;
        SVplus.alpha = stateVec.alpha;
        SVplus.phi0 = stateVec.phi0;
        SVplus.kappa = stateVec.kappa;
        SVplus.dz = stateVec.dz;
        SVplus.tanL = stateVec.tanL;
        SVplus.alpha = stateVec.alpha;
        
        return SVplus;
    }
    
}
