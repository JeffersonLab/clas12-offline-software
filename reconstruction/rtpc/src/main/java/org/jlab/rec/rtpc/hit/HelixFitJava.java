package org.jlab.rec.rtpc.hit;




public class HelixFitJava {

	void rwsmav(double r[], double a[], double v[], int n)
	{
	    //  Author: Martin Poppe. r[n] = a[n,n]*v[n]

	    int i, k, ind;
	    // Address in triangular matrix, row ii, column kk
	    for(i=1; i<=n; i++)
	    {
	    		r[i-1] = 0; 
	    		for(k=1; k<=n; k++)
	    		{
    				if (i >= k) { ind = (i*i-i)/2 + k; r[i-1] += a[ind-1]*v[k-1];}
    				else        { ind = (k*k-k)/2 + i; r[i-1] += a[ind-1]*v[k-1];}
	    		}
	    }

	} // End of  void rwsmav(...)
	
	void rwsmin(double v[], double b[], int n, int m, int nrank)
	{
	    // Author: V. Blobel

	    // Obtain solution of linear equations V*X = B with symmetric matrix V
	    // and inverse (for m=1) or matrix inversion only (for m=0).

	    // V = Symmetric n-by-n matrix in symmetric storage mode 
	    //   V(1) = V11,  V(2) = V12,  V(3) = V22,  V(4) = V13, ...
	    //   replaced by inverse matrix
	    // B = n-vector  (for m=0 use a dummy argument) 
	    //   replaced by a solution vector
	    // m = see above

	    // Method of solution is by elimination selecting the pivot point on the 
	    // diagonal each stage. The rank of the matrix is returned in nrank.

	    // For nrank != n, all remaining rows and columns of the resulting matrix 
	    // V and the corresponding elements of B are set to zero.


		double EPSS = (double)1.0E-6;

	    int    i, ii, ni, k=0, kk, j, jj, jl, jk, lk, l, ij;
	    double  vkk, d, e; /* variable 'c' in original fortran file is not used */
	    double[][]  dr = new double[2][200];

	    // -------- Construct table -----------
	    for(i=1; i<=n; i++) dr[0][i-1] = 1;
	    ni = n; 
	    ii = 0;
	    for(i=1; i<=n; i++) { ii += i; dr[1][i-1] = Math.abs(v[ii-1]); }


	    // --------- Loop begins ----------
	    nrank = n - ni;
	    
	    for (i=1; i<=ni; i++)
	    {
	      // --- Search for pivot and test for linearity and zero matrix
	      k = kk = jj = 0; vkk = 0;
	      for (j=1; j<=n; j++)
	      {
	    	  	jj += j;
	    	  	if (dr[0][j-1] == 0)              break;
	    	  	if (Math.abs(v[jj-1]) <= vkk)           break;
	    	  	if (Math.abs(v[jj-1]) < EPSS*dr[1][j-1]) break;
    	  		vkk = Math.abs(v[jj-1]);
    	  		k=j; kk=jj;
	      }
	      if (k == 0) {break;}

	      // --- Preparation for elimination ---
	      nrank = nrank + 1;
	      dr[0][k-1] = 0;
	      d = 1/v[kk-1];
	      v[kk-1] = -d;
	      if (m == 1) b[k-1] *= d;
	      jk = kk-k;
	      jl = 0;

	      // --- Elimination ---
	      for (j=1; j<=n; j++)
	      { 
	  			if( j-k < 0 ) jk++;
	  			else { 
	  				if ( j-k == 0) {jk = kk; jl += j; break;}
	  				else           jk = jk +j -1; 
	  			} 


	  			e = v[jk-1];
	  			v[jk-1] = d*e;

	  			if (m==1) b[j-1] -= b[k-1]*e;
	  			lk = kk-k;
	  			for (l=1; l<=j; l++)
	  			{
  					jl++;
  					if(l-k < 0) lk++;
  					else { 	if (l==k) 	{lk=kk;  break;}
  							else     	lk=lk + l - 1;
					}

					v[jl-1] -= v[lk-1]*e;
	  			}
	      } // End of loop over j
	    }   // End of loop over i

	    if(k != 0) {
	    // ----------- Change sign --------------
		    ij=0;
		    for (i=1; i<=n; i++) {
		      for (j=1; j<=i; j++)
		      {
			ij++; v[ij-1] = -v[ij-1];
		      }
		    }
		      return;
		    
	    }
	      // --------- Clear rest of the matrix -------------

	      ij=0;
	      for (i=1; i<=n; i++)
	      {
		if(m == 1  &&  dr[0][i-1] != 0) b[i-1]=0;
		for (j=1; j<=i; j++)
		{
		  ij++; 
		  if (dr[0][i-1] + dr[0][j-1] != 0) v[ij-1]=0;
		  v[ij-1] = -v[ij-1];
		}
	      }

	      	return;
	  } // End of void rwsmin(double v[], double b[], int n, int m, int* nrank

	


	  //-------------------------------------------------------------------------
	  void rwfthc(int npt,                      double rf[],
	    double pf[],  double wfi[],  
	    double zf[],  double wzf[],  
	    int iopt, 

	    double vv0[],  double ee0[],    double ch2ph,  double ch2z,
	    double del[],   double delz[])


	    // ----- Function for fast helix fit. -----

	    // A generalization of the TFTHEL routine to allow it to be called from a 
	    // routine that contains any list of x and y values xf,yf for a set of npt 
	    // points to be fitted.

	    // ----- Input: -----

	    // npt:         Number of 3-D points to be fit
	    // xf[]:        Array of X-values of points to be fit
	    // yf[]:        Array of Y-values of points to be fit
	    // rf[]:        Array of R-values of points to be fit
	    // pf[]:        Array of PHI-values of points to be fit
	    // wfi[]:       Array of 1/(sig(rphi))**2 for each point
	    // zf[]:        Array of Z-values of points to be fit
	    // wzf[]:       Array of 1/(sig(z))**2 for each point
	    // iopt:         0 -> Distance**2 =x**2 +y**2 minimized;
	    //               1 -> Weighted with 1/SIMA(R*PHI)**2
	    //               2 -> Error matrix calculated
	    //               3 -> 3-Dimensional iteration

	    // ----- Output: -----

	    // vv0[5]:      [0] = 1/r*charge, positive if clockwise;
	    //              [1] = tan(lambda)  { = dz/ds} tan(angle to (X,Y) PLANE);
	    //              [2] = phi0         {0, 2*PI} angle to X-axis at r=d0;
	    //              [3] = d0*sign      [cm] minimal distance to Z-axis,
	    //                    +ve if axis encircled;
	    //              [4] = z0           [cm]    z position at r=d0;
	    // ee0[15]:     Inverse of error matrix in triangular form;
	    // ch2ph:       chi squared = Sum(phi deviations / errors)^2;
	    // ch2z:        chi squared = Sum(z   deviations / errors)^2;
	    // del:         Unknown;
	    // delz:        Unknown.

	    // Note that the number of degrees of freedom = 2*npt-5

	    // Based on subroutine CIRCLE.
	    // Reference:  "Computer Physics Communications",  Volume 33, P. 329
	    // Authors:  N. Chernov, G. Ososkov and M. Poppe

	    // Modified by  Fred Weber, 8 Jun 1989.
	    // Translated into C by Michael Ispiryan, 2006


	  {
            int ITMAX = 15;    
            int IOWRIT = 6;
            double EPS    = (double)1.0e-16;   
            double ONEPI = (double)3.1415927;  
            double PI =   (double)3.1415927; 
            double TWOPI = (double)6.2831854;
            double PIBY2 = (double)1.57074635;
            int MAX_HITS_ON_CHAIN = 200;

	    double[]   sp2 = new double[MAX_HITS_ON_CHAIN],  vv1 = new double[5];
	    double[]   sxy = new double[MAX_HITS_ON_CHAIN],   ss0 = new double[MAX_HITS_ON_CHAIN]; 
	    double[]   eee = new double[MAX_HITS_ON_CHAIN];
	    double[]   grad = new double[5], cov= new double[15], dv= new double[5];
	    double[]   deln = new double[MAX_HITS_ON_CHAIN],  delzn = new double[MAX_HITS_ON_CHAIN];  

	    double[]  xf = new double[MAX_HITS_ON_CHAIN], yf= new double[MAX_HITS_ON_CHAIN], wf= new double[MAX_HITS_ON_CHAIN];

	    double  alf,   a0,   a1,  a2,    a22,   bet,  cur,   dd,   den;
	    double  det,   dy,    d2,   f,   fact,  fg,   f1,    g,    gam,   gam0; 
	    double  g1,    h,     h2,   p2,  q2,    rm,   rn,    xa,   xb = 0,    xd,   xi;
	    double  xm,    xx,    xy,   x1,  x2,    den2, ya,    yb,   yd,    yi,   ym; 
	    double  yy,    y1,    y2,   wn,  sa2b2, dd0,   phic;

	    int     i,     n,     iter, nrank= 0;

	    double   chi2_here,  rr0,       asym, sst, ph0,   check;
	    double   aa0,        ome,       gg0,  hh0, ff0,   sums, sumss, sumz, sumsz, sumw; 
	    double   denom,      dzds_here, zz0,  eta, dfd,   dfo,  dpd,   dpo,  ggg,   dza;
	    double   dzd,        dzo,       chi1;

	    //nkb added these variables for the recalculation of dz/ds
	    double   kangle, my_phi,     xc,        yc,   xdca;
	    double   ydca, xbar, ybar, xpt, ypt, alpha, beta; 
	    if (npt <= 2) 
	    {
	      System.out.println("BonusHelixFit::rwfthc(): Cannot fit less than 3 points; exiting..\n");  
	      return;
	    }
	    if (npt > MAX_HITS_ON_CHAIN) 
	    {
	    	System.out.println("BonusHelixFit::rwfthc(): Cannot fit more than " +  MAX_HITS_ON_CHAIN + " points; exiting..\n" );
	      return;
	    }
	    for(i=0; i<npt; i++) 
	    {
	      xf[i] = rf[i]*Math.cos(pf[i]); 
	      yf[i] = rf[i]*Math.sin(pf[i]);   
	      wf[i] = wfi[i];
	    }

	    n = npt; 
	    xm = 0.0; 
	    ym = 0.0;
	    for(i=0; i<15; i++) ee0[i]=0;
	    for(i=0; i<5;  i++) { grad[i]=0; vv0[i]=0; }
	    chi2_here = 0;
	    ch2ph    = 0;
	    ch2z     = 0;
	    for(i=0; i<n; i++) sp2[i]=wf[i]*(rf[i]*rf[i]);

	    if(iopt == 0)
	    {
	      for(i=0; i<n; i++)
	      {
		wzf[i]=1.0;
		wf[i]=1.0; 
		xm += xf[i]; 
		ym += yf[i]; 
	      }
	      rn = 1.0/(double)(n);
	    }

	    else
	    {
	      wn=0.0;
	      for(i=0; i<n; i++)
	      {
		xm += xf[i]*wf[i]; 
		ym += yf[i]*wf[i];   
		wn += wf[i];
	      }  
	      rn = 1.0/(double)(wn);
	    } // End of else


	    xm *= rn; 
	    ym *= rn; 
	    x2=0.0;
	    y2=0.0;
	    xy=0.0;
	    xd=0.0;
	    yd=0.0;
	    d2=0.0;

	    for(i=0; i<n; i++)
	    {
	      xi  = xf[i] - xm;  
	      yi  = yf[i] - ym;
	      xx  = xi*xi;       
	      yy  = yi*yi;
	      x2 += xx*wf[i];
	      y2 += yy*wf[i];
	      xy += xi*yi*wf[i];
	      dd  = xx + yy;
	      xd += xi*dd*wf[i];
	      yd += yi*dd*wf[i];  
	      d2 += dd*dd*wf[i];
	    }

	    x2 *= rn;  y2 *= rn;  xy *= rn;  d2 *= rn;  xd *= rn;  yd *= rn; 
	    f  = 3.0*x2 + y2;
	    g  = 3.0*y2 + x2;
	    fg = f*g;
	    h  = xy + xy;     h2 = h*h;
	    p2 = xd*xd;      q2 = yd*yd;   gam0 = x2 + y2; fact = gam0*gam0;
	    a2 = (fg-h2-d2)/fact;
	    fact *= gam0;
	    a1 = (d2*(f+g) - 2.0*(p2+q2))/fact;
	    fact *= gam0;
	    a0 = (d2*(h2-fg) + 2.0*(p2*g + q2*f) - 4.0*xd*yd*h)/fact;
	    a22 = a2 + a2; 
	    yb = 1.0E+30; iter=0; xa = 1.0;


	    // -------------------- Main iteration ----------------------------
	while(true) {
	    ya = a0 + xa*(a1 + xa*(a2 + xa*(xa-4.0)));
	    if (iter >= ITMAX) break;
	    dy = a1 + xa*(a22 + xa*(4.0*xa - 12.0));
	    xb = xa - ya/dy;
	    if (Math.abs(ya)    >  Math.abs(yb)) xb = 0.5*(xb+xa);
	    if (Math.abs(xa-xb) <  EPS)     break;
	    xa = xb; yb = ya; iter++; 
	}
	
	    gam = gam0*xb;
	    f1 = f - gam;
	    g1 = g - gam;
	    x1 = xd*g1 - yd*h;
	    y1 = yd*f1 - xd*h;
	    det = f1*g1 - h2;   den2 = 1.0/(x1*x1 + y1*y1 + gam*det*det);
	    if (den2 <= 0.0) {ch2ph = 1.0E+30; ch2z = 1.0E+30; return;}
	    den = Math.sqrt(den2); cur = det*den + 0.0000000001;
	    alf = -(xm*det + x1)*den;
	    bet = -(ym*det + y1)*den;
	    rm = xm*xm + ym*ym;

	    // -------  Calculation of standard circle parameters. NB: cur is 
	    // -------  always positive.
	    asym = bet*xm - alf*ym;
	    sst = 1.0;
	    if (asym<0.0) sst = -1.0;
	    rr0 = sst*cur;
	    if((alf*alf + bet*bet) <= 0.0) {ch2ph = 1.0E+30; ch2z = 1.0E+30; return;}
	    sa2b2 = 1.0/(Math.sqrt(alf*alf + bet*bet));
	    dd0 = (1.0 - 1.0/sa2b2)/cur;
	    phic = Math.asin(alf*sa2b2) + PIBY2;
	    if (bet > 0.0) phic = TWOPI - phic;
	    ph0 = phic + PIBY2;

	    if (rr0 <= 0.0)   ph0 -= ONEPI;
	    if (ph0 >  TWOPI) ph0 -= TWOPI;
	    if (ph0 <  0.0)   ph0 += TWOPI;

	    vv0[0]=rr0;  vv0[2]=ph0;  vv0[3]=dd0;
	    //printf("rr0,ph0,dd0 = %f %f %f\n",1/rr0,ph0,dd0);
	    check = sst*rr0*dd0;
	    if (check == 1.0) { dd0 -= 0.007; vv0[3] = dd0; }

	    //  ------- Calculate phi distances to measured points 
	    aa0=sst; ome=rr0; gg0=ome*dd0-aa0; hh0=1.0/gg0;
	    for(i=0; i<n; i++)
	    {
	      asym = bet*xf[i] - alf*yf[i];   ss0[i] = 1.0;
	      if (asym < 0.0) ss0[i] = -1.0;
	      //By jixie: if rf[i] == 0, there is a problem on ff0. 
	      if(rf[i] == 0.0) ff0 = 0; 
	      else ff0 = ome*(rf[i]*rf[i] - dd0*dd0)/(2.0*rf[i]*gg0) + dd0/rf[i];

	      if (ff0 < -1.0) ff0 = -1.0;
	      if (ff0 >  1.0) ff0 =  1.0;

	      del[i] = ph0 + (ss0[i]-aa0)*PIBY2 + ss0[i]*Math.asin(ff0) - pf[i];
	      if (del[i] >  ONEPI) del[i] -= TWOPI;
	      if (del[i] < -ONEPI) del[i] += TWOPI;
	    }

	    
	    // -------- Fit straight line in S-Z
	    for(i=0; i<n; i++)
	    {
	      eee[i] = 0.5*vv0[0] * 
		       Math.sqrt(Math.abs( (rf[i]*rf[i] - vv0[3]*vv0[3]) / 
		                   (1.0-aa0*vv0[0]*vv0[3])         ));
	      //printf("eee[%d] = %f\n",i,eee[i]);
	      if (eee[i] >  0.9999) 
	      { 
		//quiet = FALSE;
		//fprintf(stderr, "+Track circles too much for this code(eee=%f); bad dzds\n",eee[i]);
		//badarg = TRUE;//break;
		//printf("eee[%d] = %f\n",i,eee[i]);
		eee[i] =  0.9999;
	      }
	      if (eee[i] < -0.9999) 
	      {
		//quiet = FALSE;
		//fprintf(stderr, "-Track circles too much for this code(eee=%f); bad dzds\n",eee[i]);
		//badarg = TRUE;//break;
		//printf("eee[%d] = %f\n",i,eee[i]);
		eee[i] = -0.9999;
	      }

	      sxy[i] = 2.0*Math.asin(eee[i])/ome;
	      //printf("original sxy[%d] = %f\n",i,sxy[i]);
	    }
	    
	    //if(badarg)
	    {
	      /*
	      for(i=0; i<n; i++)
	      {
	      //printf("rf[%d] = %f; pf[%d] = %f; zf[%d] = %f; wfi[%d] = %f;\n",
	      //i, rf[i], i, pf[i], i, zf[i],i, wfi[i]); 
	      printf("original sxy[%d] = %f, eee = %f\n",i,sxy[i], eee[i]);
	      }*/

	      //nate's attempt to use the points' arc distance from the dca as the parameter 's'
	      //we only use this method if the argument of the arcsin is out of range
	      my_phi = ph0 + PI;
	      if (vv0[0]<0.0) my_phi+=PI;
	      if(my_phi>2.0*PI) my_phi-=2.0*PI;
	      xc   = -Math.sin(my_phi)*((-vv0[3])+Math.abs(1.0/vv0[0]));
	      yc   =  Math.cos(my_phi)*((-vv0[3])+Math.abs(1.0/vv0[0]));
	      xdca = -Math.sin(my_phi)*(-vv0[3]);
	      ydca =  Math.cos(my_phi)*(-vv0[3]);
	      xbar = xdca - xc;
	      ybar = ydca - yc;
	      //printf("xdca= %.1f ydca= %.1f xc= %.1f yc= %.1f xbar= %.1f ybar= %.1f\n",
	      //	    xdca, ydca, xc, yc, xbar, ybar);
	      for(i=0; i<n; i++)
	      {
		/*//using law of cosines to determine s coordinate
		mydd =  (xf[i]-Math.abs(dd0)*Math.cos(ph0))*(xf[i]-Math.abs(dd0)*Math.cos(ph0)) 
		+ (yf[i]-Math.abs(dd0)*Math.sin(ph0))*(yf[i]-Math.abs(dd0)*Math.sin(ph0));
		eee[i] = 1 - mydd*rr0*rr0/2;
		if(Math.abs(eee[i]) > 1.0) 
		{
		//quiet = FALSE;
		//printf("eee[%d] = %f, rad = %f, mydd = %f\n",i,eee[i],1/rr0,Math.sqrt(mydd));
		//getchar();
		}
		sxy[i] = (1/(sst*rr0))*acos(eee[i]);
		//printf("law of Math.cos sxy[%d] = %f\n",i,sxy[i]);
		*/
		//ksxy = sxy[i];
		xpt = xf[i] - xc;
		ypt = yf[i] - yc;
		alpha = Math.atan2(ypt,xpt);
		beta  = Math.atan2(ybar,xbar); //if(alpha > 2*PI)alpha -= 2*PI;
		//if(alpha < 0)   alpha += 2*PI;
		//if(beta > 2*PI) beta  -= 2*PI;
		//if(beta < 0)    beta  += 2*PI;
		//printf("alpha = %.2f beta = %.2f\n",alpha*180./PI,beta*180./PI);
		sxy[i] = beta - alpha;
		if(sxy[i] > PI) sxy[i] = sxy[i] - 2*PI;
		if(sxy[i] < -PI)sxy[i] = sxy[i] + 2*PI;
		//if(sxy[i] < 0)   sxy[i] += 2*PI;
		kangle = sxy[i];
		sxy[i] = (1/rr0)*sxy[i];

		//printf("[%d] xf= %.1f yf= %.1f xpt= %.1f ypt= %.1f\n",
		//i, xf[i], yf[i], xpt, ypt); 
		//HFILL(9916, ksxy - sxy[i], 0.0, 1.0);
		//printf("%f\n",ksxy-sxy[i]);
	      }

	    }


	    sums = 0.0;
	    sumss = 0.0;
	    sumz =  0.0;
	    sumsz =  0.0;
	    sumw = 0.0;
	    for(i=0; i<n; i++)
	    {
	      sumw   += wzf[i];
	      sums   += sxy[i]*wzf[i];
	      sumss  += sxy[i]*sxy[i]*wzf[i];
	      sumz   += zf[i]*wzf[i];
	      sumsz  += zf[i]*sxy[i]*wzf[i];
	    }

	    denom = sumw*sumss - sums*sums;
	    if (Math.abs(denom) < 1.0E-6) 
	    { 
	      if (denom >= 0.0)  denom =  1.0E-6; 
	      else               denom = -1.0E-6; 
	    }

	    dzds_here = (sumw*sumsz - sums*sumz)  / denom;
	    zz0  = (sumss*sumz - sums*sumsz) / denom;
	    vv0[1] = dzds_here;  vv0[4] = zz0;

	    // --------- Calculation of chi**2
	    for(i=0; i<n; i++)
	    {
	      delz[i]   = zz0 + dzds_here*sxy[i] - zf[i];
	      ch2ph   += sp2[i]*del[i]*del[i];
	      ch2z    += wzf[i]*delz[i]*delz[i];
	      chi2_here = ch2ph + ch2z;
	    }

	    if (iopt < 2) return;


	    // ----- Calculation of the error matrix -------
	    for(i=0; i<n; i++)
	    {
	      ff0 = ome*(rf[i]*rf[i] - dd0*dd0) / (2.0*rf[i]*gg0) + dd0/rf[i];
	      if (ff0 >  0.9999)  ff0 =  0.9999;
	      if (ff0 < -0.9999)  ff0 = -0.9999; 
	      eta = ss0[i] / Math.sqrt(Math.abs(1.0+ff0)*(1.0-ff0));
	      dfd = (1.0 + hh0*hh0*(1.0-ome*ome*rf[i]*rf[i])) / (2.0*rf[i]);
	      dfo = -aa0*(rf[i]*rf[i] - dd0*dd0)*hh0*hh0 / (2.0*rf[i]);
	      dpd = eta*dfd;  dpo = eta*dfo; 
	      // --- Derivatives of z component
	      ggg = eee[i] / Math.sqrt(Math.abs( (1.0+eee[i])*(1.0-eee[i])));
	      dza = sxy[i];
	      check = rf[i]*rf[i] - vv0[3]*vv0[3];
	      if (check == 0.0) check = 2.0*0.007;
	      dzd = 2.0*(vv0[1]/vv0[0]) * Math.abs(ggg) * (0.5*aa0*vv0[0] /
		(1.0 - aa0*vv0[3]*vv0[0]) - vv0[3]/check);
	      dzo = -vv0[1]*sxy[i]/vv0[0] + vv0[1]*ggg/(vv0[0]*vv0[0]) * 
		(2.0 + aa0*vv0[0]*vv0[3]/(1.0 - aa0*vv0[0]*vv0[3]));

	      // ---- Error matrix
	      ee0[0]  += sp2[i]*dpo*dpo + wzf[i]*dzo*dzo;
	      ee0[1]  +=                wzf[i]*dza*dzo;
	      ee0[2]  +=                wzf[i]*dza*dza;
	      ee0[3]  += sp2[i]*dpo;
	      ee0[5]  += sp2[i];
	      ee0[6]  += sp2[i]*dpo*dpd + wzf[i]*dzo*dzd;
	      ee0[8]  += sp2[i]*dpd; 
	      ee0[9]  += sp2[i]*dpd*dpd + wzf[i]*dzd*dzd;
	      ee0[10] +=                    wzf[i]*dzo;
	      ee0[11] +=                    wzf[i]*dza;
	      ee0[13] +=                    wzf[i]*dzd;
	      ee0[14] +=                    wzf[i];

	      // --- Gradient vector
	      grad[0] += -del[i]*sp2[i]*dpo - delz[i]*wzf[i]*dzo; 
	      grad[1] +=                  - delz[i]*wzf[i]*dza;
	      grad[2] += -del[i]*sp2[i];
	      grad[3] += -del[i]*sp2[i]*dpd - delz[i]*wzf[i]*dzd;
	      grad[4] +=                  - delz[i]*wzf[i];   
	    } // End of for(i...)


	    if (iopt < 3) return;


	    // --------------- Newton's next guess
	    for(i=0; i<15; i++) cov[i] = ee0[i];

	    rwsmin(cov, vv1, 5, 0, nrank);
	    rwsmav(dv,  cov, grad, 5);

	    for(i=0; i<5; i++) vv1[i] = vv0[i] + dv[i];

	    //------- New differences in phi and z
	    gg0 = vv1[0]*vv1[3] - aa0;
	    for(i=0; i<n; i++)
	    {
	      ff0 = vv1[0]*(rf[i]*rf[i] - vv1[3]*vv1[3]) / 
		(2.0*rf[i]*gg0) + vv1[3]/rf[i];

	      if (ff0 >  1.0) ff0 =  1.0;
	      if (ff0 < -1.0) ff0 = -1.0;

	      deln[i] = vv1[2] + (ss0[i]-aa0)*PIBY2 + ss0[i]*Math.asin(ff0) - pf[i];
	      if (deln[i] >  ONEPI) deln[i] -= TWOPI;
	      if (deln[i] < -ONEPI) deln[i] += TWOPI;
	      eee[i] = 0.5*vv1[0]*Math.sqrt(Math.abs( (rf[i]*rf[i] - vv1[3]*vv1[3]) / 
		(1.0 - aa0*vv1[0]*vv1[3]) ));
	      if (eee[i] >  0.9999) eee[i] =  0.9999;
	      if (eee[i] < -0.9999) eee[i] = -0.9999;
	      sxy[i]   = 2.0*Math.asin(eee[i]) / vv1[0];
	      delzn[i] = vv1[4] + vv1[1]*sxy[i] - zf[i];
	    } 

	    // ---------- Calculation of chi**2
	    chi1 = ch2ph = ch2z = 0.0;
	    for(i=0; i<n; i++)
	    {
	      chi1  += sp2[i]*deln[i]*deln[i] + wzf[i]*delzn[i]*delzn[i];
	      ch2ph += sp2[i]*deln[i]*deln[i];
	      ch2z  += wzf[i]*delzn[i]*delzn[i]; 
	    }

	    if (chi1 < chi2_here) { for(i=0; i<5; i++) vv0[i] = vv1[i]; }
	    return;
	  }
	  
	  void CorrHelixRPhi(double Rho, double Phi)
	  {
	    //std::cout<<"\nBefore CorrHelixRPhi():  R="<<Rho<<"  Phi="<<Phi<<std::endl;

	    /*******************************************   
	    ph_hel-ph0:r_hel          
	    Minimizer is Linear 
	    Minimizer is Linear
	    Chi2                      =      295.674
	    NDf                       =           70
	    p0                        =     0.753136   +/-   0.012418
	    p1                        =    -0.391614   +/-   0.00821305
	    p2                        =    0.0836029   +/-   0.00212053
	    p3                        =  -0.00944999   +/-   0.000278826
	    p4                        =  0.000611348   +/-   2.03734e-05
	    p5                        = -2.26783e-05   +/-   8.35706e-07
	    p6                        =  4.47764e-07   +/-   1.79782e-08
	    p7                        = -3.64278e-09   +/-   1.57649e-10
	    double Para_dPhiVsR[] = {0.753136, -0.391614, 0.0836029, -0.00944999,
	    0.000611348, -2.26783e-05, 4.47764e-07, -3.64278e-09};
	    //this is the 1st iteration, it has some problem at r<2.0, its deviation 
	    //goes up to 0.1 rad
	    //below is the 2nd iteration
	    Minimizer is Linear
	    Chi2                      =       60.333
	    NDf                       =           17
	    p0                        =      11.0937   +/-   0.949287
	    p1                        =     -17.3801   +/-   1.61812
	    p2                        =      11.0492   +/-   1.11925
	    p3                        =     -3.65267   +/-   0.40273
	    p4                        =     0.663338   +/-   0.0796389
	    p5                        =   -0.0628701   +/-   0.00821993
	    p6                        =   0.00243461   +/-   0.000346527
	    c1->SaveAs("dPhiVSR_pol7_2nd.png")
	    *///****************************************

	    double R=Math.abs(Rho);
	    if(R>24.0) R=24.;
	    if(R<1.5) R=1.5; 
	    double Para_dPhiVsR[] = {0.753136, -0.391614, 0.0836029, -0.00944999,
	      0.000611348, -2.26783e-05, 4.47764e-07, -3.64278e-09};
	    double dPhi = Para_dPhiVsR[0];
	    for(int i=1;i<=7;i++) dPhi += Para_dPhiVsR[i]*Math.pow(R,i);
	    Phi = Phi - dPhi;
	    //apply 2nd correction if R<5.5 cm
	    if(R<5.5) {
	      double Para_dPhiVsR_2nd[] = {11.0937, -17.3801, 11.0492, -3.65267,
	        0.663338, -0.0628701, 0.00243461};
	      double dPhi_2nd = Para_dPhiVsR_2nd[0];
	      for(int i=1;i<=6;i++) dPhi_2nd += Para_dPhiVsR_2nd[i]*Math.pow(R,i);
	      Phi = Phi - dPhi_2nd;
	    }

	    return;

	    /******************************************
	    r_hel-rho_1st:r_hel, only good for 1.0<r<14.5          
	    Minimizer is Linear
	    Chi2                      =      309.129
	    NDf                       =           36
	    p0                        =       8.2822   +/-   0.315595
	    p1                        =     -8.35421   +/-   0.352006
	    p2                        =      3.32568   +/-   0.158319
	    p3                        =    -0.685895   +/-   0.0371099
	    p4                        =    0.0797764   +/-   0.00489482
	    p5                        =  -0.00527024   +/-   0.000364195
	    p6                        =  0.000184178   +/-   1.42139e-05
	    p7                        = -2.64062e-06   +/-   2.2562e-07  
	    *///****************************************
	    /*R=Math.abs(Rho);
	    if(R>14.5) R=14.5;
	    if(R<14.50001) {
	      double Para_dRVsR[] = { 8.2822, -8.35421, 3.32568, -0.685895, 0.0797764,
	        -0.00527024, 0.000184178, -2.64062e-06};
	      double dR = Para_dRVsR[0];
	      for(int i=1;i<=7;i++) dR += Para_dRVsR[i]*Math.pow(R,i);
	      R = R - dR;
	      Rho = (Rho<0)? -R : R;*/
	      //std::cout<<"\t\t dR="<<dR<<std::endl;
	    }

	    //std::cout<<"After  CorrHelixRPhi():  R="<<Rho<<"  Phi="<<Phi<<std::endl;

	  

	  /*------------------------------------------------------------------------\
	  ///////////////////////////////////////////
	  //By Jixie:  this helix fit does not work if the track curve back
	  //User should simply remove those points 
	  //
	  //Function name: void (int PointNum,double szPos[][3], 
	  //  double Rho, double A, double B,double Phi, double Theta, 
	  //  double X0, double Y0,double Z0, double &DCA, double &chi2);
	  //
	  //  Calculate the raidus of the trajectory
	  //
	  //Input parameters:
	  //  PointNum:       number of x-y points
	  //  szPos[any number>3][3]:  xyz array, 
	  //OutPut: 
	  //  Rho:       Radius, positive is clockwise, 
	  //  A,B:       helix center X,Y position
	  //  Theta,Phi: theta and phi angle at the initial step
	  //  X0,Y0,Z0:  vertex position (x,y,z) for inital step
	  //  DCA:       distance of closest approach to beamline
	  //  Chi2:      ch2ph+ch2z/(npt-5) 
	  \------------------------------------------------------------------------*/
	  HelixFitObject helix_fit(int PointNum,double szPos[][],int fit_track_to_beamline)// double Rho, double A, double B,
	    //double Phi, double Theta, double X0, double Y0,double Z0,  
	    //double DCA, double Chi2,int fit_track_to_beamline)
	  {
             double Rho;
             double A;
             double B;
             double Phi;
             double Theta;
             double X0;
             double Y0;
             double Z0;
             double DCA;
             double Chi2;
	     int kMaxHit = 200;  //set it to 200 to match that one in rwfthc.cc
	     double PI = Math.acos(0.0)*2.0;

	    int jj;
	    double my_phi;
	    double[] rf = new double[kMaxHit];
	    double[] pf= new double[kMaxHit];
	    double[] wfi= new double[kMaxHit];
	    double[] zf= new double[kMaxHit];
	    double[] wzf= new double[kMaxHit];
	    int iopt;
	    int npt;
	    double[] vv0= new double[5];
	    double[] ee0= new double[15];
	    double ch2ph = 0;
	    double ch2z = 0;
	    double[] del= new double[kMaxHit];
	    double[] delz= new double[kMaxHit];

	    double phi0;
	    if(PointNum>=kMaxHit) PointNum=kMaxHit-1;

	    npt = PointNum;
	    if(npt<5) return new HelixFitObject();


	    for (jj=0; jj<npt; jj++)
	    {// r,phi,z coordinate

	      rf[jj] = Math.sqrt(Math.pow(szPos[jj][0],2)+Math.pow(szPos[jj][1],2));
	      pf[jj] = Math.atan(szPos[jj][1]/szPos[jj][0]); //phi angle
	      if(szPos[jj][1]>0 && szPos[jj][0]<0) pf[jj] +=PI;
	      if(szPos[jj][1]<0 && szPos[jj][0]<0) pf[jj] +=PI;
	      if(szPos[jj][1]<0 && szPos[jj][0]>0) pf[jj] +=2*PI;
	      if(pf[jj]>2*PI)    pf[jj] -=2*PI;
	      zf[jj] = szPos[jj][2];
	      wfi[jj]= 1.0;
	      wzf[jj]= 1.0;
	    }

	    if(fit_track_to_beamline == 1)
	    {
	      rf[npt]= 0.0001;  //rf=0 will cause chi2=nan problem
	      pf[npt]= 0.0;
	      zf[npt]= 0.0; 
	      wfi[npt]= 1.0;
	      wzf[npt]= 0.0; /* zero weight for Z on the beamline point*/
	      //This means that don't calculate the chi square for Z on the beamline point
	      npt++;
	    }
	    iopt=1; /* tells rwfthl what kind of fit to do */

	    rwfthc(npt,rf,pf,wfi,zf,wzf,iopt,vv0,ee0,ch2ph,ch2z,del,delz);
	    /*
	    OUTPUT:      VV0 = 1/R*CHARGE   POS. IF CLOCKWISE
	    C                  TAN(LAMBDA)  {=DZ/DS}TAN(ANGLE TO X,Y PLANE)
	    C                  PHI0         {0,2PI} ANGLE TO X-AXIS at R=D0
	    C                  D0*SIGN      [CM]    MINIMAL DIST. TO Z-AXIS,
	    C                                       +VE IF AXIS ENCIRCLED
	    C                  Z0           [CM]    Z POS AT R=D0
	    */
	    //reconstruct the output
	    Rho  = (double)(1.0/vv0[0]); /* minimum distance to z=0 */
	    phi0 = vv0[2]; /* in xy plane, direction of track relative to x axis */

	    //This is from Nate, it is based on the definition of VV0
	    my_phi = phi0+PI;
	    if (vv0[0]<0.0) my_phi += PI;
	    //vv0[0] negtive means curve to anti-CLOCKWISE direction, This is true in BONUS
	    if(my_phi >  PI) my_phi -= 2.0*PI;
	    if(my_phi < -PI) my_phi += 2.0*PI;


	    //center of the circle
	    A = (double)(-Math.sin(my_phi)*((-vv0[3])+Math.abs(1.0/vv0[0])));
	    B = (double)(Math.cos(my_phi)*((-vv0[3])+Math.abs(1.0/vv0[0])));
	    //position of the initial step
	    Phi = (double)vv0[2];
	    if(Phi> PI) Phi-=2*PI;
	    if(Phi<-PI) Phi+=2*PI;
	    Theta = PI/2.-Math.atan(vv0[1]);
	    X0 = -Math.sin(my_phi)*(-vv0[3]);
	    Y0 =  Math.cos(my_phi)*(-vv0[3]);
	    Z0 = (double)vv0[4];
	    DCA = Math.abs(vv0[3]); /* dca = distance of closest approach to beamline */
	    Chi2 = (npt>5)? (double)(ch2ph+ch2z/(npt-5)) : 9999.9;
	    
	    //By Jixie: apply correction, only useful for RTPC12 
	    //user should provide the next subroutine, it is detector dependence thing
	    //CorrHelixRPhi(Rho,Phi);
	    return new HelixFitObject(Rho,A,B,Phi,Theta,X0,Y0,Z0,DCA,Chi2);

	  }

	  /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	  HelixFitObject HelixFit(int PointNum, double szPos[][], int fit_track_to_beamline)// double R, double A, double B,
	    //double Phi_deg, double Theta_deg, double Z0, int fit_track_to_beamline )
	  {
	    double PI=Math.acos(0.0)*2;
	    //double Rho=0,Phi=0,Theta=0,X0=0,Y0=0,DCA=0,Chi2=0;
            double Phi_deg;
            double Theta_deg;
            
	    HelixFitObject h = helix_fit(PointNum, szPos, fit_track_to_beamline);
	    
	    Phi_deg=Math.toDegrees(h.get_Phi());
            if(Phi_deg >= 180){
                Phi_deg -= 360;
            }
            if(Phi_deg < -180){
                Phi_deg += 360;
            }
	    Theta_deg=Math.toDegrees(h.get_Theta()); 
            h.set_Phi(Phi_deg);
            h.set_Theta(Theta_deg);
            

            //System.out.println("DOCA " + h.get_DCA());
	    return h;
	  }

}
