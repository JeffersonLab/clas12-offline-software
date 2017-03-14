package org.jlab.geom.base;

/**
 * This is an interface for providing constants for geometry objects.
 * @author gavalian
 */
public interface ConstantProvider {
    /**
     * Returns true if this ConstantProvider contains an entry for the given
     * key string.
     * @param name the key string
     * @return true if the ConstnatProvider contains the given key string
     */
    boolean hasConstant(String name);
    
    /**
     * Returns the number of rows associated with the given key string or 0 if
     * the ConstantProvider does not contain the key string.
     * @param name the key string
     * @return the number of rows for the key string or 0
     */
    int length(String name);
    
    /**
     * Returns the double value for the given row associated with the given key
     * string or 0 if the ConstnatProvider does not contain the key string, the
     * key string does not have value at the specified row index, or the value
     * at the specified row index is an integer.
     * @param name the key string
     * @param row the row index
     * @return the double value for the given row associated with the given key
     * string or 0
     */
    double getDouble(String name, int row);
    
    /**
     * Returns the integer value for the given row associated with the given key
     * string or 0 if the ConstnatProvider does not contain the key string, the
     * key string does not have value at the specified row index, or the value
     * at the specified row index is an double.
     * @param name the key string
     * @param row the row index
     * @return the integer value for the given row associated with the given key
     * string or 0
     */
    int getInteger(String name, int row);
}
