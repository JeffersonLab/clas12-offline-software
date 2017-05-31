package ann.cnuphys.edu.layer;


import java.util.ArrayList;
import java.util.Arrays;

import ann.cnuphys.edu.neuron.Neuron;

public class InputLayer extends Layer {

	public InputLayer initLayer(InputLayer inputLayer) {
		
		ArrayList<Double> listOfWeightInTemp = new ArrayList<Double>();
		ArrayList<Neuron> listOfNeurons = new ArrayList<Neuron>();
		
		for (int i = 0; i < inputLayer.getNumberOfNeuronsInLayer(); i++) {
			Neuron neuron = new Neuron();
			
			listOfWeightInTemp.add( neuron.initNeuron() );

			neuron.setListOfWeightIn( listOfWeightInTemp );
			listOfNeurons.add( neuron );

			listOfWeightInTemp = new ArrayList<Double>();
		}

		inputLayer.setListOfNeurons(listOfNeurons);

		return inputLayer;
	}

	public void printLayer(InputLayer inputLayer){
		System.out.println("### INPUT LAYER ###");
		int n = 1;
		for (Neuron neuron : inputLayer.getListOfNeurons()) {
			System.out.println("Neuron #" + n + ":");
			System.out.println("Input Weights:");
			System.out.println(Arrays.deepToString( neuron.getListOfWeightIn().toArray() ));
			n++;
		}
	}
	
	
	public void setNumberOfNeuronsInLayer(int numberOfNeuronsInLayer) {
		_numberOfNeurons = numberOfNeuronsInLayer + 1; //BIAS
	}
	
}
