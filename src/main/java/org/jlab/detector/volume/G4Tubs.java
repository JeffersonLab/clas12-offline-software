package org.jlab.detector.volume;

import org.jlab.detector.units.SystemOfUnits.Angle;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.geometry.prim.Tube;

/**
 * @author pdavies
 */

public class G4Tubs extends Geant4Basic {

	public G4Tubs( String name, double rmin, double rmax, double zlen, double phi0, double dphi )
	{
		super( new Tube( rmin, rmax, zlen, phi0, dphi ) );
		setName( name );
		setType("Tube");
		setDimensions( Length.value(rmin), Length.value(rmax), Length.value(zlen), Angle.value(phi0), Angle.value(dphi) );
	}

}
