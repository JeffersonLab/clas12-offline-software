package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Poissondist;

public class Test_Poissondist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,ju,N=21;
    double lambda,a,b,sum,sbeps=1.e-15;
    int kk[]={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
    double ppexp[]={4.5399929762484854e-005,0.00045399929762484861,0.0022699964881242435,
      0.007566654960414157,0.018916637401035368,0.037833274802070861,
      0.063055458003451248,0.090079225719216005,0.1125990321490199,
      0.12511003572113372,0.12511003572113327,0.11373639611012128,
      0.094780330091767701,0.072907946224436804,0.052077104446025965,
      0.03471806963068437,0.021698793519177671,0.012763996187751595,
      0.0070911089931953337,0.0037321626279975023,0.0018660813139987742};
    int[] k = buildVector(kk);
    double[] pexp=buildVector(ppexp),p= new double[N],c= new double[N],d= new double[N];
    boolean localflag=false, globalflag=false;

    

    // Test Poissondist
    System.out.println("Testing Poissondist");

    // Test special cases
    lambda=1.0; ju=0;
    Poissondist norm1 = new Poissondist(lambda);
    localflag = abs(norm1.p(ju)-exp(-1.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Special case #1 failed");
      
    }

    lambda=1.0; ju=1;
    Poissondist norm2 = new Poissondist(lambda);
    localflag = abs(norm2.p(ju)-exp(-1.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Special case #2 failed");
      
    }

    lambda=2.0; ju=0;
    Poissondist norm3 = new Poissondist(lambda);
    localflag = abs(norm3.p(ju)-exp(-2.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Special case #3 failed");
      
    }

    lambda=2.0; ju=1;
    Poissondist norm4 = new Poissondist(lambda);
    localflag = abs(norm4.p(ju)-2.0*exp(-2.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Special case #4 failed");
      
    }

    lambda=1.0; ju=2;
    Poissondist norm5 = new Poissondist(lambda);
    localflag = abs(norm5.p(ju)-0.5*exp(-1.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Special case #5 failed");
      
    }

    lambda=2.0; ju=2;
    Poissondist norm6 = new Poissondist(lambda);
    localflag = abs(norm6.p(ju)-2.0*exp(-2.0)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Special case #6 failed");
      
    }

    // Sum over k is one
    sbeps=1.e-15;
    lambda=5.0;
    sum=0.0;
    Poissondist dist = new Poissondist(lambda);
    for (i=0;i<50;i++)
      sum += dist.p(i);
    localflag = abs(1.0-sum) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-sum);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with truncated sum
    sbeps=1.e-15;
    lambda=5.0;
    Poissondist dist2 = new Poissondist(lambda);
    localflag=false;
    sum=0.0;
    for (i=0;i<20;i++) {
      sum += dist2.p(i);
      c[i]=sum;
      d[i]=dist2.cdf(i+1);
//      System.out.printf(c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: cdf does not agree with result of quadrature");
      
    }

    // inverse cdf agrees with cdf
    lambda=10.0;
    Poissondist normc = new Poissondist(lambda);
    sbeps=1.0e-12;
    localflag=false;
    for (i=1;i<20;i++) {
      a=normc.cdf(i);
      b=normc.invcdf(a);
      localflag = localflag || (i-b != 0) && (i-b-1 != 0) ;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Inverse cdf does not accurately invert the cdf");
      
    }

    // Fingerprint test
    lambda=10.0;
    Poissondist normf = new Poissondist(lambda);
    for (i=0;i<N;i++) {
      p[i]=normf.p(k[i]);
//      System.out.printf(setprecision(17) << p[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Poissondist: Maximum discrepancy = %f\n", maxel(vecsub(p,pexp)));
    localflag = maxel(vecsub(p,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Poissondist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
