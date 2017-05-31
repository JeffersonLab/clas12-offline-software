package com.nr.test.test_chapter7;

import static com.nr.NRUtil.SQR;
import static com.nr.NRUtil.buildVector;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.ran.MCintegrate;

public class Test_MCintegrate {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    double R1=3.0,R2=1.0,PI=acos(-1.0);
    double[] xlo = new double[3],xhi = new double[3],slo = new double[3],shi = new double[3];
    boolean localflag, globalflag=false;

    

    // Test MCintegrate
    System.out.println("Testing MCintegrate");

    xlo[0]=0.0;
    xhi[0]=4.0;
    xlo[1]=-4.0;
    xhi[1]=4.0;
    xlo[2]=-1.0;
    xhi[2]=1.0;
    MCintegrate mymc = new MCintegrate(xlo,xhi,17,false){
      
      @Override
      public double[] funcs(final double[] x){
        return torusfuncs(x);
      }

      @Override
      public boolean inregion(final double[] x) {
        return torusregion(x);
      }
      
      @Override
      public double[] xmap(final double[] x){
        return null;
      }
    };
    mymc.step(1000000);
    mymc.calcanswers();
//    for (i=0;i<4;i++)
//      System.out.printf(mymc.ff[i] << "  %f\n", mymc.fferr[i]);

    localflag = abs(mymc.ff[0]-SQR(PI)*R1*SQR(R2)) > 2.0*mymc.fferr[0];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MCintegrate: Calculation of hemitorus volume is inaccurate");
      
    }

    localflag = abs(mymc.ff[1]-2.0*PI*SQR(R1)*SQR(R2)*(1+SQR(R2/R1)/4.0)) > 2.0*mymc.fferr[1];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MCintegrate: Calculation of x moment of hemitorus is inaccurate");
      
    }

    localflag = abs(mymc.ff[2]) > 2.0*mymc.fferr[2];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MCintegrate: Calculation of y moment of hemitorus is inaccurate");
      
    }

    localflag=abs(mymc.ff[3]) > 2.0*mymc.fferr[3];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MCintegrate: Calculation of z moment of hemitorus is inaccurate");
      
    }

    // Test MCintegrate with  change of variables for weighting by z^2
    slo[0]=0.0;
    shi[0]=4.0;
    slo[1]=-4.0;
    shi[1]=4.0;
    slo[2]=-1.0/3.0;
    shi[2]=1.0/3.0;
    //MCintegrate mymc2(slo,shi,torusfuncs,torusregion,torusmap2,17);
    MCintegrate mymc2 = new MCintegrate(xlo,xhi,17,true){

      @Override
      public double[] funcs(final double[] x){
        return torusfuncs(x);
      }
      
      @Override
      public boolean inregion(final double[] x) {
        return torusregion(x);
      }

      @Override
      public double[] xmap(final double[] x){
        return torusmap2(x);
      }
    };
    mymc2.step(1000000);
    mymc2.calcanswers();
//    for (i=0;i<4;i++)
//      System.out.printf(mymc2.ff[i] << "  %f\n", mymc2.fferr[i]);

    localflag = abs(mymc2.ff[0]-SQR(PI)*R1*SQR(SQR(R2))/4.0) > 2.0*mymc2.fferr[0];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MCintegrate: Incorrect weighted volume calculation with change of variables");
      
    }

    localflag = abs(mymc2.ff[1]-PI*SQR(R1)*SQR(SQR(R2))*(1+SQR(R2/R1)/6.0)/2.0) > 2.0*mymc2.fferr[1];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MCintegrate: Incorrect weighted x moment with change of variables");
      
    }

    localflag = abs(mymc2.ff[2]) > 2.0*mymc2.fferr[2];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MCintegrate: Incorrect weighted y moment with change of variables");
      
    }

    localflag=abs(mymc2.ff[3]) > 2.0*mymc2.fferr[3];
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** MCintegrate: Incorrect weighted z moment with change of variables");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  double[] torusmap2(final double[] s) {
    double[] xx = buildVector(s);
    xx[2]=(s[2] < 0.0 ? -pow(abs(3.0*s[2]),1.0/3.0) : pow(3.0*s[2],1.0/3.0));
    return xx;
  }

}
