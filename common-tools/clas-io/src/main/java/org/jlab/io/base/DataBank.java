package org.jlab.io.base;

import javax.swing.table.TableModel;

public interface DataBank {
    /**
     * Get the names of the columns in the bank.
     * The name is used in the calls getInt() and getDouble()
     * and other calls for get the arrays.
     * @return array of strings with names.
     */
    String[] getColumnList();
    /**
     * Returns the descriptor of the bank, that contains
     * names of the variables and their types.
     * @return bank descriptor
     */
    DataDescriptor getDescriptor();
    
    /**
     * Returns a native array of doubles for given name.     
     * @param path - name of the column.
     * @return double[] array with values.
     */
    double[] getDouble(String path);
    double   getDouble(String path, int index);
    /**
     * Adds array of doubles into the bank under the name.    
     * @param path - name of the array.
     * @param arr primitive type array of doubles.
     */
    void setDouble(String path, double[] arr);
    void setDouble(String path, int row, double value);
    /**
     * Appends an array to existing array with the same name.
     * The resulting array will increase in size by arr.length.
     * @param path name of the variable
     * @param arr primitive type array of doubles.
     */
    void appendDouble(String path, double[] arr);
    
    float[] getFloat(String path);
    float   getFloat(String path, int index);
    void setFloat(String path, float[] arr);
    void setFloat(String path, int row, float value);
    void appendFloat(String path, float[] arr);
    
    int[] getInt(String path);
    int   getInt(String path, int index);
    void setInt(String path, int[] arr);
    void setInt(String path, int row, int value);
    void appendInt(String path, int[] arr);

    short[] getShort(String path);
    short   getShort(String path, int index);
    void setShort(String path, short[] arr);
    void setShort(String path, int row, short value);
    void appendShort(String path, short[] arr);
    
    byte[] getByte(String path);
    byte   getByte(String path, int index);
    void setByte(String path, byte[] arr);
    void setByte(String path, int row, byte value);
    void appendByte(String path, byte[] arr);
    
    long[] getLong(String path);
    long   getLong(String path, int index);
    void setLong(String path, long[] arr);
    void setLong(String path, int row, long value);
    void appendLong(String path, long[] arr);
    /**
     * Returns the number of columns in the bank. columns are number of
     * variables.
     * @return number of columns
     */
    int  columns();
    /**
     * returns the number of rows in the bank, assuming all columns have the
     * same length.
     * @return number of rows
     */
    int  rows();
    /**
     * Prints the content of the bank on the screen.
     * The implementation of printout is left up to the
     * particular implementation of the class.
     */
    void show();
    /**
     * Clears the content of the bank. all columns are removed.
     */
    void reset();
    /**
     * Allocates all variables for the the bank. the names are taken from
     * the descriptor object.
     * @param rows number of rows for each column to allocate 
     */
    void allocate(int rows);
    /**
     * Returns a table model for displaying the bank information
     * @param mask
     * @return 
     */
    TableModel getTableModel(String mask);
}
