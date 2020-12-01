package org.jlab.rec.vtx;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

public class Vertex {
	
	public Vertex() {
		// TODO Auto-generated constructor stub
	}
	
	private Point3D _Vertex;
	private List<ArrayList<TrackParsHelix>> _HelixPairs = new ArrayList<ArrayList<TrackParsHelix>>();	

	public Point3D get_Vertex() {
		return _Vertex;
	}
	public void set_Vertex(Point3D _Vertex) {
		this._Vertex = _Vertex;
	}

	public List<ArrayList<TrackParsHelix>> get_HelixPairs() {
		return _HelixPairs;
	}
	public void set_HelixPairs(List<ArrayList<TrackParsHelix>> _HelixPairs) {
		this._HelixPairs = _HelixPairs;
	}

	
	static void combinations2(int[] arr, List<int[]> list){
		
        for(int i = 0; i<arr.length; i++)
            for(int j = i+1; j<arr.length; j++)
                list.add(new int[]{arr[i],arr[j]});
    }	
	
	
	public void FindHelixPairs(List<TrackParsHelix> allHelices) {
		
		List<ArrayList<TrackParsHelix>> allHelixPairs = new ArrayList<ArrayList<TrackParsHelix>>();	
		List<int[]> list = new ArrayList<int[]>();
		int[] arr = new int[allHelices.size()];
		for(int i =0; i< allHelices.size(); i++) {
			arr[i] = i;
		}
		combinations2(arr,list);	

		for(int i=0; i< list.size(); i++) {
			int h1Idx = list.get(i)[0];
			int h2Idx = list.get(i)[1];
			ArrayList<TrackParsHelix> helixPair = new ArrayList<TrackParsHelix>(2);
			helixPair.add(allHelices.get(h1Idx));
			helixPair.add(allHelices.get(h2Idx));
			allHelixPairs.add(helixPair);
		}
		this.set_HelixPairs(allHelixPairs);
	}
	
	
	public double[] InterpolateBetwHelices(ArrayList<TrackParsHelix> helixPair) {
		
		TrackParsHelix h1 = helixPair.get(0);
		TrackParsHelix h2 = helixPair.get(1);

	    int nsteps=50;
	    double step = 0.005;    		
	    double minD = 9999.;
	    double phiMin1 = 9999.;
	    double phiMin2 = 9999.;	    
	    
	    // loop through the points of both helix curves 
	    // and find two points with the minimal distance between them.
		for(double phi1=-(nsteps*step) ; phi1<(nsteps*step); phi1+=step) {
		for(double phi2=-(nsteps*step) ; phi2<(nsteps*step); phi2+=step) {
			double d = h1.calcPoint(phi1).distance(h2.calcPoint(phi2));
			if(d<minD) { minD = d; phiMin1=phi1; phiMin2=phi2; }
		}
		}
		
		// repeat previous loops around phiMin1 and phiMin2 with smaller step
		nsteps=15;
		step = step/10.;
		for(double phi1=phiMin1-(nsteps*step) ; phi1<phiMin1+(nsteps*step); phi1+=step) {
		for(double phi2=phiMin2-(nsteps*step) ; phi2<phiMin2+(nsteps*step); phi2+=step) {
			double d = h1.calcPoint(phi1).distance(h2.calcPoint(phi2));
			if(d<minD) { minD = d; phiMin1=phi1; phiMin2=phi2; }
		}
		}

		// one more iteration
		nsteps=15;
		step = step/10.;
		for(double phi1=phiMin1-(nsteps*step) ; phi1<phiMin1+(nsteps*step); phi1+=step) {
		for(double phi2=phiMin2-(nsteps*step) ; phi2<phiMin2+(nsteps*step); phi2+=step) {
			double d = h1.calcPoint(phi1).distance(h2.calcPoint(phi2));
			if(d<minD) { minD = d; phiMin1=phi1; phiMin2=phi2; }
		}
		}
				
		// the line connecting the points of two helixes with minimal distance between them
		Line3D intrxLine = new Line3D(h1.calcPoint(phiMin1), h2.calcPoint(phiMin2));
		Point3D intrxPoint = intrxLine.midpoint();		
		double intrx[] = {intrxPoint.x(), intrxPoint.y(), intrxPoint.z()};
	    return intrx;
	
	} // end InterpolateBetwHelices()
	
	
	
	
	//
	//  Interpolation method based on bhabha.f routine.
	//  It does not work now.
	//
	/*
	// find the z coord. and track azimuth corres. to the
	// point [xloc,yloc] for the helix hx1
	private double[] zatxy(double[] hx1,double xloc,double yloc){

        double zloc;
        double philoc;
        double  cosref,sinref,rvert,cvert,svert,cdphi,sdphi,dphi;

		double   pival,halfpi,twopi;
	    pival  = 3.14159265359 ;
        twopi  = 6.28318530718 ;
        halfpi = 1.57079632680 ;

		cosref   = Math.cos( hx1[4] );
		sinref   = Math.sin( hx1[4] );
		rvert    = hx1[3];
		cvert    = ( xloc-hx1[1] )/rvert;
		svert    = ( yloc-hx1[2] )/rvert;
		cdphi    = cvert*cosref + svert*sinref;
		sdphi    = svert*cosref - cvert*sinref;
		dphi     = Math.atan2( sdphi,cdphi );
		zloc     = hx1[6] + dphi*hx1[5];
		philoc   = hx1[4] + dphi - hx1[7]*halfpi;

		if( philoc  < -pival ) philoc  = philoc  + twopi;
		if( philoc  >  pival ) philoc  = philoc  - twopi;
	
		double[] result = new double[2];
		result[0]=zloc;
		result[1]=philoc;

		return result;
	}
	
	
	private double[] initHelix(double dca,double phi_dca,double rho,double z_0,double tan_lambda, int charge) {
        
        double  helix[]= new double [8];
        
        double x0,y0,z0,phi0;
        x0 = dca*Math.cos(phi_dca);
        y0 = dca*Math.sin(phi_dca);
        z0 = z_0;
        phi0=phi_dca;
                
        double xzero   =  x0;     //there is dca=sqrt(x0^2+y0^2)  // ! x|
        double yzero   =  y0;     // ! y| coord. of refce. point
        double zzero   =  z0;     //perhaps, it is our z_0   // ! z|
        double phitan  =  phi0;   //phi_dca???  // ! track azimuth at ref.point
        double chgsign =  charge; //there is // ! charge of track
        double pzsign  =  1.0;
        if (tan_lambda<0) pzsign  =  -1.0;

        double pival  = 3.14159265359 ;
        double halfpi = 1.57079632680;
        double phicen  = phitan-chgsign*halfpi;  // ! azimuth of centre w.r.t.

        double xcen    = xzero + rho*Math.cos(phicen);  // ! x coord. of centre
        double ycen    = yzero + rho*Math.sin(phicen);  // ! y coord. of centre
        double phiref  = phicen + chgsign*pival;        //! azimuth of ref. pt. w.r.t.
        double dzdphi  = rho*tan_lambda*(-chgsign);     //CHECK IT!          ! turning rate cm/radian

        helix[0] = xcen;    //to calc. // ! x coord. of center of circle
        helix[1] = ycen;    //to calcю // ! y coord. of center of circle
        helix[2] = rho;     //like our rho                  // !             radius of circle
        helix[3] = phiref;  //like our phi_dca, check it!   // ! reference phi w.r.t. center
        helix[4] = dzdphi;  //its rho*tan_lambda*(-chgsign) // ! rate of change of z w.r.t. phi
        helix[5] = zzero;   //perhaps, it is our z_0        // ! reference z corres. to phiref
        helix[6] = chgsign; //?                             // ! sign of charge
        helix[7] = pzsign;  //?                     
        
        return helix;
    
    }
	
	
	public double[] InterpolateBetwHelices(ArrayList<TrackParsHelix> helixPair) {
		// Using the variables below find the point of closes approach between 2 helixes:
		//dca ;	 		// distance of closest approach to the z-axis in the lab frame
		//phi_dca;		// azimuth at the DOCA
		//rho ;         	// track curvature = 1/R, where R is the radius of the circle 
		//z_0 ;	       		// intersection of the helix axis with the z-axis
		//tan_lambda ;		// tangent of the dip angle  
		// first helix
		int charge1 = Math.round((float)helixPair.get(0).get_q());
		double dca1 = helixPair.get(0).get_dca();
		double phi_dca1 = helixPair.get(0).get_phi_dca();
		double rho1 = helixPair.get(0).get_rho();
		double z_01 = helixPair.get(0).get_z_0();
		double tan_lambda1 = helixPair.get(0).get_tan_lambda();
		double xref1 = helixPair.get(0).get_x();
		double yref1 = helixPair.get(0).get_y();
		double zref1 = helixPair.get(0).get_z();		
		// second helix
		int charge2 = Math.round((float)helixPair.get(1).get_q());
		double dca2 = helixPair.get(1).get_dca();
		double phi_dca2 = helixPair.get(1).get_phi_dca();
		double rho2 = helixPair.get(1).get_rho();
		double z_02 = helixPair.get(1).get_z_0();
		double tan_lambda2 = helixPair.get(1).get_tan_lambda();
		
		
    	double  distint;    
    	//int icase;
    	double   fr1,fr2,r1sq,r2sq,x1sq,x2sq,
                 y1sq,y2sq,sx12,sy12,drsq,tdsq,bint,cint,
                 dprim,dint,yint,xi1,xi2,yi1,yi2,dxi1,dyi1,
                 dcen,zi11,zi12,zi21,zi22,phii11,phii12,phii21,
                 phii22,dz1,dz2;

    	double  hx1[] = initHelix(dca1,phi_dca1,rho1,z_01,tan_lambda1,charge1);
    	double  hx2[] = initHelix(dca2,phi_dca2,rho2,z_02,tan_lambda2,charge2);

        //System.out.println(" HEL1 " +dca1+" "+phi_dca1+" "+rho1+" "+z_01+" "+tan_lambda1+" "+charge1);
        //System.out.println("   L1 " +hx1[0]+" "+hx1[1]+" "+hx1[2]+" "+hx1[3]
        //		                +" "+hx1[4]+" "+hx1[5]+" "+hx1[6]+" "+hx1[7]);
    	
  		double x1   = hx1[0];
  		double y1   = hx1[1];
  		double r1   = hx1[2];
  		double x2   = hx2[0];
  		double y2   = hx2[1];
  		double r2   = hx2[2];

  		double dx12 = x1 - x2;
  		double dy12 = y1 - y2;
  		double dsq  = Math.pow(dx12,2) + Math.pow(dy12,2);
  		double d12  = Math.sqrt( dsq );
  		
        //System.out.println("   d1 " +x1+" "+x2+" "+y1+" "+y2+" "+d12);
        //System.out.println("   rr " +r1+" "+r2+" "+d12);
  		
  		
  		//double  pint1[]= new double[4];
  		pint1 = new double[4];
  		pint1[0]=-1; pint1[1]=-1; pint1[2]=-1; pint1[3]=-1;
  		pint2 = new double[4];
  		pint2[0]=-1; pint2[1]=-1; pint2[2]=-1; pint2[3]=-1;
  		double[] pint = new double[3];
  		pint[0]=-1; pint[1]=-1; pint[2]=-1;
  		
  		            
  		double xh1=-2, yh1=-2, zh1=-2, phih1=-2;
  		double xh2=-2, yh2=-2, zh2=-2, phih2=-2;

  		int case_me=0;
  		double  result_zatxy[] = new double [2];
  
  		// no intersection   - one circle outside the other
  		if ( d12>(r1+r2) ) {
    	//System.out.println(" AA01 ");
  			
  		case_me = 1;
  		//icase = 1;

  		fr1  = r1/d12;
  		fr2  = r2/d12;
  		xh1  = x1 - fr1*dx12;
  		yh1  = y1 - fr1*dy12;
  		xh2  = x2 + fr2*dx12;
  	    yh2  = y2 + fr2*dy12;

  		result_zatxy=zatxy(hx1,xh1,yh1);
  		zh1=result_zatxy[0];
  		phih1=result_zatxy[1];
  
  		result_zatxy=zatxy(hx2,xh2,yh2);
  		zh2=result_zatxy[0];
  		phih2=result_zatxy[1];

  		pint1[0] = xh1;
  		pint1[1] = yh1;
  		pint1[2] = zh1;
  		pint1[3] = phih1;
  		pint2[0] = xh2;
  		pint2[1] = yh2;
  		pint2[2] = zh2;
  		pint2[3] = phih2;
  		distint = Math.sqrt(Math.pow((xh1-xh2),2) + Math.pow((yh1-yh2),2) + Math.pow((zh1-zh2),2) );
  		pint[0] = 0.5*(xh1+xh2);
  		pint[1] = 0.5*(yh1+yh2);
  		pint[2] = 0.5*(zh1+zh2);  		
  		return pint;
  		
  		} // end if()

  		// no intersection   - one circle  inside the other
  		if (((d12 + r1)<r2 )|| ((d12+r2)<r1 )) {
  	    System.out.println(" AA02 ");
  		case_me=1;
  		fr1 = r1/d12;
  		fr2  = r2/d12;
  
  if (r2<=r1 ){
    xh1   = x1 - fr1*dx12;
    yh1   = y1 - fr1*dy12;
    xh2   = x2 - fr2*dx12;
    yh2   = y2 - fr2*dy12;
  
    result_zatxy=zatxy(hx1,xh1,yh1);
    zh1=result_zatxy[0];
    phih1=result_zatxy[1]; 
  
    result_zatxy=zatxy(hx2,xh2,yh2);
    zh2=result_zatxy[0];
    phih2=result_zatxy[1];

	   System.out.println(" INT B " + xh1 +" "+yh1+" "+zh1 +" "+ xh2 +" "+yh2+" "+zh2);
    pint1[0] = xh1;
    pint1[1] = yh1;
    pint1[2] = zh1;
    pint1[3] = phih1;
    pint2[0]= xh2;
    pint2[1] = yh2;
    pint2[2] = zh2;
    pint2[3] = phih2;
    distint    = Math.sqrt(Math.pow((xh1-xh2),2) + Math.pow((yh1-yh2),2) + Math.pow((zh1-zh2),2) );
		pint[0] = 0.5*(xh1+xh2);
		pint[1] = 0.5*(yh1+yh2);
		pint[2] = 0.5*(zh1+zh2);  		
		return pint;

  }
    if (r2>r1){
      System.out.println(" AA03 ");
      xh2   = x2 + fr2*dx12;
      yh2   = y2 + fr2*dy12;
      xh1   = x1 + fr1*dx12;
      yh1   = y1 + fr1*dy12;
      
      result_zatxy=zatxy(hx1,xh1,yh1);
      zh1=result_zatxy[0];
      phih1=result_zatxy[1];
  
      result_zatxy=zatxy(hx2,xh2,yh2);
      zh2=result_zatxy[0];
      phih2=result_zatxy[1];

 	   System.out.println(" INT B " + xh1 +" "+yh1+" "+zh1+" "+xh2 +" "+yh2+" "+zh2);
      pint1[0] = xh1;
      pint1[1] = yh1;
      pint1[2] = zh1;
      pint1[3] = phih1;
      pint2[0] = xh2;
      pint2[1] = yh2;
      pint2[2] = zh2;
      pint2[3] = phih2;
      distint    = Math.sqrt(Math.pow((xh1-xh2),2) + Math.pow((yh1-yh2),2) + Math.pow((zh1-zh2),2) );
		pint[0] = 0.5*(xh1+xh2);
		pint[1] = 0.5*(yh1+yh2);
		pint[2] = 0.5*(zh1+zh2);  		
		return pint;

    }
   }
  
  //     circles intersect
   if (case_me==0){
   //icase = 3;

  r1sq  = r1*r1;
  r2sq  = r2*r2;
  x1sq  = x1*x1;
  x2sq  = x2*x2;
  y1sq  = y1*y1;
  y2sq  = y2*y2;
  sx12  = x1+x2;
  sy12  = y1+y2;
  drsq  = r2sq - r1sq;
  tdsq  = 2.*dsq;
  bint  = ( sx12*dy12*dy12 + dx12*(drsq + x1sq - x2sq) )/tdsq;
  cint  = ( sy12*dx12*dx12 + dy12*(drsq + y1sq - y2sq) )/tdsq;
  dprim = Math.sqrt( 4.*r1sq*dsq - Math.pow((drsq - dsq),2) )/tdsq;
  dint  = dy12*dprim;
  yint  = dx12*dprim;
  xi1   = bint + dint;
  xi2   = bint - dint;
  yi1   = cint - yint;
  yi2   = cint + yint;

// check that +/- correctly assigned
  dxi1  = xi1 - x1;
  dyi1  = yi1 - y1;
  dcen  = r1sq - dxi1*dxi1 - dyi1*dyi1;

   //     choose the intersection point corresponding to the smaller
// difference in the z coord. values for the given (x,y)

   result_zatxy=zatxy(hx1,xi1,yi1);
zi11=result_zatxy[0];
phii11=result_zatxy[1];

   result_zatxy=zatxy(hx2,xi1,yi1);
zi12=result_zatxy[0];
phii12=result_zatxy[1];

   result_zatxy=zatxy(hx1,xi2,yi2);
zi21=result_zatxy[0];
phii21=result_zatxy[1];

   result_zatxy=zatxy(hx2,xi2,yi2);
zi22=result_zatxy[0];
phii22=result_zatxy[1];

  dz1      = Math.abs(zi11 - zi12);
  dz2      = Math.abs(zi21 - zi22);
  if( dz2>=dz1 ){
  xh1      = xi1;
  yh1      = yi1;
  zh1      = zi11;
  phih1    = phii11;
  xh2      = xi1;
  yh2      = yi1;
  zh2      = zi12;
  phih2    = phii12;
  }
  if( dz2<dz1 ){
      
  xh1      = xi2;
  yh1      = yi2;
  zh1      = zi21;
  phih1    = phii21;
  xh2      = xi2;
  yh2      = yi2;
  zh2      = zi22;
  phih2    = phii22;     
  }   

	  System.out.println(" INT C " + xh1 +" "+yh1+" "+zh1 +" "+ xh2 +" "+yh2+" "+zh2);

      pint1[0] = xh1;
      pint1[1] = yh1;
      pint1[2] = zh1;
      pint1[3] = phih1;
      pint2[0] = xh2;
      pint2[1] = yh2;
      pint2[2] = zh2;
      pint2[3] = phih2;
      distint    = Math.sqrt(Math.pow((xh1-xh2),2) + Math.pow((yh1-yh2),2) + Math.pow((zh1-zh2),2) );
		pint[0] = 0.5*(xh1+xh2);
		pint[1] = 0.5*(yh1+yh2);
		pint[2] = 0.5*(zh1+zh2);  		
		return pint;
  		}//case 3
		
		
	return pint;
		
	}
	*/
	
	
} // end class
