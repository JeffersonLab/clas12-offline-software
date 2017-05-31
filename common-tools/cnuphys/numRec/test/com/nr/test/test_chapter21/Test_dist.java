package com.nr.test.test_chapter21;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import com.nr.ran.*;

import com.nr.cg.*;
import static com.nr.cg.Point.*;


public class Test_dist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=100;
    double sbeps;
    double[] d=new double[100],dexp=new double[100];
    boolean localflag, globalflag=false;

    

    // Test dist
    System.out.println("Testing dist");

    // Distance between two points in 2D
    Ran myran=new Ran(17);
    Point x=new Point(2),y=new Point(2);
    for (i=0;i<N;i++) {
      x.x[0]=myran.doub();
      x.x[1]=myran.doub();
      y.x[0]=myran.doub();
      y.x[1]=myran.doub();
      dexp[i]=sqrt(SQR(y.x[0]-x.x[0])+SQR(y.x[1]-x.x[1]));
      d[i]=dist(x,y);
    }

    sbeps=1.0e-15;
    localflag = maxel(vecsub(d,dexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** dist: Error in distance between two Points in 2D");
      
    }

    // Distance between two points in 3D
    Point xx=new Point(3),yy=new Point(3);
    for (i=0;i<N;i++) {
      xx.x[0]=myran.doub();
      xx.x[1]=myran.doub();
      xx.x[2]=myran.doub();
      yy.x[0]=myran.doub();
      yy.x[1]=myran.doub();
      yy.x[2]=myran.doub();
      dexp[i]=sqrt(SQR(yy.x[0]-xx.x[0])+SQR(yy.x[1]-xx.x[1])
        +SQR(yy.x[2]-xx.x[2]));
      d[i]=dist(xx,yy);
    }

    sbeps=1.0e-15;
    localflag = maxel(vecsub(d,dexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** dist: Error in distance between two Points in 3D");
      
    }

    // Distance between a Point and a Box in 2D
    x.x[0]=0.25;
    x.x[1]=0.25;
    y.x[0]=0.75;
    y.x[1]=0.75;
    Box box=new Box(x,y);
    for (i=0;i<N;i++) {
      x.x[0]=myran.doub();
      x.x[1]=myran.doub();
      dexp[i]=0.0;
      for (j=0;j<2;j++) {
        if (x.x[j] < box.lo.x[j])
          dexp[i] += SQR(box.lo.x[j]-x.x[j]);
        else if (x.x[j] > box.hi.x[j])
          dexp[i] += SQR(box.hi.x[j]-x.x[j]);
      }
      dexp[i]=sqrt(dexp[i]);
      d[i]=Box.dist(box,x);
    }
    sbeps=1.0e-15;
    localflag = maxel(vecsub(d,dexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** dist: Error in distance between a Point and Box in 2D");
      
    }

    // Distance between a Point and a Box in 3D
    xx.x[0]=0.25;
    xx.x[1]=0.25;
    xx.x[2]=0.25;
    yy.x[0]=0.75;
    yy.x[1]=0.75;
    yy.x[2]=0.75;
    Box bbox=new Box(xx,yy);
    for (i=0;i<N;i++) {
      xx.x[0]=myran.doub();
      xx.x[1]=myran.doub();
      xx.x[2]=myran.doub();
      dexp[i]=0.0;
      for (j=0;j<3;j++) {
        if (xx.x[j] < bbox.lo.x[j])
          dexp[i] += SQR(bbox.lo.x[j]-xx.x[j]);
        else if (xx.x[j] > bbox.hi.x[j])
          dexp[i] += SQR(bbox.hi.x[j]-xx.x[j]);
      }
      dexp[i]=sqrt(dexp[i]);
      d[i]=Box.dist(bbox,xx);
    }
    sbeps=1.0e-15;
    localflag = maxel(vecsub(d,dexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** dist: Error in distance between a Point and Box in 3D");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
