// single particle monte carlo validation

package analysis;

import java.util.ArrayList;

import org.jlab.analysis.plotting.H1FCollection2D;
import org.jlab.analysis.plotting.H1FCollection3D;
import org.jlab.analysis.plotting.TCanvasP;
import org.jlab.analysis.plotting.TCanvasPTabbed;
import org.jlab.clas.physics.Vector3;
import org.jlab.groot.data.H2F;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
*
* @author naharrison
*/
public class SPMCValidation {
	
	public static String fileName;
	public static int NpBins, NthBins, NphBins;
	public static double pMin, pMax, thMin, thMax, phMin, phMax;
	public static H2F nRecoVcharge;
	public static H1FCollection2D genPhi;
	public static H1FCollection2D recPhi;
	public static H1FCollection3D DpOp;
	public static H1FCollection3D Dth;
	public static H1FCollection3D Dph;
	
	
	private static void init() {
		nRecoVcharge = new H2F("nRecoVcharge", 3, -1.5, 1.5, 5, -0.5, 4.5);

		genPhi = new H1FCollection2D("genPhi", 100, phMin, phMax,
				range2array(NpBins, pMin, pMax),
				range2array(NthBins, thMin, thMax));

		recPhi = new H1FCollection2D("recPhi", 100, phMin, phMax,
				range2array(NpBins, pMin, pMax),
				range2array(NthBins, thMin, thMax));
		recPhi.setLineColor(2);
		
		DpOp = new H1FCollection3D("DpOp", 100, -0.05, 0.05,
				range2array(NpBins, pMin, pMax),
				range2array(NthBins, thMin, thMax),
				range2array(NphBins, phMin, phMax));
		
		Dth = new H1FCollection3D("Dth", 100, -0.5, 0.5,
				range2array(NpBins, pMin, pMax),
				range2array(NthBins, thMin, thMax),
				range2array(NphBins, phMin, phMax));
		
		Dph = new H1FCollection3D("Dph", 100, -1.5, 1.5,
				range2array(NpBins, pMin, pMax),
				range2array(NthBins, thMin, thMax),
				range2array(NphBins, phMin, phMax));
	}
	
	
	private static ArrayList<Double> range2array(int n, double min, double max) {
		ArrayList<Double> result = new ArrayList<>();
		double width = (max - min)/n;
		for(int k = 0; k <= n; k++) {
			result.add(min + k*width);
		}
		return result;
	}
	

	private static void processEvent(DataEvent event) {
		Vector3 genvec = null;
		Vector3 recvec = null;

		if(event.hasBank("MC::Particle")) {
			DataBank gbank = event.getBank("MC::Particle");
			//if(gbank.rows() == 1) { // doesn't work with background
			if(true) {
				genvec = new Vector3(gbank.getFloat("px", 0), gbank.getFloat("py", 0), gbank.getFloat("pz", 0));
				genPhi.fill(Math.toDegrees(genvec.phi()), genvec.mag(), Math.toDegrees(genvec.theta()));
			}
		}

		if(event.hasBank("REC::Particle") && genvec != null) {
			DataBank rbank = event.getBank("REC::Particle");
			int nPos = 0;
			int nNeut = 0;
			int nNeg = 0;
			for(int k = 0; k < rbank.rows(); k++) {
				byte charge = rbank.getByte("charge", k);
				if(charge == 1) nPos++;
				else if(charge == 0) nNeut++;
				else if(charge == -1) nNeg++;
			}
			nRecoVcharge.fill(1, nPos);
			nRecoVcharge.fill(0, nNeut);
			nRecoVcharge.fill(-1, nNeg);
			
			if(rbank.rows() > 0) {
				recvec = getClosestParticleMatch(rbank, genvec);
				recPhi.fill(Math.toDegrees(recvec.phi()), recvec.mag(), Math.toDegrees(recvec.theta()));
			}
		}
		
		if(genvec != null && recvec != null) {
			DpOp.fill((genvec.mag() - recvec.mag())/genvec.mag(), genvec.mag(), Math.toDegrees(genvec.theta()), Math.toDegrees(genvec.phi()));
			Dth.fill(Math.toDegrees(genvec.theta() - recvec.theta()), genvec.mag(), Math.toDegrees(genvec.theta()), Math.toDegrees(genvec.phi()));
			Dph.fill(Math.toDegrees(genvec.phi() - recvec.phi()), genvec.mag(), Math.toDegrees(genvec.theta()), Math.toDegrees(genvec.phi()));
		}
	}
	
	
	public static Vector3 getClosestParticleMatch(DataBank b, Vector3 v) {
		double euclideanDistance = Double.MAX_VALUE;
		int index = -1;
		for(int k = 0; k < b.rows(); k++) {
			Vector3 thisVec = new Vector3(b.getFloat("px", k), b.getFloat("py", k), b.getFloat("pz", k));
			Vector3 diff = new Vector3(v);
			diff.sub(thisVec);
			if(diff.mag() < euclideanDistance) {
				euclideanDistance = diff.mag();
				index = k;
			}
		}
		return new Vector3(b.getFloat("px", index), b.getFloat("py", index), b.getFloat("pz", index));
	}
	
	
	private static void draw() {
		TCanvasP nRecoCan = new TCanvasP("nRecoCan", 500, 300);
		nRecoCan.draw(nRecoVcharge);
		
		TCanvasP phiCan = new TCanvasP("phiCan", 1000, 700);
		phiCan.draw(genPhi);
		phiCan.draw(recPhi, "same");

		TCanvasPTabbed DpOpCan = new TCanvasPTabbed("DpOpCan", 1000, 700);
		DpOpCan.draw(DpOp);

		TCanvasPTabbed DthCan = new TCanvasPTabbed("DthCan", 1000, 700);
		DthCan.draw(Dth);

		TCanvasPTabbed DphCan = new TCanvasPTabbed("DphCan", 1000, 700);
		DphCan.draw(Dph);
	}


	public static void main(String[] args) {
		if(args.length == 0) {
			fileName = "/Users/harrison/software_validation/single_e/cooked/out_sim_L33.hipo";
			NpBins = 4;
			pMin = 0.0;
			pMax = 10.0;
			NthBins = 4;
			thMin = 0.0;
			thMax = 30.0;
			NphBins = 3;
			phMin = -30.0;
			phMax = 30.0;
		}
		else {
			fileName = args[0];
			NpBins = Integer.parseInt(args[1]);
			pMin = Double.parseDouble(args[2]);
			pMax = Double.parseDouble(args[3]);
			NthBins = Integer.parseInt(args[4]);
			thMin = Double.parseDouble(args[5]);
			thMax = Double.parseDouble(args[6]);
			NphBins = Integer.parseInt(args[7]);
			phMin = Double.parseDouble(args[8]);
			phMax = Double.parseDouble(args[9]);
		}
		
		init();
		
		HipoDataSource reader = new HipoDataSource();
		reader.open(fileName);
		
		while(reader.hasEvent()) {
			processEvent(reader.getNextEvent());
		}
		
		reader.close();
		
		draw();
	}


}

