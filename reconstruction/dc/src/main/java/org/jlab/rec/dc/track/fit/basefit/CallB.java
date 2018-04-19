/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.dc.track.fit.basefit;

import java.util.Random;

import org.jlab.rec.dc.trajectory.DCSwimmer;

import cnuphys.magfield.MagneticFields;
/**
 *
 * @author ziegler
 */
public class CallB {
    private Random aRandom;
    private DCSwimmer swim;
    public void testB() {
        aRandom = new Random();
        swim = new DCSwimmer();
       
        float[] result = new float[3];
        double x=0; double y=0; double z=0;
        //while (true) {
           // x = 10+100*aRandom.nextDouble();
            //y= 10*aRandom.nextDouble();
            //z=200+200*aRandom.nextDouble();
            x=31; y=0; z=300;
           // swim.Bfield(x, y, z, result);
            System.out.println(result[0]+", "+result[1]+" , "+result[2]+" = ? ");
            MagneticFields.getInstance().initializeMagneticFields();
            MagneticFields.getInstance().setActiveField(MagneticFields.FieldType.COMPOSITEROTATED);
            MagneticFields.getInstance().getActiveField().field((float)x, (float)y, (float)z, result);
            System.out.println(result[0]+", "+result[1]+" , "+result[2]+"  "+
                    MagneticFields.getInstance().getActiveField().getName());
        //}
    }
    public static void main(String[] args)  {

        DCSwimmer.getMagneticFields(11);
        DCSwimmer.setMagneticFieldsScales(0.0, -1.0, 0);
        DCSwimmer swim2 = new DCSwimmer();
        swim2.SetSwimParameters(0, 0, 0, 2*Math.sin(Math.toRadians(20.-25.)), 0, 2*Math.cos(Math.toRadians(20.-25)), -1);
        double[]swimVal =swim2.SwimToPlane(500);
        for(int i = 0; i<swimVal.length; i++)
            System.out.println("swimVal["+i+"]= "+swimVal[i]);
        //CallB en = new CallB();
        //en.testB();
    }
    
}
