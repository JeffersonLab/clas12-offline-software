package org.jlab.rec.cnd.hit;

import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cnd.constants.CalibrationConstantsLoader;
import org.jlab.rec.cnd.constants.Parameters;
import org.jlab.rec.cvt.trajectory.Helix;

import eu.mihosoft.vrl.v3d.Vector3d;

public class CvtGetHTrack { // this class is used to extract helical tracks from the cvt event bank. 

	public CvtGetHTrack() {
		helices = new ArrayList<CVTTrack>();
	}

	private List<CVTTrack> helices;

	public List<CVTTrack> getHelices() {
		return helices;
	}

	public void setHelices(List<CVTTrack> helices) {
		this.helices = helices;
	}

	public void getCvtHTrack(DataEvent event, CalibrationConstantsLoader ccdb) {

		helices.clear();

		if (event == null) { // check if there is an event
			//System.out.println(" no event");
		}

		if (event.hasBank("CVTRec::Trajectory") == false) {
			// check if there are some cvt tracks in the event
			//System.out.println(" no cvt tracks");
		}

		if (event.hasBank("CVTRec::Trajectory") == true) {

			DataBank bank = event.getBank("CVTRec::Trajectory");

			int nt = bank.rows();   // number of tracks in the cvt event
			CVTTrack trk = new CVTTrack();
			int indexTrack = 0;

			for (int i = 0; i < nt; i++) {


				if(bank.getByte("detector", i)==DetectorType.CND.getDetectorId()) { //assume layer 1 match hit comes first, layer 2 second and layer 3 last
					

					int id       = (bank.getShort("id", i))-1;
					if(id!=indexTrack) {
						trk = new CVTTrack();
						indexTrack = id;
					}
					
					int layer	  = bank.getByte("layer", i);
					double x     = bank.getFloat("x", i);
					double y     = bank.getFloat("y", i);
					double z     = bank.getFloat("z", i);
					double theta = bank.getFloat("theta", i);
					double phi   = bank.getFloat("phi", i);                
					double ux    = Math.sin(theta)*Math.cos(phi);
					double uy    = Math.sin(theta)*Math.sin(phi);
					double uz    = Math.cos(theta);
					double path  = bank.getFloat("path", i);


					trk.set_Id(id);

					double entryradius  = (ccdb.INNERRADIUS[0] + (layer - 1) * ccdb.THICKNESS[0] + (layer - 1) * Parameters.LayerGap)/10.;
					double escaperadius = (ccdb.INNERRADIUS[0] + (layer) * ccdb.THICKNESS[0] + (layer - 1) * Parameters.LayerGap)/10.;

					//find intercept of line defined by interaction point + director cosines with cylinder of radius defined above

					double b = 2*(ux*x+uy*y);
					double a = ux*ux+uy*uy;
					double co = x*x+y*y-((escaperadius*escaperadius));
					double ci = x*x+y*y-((entryradius*entryradius));

					double uo1 = (-b+Math.sqrt(b*b-4*a*co))/(2*a); 
					double ui1 = (-b+Math.sqrt(b*b-4*a*ci))/(2*a); 
					
					if((Double.isNaN(uo1)) ||  (Double.isNaN(ui1)) )continue; //check if the track crosses the paddle. If not then go to the next swimmer intersection
						
					Point3D entryPoint = new Point3D((x+ui1*ux)*10,(y+ui1*uy)*10,(z+ui1*uz)*10);
					Point3D midPoint = new Point3D(x*10,y*10,z*10);
					Point3D exitPoint = new Point3D((x+uo1*ux)*10,(y+uo1*uy)*10,(z+uo1*uz)*10);

					trk._TrkInters.get(layer - 1).add(entryPoint);
					trk._TrkInters.get(layer - 1).add(midPoint);
					trk._TrkInters.get(layer - 1).add(exitPoint);

					trk._TrkLengths.add(path*10);

					helices.add(trk);
					//System.out.println("layer from swimmer "+layer+ " x "+trk._TrkInters.get(layer-1).get(0).x()+ " "+y+" "+z);

					//System.out.println(layer + " "+id +" path in paddle new "+entryPoint.distance(exitPoint)+ " pathlength "+ trk._TrkInters.get(layer-1)+ " id "+id);

				} 
				
				//add 
			}	

		}


		//old code (kept only for quick reference)
/*
		   if (event.hasBank("CVTRec::Tracks") == false) {
			// check if there are some cvt tracks in the event
            //System.out.println(" no cvt tracks");
        }
        if (event.hasBank("CVTRec::Tracks") == true) {

            //System.out.println(" cvt tracks present");
            DataBank bank = event.getBank("CVTRec::Tracks");
            DataBank config = event.getBank("RUN::config");

            int nt = bank.rows();   // number of tracks in the cvt event

			//test (check if cvt bank is here)
            //System.out.println("number of cvt tracks "+nt);
            //bank.show();
            //config.show();
            // retrieve the helix parameters
            for (int i = 0; i < nt; i++) {
                //Matrix m = new Matrix(5,5);
                int trkID = i;
                double dca    = bank.getFloat("d0", i)*10.;
                double phi0   = bank.getFloat("phi0", i);
                double z0     = bank.getFloat("z0", i)*10.;
                double tandip = bank.getFloat("tandip", i);
                int q         = bank.getInt("q", i);
                double pt     = bank.getFloat("pt", i);
                // use the formula R=pt/qB to get the curvature (see cvt reconstruction class Track)/Pt is in Gev
                double rho = (0.000299792458 * q * 5. * -1.) / pt;
				// the max value of B is set to 5.Tesla 
                //double p=bank.getFloat("p", i);

				//test (check the parameters)
                //				bank.show();
                //				System.out.println("p helixi "+bank.getFloat("p", i));
                //				System.out.println("pt "+bank.getFloat("pt", i));
                //				System.out.println(config.getFloat("solenoid",0));
                //				System.out.println(bank.getFloat("tandip", i));
                //				System.out.println((bank.getFloat("cov_d02", i)));
                //				System.out.println("R "+ 1./rho);
				//not used in the code
                //m.set(0,0, bank.getFloat("cov_d02", i));
                //m.set(0,1, bank.getFloat("cov_d0phi0", i));
                //m.set(1,0, bank.getFloat("cov_d0phi0", i));
                //m.set(0,2, bank.getFloat("cov_d0rho", i));
                //m.set(2,0, bank.getFloat("cov_d0rho", i));
                //m.set(1,1, bank.getFloat("cov_phi02", i));
                //m.set(1,2, bank.getFloat("cov_phi0rho", i));
                //m.set(2,1, bank.getFloat("cov_phi0rho", i));
                //m.set(2,2, bank.getFloat("cov_rho2", i));
                //m.set(3,3, bank.getFloat("cov_z02", i));
                //m.set(4,4, bank.getFloat("cov_tandip2", i));
				//Helix helixi = new Helix(dca,phi0,rho,z0,tandip,null); // reconstruct the helix from the cvt track
                //System.out.println("helix proj "+helixi.getPointAtRadius(CalibrationConstantsLoader.INNERRADIUS[0]).toString());
                //Track track= new Track(helixi);
                //track.set_P(p);
                //track.set_Pt(pt);
                //track.set_Q(q);
                //helix.add(track);	
                CVTTrack trk = new CVTTrack();
                trk.set_Helix(new Helix(dca, phi0, rho, z0, tandip, null));
                trk.set_Id(trkID);

				//(VZ)
                //find the points of intersection of the track with the relevant radii at the CND surfaces; these will be used for matching
                // The position of the tracks at the relevant radii is accessed as an ordered List of List:
                // first index 0...2 is layer index
                // second index 0...2 corresponds to entrance, middle and exit of track wrt the counter
                for (int lay = 1; lay <= 3; lay++) {
                    double radius       = ccdb.INNERRADIUS[0] + (lay - 0.5) * ccdb.THICKNESS[0] + (lay - 1) * Parameters.LayerGap;
                    double entryradius  = ccdb.INNERRADIUS[0] + (lay - 1) * ccdb.THICKNESS[0] + (lay - 1) * Parameters.LayerGap;
                    double escaperadius = ccdb.INNERRADIUS[0] + (lay) * ccdb.THICKNESS[0] + (lay - 1) * Parameters.LayerGap;
                    trk._TrkInters.get(lay - 1).add(trk.get_Helix().getPointAtRadius(entryradius));
                    Point3D Xm = trk.get_Helix().getPointAtRadius(radius);
                    trk._TrkInters.get(lay - 1).add(Xm);
                    trk._TrkInters.get(lay - 1).add(trk.get_Helix().getPointAtRadius(escaperadius));
                    double r = Math.sqrt(Xm.x() * Xm.x() + Xm.y() * Xm.y());
                    double par = 1. - ((r * r - dca * dca) * rho * rho) / (2. * (1. + dca * Math.abs(rho)));
                    double pathLengthXY = Math.abs(Math.acos(par) / rho);

                    double pathLength = Math.sqrt((Xm.z() - z0 )*(Xm.z() - z0 )+pathLengthXY*pathLengthXY);
                    trk._TrkLengths.add(pathLength);

    				System.out.println(lay + " "+trkID +"path in paddle old "+trk.get_Helix().getPointAtRadius(entryradius).distance(trk.get_Helix().getPointAtRadius(escaperadius))+ " pathlength "+ trk._TrkInters.get(lay-1)+ " id "+trkID);

                }


               // helices.add(trk);
            }

        }//end
*/
		

	}

	/**
	 * A class to hold the CVT track information relevant for CND analysis
	 *
	 * @author ziegler
	 *
	 */
	public class CVTTrack {

		private Helix _Helix; // not in use since swimmer point is used 
		private int _Id;

		// The position of the tracks at the relevant radii is accessed as an ordered List of List:
		// first index 0...2 is layer index
		// second index 0...2 corresponds to entrance, middle and exit of track wrt the counter
		private List<ArrayList<Point3D>> _TrkInters = new ArrayList<ArrayList<Point3D>>(); // intersection point wrt front middle and end of counter
		private List<Double>             _TrkLengths = new ArrayList<Double>();  //the pathlength of the track from the doca to the beam line to the middle of the CND counter

		public CVTTrack() {
			for (int i = 0; i < 3; i++) {
				_TrkInters.add(new ArrayList<Point3D>());
			}
		}

		public Helix get_Helix() {
			return _Helix;
		}

		public void set_Helix(Helix _Helix) {
			this._Helix = _Helix;
		}

		public int get_Id() {
			return _Id;
		}

		public void set_Id(int _Id) {
			this._Id = _Id;
		}

		public List<ArrayList<Point3D>> get_TrkInters() {
			return _TrkInters;
		}

		public void set_TrkInters(List<ArrayList<Point3D>> _TrkInters) {
			this._TrkInters = _TrkInters;
		}

		public List<Double> get_TrkLengths() {
			return _TrkLengths;
		}

		public void set_TrkLengths(List<Double> _PathLengths) {
			this._TrkLengths = _PathLengths;
		}

	}
}
