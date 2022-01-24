package org.jlab.rec.rtpc.KF;

import java.util.Objects;

public class Material {

    double I; // in MeV
    double rho; // g cm^{-3}
    double ZA;
    double X0; // cm
    String name;

    public Material() {
    }

    public Material(double I, double rho, double ZA, double X0 , String name) {
        this.I = I;
        this.rho = rho;
        this.ZA = ZA;
        this.X0 = X0;
        this.name = name;
    }

    public Material(String name) throws Exception {
        if (Objects.equals(name, "Deuterium")){
            this.I = 19.2 * 1e-6;
            this.rho =  1.26 * 1e-3;
            this.ZA = 0.5;
            this.X0 = 125.98 / this.rho;
            this.name = name;
        }
        else if (Objects.equals(name, "Kapton")){
            this.I = 79.6 * 1e-6;
            this.rho = 1.42;
            this.ZA = 0.51264;
            this.X0 = 28.57;
            this.name = name;
        }
        else if (Objects.equals(name, "HeCO2")){
            this.I = 22.4 * 1e-6;
            this.rho = 0.8 * 0.0001664 + 0.2 * 0.00184212;
            this.ZA = 0.49984;
            double w_He = 4 * 4.0026 / (4 * 4.0026 + 12.0107 + 2 * 15.999);
            double w_C = 12.0107 / (4 * 4.0026 + 12.0107 + 2 * 15.999);
            double w_O = 2 * 15.999 / (4 * 4.0026 + 12.0107 + 2 * 15.999);
            double w_CO2 = w_C + 2 * w_O;
            double X0_He = 5.671E+05; // cm
            double X0_C02 = 1.965E+04; // cm
            this.X0 = 1 / (w_He / X0_He + w_CO2 / X0_C02);
            this.name = name;
        }
        else if (Objects.equals(name, "Mylar")){
            this.I = 78.7 * 1e-6;
            this.rho = 1.400;
            this.ZA = 0.52037;
            this.X0 = 28.54;
            this.name = name;
        }
        else {
            throw new Exception("Wrong name of material");
        }

    }

}

