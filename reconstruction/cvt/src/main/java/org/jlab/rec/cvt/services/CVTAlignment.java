package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
//import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.base.GeometryFactory;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.detector.geant4.v2.SVT.SVTAlignmentFactory;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;

import org.jlab.rec.cvt.banks.AlignmentBankWriter;
import org.jlab.rec.cvt.banks.RecoBankReader;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.Trajectory;

import Jama.Matrix;
import eu.mihosoft.vrl.v3d.Vector3d;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTAlignment extends ReconstructionEngine {

	org.jlab.rec.cvt.svt.Geometry SVTGeom;
	org.jlab.rec.cvt.bmt.BMTGeometry BMTGeom;
	CTOFGeant4Factory CTOFGeom;
	Detector          CNDGeom ;
	SVTStripFactory svtIdealStripFactory;

	public CVTAlignment() {
		super("CVTAlignment", "spaul", "4.0");

		SVTGeom = new org.jlab.rec.cvt.svt.Geometry();
		BMTGeom = new org.jlab.rec.cvt.bmt.BMTGeometry();

	}

	String FieldsConfig = "";
	int Run = -1;
	public boolean isSVTonly = false;
	private Boolean svtTopBottomSep;
	public void setRunConditionsParameters(DataEvent event, String FieldsConfig, int iRun, boolean addMisAlignmts, String misAlgnFile) {
		if (event.hasBank("RUN::config") == false) {
			System.err.println("RUN CONDITIONS NOT READ!");
			return;
		}

		int Run = iRun;

		boolean isMC = false;
		boolean isCosmics = false;
		DataBank bank = event.getBank("RUN::config");
		//System.out.println("EVENTNUM "+bank.getInt("event",0));
		if (bank.getByte("type", 0) == 0) {
			isMC = true;
		}
		if (bank.getByte("mode", 0) == 1) {
			isCosmics = true;
		}



		// Load the fields
		//-----------------
		String newConfig = "SOLENOID" + bank.getFloat("solenoid", 0);

		if (FieldsConfig.equals(newConfig) == false) {
			// Load the Constants

			this.setFieldsConfig(newConfig);
		}
		FieldsConfig = newConfig;

		// Load the constants
		//-------------------
		int newRun = bank.getInt("run", 0);

		if (Run != newRun) {
			boolean align=false;
			//Load field scale
			double SolenoidScale =(double) bank.getFloat("solenoid", 0);
			Constants.setSolenoidscale(SolenoidScale);
			if(Math.abs(SolenoidScale)<0.001)
				Constants.setCosmicsData(true);

			System.out.println(" LOADING BMT GEOMETRY...............................variation = "+variationName);
			CCDBConstantsLoader.Load(new DatabaseConstantProvider(newRun, variationName));
			//            System.out.println("SVT LOADING WITH VARIATION "+variationName);
			//            DatabaseConstantProvider cp = new DatabaseConstantProvider(newRun, variationName);
			//            cp = SVTConstants.connect( cp );
			//            cp.disconnect();  
			//            SVTStripFactory svtFac = new SVTStripFactory(cp, true);
			//            SVTGeom.setSvtStripFactory(svtFac);
			Constants.Load(isCosmics, isSVTonly);
			this.setRun(newRun);

		}

		Run = newRun;
		this.setRun(Run);
	}

	public int getRun() {
		return Run;
	}

	public void setRun(int run) {
		Run = run;
	}

	public String getFieldsConfig() {
		return FieldsConfig;
	}

	public void setFieldsConfig(String fieldsConfig) {
		FieldsConfig = fieldsConfig;
	}

	boolean isCosmics = false;
	private boolean isBMTonly;

	@Override
	public boolean processDataEvent(DataEvent event) {
		int runNum = event.getBank("RUN::config").getInt("run", 0);
		int eventNum = event.getBank("RUN::config").getInt("event", 0);
		this.setRunConditionsParameters(event, FieldsConfig, Run, false, "");

		double shift = 0;//org.jlab.rec.cvt.Constants.getZoffset();;

		this.FieldsConfig = this.getFieldsConfig();


		RecoBankReader reader = new RecoBankReader();

		//reader.fetch_Cosmics(event, SVTGeom, 0);

		List<? extends Trajectory> tracks;
		if(isCosmics) {
			reader.fetch_Cosmics(event, shift);
			tracks = reader.get_Cosmics();
		} else {
			reader.fetch_Tracks(event, SVTGeom, shift);
			tracks = reader.get_Tracks();
		}
		/*System.out.println(reader.get_ClustersSVT().size()+ " clusters found in SVT");
		System.out.println(reader.get_ClustersBMT().size()+ " clusters found in BMT");
		System.out.println(reader.get_CrossesSVT().size() + " crosses found in SVT");
		System.out.println(reader.get_CrossesBMT().size() + " crosses found in BMT");
		System.out.println(tracks.size() + " tracks found");*/
		

		//System.out.println("H");
		List<Matrix> Is = new ArrayList<Matrix>();
		List<Matrix> As = new ArrayList<Matrix>();
		List<Matrix> Bs = new ArrayList<Matrix>();
		List<Matrix> Vs = new ArrayList<Matrix>();
		List<Matrix> ms = new ArrayList<Matrix>();
		List<Matrix> cs = new ArrayList<Matrix>();
		List<Integer> trackIDs = new ArrayList<Integer>();


		tracksLoop : for (Trajectory track : tracks) {

			if(Math.abs(getDoca(track))>maxDocaCut)
				continue;
			/*System.out.println("track read: ");
			System.out.println("track chi2: "+ track.get_chi2());
			System.out.println("ndf: "+ track.get_ndf());
			System.out.println("ncrosses: "+ track.size());
			System.out.println("ray: "+ track.get_ray().get_refPoint() + 
					" + lambda*" + track.get_ray().get_dirVec());
			System.out.println();*/

			//System.out.println("BMT crosses");
			int nCrossSVT = 0, nCrossBMT = 0;
			for(Cross c : track) {
				if(c.get_Detector().equalsIgnoreCase("SVT") && !isBMTonly)
					nCrossSVT++;
				if(c.get_Detector().equalsIgnoreCase("BMT") && !isSVTonly) {
					nCrossBMT++;
					//System.out.println(c.get_Sector()+" "+c.get_Region() + " " + c.get_Cluster1().get_Centroid()+" " + c.get_Id());
				}
				if(nCrossBMT>12) {
					System.out.println("Too many BMT crosses!");
					System.exit(0);
				}
			}
			int nCross = nCrossSVT + nCrossBMT;
			if(nCross <= 2)
				continue;
			Ray ray = track.get_ray();
			if(ray == null) {
				ray = getRay(track.get_helix());
				//System.out.println("curvature " +  track.get_helix().get_curvature());
				//System.out.println("doca " +  track.get_helix().get_dca());
				if(Math.abs(track.get_helix().get_curvature())>0.001) {
					continue;
				}
			}
			//getRay(track);
			//System.out.println(ray.get_dirVec().toString());
			//System.out.println(ray.get_refPoint().toString());

			
			
			int colsA = nAlignVars*((svtTopBottomSep ? 2*nCrossSVT : nCrossSVT) + nCrossBMT + (isCosmics ? 0:1));
			int rows = 2*nCrossSVT+nCrossBMT + (isCosmics ? 0:1);
			Matrix A = new Matrix(rows, colsA);//not sure why there aren't 6 columns
			Matrix B = new Matrix(rows, 4);
			Matrix V = new Matrix(rows,rows);
			Matrix m = new Matrix(rows,1);
			Matrix c = new Matrix(rows,1);
			Matrix I = new Matrix(rows,1);
			
			int i = 0;

			for(Cross cross : track) {
				//System.out.println("cross " +cross.get_Point());
				if(cross.get_Detector().equalsIgnoreCase("SVT"))
				{
					if(isBMTonly)
						continue;
					Cluster cl1 = cross.get_Cluster1();
					boolean ok = fillMatricesSVT(i,ray,cl1,A,B,V,m,c,I);
					i++;
					if(!ok) { //reject track if there's a cluster with really bad values.
						if(debug) System.out.println("rejecting track due to problem in an SVT layer");
						continue tracksLoop;
					}
					Cluster cl2 = cross.get_Cluster2();
					ok = fillMatricesSVT(i,ray,cl2,A,B,V,m,c,I);
					i++;
					if(!ok) { //reject track if there's a cluster with really bad values.
						if(debug) System.out.println("rejecting track due to problem in an SVT layer");
						continue tracksLoop;
					}
				} else {
					if(isSVTonly)
						continue;
					Cluster cl = cross.get_Cluster1();
					
					
					if(cross.get_DetectorType() == BMTType.Z) {
						boolean ok = fillMatricesBMTZ(i,ray,cl,A,B,V,m,c,I);
						i++;
						if(!ok) { //reject track if there's a cluster with really bad values.
							if(debug) System.out.println("rejecting track due to problem in a BMT Z layer");
							continue tracksLoop;
						}
					}
					else if(cross.get_DetectorType() == BMTType.C) {
						boolean ok = fillMatricesBMTC(i,ray,cl,A,B,V,m,c,I);
						i++;
						if(!ok) { //reject track if there's a cluster with really bad values.
							if(debug) System.out.println("rejecting track due to problem in a BMT C layer");
							continue tracksLoop;
						}
					}
					
				}

			}
			if(!isCosmics) {
				fillMatricesBeamspot(i, ray, A,B,V,m,c,I);
			}
			/*Matrix dm = m.minus(c);
			System.out.println("dm: ");
			dm.print(6, 2);
			System.out.println("V:  ");
			V.print(7, 4);
			System.out.println("B:  ");
			B.print(7, 4);
			System.out.println("A:  ");
			A.print(7, 4);
			System.out.println("I:  ");
			I.print(7, 4);
			System.out.println("track chi2: " + dm.transpose().times(V.inverse()).times(dm).get(0, 0));
			System.out.println();*/

			for(double res : c.minus(m).getRowPackedCopy()) {
				if(Math.abs(res)>maxResidualCut) {
					System.out.println("rejecting track due to large residual");
					continue tracksLoop;
				}
			}
			As.add(A);
			Bs.add(B);
			Vs.add(V);
			ms.add(m);
			cs.add(c);
			Is.add(I);


			//c.print(7, 4);
			//m.print(7, 4);

			trackIDs.add(track.get_Id());
		}
		AlignmentBankWriter writer = new AlignmentBankWriter();
		writer.write_Matrix(event, "I", Is);
		writer.write_Matrix(event, "A", As);
		writer.write_Matrix(event, "B", Bs);
		writer.write_Matrix(event, "V", Vs);
		writer.write_Matrix(event, "m", ms);
		writer.write_Matrix(event, "c", cs);
		fillMisc(event,runNum,eventNum,trackIDs,As,Bs,Vs,ms,cs,Is);

		//event.show();
		return true;

	}
	int nAlignables;

	private Ray getRay(Helix h) {
		
		double d = h.get_dca();
		double z = h.get_Z0();
		double phi = h.get_phi_at_dca();
		double td = h.get_tandip();
		double cd = 1/Math.hypot(td, 1);
		double sd = td*cd;
		//Vector3D u = new Vector3D(-cd*Math.sin(phi), cd*Math.cos(phi), sd);
		//Point3D x = new Point3D(d*Math.cos(phi),d*Math.sin(phi), z);
		Vector3D u = new Vector3D(cd*Math.cos(phi), cd*Math.sin(phi), sd);
		
		
		Point3D x = new Point3D(-d*Math.sin(phi)+xb,d*Math.cos(phi)+yb, z);
		//Point3D x = new Point3D(-d*Math.sin(phi),d*Math.cos(phi), z);
		//if(u.y() <0)
		//	u = u.multiply(-1);
		//x = x.toVector3D().add(u.multiply(-x.y()/u.y())).toPoint3D();
		Ray ray = new Ray(x, u);
		//System.out.println("doca " + d);
		//System.out.println("td " + td);

		return ray;
	}



	private double getDoca(Trajectory track) {
		if(track instanceof StraightTrack) {
			Ray ray = track.get_ray();
			double intercept = ray.get_yxinterc();
			double slope = ray.get_yxslope();
			return Math.abs(intercept)/Math.hypot(1, slope);
		} else return track.get_helix().get_dca();
	}

	private void fillMisc(DataEvent event, int runNum, int eventNum, List<Integer> trackIDs, 
			List<Matrix> As, List<Matrix> Bs, List<Matrix> Vs, List<Matrix> ms, List<Matrix> cs,
			List<Matrix> is) {
		DataBank bank = event.createBank("Align::misc", trackIDs.size());
		for(int i = 0; i<trackIDs.size(); i++) {
			bank.setInt("run", i, runNum);
			bank.setInt("event", i, eventNum);
			Matrix c = cs.get(i), m = ms.get(i), V = Vs.get(i);
			bank.setFloat("chi2", i, (float)(m.minus(c)).transpose().times(V.inverse()).times(m.minus(c)).get(0, 0));

			bank.setShort("ndof", i, (short)(Vs.get(i).getRowDimension()-4));
			bank.setShort("track", i, (short)(int)trackIDs.get(i));
			bank.setShort("nalignables", i, (short)this.nAlignables);
			bank.setShort("nparameters", i, (short)this.nAlignVars);
		}

		event.appendBank(bank);
	}

	/*
	 * converts a Vector3D to a Vector3d.  
	 * These objects are from two different packages.
	 */
	private Vector3d convertVector(Vector3D v) {
		return new Vector3d(v.x(),v.y(),v.z());
	}
	/*
	 * converts a Vector3D to a Vector3d.  
	 * These objects are from two different packages.
	 */
	private Line3d convertLine(Line3D line) {
		return new Line3d(convertVector(line.origin().toVector3D()), convertVector(line.end().toVector3D()));
	}

	boolean useDocaPhiZTandip=true;

	private boolean fillMatricesBeamspot(int i, Ray ray, Matrix A, Matrix B, Matrix V, Matrix m, Matrix c,
			Matrix I){
		// a point along the beam
		Vector3d xref = convertVector(ray.get_refPoint().toVector3D());
		System.out.println("xref:  " + xref.toStlString());
		Vector3d u = convertVector(ray.get_dirVec()); 
		
		Vector3d e = new Vector3d(xb,yb,0);
		Vector3d l = new Vector3d(0,0,1);
		
		//in this case 
		Vector3d n = new Vector3d(u.x, u.y, 0);
		n = n.normalized();
		Vector3d s = l.cross(n);

		
		double udotn = u.dot(n);
		if(Math.abs(udotn)<0.01)
			return false;
		double sdotu = s.dot(u);
		Vector3d extrap = xref.plus(u.times(n.dot(e.minus(xref))/udotn));
		

		//this should be about equal to the beam width
		double resolution = 0.2;
		

		V.set(i, i, Math.pow(resolution,2));


		Vector3d sp = s.minus(n.times(sdotu/udotn));
		if(sp.magnitude() > 10) {  //this can only happen if the angle between the track and the normal is small
			//System.out.println("rejecting track");
			return false;
		}
		int index = nAlignables-1;


		//Use the same reference point for both inner and outer layer of region
		Vector3d cref = new Vector3d(0,0,0);



		//for debugging
		/*
		double phi1 = Math.atan2(n.y, n.x), phi2 = Math.atan2(cref.y, cref.x);
		double dphi = phi1-phi2;
		while (dphi < -Math.PI)
			dphi += 2*Math.PI;
		while (dphi > Math.PI)
			dphi -= 2*Math.PI;
		System.out.println(layer + " "+phi1 + " " + phi2 + " " + dphi);
		 */

		Vector3d dmdr =sp.cross(extrap).plus(n.cross(cref).times(sdotu/udotn));
		dmdr = dmdr.minus(n.cross(u).times(n.dot(e.minus(extrap))*sdotu/(udotn*udotn)));
		/*A.set(i, (svtTopBottomSep? i : i/2)*6 + 0, -sp.x);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 1, -sp.y);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 2, -sp.z);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 3, dmdr.x);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 4, dmdr.y);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 5, dmdr.z);*/
		
		//System.out.println("i = " + i + "; rows = " + A.getRowDimension() + "; cols = " + + A.getColumnDimension());
		if(orderTx >= 0)
			A.set(i, i*nAlignVars + orderTx, -sp.x);
		if(orderTy >= 0)
			A.set(i, i*nAlignVars + orderTy, -sp.y);
		if(orderTz >= 0)
			A.set(i, i*nAlignVars + orderTz, -sp.z);
		if(orderRx >= 0)
			A.set(i, i*nAlignVars + orderRx, dmdr.x);
		if(orderRy >= 0)
			A.set(i, i*nAlignVars + orderRy, dmdr.y);
		if(orderRz >= 0)
			A.set(i, i*nAlignVars + orderRz, dmdr.z);



		I.set(i, 0, index);

		Vector3d dmdu = sp.times(e.minus(xref).dot(n)/udotn);
		if(!this.useDocaPhiZTandip) {
			B.set(i,0, sp.x);
			B.set(i,1, sp.z);
			B.set(i,2, dmdu.x);
			B.set(i,3, dmdu.z);
		} else {

			double phi = Math.atan2(u.y,u.x);
			Vector3d csphi = new Vector3d(Math.cos(phi), Math.sin(phi),0);
			Vector3d mscphi = new Vector3d(-Math.sin(phi), Math.cos(phi),0);
			double cosdip = Math.hypot(u.x, u.y);
			double d = mscphi.dot(xref);
			B.set(i, 0, s.dot(mscphi.minus(u.times(n.dot(mscphi)/udotn))));
			//B.set(i, 1, s.dot(csphi.times(-d)
			//		.plus(mscphi.times(n.dot(e.minus(xref))/udotn))
			//		.minus(u.times(mscphi.dot(n)*n.dot(e.minus(xref))/(udotn*udotn)))
			//		.plus(u.times(d*n.dot(csphi)/udotn)))
			//		);
			B.set(i, 1, -s.dot(csphi)*d
					+ cosdip*(s.dot(mscphi)/udotn-sdotu/(udotn*udotn)*n.dot(mscphi))*n.dot(e.minus(xref))
					+ sdotu/udotn*d*n.dot(csphi));
			B.set(i, 2, s.z-sdotu*n.z/udotn);
			B.set(i, 3, (s.z/udotn-n.z*sdotu/(udotn*udotn))*n.dot(e.minus(xref)));


		}
		//dm.set(i,0, s.dot(e.minus(extrap)));
		
		double ci = s.dot(extrap);
		double mi = s.dot(e);
		

		System.out.println(extrap.toStlString());
		System.out.println(e.toStlString());
		System.out.println(extrap.minus(e).toStlString());
		System.out.println(s.toStlString());
		
		if(Math.abs(ci-mi)>maxResidualCut)
			return false;
		c.set(i,0,ci);
		m.set(i,0,mi);
		
		return true;
	}

	//returns false if there's a problem
	private boolean fillMatricesSVT(int i, Ray ray, Cluster cl, Matrix A, Matrix B, Matrix V, Matrix m, Matrix c, Matrix I) {
		int region = cl.get_Region();
		int layer = cl.get_Layer();
		int sector = cl.get_Sector();
		//System.out.println("RLS " + region + " " + layer + " " + sector);
		//System.out.println("th" + c.get_Phi());
		double centroid = cl.get_Centroid();

		// this avoids a certain bug that only occurs if
		// there is a single-hit cluster on the last strip,
		// in which obtaining the next strip (line2) gives 
		// an IllegalArgumentException


		if(centroid == SVTConstants.NSTRIPS)
			centroid = SVTConstants.NSTRIPS-.001;
		Line3d line1 = SVTGeom.getStripFactory().getShiftedStrip(layer-1, sector-1, (int)Math.floor(centroid)-1);
		Line3d line2 = SVTGeom.getStripFactory().getShiftedStrip(layer-1, sector-1, (int)Math.floor(centroid)-0); 

		//System.out.println( SVTConstants.getLayerSectorAlignmentData()[0][0][1]);


		//take the weighted average of the directions of the two lines.
		Vector3d l = line1.diff().normalized().times(1-(centroid%1)).add(line2.diff().normalized().times((centroid%1))).normalized();

		Vector3d e1 = line1.origin();
		Vector3d e2 = line2.origin();
		Vector3d e = e1.times(1-(centroid%1)).add(e2.times((centroid%1)));
		Vector3d s = e2.minus(e1);
		s = s.minus(l.times(s.dot(l))).normalized();

		Vector3d xref = convertVector(ray.get_refPoint().toVector3D());
		Vector3d u = convertVector(ray.get_dirVec()); 
		Vector3d n = l.cross(s);
		double udotn = u.dot(n);
		if(Math.abs(udotn)<0.01) {
			if(debug) System.out.println("rejecting track:  abs(udotn)<0.01");
			return false;	
		}
		double sdotu = s.dot(u);
		Vector3d extrap = xref.plus(u.times(n.dot(e.minus(xref))/udotn));


		//System.out.println(extrap.toStlString());
		double resolution = cl.get_ResolutionAlongZ(extrap.z-SVTConstants.Z0ACTIVE[(layer-1)/2], SVTGeom);
		//System.out.println("resolution:  " + resolution + "; z=" + extrap.z-);

		V.set(i, i, Math.pow(resolution,2));


		Vector3d sp = s.minus(n.times(sdotu/udotn));
		if(sp.magnitude() > 10) {  //this can only happen if the angle between the track and the normal is small
			if(debug) System.out.println("rejecting track:  sp.magnitude() > 10");
			return false;
		}
		int index = getIndexSVT(layer-1, sector-1);


		//Use the same reference point for both inner and outer layer of region
		Vector3d cref = getModuleReferencePoint(sector,layer);



		//for debugging
		/*
		double phi1 = Math.atan2(n.y, n.x), phi2 = Math.atan2(cref.y, cref.x);
		double dphi = phi1-phi2;
		while (dphi < -Math.PI)
			dphi += 2*Math.PI;
		while (dphi > Math.PI)
			dphi -= 2*Math.PI;
		System.out.println(layer + " "+phi1 + " " + phi2 + " " + dphi);
		 */

		Vector3d dmdr =sp.cross(extrap).plus(n.cross(cref).times(sdotu/udotn));
		dmdr = dmdr.minus(n.cross(u).times(n.dot(e.minus(extrap))*sdotu/(udotn*udotn)));
		/*A.set(i, (svtTopBottomSep? i : i/2)*6 + 0, -sp.x);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 1, -sp.y);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 2, -sp.z);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 3, dmdr.x);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 4, dmdr.y);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 5, dmdr.z);*/
		if(orderTx >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderTx, -sp.x);
		if(orderTy >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderTy, -sp.y);
		if(orderTz >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderTz, -sp.z);
		if(orderRx >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderRx, dmdr.x);
		if(orderRy >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderRy, dmdr.y);
		if(orderRz >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderRz, dmdr.z);



		I.set(i, 0, index);

		Vector3d dmdu = sp.times(e.minus(xref).dot(n)/udotn);
		if(!this.useDocaPhiZTandip) {
			B.set(i,0, sp.x);
			B.set(i,1, sp.z);
			B.set(i,2, dmdu.x);
			B.set(i,3, dmdu.z);
		} else {

			double phi = Math.atan2(u.y,u.x);
			Vector3d csphi = new Vector3d(Math.cos(phi), Math.sin(phi),0);
			Vector3d mscphi = new Vector3d(-Math.sin(phi), Math.cos(phi),0);
			double cosdip = Math.hypot(u.x, u.y);
			double d = mscphi.dot(xref);
			B.set(i, 0, s.dot(mscphi.minus(u.times(n.dot(mscphi)/udotn))));
			//B.set(i, 1, s.dot(csphi.times(-d)
			//		.plus(mscphi.times(n.dot(e.minus(xref))/udotn))
			//		.minus(u.times(mscphi.dot(n)*n.dot(e.minus(xref))/(udotn*udotn)))
			//		.plus(u.times(d*n.dot(csphi)/udotn)))
			//		);
			B.set(i, 1, -s.dot(csphi)*d
					+ cosdip*(s.dot(mscphi)/udotn-sdotu/(udotn*udotn)*n.dot(mscphi))*n.dot(e.minus(xref))
					+ sdotu/udotn*d*n.dot(csphi));
			B.set(i, 2, s.z-sdotu*n.z/udotn);
			B.set(i, 3, (s.z/udotn-n.z*sdotu/(udotn*udotn))*n.dot(e.minus(xref)));


		}
		//dm.set(i,0, s.dot(e.minus(extrap)));
		
		double ci = s.dot(extrap);
		double mi = s.dot(e);
		if(Math.abs(ci-mi)>maxResidualCut) {
			if(debug) System.out.println("rejecting track:  Math.abs(ci-mi)>maxResidualCut");
			return false;
		}
		c.set(i,0,ci);
		m.set(i,0,mi);
		
		
		return true;

	}
	boolean debug = false;

	
	private boolean fillMatricesBMTZ(int i, Ray ray, Cluster cl, Matrix A, Matrix B, Matrix V, Matrix m, Matrix c,
			Matrix I) {
		int region = cl.get_Region();
		int layer = cl.get_Layer();
		int sector = cl.get_Sector();
		double centroid = cl.get_Centroid();
		//Z layer
		if(centroid == org.jlab.rec.cvt.bmt.Constants.getCRZNSTRIPS()[region-1])
			centroid = org.jlab.rec.cvt.bmt.Constants.getCRZNSTRIPS()[region-1]-.001;
		Line3d line1 = convertLine(BMTGeom.getZstrip(region, sector, (int)Math.floor(centroid)-0));
		Line3d line2 = convertLine(BMTGeom.getZstrip(region, sector, (int)Math.floor(centroid)+1)); 



		Vector3d l = line1.diff().normalized().times(1-(centroid%1)).add(line2.diff().normalized().times((centroid%1))).normalized();

		Vector3d e1 = line1.origin();
		Vector3d e2 = line2.origin();
		Vector3d e = e1.times(1-(centroid%1)).add(e2.times((centroid%1)));
		Vector3d s = e2.minus(e1);
		s = s.minus(l.times(s.dot(l))).normalized();


		Vector3d xref = convertVector(ray.get_refPoint().toVector3D());
		Vector3d u = convertVector(ray.get_dirVec()); 
		Vector3d n = l.cross(s);
		double udotn = u.dot(n);
		if(Math.abs(udotn)<0.01)
			return false;
		double sdotu = s.dot(u);

		Vector3d extrap = xref.plus(u.times(n.dot(e.minus(xref))/udotn));
		//org.jlab.rec.cvt.bmt.Constants.
		//Vector3d extrap = xref.plus(u.times(n.dot(e.minus(xref))/udotn));

		//System.out.println(extrap.toStlString());
		double resolution = e1.distance(e2)/Math.sqrt(12);
		
		
		V.set(i, i, Math.pow(resolution,2));


		Vector3d sp = s.minus(n.times(sdotu/udotn));
		if(sp.magnitude() > 10) {  //this can only happen if the angle between the track and the normal is small
			System.out.println("rejecting track");
			return false;
		}
		int index = getIndexBMT(layer-1, sector-1);


		//Use the same reference point for both inner and outer layer of region
		Vector3d cref = getModuleReferencePoint(sector,layer);



		//for debugging
		/*
			double phi1 = Math.atan2(n.y, n.x), phi2 = Math.atan2(cref.y, cref.x);
			double dphi = phi1-phi2;
			while (dphi < -Math.PI)
				dphi += 2*Math.PI;
			while (dphi > Math.PI)
				dphi -= 2*Math.PI;
			System.out.println(layer + " "+phi1 + " " + phi2 + " " + dphi);
		 */

		Vector3d dmdr =sp.cross(extrap).plus(n.cross(cref).times(sdotu/udotn));
		dmdr = dmdr.minus(n.cross(u).times(n.dot(e.minus(extrap))*sdotu/(udotn*udotn)));
		/*A.set(i, (svtTopBottomSep? i : i/2)*6 + 0, -sp.x);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 1, -sp.y);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 2, -sp.z);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 3, dmdr.x);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 4, dmdr.y);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 5, dmdr.z);*/
		if(orderTx >= 0)
			A.set(i, i*nAlignVars + orderTx, -sp.x);
		if(orderTy >= 0)
			A.set(i, i*nAlignVars + orderTy, -sp.y);
		if(orderTz >= 0)
			A.set(i, i*nAlignVars + orderTz, -sp.z);
		if(orderRx >= 0)
			A.set(i, i*nAlignVars + orderRx, dmdr.x);
		if(orderRy >= 0)
			A.set(i, i*nAlignVars + orderRy, dmdr.y);
		if(orderRz >= 0)
			A.set(i, i*nAlignVars + orderRz, dmdr.z);



		I.set(i, 0, index);

		Vector3d dmdu = sp.times(e.minus(xref).dot(n)/udotn);
		if(!this.useDocaPhiZTandip) {
			B.set(i,0, sp.x);
			B.set(i,1, sp.z);
			B.set(i,2, dmdu.x);
			B.set(i,3, dmdu.z);
		} else {

			double phi = Math.atan2(u.y,u.x);
			Vector3d csphi = new Vector3d(Math.cos(phi), Math.sin(phi),0);
			Vector3d mscphi = new Vector3d(-Math.sin(phi), Math.cos(phi),0);
			double cosdip = Math.hypot(u.x, u.y);
			double d = mscphi.dot(xref);
			B.set(i, 0, s.dot(mscphi.minus(u.times(n.dot(mscphi)/udotn))));
			//B.set(i, 1, s.dot(csphi.times(-d)
			//		.plus(mscphi.times(n.dot(e.minus(xref))/udotn))
			//		.minus(u.times(mscphi.dot(n)*n.dot(e.minus(xref))/(udotn*udotn)))
			//		.plus(u.times(d*n.dot(csphi)/udotn)))
			//		);
			B.set(i, 1, -s.dot(csphi)*d
					+ cosdip*(s.dot(mscphi)/udotn-sdotu/(udotn*udotn)*n.dot(mscphi))*n.dot(e.minus(xref))
					+ sdotu/udotn*d*n.dot(csphi));
			B.set(i, 2, s.z-sdotu*n.z/udotn);
			B.set(i, 3, (s.z/udotn-n.z*sdotu/(udotn*udotn))*n.dot(e.minus(xref)));


		}
		//dm.set(i,0, s.dot(e.minus(extrap)));
		//c.set(i,0,s.dot(extrap));
		//m.set(i,0,s.dot(e));
		double mi = e1.distance(e2)*centroid;
		double ci = mi+s.dot(extrap.minus(e));
		m.set(i, 0, mi);
		if(Math.abs(ci-mi)>maxResidualCut)
			return false;
		c.set(i, 0, ci);
		
		return true;

	}
	private boolean fillMatricesBMTC(int i, Ray ray, Cluster cl, Matrix A, Matrix B, Matrix V, Matrix m, Matrix c,
			Matrix I) {

		int region = cl.get_Region();
		int layer = cl.get_Layer();
		int sector = cl.get_Sector();
		//System.out.println(region+" "+layer +" "+sector);
		double centroid = cl.get_Centroid();
		if(centroid == org.jlab.rec.cvt.bmt.Constants.getCRCNSTRIPS()[region-1])
			centroid = org.jlab.rec.cvt.bmt.Constants.getCRCNSTRIPS()[region-1]-.001;
		
		Vector3d xref = convertVector(ray.get_refPoint().toVector3D());
		Vector3d u = convertVector(ray.get_dirVec()); 
		
		Arc3D arc1 = BMTGeom.getCstrip(region, sector, (int)Math.floor(centroid)-1);
		Arc3D arc2 = BMTGeom.getCstrip(region, sector, (int)Math.floor(centroid)-0); 
		
		//find the extrapolation point to the cylinder
		double R = arc1.radius();
		//cylindrical axis
		Vector3d axis = convertVector(arc1.normal());
		Vector3d center = convertVector(arc1.center().toVector3D());
		double a = 1-Math.pow(u.dot(axis),2);
		double b = 2*center.minus(xref).dot(u.minus(axis.times(axis.dot(u))));
		double cc = xref.minus(center).minus(axis.times(axis.dot(xref.minus(center)))).magnitudeSq()-R*R;
		// two intersections between line and cylinder.  Whichever is closest to the midpoint of the strip
		// is chosen
		double lambda = (-b+Math.sqrt(b*b-4*a*cc))/(2*a);
		double lambda_alt = (-b-Math.sqrt(b*b-4*a*cc))/(2*a);
		Vector3d extrap1 = xref.plus(u.times(lambda));
		Vector3d extrap2 = xref.plus(u.times(lambda_alt));
		Vector3d midpoint = midpoint(arc1).midpoint(midpoint(arc2));
		Vector3d extrap = null;
		if(extrap1.distance(midpoint) > (extrap2.distance(midpoint)))
			extrap = extrap2;
		else
			extrap = extrap1;
		
		Vector3d s = axis;
		Vector3d n = extrap.minus(center);
		n = n.minus(axis.times(axis.dot(n))).normalized();
		Vector3d l = s.cross(n);
		

		//System.out.printf("s vector %f %f %f\n", s.x,s.y,s.z);
		//System.out.printf("n vector %f %f %f\n", n.x,n.y,n.z);
		//System.out.printf("l vector %f %f %f\n", l.x,l.y,l.z);
		//Vector3d l = line1.diff().normalized().times(1-(centroid%1)).add(line2.diff().normalized().times((centroid%1))).normalized();

		Vector3d e1 = convertVector(arc1.origin().toVector3D());
		Vector3d e2 = convertVector(arc2.origin().toVector3D());
		Vector3d e = e1.times(1-(centroid%1)).add(e2.times((centroid%1)));
		//Vector3d s = e2.minus(e1);
		//s = s.minus(l.times(s.dot(l))).normalized();


		
		double udotn = u.dot(n);
		if(Math.abs(udotn)<0.01)
			return false;
		double sdotu = s.dot(u);

		
		//org.jlab.rec.cvt.bmt.Constants.
		//Vector3d extrap = xref.plus(u.times(n.dot(e.minus(xref))/udotn));

		//System.out.println(extrap.toStlString());
		
			
		double resolution = e1.distance(e2)/Math.sqrt(12);
		//fake resolution 
		resolution = 1;	
		V.set(i, i, Math.pow(resolution,2));


		Vector3d sp = s.minus(n.times(sdotu/udotn));
		if(sp.magnitude() > 10) {  //this can only happen if the angle between the track and the normal is small
			System.out.println("rejecting track");
			return false;
		}
		int index = getIndexBMT(layer-1, sector-1);


		//Use the same reference point for both inner and outer layer of region
		Vector3d cref = getModuleReferencePoint(sector,layer);

		Vector3d dmdr =sp.cross(extrap).plus(n.cross(cref).times(sdotu/udotn));
		dmdr = dmdr.minus(n.cross(u).times(n.dot(e.minus(extrap))*sdotu/(udotn*udotn)));
		/*A.set(i, (svtTopBottomSep? i : i/2)*6 + 0, -sp.x);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 1, -sp.y);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 2, -sp.z);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 3, dmdr.x);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 4, dmdr.y);
			A.set(i, (svtTopBottomSep? i : i/2)*6 + 5, dmdr.z);*/
		if(orderTx >= 0)
			A.set(i, i*nAlignVars + orderTx, -sp.x);
		if(orderTy >= 0)
			A.set(i, i*nAlignVars + orderTy, -sp.y);
		if(orderTz >= 0)
			A.set(i, i*nAlignVars + orderTz, -sp.z);
		if(orderRx >= 0)
			A.set(i, i*nAlignVars + orderRx, dmdr.x);
		if(orderRy >= 0)
			A.set(i, i*nAlignVars + orderRy, dmdr.y);
		if(orderRz >= 0)
			A.set(i, i*nAlignVars + orderRz, dmdr.z);



		I.set(i, 0, index);

		Vector3d dmdu = sp.times(e.minus(xref).dot(n)/udotn);
		if(!this.useDocaPhiZTandip) {
			B.set(i,0, sp.x);
			B.set(i,1, sp.z);
			B.set(i,2, dmdu.x);
			B.set(i,3, dmdu.z);
		} else {

			double phi = Math.atan2(u.y,u.x);
			Vector3d csphi = new Vector3d(Math.cos(phi), Math.sin(phi),0);
			Vector3d mscphi = new Vector3d(-Math.sin(phi), Math.cos(phi),0);
			double cosdip = Math.hypot(u.x, u.y);
			double d = mscphi.dot(xref);
			B.set(i, 0, s.dot(mscphi.minus(u.times(n.dot(mscphi)/udotn))));
			//B.set(i, 1, s.dot(csphi.times(-d)
			//		.plus(mscphi.times(n.dot(e.minus(xref))/udotn))
			//		.minus(u.times(mscphi.dot(n)*n.dot(e.minus(xref))/(udotn*udotn)))
			//		.plus(u.times(d*n.dot(csphi)/udotn)))
			//		);
			B.set(i, 1, -s.dot(csphi)*d
					+ cosdip*(s.dot(mscphi)/udotn-sdotu/(udotn*udotn)*n.dot(mscphi))*n.dot(extrap.minus(xref))
					+ sdotu/udotn*d*n.dot(csphi));
			B.set(i, 2, s.z-sdotu*n.z/udotn);
			B.set(i, 3, (s.z/udotn-n.z*sdotu/(udotn*udotn))*n.dot(extrap.minus(xref)));


		}
		//dm.set(i,0, s.dot(e.minus(extrap)));
		double ci = s.dot(extrap);
		double mi = s.dot(e);
		if(Math.abs(ci-mi)>maxResidualCut)
			return false;
		c.set(i,0,ci);
		m.set(i,0,mi);
		
		return true;
	}

	private Vector3d midpoint(Arc3D arc1) {
		
		return convertVector(arc1.point(arc1.theta()/2).toVector3D());
	}

	private int getIndex(Cluster c) {
		int layer = c.get_Layer()-1;
		int sector = c.get_Sector()-1;
		int index = -1;
		if(c.get_Detector() == 0) {
			int region = layer/2;
			if (region == 0)
				index = sector;
			else if (region == 1)
				index =  org.jlab.rec.cvt.svt.Constants.NSECT[0] + sector;
			else if (region == 2)
				index = org.jlab.rec.cvt.svt.Constants.NSECT[0] +
				org.jlab.rec.cvt.svt.Constants.NSECT[2] + sector;
			if(svtTopBottomSep && layer%2==1) {
				index += 42;
			}
		} else if (c.get_Detector() == 1) {
			index =  84+layer*3+sector;
		}

		return index;
	}

	private int getIndexBMT(int layer, int sector) {
		if (layer < 0 || sector < 0)
			return -1;
		return 84+layer*3+sector;
	}

	private int getIndexSVT(int layer, int sect){
		int index = -1;
		int region = layer/2;
		if (region == 0)
			index = sect;
		else if (region == 1)
			index =  org.jlab.rec.cvt.svt.Constants.NSECT[0] + sect;
		else if (region == 2)
			index = org.jlab.rec.cvt.svt.Constants.NSECT[0] +
			org.jlab.rec.cvt.svt.Constants.NSECT[2] + sect;
		if(svtTopBottomSep && layer%2==1) {
			index += 42;
		}
		return index;

	}

	private Vector3d getModuleReferencePoint(int sector, int layer) {
		return SVTAlignmentFactory.getIdealFiducialCenter((layer-1)/2, sector-1);
	}

	@Override
	public boolean init() {
		if(this.getEngineConfiguration() == null || "null".equals(this.getEngineConfiguration())) {
			return true; //prevents init from being run twice.
		}
		// Load config
		String rmReg = this.getEngineConfigString("removeRegion");

		if (rmReg!=null) {
			System.out.println("["+this.getName()+"] run with region "+rmReg+"removed config chosen based on yaml");
			Constants.setRmReg(Integer.valueOf(rmReg));
		}
		else {
			rmReg = System.getenv("COAT_CVT_REMOVEREGION");
			if (rmReg!=null) {
				System.out.println("["+this.getName()+"] run with region "+rmReg+"removed config chosen based on env");
				Constants.setRmReg(Integer.valueOf(rmReg));
			}
		}
		if (rmReg==null) {
			System.out.println("["+this.getName()+"] run with all region (default) ");
		}
		//svt stand-alone
		String svtStAl = this.getEngineConfigString("svtOnly");

		if (svtStAl!=null) {
			System.out.println("["+this.getName()+"] align SVT only "+svtStAl+" config chosen based on yaml");
			this.isSVTonly= Boolean.valueOf(svtStAl);
		}
		else {
			svtStAl = System.getenv("COAT_ALIGN_SVT_ONLY");
			if (svtStAl!=null) {
				System.out.println("["+this.getName()+"] align SVT only "+svtStAl+" config chosen based on env");
				this.isSVTonly= Boolean.valueOf(svtStAl);
			}
		}
		if (svtStAl==null) {
			System.out.println("["+this.getName()+"] align SVT only (default) ");
			this.isSVTonly = true;
		}
		
		String bmtOnly = this.getEngineConfigString("bmtOnly");
		
		if (bmtOnly!=null) {
			System.out.println("["+this.getName()+"] align BMT only "+bmtOnly+" config chosen based on yaml");
			this.isBMTonly= Boolean.valueOf(bmtOnly);
		}
		else {
			bmtOnly = System.getenv("COAT_ALIGN_SVT_ONLY");
			if (bmtOnly!=null) {
				System.out.println("["+this.getName()+"] align BMT only "+bmtOnly+" config chosen based on env");
				this.isBMTonly= Boolean.valueOf(bmtOnly);
			}
		}
		if (bmtOnly==null) {
			System.out.println("["+this.getName()+"] not BMT only (default) ");
			this.isBMTonly = false;
		}
		// Load other geometries

		variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
		System.out.println(" CVT YAML VARIATION NAME + "+variationName);

		System.out.println("SVT LOADING WITH VARIATION "+variationName);
		DatabaseConstantProvider cp = new DatabaseConstantProvider(11, variationName);
		//cp = new HackConstantsProvider(cp);
		cp = SVTConstants.connect( cp );
		cp.disconnect();  
		CCDBConstantsLoader.Load(new DatabaseConstantProvider(11, variationName));
		xb = org.jlab.rec.cvt.Constants.getXb();
	    yb = org.jlab.rec.cvt.Constants.getYb();
		System.out.println("Check SVT Geom lay1 sec1:  " + Arrays.toString(SVTConstants.getLayerSectorAlignmentData()[0][0]));
		System.out.println("Check SVT Geom lay1 sec1:  " + Arrays.toString(SVTConstants.getLayerSectorAlignmentData()[0][1]));
		SVTStripFactory svtFac = new SVTStripFactory(cp, true);
		SVTGeom.setSvtStripFactory(svtFac);

		String svtTopBottomSep = this.getEngineConfigString("svtAlignTopBottomSeparately");
		if (svtTopBottomSep!=null) {
			System.out.println("["+this.getName()+"] run with SVT alignment for top and bottom as separate modules "+svtTopBottomSep+" config chosen based on yaml");
			this.svtTopBottomSep= Boolean.valueOf(svtTopBottomSep);
		}
		else {
			svtTopBottomSep = System.getenv("COAT_SVT_TOP_BOTTOM");
			if (svtTopBottomSep!=null) {
				System.out.println("["+this.getName()+"] run with SVT alignment for top and bottom as separate modules "+svtTopBottomSep+" config chosen based on env");
				this.svtTopBottomSep= Boolean.valueOf(svtTopBottomSep);
			}
		}
		if (svtTopBottomSep==null) {
			System.out.println("["+this.getName()+"] run with SVT top and bottom as a single module (default) ");
			this.svtTopBottomSep = false;
		}

		String alignVars = this.getEngineConfigString("alignVariables");
		if (alignVars!=null) {
			System.out.println("["+this.getName()+"] obtain alignment derivatives for the following variables "+svtTopBottomSep+" config chosen based on yaml");
			this.setAlignVars(alignVars);
		}
		else {
			alignVars = System.getenv("COAT_ALIGN_VARS");
			if (alignVars!=null) {
				System.out.println("["+this.getName()+"] obtain alignment derivatives for the following variables "+svtTopBottomSep+" config chosen based on env");
				this.setAlignVars(alignVars);
			}
		}
		if (alignVars==null) {
			System.out.println("["+this.getName()+"] obtain alignment derivatives for all 6 variables (default) ");
			this.setAlignVars("Tx Ty Tz Rx Ry Rz");
		}


		String maxDocaCut = this.getEngineConfigString("maxDocaCut");

		if(maxDocaCut != null) {
			System.out.println("["+this.getName()+"] max doca cut "+ maxDocaCut + " mm");
			this.maxDocaCut = Double.parseDouble(maxDocaCut);
		}
		else {
			System.out.println("["+this.getName()+"] no max doca cut set (default)");
			this.maxDocaCut = Double.MAX_VALUE;
		}

		String cosmics = this.getEngineConfigString("cosmics");

		if(cosmics != null) {
			System.out.println("["+this.getName()+"] use cosmics bank instead of tracks bank? "+ cosmics );
			this.isCosmics = Boolean.parseBoolean(cosmics);
		}
		else {
			System.out.println("["+this.getName()+"] using tracks bank (default)");
			this.isCosmics = false;
		}

		//svt stand-alone
		String maxResidual = this.getEngineConfigString("maxResidual");

		if (maxResidual!=null) {
			System.out.println("["+this.getName()+"] run with cut on maximum residual "+maxResidual+" config chosen based on yaml");
			this.maxResidualCut =  Double.valueOf(maxResidual);
		}

		if (maxResidual==null) {
			System.out.println("["+this.getName()+"] run with maximum residual cut setting default = none");
			this.maxResidualCut = Double.MAX_VALUE;
		}

		for(int layer = 0; layer<6; layer++)
		{
			Line3d line = SVTGeom.getStrip(layer, 0, 0);
			System.out.println("debug. Layer" + layer + " (" + line.origin().x + ", "+ line.origin().y + ", "+ line.origin().z+"), "
					+ " (" + line.end().x + ", "+ line.end().y + ", "+ line.end().z+"), ");
		}

		this.nAlignables = ((this.svtTopBottomSep ? 2*42 : 42) + (this.isSVTonly ? 0: 18) + (isCosmics? 0 : 1));
		
		//svt stand-alone
		String debug = this.getEngineConfigString("debug");

		if (debug!=null) {
			System.out.println("["+this.getName()+"] debug "+debug+" config chosen based on yaml");
			this.debug =  Boolean.parseBoolean(debug);
		} else {
			System.out.println("["+this.getName()+"] debug false; config chosen based on yaml");
			this.debug =  false;
		}
		
		
		return true;
	}

	double xb, yb;
	
	double maxResidualCut;

	double maxDocaCut;



	private void setAlignVars(String alignVars) {
		String split[] = alignVars.split("[ \t]+");
		int i = 0;
		orderTx = -1;
		orderTy = -1;
		orderTz = -1;
		orderRx = -1;
		orderRy = -1;
		orderRz = -1;
		for(String s : split) {
			if(s.equals("Tx")) {
				orderTx = i; i++;
			} else if(s.equals("Ty")) {
				orderTy = i; i++;
			} else if(s.equals("Tz")) {
				orderTz = i; i++;
			} else if(s.equals("Rx")) {
				orderRx = i; i++;
			} else if(s.equals("Ry")) {
				orderRy = i; i++;
			} else if(s.equals("Rz")) {
				orderRz = i; i++;
			}
		}
		nAlignVars = i;
		System.out.println(nAlignVars + " alignment variables requested");
	}
	private int nAlignVars;
	private int orderTx;
	private int orderTy;
	private int orderTz;
	private int orderRx;
	private int orderRy;
	private int orderRz; 

	private String variationName;


}