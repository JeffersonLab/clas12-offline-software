package com.nr.test.test_chapter6;

import static com.nr.NRUtil.buildVector;
import static com.nr.test.NRTestUtil.maxel;
import static com.nr.test.NRTestUtil.vecsub;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.sf.Bessel;
import com.nr.sf.Bessik;
import com.nr.sf.Bessjy;

public class Test_Bessel {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @SuppressWarnings("unused")
  @Test
  public void test() {
    int i,j,N=16;
    double nnu,xx,sbeps=1.e-14;
    double nu[]={0.5,0.5,0.5,0.5,1.5,1.5,1.5,1.5,2.5,2.5,2.5,2.5,3.5,4.5,5.5,6.5};
    double x[]={0.2,0.5,1.0,2.0,0.2,0.5,1.0,2.0,0.2,0.5,1.0,2.0,2.0,2.0,2.0,2.0};
    double u1[]={0.354450744211401103,0.540973789934528091,0.671396707141803090,
      0.513016136561827752,0.0236933040951292395,0.0917016996256513026,
      0.240297839123427011,0.491293778687162345,0.000948817215537489923,
      0.00923640781937972450,0.0494968102284779423,0.223924531468915766,
      0.0685175499851270696,0.0158868934790289778,0.00297347067050333037,
      0.000467195208739339254};
    double u2[]={-1.74856041696187628,-0.990245880243404880,-0.431098868018376080,
      0.234785710406248469,-9.09725282902078249,-2.52146555042133785,
      -1.10249557516017917,-0.395623281358703517,-134.710232018349861,
      -14.1385474222846222,-2.87638785746216143,-0.828220632444303745,
      -1.67492829975205584,-5.03402841668789171,-20.9781995753434569,
      -110.346069247701121};
    double u3[]={0.359208417583361390,0.587993086790416325,0.937674888245487647,
      2.04623686308905504,0.0238836108689015129,0.0964034738340167409,
      0.293525326347479800,1.09947318863310968,0.000954254549838696096,
      0.00957224378631588027,0.0570989092030482474,0.397027080139390523,
      0.106905488284633367,0.0228578711431737382,0.00404506814035154520,
      0.000609996371240239640};
    double u4[]={2.29448933979847487,1.07504760349992024,0.461068504447894558,
      0.119937771968061447,13.7669360387908492,3.22514281049976072,
      0.922137008895789117,0.179906657952092171,208.798529921661213,
      20.4259044664984845,3.22747953113526191,0.389797758896199704,
      1.15440105519259143,4.43020145207026971,21.0903075895088051,
      120.426893194368698};
    double[] uu1=buildVector(u1),uu2=buildVector(u2),uu3=buildVector(u3),uu4=buildVector(u4),zz=new double[N],zz1=new double[N],zz2=new double[N];
    boolean localflag, globalflag=false;

    Bessel bess = new Bessel();
    Bessjy bjy = new Bessjy();
    Bessik bik = new Bessik();

    // Test Bessel (besseljy and besselik)
    System.out.println("Testing Bessel (besseljy)");

    // Test for integer values of nu
    sbeps=1.e-10;
    for (i=0;i<5;i++) {
      nnu=(double)(i);

      for (j=0;j<N;j++) {
        xx=0.2*(j+1);
        if (i == 0) {
          zz1[j]=bjy.j0(xx);
          zz2[j]=bess.jnu(nnu,xx);
        } else if (i == 1) {
          zz1[j]=bjy.j1(xx);
          zz2[j]=bess.jnu(nnu,xx);
        } else {
          zz1[j]=bjy.jn(i,xx);
          zz2[j]=bess.jnu(nnu,xx);
        }
      }
      localflag = maxel(vecsub(zz1,zz2)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Bessel (besseljy,jnu): Incorrect function values, integer order");
        
      }

      for (j=0;j<N;j++) {
        xx=0.2*(j+1);
        if (i == 0) {
          zz1[j]=bjy.y0(xx);
          zz2[j]=bess.ynu(nnu,xx);
        } else if (i == 1) {
          zz1[j]=bjy.y1(xx);
          zz2[j]=bess.ynu(nnu,xx);
        } else {
          zz1[j]=bjy.yn(i,xx);
          zz2[j]=bess.ynu(nnu,xx);
        }
      }
      localflag = maxel(vecsub(zz1,zz2)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Bessel (besseljy,ynu): Incorrect function values, integer order");
        
      }

      for (j=0;j<N;j++) {
        xx=0.2*(j+1);
        if (i == 0) {
          zz1[j]=bik.i0(xx);
          zz2[j]=bess.inu(nnu,xx);
        } else if (i == 1) {
          zz1[j]=bik.i1(xx);
          zz2[j]=bess.inu(nnu,xx);
        } else {
          zz1[j]=bik.in(i,xx);
          zz2[j]=bess.inu(nnu,xx);
        }
      }
      localflag = maxel(vecsub(zz1,zz2)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Bessel (besselik,inu): Incorrect function values, integer order");
        
      }

      for (j=0;j<N;j++) {
        xx=0.2*(j+1);
        if (i == 0) {
          zz1[j]=bik.k0(xx);
          zz2[j]=bess.knu(nnu,xx);
        } else if (i == 1) {
          zz1[j]=bik.k1(xx);
          zz2[j]=bess.knu(nnu,xx);
        } else {
          zz1[j]=bik.kn(i,xx);
          zz2[j]=bess.knu(nnu,xx);
        }
      }
      localflag = maxel(vecsub(zz1,zz2)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Bessel (besselik,knu): Incorrect function values, integer order");
        
      }
    }

    // Test some non-integer values of nu
    // Test Bessel (besseljy)
    System.out.println("Testing Bessel (besseljy),jnu");
    for (i=0;i<N;i++)
      zz[i]=bess.jnu(nu[i],x[i]);
    System.out.printf("Bessel (besseljy),jnu: Maximum discrepancy = %f\n", maxel(vecsub(zz,uu1)));
    localflag = maxel(vecsub(zz,uu1)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessel (besselij),jnu: Incorrect function values, non-integer order");
      
    }

    System.out.println("Testing Bessel (besseljy),ynu");
    for (i=0;i<N;i++)
      zz[i]=bess.ynu(nu[i],x[i]);
    System.out.printf("Bessel (besseljy),ynu: Maximum discrepancy = %f\n", maxel(vecsub(zz,uu2)));
    localflag = maxel(vecsub(zz,uu2)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessel (besseljy),ynu: Incorrect function values, non-integer order");
      
    }

    // Test Bessel (besselik)
    System.out.println("Testing Bessel (besselik),inu");
    for (i=0;i<N;i++) zz[i]=bess.inu(nu[i],x[i]);
    System.out.printf("Bessel (besselik),inu: Maximum discrepancy = %f\n", maxel(vecsub(zz,uu3)));
    localflag = maxel(vecsub(zz,uu3)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessel (besselik),inu: Incorrect function values, non-integer order");
      
    }

    System.out.println("Testing Bessel (besselik),knu");
    for (i=0;i<N;i++) zz[i]=bess.knu(nu[i],x[i]);
    System.out.printf("Bessel (besselik),knu: Maximum discrepancy = %f\n", maxel(vecsub(zz,uu4)));
    localflag = maxel(vecsub(zz,uu4)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessel (besselik),knu: Incorrect function values, non-integer order");
      
    }

    N=17;
    double x3[]={-64.0,-32.0,-16.0,-8.0,-4.0,-2.0,-1.0,-0.5,0.0,
      0.5,1.0,2.0,4.0,8.0,16.0,32.0,64.0};
    double y3[]={6.181292862603287e-2,2.066697536442034e-1,-1.430579316691003e-1,
      -5.270505035638810e-2,-7.026553294929175e-2,2.274074282016857e-1,
      5.355608832923522e-1,4.757280916105395e-1,3.550280538878172e-1,
      2.316936064808334e-1,1.352924163128815e-1,3.492413042327446e-2,
      9.515638512048023e-4,4.692207616099237e-8,4.156888828917034e-020,
      4.606731112410277e-054,-5.748819722355254e-150};
    double y4[]={-1.896519810390566e-1,-1.164350222348790e-1,2.431231514282269e-1,
      -3.312515807511406e-1,3.922347057070000e-1,-4.123025879563988e-1,
      1.039973894969446e-1,3.803526597510537e-1,6.149266274460007e-1,
      8.542770431031557e-1,1.207423594952871,3.298094999978216,
      8.384707140846841e1,1.199586004124440e6,9.572123906049169e17,
      6.107371662410141e51,3.460602472409225e+147};
    double y5[]={1.517458208520610e0,6.602736519705829e-1,-9.747644416212706e-1,
      9.355609381983173e-1,-7.906285753685707e-1,6.182590207416919e-1,
      -1.016056711664515e-2,-2.040816703395474e-1,-2.588194037928068e-1,
      -2.249105326646840e-1,-1.591474412967932e-1,-5.309038443365637e-2,
      -1.958640950204180e-3,-1.341439297906788e-7,-1.669188676838185e-19,
      -2.609547331124277e-53,-4.601298677332503e-149};
    double y6[]={4.937628983497952e-1,1.168196625671367e0,-5.684556059761371e-1,
      -1.594504978129538e-1,-1.166705674383235e-1,2.787951669211703e-1,
      5.923756264227923e-1,5.059337136238472e-1,4.482883573538264e-1,
      5.445725641405924e-1,9.324359333927754e-1,4.100682049932892e0,
      1.619266835046139e2,3.354342312744483e6,3.813743507121854e+018,
      3.450063137771686e+052,2.767128525086472e+148};
    double[] yy3=buildVector(y3),yy4=buildVector(y4),yy5=buildVector(y5),yy6=buildVector(y6),one=buildVector(N,1.0),
      zz3=new double[N],zz4=new double[N],zz5=new double[N],zz6=new double[N];

    /*

    Bessel2 bess2 = new Bessel2();

    // Test Bessel (airy)
    sbeps=1.e-12;
    System.out.println("Testing Bessel (airy_ai, airy_bi, airy_aip, airy_bip)");
    for (i=0;i<N;i++) {
      zz3[i]=bess2.airy_ai(x3[i]);
      zz4[i]=bess2.airy_bi(x3[i])/yy4[i];
      zz5[i]=bess2.airy_aip(x3[i]);
      zz6[i]=bess2.airy_bip(x3[i])/yy6[i];
    }
    System.out.printf("Bessel (airy_ai): Maximum discrepancy = %f\n", maxel(vecsub(zz3,yy3)));
    localflag = maxel(vecsub(zz3,yy3)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessel (airy_ai): Incorrect function values");
      
    }
    System.out.printf("Bessel (airy_bi): Maximum discrepancy = %f\n", maxel(vecsub(one,zz4)));
    localflag = maxel(vecsub(one,zz4)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessel (airy_bi): Incorrect function values");
      
    }
    System.out.printf("Bessel (airy_aip): Maximum discrepancy = %f\n", maxel(vecsub(zz5,yy5)));
    localflag = maxel(vecsub(zz5,yy5)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessel (airy_aip): Incorrect function values");
      
    }
    System.out.printf("Bessel (airy_bip): Maximum discrepancy = %f\n", maxel(vecsub(one,zz6)));
    localflag = maxel(vecsub(one,zz6)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** Bessel (airy_bip): Incorrect function values");
      
    }

    // Test Bessel (sphbes)
    System.out.println("Testing Bessel (sphbes)");

    int M=5;
    N=10;
    double[] yy7=new double[N],yy8=new double[N],zz7=new double[N],zz8=new double[N];
    double sqrtpio2=sqrt(acos(-1.0)/2);
    sbeps=6.e-11;
    for (j=0;j<M;j++) {

      // Spherical Bessel function jn
      for (i=0;i<N;i++) {
        zz7[i]=bess2.sphbesj(j,x[i]);
        yy7[i]=sqrtpio2*bess2.jnu((double)(j)+0.5,x[i])/sqrt(x[i]);
      }
      System.out.printf("Bessel (sphbes), Jnu: Maximum discrepancy = %f\n", maxel(vecsub(zz7,yy7)));
      localflag = maxel(vecsub(zz7,yy7)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Bessel (sphbes), Jnu: Incorrect function values");
        
      }

      // Spherical Bessel function yn
      for (i=0;i<N;i++) {
        zz8[i]=bess2.sphbesy(j,x[i]);
        yy8[i]=sqrtpio2*bess2.ynu((double)(j)+0.5,x[i])/sqrt(x[i]);
      }
      System.out.printf("Bessel (sphbes), Ynu: Maximum discrepancy = %f\n", maxel(vecsub(zz8,yy8)));
      localflag = maxel(vecsub(zz8,yy8)) > sbeps;
      globalflag = globalflag || localflag;
      if (localflag) {
        fail("*** Bessel (sphbes), Ynu: Incorrect function values");
        
      }
    }
    */
    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }
  
  class Bessel2 extends Bessel
  {
    double airy_aip(final double x) {
    // Simple interface returning Ai'(x)
      // if (x != xo) airy(x); // XXX HWH disable it  
      return aipo;
    }

    double airy_bip(final double x) {
    // Simple interface returning Bi'(x)
      // if (x != xo) airy(x); // XXX HWH disable it  
      return bipo;
    }
  };
}
