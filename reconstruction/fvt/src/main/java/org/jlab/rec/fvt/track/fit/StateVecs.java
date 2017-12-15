package org.jlab.rec.fvt.track.fit;

import java.util.HashMap;
import java.util.Map;

import org.jlab.rec.dc.trajectory.DCSwimmer;

import Jama.Matrix;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.fvt.fmt.Constants;
import static org.jlab.rec.fvt.fmt.Constants.FVT_Nstrips;
import org.jlab.rec.fvt.track.Track;

public class StateVecs {

    final double speedLight = 0.002997924580;
    public double[] Z;
   // public List<B> bfieldPoints = new ArrayList<B>();
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();

    private double stepSize = 1.; // step size 
    public StateVec StateVec;
    public CovMat CovMat;
    public Matrix F;
    private final double[] A = new double[2];
    private final double[] dA = new double[4];
    private final float[] bf = new float[3];
    
    private StateVec f(int i, int f, StateVec iVec) {

        dcSwim.SetSwimParameters((int)Math.signum(Z[f] - Z[i]), iVec.x, iVec.y, iVec.z, iVec.tx, iVec.ty, Math.abs(1. / iVec.Q), (int)Math.signum(iVec.Q));
        double[] SwRes = dcSwim.SwimToPlaneLab(Z[f]);
        
        StateVec fVec = new StateVec(f);
        fVec.z = SwRes[2];
        fVec.x = SwRes[0];
        fVec.y = SwRes[1];
        fVec.tx = SwRes[3]/SwRes[5];
        fVec.ty = SwRes[4]/SwRes[5];
        fVec.Q = Math.signum(iVec.Q)/Math.sqrt(SwRes[3]*SwRes[3]+SwRes[4]*SwRes[4]+SwRes[5]*SwRes[5]);
        
        return fVec;

    }
    
    private Matrix F(int i, int f, StateVec stateVec) {
        StateVec SVplus = null;// = new StateVec(stateVec.k);
        StateVec SVminus = null;// = new StateVec(stateVec.k);

        double delta_d_x = 7.449126e-03;
        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        SVplus.x = stateVec.x + delta_d_x / 2.; 
        SVminus.x = stateVec.x - delta_d_x / 2.;

        double delta_y_dx = (f(i, f, SVplus).y - f(i, f, SVminus).y) / delta_d_x;
        double delta_tx_dx = (f(i, f, SVplus).tx - f(i, f, SVminus).tx) / delta_d_x;
        double delta_ty_dx = (f(i, f, SVplus).ty - f(i, f, SVminus).ty) / delta_d_x;
        double delta_Q_dx = (f(i, f, SVplus).Q - f(i, f, SVminus).Q) / delta_d_x;
        
        double delta_d_y = 9.019044e-02;
        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        SVplus.y = stateVec.y + delta_d_y / 2.; 
        SVminus.y = stateVec.y - delta_d_y / 2.;

        double delta_x_dy = (f(i, f, SVplus).x - f(i, f, SVminus).x) / delta_d_y;
        double delta_tx_dy = (f(i, f, SVplus).tx - f(i, f, SVminus).tx) / delta_d_y;
        double delta_ty_dy = (f(i, f, SVplus).ty - f(i, f, SVminus).ty) / delta_d_y;
        double delta_Q_dy = (f(i, f, SVplus).Q - f(i, f, SVminus).Q) / delta_d_y;
        
        double delta_d_tx = 7.159996e-04;
        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        SVplus.tx = stateVec.tx + delta_d_tx / 2.;
        SVminus.tx = stateVec.tx - delta_d_tx / 2.;

        double delta_x_dtx = (f(i, f, SVplus).x - f(i, f, SVminus).x) / delta_d_tx;
        double delta_y_dtx = (f(i, f, SVplus).y - f(i, f, SVminus).y) / delta_d_tx;
        double delta_ty_dtx = (f(i, f, SVplus).ty - f(i, f, SVminus).ty) / delta_d_tx;
        double delta_Q_dtx = (f(i, f, SVplus).Q - f(i, f, SVminus).Q) / delta_d_tx;

        double delta_d_ty = 7.169160e-04;
        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        SVplus.ty = stateVec.ty + delta_d_ty / 2.;
        SVminus.ty = stateVec.ty - delta_d_ty / 2.;

        double delta_x_dty = (f(i, f, SVplus).x - f(i, f, SVminus).x) / delta_d_ty;
        double delta_y_dty = (f(i, f, SVplus).y - f(i, f, SVminus).y) / delta_d_ty;
        double delta_tx_dty = (f(i, f, SVplus).tx - f(i, f, SVminus).tx) / delta_d_ty;
        double delta_Q_dty = (f(i, f, SVplus).Q - f(i, f, SVminus).Q) / delta_d_ty;
        
        double delta_d_Q = 4.809686e-04;
        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        SVplus.Q = stateVec.Q + delta_d_Q / 2.;
        SVminus.Q = stateVec.Q - delta_d_Q / 2.;

        double delta_x_dQ = (f(i, f, SVplus).x - f(i, f, SVminus).x) / delta_d_Q;
        double delta_y_dQ = (f(i, f, SVplus).y - f(i, f, SVminus).y) / delta_d_Q;
        double delta_tx_dQ = (f(i, f, SVplus).tx - f(i, f, SVminus).tx) / delta_d_Q;
        double delta_ty_dQ = (f(i, f, SVplus).ty - f(i, f, SVminus).ty) / delta_d_Q;
       
        double[][] F = new double[][] { {1., delta_x_dy, delta_x_dtx, delta_x_dty, delta_x_dQ},
                                        {delta_y_dx, 1., delta_y_dtx, delta_y_dty, delta_y_dQ},
                                        {delta_tx_dx, delta_tx_dy, 1., delta_tx_dty, delta_tx_dQ},
                                        {delta_ty_dx, delta_ty_dy, delta_ty_dtx, 1., delta_ty_dQ},
                                        {delta_Q_dx, delta_Q_dy, delta_Q_dtx, delta_Q_dty, 1.},
                                        }; 
        
        return new Matrix(F);
    }
    
    private Matrix FTCF(int i, int f, StateVec stateVec, Matrix covMat1) {
        /*
        double[] F = F( i,  f, stateVec);
        double[][] FTCF = new double[][]{
                {F[0] * F[0] *covMat1.get(0, 0), F[0] * F[1] *covMat1.get(0, 1), F[0] * F[2] *covMat1.get(0, 2), F[0] * F[3] *covMat1.get(0, 3), F[0] * F[4] *covMat1.get(0, 4)},
                {F[1] * F[0] *covMat1.get(1, 0), F[1] * F[1] *covMat1.get(1, 1), F[1] * F[2] *covMat1.get(1, 2), F[1] * F[3] *covMat1.get(1, 3), F[1] * F[4] *covMat1.get(1, 4)},
                {F[2] * F[0] *covMat1.get(2, 0), F[2] * F[1] *covMat1.get(2, 1), F[2] * F[2] *covMat1.get(2, 2), F[2] * F[3] *covMat1.get(2, 3), F[2] * F[4] *covMat1.get(2, 4)},
                {F[3] * F[0] *covMat1.get(3, 0), F[3] * F[1] *covMat1.get(3, 1), F[3] * F[2] *covMat1.get(3, 2), F[3] * F[3] *covMat1.get(3, 3), F[3] * F[4] *covMat1.get(3, 4)},
                {F[4] * F[0] *covMat1.get(4, 0), F[4] * F[1] *covMat1.get(4, 1), F[4] * F[2] *covMat1.get(4, 2), F[4] * F[3] *covMat1.get(4, 3), F[4] * F[4] *covMat1.get(4, 4)}
            };
        */
        Matrix F = F( i,  f, stateVec);
       // System.out.println("F "); this.printMatrix(F);
        Matrix covMatrx = F.times(covMat1).times(F.transpose());
        if(f>i)
            this.AddProcessNoise(stateVec, covMatrx);
        return covMatrx;
    }
    
    private void AddProcessNoise(StateVec stateVec, Matrix covMat1) {
        // Q_atPlane	
        double p = Math.abs(1. / stateVec.Q);
        double pz = p / Math.sqrt(1 + stateVec.tx * stateVec.tx + stateVec.ty * stateVec.ty);
        double px = stateVec.tx * pz;
        double py = stateVec.ty * pz;
        double t_ov_X0 = 0;

        if(stateVec.z>Constants.FVT_Z1stlayer) {
            
            double gap = Z[stateVec.k+1]-Z[stateVec.k];
            t_ov_X0 = gap / Constants.get_X0()[9];
            
            double mass = 0.13957018;
            double mass_e = 0.000510998;
            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            double cosEntranceAngle = Math.abs((stateVec.x * px + stateVec.y * py + stateVec.z * pz) / (Math.sqrt(stateVec.x * stateVec.x + stateVec.y * stateVec.y + stateVec.z * stateVec.z) * p));
            //Eloss
            
            double gamma = 1. / Math.sqrt(1 - beta * beta);

            double Wmax = 2. * mass * beta * beta * gamma * gamma / (1. + 2. * (mass_e / mass) * gamma + (mass_e / mass) * (mass_e / mass));
            double I = 0.000000172;

            double logterm = 2. * mass * beta * beta * gamma * gamma * Wmax / (I * I);

            double delta = 0.;
           // double dEdx = 0.0001535 * detMat_Z_ov_A_timesThickn * (Math.log(logterm) - 2 * beta * beta - delta) / (beta * beta); //in GeV/cm
            //System.out.println("  at "+Math.sqrt(iVec.x_atPlane*iVec.x_atPlane+iVec.y_atPlane*iVec.y_atPlane));
            //Eloss +=  Math.abs(dEdx / cosEntranceAngle);

            //
            double pathLength = t_ov_X0 / cosEntranceAngle;

            double sctRMS = (0.0136 / (beta * p)) * Math.sqrt(pathLength) * (1 + 0.038 * Math.log(pathLength)); // Highland-Lynch-Dahl formula
            double cov_txtx = (1 + stateVec.tx * stateVec.tx) * (1 + stateVec.tx * stateVec.tx + stateVec.ty * stateVec.ty) * sctRMS * sctRMS ;
            double cov_tyty = (1 + stateVec.ty * stateVec.ty) * (1 + stateVec.tx * stateVec.tx + stateVec.ty * stateVec.ty) * sctRMS * sctRMS;
            double cov_txty = stateVec.tx * stateVec.ty * (1 + stateVec.tx * stateVec.tx + stateVec.ty * stateVec.ty) * sctRMS * sctRMS;

            covMat1.set(2, 2, covMat1.get(2, 2) + cov_txtx);
            covMat1.set(2, 3, covMat1.get(2, 3) + cov_txty);
            covMat1.set(3, 2, covMat1.get(3, 2) + cov_txty);
            covMat1.set(3, 3, covMat1.get(3, 3) + cov_tyty);
            
        }
    }
    private StateVec reset(StateVec SVplus, StateVec stateVec) {
        SVplus = new StateVec(stateVec.k);
        SVplus.x = stateVec.x;
        SVplus.y = stateVec.y;
        SVplus.z = stateVec.z;
        SVplus.tx = stateVec.tx;
        SVplus.ty = stateVec.ty;
        SVplus.Q = stateVec.Q;

        return SVplus;
    }
    
    public void transport(int i, int f, StateVec iVec, CovMat icovMat, boolean doEloss) { 
      
        dcSwim.SetSwimParameters((int)Math.signum(Z[f] - Z[i]), iVec.x, iVec.y, iVec.z, iVec.tx, iVec.ty, Math.abs(1. / iVec.Q), (int)Math.signum(iVec.Q));
        double[] SwRes = dcSwim.SwimToPlaneLab(Z[f]);
        StateVec fVec = new StateVec(f);
        
        fVec.x = SwRes[0];
        fVec.y = SwRes[1];
        fVec.z = SwRes[2];
        fVec.tx = SwRes[3]/SwRes[5];
        fVec.ty = SwRes[4]/SwRes[5];
        fVec.Q = Math.signum(iVec.Q)/Math.sqrt(SwRes[3]*SwRes[3]+SwRes[4]*SwRes[4]+SwRes[5]*SwRes[5]);
        
          //StateVec = fVec;
        this.trackTraj.put(f, fVec);
        /*
        //if(f<i)
        //    return;
        Matrix Cprop = FTCF( i,  f,  iVec,  icovMat.covMat);
        
        if (Cprop != null) {
            CovMat fCov = new CovMat(f);
            fCov.covMat = Cprop;
            
            //CovMat = fCov;
            this.trackCov.put(f, fCov);
        }
        
        */
        CovMat covMat = icovMat;
        double[][] u = new double[5][5];       
        double[][] C = new double[5][5];
        
        double x = iVec.x;
        double y = iVec.y;
        double tx = iVec.tx;
        double ty = iVec.ty;
        double Q = iVec.Q;

        double Eloss = 0;
        
        double mass = MassHypothesis(2); // assume given mass hypothesis
        
        double[] bf;  
        
        int nSteps = (int) (Math.abs((Z[i] - Z[f]) / stepSize) + 1);

        double s  = (Z[f] - Z[i]) / (double) nSteps;
        double z = Z[i];

       //System.out.println(" stepsize "+s +" nstep "+nSteps);
        for (int j = 0; j < nSteps; j++) {

            if (j == nSteps - 1) {
                s = Math.signum(Z[f] - Z[i]) * Math.abs(z - Z[f]);
            }

            //B bf = new B(i, z_atPlane, x_atPlane, y_atPlane, tx_atPlane, ty_atPlane, s);
            //bfieldPoints.add(bf);
            bf = new double[] {dcSwim.BfieldLab(x, y, z).x(), dcSwim.BfieldLab(x, y, z).y(), dcSwim.BfieldLab(x, y, z).z()};
           
            A(tx, ty, bf[0], bf[1], bf[2], A);
            delA_delt(tx, ty, bf[0], bf[1], bf[2], dA);

            // transport covMat            
            //double delx_deltx0 = s;
            double delx_deltx0 = s + 0.5 * Q * speedLight * s * s * dA[0];
            double dely_deltx0 = 0.5 * Q * speedLight * s * s * dA[2];
            double deltx_delty0 = Q * speedLight * s * dA[1];
            double deltx_deltx0 = 1 + Q * speedLight * s * dA[0];
            double delx_delQ = 0.5 * speedLight * s * s * A[0];
            double deltx_delQ = speedLight * s * A[0];
            double delx_delty0 = 0.5 * Q * speedLight * s * s * dA[1];
            //double dely_delty0 = s;
            double dely_delty0 = s + 0.5 * Q * speedLight * s * s * dA[3];
            double delty_deltx0 = Q * speedLight * s * dA[2];
            double delty_delty0 = 1 + Q * speedLight * s * dA[3];
            double dely_delQ = 0.5 * speedLight * s * s * A[1];
            double delty_delQ = speedLight * s * A[1];

            
            //double transpStateJacobian00=1; 
            //double transpStateJacobian01=0; 
            double transpStateJacobian02=delx_deltx0; 
            double transpStateJacobian03=delx_delty0; 
            double transpStateJacobian04=delx_delQ;
            //double transpStateJacobian10=0; 
            //double transpStateJacobian11=1; 
            double transpStateJacobian12=dely_deltx0;  
            double transpStateJacobian13=dely_delty0;  
            double transpStateJacobian14=dely_delQ;
            //double transpStateJacobian20=0; 
            //double transpStateJacobian21=0; 
            double transpStateJacobian22=deltx_deltx0;  
            double transpStateJacobian23=deltx_delty0;  
            double transpStateJacobian24=deltx_delQ;
            //double transpStateJacobian30=0; 
            //double transpStateJacobian31=0; 
            double transpStateJacobian32=delty_deltx0; 
            double transpStateJacobian33=delty_delty0; 
            double transpStateJacobian34=delty_delQ;
            //double transpStateJacobian40=0; 
            //double transpStateJacobian41=0; 
            //double transpStateJacobian42=0; 
            //double transpStateJacobian43=0; 
            //double transpStateJacobian44=1;

            //covMat = FCF^T; u = FC;
            for (int j1 = 0; j1 < 5; j1++) {
                u[0][j1] = covMat.covMat.get(0,j1) + covMat.covMat.get(2,j1) * transpStateJacobian02 + covMat.covMat.get(3,j1)* transpStateJacobian03 + covMat.covMat.get(4,j1) * transpStateJacobian04;
                u[1][j1] = covMat.covMat.get(1,j1) + covMat.covMat.get(2,j1) * transpStateJacobian12 + covMat.covMat.get(3,j1) * transpStateJacobian13 + covMat.covMat.get(4,j1) * transpStateJacobian14;
                u[2][j1] = covMat.covMat.get(2,j1) * transpStateJacobian22 + covMat.covMat.get(3,j1) * transpStateJacobian23 + covMat.covMat.get(4,j1) * transpStateJacobian24;
                u[3][j1] = covMat.covMat.get(2,j1) * transpStateJacobian32 + covMat.covMat.get(3,j1) * transpStateJacobian33 + covMat.covMat.get(4,j1) * transpStateJacobian34;
                u[4][j1] = covMat.covMat.get(4,j1);
            }

            for (int i1 = 0; i1 < 5; i1++) {
                C[i1][0] = u[i1][0] + u[i1][2] * transpStateJacobian02 + u[i1][3] * transpStateJacobian03 + u[i1][4] * transpStateJacobian04;
                C[i1][1] = u[i1][1] + u[i1][2] * transpStateJacobian12 + u[i1][3] * transpStateJacobian13 + u[i1][4] * transpStateJacobian14;
                C[i1][2] = u[i1][2] * transpStateJacobian22  + u[i1][3] * transpStateJacobian23 + u[i1][4] * transpStateJacobian24;
                C[i1][3] = u[i1][2] * transpStateJacobian32 + u[i1][3] * transpStateJacobian33 + u[i1][4] * transpStateJacobian34;
                C[i1][4] = u[i1][4];
            }
            
            // Q_atPlane	
            double p = Math.abs(1. / Q);
            double pz = p / Math.sqrt(1 + tx * tx + ty * ty);
            double px = tx * pz;
            double py = ty * pz;
            double t_ov_X0 = 0;
            
            if(Math.signum(Z[f] - Z[i]) >0 && z>Constants.FVT_Z1stlayer) {
                t_ov_X0 =  s / Constants.get_X0()[9];

                double detMat_Z_ov_A_timesThickn = Math.signum(Z[f] - Z[i]) * s * Constants.getEFF_Z_OVER_A()[9];
                for(int i1 = 1; i1<Constants.get_RELPOS().length; i1++) {
                    if(z>Constants.FVT_Z1stlayer && Math.abs(z-Z[f])>Constants.get_RELPOS()[i1-1] && Math.abs(z-Z[f])<Constants.get_RELPOS()[i1]) {
                        t_ov_X0 = s / Constants.get_X0()[i1]; //path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm
                        detMat_Z_ov_A_timesThickn = Math.signum(Z[f] - Z[i]) * s * Constants.getEFF_Z_OVER_A()[i1];
                    } else {
                        t_ov_X0 = s / Constants.get_X0()[9];
                    }
                }


                double mass_e = 0.000510998;
                double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
                double cosEntranceAngle = Math.abs((x * px + y * py + z * pz) / (Math.sqrt(x * x + y * y + z * z) * p));
                //Eloss

                double gamma = 1. / Math.sqrt(1 - beta * beta);

                double Wmax = 2. * mass * beta * beta * gamma * gamma / (1. + 2. * (mass_e / mass) * gamma + (mass_e / mass) * (mass_e / mass));
                double I = 0.000000172;

                double logterm = 2. * mass * beta * beta * gamma * gamma * Wmax / (I * I);

                double delta = 0.;
               // double dEdx = 0.0001535 * detMat_Z_ov_A_timesThickn * (Math.log(logterm) - 2 * beta * beta - delta) / (beta * beta); //in GeV/cm
                //System.out.println("  at "+Math.sqrt(iVec.x_atPlane*iVec.x_atPlane+iVec.y_atPlane*iVec.y_atPlane));
                //Eloss +=  Math.abs(dEdx / cosEntranceAngle);

                //
                double pathLength = t_ov_X0 / cosEntranceAngle;

                double sctRMS = (0.0136 / (beta * p)) * Math.sqrt(pathLength) * (1 + 0.038 * Math.log(pathLength)); // Highland-Lynch-Dahl formula
                //double sctRMS = (0.0141 / (beta * p)) * Math.sqrt(pathLength) * (1 + Math.log10(pathLength)/9.); // Highland-Lynch-Dahl formula
                //System.out.println(z+"] cos A "+cosEntranceAngle+"  A "+Math.toDegrees(Math.acos(cosEntranceAngle)) +" X0 "+s/t_ov_X0+" sctRMS "+sctRMS);
                double cov_txtx = (1 + tx * tx) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS ;
                double cov_tyty = (1 + ty * ty) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
                double cov_txty = tx * ty * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
                
                if (s > 0) {
                    C[2][2] += cov_txtx;
                    C[2][3] += cov_txty;
                    C[3][2] += cov_txty;
                    C[3][3] += cov_tyty;
                }
                
            }
           
            covMat.covMat = new Matrix(C);
            // transport stateVec
            x += tx * s + 0.5 * Q * speedLight * A[0] * s * s;
            y += ty * s + 0.5 * Q * speedLight * A[1] * s * s;
            tx += Q * speedLight * A[0] * s;
            ty += Q * speedLight * A[1] * s;

            z += s;
           
        }
        /*
        StateVec fVec = new StateVec(f);
        fVec.z = Z[f];
        fVec.x = x;
        fVec.y = y;
        fVec.tx = tx;
        fVec.ty = ty;
        // correct for Eloss
        double p = Math.abs(1. / Q);
        double Ecorr = Math.sqrt((1./Q)*(1./Q)+mass*mass) - Math.signum(Z[f] - Z[i])*Eloss;
        double pcorr = Math.sqrt(Ecorr*Ecorr-mass*mass);
        //System.out.println(" momentum correction "+1./Q+" "+pcorr);
        
        if(doEloss==true) {
            fVec.Q = Math.signum(Q)/pcorr;
        } else {
            fVec.Q = Q;
        }
        
        StateVec = fVec;
        this.trackTraj.put(f, fVec);
        */
        //System.out.println(" i "+i+" f "+f+" Zi "+Z[i]+" "+iVec.x+", "+iVec.x+", "+iVec.z+" "+fVec.x+", "+fVec.y+" "+fVec.z+" swim "+
        //        SwRes[0]+", "+SwRes[1]+", "+SwRes[2]+" charge "+(int)Math.signum(iVec.Q));
        //System.out.println("cov mat ");this.printMatrix(covMat.covMat); System.out.println("-------------------------------------");
        
//if(transpStateJacobian!=null) {
        //	F = new Matrix(transpStateJacobian); 
        //} 
        
        if (covMat.covMat != null) {
            CovMat fCov = new CovMat(f);
            fCov.covMat = covMat.covMat;
            
            //CovMat = fCov;
            this.trackCov.put(f, fCov);
        }
        
    }

    
    
    public class StateVec {

        final int k;
        public double z;
        public double x;
        public double y;
        public double tx;
        public double ty;
        public double Q;

        StateVec(int k) {
            this.k = k;
        }
        
        public double transportTroughDriftGap(double FracDriftGap, MeasVecs mv) {
            double Zi = this.z;
            double Zf = Zi + Constants.hDrift*FracDriftGap;
            double stepSiz = 0.01;
            double[] b_f;  

            int nstepsInDriftGap = 0;
            double h =0; 

            int nSteps = (int) (Math.abs((Constants.hDrift*FracDriftGap) / stepSiz) + 1);

            double s  = (Constants.hDrift*FracDriftGap) / (double) nSteps;
            double z_atPlane = Zi;
            double x_atPlane = this.x;
            double y_atPlane = this.y;
            double tx_atPlane = this.tx;
            double ty_atPlane = this.ty;
            double Q_atPlane = this.Q;  
           //System.out.println(" state vec "+k +" at "+x+", "+y);
            int strip = mv.getClosestStrip(x_atPlane, y_atPlane, this.k);
            if(h> 0 && h<FVT_Nstrips) {            
                nstepsInDriftGap =1;
                h = Constants.FVT_stripsYlocref[strip-1];
            } else {
                nstepsInDriftGap = 0;
                h = 0;
            }
           
           
           
            for (int j = 0; j < nSteps; j++) {

                if (j == nSteps - 1) {
                    s = Math.signum(Zf - Zi) * Math.abs(z_atPlane - Zf);
                }

                //B bf = new B(i, z_atPlane, x_atPlane, y_atPlane, tx_atPlane, ty_atPlane, s);
                //bfieldPoints.add(bf);
                b_f = new double[] {dcSwim.BfieldLab(x_atPlane, y_atPlane, z_atPlane).x(), dcSwim.BfieldLab(x_atPlane, y_atPlane, z_atPlane).y(), dcSwim.BfieldLab(x_atPlane, y_atPlane, z_atPlane).z()};

                A(tx_atPlane, ty_atPlane, b_f[0], b_f[1], b_f[2], A);

                // transport stateVec
                x_atPlane += tx_atPlane * s + 0.5 * Q_atPlane * speedLight * A[0] * s * s;
                y_atPlane += ty_atPlane * s + 0.5 * Q_atPlane * speedLight * A[1] * s * s;
                tx_atPlane += Q_atPlane * speedLight * A[0] * s;
                ty_atPlane += Q_atPlane * speedLight * A[1] * s;

                z_atPlane += s;

                int closestStrip = mv.getClosestStrip(x_atPlane, y_atPlane, this.k);
                if(closestStrip> 0 && closestStrip<FVT_Nstrips) {            
                    nstepsInDriftGap++;
                    h+=Constants.FVT_stripsYlocref[closestStrip-1];
                    //System.out.println(" Z "+Zi+" Z' "+z_atPlane+" s "+s+" strip  "+closestStrip+" h "+h/nstepsInDriftGap);
                } else {
                    //System.out.println(x_atPlane+" ,  "+y_atPlane+" ,  "+z_atPlane);
                }
            }
        
            if(nstepsInDriftGap>0) {//System.out.println(" h_ave "+h/nstepsInDriftGap);
                return h/nstepsInDriftGap; 
            } else {//System.out.println(" h_ave outside range "+x_atPlane+" ,  "+y_atPlane+" ,  "+z_atPlane);
                return Double.POSITIVE_INFINITY;
            }

        }

    }

    public class CovMat {

        final int k;
        public Matrix covMat;

        CovMat(int k) {
            this.k = k;
        }

    }

    DCSwimmer dcSwim = new DCSwimmer();

    
    private void A(double tx, double ty, double Bx, double By, double Bz, double[] a) {

        double C = Math.sqrt(1 + tx * tx + ty * ty);
        a[0] = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        a[1] = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);
    }

    private void delA_delt(double tx, double ty, double Bx, double By, double Bz, double[] dela_delt) {

        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

        dela_delt[0] = tx * Ax / C2 + C * (ty * Bx - 2 * tx * By); //delAx_deltx
        dela_delt[1] = ty * Ax / C2 + C * (tx * Bx + Bz); //delAx_delty
        dela_delt[2] = tx * Ay / C2 + C * (-ty * By - Bz); //delAy_deltx
        dela_delt[3] = ty * Ay / C2 + C * (-tx * By + 2 * ty * Bx); //delAy_delty
        
    }

    
     /**
        1 piMass = 0.13957018;
        2 KMass = 0.493677;
        3 muMass = 0.105658369;
        0 eMass = 0.000510998;
        4 pMass = 0.938272029;
        **/
    private void setMass(int hypo, double mass) {
          
        switch (hypo) {
            case 0:
                mass = 0.000510998;
                break;
            case 1:
                mass = 0.13957018;
                break;
            case 2:    
                mass = 0.493677;
                break;
            case 3:  
                mass = 0.105658369;
                break;
            case 4:  
                mass = 0.938272029;    
                break;
        }
    }
     

    public void rinit(double z0, int kf) {
        if (this.trackTraj.get(kf) != null) {
            double x = this.trackTraj.get(kf).x;
            double y = this.trackTraj.get(kf).y;
            double z = this.trackTraj.get(kf).z;
            double tx = this.trackTraj.get(kf).tx;
            double ty = this.trackTraj.get(kf).ty;
            double p = 1. / Math.abs(this.trackTraj.get(kf).Q);
            int q = (int) Math.signum(this.trackTraj.get(kf).Q);

            dcSwim.SetSwimParameters(-1, x, y, z, tx, ty, p, q);
            double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneLab(z0);
            StateVec initSV = new StateVec(0);
            if(VecAtFirstMeasSite==null)
                return;
            initSV.x = VecAtFirstMeasSite[0];
            initSV.y = VecAtFirstMeasSite[1];
            initSV.z = VecAtFirstMeasSite[2];
            initSV.tx = VecAtFirstMeasSite[3] / VecAtFirstMeasSite[5];
            initSV.ty = VecAtFirstMeasSite[4] / VecAtFirstMeasSite[5];
            initSV.Q = this.trackTraj.get(kf).Q;
            this.trackTraj.put(0, initSV);
        } else {
        }
    }

    public void init(Track trkcand, KFitter kf) {

        if (trkcand != null) {
            
            StateVec initSV = new StateVec(0);
            initSV.x = trkcand.getX();
            initSV.y = trkcand.getY();
            initSV.z = trkcand.getZ();
            initSV.tx = trkcand.getPx() / trkcand.getPz();
            initSV.ty = trkcand.getPy() / trkcand.getPz();
            double p = Math.sqrt(trkcand.getPx()*trkcand.getPx()+trkcand.getPy()*trkcand.getPy()+trkcand.getPz()*trkcand.getPz());
            initSV.Q = (double)trkcand.getQ() / p; 
            this.trackTraj.put(0, initSV);
            
            double exSq = 7.449126e-03*7.449126e-03;
            double eySq = 9.019044e-02*9.019044e-02;
            double euxSq = 7.159996e-04*7.159996e-04;
            double euySq = 7.169160e-04*7.169160e-04;
            double epSq = 4.809686e-04*4.809686e-04 ;
            
           
            Matrix initCMatrix = new Matrix(new double[][]{
                {exSq, 0, 0, 0, 0},
                {0, eySq, 0, 0, 0},
                {0, 0, euxSq, 0, 0},
                {0, 0, 0, euySq, 0},
                {0, 0, 0, 0, epSq}
            });

            CovMat initCM = new CovMat(0);
            initCM.covMat = initCMatrix;
            this.trackCov.put(0, initCM); 
            
        } else {
            kf.setFitFailed = true;
            return;
        }
           
    }
    public void setFittedTrackPars(Track trk, StateVec finalSV) {
        int q = (int) Math.signum(finalSV.Q);
        double p = 1./Math.abs(finalSV.Q);
        double pz = p/Math.sqrt(finalSV.tx*finalSV.tx + finalSV.ty*finalSV.ty + 1);
        double px = finalSV.tx*pz;
        double py = finalSV.ty*pz;
        //System.out.println(finalSV.x_atPlane+", "+ finalSV.y_atPlane+", "+ finalSV.z_atPlane+" new track momentum "+px+" ; "+py+" ; "+pz+" in sector "+trk.get_Sector());
        dcSwim.SetSwimParameters(finalSV.x, finalSV.y, finalSV.z, -px, -py, -pz, -q);
	
        double theta_n = ((double)(trk.get_Sector()-1))*Math.toRadians(60.);
        double x_n = Math.cos(theta_n) ; 
        double y_n = Math.sin(theta_n) ;  
        double[] Vt = dcSwim.SwimToPlaneBoundary(0, new Vector3D(x_n, y_n, 0), -1);


        double xOrFix = Vt[0];
        double yOrFix = Vt[1];
        double zOrFix = Vt[2];
        double pxOrFix = -Vt[3];
        double pyOrFix = -Vt[4];
        double pzOrFix = -Vt[5];
        double arclen = Vt[6];
        //System.out.println(" old track "+trk.getX()+", "+trk.getY()+", "+trk.getZ()+"; "+trk.getPx()+", "+trk.getPy()+", "+trk.getPz());
        trk.setX(xOrFix);
        trk.setY(yOrFix);
        trk.setZ(zOrFix);
        trk.setPx(pxOrFix);
        trk.setPy(pyOrFix);
        trk.setPz(pzOrFix);
        //System.out.println(" new track "+trk.getX()+", "+trk.getY()+", "+trk.getZ()+"; "+trk.getPx()+", "+trk.getPy()+", "+trk.getPz());
    }
    
    public double MassHypothesis(int H) {
        double piMass = 0.13957018;
        double KMass = 0.493677;
        double muMass = 0.105658369;
        double eMass = 0.000510998;
        double pMass = 0.938272029;
        double value = piMass; //default
        if (H == 4) {
            value = pMass;
        }
        if (H == 1) {
            value = eMass;
        }
        if (H == 2) {
            value = piMass;
        }
        if (H == 3) {
            value = KMass;
        }
        if (H == 0) {
            value = muMass;
        }
        return value;
    }
    
    
    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
            System.out.println(C.get(k, 0) + "	" + C.get(k, 1) + "	" + C.get(k, 2) + "	" + C.get(k, 3) + "	" + C.get(k, 4));
        }
    }
}
