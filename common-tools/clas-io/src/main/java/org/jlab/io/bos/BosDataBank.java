/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.bos;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableModel;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;

/**
 *
 * @author gavalian
 */
public class BosDataBank implements DataBank {

	private BosDataDescriptor bankDescriptor = null;
	private HashMap<String, short[]> shortContainer = new HashMap<String, short[]>();
	private HashMap<String, int[]>   intContainer = new HashMap<String, int[]>();
	private HashMap<String, float[]> floatContainer = new HashMap<String, float[]>();
	private HashMap<String, long[]>  longContainer = new HashMap<String, long[]>();

	public BosDataBank(BosDataDescriptor d) {
		bankDescriptor = d;
	}

	public String[] getColumnList() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public DataDescriptor getDescriptor() {
		return this.bankDescriptor;
	}

	public double[] getDouble(String path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setDouble(String path, double[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void appendDouble(String path, double[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public float[] getFloat(String path) {
		return floatContainer.get(path);
	}

	public void setFloat(String path, float[] arr) {
		floatContainer.put(path, arr);
	}

	public void appendFloat(String path, float[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public int[] getInt(String path) {
		return intContainer.get(path);
	}

	public void setInt(String path, int[] arr) {
		intContainer.put(path, arr);
	}

	public void appendInt(String path, int[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public short[] getShort(String path) {
		return shortContainer.get(path);
	}

	public void setShort(String path, short[] arr) {
		shortContainer.put(path, arr);
	}

	public void appendShort(String path, short[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

        public long[] getLong(String path) {
            return longContainer.get(path);
        }

        public void setLong(String path, long[] arr) {
		longContainer.put(path, arr);
        }

        public void appendLong(String path, long[] arr) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
	public void show() {
		int size = shortContainer.size() + intContainer.size() + floatContainer.size();
		System.out.println("*****>>>>> BANK " + this.bankDescriptor.getName() + "  >>>> SIZE = " + size);

		for (Map.Entry<String, short[]> item : shortContainer.entrySet()) {
			System.out.print(String.format("%14s (short) : ", item.getKey()));
			short[] itemdata = item.getValue();
			for (int loop = 0; loop < itemdata.length; loop++)
				System.out.print(String.format(" %12.3f  ", itemdata[loop]));
			System.out.println();
		}

		for (Map.Entry<String, int[]> item : intContainer.entrySet()) {
			System.out.print(String.format("%14s ( int ) : ", item.getKey()));
			int[] itemdata = item.getValue();
			for (int loop = 0; loop < itemdata.length; loop++)
				System.out.print(String.format(" %12d  ", itemdata[loop]));
			System.out.println();
		}
		for (Map.Entry<String, float[]> item : floatContainer.entrySet()) {
			System.out.print(String.format("%14s (float) : ", item.getKey()));
			float[] itemdata = item.getValue();
			for (int loop = 0; loop < itemdata.length; loop++)
				System.out.print(String.format(" %12.5f  ", itemdata[loop]));
			System.out.println();
		}

	}

	public int columns() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public int rows() {
		int nrows = 0;

		if( !floatContainer.isEmpty() ){
			return floatContainer.entrySet().iterator().next().getValue().length;
		}
		if( !intContainer.isEmpty() ){
			return intContainer.entrySet().iterator().next().getValue().length;
		}
		if( !shortContainer.isEmpty() ){
			return shortContainer.entrySet().iterator().next().getValue().length;
		}
		if( !longContainer.isEmpty() ){
			return longContainer.entrySet().iterator().next().getValue().length;
		}

/*
		for (Map.Entry<String, float[]> item : floatContainer.entrySet()) {
			float[] itemdata = item.getValue();
			nrows = itemdata.length;
		}
*/
		/*
		 * for(Map.Entry<String,short[]> item : shortContainer.entrySet()){ short[] itemdata = item.getValue(); nrows = itemdata.length; }
		 */

		return nrows;
	}

	public byte[] getByte(String path) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setByte(String path, byte[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void appendByte(String path, byte[] arr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setDouble(String path, int row, double value) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setFloat(String path, int row, float value) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setInt(String path, int row, int value) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setShort(String path, int row, short value) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void setByte(String path, int row, byte value) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

        public void setLong(String path, int row, long value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
	public void reset() {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public void allocate(int rows) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public double getDouble(String path, int index) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public float getFloat(String path, int index) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public int getInt(String path, int index) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public short getShort(String path, int index) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

	public byte getByte(String path, int index) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

        public long getLong(String path, int index) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

	public TableModel getTableModel(String mask) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
	}

}
