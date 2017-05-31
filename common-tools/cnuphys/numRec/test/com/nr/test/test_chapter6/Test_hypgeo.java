package com.nr.test.test_chapter6;

import static com.nr.sf.Hypergeo.hypgeo;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;

public class Test_hypgeo {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20,M=100;
    double err,errmax,sbeps;
    Complex aa,bb,cc,zz,res1,res2;
    Complex a[]={new Complex(1.0,0.0), new Complex(0.0,1.0),new Complex(1.0,0.0),new Complex(1.0,0.0),
      new Complex(1.0,0.0),new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(1.0,0.0),new Complex(0.0,1.0),new Complex(1.0,0.0),new Complex(1.0,0.0),
      new Complex(5.0,0.0),new Complex(0.0,3.0),new Complex(0.0,-4.0),new Complex(-5.0,0.0)};
    Complex b[]={new Complex(1.0,0.0),new Complex(1.0,0.0),new Complex(0.0,1.0),new Complex(1.0,0.0),
      new Complex(1.0,0.0),new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(1.0,0.0),new Complex(0.0,1.0),new Complex(0.0,1.0),new Complex(1.0,0.0),
      new Complex(0.0,5.0),new Complex(2.0,0.0),new Complex(0.0,-1.0),new Complex(3.0,0.0)};
    Complex c[]={new Complex(1.0,0.0),new Complex(1.0,0.0),new Complex(1.0,0.0),new Complex(0.0,1.0),
      new Complex(1.0,0.0),new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(1.0,0.0),new Complex(0.0,1.0),new Complex(1.0,0.0),new Complex(0.0,1.0),
      new Complex(0.0,-2.0),new Complex(0.0,-2.0),new Complex(7.0,0.0),new Complex(0.0,7.0)};   
    Complex z[]={new Complex(0.5,0.0),new Complex(0.5,0.0),new Complex(0.5,0.0),new Complex(0.5,0.0),
      new Complex(0.0,1.0),new Complex(0.0,1.0),new Complex(0.0,1.0),new Complex(0.0,0.0),
      new Complex(0.5,0.0),new Complex(0.5,0.0),new Complex(-0.5,0.0),new Complex(-0.5,0.0),
      new Complex(-0.5,0.0),new Complex(0.0,1.0),new Complex(1.0,1.0),new Complex(1.0,1.0),
      new Complex(1.0,1.0),new Complex(1.0,3.0),new Complex(2.0,-3.0),new Complex(5.0,7.0)};
    Complex eexpect[]={new Complex(2.0,0.0),
      new Complex(0.76923890136397212658,0.63896127631363480115),
      new Complex(0.76923890136397212658,0.63896127631363480115),
      new Complex(0.18874993960184887345,-0.73280804956611519935),
      new Complex(0.5,0.5),new Complex(0.0,0.5),new Complex(-0.25,0.25),
      new Complex(1.0,0.0),new Complex(4.0,0.0),new Complex(8.0,0.0),
      new Complex(0.29629629629629629630,0.0),
      new Complex(0.44444444444444444444,0.0),
      new Complex(0.66666666666666666667,0.0),
      new Complex(0.42882900629436784932,-0.15487175246424677819),
      new Complex(0.20787957635076190855,0.0),
      new Complex(2.4639512200927103386,5.0258643859042736965),
      new Complex(0.000782175555099748119,0.065075199065027035764),
      new Complex(-0.31205397840397702583,-0.04344693995132976350),
      new Complex(0.34836454596486382673,0.65394630061667130711),
      new Complex(617.9369000550522997,2595.6638964158808010)};
    Complex[] y=new Complex[N],expect=new Complex[N];
    System.arraycopy(eexpect, 0,expect,0, N);;
    boolean localflag, globalflag=false;

    

    // Test selected values
    System.out.println("Testing hypgeo");
    errmax=0.0;
    for (i=0;i<N;i++) {
      y[i]=hypgeo(a[i],b[i],c[i],z[i]);
      err=y[i].sub(expect[i]).abs()/expect[i].abs();
//      System.out.printf(y[i] << "  %f\n", expect[i] << "  %f\n", err);
      if (err > errmax) errmax=err;
    }
//    System.out.println("hypgeo: Maximum fractional discrepancy = %f\n", errmax);
    sbeps=1.e-10;
    localflag = errmax > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Incorrect function values");
      
    }

    // Test symmetry of a and b
    sbeps=1.e-15;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(0.5,5.0-0.1*i);
      cc=new Complex(-5.0+0.1*i,1.0);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);

      res1=hypgeo(aa,bb,cc,zz);
      res2=hypgeo(bb,aa,cc,zz);
//      System.out.printf(abs(res1-res2));
      localflag = localflag || res1.sub(res2).abs() > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Lack of symmetry in first two arguments");
      
    }

    // Test case a=c, b=1 where hypgeo=1/(1-z)
    sbeps=1.e-12;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(1.0,0.0);
      cc=new Complex(-0.5,-5.0+0.1*i);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);

      res1=hypgeo(aa,bb,cc,zz);
      res2=new Complex(1.0).div(new Complex(1.0).sub(zz));
//      System.out.printf(abs(res1-res2));
      localflag = localflag || res1.sub(res2).abs() > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Failure of special case a=c, b=1");
      
    }
    
    // Test a contiguous relationship for aa (Abromowitz & Stegun, 15.2.13)
    sbeps=1.e-12;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(1.0,0.0);
      cc=new Complex(-0.5,-5.0+0.1*i);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);
      
      //res1=(cc-2.0*aa-(bb-aa)*zz)*hypgeo(aa,bb,cc,zz)
      //  + aa*(1.0-zz)*hypgeo(aa+1.0,bb,cc,zz);
      Complex r = bb.sub(aa).mul(zz);
      r = cc.sub(aa.mul(2.0)).sub(r);
      r = r.mul(hypgeo(aa,bb,cc,zz));
      res1 = r.add( aa.mul(new Complex(1.0).sub(zz)).mul(
          hypgeo(aa.add(new Complex(1.0)),bb,cc,zz)) );
      
      //res2=(cc-aa)*hypgeo(aa-1.0,bb,cc,zz);
      //      System.out.printf(abs(res1-res2));
      r = cc.sub(aa);
      res2 = r.mul(hypgeo(aa.sub(new Complex(1.0)),bb,cc,zz));
      localflag = localflag || res1.sub(res2).abs() > sbeps;
      
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Failure of contiguous relationship for aa");
      
    }
    
    // Test a contiguous relationship for cc (Abromowitz & Stegun, 15.2.27)
    sbeps=1.e-10;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(1.0,0.0);
      cc=new Complex(-0.5,-5.0+0.1*i);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);
      Complex r = cc.mul(2.0).sub(aa).sub(bb).sub(new Complex(1.0));
      r = cc.sub(new Complex(1.0)).sub(r.mul(zz));
      r = cc.mul(r).mul(hypgeo(aa,bb,cc,zz));
      Complex rr =(cc.sub(aa)).mul(cc.sub(bb)).mul(zz).mul(hypgeo(aa,bb,cc.sub(new Complex(1.0)),zz));
      res1 = r.add(rr);
      //res1=cc*(cc-1.0-(2.0*cc-aa-bb-1.0)*zz)*hypgeo(aa,bb,cc,zz)
      //  + (cc-aa)*(cc-bb)*zz*hypgeo(aa,bb,cc+1.0,zz);
      res2 = cc.mul(cc.sub(new Complex(1.0))).mul(new Complex(1.0).sub(zz));
      res2 = res2.mul(hypgeo(aa,bb,cc.sub(new Complex(1.0)),zz));
      // res2=cc*(cc-1.0)*(1.0-zz)*hypgeo(aa,bb,cc-1.0,zz);
      
//      System.out.printf(abs(res1-res2));
      localflag = localflag || res1.sub(res2).abs() > sbeps;
      
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Failure of contiguous relationship for cc");
      
    }
    
    // Test of mirror symmetry
    sbeps=1.e-15;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(0.5,5.0-0.1*i);
      cc=new Complex(-5.0+0.1*i,1.0);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);

      res1=hypgeo(aa,bb,cc,zz);
      res2=hypgeo(aa.conj(),bb.conj(),cc.conj(),zz.conj());
//      System.out.printf(res1 << " %f\n", res2);
      localflag = localflag || res1.sub(res2.conj()).abs() > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Function does not follow mirror symmetry rule");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
