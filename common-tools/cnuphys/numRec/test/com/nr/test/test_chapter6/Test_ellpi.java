package com.nr.test.test_chapter6;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.nr.test.NRTestUtil.*;
import static com.nr.sf.Elliptic.*;

public class Test_ellpi {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=16,M=10;
    double sbeps,en;
    double ellpi1[]={0.0,0.100502764417104114,0.204089873528317272,0.314201094547181115,
      0.435071344871511048,0.572373236468860396,0.734271021897322517,
      0.933304100500074239,1.19002091819793195,1.54058608179260362,
      2.05433293325624867,2.87964064018147283,4.38603767215661825,
      7.72963041510352791,18.2848934716992433,101.345047515285793};
    double ellpi2[]={0.577350269725227267,0.578430405929852887,0.581733633663630267,
      0.5874607596963754851,0.595993115974939418,0.607986406118820100,
      0.624573540241628753,0.647846914950448347,0.682245757590558980,
      0.740309437183358951};
    double ellpi3[]={0.0,0.0501255025975285727,0.1010163386701090373,
      0.153502472640551107,0.208558872308446512,0.267425749123095321,
      0.331818001829399680,0.404347204331590915,0.489536097087013840,
      0.597017539786347541,0.759657207054964912};
    double ellpi4[]={0.0,0.0998354001533643479,0.198731283496840194,
      0.295972908157878589,0.391232663483947057,0.484645541930822404,
      0.576819001486806192,0.668825019188343865,0.762228160389261813,
      0.859207265936498094,0.962856407616018361,1.077849756916385261,
      1.211968082663659937,1.380119771567129244,1.61803271724411808,
      2.06241081470741402};
    double ellpi5[]={0.484169591972923381,0.484961714960443253,0.487381591588874436,
      0.491568024729042223,0.497783554574451842,0.506476877038506087,
      0.518416731663483363,0.535007150197453044,0.559183303289873315,
      0.599067832583639964};
    double ellpi6[]={0.0,0.0500419596493322059,0.100342838645592538,
      0.151198919592696644,0.202989354783613223,0.256242731928117456,
      0.311751967994650342,0.370807081967643963,0.435763040026915024,
      0.511863662072828734,0.617791316339181780};
    double[] a= new double[N],b= new double[N],c=new double[M],d=new double[M];
    boolean localflag, globalflag=false;

    

    // Test ellpi
    System.out.println("Testing ellpi");

    // Test values vs. phi for k=1 and n=-1
    en=-1.0;
    sbeps=1.e-13;
    for (i=0;i<N;i++) {
      a[i]=ellpi(0.1*i,en,1.0);
      b[i]=ellpi1[i];
//      System.out.printf(a[i] << " %f\n", b[i]);
    }
//    System.out.println("ellpi: Maximum discrepancy = %f\n", maxel(vecsub(a,b)));
    localflag = maxel(vecsub(a,b)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellpi: Incorrect function of phi for k=1 and n=-1");
      
    }

    // Test values vs. k for phi=0.523598776 (approx 30 deg) and n=-1
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      c[i]=ellpi(0.523598776,en,0.2*i);
      d[i]=ellpi2[i];
    }
//    System.out.println("ellpi: Maximum discrepancy = %f\n", maxel(vecsub(c,d)));
    localflag = maxel(vecsub(c,d)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellpi: Incorrect function of k for phi=0.523598776 (30 deg) and n=-1");
      
    }

    // Test values vs. k for phi=0.523598776 (approx 30 deg) and n=-1
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      c[i]=ellpi(0.05*i,en,2.0);
      d[i]=ellpi3[i];
    }
//    System.out.println("ellpi: Maximum discrepancy = %f\n", maxel(vecsub(c,d)));
    localflag = maxel(vecsub(c,d)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellpi: Incorrect function of phi for k=2 and n=-1");
      
    }

    // Test values vs. phi for k=1 and n=+1
    en=+1.0;
    sbeps=1.e-14;
    for (i=0;i<N;i++) {
      a[i]=ellpi(0.1*i,en,1.0);
      b[i]=ellpi4[i];
//      System.out.printf(a[i] << " %f\n", b[i]);
    }
//    System.out.println("ellpi: Maximum discrepancy = %f\n", maxel(vecsub(a,b)));
    localflag = maxel(vecsub(a,b)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellpi: Incorrect function of phi for k=1 and n=+1");
      
    }

    // Test values vs. k for phi=0.523598776 (approx 30 deg) and n=+1
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      c[i]=ellpi(0.523598776,en,0.2*i);
      d[i]=ellpi5[i];
    }
//    System.out.println("ellpi: Maximum discrepancy = %f\n", maxel(vecsub(c,d)));
    localflag = maxel(vecsub(c,d)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellpi: Incorrect function of k for phi=0.523598776 (30 deg) and n=+1");
      
    }

    // Test values vs. k for phi=0.523598776 (approx 30 deg) and n=+1
    sbeps=1.e-15;
    for (i=0;i<M;i++) {
      c[i]=ellpi(0.05*i,en,2.0);
      d[i]=ellpi6[i];
    }
//    System.out.println("ellpi: Maximum discrepancy = %f\n", maxel(vecsub(c,d)));
    localflag = maxel(vecsub(c,d)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** ellpi: Incorrect function of phi for k=2 and n=+1");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
