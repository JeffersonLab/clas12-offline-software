package com.nr.test.test_chapter3;

import static com.nr.NRUtil.*;
import static com.nr.interp.Spline2D_interp.bcuint;
import static com.nr.test.NRTestUtil.*;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

public class Test_bcuint {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i; // N=10;
    double x1l,x1u,x2l,x2u,uv,sbeps=1.e-16;
    double u[]={0.0,0.5,0.5,0.0};
    double v[]={0.0,0.0,0.5,0.5};
    double[] yy=new double[4],yy1=new double[4],yy2=new double[4],yy12=new double[4];
    boolean localflag, globalflag=false;

    

    // Test bcucof and bcuint
    System.out.println("Testing bcucof and bcuint");
    x1l=u[0];
    x1u=u[1];
    x2l=v[0];
    x2u=v[3];
    for (i=0;i<4;i++) {
      uv=u[i]*v[i];
      yy[i]=uv*uv;
      yy1[i]=2.0*v[i]*uv;
      yy2[i]=2.0*u[i]*uv;
      yy12[i]=4.0*uv;
    }
    double[] y = buildVector(yy),y1 = buildVector(yy1),y2 = buildVector(yy2),y12 = buildVector(yy12);
    double x1[]={0.1,0.2,0.3,0.4,0.5};
    double x2[]={0.2,0.4,0.1,0.3,0.3};
    double act[]={0.0004,0.0064,0.0009,0.0144,0.0225};
    double[] ansy=new double[5],ansy1=new double[5],ansy2=new double[5];
    for (i=0;i<5;i++) {
      doubleW ansyW = new doubleW(ansy[i]);
      doubleW ansy1W = new doubleW(ansy1[i]);
      doubleW ansy2W = new doubleW(ansy2[i]);
      
      bcuint(y,y1,y2,y12,x1l,x1u,x2l,x2u,x1[i],x2[i],ansyW,ansy1W,ansy2W);
      ansy[i] = ansyW.val;
      ansy1[i] = ansy1W.val;
      ansy2[i] = ansy2W.val;
    }

    double[] actual = buildVector(act),answer=buildVector(ansy);
    localflag = maxel(vecsub(actual,answer)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** bcucof,bcuint: Inaccurate bicubic interpolation.");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
