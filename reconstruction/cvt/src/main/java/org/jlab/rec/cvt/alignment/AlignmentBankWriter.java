package org.jlab.rec.cvt.alignment;

import java.util.List;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author spaul
 *
 */
public class AlignmentBankWriter {
	
	public void write_Matrix(DataEvent event, String matrixName, List<SimpleMatrix> matrices) {

		//System.out.println("attempting to write matrices");
		if(event == null)
			System.out.println("event is null");
		if(matrixName == null)
			System.out.println("matrixName is null");
		if(matrices == null)
			System.out.println("matrix list is null");
		DataBank bank = event.createBank("Align::" + matrixName, matrices.size());
		for(int i = 0; i< matrices.size(); i++) {
			bank.setShort("rows",i,(short) matrices.get(i).numRows());
			bank.setShort("columns",i,(short) matrices.get(i).numCols());
			for(int j = 0; j<matrices.get(i).numRows(); j++) {
				for(int k = 0; k<matrices.get(i).numCols(); k++) {
                                    if(Double.isNaN(matrices.get(i).get(j,k))) 
                                        System.out.println(event.getBank("RUN::config").getInt("event", 0) + " " + matrixName + " " + matrices.get(i).get(j,k));
					bank.setFloat("element_"+j+"_"+k, i, (float) matrices.get(i).get(j,k));
				}
			}
		}
		event.appendBank(bank);
	}	
}