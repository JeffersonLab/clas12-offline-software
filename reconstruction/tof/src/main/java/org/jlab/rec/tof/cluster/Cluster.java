package org.jlab.rec.tof.cluster;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.jlab.geom.prim.Point3D;
import org.jlab.rec.ftof.Constants;
import org.jlab.rec.tof.hit.AHit;

/**
 *
 * @author ziegler
 *
 */
public class Cluster extends ArrayList<AHit> implements Comparable<Cluster> {

    /**
     *
     */
    private static final long serialVersionUID = 8662188140594350690L;

    private int _Id;
    private int _Panel;
    private int _Sector;

    private double _y_loc; // y in the local cluster coordinate system
    private double _x; // the cluster energy-weighted x global coordinate
    private double _y; // the cluster energy-weighted y global coordinate
    private double _z; // the cluster energy-weighted z global coordinate

    private double[] _xTrk; // track x global coordinate for each hit in the
    // cluster that the track intersects
    private double[] _yTrk; // track y global coordinate for each hit in the
    // cluster that the track intersects
    private double[] _zTrk; // track z global coordinate for each hit in the
    // cluster that the track intersects

    private double _Energy; // the total energy of the cluster

    private double _t; // the cluster time
    private double[] _tCorr; // corrected time using 3 algorithms to compute
    // deltaR

    private double _y_locUnc; // uncertainty in y in the local cluster
    // coordinate system
    private double _EnergyUnc; // uncertainty in total energy of the cluster
    private double _tUn; // uncertainty in cluster time

    private String _StatusWord; // a status word for each hit: 1111 - fully
    
    private double _pathLengthThruBar; // total pathlength of the track throught the bars of the cluster
    
    // functioning, 0111-noADC[L,U],
    // 1011-noTDC[L,U], 1101-noADC[R,D],
    // 1110-noTDC[R,D]; the cluster word is the
    // status of each hit in the cluster

    public Cluster(int sector, int panel, int id) {
        _Id = id;
        _Panel = panel;
        _Sector = sector;
    }

    public int get_Id() {
        return _Id;
    }

    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    public int get_Panel() {
        return _Panel;
    }

    public void set_Panel(int _Panel) {
        this._Panel = _Panel;
    }

    public int get_Sector() {
        return _Sector;
    }

    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    public double get_y_loc() {
        return _y_loc;
    }

    public void set_y_loc(double _y_loc) {
        this._y_loc = _y_loc;
    }

    public double get_x() {
        return _x;
    }

    public void set_x(double _x) {
        this._x = _x;
    }

    public double get_y() {
        return _y;
    }

    public void set_y(double _y) {
        this._y = _y;
    }

    public double get_z() {
        return _z;
    }

    public void set_z(double _z) {
        this._z = _z;
    }

    public double[] get_xTrk() {
        return _xTrk;
    }

    public void set_xTrk(double[] _xTrk) {
        this._xTrk = _xTrk;
    }

    public double[] get_yTrk() {
        return _yTrk;
    }

    public void set_yTrk(double[] _yTrk) {
        this._yTrk = _yTrk;
    }

    public double[] get_zTrk() {
        return _zTrk;
    }

    public void set_zTrk(double[] _zTrk) {
        this._zTrk = _zTrk;
    }

    public double get_Energy() {
        return _Energy;
    }

    public void set_Energy(double _Energy) {
        this._Energy = _Energy;
    }

    public double get_t() {
        return _t;
    }

    public void set_t(double _t) {
        this._t = _t;
    }

    public double[] get_tCorr() {
        return _tCorr;
    }

    public void set_tCorr(double[] _tCorr) {
        this._tCorr = _tCorr;
    }

    public double get_y_locUnc() {
        return _y_locUnc;
    }

    public void set_y_locUnc(double _y_locUnc) {
        this._y_locUnc = _y_locUnc;
    }

    public double get_EnergyUnc() {
        return _EnergyUnc;
    }

    public void set_EnergyUnc(double _EnergyUnc) {
        this._EnergyUnc = _EnergyUnc;
    }

    public double get_tUnc() {
        return _tUn;
    }

    public void set_tUnc(double _tUn) {
        this._tUn = _tUn;
    }

    public double get_PathLengthThruBar() {
        return _pathLengthThruBar;
    }

    public void set_PathLengthThruBar(double _pathLengthThruBar) {
        this._pathLengthThruBar = _pathLengthThruBar;
    }

    /**
     *
     * @return the energy-weighted strip number
     */
    public void calc_Centroids() {

        if (this.size() != 0) {

            double CentX = 0; // Cluster x position
            double CentY = 0; // Cluster y position
            double CentZ = 0; // Cluster z position
            double CentYLoc = 0; // Cluster local y coordinate
            double CentT = 0; // Cluster time

            double totEn = 0; // total cluster energy of the cluster
            double Norm = this.size(); // normalization
            double averageX = 0; // average x
            double weightedY = 0; // average y
            double averageZ = 0; // average z
            double weightedYloc = 0; // weighted local y
            double weightedT = 0; // weighted time
            double pathThroughCluster = 0.; // pathlength through the cluster bars

            double errESq = 0; // contribution factor to uncertainty in E
            double errYlocSq = 0; // contribution factor to uncertainty in local
            // y
            double errTSq = 0; // contribution factor to uncertainty in time

            String statusWord = ""; // the status word
            int StWordBt1 = 0;
            int StWordBt2 = 0;
            int StWordBt3 = 0;
            int StWordBt4 = 0;

            for (AHit thehit : this) {

                StWordBt1 += Character.getNumericValue(thehit.get_StatusWord()
                        .charAt(0));
                StWordBt2 += Character.getNumericValue(thehit.get_StatusWord()
                        .charAt(1));
                StWordBt3 += Character.getNumericValue(thehit.get_StatusWord()
                        .charAt(2));
                StWordBt4 += Character.getNumericValue(thehit.get_StatusWord()
                        .charAt(3));
                // Centroid terms in global coordinate system. The values are
                // discretized in x and z since the measured values in the local
                // frame are the center of the bar in local x and the position
                // along the bar in local y
                double E = thehit.get_Energy();
                Point3D X = thehit.get_Position();
                double x = X.x();
                double y = X.y();
                double z = X.z();
                double yl = thehit.get_y();

                totEn += E;
                averageX += x;
                weightedY += y * E;
                averageZ += z;
                weightedYloc += yl * E;

                double t = thehit.get_t();
                weightedT += t * E;

                // uncertainties
                errESq += thehit.get_EnergyUnc() * thehit.get_EnergyUnc();

                errYlocSq += E * thehit.get_yUnc() * E * thehit.get_yUnc()
                        + thehit.get_y() * thehit.get_EnergyUnc()
                        * thehit.get_y() * thehit.get_EnergyUnc();

                errTSq += E * thehit.get_tUnc() * E * thehit.get_tUnc()
                        + thehit.get_t() * thehit.get_EnergyUnc()
                        * thehit.get_t() * thehit.get_EnergyUnc();
                
                pathThroughCluster += thehit.get_TrkPathLenThruBar();

            }

            CentYLoc = weightedYloc / totEn;
            CentX = averageX / Norm;
            CentY = weightedY / totEn;
            CentZ = averageZ / Norm;
            CentT = weightedT / totEn;
            StWordBt1 /= Norm;
            StWordBt2 /= Norm;
            StWordBt3 /= Norm;
            StWordBt4 /= Norm;

            statusWord = (StWordBt1 + "" + StWordBt2 + "" + StWordBt3 + "" + StWordBt4);

            this.set_y_loc(CentYLoc);
            this.set_x(CentX);
            this.set_y(CentY);
            this.set_z(CentZ);
            this.set_t(CentT);
            this.set_Energy(totEn);
            this.set_EnergyUnc(Math.sqrt(errESq));
            this.set_y_locUnc(Math.sqrt(errYlocSq) / totEn);
            this.set_tUnc(Math.sqrt(errTSq) / totEn);
            this.set_PathLengthThruBar(pathThroughCluster);
            this.set_StatusWord(statusWord);

        }

    }

    public String get_StatusWord() {
        return _StatusWord;
    }

    public void set_StatusWord(String _StatusWord) {
        this._StatusWord = _StatusWord;
    }

    public void printInfo() {
        DecimalFormat form = new DecimalFormat("#.##");
        String s = "Cluster in " + " Sector " + this.get_Sector() + " Panel "
                + this.get_Panel() + " ID " + this._Id + " : \n" + " t =  "
                + form.format(_t) + " Energy =  " + form.format(_Energy)
                + " xPos =  " + form.format(_x) + " yPos =  " + form.format(_y)
                + " zPos =  " + form.format(_z);

        System.out.println(s);
    }

    public int[] indexesClusHitsMatchedToTrk;

    public void matchToTrack() {
        double xTrk = 0;
        double yTrk = 0;
        double zTrk = 0;
        int id =0;
        int[] iClusHitsMatchedToTrk = new int[this.size()];
        int totNbMatches = 0;
        if(this.size()<1 || this.get(0).get_TrkId()<1)
            return;
        id = this.get(0).get_TrkId();
        for (int i = 0; i < this.size(); i++) {
            iClusHitsMatchedToTrk[i] = -1;

            AHit h = this.get(i);
            
            if (h.get_TrkPosition() == null
                    || Double.isNaN(h.get_TrkPosition().x()) || this.get(i).get_TrkId()!=id) {
                continue;
            }
            id = h.get_TrkId();
            xTrk = h.get_TrkPosition().x();
            yTrk = h.get_TrkPosition().y();
            zTrk = h.get_TrkPosition().z();
            
            if (Math.abs(xTrk - this.get_x()) < Constants.TRKMATCHXPAR[this
                    .get_Panel() - 1]
                    && Math.abs(yTrk - this.get_y()) < Constants.TRKMATCHYPAR[this
                    .get_Panel() - 1]
                    && Math.abs(zTrk - this.get_z()) < Constants.TRKMATCHZPAR[this
                    .get_Panel() - 1]) {
                iClusHitsMatchedToTrk[i] = i;

                totNbMatches++;
            }
        }
        if (totNbMatches == 0) {
            return;
        }

        int nbMatches = 0;
        double[] x_Trk = new double[totNbMatches];
        double[] y_Trk = new double[totNbMatches];
        double[] z_Trk = new double[totNbMatches];

        for (int j = 0; j < iClusHitsMatchedToTrk.length; j++) {
            if (iClusHitsMatchedToTrk[j] != -1) {
                x_Trk[nbMatches] = this.get(iClusHitsMatchedToTrk[j])
                        .get_TrkPosition().x();
                y_Trk[nbMatches] = this.get(iClusHitsMatchedToTrk[j])
                        .get_TrkPosition().y();
                z_Trk[nbMatches] = this.get(iClusHitsMatchedToTrk[j])
                        .get_TrkPosition().z();
                
                nbMatches++;
            }

        }
        
        this.set_xTrk(x_Trk);
        this.set_yTrk(y_Trk);
        this.set_zTrk(z_Trk);

    }

    @Override
    public int compareTo(Cluster arg) {

        // Sort by sector, panel, paddle
        int return_val = 0;
        int CompSec = this.get_Sector() < arg.get_Sector() ? -1 : this
                .get_Sector() == arg.get_Sector() ? 0 : 1;
        int CompPan = this.get_Panel() < arg.get_Panel() ? -1 : this
                .get_Panel() == arg.get_Panel() ? 0 : 1;

        return_val = ((CompSec == 0) ? CompPan : CompSec);

        return return_val;

    }
}
