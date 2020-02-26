package cnuphys.fastMCed.snr;

import java.io.Serializable;

import cnuphys.lund.GeneratedParticleRecord;

public class ReducedParticleRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6934172911710095616L;
	public byte charge;
	public float xo; //cm
	public float yo; //cm
	public float zo; //cm
	public float p; //GeV/c
	public float theta; //deg
	public float phi; //deg
	
	public ReducedParticleRecord(GeneratedParticleRecord gr) {
		charge = (byte) gr.getCharge();
		xo = (float) (gr.getVertexX()*10.); //convert to cm
		yo = (float) (gr.getVertexY()*10.); //convert to cm
		zo = (float) (gr.getVertexZ()*10.); //convert to cm
		
		p = (float) gr.getMomentum();
		theta = (float) (gr.getTheta());
		phi = (float) (gr.getPhi());
	}
}
