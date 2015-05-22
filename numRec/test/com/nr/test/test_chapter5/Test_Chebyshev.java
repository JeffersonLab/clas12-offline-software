package com.nr.test.test_chapter5;

import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.UniVarRealValueFun;
import com.nr.fe.Chebyshev;
import com.nr.sf.Bessjy;

public class Test_Chebyshev {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,N=5,NN=50,m,MM=11;
    double x,xx,aa=-5.0,bb=5.0,val,thresh=1.e-6,sbeps=1.e-6;
    double[] y = new double[MM],yy = new double[MM];
    Bessjy b = new Bessjy();
    boolean localflag, globalflag=false;

    

    // Test Chebyshev (eval)
    System.out.println("Testing Chebyshev (eval)");
    bjn bjn = new bjn();
    Chebyshev cheb = new Chebyshev(bjn,aa,bb,NN);
    m=cheb.setm(thresh);
    for (i=0;i<MM;i++) {
      x=-5.0+i;
      y[i]=cheb.eval(x,m);
      yy[i]=bjn.funk(x);
    }
    System.out.printf("Chebyshev (eval): Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chebyshev (eval): Chebyshev approximation does not evaluate to accurate function values");
      
    }

    // Test Chebyshev (polycofs)
    System.out.println("Testing Chebyshev (polycofs)");
    double[] d=cheb.polycofs(m);
    for (i=0;i<MM;i++) {
      x=-5.0+(double)(i);
      y[i]=cheb.eval(x,m);
      xx=(x-0.5*(aa+bb))/(0.5*(bb-aa));
      val=d[m-1];
      for (j=m-2;j>=0;j--) val=val*xx+d[j];
      yy[i]=val;
//    System.out.printf(y[i] << " %f\n", yy[i] << endl;
    }
    System.out.printf("Chebyshev (polycofs): Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chebyshev (polycofs): Polynomial coefficients do not yield correct function values");
      
    }

    // Test Chebyshev (derivative)
    System.out.println("Testing Chebyshev (derivative)");
    Chebyshev cder=cheb.derivative();
    for (i=0;i<MM;i++) {
      x=-5.0+i;
      y[i]=cder.eval(x,m);
      yy[i]=0.5*(b.jn(N-1,x)-b.jn(N+1,x));
//      System.out.printf(y[i] << " %f\n", yy[i] << endl;
    }
    System.out.printf("Chebyshev (derivative): Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chebyshev (derivative): Chebyshev derivative has incorrect values");
      
    }

    // Test Chebyshev (integral)
    System.out.println("Testing Chebyshev (integral)");
    bj1 bj1 = new bj1();
    Chebyshev cheb1 = new Chebyshev(bj1,aa,bb,NN);
    Chebyshev cint=cheb1.integral();
    for (i=0;i<MM;i++) {
      x=-5.0+i;
      y[i]=cint.eval(x,m);
      yy[i]=b.j0(aa)-b.j0(x);
//    System.out.printf(y[i] << " %f\n", yy[i] << endl;
    }
    System.out.printf("Chebyshev (integral): Maximum discrepancy = %f\n", maxel(vecsub(y,yy)));
    sbeps=2.0e-6;
    localflag = maxel(vecsub(y,yy)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chebyshev (integral): Chebyshev integral has incorrect values");
      
    }

    // Test Chebyshev (construct from polynomial)
    System.out.println("Testing Chebyshev (construct from polynomial)");
    Chebyshev chebnew = new Chebyshev(d);
    double[] z = new double[m],zz = new double[m];
    for (i=0;i<m;i++) {
      z[i]=cheb.getc()[i];
      zz[i]=chebnew.getc()[i];
//      System.out.printf(z[i] << " %f\n", zz[i] << endl;
    }
    System.out.printf("Chebyshev (construct from polynomial): Maximum discrepancy = %f\n", maxel(vecsub(z,zz)));
    sbeps=1.e-15;
    localflag = maxel(vecsub(z,zz)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Chebyshev (integral): Chebyshev (construct from polynomial): Reconstructed Chebyshev coeffiecients not the same as originals");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class bjn implements UniVarRealValueFun{
    public double funk(final double x) {
      int N=5;

      Bessjy b = new Bessjy();
      return(b.jn(N,x));
    }
  }
  
  class bj1 implements UniVarRealValueFun{
    public double funk(final double x) {
      int N=1;

      Bessjy b = new Bessjy();
      return(b.jn(N,x));
    }
  }
}
