package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jlab.clas.reco.ReconstructionEngine;
//import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.base.DetectorType;
//import org.jlab.detector.calib.utils.DatabaseConstantProvider;
//import org.jlab.detector.geant4.v2.SVT.SVTConstants;
//import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;

import org.jlab.rec.cvt.banks.AlignmentBankWriter;
import org.jlab.rec.cvt.banks.RecoBankReader;
import org.jlab.rec.cvt.bmt.BMTType;
//import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.Trajectory;

import Jama.Matrix;
//import eu.mihosoft.vrl.v3d.Vector3d;
import cnuphys.magfield.MagneticFields;

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class CVTAlignment extends ReconstructionEngine {

	//org.jlab.rec.cvt.bmt.BMTGeometry BMTGeom;
	//CTOFGeant4Factory CTOFGeom;
	//SVTStripFactory svtIdealStripFactory;

	public CVTAlignment() {
		super("CVTAlignment", "spaul", "4.0");

		//BMTGeom = new org.jlab.rec.cvt.bmt.BMTGeometry();

	}

	String FieldsConfig = "";
	int Run = -1;
	public boolean isSVTonly = false;
	private Boolean svtTopBottomSep;
	/*public void setRunConditionsParameters(DataEvent event, String FieldsConfig, int iRun, boolean addMisAlignmts, String misAlgnFile) {
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

			//System.out.println(" LOADING BMT GEOMETRY...............................variation = "+variationName);
			//CCDBConstantsLoader.Load(new DatabaseConstantProvider(newRun, variationName));
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
	}*/

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
	boolean skipBMTC = false;
	
	//total counts of clusters on each type of channel
	int gCountBMTC = 0;
	int gCountSVT = 0;
	int gCountBMTZ =0;
	int debugPrintEventCount = 0;
	
	//check if there are multiple hits in the same BMTC tile (can happen sometimes in cosmic tracks)
	//boolean checkClusterAmbiguityBMTC = true;
	
	@Override
	public boolean processDataEvent(DataEvent event) {
		int runNum = event.getBank("RUN::config").getInt("run", 0);
		int eventNum = event.getBank("RUN::config").getInt("event", 0);

		if((debugPrintEventCount++) %1000==0)
			System.out.println("BMTC total clusters: " + gCountBMTC+"; BMTZ total clusters: " + gCountBMTZ+"; SVT total crosses: "+ gCountSVT);
		


		//this.setRunConditionsParameters(event, FieldsConfig, Run, false, "");
		double shift = 0;//org.jlab.rec.cvt.Constants.getZoffset();;

		//this.FieldsConfig = this.getFieldsConfig();


		RecoBankReader reader = new RecoBankReader();

		//reader.fetch_Cosmics(event, SVTGeom, 0);

		List<? extends Trajectory> tracks;
		if(isCosmics) {
			reader.fetch_Cosmics(event, shift);
			tracks = reader.get_Cosmics();
		} else {
			reader.fetch_Tracks(event, shift);
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
		List<Matrix> qs = new ArrayList<Matrix>();
		List<Integer> trackIDs = new ArrayList<Integer>();

		if (tracks.size() < 3)
		tracksLoop : for (Trajectory track : tracks) {

			if(Math.abs(getDoca(track))>maxDocaCut)
				continue;
			/*System.out.println("track read: ");
			System.out.println("track chi2: "+ track.get_chi2());
			System.out.println("ndf: "+ track.get_ndf());
			System.out.println("ncrosses: "+ track.size());
			System.out.println("ray: "+ track.getRay().getRefPoint() + 
					" + lambda*" + track.getRay().getDirVec());
			System.out.println();*/

			//System.out.println("BMT crosses");
			int nCrossSVT = 0, nCrossBMT = 0;
			int countBMTZ = 0, countBMTC = 0;
			for(Cross c : track) {
				if(c.getDetector() == DetectorType.BST && !isBMTonly) {
					nCrossSVT++;
					gCountSVT++;
				}
				if(c.getDetector() == DetectorType.BMT && !isSVTonly) {
					if (c.getCluster1().getType() != BMTType.C || ! skipBMTC)
						nCrossBMT++;
					if (c.getCluster1().getType() == BMTType.C) {
						countBMTC++;
						gCountBMTC++;
					}
					if(c.getCluster1().getType() == BMTType.Z) {
						gCountBMTZ++;
						countBMTZ++;
					}
					//System.out.println(c.getSector()+" "+c.getRegion() + " " + c.getCluster1().getCentroid()+" " + c.getId());
				}
				if(nCrossBMT>12) {
					System.out.println("Too many BMT crosses!");
					return false;
				}
			}
			
			
			if(nCrossSVT*2<minClustersSVT)
				continue;
			if(countBMTZ<minClustersBMTZ)
				continue;
			if(countBMTC<minClustersBMTC)
				continue;
			
			if(nCrossSVT+countBMTZ< 3) //no transverse degrees of freedom
				continue;
			if(nCrossSVT+countBMTC< 3) //no transverse degrees of freedom
				continue;
			//int nCross = nCrossSVT + nCrossBMT;
			//if(nCross <= 2)
			// 	continue;
			Ray ray = track.getRay();
			if(ray == null) {
				ray = getRay(track.getHelix());
				//System.out.println("curvature " +  track.getHelix().get_curvature());
				//System.out.println("doca " +  track.getHelix().get_dca());
				if(Math.abs(track.getHelix().getCurvature())>0.001) {
					continue;
				}
			}
			//getRay(track);
			//System.out.println(ray.getDirVec().toString());
			//System.out.println(ray.getRefPoint().toString());


			int paramsFromBeamspot = (isCosmics || ! includeBeamspot ? 0:1);
			int colsA = nAlignVars*((svtTopBottomSep ? 2*nCrossSVT : nCrossSVT) + nCrossBMT + paramsFromBeamspot);
			int rows = 2*nCrossSVT+nCrossBMT + paramsFromBeamspot;
			Matrix A = new Matrix(rows, colsA);//not sure why there aren't 6 columns
			Matrix B = new Matrix(rows, 4);
			Matrix V = new Matrix(rows,rows);
			Matrix m = new Matrix(rows,1);
			Matrix c = new Matrix(rows,1);
			Matrix I = new Matrix(rows,1);
			Matrix q = new Matrix(4, 1); //track parameters, for plotting kinematic dependence.  Not used in KFA.  
			if (track.getHelix() == null) {
				track.setHelix(createHelixFromRay(track.getRay()));
			}
			
			q.set(0, 0, track.getHelix().getDCA());
			q.set(1, 0, track.getHelix().getPhiAtDCA());
			q.set(2, 0, track.getHelix().getZ0());
			q.set(3, 0, track.getHelix().getTanDip());
			
			if(debug) {
				System.out.println("track parameters");
				q.print(4, 4);
			}
			
			int i = 0;
			boolean useNewFillMatrices = true;
			if(!curvedTracks) { 
				for(Cross cross : track) {
					if(useNewFillMatrices) {
						if(cross.getDetector() == DetectorType.BST){
							Cluster cl1 = cross.getCluster1();
							boolean ok = fillMatricesNew(i,ray,cl1,A,B,V,m,c,I,debug,false);
							i++;
							if(!ok) { //reject track if there's a cluster with really bad values.
								if(debug) System.out.println("rejecting track due to problem in an SVT layer");
								continue tracksLoop;
							}
							Cluster cl2 = cross.getCluster2();
							ok = fillMatricesNew(i,ray,cl2,A,B,V,m,c,I,debug,false);
							i++;
							if(!ok) { //reject track if there's a cluster with really bad values.
								if(debug) System.out.println("rejecting track due to problem in an SVT layer");
								continue tracksLoop;
							}
						} else {
							Cluster cl1 = cross.getCluster1();
							boolean ok = true;
							if(cl1.getType() == BMTType.Z || !skipBMTC){
							     ok = fillMatricesNew(i,ray,cl1,A,B,V,m,c,I, this.debug, false);
							}
							i++;
							if(!ok) { //reject track if there's a cluster with really bad values.
								if(debug) System.out.println("rejecting track due to problem in a BMT"+ cl1.getType().name() + " layer");
								continue tracksLoop;
							}
							//}
						}
						continue;
					}
					
	
				}
				if(!isCosmics && includeBeamspot) {
					//fillMatricesBeamspot(i, ray, A,B,V,m,c,I, reader.getXbeam(), reader.getYbeam());
					
					
					//pseudo cluster for the beamspot
					Cluster cl1 = new Cluster(null, null, 0, 0, 0);
					cl1.setLine(new Line3D(reader.getXbeam(),reader.getYbeam(),-100, reader.getXbeam(),reader.getYbeam(),100));
					
					Vector3D n = ray.getDirVec();
					Vector3D l = new Vector3D(0,0,1);
					cl1.setN(n);
					cl1.setL(l);
					cl1.setS(n.cross(l));
					cl1.setResolution(0.6);
					
					fillMatricesNew(i, ray, cl1, A,B,V,m,c,I, this.debug, true);
					
					
				}
			} else { 
				Helix helix = track.getHelix();
				//curved tracks
				for(Cross cross : track) {
					if(useNewFillMatrices) {
						if(cross.getDetector() == DetectorType.BST){
							Cluster cl1 = cross.getCluster1();
							boolean ok = fillMatricesNew(i,helix,cl1,A,B,V,m,c,I,debug,false);
							i++;
							if(!ok) { //reject track if there's a cluster with really bad values.
								if(debug) System.out.println("rejecting track due to problem in an SVT layer");
								continue tracksLoop;
							}
							Cluster cl2 = cross.getCluster2();
							ok = fillMatricesNew(i,helix,cl2,A,B,V,m,c,I,debug,false);
							i++;
							if(!ok) { //reject track if there's a cluster with really bad values.
								if(debug) System.out.println("rejecting track due to problem in an SVT layer");
								continue tracksLoop;
							}
						} else {
							Cluster cl1 = cross.getCluster1();
							boolean ok = true;
							if(cl1.getType() == BMTType.Z || !skipBMTC){
							     ok = fillMatricesNew(i,helix,cl1,A,B,V,m,c,I, this.debug, false);
							}
							i++;
							if(!ok) { //reject track if there's a cluster with really bad values.
								if(debug) System.out.println("rejecting track due to problem in a BMT"+ cl1.getType().name() + " layer");
								continue tracksLoop;
							}
							//}
						}
						continue;
					}
					
	
				}
				if(!isCosmics && includeBeamspot) {
					//fillMatricesBeamspot(i, ray, A,B,V,m,c,I, reader.getXbeam(), reader.getYbeam());
					
					
					//pseudo cluster for the beamspot
					Cluster cl1 = new Cluster(null, null, 0, 0, 0);
					cl1.setLine(new Line3D(reader.getXbeam(),reader.getYbeam(),-100, reader.getXbeam(),reader.getYbeam(),100));
					
					Vector3D n = ray.getDirVec();
					Vector3D l = new Vector3D(0,0,1);
					cl1.setN(n);
					cl1.setL(l);
					cl1.setS(n.cross(l));
					cl1.setResolution(0.6);
					
					fillMatricesNew(i, helix, cl1, A,B,V,m,c,I, this.debug, true);
					
					
				}
			}
			As.add(A);
			Bs.add(B);
			Vs.add(V);
			ms.add(m);
			cs.add(c);
			Is.add(I);
			qs.add(q);


			//c.print(7, 4);
			//m.print(7, 4);

			trackIDs.add(track.getId());
		}
		AlignmentBankWriter writer = new AlignmentBankWriter();
		writer.write_Matrix(event, "I", Is);
		writer.write_Matrix(event, "A", As);
		writer.write_Matrix(event, "B", Bs);
		writer.write_Matrix(event, "V", Vs);
		writer.write_Matrix(event, "m", ms);
		writer.write_Matrix(event, "c", cs);
		writer.write_Matrix(event, "q", qs);
		fillMisc(event,runNum,eventNum,trackIDs,As,Bs,Vs,ms,cs,Is);
		//event.show();
		
		//only include events that have tracks that will be used in alignment
		if(As.size() == 0)
			return false;
		return true;

	}
	private Helix createHelixFromRay(Ray ray) {
		Vector3D u = ray.getDirVec();
		Vector3D xref = ray.getRefPoint().toVector3D();
		double phi = Math.atan2(u.y(),u.x());
		Vector3D uT = new Vector3D(Math.cos(phi), Math.sin(phi),0);
		Vector3D mscphi = new Vector3D(-Math.sin(phi), Math.cos(phi),0);
		double cosdip = Math.hypot(u.x(), u.y());
		double d = mscphi.dot(xref);
		double curvature = 0;
		double Z0 = xref.z()-u.z()*xref.dot(uT)/u.dot(uT);
		double tandip = u.z()/Math.hypot(u.x(), u.y());
		return new Helix(d, phi, curvature, Z0, tandip, 0,0);
	}

	int nAlignables;

	private Ray getRay(Helix h) {

		double d = h.getDCA();
		double z = h.getZ0();
		double phi = h.getPhiAtDCA();
		double td = h.getTanDip();
		double cd = 1/Math.hypot(td, 1);
		double sd = td*cd;
		double xb = h.getXb();
		double yb = h.getYb();
		//Vector3D u = new Vector3D(-cd*Math.sin(phi), cd*Math.cos(phi), sd);
		//Point3D x = new Point3D(d*Math.cos(phi),d*Math.sin(phi), z);
		Vector3D u = new Vector3D(cd*Math.cos(phi), cd*Math.sin(phi), sd);


		Point3D x = new Point3D(-d*Math.sin(phi)+xb,d*Math.cos(phi)+yb, z);
		//Point3D x = new Point3D(-d*Math.sin(phi),d*Math.cos(phi), z);

		//System.out.println("xb yb from db" + xb + yb);
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
			Ray ray = track.getRay();
			double intercept = ray.getYXInterc();
			double slope = ray.getYXSlope();
			return Math.abs(intercept)/Math.hypot(1, slope);
		} else return track.getHelix().getDCA();
	}

	private void fillMisc(DataEvent event, int runNum, int eventNum, List<Integer> trackIDs, 
			List<Matrix> As, List<Matrix> Bs, List<Matrix> Vs, List<Matrix> ms, List<Matrix> cs,
			List<Matrix> is) {
		DataBank bank = event.createBank("Align::misc", trackIDs.size());
		for(int i = 0; i<trackIDs.size(); i++) {
			bank.setInt("run", i, runNum);
			bank.setInt("event", i, eventNum);
			Matrix c = cs.get(i), m = ms.get(i), V = Vs.get(i);
			if(V.det() != 0)
				bank.setFloat("chi2", i, (float)(m.minus(c)).transpose().times(V.inverse()).times(m.minus(c)).get(0, 0));
			else {
				System.out.println("Error:  V is singular: ");
				V.print(5, 5);
			}
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
	/*private Vector3d convertVector(Vector3D v) {
		return new Vector3d(v.x(),v.y(),v.z());
	}*/
	/*
	 * converts a Vector3D to a Vector3d.  
	 * These objects are from two different packages.
	 */
	/*private Line3d convertLine(Line3D line) {
		return new Line3d(convertVector(line.origin().toVector3D()), convertVector(line.end().toVector3D()));
	}*/

	boolean useDocaPhiZTandip=true;

	boolean includeBeamspot = false;
	private double minCosIncident = Math.cos(75*Math.PI/180);
	private double spMax = 10;
	private final static double indexBeamspot = 102;
	private boolean fillMatricesBeamspot(int i, Ray ray, Matrix A, Matrix B, Matrix V, Matrix m, Matrix c,
			Matrix I, double xb, double yb){
		// a point along the beam
		Vector3D xref = ray.getRefPoint().toVector3D();
		//System.out.println("xref:  " + xref.toStlString());
		Vector3D u = ray.getDirVec(); 

		Vector3D e = new Vector3D(xb,yb,0);
		Vector3D l = new Vector3D(0,0,1);

		//in this case 
		Vector3D n = new Vector3D(u.x(), u.y(), 0);
		n = n.asUnit();
		Vector3D s = l.cross(n);


		double udotn = u.dot(n);
		if(Math.abs(udotn)<minCosIncident )
			return false;
		double sdotu = s.dot(u);
		Vector3D extrap = xref.clone().add(u.multiply(n.dot(e.clone().sub(xref))/udotn));


		//this should be about equal to the beam width
		double resolution = 0.5;


		V.set(i, i, Math.pow(resolution,2));


		Vector3D sp = s.clone().sub(n.multiply(sdotu/udotn));
		if(sp.mag() > spMax) {  //this can only happen if the angle between the track and the normal is small
			//System.out.println("rejecting track");
			return false;
		}
		int index = nAlignables-1;


		//Use the same reference point for both inner and outer layer of region
		//Vector3d cref = new Vector3d(0,0,0);



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

		//Vector3D dmdr =sp.cross(extrap);
		//dmdr = dmdr.clone().sub(n.cross(u).multiply(n.dot(e.clone().sub(extrap))*sdotu/(udotn*udotn)));
		/*A.set(i, (svtTopBottomSep? i : i/2)*6 + 0, -sp.x);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 1, -sp.y);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 2, -sp.z);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 3, dmdr.x);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 4, dmdr.y);
		A.set(i, (svtTopBottomSep? i : i/2)*6 + 5, dmdr.z);*/

		//System.out.println("i = " + i + "; rows = " + A.getRowDimension() + "; cols = " + + A.getColumnDimension());
		Vector3D dmdr = sp.cross(xref).sub(sp.cross(u).multiply(n.dot(xref.clone().sub(e))/udotn));
		if(orderTx >= 0)
			A.set(i, i*nAlignVars + orderTx, sp.x());
		if(orderTy >= 0)
			A.set(i, i*nAlignVars + orderTy, sp.y());
		if(orderTz >= 0)
			A.set(i, i*nAlignVars + orderTz, sp.z());
		if(orderRx >= 0)
			A.set(i, i*nAlignVars + orderRx, -dmdr.x());
		if(orderRy >= 0)
			A.set(i, i*nAlignVars + orderRy, -dmdr.y());
		if(orderRz >= 0)
			A.set(i, i*nAlignVars + orderRz, -dmdr.z());



		I.set(i, 0, index);

		Vector3D dmdu = sp.multiply(e.clone().sub(xref).dot(n)/udotn);
		if(!this.useDocaPhiZTandip) {
			B.set(i,0, sp.x());
			B.set(i,1, sp.z());
			B.set(i,2, dmdu.x());
			B.set(i,3, dmdu.z());
		} else {

			double phi = Math.atan2(u.y(),u.x());
			Vector3D csphi = new Vector3D(Math.cos(phi), Math.sin(phi),0);
			Vector3D mscphi = new Vector3D(-Math.sin(phi), Math.cos(phi),0);
			double cosdip = Math.hypot(u.x(), u.y());
			double d = mscphi.dot(xref);
			B.set(i, 0, -sp.dot(mscphi));
			B.set(i, 1, -sp.dot(mscphi)*n.dot(e.clone().sub(xref))*cosdip/udotn+sp.dot(csphi)*d);
			B.set(i, 2, -sp.z());
			B.set(i, 3, -sp.z()*n.dot(e.clone().sub(xref))/udotn);


		}
		//dm.set(i,0, s.dot(e.minus(extrap)));

		double ci = s.dot(extrap);
		double mi = s.dot(e);


		//System.out.println(extrap.toStlString());
		//System.out.println(e.toStlString());
		//System.out.println(extrap.minus(e).toStlString());
		//System.out.println(s.toStlString());

		//if(Math.abs(ci-mi)>50)
		//	return false;
		c.set(i,0,ci);
		m.set(i,0,mi);

		return true;
	}

	
	private boolean fillMatricesNew(int i, Helix helix, Cluster cl, Matrix A, Matrix B, Matrix V, Matrix m, 
			Matrix c, Matrix I, boolean debug, boolean isBeamspot) {
		Vector3D u= helix.getTrackDirectionAtRadius(cl.getRadius());
		Point3D xref = helix.getPointAtRadius(cl.getRadius());
		Ray ray = new Ray(xref, u);
		return fillMatricesNew(i, ray, cl, A, B, V, m, 
				c, I, debug, isBeamspot);
	}
	
	/**
	 * generic method that uses any type of cluster.  
	 * @param i
	 * @param ray
	 * @param cl
	 * @param A
	 * @param B
	 * @param V
	 * @param m
	 * @param c
	 * @param I
	 * @param string 
	 * @return
	 */
	private boolean fillMatricesNew(int i, Ray ray, Cluster cl, Matrix A, Matrix B, Matrix V, Matrix m, 
			Matrix c, Matrix I, boolean debug, boolean isBeamspot) {
		int layer = cl.getLayer();
		int sector = cl.getSector();
		//System.out.println("RLS " + region + " " + layer + " " + sector);
		//System.out.println("th" + c.getPhi());
		Vector3D l;
		Vector3D s;
		Vector3D n;

		/*if(detector == DetectorType.BMT && bmtType==BMTType.C) {
			Vector3D u = ray.getdirVec();
			double phi = Math.atan2(u.y(),u.x());
			System.out.println("prelim phi is " + phi + " sector= ="+cl.getSector());
			
		}*/
		
		DetectorType detector = cl.getDetector(); 
		BMTType bmtType = cl.getType();

		if(debug) {
			System.out.println("\n\nNew method " + detector + " layer " + layer + " sector " + sector);
			
		}
		
		


		Vector3D xref = ray.getRefPoint().toVector3D();
		Vector3D u = ray.getDirVec(); 
		
	




		


		//Vector3d e1 = cl.getX
		Vector3D e = null;
		Vector3D extrap = null;
		if(detector == DetectorType.BST || (detector == DetectorType.BMT && bmtType==BMTType.Z) || isBeamspot) {
			l = cl.getL();
			s = cl.getS();
			n = cl.getN();
			/*
			e= new Vector3D(cl.getX1(),cl.getY1(),cl.getZ1());
			e= new Vector3D((cl.getX1()+cl.getX2())/2,
					(cl.getY1()+cl.getY2())/2,
					(cl.getZ1()+cl.getZ2())/2);
					*/
			e = cl.getLine().midpoint().toVector3D();

			double udotn = u.dot(n);
			extrap= xref.clone().add(u.multiply(n.dot(e.clone().sub(xref))/udotn));
		}
		else { // BMTC
			Vector3D a = cl.getArc().normal();
			
			if(debug)
				System.out.println("a: " +a);
			Vector3D cc = cl.getArc().center().toVector3D();
			Vector3D uT = perp(u,a);
			
			Vector3D tmp1 = perp(xref.clone().sub(cc),a);
			
			Vector3D endpoint = cl.getArc().origin().toVector3D();
			
			double R = perp(endpoint.clone().sub(cc),a).mag();
			if(debug) {
				System.out.println("center: " + cc.toStringBrief());
				System.out.println("R:  " + R);
			}
			double AA = uT.mag2();
			
			
			double BB = 2*tmp1.dot(uT);
			double CC = tmp1.mag2()-R*R;
			double lambda_plus = (-BB+Math.sqrt(BB*BB-4*AA*CC))/(2*AA); 
			double lambda_minus = (-BB-Math.sqrt(BB*BB-4*AA*CC))/(2*AA);
			Vector3D extrap_plus = xref.clone().add(u.multiply(lambda_plus));
			Vector3D extrap_minus = xref.clone().add(u.multiply(lambda_minus));
			
			if(debug) {
				System.out.println("extrap is on cylinder:  this should be zero: " + (perp(extrap_plus.clone().sub(cc),a).mag()-R));
			}
			
			/*double phi_mid = cl.get_Arc().origin().midpoint(cl.get_Arc().end()).toVector3D().phi();
			double delta_phi_plus=extrap_plus.phi()-phi_mid;
			while (delta_phi_plus >Math.PI)
				delta_phi_plus-=2*Math.PI;
			while (delta_phi_plus <-Math.PI)
				delta_phi_plus+=2*Math.PI;
			
			double delta_phi_minus=extrap_minus.phi()-phi_mid;
			while (delta_phi_minus >Math.PI)
				delta_phi_minus-=2*Math.PI;
			while (delta_phi_minus <-Math.PI)
				delta_phi_minus+=2*Math.PI;*/
			/*double phi_n = cl.getN().phi();
			System.out.println("phi_recon="+ phi_n);
			System.out.println("phi_+="+ extrap_plus.phi());
			System.out.println("phi_-="+ extrap_minus.phi());
			double delta_phi_plus=extrap_plus.phi()-phi_n;
			while (delta_phi_plus >Math.PI)
				delta_phi_plus-=2*Math.PI;
			while (delta_phi_plus <-Math.PI)
				delta_phi_plus+=2*Math.PI;
			
			double delta_phi_minus=extrap_minus.phi()-phi_n;
			while (delta_phi_minus >Math.PI)
				delta_phi_minus-=2*Math.PI;
			while (delta_phi_minus <-Math.PI)
				delta_phi_minus+=2*Math.PI;
			
			if(Math.abs(delta_phi_plus)<Math.abs(delta_phi_minus))
				extrap = extrap_plus;
			else
				extrap = extrap_minus;*/
			
			
			//choose the extrapolated point that is closer in z to the measured cluster.  
			if(Math.abs(extrap_plus.clone().sub(cc).z())<Math.abs(extrap_minus.clone().sub(cc).z()))
				extrap = extrap_plus;
			else
				extrap = extrap_minus;
			e = extrap.clone().add(endpoint.clone().sub(extrap).projection(a));
			s = a;
			n = perp(extrap.clone().sub(cc),a).asUnit();
			l = s.cross(n);
			//cl.get
			
		}
		
		n = n.sub(l.multiply(n.dot(l))).asUnit();
 		s= s.sub(l.multiply(s.dot(l))).asUnit();
		
		/*if(debug) {
			System.out.println("s: " + s);
			System.out.println("n: " + n);
			System.out.println("l: " + l);
			
			
			System.out.println("s: " + s.toString() + "\nn: "+n.toString() + "\nl: " + l.toString());
			System.out.println("check s.n, s.l, l.n should be zero: " + s.dot(n)
					+ " " + l.dot(s) +  " "+ l.dot(n) );
			System.out.println("check s.s, n.n, l.l should be one: " + s.dot(s)
			+ " " + n.dot(n) +  " " + l.dot(l));
			
			
			Vector3D deltaE = cl.;
			
			System.out.println("(e1-e2):" + deltaE); 
					
			System.out.println("check s.(e1-e2) and n.(e1-e2) should be zero for lines: " + 
			      s.dot(deltaE)+ " " + n.dot(deltaE));
			System.out.println("angle between (e1-e2) and l should equal 0 for lines: " + 
				      l.angle(deltaE));
			if(detector.equals("BMTC")) {
				//System.out.println("cylinder axis: " + cl.getCylAxis());
				System.out.println("track id: " + cl.get_AssociatedTrackID());
				//System.out.println("C: " + cl.getCx() + " " + cl.getCy() + " " + cl.getCz());
				//System.out.println("O: " + cl.getOx() + " " + cl.getOy() + " " + cl.getOz());
				//System.out.println("theta: " + cl.getTheta());
				//System.out.println("angle between cylinder axis and s should be zero: "+cl.getCylAxis().direction().angle(s));
				//System.out.println("dot between cylinder axis and l should be zero: "+cl.getCylAxis().direction().dot(l));
				//System.out.println("dot between cylinder axis and n should be zero: "+cl.getCylAxis().direction().dot(n));
				System.out.println("track interesection: "+cl.getTrakInters());
			}
		}*/
		
		
		double udotn = u.dot(n);
		if(Math.abs(udotn)<minCosIncident) {
			if(debug) {
				System.out.println("rejecting track:  abs(udotn)<" + minCosIncident);
				System.out.println("u = " + u.toString());
				System.out.println("n = " + n.toString());
			}
			return false;	
		}
		double sdotu = s.dot(u);
		
		
		if(debug) {
			//System.out.println("new method");
			System.out.println("e: " + e.toString());
		}
		//Vector3d extrap = xref.plus(u.times(n.dot(e.minus(xref))/udotn));

		if(detector == DetectorType.BMT && bmtType==BMTType.Z && debug) {
			Vector3D diff = xref.clone();
			double check = l.cross(u).dot(diff);
			System.out.println("distance between track and strip, phi,r: " + check + " " + u.phi() + " "+ e.mag());
		}
		

		
		


		//System.out.println(extrap.toStlString());
		double resolution = cl.getResolution();
		//System.out.println("resolution:  " + resolution + "; z=" + extrap.z-);

		V.set(i, i, Math.pow(resolution,2));
		if(debug) {
			System.out.println("resolution "+ resolution);
		}

		Vector3D sp = s.clone().sub(n.multiply(sdotu/udotn));
		if(sp.mag() > spMax) {  //this can only happen if the angle between the track and the normal is small
			if(debug) System.out.println("rejecting track:  sp.magnitude() > "+spMax);
			return false;
		}



		//Vector3D cref = new Vector3D(0,0,0);





		//Vector3D dmdr =sp.cross(extrap).add(n.cross(cref).multiply(sdotu/udotn));
		//dmdr = dmdr.sub(n.cross(u).multiply(n.dot(e.clone().sub(extrap))*sdotu/(udotn*udotn)));
		Vector3D dmdr =sp.cross(xref).sub(sp.cross(u).multiply(n.dot(xref.clone().sub(e))/udotn));
		
		if(orderTx >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderTx, sp.x());
		if(orderTy >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderTy, sp.y());
		if(orderTz >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderTz, sp.z());
		if(orderRx >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderRx, -dmdr.x());
		if(orderRy >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderRy, -dmdr.y());
		if(orderRz >= 0)
			A.set(i, (svtTopBottomSep? i : i/2)*nAlignVars + orderRz, -dmdr.z());


		if(detector == DetectorType.BST)
			I.set(i, 0, getIndexSVT(layer-1,sector-1));
		else if(detector == DetectorType.BMT)
			I.set(i, 0, getIndexBMT(layer-1,sector-1));
		else
			I.set(i, 0, indexBeamspot );
		Vector3D dmdu = sp.multiply(e.clone().sub(xref).dot(n)/udotn);
		if(!this.useDocaPhiZTandip) {
			B.set(i,0, -sp.x());
			B.set(i,1, -sp.z());
			B.set(i,2, -dmdu.x());
			B.set(i,3, -dmdu.z());
		} else {

			double phi = Math.atan2(u.y(),u.x());
			/*if(detector == DetectorType.BMT && bmtType==BMTType.C) {
				System.out.println("phi is " + phi);
			}*/
			Vector3D csphi = new Vector3D(Math.cos(phi), Math.sin(phi),0);
			Vector3D mscphi = new Vector3D(-Math.sin(phi), Math.cos(phi),0);
			double cosdip = Math.hypot(u.x(), u.y());
			double d = mscphi.dot(xref);
			B.set(i, 0, -sp.dot(mscphi));
			B.set(i, 1, -sp.dot(mscphi)*n.dot(e.clone().sub(xref))*cosdip/udotn+sp.dot(csphi)*d);
			B.set(i, 2, -sp.z());
			B.set(i, 3, -sp.z()*n.dot(e.clone().sub(xref))/udotn);


		}
		//dm.set(i,0, s.dot(e.minus(extrap)));

		double ci = s.dot(extrap);
		double mi = s.dot(e);
		
		c.set(i,0,ci);
		m.set(i,0,mi);
		if(debug) {
			System.out.println("n.(e-xref): "+ n.dot(e.clone().sub(xref)));
			System.out.println("u.n: "+ udotn);
			System.out.println("s: " + s.toString());
			System.out.println("sp: " + sp.toString());
			System.out.println("extrap: " + extrap.toString());
			System.out.println("n: " + n.toString());
			System.out.println("e: " + e.toString());
			System.out.println("xref: " + xref.toString());
			System.out.println("u: " + u.toString());
			System.out.println("m: " + mi);
			System.out.println("c: " + ci);
		}
		if(Math.abs(ci-mi)>maxResidualCutSVT && detector == DetectorType.BST || 
				Math.abs(ci-mi)>maxResidualCutBMTZ && detector == DetectorType.BMT && bmtType==BMTType.Z ||
				Math.abs(ci-mi)>maxResidualCutBMTC && detector == DetectorType.BMT && bmtType==BMTType.C) {
			if(debug) System.out.println("rejecting track:  Math.abs(ci-mi)>maxResidualCut");
			return false;
		}
		return true;

	}
	Vector3D perp(Vector3D v, Vector3D a) {
		return v.clone().sub(a.multiply(v.dot(a)));
	}

	/*
	//returns false if there's a problem
	private boolean fillMatricesSVT(int i, Ray ray, Cluster cl, Matrix A, Matrix B, Matrix V, Matrix m, Matrix c, Matrix I) {
		int region = cl.getRegion();
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

		Vector3d xref = convertVector(ray.getRefPoint().toVector3D());
		Vector3d u = convertVector(ray.getDirVec()); 
		Vector3d n = l.cross(s);
		double udotn = u.dot(n);
		if(Math.abs(udotn)<0.01) {
			if(debug) System.out.println("rejecting track:  abs(udotn)<0.01");
			return false;	
		}
		double sdotu = s.dot(u);
		Vector3d extrap = xref.plus(u.times(n.dot(e.minus(xref))/udotn));


		//System.out.println(extrap.toStlString());
		double resolution = cl.getResolutionAlongZ(extrap.z-SVTConstants.Z0ACTIVE[(layer-1)/2], SVTGeom);
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



		

		Vector3d dmdr =sp.cross(extrap).plus(n.cross(cref).times(sdotu/udotn));
		dmdr = dmdr.minus(n.cross(u).times(n.dot(e.minus(extrap))*sdotu/(udotn*udotn)));
		
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
		if(Math.abs(ci-mi)>maxResidualCutSVT) {
			if(debug) System.out.println("rejecting track:  Math.abs(ci-mi)>maxResidualCut");
			return false;
		}
		c.set(i,0,ci);
		m.set(i,0,mi);

		System.out.println("\nold method");
		System.out.println("n.(e-xref): "+ n.dot(e.minus(xref)));
		System.out.println("u.n: "+ udotn);
		System.out.println("s: " + s.toString());
		System.out.println("sp: " + sp.toString());
		System.out.println("extrap: " + extrap.toString());
		System.out.println("n: " + n.toString());
		System.out.println("e: " + e.toString());
		System.out.println("xref: " + xref.toString());
		System.out.println("u: " + u.toString());


		return true;

	}*/
	boolean debug = false;
	private int minClustersSVT = 0;
	private int minClustersBMTC = 0;
	private int minClustersBMTZ = 0;
	//private SVTGeometry SVTGeom;

	/*
	private boolean fillMatricesBMTZ(int i, Ray ray, Cluster cl, Matrix A, Matrix B, Matrix V, Matrix m, Matrix c,
			Matrix I) {
		int region = cl.getRegion();
		int layer = cl.get_Layer();
		int sector = cl.get_Sector();
		double centroid = cl.get_Centroid();
		//Z layer
		//if(centroid == org.jlab.rec.cvt.bmt.Constants.getCRZNSTRIPS()[region-1])
		//	centroid = org.jlab.rec.cvt.bmt.Constants.getCRZNSTRIPS()[region-1]-.001;
		Line3d line1 = convertLine(BMTGeom.getLCZstrip(region, sector, (int)Math.floor(centroid), null));
		Line3d line2 = convertLine(BMTGeom.getLCZstrip(region, sector, (int)Math.floor(centroid)+1, null)); 



		Vector3d l = line1.diff().normalized();//.times(1-(centroid%1)).add(line2.diff().normalized().times((centroid%1))).normalized();

		Vector3d e1 = line1.origin();
		Vector3d e2 = line2.origin();
		Vector3d e = e1.times(1-(centroid%1)).add(e2.times((centroid%1)));
		Vector3d s = e2.minus(e1);
		s = s.minus(l.times(s.dot(l))).normalized();


		Vector3d xref = convertVector(ray.getRefPoint().toVector3D());
		Vector3d u = convertVector(ray.getDirVec()); 
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




		Vector3d dmdr =sp.cross(extrap).plus(n.cross(cref).times(sdotu/udotn));
		dmdr = dmdr.minus(n.cross(u).times(n.dot(e.minus(extrap))*sdotu/(udotn*udotn)));
		
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
		
		c.set(i, 0, ci);

		//Vector3d extrap = xref.plus(u.times(n.dot(e.minus(xref))/udotn));

		System.out.println("\nold method");
		System.out.println("n.(e-xref): "+ n.dot(e.minus(xref)));
		System.out.println("u.n: "+ udotn);
		System.out.println("s: " + s.toString());
		System.out.println("sp: " + sp.toString());
		System.out.println("extrap: " + extrap.toString());
		System.out.println("n: " + n.toString());
		System.out.println("e: " + e.toString());
		System.out.println("xref: " + xref.toString());
		System.out.println("u: " + u.toString());
		System.out.println("m: " + mi);
		System.out.println("c: " + ci);
		if(Math.abs(ci-mi)>maxResidualCutBMTZ)
			return false;

		return true;

	}
	private boolean fillMatricesBMTC(int i, Ray ray, Cluster cl, Matrix A, Matrix B, Matrix V, Matrix m, Matrix c,
			Matrix I) {
		
		System.out.println("\nOld method BMTC");
		int region = cl.getRegion();
		int layer = cl.get_Layer();
		int sector = cl.get_Sector();
		//System.out.println(region+" "+layer +" "+sector);
		double centroid = cl.get_Centroid();
		if(centroid == org.jlab.rec.cvt.bmt.Constants.getCRCNSTRIPS()[region-1])
			centroid = org.jlab.rec.cvt.bmt.Constants.getCRCNSTRIPS()[region-1]-.001;

		Vector3d xref = convertVector(ray.getRefPoint().toVector3D());
		Vector3d u = convertVector(ray.getDirVec()); 

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
		if(debug) {
			System.out.println("m: " + mi);
			System.out.println("c: " + ci);
		}
		
		if(Math.abs(ci-mi)>maxResidualCutBMTC)
			return false;
		c.set(i,0,ci);
		m.set(i,0,mi);

		
		
		return true;
	}*/

	/*private Vector3d midpoint(Arc3D arc1) {

		return convertVector(arc1.point(arc1.theta()/2).toVector3D());
	}*/

	/*private int getIndex(Cluster c) {
		int layer = c.get_Layer()-1;
		int sector = c.get_Sector()-1;
		int index = -1;
		if(c.get_Detector() == DetectorType.BST) {
			int region = layer/2;
			if (region == 0)
				index = sector;
			else if (region == 1)
				index =  SVTGeometry.NSECTORS[0] + sector;
			else if (region == 2)
				index =SVTGeometry.NSECTORS[0] +
						SVTGeometry.NSECTORS[2] + sector;
			if(svtTopBottomSep && layer%2==1) {
				index += 42;
			}
		} else if (c.get_Detector() == DetectorType.BMT) {
			index =  84+layer*3+sector;
		}

		return index;
	}*/

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
			index =  SVTGeometry.NSECTORS[0] + sect;
		else if (region == 2)
			index = SVTGeometry.NSECTORS[0] +
					SVTGeometry.NSECTORS[2] + sect;
		if(svtTopBottomSep && layer%2==1) {
			index += 42;
		}
		return index;

	}

	/*private Vector3d getModuleReferencePoint(int sector, int layer) {
		//return SVTAlignmentFactory.getIdealFiducialCenter((layer-1)/2, sector-1);
		return new Vector3d(0,0,0);
	}*/

	@Override
	public boolean init() {
		if(this.getEngineConfiguration() == null || "null".equals(this.getEngineConfiguration())) {
			return true; //prevents init from being run twice.
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
		
		String skipBMTC = this.getEngineConfigString("skipBMTC");

		if (skipBMTC!=null) {
			System.out.println("["+this.getName()+"] skip BMTC? "+skipBMTC+" config chosen based on yaml");
			this.isBMTonly= Boolean.valueOf(skipBMTC);
		}

		
		
		variationName = Optional.ofNullable(this.getEngineConfigString("variation")).orElse("default");
		System.out.println(" CVT YAML VARIATION NAME + "+variationName);

		System.out.println("SVT LOADING WITH VARIATION "+variationName);
		/*DatabaseConstantProvider cp = new DatabaseConstantProvider(11, variationName);
		//cp = new HackConstantsProvider(cp);
		cp = SVTConstants.connect( cp );
		cp.disconnect();  
		CCDBConstantsLoader.Load(new DatabaseConstantProvider(11, variationName));
		xb = org.jlab.rec.cvt.Constants.getXb();
		yb = org.jlab.rec.cvt.Constants.getYb();*/
		//System.out.println("Check SVT Geom lay1 sec1:  " + Arrays.toString(SVTConstants.getLayerSectorAlignmentData()[0][0]));
		//System.out.println("Check SVT Geom lay1 sec1:  " + Arrays.toString(SVTConstants.getLayerSectorAlignmentData()[0][1]));
		//SVTStripFactory svtFac = new SVTStripFactory(cp, true);
		//SVTGeom = new SVTGeometry(svtFac);

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
			System.out.println("["+this.getName()+"] run with SVT top and bottom as separate modules (default) ");
			this.svtTopBottomSep = true;
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
		
		String cosmics = this.getEngineConfigString("cosmics");

		if(cosmics != null) {
			System.out.println("["+this.getName()+"] use cosmics bank instead of tracks bank? "+ cosmics );
			this.isCosmics = Boolean.parseBoolean(cosmics);
		}
		else {
			System.out.println("["+this.getName()+"] using tracks bank (default)");
			this.isCosmics = false;
		}


		String maxDocaCut = this.getEngineConfigString("maxDocaCut");

		if(maxDocaCut != null) {
			System.out.println("["+this.getName()+"] max doca cut "+ maxDocaCut + " mm");
			this.maxDocaCut = Double.parseDouble(maxDocaCut);
		}
		else {
			if (isCosmics) {
				System.out.println("["+this.getName()+"] no max doca cut set (default for cosmics)");
				this.maxDocaCut = Double.MAX_VALUE;
			} else {
				System.out.println("["+this.getName()+"]  doca cut set to 10 mm (default for field-off tracks)");
				this.maxDocaCut = 10;
			}
		}

		
		String maxResidual = this.getEngineConfigString("maxResidual");

		if (maxResidual!=null) {
			System.out.println("["+this.getName()+"] run with cut on maximum residual "+maxResidual+" config chosen based on yaml");
			this.maxResidualCutBMTZ = this.maxResidualCutBMTC = this.maxResidualCutSVT =  Double.valueOf(maxResidual);
		}

		if (maxResidual==null) {
			System.out.println("["+this.getName()+"] run with maximum residual cut setting default = none");
			this.maxResidualCutBMTC = Double.MAX_VALUE;
			this.maxResidualCutBMTZ = Double.MAX_VALUE;
			this.maxResidualCutSVT = Double.MAX_VALUE;
		}
		
		maxResidual = this.getEngineConfigString("maxResidualBMTZ");

		if (maxResidual!=null) {
			System.out.println("["+this.getName()+"] run with cut on maximum BMTZ residual "+maxResidual+" config chosen based on yaml");
			this.maxResidualCutBMTZ =  Double.valueOf(maxResidual);
		}

		maxResidual = this.getEngineConfigString("maxResidualBMTC");

		if (maxResidual!=null) {
			System.out.println("["+this.getName()+"] run with cut on maximum BMTC residual "+maxResidual+" config chosen based on yaml");
			this.maxResidualCutBMTC =  Double.valueOf(maxResidual);
		}

		maxResidual = this.getEngineConfigString("maxResidualSVT");

		if (maxResidual!=null) {
			System.out.println("["+this.getName()+"] run with cut on maximum SVT residual "+maxResidual+" config chosen based on yaml");
			this.maxResidualCutSVT =  Double.valueOf(maxResidual);
		}
		
		String minClusters = this.getEngineConfigString("minClustersSVT");

		if (minClusters!=null) {
			System.out.println("["+this.getName()+"] run with cut on minimum SVT clusters "+minClusters+" config chosen based on yaml");
			this.minClustersSVT  =  Integer.valueOf(minClusters);
		}

		minClusters = this.getEngineConfigString("minClustersBMTZ");

		if (minClusters!=null) {
			System.out.println("["+this.getName()+"] run with cut on minimum BMTZ clusters "+minClusters+" config chosen based on yaml");
			this.minClustersBMTZ =  Integer.valueOf(minClusters);
		}
		
		minClusters = this.getEngineConfigString("minClustersBMTC");

		if (minClusters!=null) {
			System.out.println("["+this.getName()+"] run with cut on minimum BMTZ clusters "+minClusters+" config chosen based on yaml");
			this.minClustersBMTC =  Integer.valueOf(minClusters);
		}
		
		/*for(int layer = 0; layer<6; layer++)
		{
			Line3d line = SVTGeometry.getStrip(layer, 0, 0);
			System.out.println("debug. Layer" + layer + " (" + line.origin().x + ", "+ line.origin().y + ", "+ line.origin().z+"), "
					+ " (" + line.end().x + ", "+ line.end().y + ", "+ line.end().z+"), ");
		}*/

		String useBeamspotStr = this.getEngineConfigString("useBeamspot");

		if (useBeamspotStr!=null) {
			System.out.println("["+this.getName()+"] treat beamspot as an additional measurement "+useBeamspotStr+" config chosen based on yaml");
			this.includeBeamspot =  Boolean.valueOf(useBeamspotStr);
		} else{
			System.out.println("["+this.getName()+"] treat beamspot as an additional measurement false [default]");
			
		}
		
		this.nAlignables = ((this.svtTopBottomSep ? 2*42 : 42) + (this.isSVTonly ? 0: 18) + (includeBeamspot? 1 : 0));


		String debug = this.getEngineConfigString("debug");

		if (debug!=null) {
			System.out.println("["+this.getName()+"] debug "+debug+" config chosen based on yaml");
			this.debug =  Boolean.parseBoolean(debug);
		} else {
			System.out.println("["+this.getName()+"] debug false; config chosen based on yaml");
			this.debug =  false;
		}
		
		String curvedTracks=this.getEngineConfigString("curvedTracks");
		if (curvedTracks!=null) {
			System.out.println("["+this.getName()+"] curvedTracks "+curvedTracks+" config chosen based on yaml");
			this.curvedTracks =  Boolean.parseBoolean(curvedTracks);
		} else {
			System.out.println("["+this.getName()+"] curvedTracks false; config chosen based on yaml");
			this.curvedTracks =  false;
		}
		//MagneticFields.getInstance().getSolenoid().setScaleFactor(1e-7);

		return true;
	}

	//double xb, yb;

	double maxResidualCutSVT;
	double maxResidualCutBMTC;
	double maxResidualCutBMTZ;

	double maxDocaCut;



	private void setAlignVars(String alignVars) {
		orderTx = -1;
		orderTy = -1;
		orderTz = -1;
		orderRx = -1;
		orderRy = -1;
		orderRz = -1;
		if(alignVars.length() >2 && !alignVars.contains(" ")) {
			for(int i = 0;i<alignVars.length()/2; i++) {
				String s = alignVars.substring(2*i,2*i+2);
				if(s.equals("Tx")) {
					orderTx = i; 
				} else if(s.equals("Ty")) {
					orderTy = i;
				} else if(s.equals("Tz")) {
					orderTz = i; 
				} else if(s.equals("Rx")) {
					orderRx = i; 
				} else if(s.equals("Ry")) {
					orderRy = i; 
				} else if(s.equals("Rz")) {
					orderRz = i; 
				}
				nAlignVars = i+1;
			}
			System.out.println(nAlignVars + " alignment variables requested");
			//System.exit(0);
			return;
		}
		//old version
		String split[] = alignVars.split("[ \t]+");
		int i = 0;
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
	private boolean curvedTracks = false;

}
