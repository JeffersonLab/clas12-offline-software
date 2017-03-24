package org.jlab.detector.volume;

import org.jlab.detector.units.SystemOfUnits.Angle;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.geometry.prim.Tube;

/**
 * @author pdavies
 */

public class G4Tubs extends Geant4Basic {

	public G4Tubs( String name, double rmin, double rmax, double zhalflen, double phi0, double dphi )
	{
		super( new Tube( rmin, rmax, zhalflen, phi0, dphi ) );
		setName( name );
		setType("Tube");
		setDimensions( Length.value(rmin), Length.value(rmax), Length.value(zhalflen), Angle.value(phi0), Angle.value(dphi) );
	}
	
    public double getRMin() {
        return volumeDimensions.get(0).value;
    }

    public double getRMax() {
        return volumeDimensions.get(1).value;
    }

    public double getZHalfLength() {
        return volumeDimensions.get(2).value;
    }
    
    public double getPhiStart() {
        return volumeDimensions.get(3).value;
    }

    public double getPhiDelta() {
        return volumeDimensions.get(4).value;
    }

}
