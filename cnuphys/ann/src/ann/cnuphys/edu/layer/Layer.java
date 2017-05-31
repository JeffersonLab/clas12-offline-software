package ann.cnuphys.edu.layer;


import java.util.ArrayList;

import ann.cnuphys.edu.neuron.Neuron;

public abstract class Layer {

	//a list of neurons
	private ArrayList<Neuron> _neurons;
	
	//the number of neurons in the layer
	protected int _numberOfNeurons;
	
	/**
	 * Print the layer
	 */
	public void printLayer(){
	}

	/**
	 * Getter for the layer's list of neurons
	 * @return the list of neurons for this layer
	 */
	public ArrayList<Neuron> getListOfNeurons() {
		return _neurons;
	}

	/**
	 * Set the list of neurons for the layer
	 * @param listOfNeuronsnthe list of neurons
	 */
	public void setListOfNeurons(ArrayList<Neuron> listOfNeurons) {
		this._neurons = listOfNeurons;
	}

	/**
	 * Get the number of neurons in the layer
	 * @return the number of neurons in the layer
	 */
	public int getNumberOfNeuronsInLayer() {
		return _numberOfNeurons;
	}

	/**
	 * Set the number of neurons in the layer
	 * @param numberOfNeuronsInLayer the number of neurons
	 */
	public void setNumberOfNeuronsInLayer(int numberOfNeuronsInLayer) {
		this._numberOfNeurons = numberOfNeuronsInLayer;
	}
	
	
	
	
}
