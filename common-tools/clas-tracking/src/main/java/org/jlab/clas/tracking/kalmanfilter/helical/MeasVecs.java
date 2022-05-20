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
    public double[] H(AStateVecs.StateVec stateVec, AStateVecs sv, MeasVec mv, Swim swimmer) {
        AStateVecs.StateVec SVplus  = sv.new StateVec(stateVec.k);
        AStateVecs.StateVec SVminus = sv.new StateVec(stateVec.k);
        
        delta_d_a[0]=2*sqrt_epsilon*(stateVec.d_rho+1);
        delta_d_a[1]=2*sqrt_epsilon*(stateVec.phi0+1);
        delta_d_a[2]=2*sqrt_epsilon*(stateVec.kappa+1);
        delta_d_a[3]=2*sqrt_epsilon*(stateVec.dz+1);
        delta_d_a[4]=2*sqrt_epsilon*(stateVec.tanL+1);
//        delta_d_a[0]=2*Math.sqrt(sv.forwardCov.get(stateVec.k).covMat[0][0]);
//        delta_d_a[1]=2*Math.sqrt(sv.forwardCov.get(stateVec.k).covMat[1][1]);
//        delta_d_a[2]=2*Math.sqrt(sv.forwardCov.get(stateVec.k).covMat[2][2]);
//        delta_d_a[3]=2*Math.sqrt(sv.forwardCov.get(stateVec.k).covMat[3][3]);
//        delta_d_a[4]=2*Math.sqrt(sv.forwardCov.get(stateVec.k).covMat[4][4]);
//            System.out.println("calculating H");
        
        for(int i = 0; i < Hval.length; i++)
            Hval[i] = 0;
        
        for(int i = 0; i < getDelta_d_a().length; i++) {
//            System.out.println("initial statevec");sv.printlnStateVec(stateVec);
            SVplus.copy(stateVec);
            SVminus.copy(stateVec);
//            System.out.println("SVplus before shift");sv.printlnStateVec(SVplus);
            if(i ==0) {
                SVplus.d_rho = SVplus.d_rho + getDelta_d_a()[i] / 2.;
                SVminus.d_rho = SVminus.d_rho - getDelta_d_a()[i] / 2.;
            }
            if(i ==1) {
                SVplus.phi0 = SVplus.phi0 + getDelta_d_a()[i] / 2.;
                if(Math.abs(SVplus.phi0)>Math.PI) SVplus.phi0 -= Math.signum(SVplus.phi0)*2*Math.PI;
                SVminus.phi0 = SVminus.phi0 - getDelta_d_a()[i] / 2.;
                if(Math.abs(SVminus.phi0)>Math.PI) SVminus.phi0 -= Math.signum(SVminus.phi0)*2*Math.PI;
            }
            if(i ==2) {
                SVplus.kappa = SVplus.kappa + getDelta_d_a()[i] / 2.;
                SVminus.kappa = SVminus.kappa - getDelta_d_a()[i] / 2.;
            }
            if(i ==3) {
                SVplus.dz = SVplus.dz + getDelta_d_a()[i] / 2.;
                SVminus.dz = SVminus.dz - getDelta_d_a()[i] / 2.;
            }
            if(i ==4) {
                SVplus.tanL = SVplus.tanL + getDelta_d_a()[i] / 2.;
                SVminus.tanL = SVminus.tanL - getDelta_d_a()[i] / 2.;
            }
//            System.out.println("after shift " + getDelta_d_a()[i] / 2.);sv.printlnStateVec(SVplus);
            SVplus.updateFromHelix();
            SVminus.updateFromHelix();
//            System.out.println("after recalculation " + getDelta_d_a()[i] / 2.);sv.printlnStateVec(SVplus);
//            sv.printlnStateVec(SVminus);
            // if using swimmer, roll-back by few mm before the measurement surface
            if(swimmer!= null && !sv.straight) {
                SVplus.rollBack(rollBackAngle);
                SVminus.rollBack(rollBackAngle);
            }
            else {
                SVplus.toDoca();
                SVminus.toDoca();
            }
            sv.setStateVecPosAtMeasSite(SVplus,  mv, null);
            sv.setStateVecPosAtMeasSite(SVminus, mv, null);
//            sv.printlnStateVec(SVplus);
//            sv.printlnStateVec(SVminus);
            Hval[i] = (this.h(stateVec.k, SVplus) - this.h(stateVec.k, SVminus)) / getDelta_d_a()[i] ;
//            System.out.println(i + " " + getDelta_d_a()[i] + " " + this.h(stateVec.k, SVplus) + " " + this.h(stateVec.k, SVminus) + " " + Hval[i]);
        }
//            System.out.println("done with H");
       
        return Hval;
    }
    
}
