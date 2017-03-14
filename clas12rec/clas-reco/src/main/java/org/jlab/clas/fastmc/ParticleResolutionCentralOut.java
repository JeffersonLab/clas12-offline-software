/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.fastmc;

import java.util.HashMap;
import java.util.Map;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.physics.Particle;

/**
 *
 * @author gavalian
 */
public class ParticleResolutionCentralOut implements IParticleResolution {
	Map<String, Double> params = new HashMap<String, Double>();

	public ParticleResolutionCentralOut() {

		params.put("FoMT1S1", .20678E-3);
		params.put("FoMT1S2", -.57466E-5);

		params.put("FoMT1S3", .38913E-6);// 0.38913E-06 params.put("Forward 3-d param for sigma_1
		params.put("FoMT2S1", .26652E-2);// 0.26652E-02 params.put("Forward 1-st param for sigma_1
		params.put("FoMT2S2", -.80631E-4);// -0.80631E-04 params.put("Forward 2-d param for sigma_1
		params.put("FoMT2S3", .87599E-5);// 0.87599E-05 params.put("Forward 3-d param for sigma_1
		params.put("FoInThS1", 3.684E-05);// 3.684E-05 params.put("Forward inbending(outbending) Theta sigma_1
		params.put("FoInThS2", .00162613);// 0.00162613 params.put("Forward inbending(outbending) Theta sigma_2

		params.put("FoInMoS1", 0.005);// params.put("Forward inbending mometum sigma_1
		params.put("FoInMoS2", 0.0);// params.put("Forward inbending mometum sigma_2
		params.put("FoOuMoS1", 0.007);// params.put("Forward outbending mometum sigma_1
		params.put("FoOuMoS2", 0.0);// params.put("Forward outbending mometum sigma_2
		params.put("FoNeMoS1", 0.050);// params.put("Forward neutral mometum sigma_1
		params.put("FoNeMoS2", 0.0);// params.put("Forward neutral mometum sigma_2

		params.put("FoOuThS1", .0286);// params.put("Forward outbending Theta sigma_1
		params.put("FoOuThS2", .00);// params.put("Forward outbending Theta sigma_2
		params.put("FoNeThS1", .0286);// params.put("Forward neutral Theta sigma_1
		params.put("FoNeThS2", .00);// params.put("Forward neutral Theta sigma_2

		params.put("FoIOFiS1", .683E-4);// params.put("Forward in(out)bending phi sigma_1

		params.put("FoA0FiS1", .3125E-2);// params.put("Forward in(out)bending A0 phi sigma_1
		params.put("FoA1FiS1", .262E-4);// params.put("Forward in(out)bending A1 phi sigma_1
		params.put("FoA2FiS1", -.350E-6);// params.put("Forward in(out)bending A2 phi sigma_1

		params.put("FoA0FiS2", .5306E-3);// params.put("Forward in(out)bending A0 phi sigma_2
		params.put("FoA1FiS2", -.538E-5);// params.put("Forward in(out)bending A1 phi sigma_2
		params.put("FoA2FiS2", .898E-7);// params.put("Forward in(out)bending A1 phi sigma_2
		params.put("FoNeFiS1", .0573);// params.put("Forward neutral phi sigma_1
		params.put("FoNeFiS2", .0);// params.put("Forward neutral phi sigma_2

	}

	/*
	 * pnorm=p*Tmax/abs(torcur)^M^M c^M^M thr=thetad*d2r^M^M phir=phid*d2r^M^M c select which of the 6 functions we want^M^M if(inbend.and.fwd)then^M^M ckm
	 * 14-apr-2005 (fit on all thetas), theta, phi resolutions:^M^M sig1p=Tmax/torcur*^M^M & (FoMT1S1+FoMT1S2*thetad+FoMT1S3*thetad*thetad)^M^M
	 * sig2p=Tmax/torcur*^M^M & (FoMT2S1+FoMT2S2*thetad+FoMT2S3*thetad*thetad)^M^M ckm sigma_theta is at 5 degrees(as upper limit)^M^M sig1th=FoInThS1^M^M
	 * sig2th=FoInThS2^M^M ckm May 28, 2005: Gail's phi-resolution as function of theta^M^M ckm sig1fi=2.*sig1th^M^M ckm sig2fi=2.*sig2th^M^M ^M^M ckm
	 * sig1fi=FoIOFiS1/SIN(thr)^M^M c print*,'sig1fi,FoIOFiS1,thr',sig1fi,FoIOFiS1,thr*r2d^M^M ckm sig2fi=FoA0FiS2+FoA1FiS2*thr*r2d+FoA2FiS2*(thr*r2d)**2!theta in
	 * degrees^M^M ^M^M ckm May 31, 2005^M^M sig1fi=FoA0FiS1/(thr*r2d) + FoA1FiS1 + FoA2FiS1*(thr*r2d)!theta in degrees^M^M
	 * sig2fi=FoA0FiS2+FoA1FiS2*thr*r2d+FoA2FiS2*(thr*r2d)**2!theta in degrees^M^M ^M^M pout=p+ran(1)*p*sqrt((sig1p*p)**2+(sig2p/beta)**2)^M^M
	 * thout=thr+ran(2)*sqrt(sig1th**2+(sig2th/p/beta)**2)^M^M phiout=phir+ran(3)*sqrt(sig1fi**2+(sig2fi/p/beta)**2)^M^M
	 */
	public double getSigmaTheta(Particle part, double torus) {
		double result = 0.0;
		double beta = part.p() / part.vector().e();
		double p = part.p();
		double sig1th = params.get("FoInThS1");
		double sig2th = params.get("FoInThS2");
		result = Math.sqrt(Math.pow(sig1th, 2) + Math.pow(sig2th / p / beta, 2));
		return result;
	}

	public double getSigmaPhi(Particle part, double torus) {
		double result = 0.0;
		double beta = part.p() / part.vector().e();
		double p = part.p();
		double sig1fi = params.get("FoA0FiS1") / Math.toDegrees(part.theta()) + params.get("FoA1FiS1")
		        + params.get("FoA2FiS1") * Math.toDegrees(part.theta());
		double sig2fi = params.get("FoA0FiS2") + params.get("FoA1FiS2") * Math.toDegrees(part.theta())
		        + params.get("FoA2FiS2") * Math.toDegrees(part.theta());
		result = Math.sqrt(Math.pow(sig1fi, 2) + Math.pow(sig2fi / p / beta, 2));
		return result;
	}

	public double getSigmaMom(Particle part, double torus) {
		double result = 0.0;
		double beta = part.p() / part.vector().e();
		double p = part.p();
		double sig1p = params.get("FoOuMoS1");
		double sig2p = params.get("FoOuMoS2");
		result = Math.sqrt(Math.pow(sig1p * part.p(), 2) + Math.pow(sig2p / beta, 2));
		return result;
	}

	public void apply(Particle p, double torus_scale, double solenoid_scale) {
		double theta_res = this.getSigmaTheta(p, torus_scale);
		double phi_res = this.getSigmaPhi(p, torus_scale);
		double p_res = this.getSigmaMom(p, torus_scale);
		// System.out.println(" Resolution --> P = " + p_res
		// + " theta = " + theta_res + " phi = " + phi_res);
		p.setP(p.p() + PhysicsConstants.getRandomGauss(0.0, p_res));
		p.setTheta(p.theta() + PhysicsConstants.getRandomGauss(0.0, theta_res));
	}
}
