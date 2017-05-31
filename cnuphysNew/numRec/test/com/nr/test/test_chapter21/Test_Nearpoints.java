package com.nr.test.test_chapter21;

import static com.nr.cg.Point.dist;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.cg.KDtree;
import com.nr.cg.Nearpoints;
import com.nr.cg.Point;
import com.nr.ran.Ran;


public class Test_Nearpoints {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,nkd,nqo,N=1000,M=100;
    double r,d,min,sbeps;
    Point testpt = new  Point(2);
    Point[] pvec=new Point[N];
    boolean localflag, globalflag=false;

    

    // Test Nearpoints
    System.out.println("Testing Nearpoints");

    Ran myran=new Ran(17);

    // Create KDtree and Qotree of same Points<2> in 2D
    for (i=0;i<N;i++) {
      pvec[i] = new Point(2);
      pvec[i].x[0]=myran.doub();
      pvec[i].x[1]=myran.doub();
    }

    Nearpoints qo = new Nearpoints(2,pvec);
    KDtree kd = new KDtree(2,pvec);

    int[] kdlist=new int[M];
    Point[] qolist=new Point[M];

    testpt.x[0]=0.6;
    testpt.x[1]=0.7;
    r=0.1;
    nkd=kd.locatenear(testpt,r,kdlist,M);
    nqo=qo.locatenear(testpt,r,qolist,M);

    // See if KDtree and Nearpoints found the same number of
    // neighbors withing radius r
    localflag = (nkd != nqo);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Nearpoints,2D: KDtree found a different number of neighbors inside radius r");
      
    }

    // See if each point from kdlist can be found in qolist
    sbeps=1.e-16;
    if (nkd == nqo) {
      for (i=0;i<nkd;i++) {
        min=1.e99;
        for (j=0;j<nqo;j++) {
          d=dist(kd.ptss[kdlist[i]],qolist[j]);
          if (d < min) min=d;
        }
//        System.out.printf(min);
        localflag = min > sbeps;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Nearpoints,2D: A point identified by KDtree was not found by Nearpoints");
          
        }
      }
    }

    // Create KDtree and Qotree of same Points<3> in 3D
    Point test3pt=new Point(3);
    Point[] p3vec=new Point[N];  
    for (i=0;i<N;i++) {
      p3vec[i] = new Point(3);
      p3vec[i].x[0]=myran.doub();
      p3vec[i].x[1]=myran.doub();
      p3vec[i].x[2]=myran.doub();
    }

    Nearpoints qo3=new Nearpoints(3,p3vec);
    KDtree kd3=new KDtree(3,p3vec);

    int[] kd3list=new int[M];
    Point[] qo3list=new Point[M];

    test3pt.x[0]=0.6;
    test3pt.x[1]=0.7;
    test3pt.x[2]=0.6;
    r=0.1;
    nkd=kd3.locatenear(test3pt,r,kd3list,M);
    nqo=qo3.locatenear(test3pt,r,qo3list,M);

    // See if KDtree and Nearpoints found the same number of
    // neighbors withing radius r
    localflag = (nkd != nqo);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Nearpoints,3D: KDtree found a different number of neighbors inside radius r");
      
    }

    // See if each point from kdlist can be found in qolist
    sbeps=1.e-16;
    if (nkd == nqo) {
      for (i=0;i<nkd;i++) {
        min=1.e99;
        for (j=0;j<nqo;j++) {
          d=dist(kd3.ptss[kd3list[i]],qo3list[j]);
          if (d < min) min=d;
        }
//        System.out.printf(min);
        localflag = min > sbeps;
        globalflag = globalflag || localflag;
        if (localflag) {
          fail("*** Nearpoints: A point identified by KDtree was not found by Nearpoints");
          
        }
      }
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
