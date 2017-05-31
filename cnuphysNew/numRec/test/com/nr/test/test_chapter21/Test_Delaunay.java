package com.nr.test.test_chapter21;

import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import com.nr.ran.*;

import com.nr.cg.*;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_Delaunay {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=10000,M=1000;
    Point[] pvec = new Point[N];
    for(i=0;i<N;i++)
      pvec[i]= new Point(2);
    
    boolean localflag=false, globalflag=false;

    

    // Test Delaunay
    System.out.println("Testing Delaunay");

    Ran myran=new Ran(17);
    for (i=0;i<N;i++) {
      pvec[i].x[0]=myran.doub();
      pvec[i].x[1]=myran.doub();
    }
    Delaunay del=new Delaunay(pvec);

    // Test whichcontainspt()
    Point test=new Point(2);
    int nwhich;
    Triel tri;
    for (i=0;i<M;i++) {
      test.x[0]=myran.doub();
      test.x[1]=myran.doub();
      nwhich=del.whichcontainspt(test,0);
//      System.out.printf(nwhich);
      if (nwhich == -1) {
        System.out.println("   Random Point falls outside of tesselation");
      } else {
        tri=del.thelist[nwhich];
//        System.out.printf(tri.pts[tri.p[0]].x[0] << " %f\n", tri.pts[tri.p[0]].x[1]);
//        System.out.printf(tri.pts[tri.p[1]].x[0] << " %f\n", tri.pts[tri.p[1]].x[1]);
//        System.out.printf(tri.pts[tri.p[2]].x[0] << " %f\n", tri.pts[tri.p[2]].x[1]);
        localflag = tri.contains(test) < 0.0;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Delaunay, whichcontainspt(): Point not contained by identified triangle");
          
        }
      }
    }

    // Test erasetriangle() : need to test separately??

    // Test storetriangle() : need to test separately??

    // Test interpolate()
    double defaultval=0.0,sbeps=1.e-2;
    double[] result=new double[M],expect=new double[M];
    double[] fnvals=new double[N];

    for (i=0;i<N;i++)
      fnvals[i]=exp(-10.0*(SQR(pvec[i].x[0]-0.5)+SQR(pvec[i].x[1]-0.5)));

    for (i=0;i<M;i++) {
      test.x[0]=myran.doub();
      test.x[1]=myran.doub();
      result[i]=del.interpolate(test,fnvals,defaultval);
      expect[i]=exp(-10.0*(SQR(test.x[0]-0.5)+SQR(test.x[1]-0.5)));
//      System.out.printf(result[i] << " %f\n", expect[i] << " %f\n", result[i]-expect[i]);
    }
//    System.out.printf(maxel(vecsub(result,expect)));
    localflag = maxel(vecsub(result,expect)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Delaunay, interpolate(): Unexpectedly inaccurate interpolated value ");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
