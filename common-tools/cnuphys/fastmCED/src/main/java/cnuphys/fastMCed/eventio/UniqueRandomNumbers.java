package cnuphys.fastMCed.eventio;

import java.util.ArrayList;
import java.util.Collections;

public class UniqueRandomNumbers {

	private ArrayList<Integer> _list;

	/**
	 * Create an object used for a random list of unique integers
	 * 
	 * @param min the min value
	 * @param max the max valye
	 */
	public UniqueRandomNumbers(int min, int max) {
		_list = new ArrayList<Integer>(max - min + 1);
		for (int i = min; i <= max; i++) {
			_list.add(i);
		}
	}

	/**
	 * Return the randomized list
	 * 
	 * @return the randomized list
	 */
	public ArrayList<Integer> getRandomList() {
		Collections.shuffle(_list);
		return _list;
	}

	public static void main(String arg[]) {
		UniqueRandomNumbers urn = new UniqueRandomNumbers(0, 111);

		for (int i = 0; i < 10; i++) {
			ArrayList<Integer> list = urn.getRandomList();

			for (int j = 0; j < 5; j++) {
				System.out.print(" " + list.get(j));
			}
			System.out.println();
		}
	}
}
