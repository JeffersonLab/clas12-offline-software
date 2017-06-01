package com.nr.test.test_chapter20;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.pde.Mgfas;

public class Test_Mgfas {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int JMAX=33;
    int i,j;
    double diff,maxdiff,sumsq,rms,sbeps;
    double[][] f=new double[JMAX][JMAX], u=new double[JMAX][JMAX],expect=new double[JMAX][JMAX];
    boolean localflag, globalflag=false;
    

    // Test Mgfas
    System.out.println("Testing Mgfas");

    for (i=0;i<JMAX;i++) {
      for (j=0;j<JMAX;j++) {
        u[i][j] = 1.0/(1+pow(i-JMAX/2.0,2.0)/4.0);
        u[i][j] *= 1.0/(1+pow(j-JMAX/2.0,2.0)/4.0);
        expect[i][j]=u[i][j];
      }
    }
    Mgfas mgfas = new Mgfas(u,2);u=mgfas.u;

    // Test that solution satisfies difference equations:";
    sumsq=0.0;
    maxdiff=0.0;
    for (i=1;i<JMAX-1;i++) {
      for (j=1;j<JMAX-1;j++) {
        f[i][j]=(u[i+1][j]+u[i-1][j]+u[i][j+1]+u[i][j-1]-
          4.0*u[i][j])*SQR(JMAX-1.0)+u[i][j]*u[i][j];
        diff = f[i][j]-expect[i][j];
        if (abs(diff) > maxdiff) maxdiff=abs(diff);
        sumsq += pow(diff,2.0);
      }
    }
    rms=sqrt(sumsq/(JMAX-2)/(JMAX-2));
    System.out.printf("max difference = %f\n", maxdiff);
    System.out.printf("rms error = %f\n", rms);

    sbeps=1.e-3;
    localflag = rms > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Mgfas: RMS error is unexpectedly large");
      
    }

    sbeps=1.e-2;
    localflag = maxdiff > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Mgfas: Maximum deviation is unexpectedly large");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
