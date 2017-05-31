package com.nr.test.test_chapter9;

import static com.nr.root.Roots.laguer;
import static com.nr.test.NRTestUtil.maxel;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.intW;

import com.nr.Complex;
import com.nr.ran.Ran;

public class Test_laguer {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    intW its = new intW(0);
    int i,M=5,N=20;
    double sbeps=1.e-14;
    Complex x = new Complex(),re1 = new Complex(1.0,0.0),im1=new Complex(0.0,1.0);
    Complex a[]={
        re1.mul(2.0).add(im1),
        im1.mul(2.0),
        re1.mul(3.0).add(im1),
        re1.add(im1.mul(2.0)),
        re1.sub(im1),
        new Complex(1.0)
        };
    Complex[] aa = new Complex[M+1];System.arraycopy(a, 0, aa, 0 , M+1);
    double[] dy = new double[N];
    boolean localflag, globalflag=false;

    

    // Test laguer
    System.out.println("Testing laguer");
    // Roots of polynomial x^5+(1-i)x^4+(1+2i)x^3+(3+i)x^2+(2i)x+(2+i)"
    // Roots are x=i, x=-i, x=sqrt(2i), x=-i*sqrt(2i), x=(i-1)
    Ran myran =new Ran(17);
    for (i=0;i<N;i++) {
      x=new Complex(-2.0+4.0*myran.doub(),-2.0+4.0*myran.doub());
      x = laguer(aa,x,its);
      Complex r = a[4].add(x.mul(a[5]));
      r = a[3].add(x.mul(r));
      r = a[2].add(x.mul(r));
      r = a[1].add(x.mul(r));
      r = a[0].add(x.mul(r));
      dy[i] = r.abs();
      //dy[i]=abs(a[0]+x*(a[1]+x*(a[2]+x*(a[3]+x*(a[4]+x*a[5])))));
      
    }
    System.out.printf("laguer: Maximum discrepancy = %f\n", maxel(dy));
    localflag = maxel(dy) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** laguer: Incorrect roots");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
