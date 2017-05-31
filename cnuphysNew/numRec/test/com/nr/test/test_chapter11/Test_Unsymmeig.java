package com.nr.test.test_chapter11;

import static com.nr.test.NRTestUtil.matmul;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.ranmat;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;
import com.nr.eig.Unsymmeig;
import com.nr.ran.Ran;

public class Test_Unsymmeig {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,j,k,N=10;
    double max,sbeps=5.e-14;
    double[] vec=new double[N],res=new double[N];
    Complex[] zres=new Complex[N],zvec=new Complex[N];
    double[][] a=new double[N][N];
    boolean localflag=false, globalflag=false;

    

    // Test Unsymmeig, symmetric, interface1
    System.out.println("Testing Unsymmeig, symmetric, interface1");

    Ran myran=new Ran(17);
    for (i=0;i<N;i++) {
      a[i][i]=myran.doub();
      for (j=0;j<i;j++) {
        a[i][j]=myran.doub();
        a[j][i]=a[i][j];
      }
    }
    Unsymmeig usym = new Unsymmeig(a,true,false);

//    for (i=0;i<N;i++) System.out.printf(usym.wri[i]);

    // Test that all eigenvalues are real for symmetric matrix
    for (i=0;i<N;i++) 
      localflag = localflag || (usym.wri[i].im() != 0);
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, symmetric, interface1: Symmetric matrix gave an eigenvalue that was not real");
      
    }

    // Test eigenvector/eigenvalue pairs
    for (i=0;i<N;i++) {   // for each eigenvector
      for (j=0;j<N;j++) vec[j]=usym.zz[j][i];
      res=matmul(a,vec);
      for (j=0;j<N;j++) vec[j] *= usym.wri[i].re();
//      System.out.printf(maxel(vecsub(res,vec)));
      localflag = localflag || (maxel(vecsub(res,vec)) > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, symmetric, interface1: Matrix times eigenvector was not the same as lambda*eigenvector");
      
    }
    
    // Test the sorting of the eigenvalues
    for (i=1;i<N;i++) 
      localflag = localflag || (usym.wri[i].re() > usym.wri[i-1].re());
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, symmetric, interface1: Eigenvalues not sorted in high-to-low order");
      
    }

    //------------------------------------------------------------------------
    // Test Unsymmeig, non-symmetric, interface1
    System.out.println("Testing Unsymmeig, non-symmetric, interface1");
    ranmat(a);
    Unsymmeig usym2 = new Unsymmeig(a,true,false);

//    for (i=0;i<N;i++) System.out.printf(usym2.wri[i]);

    // Test eigenvector/eigenvalue pairs
    for (i=0;i<N;i++) {   // for each eigenvector
      if (usym2.wri[i].im() == 0.0) {
        for (j=0;j<N;j++) vec[j]=usym2.zz[j][i];
        res=matmul(a,vec);
        for (j=0;j<N;j++) vec[j] *= usym2.wri[i].re();
//        System.out.println("real eigenvalue  " << maxel(vecsub(res,vec)));
        localflag = localflag || (maxel(vecsub(res,vec)) > sbeps);
      } else {
        if (usym2.wri[i].im() > 0.0)
          for (j=0;j<N;j++) zvec[j]=new Complex(usym2.zz[j][i],usym2.zz[j][i+1]);
        else
          for (j=0;j<N;j++) zvec[j]=new Complex(usym2.zz[j][i-1],-usym2.zz[j][i]);
        for (j=0;j<N;j++) {
          zres[j]=new Complex(0.,0.);
          for (k=0;k<N;k++) zres[j] = zres[j].add(zvec[k].mul(a[j][k]));
        }
        for (j=0;j<N;j++) zvec[j] = zvec[j].mul(usym2.wri[i]);
        max=0.;
        for (j=0;j<N;j++) max = (max > zres[j].sub(zvec[j]).abs() ? max : zres[j].sub(zvec[j]).abs());
//        System.out.println("imag eigenvalue  " << max);
        localflag = localflag || (max > sbeps);
      }
      max=0.;
      for (j=0;j<N;j++) max = (max > zres[j].sub(zvec[j]).abs() ? max : zres[j].sub(zvec[j]).abs());
      localflag = localflag || (max > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, non-symmetric, interface1: Matrix times eigenvector was not the same as lambda*eigenvector");
      
    }
    
    // Test the sorting of the eigenvalues
    for (i=1;i<N;i++)
      localflag = localflag || (usym2.wri[i].re() > usym2.wri[i-1].re());
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, non-symmetric, interface1: Eigenvalues not sorted in high-to-low order of real part");
      
    }

    for (i=1;i<N;i++)
      localflag = localflag || (usym2.wri[i].im() > 0.0) && (!usym2.wri[i].equals(usym2.wri[i+1].conj()));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, non-symmetric, interface1: A complex eigenvalue with positive imag part is not followed by its conjugate");
      
    }

    //------------------------------------------------------------------------
    // Test Unsymmeig, non-symmetric, interface2
    System.out.println("Testing Unsymmeig, non-symmetric, interface2");
    for (i=0;i<N;i++)
      for (j=0;j<N;j++) 
        a[i][j]= (i > j+1 ? 0.0 : myran.doub()); 
    Unsymmeig usym3 = new Unsymmeig(a,true,true);

//    for (i=0;i<N;i++) System.out.printf(usym3.wri[i]);

    // Test eigenvector/eigenvalue pairs
    for (i=0;i<N;i++) {   // for each eigenvector
      if (usym3.wri[i].im() == 0.0) {
        for (j=0;j<N;j++) vec[j]=usym3.zz[j][i];
        res=matmul(a,vec);
        for (j=0;j<N;j++) vec[j] *= usym3.wri[i].re();
//        System.out.println("real eigenvalue  " << maxel(vecsub(res,vec)));
        localflag = localflag || (maxel(vecsub(res,vec)) > sbeps);
      } else {
        if (usym3.wri[i].im() > 0.0)
          for (j=0;j<N;j++) zvec[j]=new Complex(usym3.zz[j][i],usym3.zz[j][i+1]);
        else
          for (j=0;j<N;j++) zvec[j]=new Complex(usym3.zz[j][i-1],-usym3.zz[j][i]);
        for (j=0;j<N;j++) {
          zres[j]=new Complex(0.,0.);
          for (k=0;k<N;k++) zres[j] = zres[j].add(zvec[k].mul(a[j][k]));
        }
        for (j=0;j<N;j++) zvec[j] = zvec[j].mul(usym3.wri[i]);
        max=0.;
        for (j=0;j<N;j++) max = (max > zres[j].sub(zvec[j]).abs() ? max : zres[j].sub(zvec[j]).abs());
//        System.out.println("imag eigenvalue  " << max);
        localflag = localflag || (max > sbeps);
      }
      max=0.;
      for (j=0;j<N;j++) max = (max > zres[j].sub(zvec[j]).abs() ? max : zres[j].sub(zvec[j]).abs());
      localflag = localflag || (max > sbeps);
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, non-symmetric, interface2: Matrix times eigenvector was not the same as lambda*eigenvector");
      
    }
    
    // Test the sorting of the eigenvalues
    for (i=1;i<N;i++) 
      localflag = localflag || (usym3.wri[i].re() > usym3.wri[i-1].re());
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, non-symmetric, interface2: Eigenvalues not sorted in high-to-low order of real part");
      
    }

    for (i=1;i<N;i++)
      localflag = localflag || (usym3.wri[i].im() > 0.0) && (!usym3.wri[i].equals(usym3.wri[i+1].conj()));
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Unsymmeig, non-symmetric, interface1: A complex eigenvalue with positive imag part is not followed by its conjugate");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
