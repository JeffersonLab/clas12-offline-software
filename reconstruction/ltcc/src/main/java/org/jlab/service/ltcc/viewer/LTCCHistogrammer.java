/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.ltcc.viewer;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;

/**
 *
 * @author Sylvester Joosten
 * @param <DataType>
 */
public class LTCCHistogrammer<DataType> {
    // generic histogram type that knows how to obtain its own data
    // from an object of the type DataType
    private class SmartHisto<Histo, FillArg> {
        private final Histo histo;
        private final Function<DataType, FillArg> getter;
        SmartHisto(Histo h, Function<DataType, FillArg> g) {
            this.histo = h;
            this.getter = g;
        }
        private Histo getHisto() {
            return histo;
        }
        
        private FillArg getData(DataType data) {
            return getter.apply(data);
        }
    }
    
    private final Map<String, SmartHisto<H1F, Double>> histos1D;
    private final Map<String, SmartHisto<H2F, Pair<Double, Double>>> histos2D;
    
    LTCCHistogrammer() {
        histos1D = new LinkedHashMap();
        histos2D = new LinkedHashMap();
    }
    
    public void add(H1F histo, Function<DataType, Double> getter) {
        histos1D.put(histo.getName(), new SmartHisto(histo, getter));
    }
    public void add(H2F histo, Function<DataType, Pair<Double, Double>> getter) {
        histos2D.put(histo.getName(), new SmartHisto(histo, getter));
    }
    
    public void fill(DataType data) {
        fill(data, 1.);
    }
    public void fill(DataType data, double weight) {      
        histos1D.forEach((k, v) -> v.getHisto().fill(v.getData(data), weight));
        histos2D.forEach((k, v) -> {
            Pair<Double, Double> args = v.getData(data);
            v.getHisto().fill(args.getKey(), args.getValue(), weight);
        });
    }
    public H1F getH1F(String name) {
        return histos1D.get(name).getHisto();
    }
    public H2F getH2F(String name) {
        return histos2D.get(name).getHisto();
    }
    public List<H1F> getH1Fs() {
        return histos1D.values()
                .stream()
                .map(SmartHisto::getHisto)
                .collect(Collectors.toList());
    }
    public List<H2F> getH2Fs() {
        return histos2D.values()
                .stream()
                .map(SmartHisto::getHisto)
                .collect(Collectors.toList());
    }
}
