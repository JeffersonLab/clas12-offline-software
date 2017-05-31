package ann.cnuphys.edu.neuron;


import java.util.ArrayList;
import java.util.Random;

public class Neuron {

	//the input and output weights
	private ArrayList<Double> _listOfWeightIn;
	private ArrayList<Double> _listOfWeightOut;
	
	/**
	 * Used to initialize the neuron with random numbers
	 * @return
	 */
	public double initNeuron(){
		Random r = new Random();
		return r.nextDouble();
	}

	/**
	 * Getter for the input weights
	 * @return the input weights
	 */
	public ArrayList<Double> getListOfWeightIn() {
		return _listOfWeightIn;
	}

	/**
	 * Setter for the input weights
	 * @param listOfWeightIn the input weights
	 */
	public void setListOfWeightIn(ArrayList<Double> listOfWeightIn) {
		_listOfWeightIn = listOfWeightIn;
	}

	/**
	 * Getter for the output weights
	 * @return the output weights
	 */
	public ArrayList<Double> getListOfWeightOut() {
		return _listOfWeightOut;
	}

	/**
	 * Setter for the output weights
	 * @param listOfWeightOut the input weights
	 */
	public void setListOfWeightOut(ArrayList<Double> listOfWeightOut) {
		_listOfWeightOut = listOfWeightOut;
	}
	
}