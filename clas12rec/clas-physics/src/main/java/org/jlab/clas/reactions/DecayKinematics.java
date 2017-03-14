/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reactions;

import org.jlab.clas.physics.Vector3;
import org.jlab.clas.physics.LorentzVector;
/**
 *
 * @author gavalian
 */
public class DecayKinematics {
    
    public static TransMatrix trMatrix = DecayKinematics.initMatrix();
    
    public DecayKinematics()
    {
        
    }
    
    public static TransMatrix initMatrix()
    {
        return new TransMatrix();        
    }
    
    public static Vector3 vectorToLab(Vector3 frame, Vector3 vec)
    {
        DecayKinematics.trMatrix.compose(frame);
        DecayKinematics.trMatrix.inverse();
        return DecayKinematics.trMatrix.mult(vec);
    }
    
    public static Vector3 vectorToFrame(Vector3 frame, Vector3 vec)
    {
        DecayKinematics.trMatrix.compose(frame);
        return DecayKinematics.trMatrix.mult(vec);
    }
    
    public static double TwoBodyDecayMomentum(double M, double m1, double m2)
    {
        double mult1 = M*M - (m1+m2)*(m1+m2);
        double mult2 = M*M - (m1-m2)*(m1-m2);
        double mult  = mult1*mult2;
        if(mult<0)
        {
            System.err.println("Decay:: error. particle with Mass="+M 
                    + "  can not decay to particles with masses ( " + m1
                    + " , " + m2 + " )");
            return -1.0;
        }
        return Math.sqrt(mult)/(2.0*M);
    }
    
    public static LorentzVector[] getDecayParticles(LorentzVector parent,
            double m1, double m2, double theta_rf, double phi_rf)
    {
        LorentzVector[] pd = new LorentzVector[2];
        for(int loop=0;loop<2;loop++) pd[loop] = new LorentzVector();
        double momentum = DecayKinematics.TwoBodyDecayMomentum(parent.mass(), m1, m2);
        Vector3 vz = new Vector3(0.,0.,0.);
        Vector3 v1 = new Vector3();
        Vector3 v2 = new Vector3();

        v1.setMagThetaPhi(momentum, theta_rf, phi_rf);
        vz.sub(v1);
        v2.setMagThetaPhi(momentum, Math.PI-theta_rf, 2.0*Math.PI-phi_rf);
        pd[0].setVectM(v1, m1);
        pd[1].setVectM(vz, m2);        
        return pd;
    }
    
    public static LorentzVector[] getDecayParticlesLab(LorentzVector parent,
            double m1, double m2, double theta_rf, double phi_rf)
    {
        LorentzVector[] vectD = DecayKinematics.getDecayParticles(parent, m1, m2, theta_rf, phi_rf);
        Vector3 vboost = parent.boostVector();
        vectD[0].boost(vboost);
        vectD[1].boost(vboost);
        return vectD;
    }
    
}
