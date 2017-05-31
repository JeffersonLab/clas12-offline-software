package com.nr.test.test_chapter9;

import static com.nr.NRUtil.SQR;
import static com.nr.test.NRTestUtil.maxel;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.root.MNEWT;

public class Test_mnewt {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,NTRIAL=6,NDIM=4,M=8;
    double tolx=1.0e-14,tolf=1.0e-14,sbeps=1.e-14;
    double x0[]={0.5,0.4,0.6,0.5,-0.5,-0.4,-0.5,-0.4};
    double x1[]={0.6,0.6,0.4,0.4,-0.4,-0.5,-0.6,-0.6};
    double x2[]={0.4,0.5,0.5,0.6,-0.6,-0.6,-0.4,-0.5};
    double x3[]={0.0,1.4,0.0,1.6,0.0,1.3,0.0,1.7};
    double[] fvec=new double[NDIM],x=new double[NDIM],dy = new double[M];
    double[][] fjac=new double[NDIM][NDIM];
    boolean localflag, globalflag=false;

    

    // Test mnewt
    System.out.println("Testing mnewt");
    for (i=0;i<M;i++) {
      for (j=0;j<NDIM;j++) {
        x[0]=x0[i];
        x[1]=x1[i];
        x[2]=x2[i];
        x[3]=x3[i];
      }
      MNEWT mnewt =new MNEWT(){
        public void funk(double[] x, double[] fvec, double[][] fjac) {
          usrfun(x, fvec, fjac);
        }
      };
      mnewt.mnewt(NTRIAL,x,tolx,tolf);
      usrfun(x,fvec,fjac);
      dy[i]=maxel(fvec);
    }
    System.out.printf("mnewt: Maximum discrepancy = %f\n", maxel(dy));
    localflag = maxel(dy) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** mnewt: Inaccurate roots");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  void usrfun(double[] x, double[] fvec, double[][] fjac) {
    int i;

    int n=x.length;
    fjac[0][0] = -2.0*x[0];
    fjac[0][1] = -2.0*x[1];
    fjac[0][2] = -2.0*x[2];
    fjac[0][3] = 1.0;
    for (i=0;i<n;i++) fjac[1][i] = 2.0*x[i];
    fjac[2][0] = 1.0;
    fjac[2][1] = -1.0;
    fjac[2][2] = 0.0;
    fjac[2][3] = 0.0;
    fjac[3][0] = 0.0;
    fjac[3][1] = 1.0;
    fjac[3][2] = -1.0;
    fjac[3][3] = 0.0;
    fvec[0] = -SQR(x[0])-SQR(x[1])-SQR(x[2])+x[3];
    fvec[1] = SQR(x[0])+SQR(x[1])+SQR(x[2])+SQR(x[3])-1.0;
    fvec[2] = x[0]-x[1];
    fvec[3] = x[1]-x[2];
  }


}
