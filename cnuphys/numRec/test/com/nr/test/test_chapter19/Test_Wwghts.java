package com.nr.test.test_chapter19;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealMultiValueFun;
import com.nr.inv.Wwghts;

public class Test_Wwghts {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i;
    double sum,expect,sbeps=1.e-14;
    double[] w2=new double[2],w3=new double[3];
    double[] w4=new double[4],w5=new double[5],w6=new double[6];
    boolean localflag, globalflag=false;

    

    // Test Wwghts
    System.out.println("Testing Wwghts");

    Quad quad =new Quad();
    Wwghts weight2 = new Wwghts(1.0,2,quad);
    w2=weight2.weights();
//    for (i=0;i<2;i++) System.out.printf(w2[i] << " ";
//    System.out.printf(endl;

    sum=0.0;
    for (i=0;i<2;i++) sum += w2[i];
//    System.out.printf(abs(sum-1.0));
    localflag = abs(sum-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 2-point weights do not add to 1.0");
      
    }

    // Check integral of 1+x => x + x^2/2
    expect=1.0+0.5;
    sum=w2[0]*1.0+w2[1]*2.0;
//    System.out.printf(expect << " %f\n", sum);
    localflag = abs(sum/expect-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 2-point weights fail on linear function");
      
    }

    Wwghts weight3 = new Wwghts(1.0/2.0,3,quad);
    w3=weight3.weights();
//    for (i=0;i<3;i++) System.out.printf(w3[i] << " ";
//    System.out.printf(endl;

    sum=0.0;
    for (i=0;i<3;i++) sum += w3[i];
//    System.out.printf(abs(sum-1.0));
    localflag = abs(sum-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 3-point weights do not add to 1.0");
      
    }

    // Check integral of 1+x+x^2 => x + x^2/2 + x^3/3
    expect=1.0+0.5+1.0/3.0;
    sum=w3[0]*1.0+w3[1]*(1.0+0.5*(1.0+0.5))+w3[2]*3.0;
//    System.out.printf(expect << " %f\n", sum);
    localflag = abs(sum/expect-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 3-point weights fail on quadratic function");
      
    }

    Wwghts weight4 = new Wwghts(1.0/3.0,4,quad);
    w4=weight4.weights();
//    for (i=0;i<4;i++) System.out.printf(w4[i] << " ";
//    System.out.printf(endl;

    sum=0.0;
    for (i=0;i<4;i++) sum += w4[i];
//    System.out.printf(abs(sum-1.0));
    localflag = abs(sum-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 4-point weights do not add to 1.0");
      
    }

    // Check integral of 1+x+x^2+x^3 => x + x^2/2 + x^3/3 + x^4/4
    expect=1.0+0.5+1.0/3.0+0.25;
    sum=w4[0]*1.0
      +w4[1]*(1.0+(1.0/3.0)*(1.0+(1.0/3.0)*(1.0+(1.0/3.0))))
      +w4[2]*(1.0+(2.0/3.0)*(1.0+(2.0/3.0)*(1.0+(2.0/3.0))))
      +w4[3]*4.0;
//    System.out.printf(expect << " %f\n", sum);
    localflag = abs(sum/expect-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 4-point weights fail on cubic polynomial");
      
    }

    Wwghts weight5=new Wwghts(1.0/4.0,5,quad);
    w5=weight5.weights();
//    for (i=0;i<5;i++) System.out.printf(w5[i] << " ";
//    System.out.printf(endl;

    sum=0.0;
    for (i=0;i<5;i++) sum += w5[i];
//    System.out.printf(abs(sum-1.0));
    localflag = abs(sum-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 5-point weights do not add to 1.0");
      
    }

    // Check integral of 1+x+x^2+x^3 => x + x^2/2 + x^3/3 + x^4/4
    expect=1.0+0.5+1.0/3.0+0.25;
    sum=w5[0]*1.0
      +w5[1]*(1.0+0.25*(1.0+0.25*1.25))
      +w5[2]*(1.0+0.50*(1.0+0.50*1.50))
      +w5[3]*(1.0+0.75*(1.0+0.75*1.75))
      +w5[4]*4.0;
//    System.out.printf(expect << " %f\n", sum);
    localflag = abs(sum/expect-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 5-point weights fail on cubic polynomial");
      
    }

    Wwghts weight6=new Wwghts(1.0/5.0,6,quad);
    w6=weight6.weights();
//    for (i=0;i<6;i++) System.out.printf(w6[i] << " ";
//    System.out.printf(endl;

    sum=0.0;
    for (i=0;i<6;i++) sum += w6[i];
//    System.out.printf(abs(sum-1.0));
    localflag = abs(sum-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 6-point weights do not add to 1.0");
      
    }

    // Check integral of 1+x+x^2+x^3 => x + x^2/2 + x^3/3 + x^4/4
    expect=1.0+0.5+1.0/3.0+0.25;
    sum=w6[0]*1.0
      +w6[1]*(1.0+0.2*(1.0+0.2*1.2))
      +w6[2]*(1.0+0.4*(1.0+0.4*1.4))
      +w6[3]*(1.0+0.6*(1.0+0.6*1.6))
      +w6[4]*(1.0+0.8*(1.0+0.8*1.8))
      +w6[5]*4.0;
//    System.out.printf(expect << " %f\n", sum);
    localflag = abs(sum/expect-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Wwghts: 6-point weights fail on fifth-order polynomial");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Quad implements UniVarRealMultiValueFun{
    public double[] funk(final double y) {
      int i;
      double[] w=new double[4];
      for (i=0;i<4;i++)
        w[i]=pow(y,1.0*(i+1))/(i+1.0);
      return w;
    }
  }

}
