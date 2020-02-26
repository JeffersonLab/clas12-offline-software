/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc;

//import org.jMath.Vector.threeVec;

import org.jlab.geom.prim.Vector3D;


/**
 *
 * @author S. Joosten
 * 
 * Correct the cluster position estimate by incorporating track momentum and
 * charge information.
 * 
 * For now, this is a stub that just returns three crude cluster position,
 * but it will be updated with a fit (or lookup-table) generated from the 
 * LTCC simulation.
 * Note that the correction ultimately will depend on the torus and solenoid 
 * field settings.
 */
public class LTCCClusterCorrection {
    /*
    * static member function calcPosition
        * arguments:
            - threeVec clusterPos: estimated cluster position from the 
                                   reconstruction
            - double p: track momentum
            - int charge: track charge
            - double torus: torus field setting
            - double solenoid: solenoid field setting
        * returns (threeVec): a vector with a more accurate cluster position
    */
    public static Vector3D calcPosition(Vector3D clusterPos, double p, 
            int charge, double torus, double solenoid) {
        // currently a no-op
        return new Vector3D(clusterPos);
    }
}
