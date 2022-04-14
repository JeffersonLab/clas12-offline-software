package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.*;

public class PhysicalConstants {


    static final double Avogadro = 6.02214076e+23/mole;

    //
    // c   = 299.792458 mm/ns
    // c^2 = 898.7404 (mm/ns)^2
    //
    public static final double c_light   = 2.99792458e+8 * m/s;
    static final double c_squared = c_light * c_light;

    //
    // h     = 4.13566e-12 MeV*ns
    // hbar  = 6.58212e-13 MeV*ns
    // hbarc = 197.32705e-12 MeV*mm
    //
    static final double h_Planck      = 6.62607015e-34 * joule*s;
    static final double hbar_Planck   = h_Planck/twopi;
    static final double hbarc         = hbar_Planck * c_light;
    static final double hbarc_squared = hbarc * hbarc;

    //
    //
    //
    static final double electron_charge = - eplus; // see SystemOfUnits.h
    static final double e_squared = eplus * eplus;

    //
    // amu_c2 - atomic equivalent mass unit
    //        - AKA, unified atomic mass unit (u)
    // amu    - atomic mass unit
    //
    static final double electron_mass_c2 = 0.510998910 * MeV;
    public static final double   proton_mass_c2 = 938.272013 * MeV;
    static final double  neutron_mass_c2 = 939.56536 * MeV;
    static final double           amu_c2 = 931.494028 * MeV;
    static final double              amu = amu_c2/c_squared;

    //
    // permeability of free space mu0    = 2.01334e-16 Mev*(ns*eplus)^2/mm
    // permittivity of free space epsil0 = 5.52636e+10 eplus^2/(MeV*mm)
    //
    static final double mu0      = 4*pi*1.e-7 * henry/m;
    static final double epsilon0 = 1./(c_squared*mu0);

    //
    // electromagnetic coupling = 1.43996e-12 MeV*mm/(eplus^2)
    //
    static final double elm_coupling           = e_squared/(4*pi*epsilon0);
    static final double fine_structure_const   = elm_coupling/hbarc;
    static final double classic_electr_radius  = elm_coupling/electron_mass_c2;
    static final double electron_Compton_length = hbarc/electron_mass_c2;
    static final double Bohr_radius = electron_Compton_length/fine_structure_const;

    static final double alpha_rcl2 = fine_structure_const
            *classic_electr_radius
            *classic_electr_radius;

    static final double twopi_mc2_rcl2 = twopi*electron_mass_c2
            *classic_electr_radius
            *classic_electr_radius / 100;

    static final double Bohr_magneton = (eplus*hbarc*c_light)/(2*electron_mass_c2);
    static final double nuclear_magneton = (eplus*hbarc*c_light)/(2*proton_mass_c2);

    //
    //
    //
    static final double k_Boltzmann = 8.617333e-11 * MeV/kelvin;

    //
    //
    //
    static final double STP_Temperature = 273.15*kelvin;
    public static final double STP_Pressure    = atmosphere;
    static final double kGasThreshold   = 10.*mg/cm3;

    //
    //
    //
    static final double universe_mean_density = 1.e-25*g/cm3;


}
