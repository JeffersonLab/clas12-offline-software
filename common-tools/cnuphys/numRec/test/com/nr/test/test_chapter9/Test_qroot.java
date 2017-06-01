package com.nr.test.test_chapter9;

import static com.nr.NRUtil.buildVector;
import static com.nr.root.Roots.qroot;
import static com.nr.root.Roots.zroots;
import static java.lang.Math.abs;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netlib.util.doubleW;

import com.nr.Complex;

public class Test_qroot {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int j,N=6;
    boolean polish=true;
    double eps=1.e-10,err,sbeps=1.e-14;
    doubleW b = new doubleW(0);
    doubleW c = new doubleW(0);
    
    Complex pp[]={
        new Complex(2.0),
        new Complex(-2.0),
        new Complex(7.0),
        new Complex(1.0),
        new Complex(-3.0),
        new Complex(5.0)};
    double ppr[]={2.0,-2.0,7.0,1.0,-3.0,5.0};
    Complex[] p=new Complex[N];System.arraycopy(pp, 0, p, 0, N);
    double[] pr=buildVector(ppr);
    Complex[] rts=new Complex[N-1],rts2 =new Complex[2];
    boolean localflag, globalflag=false;

    

    // Test qroot
    System.out.println("Testing qroot");
    // Ran myran = new Ran(17); not use it
    zroots(p,rts,!polish);  // Find actual roots
    //    for (j=0;j<N-1;j++) System.out.printf(rts[j] << " ");
    c.val=0.25;   // Constructed guess from actual roots
    b.val=-0.20;
    qroot(pr,b,c,eps);
    Complex[] a=new Complex[3];
    a[0]=new Complex(c.val); a[1]=new Complex(b.val); a[2]=new Complex(1.0);   // Now test result
    zroots(a,rts2,polish);
    err=0.0;  
    for (j=0;j<2;j++) {
      Complex r = p[4].add(rts2[j].mul(p[5]));
      r = p[3].add(rts2[j].mul(r));
      r = p[2].add(rts2[j].mul(r));
      r = p[1].add(rts2[j].mul(r));
      r = p[0].add(rts2[j].mul(r));
      
      err += r.abs();
    }
    System.out.printf("qroot: Discrepancy = %f\n", abs(err));
    localflag = abs(err) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** qroot: Quadratic is not a factor of the polynomial");
    }

    c.val=1.3;    // Constructed another guess from actual roots
    b.val=-1.4;
    qroot(pr,b,c,eps);
    a[0]=new Complex(c.val); a[1]=new Complex(b.val); a[2]=new Complex(1.0);   // Now test result
    zroots(a,rts2,polish);
    err=0.0;  
    for (j=0;j<2;j++) {
      Complex r = p[4].add(rts2[j].mul(p[5]));
      r = p[3].add(rts2[j].mul(r));
      r = p[2].add(rts2[j].mul(r));
      r = p[1].add(rts2[j].mul(r));
      r = p[0].add(rts2[j].mul(r));
      err += r.abs();
    }
    System.out.printf("qroot: Discrepancy = %f\n", abs(err));
    localflag = abs(err) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** qroot: Quadratic is not a factor of the polynomial");
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
