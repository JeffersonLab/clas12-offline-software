/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.dc.timetodistance;

/**
 *
 * @author ziegler
 */
public class T2DFunctions {
    
    
    public static double ExpoFcn(double x, double alpha, double bfield, double v_0, double delta_nm, double R,
        double tmax, double dmax, double delBf, double Bb1, double Bb2, double Bb3, double Bb4, int superlayer)  {

        if(x>dmax)
            x=dmax;

        double cos30minusalpha=Math.cos(Math.toRadians(30.-alpha));
        double xhat = x/dmax;
        double dmaxalpha = dmax*cos30minusalpha;
        double xhatalpha = x/dmaxalpha;
        // Assume a functional form (time=x/v0+a*(x/dmax)**n+b*(x/dmax)**m)
        // for time as a function of x for theta = 30 deg.
        // first, calculate n

        double n = ( 1.+ (delta_nm-1.)*Math.pow(R, delta_nm) )
                /( 1.- Math.pow(R, delta_nm));
        // now, calculate m
        double m = n + delta_nm;
        // determine b from the requirement that the time = tmax at dist=dmax
        double b = (tmax - dmax/v_0)/(1.- m/n);
        // determine a from the requirement that the derivative at
        // d=dmax equal the derivative at d=0
        double a = -b*m/n;

        //     Now calculate the dist to time function for theta = 'alpha' deg.
        //     Assume a functional form with the SAME POWERS N and M and
        //     coefficient a but a new coefficient 'balpha' to replace b.    
        //     Calculate balpha from the constraint that the value
        //     of the function at dmax*cos30minusalpha is equal to tmax

        //     parameter balpha (function of the 30 degree paramters a,n,m)
        double balpha = ( tmax - dmaxalpha/v_0 - a*Math.pow(cos30minusalpha,n))
                /Math.pow(cos30minusalpha, m);

        //      now calculate function    
        double time = x/v_0 + a*Math.pow(xhat, n) + balpha*Math.pow(xhat, m);

        //B correction
        time+=T2DFunctions.CorrectForB(delBf, bfield, tmax, xhatalpha, Bb1, Bb2, Bb3, Bb4, superlayer);
        return time;
    }
    
     /**
     * 
     * @param x trkDoca
     * @param alpha reduced angle
     * @param bfield bField in Tesla
     * @param v_0 min velocity
     * @param vm velocity at inflection point
     * @param R x at inflection point
     * @param tmax
     * @param dmax
     * @param delBf Bfield dependence scale
     * @param Bb1 Bfield dependence parametrization coefficient
     * @param Bb2 Bfield dependence parametrization coefficient
     * @param Bb3 Bfield dependence parametrization coefficient
     * @param Bb4 Bfield dependence parametrization coefficient
     * @param superlayer
     * @return  time
     */
    public static double polyFcnP5(double x, double alpha, double bfield, double v_0, double vm, double R, 
            double tmax, double dmax, double delBf, double Bb1, double Bb2, double Bb3, double Bb4, int superlayer) {
        
        if(x>dmax)
            x=dmax;
        double time = 0;
        // alpha correction for region 2
        //correctAlphaForB( alpha, bfield, superlayer);
        double cos30minusalpha=Math.cos(Math.toRadians(30.-alpha));
        double dmaxalpha = dmax*cos30minusalpha;
        double xhatalpha = x/dmaxalpha;
        //form: time=a*x**4+b*x**3+c*x**2+d*x where x is distance in cm and time is in nsec
        //  We apply 4 CONSTRAINTS to the parameters:
        // 1- the VELOCITY at distance = 0 is the saturated drift velocity (v0)
        // 2- the TIME at distance = dmaxalpha (which is just dmax for alpha
        //   = 30 deg) is equal to TMAX
        // 3- for alpha = 30 deg., the velocity is a MINIMUM at x = R * dmax (R~0.615)
        // This is simply because this is the point of minimum electric field
        // in an hexagonal cell (based on GARFIELD).
        // 4- the VELOCITY at distance = DMAX is the saturated drift velocity (v0)
        // This is because the electric field near the field wire is above
        // the critical field strength that yields a saturated drift velocity
        /*
{{a -> (-18 A^4 D^3 r^3 v vm + 36 r^4 T v vm + 
      12 A^2 r^2 (2 D r - T v) vm - 12 A r^3 (3 D r + 2 T v) vm + 
      6 A^3 D r^2 (v + vm + 2 D^2 r^2 v vm) + 
      A^5 D (v - vm + 6 D^2 r^2 v vm))/(A^3 D^5 (A - r) r^2 (3 A^3 + 
        19 A^2 r - 50 A r^2 + 10 r^3) v vm), 
  b -> -((15 A^4 D r^2 vm + 45 A^2 D r^4 vm - 27 A^5 D^3 r^3 v vm + 
        60 r^5 T v vm - 15 A r^4 (4 D r + 3 T v) vm + 
        2 A^6 D (v - vm + 6 D^2 r^2 v vm) + 
        5 A^3 r^2 (2 D r (v - vm) + 3 D^3 r^3 v vm - 
           3 T v vm))/(A^3 D^4 (A - r) r^2 (3 A^3 + 19 A^2 r - 
          50 A r^2 + 10 r^3) v vm)), 
  c -> (-20 A D r^6 vm - 60 A^2 r^4 T v vm + 20 r^6 T v vm + 
      20 A^3 r^3 (3 D r + 2 T v) vm - 
      18 A^5 D r^2 (v - vm + 2 D^2 r^2 v vm) + 
      A^7 D (v - vm + 6 D^2 r^2 v vm) + 
      10 A^4 D r^3 (-6 vm + v (2 + 3 D^2 r^2 vm)))/(A^3 D^3 (A - 
        r) r^2 (3 A^3 + 19 A^2 r - 50 A r^2 + 10 r^3) v vm), 
  d -> (90 A^3 D r^2 vm - 9 A^6 D^3 r v vm - 30 r^4 T v vm - 
      30 A^2 r^2 (4 D r + 3 T v) vm + 30 A r^3 (D r + 4 T v) vm + 
      12 A^5 D (v - vm + 2 D^2 r^2 v vm) - 
      5 A^4 D r (-2 vm + v (2 + 3 D^2 r^2 vm)))/(A^2 D^2 (A - 
        r) (3 A^3 + 19 A^2 r - 50 A r^2 + 10 r^3) v vm), d -> 1/v}}
        */
        double cosA = cos30minusalpha;
        
        double denom = (cosA - R) *(3*cosA*cosA*cosA 
                + 19*cosA*cosA *R 
                - 50 *cosA* R*R 
                + 10 *R*R*R)* v_0* vm;
        
        double denom_a = cosA*cosA*cosA*dmax*dmax*dmax*dmax*dmax*R*R*denom;
        double denom_b = cosA*cosA*cosA*dmax*dmax*dmax*dmax*R*R*denom;
        double denom_c = cosA*cosA*cosA*dmax*dmax*dmax*R*R*denom;
        double denom_d = cosA*cosA*dmax*dmax*denom;
        
        
        double a = (-18 *Math.pow(cosA,4) *Math.pow(dmax,3) *Math.pow(R,3)* v_0 *vm + 36 *Math.pow(R,4) *tmax *v_0 *vm + 
                    12 *Math.pow(cosA,2) *Math.pow(R,2) *(2 *dmax *R - tmax *v_0)* vm - 12 *cosA *Math.pow(R,3) *(3 *dmax *R + 2 *tmax *v_0) *vm + 
                    6 *Math.pow(cosA,3)* dmax *Math.pow(R,2) *(v_0 + vm + 2 *Math.pow(dmax,2) *Math.pow(R,2) *v_0 *vm) + 
                    Math.pow(cosA,5) *dmax *(v_0 - vm + 6 *Math.pow(dmax,2)*Math.pow(R,2)*v_0 *vm))/denom_a;
        
        double b = -(15 *Math.pow(cosA,4) *dmax *Math.pow(R,2) *vm + 45 *Math.pow(cosA,2) *dmax *Math.pow(R,4) *vm - 27 *Math.pow(cosA,5)* Math.pow(dmax,3) *Math.pow(R,3) *v_0 *vm + 
                    60 *Math.pow(R,5) *tmax *v_0 *vm - 15 *cosA *Math.pow(R,4) *(4 *dmax *R + 3 *tmax *v_0)* vm + 
                    2 *Math.pow(cosA,6) *dmax *(v_0 - vm + 6 *Math.pow(dmax,2) *Math.pow(R,2) *v_0 *vm) + 
                    5 *Math.pow(cosA,3) *Math.pow(R,2) *(2 *dmax *R *(v_0 - vm) + 3 *Math.pow(dmax,3) *Math.pow(R,3) *v_0 *vm - 
                    3 *tmax *v_0 *vm))/denom_b;
        
        double c = (-20 *cosA *dmax *Math.pow(R,6)* vm - 60 *Math.pow(cosA,2) *Math.pow(R,4) *tmax* v_0 *vm + 20 *Math.pow(R,6) *tmax *v_0 *vm + 
                    20 *Math.pow(cosA,3)* Math.pow(R,3) *(3* dmax *R + 2 *tmax* v_0) *vm - 
                    18 *Math.pow(cosA,5) *dmax *Math.pow(R,2) *(v_0 - vm + 2 *Math.pow(dmax,2) *Math.pow(R,2)* v_0 *vm) + 
                    Math.pow(cosA,7) *dmax *(v_0 - vm + 6 *Math.pow(dmax,2) *Math.pow(R,2) *v_0 *vm) + 
                    10 *Math.pow(cosA,4)* dmax* Math.pow(R,3) *(-6 *vm + v_0 *(2 + 3 *Math.pow(dmax,2) *Math.pow(R,2)* vm)))/denom_c;

        double d = (90* Math.pow(cosA,3) *dmax* Math.pow(R,2)* vm - 9 *Math.pow(cosA,6) *Math.pow(dmax,3) *R *v_0 *vm - 30* Math.pow(R,4) *tmax *v_0* vm - 
                    30 *Math.pow(cosA,2) *Math.pow(R,2) *(4 *dmax *R + 3 *tmax *v_0) *vm + 30 *cosA *Math.pow(R,3) *(dmax *R + 4 *tmax *v_0)* vm + 
                    12 *Math.pow(cosA,5)* dmax *(v_0 - vm + 2 *Math.pow(dmax,2) *Math.pow(R,2) *v_0 *vm) - 
                    5 *Math.pow(cosA,4) *dmax *R *(-2 *vm + v_0* (2 + 3 *Math.pow(dmax,2) *Math.pow(R,2)* vm)))/denom_d;
        
        double e = 1./v_0;
                
        time = a*x*x*x*x*x + b*x*x*x*x + c*x*x*x + d*x*x + e*x ;
        
        //B correction
        time+=T2DFunctions.CorrectForB(delBf, bfield, tmax, xhatalpha, Bb1, Bb2, Bb3, Bb4, superlayer);
        
        return time;
    }
    
    /**
     * 
     * @param x trkDoca
     * @param alpha reduced angle
     * @param bfield bField in Tesla
     * @param v_0 min velocity
     * @param vm velocity at inflection point
     * @param R x at inflection point
     * @param tmax
     * @param dmax
     * @param delBf Bfield dependence scale
     * @param Bb1 Bfield dependence parametrization coefficient
     * @param Bb2 Bfield dependence parametrization coefficient
     * @param Bb3 Bfield dependence parametrization coefficient
     * @param Bb4 Bfield dependence parametrization coefficient
     * @param superlayer
     * @return  time
     */
    public static double polyFcnDmaxV0Constraint(double x, double alpha, double bfield, double v_0, double vm, double R, 
            double tmax, double dmax, double delBf, double Bb1, double Bb2, double Bb3, double Bb4, int superlayer) {
        
        if(x>dmax)
            x=dmax;
        double time = 0;
        // alpha correction for region 2
        //correctAlphaForB( alpha, bfield, superlayer);
        double cos30minusalpha=Math.cos(Math.toRadians(30.-alpha));
        double dmaxalpha = dmax*cos30minusalpha;
        double xhatalpha = x/dmaxalpha;
        //form: time=a*x**4+b*x**3+c*x**2+d*x where x is distance in cm and time is in nsec
        //  We apply 4 CONSTRAINTS to the parameters:
        // 1- the VELOCITY at distance = 0 is the saturated drift velocity (v0)
        // 2- the TIME at distance = dmaxalpha (which is just dmax for alpha
        //   = 30 deg) is equal to TMAX
        // 3- for alpha = 30 deg., the velocity is a MINIMUM at x = R * dmax (R~0.615)
        // This is simply because this is the point of minimum electric field
        // in an hexagonal cell (based on GARFIELD).
        // 4- the VELOCITY at distance = DMAX is the saturated drift velocity (v0)
        // This is because the electric field near the field wire is above
        // the critical field strength that yields a saturated drift velocity
        /*
        e -> (-18 A^4 D^3 r^3 v vm + 36 r^4 T v vm + 
     12 A^2 r^2 (2 D r - T v) vm - 12 A r^3 (3 D r + 2 T v) vm + 
     6 A^3 D r^2 (v + vm + 2 D^2 r^2 v vm) + 
     A^5 D (v - vm + 6 D^2 r^2 v vm))/(12 r^2 (-A + r) (A + 
       3 r) v vm), 
 a -> (6 D^2 r^3 v vm + A (vm - v (1 + 6 D^2 r^2 vm)))/(
  4 D^3 (A - r) r^2 (A + 3 r) v vm), 
 b -> (-3 r^2 (v - vm + 2 D^2 r^2 v vm) + 
   A^2 (v - vm + 6 D^2 r^2 v vm))/(3 D^2 (A - r) r^2 (A + 3 r) v vm), 
 c -> -((3 A (vm + v (-1 + 2 A D^2 r vm - 2 D^2 r^2 vm)))/(
   2 D (A - r) (A + 3 r) v vm)), d -> 1/v}
        */
        double cosA = cos30minusalpha;
        
        double denom = (cosA - R)*(cosA + 3 *R)* v_0 *vm;
        double denom_a = 4*dmax*dmax*dmax*R*R*denom;
        double denom_b = 3*dmax*dmax*R*R*denom;
        double denom_c = 2*dmax*denom;
        double denom_e = -12*R*R*denom;
        
        double e = (-18 *cosA*cosA*cosA*cosA *dmax*dmax*dmax *R*R*R* v_0* vm 
                + 36 *R*R*R*R* tmax* v_0* vm + 
                12 *cosA*cosA* R*R* (2 *dmax* R - tmax* v_0)* vm 
                - 12 *cosA *R*R*R* (3 *dmax* R + 2 *tmax* v_0)* vm + 
                6 *cosA*cosA*cosA* dmax *R*R* (v_0 + vm + 2 *dmax*dmax* R*R* v_0* vm) + 
                cosA*cosA*cosA*cosA*cosA* dmax* (v_0 - vm + 6* dmax*dmax* R*R* v_0* vm))/denom_e;
        
        double a = (6 *dmax*dmax* R*R*R* v_0* vm + cosA *(vm - v_0* (1 + 6 *dmax*dmax* R*R* vm)))/denom_a;
        
        double b = (-3 *R*R* (v_0 - vm + 2 *dmax*dmax* R*R* v_0* vm) +
                cosA*cosA *(v_0 - vm + 6 *dmax*dmax *R*R* v_0* vm))/denom_b;
        
        double c = -(3 *cosA *(vm + v_0* (-1 + 2 *cosA *dmax*dmax *R *vm - 2* dmax*dmax* R*R* vm)))/denom_c;

        double d = 1/v_0;
                
        time = a*x*x*x*x + b*x*x*x + c*x*x + d*x + e;
        
        //B correction
        time+=T2DFunctions.CorrectForB(delBf, bfield, tmax, xhatalpha, Bb1, Bb2, Bb3, Bb4, superlayer);
        
        return time;
    }
    /**
     * 
     * @param x trkDoca
     * @param alpha reduced angle
     * @param bfield bField in Tesla
     * @param v_0 min velocity
     * @param vm velocity at inflection point
     * @param R x at inflection point
     * @param tmax
     * @param dmax
     * @param delBf Bfield dependence scale
     * @param Bb1 Bfield dependence parametrization coefficient
     * @param Bb2 Bfield dependence parametrization coefficient
     * @param Bb3 Bfield dependence parametrization coefficient
     * @param Bb4 Bfield dependence parametrization coefficient
     * @param superlayer
     * @return  time
     */
    public static double polyFcnNoDmaxV0Constraint(double x, double alpha, double bfield, double v_0, double vm, double R, 
            double tmax, double dmax, double delBf, double Bb1, double Bb2, double Bb3, double Bb4, int superlayer) {
        
        if(x>dmax)
            x=dmax;
        double time = 0;
        // alpha correction for region 2
        //correctAlphaForB( alpha, bfield, superlayer);
        double cos30minusalpha=Math.cos(Math.toRadians(30.-alpha));
        double dmaxalpha = dmax*cos30minusalpha;
        double xhatalpha = x/dmaxalpha;
        //form: time=a*x**4+b*x**3+c*x**2+d*x where x is distance in cm and time is in nsec
        //  We apply 4 CONSTRAINTS to the parameters:
        // 1- the VELOCITY at distance = 0 is the saturated drift velocity (v0)
        // 2- the TIME at distance = dmaxalpha (which is just dmax for alpha
        //   = 30 deg) is equal to TMAX
        // 3- for alpha = 30 deg., the velocity is a MINIMUM at x = R * dmax (R~0.615)
        // This is simply because this is the point of minimum electric field
        // in an hexagonal cell (based on GARFIELD).
        // 4- the VELOCITY at distance = DMAX is the saturated drift velocity (v0)
        // This is because the electric field near the field wire is above
        // the critical field strength that yields a saturated drift velocity

        double cosA = cos30minusalpha;
        double denom = cosA*cosA *dmax*dmax *(3* cosA*cosA - 8* cosA* R + 6 *R*R)* v_0 *vm;

        double a = (cosA*cosA*cosA *dmax* (v_0 - vm) 
                - 3* cosA *dmax *R*R* vm 
                + 3 *R*R* tmax* v_0 *vm 
                + 3 *cosA*cosA *dmax* R *(-v_0 + vm))/(dmax*dmax * R*R *denom);
        double b = (6 *cosA*cosA *dmax *R*R* (v_0 - vm) 
                + 8 * cosA*dmax *R*R*R* vm 
                - 8* R*R*R* tmax* v_0 *vm 
                + cosA*cosA*cosA*cosA *dmax *(-v_0 + vm))/(dmax * R*R *denom);
        double c = (3 *cosA*cosA*cosA*cosA *dmax * (v_0 - vm) 
                - 6 *cosA *dmax *R*R*R* vm 
                + 6 *R*R*R* tmax* v_0 *vm 
                + 6 *cosA*cosA*cosA *dmax* R* (-v_0 + vm))/(R*denom);

        double d = 1/v_0;
         
        time = a*x*x*x*x + b*x*x*x + c*x*x + d*x ;
        
        //B correction
        time+=T2DFunctions.CorrectForB(delBf, bfield, tmax, xhatalpha, Bb1, Bb2, Bb3, Bb4, superlayer);
        
        return time;
    }
    public static double polyFcnMac(double x, double alpha, double bfield, double v_0, double vm, double R, 
            double tmax, double dmax, double delBf, double Bb1, double Bb2, double Bb3, double Bb4, int superlayer) {
        
        if(x>dmax)
            x=dmax;
        double time = 0;
        // alpha correction 
        double cos30minusalpha=Math.cos(Math.toRadians(30.-alpha));
        double dmaxalpha = dmax*cos30minusalpha;
        double xhatalpha = x/dmaxalpha;
        //   rcapital is an intermediate parameter
        double rcapital = R*dmax;
        //   delt is another intermediate parameter
        double delt=tmax-dmax/v_0;
        double delv=1./vm-1./v_0;
        //   now calculate the primary parameters a, b, c, d
        
        double c = ((3.*delv)/(R*dmax)+(12*R*R*delt)/(2.*(1-2*R)*
            (dmax*dmax)));
        c = c /(4.-(1.-6.*R*R)/(1.-2.*R));
        double b = delv/(rcapital*rcapital) - 4.*c/(3.*rcapital);
        double d = 1/v_0;
        double a = (tmax -  b*dmaxalpha*dmaxalpha*dmaxalpha - 
                c*dmaxalpha*dmaxalpha - d*dmaxalpha)/(dmaxalpha*dmaxalpha*dmaxalpha*dmaxalpha) ;       
        time = a*x*x*x*x + b*x*x*x + c*x*x + d*x ;
        
        //B correction
        time+=T2DFunctions.CorrectForB(delBf, bfield, tmax, xhatalpha, Bb1, Bb2, Bb3, Bb4, superlayer);
        
        return time;
    }
    public static double polyFcnSpline(double x, double alpha, double bfield, double v_0, double vm, double R, 
            double tmax, double dmax, double delBf, double Bb1, double Bb2, double Bb3, double Bb4, int superlayer) {
        
        if(x>dmax)
            x=dmax;
        double time = 0;
        // alpha correction for region 2
        //correctAlphaForB( alpha, bfield, superlayer);
        double cosA = Math.cos(Math.toRadians(30.-alpha));
        double dmaxalpha = dmax*cosA;
        double xhatalpha = x/dmaxalpha;
        
        double d = 0;
        double c = 0;
        double a = 0;
        double b = 0;
        double k1 = 0.2;
        double k2 = 0.6;
        double a1 =  (cosA*cosA* dmax*dmax* (k2 - dmax *R) *(v_0 - vm) + 
                3 *dmax*dmax* R*R *tmax *v_0* vm + 
                k1*k1*k1* (-v_0 + vm) + k1*k1* (k2 *v_0 - k2 *vm - 3 *tmax *v_0* vm) + 
                cosA *dmax *(k1*k1* (2 *v_0 + vm) + 
                dmax *R *(-2 *k2 *v_0 + 2 *k2 *vm - 3 *dmax *R *vm)))
                /(3 *dmax*dmax* k1*k1* ((k1 - k2)* R*R + 2 *cosA *R *(k2 - dmax *R) 
                + cosA*cosA *(-k2 + dmax *R)) *v_0 *vm);
        double b1 = (cosA*cosA *dmax *(-k2 + dmax *R) *(v_0 - vm) + 
                R *(k1*k1 *(v_0 - vm) + 3 *k1 *tmax *v_0 *vm - 3 *dmax *R *tmax *v_0 *vm + 
                k1 *k2 *(-v_0 + vm)) + cosA *dmax *R *(2 *k2 *(v_0 - vm) + 3 *dmax *R *vm - 
                k1 *(2 *v_0 + vm)))/(dmax *k1 *((k1 - k2) *R*R + 2 *cosA *R *(k2 - dmax *R) + 
                cosA*cosA *(-k2 + dmax *R)) *v_0 *vm); 
        double c1 = 1/v_0;
        
        double a2 = (k2 *v_0 - k2 *vm - 3 *tmax *v_0 *vm + k1 *(-v_0 + vm) + cosA *dmax *(2 *v_0 + vm))/(
                3 *dmax*dmax* ((k1 - k2) *R*R + 2 *cosA *R *(k2 - dmax *R) + 
                cosA*cosA *(-k2 + dmax *R)) *v_0 *vm);
        double b2 = -((R *(k2 *v_0 - k2 *vm - 3 *tmax *v_0 *vm + k1 *(-v_0 + vm) + 
                cosA *dmax *(2 *v_0 + vm)))/(
                dmax *((k1 - k2) *R*R + 2 *cosA *R *(k2 - dmax *R) + 
                cosA*cosA *(-k2 + dmax *R)) *v_0 *vm));
        double c2 = (cosA*cosA *(k2 - dmax *R) *v_0 + R*R *(-k1 + k2 + 3 *tmax *v_0) *vm - 
                cosA *R *(2 *k2 *v_0 + dmax *R *vm))/(((-k1 + k2) *R*R + cosA*cosA *(k2 - dmax *R) + 
                2 *cosA *R *(-k2 + dmax *R)) *v_0 *vm);
        double d2 = -((k1 *(cosA*cosA *(k2 - dmax *R) *(v_0 - vm) + 3 *R*R *tmax *v_0 *vm + 
                cosA *R *(-2 *k2 *v_0 + 2 *k2 *vm - 3 *dmax *R *vm)))/(
                3 *((-k1 + k2) *R*R + cosA*cosA *(k2 - dmax *R) + 
                2 *cosA *R *(-k2 + dmax *R)) *v_0 *vm));
        double a3 = (k2*k2*k2 *v_0 - k2*k2*k2 *vm - 3 *k2*k2 *tmax *v_0 *vm + 3 *dmax*dmax *R*R *tmax *v_0 *vm + 
                k1 *k2*k2 *(-v_0 + vm) + 3 *cosA*cosA *dmax*dmax *(-k2 + dmax *R) *(v_0 + vm) + 
                cosA *dmax *(k2 - dmax *R) *(2 *k1 *(v_0 - vm) + 
                3 *(k2 + dmax *R + 2 *tmax *v_0) *vm))/(3 *dmax*dmax *Math.pow((-cosA *dmax + k2),2) 
                *((k1 - k2)* R*R + 2 *cosA *R *(k2 - dmax *R) + 
                cosA*cosA *(-k2 + dmax *R)) *v_0 *vm);
        double b3 = (3 *cosA *dmax *k2 *R *(-k2 + dmax *R) *vm + 
                cosA*cosA*cosA *dmax*dmax *(k2 - dmax *R) *(2 *v_0 + vm) + 
                cosA*cosA *dmax *(-k2 + dmax *R) *(k1 *(v_0 - vm) + 3 *tmax *v_0 *vm) + 
                k2 *R *(k1 *k2 *(v_0 - vm) + 3 *k2 *tmax *v_0* vm - 3 *dmax *R *tmax *v_0 *vm + 
                k2*k2 *(-v_0 + vm)))/(dmax *Math.pow((-cosA *dmax + k2),2) *((k1 - k2) *R*R + 
                2 *cosA *R *(k2 - dmax *R) + cosA*cosA *(-k2 + dmax *R)) *v_0 *vm);
        double c3 = (Math.pow(cosA,4) *dmax*dmax *(-k2 + dmax *R) *v_0 + (k1 - k2) *k2*k2 *R*R *vm + 
                cosA*cosA*cosA *dmax *(-k2*k2 + dmax*dmax *R*R) *vm + 
                cosA*cosA *(-k2*k2*k2 *v_0 + k1 *k2*k2 *(v_0 - vm) + dmax*dmax *k1 *R*R *vm - 
                3 *dmax*dmax *k2 *R*R *vm - 3 *dmax*dmax *R*R *tmax *v_0 *vm + 
                3 *k2*k2 *(dmax *R + tmax *v_0) *vm) + 
                2 *cosA *k2 *R *(-k1 *(k2 *(v_0 - vm) + dmax *R *vm) + 
                v_0 *(k2*k2 - 3 *k2 *tmax *vm + 3 *dmax *R *tmax *vm)))/(Math.pow((-cosA *dmax + 
                k2),2) *((k1 - k2) *R*R + 2 *cosA *R *(k2 - dmax *R) + 
                cosA*cosA *(-k2 + dmax *R)) *v_0 *vm);
        double d3 = (Math.pow(cosA,4) *dmax*dmax *k1 *(k2 - dmax *R) *(v_0 - vm) + 
                3 *(k1 - k2) *k2*k2 *R*R *tmax *v_0 *vm + 
                3 *cosA *k2 *R *(2 *k2*k2 *tmax *v_0 + dmax *R *(k2*k2 - k1 *(k2 + 2 *tmax *v_0))) *vm + 
                cosA*cosA*cosA *dmax *(-2 *k1 *k2*k2 *(v_0 - vm) - 3 *dmax*dmax *k1 *R*R *vm + 
                k2*k2*k2 *(2 *v_0 + vm)) + 
                3 *cosA*cosA *(-k2*k2*k2 *tmax *v_0 *vm + dmax*dmax *k1 *R*R *(2 *k2 + tmax *v_0) *vm - 
                dmax *k2*k2 *R *(k1 *(-v_0 + vm) + k2 *(v_0 + vm))))
                /(3 *(-cosA *dmax + k2)*(-cosA *dmax + k2) *((k1 - k2) *R*R + 2 *cosA *R *(k2 - dmax *R) + 
                cosA*cosA *(-k2 + dmax *R)) *v_0 *vm);
        if(x>=0 && x< k1) {
            d = 0;
            a = a1;
            b = b1;
            c = c1;
            
        }
        if(x>=k1 && x< k2) {
            a = a2;
            b = b2;
            c = c2;
            d = d2;
        }
        
        if(x>= k2) {
            a = a3;
            b = b3;
            c = c3;
            d = d3;
        }
        
        time = a*x*x*x + b*x*x + c*x + d ;
        
        //B correction
        time+=T2DFunctions.CorrectForB(delBf, bfield, tmax, xhatalpha, Bb1, Bb2, Bb3, Bb4, superlayer);
        
        return time;
        
    }

    /**
     * 
     * @param delBf
     * @param bfield
     * @param tmax
     * @param xhatalpha
     * @param Bb1
     * @param Bb2
     * @param Bb3
     * @param Bb4
     * @param superlayer
     * @return 
     */
    private static double CorrectForB(double delBf, double bfield, double tmax, 
            double xhatalpha, double Bb1, double Bb2, double Bb3, double Bb4, int superlayer) {
        //     and here's a parameterization of the change in time due to a non-zero
        //     bfield for where xhat=x/dmaxalpha where dmaxalpha is the 'dmax' for 
        //	   a track with local angle alpha (for local angle = alpha)
        double time = 0;
        if(superlayer==3 || superlayer==4) {
            double deltatime_bfield = delBf*Math.pow(bfield,2)*tmax*(Bb1*xhatalpha+Bb2*Math.pow(xhatalpha, 2)+
                     Bb3*Math.pow(xhatalpha, 3)+Bb4*Math.pow(xhatalpha, 4));
        
            //calculate the time at alpha deg. and at a non-zero bfield	          
            time += deltatime_bfield;
        }
        return time;
    }
       
    
}
