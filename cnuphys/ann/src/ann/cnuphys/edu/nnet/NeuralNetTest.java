package ann.cnuphys.edu.nnet;

import java.util.Arrays;

import ann.cnuphys.edu.training.Training.ActivationFncENUM;
import ann.cnuphys.edu.training.Training.TrainingTypesENUM;

public class NeuralNetTest {

	public static void main(String[] args) {
		NeuralNetTest test = new NeuralNetTest();
		
		test.testPerceptron();
		
		test.testAdaline();
	}

	private void testPerceptron() {
		NeuralNet testNet = new NeuralNet();

		testNet = testNet.initNet(2, 0, 0, 1);

		System.out.println("---------PERCEPTRON INIT NET---------");

		testNet.printNet(testNet);

		NeuralNet trainedNet = new NeuralNet();

		// first column has BIAS
		testNet.setTrainSet(new double[][] { { 1.0, 0.0, 0.0 },
				{ 1.0, 0.0, 1.0 }, { 1.0, 1.0, 0.0 }, { 1.0, 1.0, 1.0 } });
		testNet.setRealOutputSet(new double[] { 0.0, 0.0, 0.0, 1.0 });
		testNet.setMaxEpochs(10);
		testNet.setTargetError(0.002);
		testNet.setLearningRate(1.0);
		testNet.setTrainType(TrainingTypesENUM.PERCEPTRON);
		testNet.setActivationFnc(ActivationFncENUM.STEP);

		trainedNet = testNet.trainNet(testNet);

		System.out.println();
		System.out.println("---------PERCEPTRON TRAINED NET---------");

		testNet.printNet(trainedNet);

		System.out.println();
		System.out.println("---------PERCEPTRON PRINT RESULT---------");

		testNet.printTrainedNetResult(trainedNet);
	}

	private void testAdaline() {

		NeuralNet testNet = new NeuralNet();

		testNet = testNet.initNet(3, 0, 0, 1);

		System.out.println("---------ADALINE INIT NET---------");

		testNet.printNet(testNet);
		
		NeuralNet trainedNet = new NeuralNet();

		// first column has BIAS
		testNet.setTrainSet(new double[][] { { 1.0, 0.98, 0.94, 0.95 },
				{ 1.0, 0.60, 0.60, 0.85 }, { 1.0, 0.35, 0.15, 0.15 },
				{ 1.0, 0.25, 0.30, 0.98 }, { 1.0, 0.75, 0.85, 0.91 },
				{ 1.0, 0.43, 0.57, 0.87 }, { 1.0, 0.05, 0.06, 0.01 } });
		testNet.setRealOutputSet(new double[] { 0.80, 0.59, 0.23, 0.45, 0.74,
				0.63, 0.10 });
		testNet.setMaxEpochs(10);
		testNet.setTargetError(0.0001);
		testNet.setLearningRate(0.5);
		testNet.setTrainType(TrainingTypesENUM.ADALINE);
		testNet.setActivationFnc(ActivationFncENUM.LINEAR);

		trainedNet = new NeuralNet();
		trainedNet = testNet.trainNet(testNet);

		System.out.println();
		System.out.println("---------ADALINE TRAINED NET---------");

		testNet.printNet(trainedNet);

		System.out.println();
		System.out.println("---------ADALINE PRINT RESULT---------");

		testNet.printTrainedNetResult(trainedNet);
		
		System.out.println();
		System.out.println("---------ADALINE MSE BY EPOCH---------");
		System.out.println( Arrays.deepToString( trainedNet.getListOfMSE().toArray() ).replace(" ", "\n") );
		

	}
}
