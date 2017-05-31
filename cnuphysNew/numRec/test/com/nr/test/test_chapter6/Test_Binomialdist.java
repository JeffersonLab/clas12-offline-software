package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Binomialdist;

public class Test_Binomialdist {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,n,ju,jb,M=21;
    double p,a,sum,sbeps=2.e-15;
    // int kk[]={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
    double ppexp[]={3.6561584400629632e-005,0.00048748779200839343,0.0030874226827198371,
      0.012349690730879482,0.034990790404158277,0.074647019528871122,
      0.12441169921478451,0.16588226561971309,0.17970578775468887,
      0.1597384780041681,0.11714155053638949,0.070994879112963691,
      0.035497439556481721,0.014563052125736135,0.0048543507085786804,
      0.0012944935222876605,0.00026968615047659553,4.2303709878681877e-005,
      4.700412208742411e-006,3.2985348833279697e-007,1.0995116277760029e-008};
    // int[] k = buildVector(kk);
    double[] pexp=buildVector(ppexp),pp=new double[M],c=new double[M],d=new double[M];
    boolean localflag=false, globalflag=false;


    

    // Test Binomialdist
    System.out.println("Testing Binomialdist");

    // Test special cases

    n=1; p=0.5; ju=1;
    Binomialdist norm2 = new Binomialdist(n,p);
    localflag = abs(norm2.p(ju)-0.5) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdist: Special case #1 failed");
      
    }

    n=2; p=0.5; ju=1;
    Binomialdist norm3 = new Binomialdist(n,p);
    localflag = abs(norm3.p(ju)-0.5) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdist: Special case #2 failed");
      
    }

    n=2; p=0.5; ju=2;
    Binomialdist norm4 = new Binomialdist(n,p);
    localflag = abs(norm4.p(ju)-0.25) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdist: Special case #3 failed");
      
    }

    n=3; p=1.0/3.0; ju=2;
    Binomialdist norm5 = new Binomialdist(n,p);
    localflag = abs(norm5.p(ju)-2.0/9.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdist: Special case #4 failed");
      
    }

    // Sum of distribution is one
    sbeps=1.e-14;
    n=10; p=0.4;
    Binomialdist dist1 = new Binomialdist(n,p);
    sum=0.0;
    for (i=0;i<=n;i++)
      sum += dist1.p(i);
    localflag = abs(1.0-sum) > sbeps;
//    System.out.printf(setprecision(15) << 1.0-sum);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdist: Distribution is not normalized to 1.0");
      
    }

    // cdf agrees with incomplete integral
    sbeps=1.e-14;
    n=10; p=0.4;
    Binomialdist dist3 =new Binomialdist(n,p);
    sum=0.0;
    localflag=false;
    for (i=0;i<n;i++) {
      sum += dist3.p(i);
      c[i]=sum;
      d[i]=dist3.cdf(i+1);
//      System.out.printf(c[i]-d[i]);
      localflag = localflag || abs(c[i]-d[i]) > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdist: cdf does not agree with summation");
      
    }

    // inverse cdf agrees with cdf
    n=10; p=0.4;
    Binomialdist normc=new Binomialdist(n,p);
    sbeps=5.0e-14;
    localflag=false;
    for (i=1;i<10;i++) {
      a=normc.cdf(i);
      jb=normc.invcdf(a);
//      if (abs(i-jb) > sbeps) {
//        System.out.printf(setprecision(15) << i << " %f\n", jb << " %f\n", abs(i-jb));
//      }
      localflag = localflag || (i-jb) != 0 && (i-jb-1) != 0;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdist: Inverse cdf does not accurately invert the cdf");
      
    }
      
    // Fingerprint test
    n=M-1; p=0.4;
    sbeps=2.e-15;
    Binomialdist normf=new Binomialdist(n,p);
    for (i=0;i<M;i++) {
      pp[i]=normf.p(i);
//      System.out.printf(setprecision(17) << pp[i] << " %f\n", pexp[i]);
    }
//    System.out.println("Binomialdist: Maximum discrepancy = %f\n", maxel(vecsub(pp,pexp)));
    localflag = maxel(vecsub(pp,pexp)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Binomialdist: Fingerprint does not match expectations");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
