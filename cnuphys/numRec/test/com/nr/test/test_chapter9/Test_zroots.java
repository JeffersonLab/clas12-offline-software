package com.nr.test.test_chapter9;

import static com.nr.root.Roots.zroots;
import static com.nr.test.NRTestUtil.maxel;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;


public class Test_zroots {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=5;
    boolean polish=true;
    double sbeps=1.e-14;
    Complex re1 = new Complex(1.0,0.0),im1=new Complex(0.0,1.0);
    Complex a[]={
        re1.mul(2.0).add(im1),
        im1.mul(2.0),
        re1.mul(3.0).add(im1),
        re1.add(im1.mul(2.0)),
        re1.sub(im1),
        new Complex(1.0)
        };
    
    Complex[] aa = new Complex[N+1];System.arraycopy(a, 0, aa, 0 , N+1);
    
    Complex[] rts = new Complex[N];
    double[] dy=new double[N];
    boolean localflag, globalflag=false;

    

    // Test zroots
    System.out.println("Testing zroots");
    // Roots of polynomial x^5+(1-i)x^4+(1+2i)x^3+(3+i)x^2+(2i)x+(2+i)"
    // Roots are x=i, x=-i, x=sqrt(2i), x=-i*sqrt(2i), x=(i-1)
    zroots(aa,rts,polish);
    for (i=0;i<N;i++) {
      Complex r = a[4].add(rts[i].mul(a[5]));
      r = a[3].add(rts[i].mul(r));
      r = a[2].add(rts[i].mul(r));
      r = a[1].add(rts[i].mul(r));
      r = a[0].add(rts[i].mul(r));
      dy[i]=r.abs();
    }
    System.out.printf("zroots: Maximum discrepancy = %f\n", maxel(dy));
    localflag = maxel(dy) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** zroots: Incorrect roots");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
