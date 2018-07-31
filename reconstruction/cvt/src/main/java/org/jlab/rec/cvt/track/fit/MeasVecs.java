package org.jlab.rec.cvt.track.fit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.swimtools.Swim;

import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.fit.StateVecs.StateVec;

public class MeasVecs {

    public List<MeasVec> measurements = new ArrayList<MeasVec>();

    public class MeasVec implements Comparable<MeasVec> {

        public double x = Double.NaN; // for BMT
        public double y = Double.NaN; // for BMT
        public double z = Double.NaN; // for BMT
        public double centroid; // for SVT
        public double error;
        public int layer;
        public int sector;
        public int type; // 0=svt; 1=BMT Z-det; 2=BMT C-det
        public int k;

        MeasVec() {
        }

        @Override
        public int compareTo(MeasVec arg) {
            int CompLay = this.layer < arg.layer ? -1 : this.layer == arg.layer ? 0 : 1;
            return CompLay;
        }

    }

        public void setMeasVecs(Seed trkcand, org.jlab.rec.cvt.svt.Geometry sgeo) {
       // for(Cross c : trkcand.get_Crosses() ) { System.out.println(" crosses in seed passed to KF: "+c.printInfo());}System.out.println(" =========");
        measurements = new ArrayList<MeasVec>();
        MeasVec meas0 = new MeasVec();
        meas0.centroid = 0;
        meas0.error = 1;
        meas0.sector = 0;
        meas0.layer = 0;
        measurements.add(meas0); 
        for (int i = 0; i < trkcand.get_Clusters().size(); i++) {
            if(trkcand.get_Clusters().get(i).get_Detector()==0) {
                MeasVec meas = new MeasVec();
                meas.centroid = trkcand.get_Clusters().get(i).get_Centroid();
                meas.type = 0;
                meas.error = trkcand.get_Clusters().get(i).get_CentroidError()*trkcand.get_Clusters().get(i).get_CentroidError()*trkcand.get_Clusters().get(i).size();

                meas.sector = trkcand.get_Clusters().get(i).get_Sector();
                meas.layer = trkcand.get_Clusters().get(i).get_Layer();
                if(i>0 && measurements.get(measurements.size()-1).layer==meas.layer)
                    continue;
                measurements.add(meas);
            }
        }

        // adding the BMT
        for (int c = 0; c < trkcand.get_Crosses().size(); c++) {
            if (trkcand.get_Crosses().get(c).get_Detector().equalsIgnoreCase("BMT")) {

                MeasVec meas = new MeasVec();
                meas.sector = trkcand.get_Crosses().get(c).get_Sector();
                meas.layer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer()+6;
                meas.centroid = trkcand.get_Crosses().get(c).get_Cluster1().get_Centroid();
                if(measurements.size()>0 && measurements.get(measurements.size()-1).layer==meas.layer)
                    continue;
                if (trkcand.get_Crosses().get(c).get_DetectorType().equalsIgnoreCase("Z")) {
                    meas.x = trkcand.get_Crosses().get(c).get_Point().x();
                    meas.y = trkcand.get_Crosses().get(c).get_Point().y();
                    double res = trkcand.get_Crosses().get(c).get_Cluster1().get_PhiErr();
                    meas.error = res * res;
                    meas.type = 1;
                }
                if (trkcand.get_Crosses().get(c).get_DetectorType().equalsIgnoreCase("C")) {
                    meas.z = trkcand.get_Crosses().get(c).get_Point().z();
                    double res = trkcand.get_Crosses().get(c).get_Cluster1().get_ZErr();
                    meas.error = res * res;
                    meas.type = 2;
                }

                measurements.add(meas);
            }
        }
        Collections.sort(measurements);
        
    }

    

    public double h(StateVec stateVec, org.jlab.rec.cvt.svt.Geometry sgeo) {
        if (stateVec == null) {
            return 0;
        }
        if (this.measurements.get(stateVec.k) == null) {
            return 0;
        }
        int sec = this.measurements.get(stateVec.k).sector;
        int lay = this.measurements.get(stateVec.k).layer;

        return sgeo.calcNearestStrip(stateVec.x, stateVec.y, stateVec.z, lay, sec);
    }

    public double hPhi(StateVec stateVec) {
        return Math.atan2(stateVec.y, stateVec.x);
    }

    public double hZ(StateVec stateVec) {
        return stateVec.z;
    }

    public double[] H(StateVec stateVec, StateVecs sv, org.jlab.rec.cvt.svt.Geometry sgeo, 
            org.jlab.rec.cvt.bmt.Geometry bgeo, int type, Swim swimmer) {
        StateVec SVplus = null;// = new StateVec(stateVec.k);
        StateVec SVminus = null;// = new StateVec(stateVec.k);

        double delta_d_rho = 1;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.d_rho = stateVec.d_rho + delta_d_rho / 2.;
        SVminus.d_rho = stateVec.d_rho - delta_d_rho / 2.;

        SVplus = sv.newStateVecAtModule(stateVec.k, SVplus, sgeo, bgeo, type, swimmer);
        SVminus = sv.newStateVecAtModule(stateVec.k, SVminus, sgeo, bgeo, type, swimmer);

        double delta_m_drho = 0;
        if (type == 0) {
            delta_m_drho = (h(SVplus, sgeo) - h(SVminus, sgeo)) / delta_d_rho;
        }
        if (type == 2) {
            delta_m_drho = (hZ(SVplus) - hZ(SVminus)) / delta_d_rho;
        }
        if (type == 1) {
            delta_m_drho = (hPhi(SVplus) - hPhi(SVminus)) / delta_d_rho;
        }

        double delta_d_phi0 = Math.toRadians(0.25);
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.phi0 = stateVec.phi0 + delta_d_phi0 / 2.;
        SVminus.phi0 = stateVec.phi0 - delta_d_phi0 / 2.;

        SVplus = sv.newStateVecAtModule(stateVec.k, SVplus, sgeo, bgeo, type, swimmer);
        SVminus = sv.newStateVecAtModule(stateVec.k, SVminus, sgeo, bgeo, type, swimmer);

        double delta_m_dphi0 = 0;
        if (type == 0) {
            delta_m_dphi0 = (h(SVplus, sgeo) - h(SVminus, sgeo)) / delta_d_phi0;
        }
        if (type == 2) {
            delta_m_dphi0 = (hZ(SVplus) - hZ(SVminus)) / delta_d_phi0;
        }
        if (type == 1) {
            delta_m_dphi0 = (hPhi(SVplus) - hPhi(SVminus)) / delta_d_phi0;
        }

        double delta_d_kappa = 0.01;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.kappa = stateVec.kappa + delta_d_kappa / 2.;
        SVminus.kappa = stateVec.kappa - delta_d_kappa / 2.;

        SVplus = sv.newStateVecAtModule(stateVec.k, SVplus, sgeo, bgeo, type, swimmer);
        SVminus = sv.newStateVecAtModule(stateVec.k, SVminus, sgeo, bgeo, type, swimmer);

        double delta_m_dkappa = 0;
        if (type == 0) {
            delta_m_dkappa = (h(SVplus, sgeo) - h(SVminus, sgeo)) / delta_d_kappa;
        }
        if (type == 2) {
            delta_m_dkappa = (hZ(SVplus) - hZ(SVminus)) / delta_d_kappa;
        }
        if (type == 1) {
            delta_m_dkappa = (hPhi(SVplus) - hPhi(SVminus)) / delta_d_kappa;
        }

        double delta_d_dz = 1;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.dz = stateVec.dz + delta_d_dz / 2.;
        SVminus.dz = stateVec.dz - delta_d_dz / 2.;

        SVplus = sv.newStateVecAtModule(stateVec.k, SVplus, sgeo, bgeo, type, swimmer);
        SVminus = sv.newStateVecAtModule(stateVec.k, SVminus, sgeo, bgeo, type, swimmer);

        double delta_m_dz = 0;
        if (type == 0) {
            delta_m_dz = (h(SVplus, sgeo) - h(SVminus, sgeo)) / delta_d_dz;
        }
        if (type == 2) {
            delta_m_dz = (hZ(SVplus) - hZ(SVminus)) / delta_d_dz;
        }
        if (type == 1) {
            delta_m_dz = (hPhi(SVplus) - hPhi(SVminus)) / delta_d_dz;
        }

        double delta_d_tanL = 0.01;
        SVplus = this.reset(SVplus, stateVec, sv);
        SVminus = this.reset(SVminus, stateVec, sv);

        SVplus.tanL = stateVec.tanL + delta_d_tanL / 2.;
        SVminus.tanL = stateVec.tanL - delta_d_tanL / 2.;

        SVplus = sv.newStateVecAtModule(stateVec.k, SVplus, sgeo, bgeo, type, swimmer);
        SVminus = sv.newStateVecAtModule(stateVec.k, SVminus, sgeo, bgeo, type, swimmer);

        double delta_m_dtanL = 0;
        if (type == 0) {
            delta_m_dtanL = (h(SVplus, sgeo) - h(SVminus, sgeo)) / delta_d_tanL;
        }
        if (type == 2) {
            delta_m_dtanL = (hZ(SVplus) - hZ(SVminus)) / delta_d_tanL;
        }
        if (type == 1) {
            delta_m_dtanL = (hPhi(SVplus) - hPhi(SVminus)) / delta_d_tanL;
        }

        double[] H = new double[]{delta_m_drho, delta_m_dphi0, delta_m_dkappa, delta_m_dz, delta_m_dtanL};

        return H;

    }

    private StateVec reset(StateVec SVplus, StateVec stateVec, StateVecs sv) {
        SVplus = sv.new StateVec(stateVec.k);
        SVplus.d_rho = stateVec.d_rho;
        SVplus.alpha = stateVec.alpha;
        SVplus.phi0 = stateVec.phi0;
        SVplus.kappa = stateVec.kappa;
        SVplus.dz = stateVec.dz;
        SVplus.tanL = stateVec.tanL;
        SVplus.alpha = stateVec.alpha;

        return SVplus;
    }

    public double[] H2(StateVec stateVec, StateVecs sv) {
        double[] H = new double[]{0, 0, 0, 0, 0};
        //System.out.println(" Projecting to meas plane ...........");
        if (stateVec.k > 0) {

            double del_m_del_x = 0;
            double del_m_del_y = 0;
            double del_m_del_z = 0;

            if (!Double.isNaN(this.measurements.get(stateVec.k).x)) {
                double X = this.measurements.get(stateVec.k).x;
                double Y = this.measurements.get(stateVec.k).y;

                del_m_del_x = -Y / (X * X + Y * Y);
                del_m_del_y = X / (X * X + Y * Y);
                del_m_del_z = 0;
            }

            if (!Double.isNaN(this.measurements.get(stateVec.k).z)) {
                del_m_del_z = 1;
                del_m_del_x = 0;
                del_m_del_y = 0;
            }

            //System.out.println("... del_m_del_x = "+del_m_del_x+" del_m_del_y = "+del_m_del_y+" del_m_del_z = "+del_m_del_z);
            // find H
            //==========================================================================//
            // x = x0 + drho * cos(phi0) + alpha/kappa *(cos(phi0) - cos(phi0 + phi) )  //
            // y = y0 + drho * sin(phi0) + alpha/kappa *(sin(phi0) - sin(phi0 + phi) )  //
            // z = z0 + dz  -  alpha/kappa *tanL*phi                                    //
            //==========================================================================//
            // get vars
            double drho = stateVec.d_rho;
            double phi0 = stateVec.phi0;
            double phi = stateVec.phi;
            double alpha = stateVec.alpha;
            double kappa = stateVec.kappa;
            //double dz = stateVec.dz;
            double tanL = stateVec.tanL;

            double delx_deldrho = Math.cos(phi0);
            double dely_deldrho = Math.sin(phi0);
            double delz_deldrho = 0;
            double delx_delphi0 = -drho * Math.sin(phi0) - alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi));
            double dely_delphi0 = drho * Math.cos(phi0) + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi));
            double delz_delphi0 = 0;
            double delx_delkappa = -alpha / (kappa * kappa) * (Math.cos(phi0) - Math.cos(phi0 + phi));
            double dely_delkappa = -alpha / (kappa * kappa) * (Math.sin(phi0) - Math.sin(phi0 + phi));
            double delz_delkappa = alpha / (kappa * kappa) * tanL * phi;
            double delx_deldz = 0;
            double dely_deldz = 0;
            double delz_deldz = 1;
            double delx_deltanL = 0;
            double dely_deltanL = 0;
            double delz_deltanL = -alpha / kappa * phi;
            H = new double[]{del_m_del_x * delx_deldrho + del_m_del_y * dely_deldrho + del_m_del_z * delz_deldrho,
                del_m_del_x * delx_delphi0 + del_m_del_y * dely_delphi0 + del_m_del_z * delz_delphi0,
                del_m_del_x * delx_delkappa + del_m_del_y * dely_delkappa + del_m_del_z * delz_delkappa,
                del_m_del_x * delx_deldz + del_m_del_y * dely_deldz + del_m_del_z * delz_deldz,
                del_m_del_x * delx_deltanL + del_m_del_y * dely_deltanL + del_m_del_z * delz_deltanL

            };
            
        }

        return H;
    }

}
