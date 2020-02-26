package org.jlab.rec.cnd.hit;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cnd.constants.CalibrationConstantsLoader;
import org.jlab.rec.cnd.constants.Parameters;
import org.jlab.rec.cvt.trajectory.Helix;

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
                double rho = (0.000299792458 * q * 5. * config.getFloat("solenoid", 0)) / pt;
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
                    double pathLength = Math.abs(Math.acos(par) / rho);
                    trk._TrkLengths.add(pathLength);
                }
                helices.add(trk);
            }

        }

    }

    /**
     * A class to hold the CVT track information relevant for CND analysis
     *
     * @author ziegler
     *
     */
    public class CVTTrack {

        private Helix _Helix;
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
