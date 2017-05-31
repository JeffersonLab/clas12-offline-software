package cnuphys.bankDictionary;

public class DictionaryEntry {

    // bank attributes
    protected int tag;
    protected String bankname;
    protected String bankInfo;

    // column attributes
    protected int num;
    protected String columnName;
    protected String type;
    protected String columnInfo;

    protected static final String UNK_STR = "???";

    /**
     * Create a dictionary entry appropriate for a bank
     * 
     * @param tag
     *            the bank tag
     * @param bankname
     *            the bank name
     * @param bankInfo
     *            the bank info
     * @param num
     *            the column num
     * @param columnName
     *            the column name
     * @param type
     *            the column data type
     * @param columnInfo
     *            the column info
     */
    protected DictionaryEntry(int tag, String bankname, String bankInfo) {
	this(tag, bankname, bankInfo, 0, UNK_STR, UNK_STR, UNK_STR);
    }

    /**
     * Create a dictionary entry appropriate for a column
     * 
     * @param tag
     *            the bank tag
     * @param bankname
     *            the bank name
     * @param bankInfo
     *            the bank info
     * @param num
     *            the column num
     * @param columnName
     *            the column name
     * @param type
     *            the column data type
     * @param columnInfo
     *            the column info
     */
    protected DictionaryEntry(int tag, String bankname, String bankInfo,
	    int num, String columnName, String type, String columnInfo) {
	super();
	this.tag = tag;
	this.bankname = bankname;
	this.bankInfo = bankInfo;
	this.num = num;
	this.columnName = columnName;
	this.type = type;
	this.columnInfo = columnInfo;
    }

    /**
     * Get the bank tag
     * 
     * @return the bak tag
     */
    public int getTag() {
	return tag;
    }

    /**
     * Get the bank name
     * 
     * @return the bank name
     */
    public String getBankName() {
	return bankname;
    }

    /**
     * Get the info string for the bank
     * 
     * @return the info string for the bank
     */
    public String getBankInfo() {
	return bankInfo;
    }

    /**
     * Get the number for the column
     * 
     * @return the column number
     */
    public int getNum() {
	return num;
    }

    /**
     * Get the column name
     * 
     * @return the column name
     */
    public String getColumnName() {
	return columnName;
    }

    /**
     * Get the column data type
     * 
     * @return the column data type
     */
    public String getType() {
	return type;
    }

    /**
     * Get the info string for the column
     * 
     * @return the info string fot the column
     */
    public String getColumnInfo() {
	return columnInfo;
    }

    // should be unique or we're in trouble
    protected String haskKey() {
	return hashKey(tag, num);
    }

    /**
     * Obtain a hash key for a tag and num
     * 
     * @param tag
     *            the bank tag
     * @param num
     *            the column number
     * @return
     */
    protected static String hashKey(int tag, int num) {
	return tag + "$" + num;
    }

    /**
     * Convenience for use in the event panel in bCNU and ced
     * 
     * @return a description, either of the column or the bank.
     */
    public String getDescription() {
	if ((num == 0) && UNK_STR.equals(getColumnInfo())) {
	    return getBankInfo();
	} else {
	    return getColumnInfo();
	}
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer(500);

	sb.append("Tag: " + getTag() + "\n");
	sb.append("Bank name: " + getBankName() + "\n");
	sb.append("Bank info: " + getBankInfo() + "\n");
	sb.append("Num: " + getNum() + "\n");
	sb.append("Column name: " + getColumnName() + "\n");
	sb.append("Data type: " + getType() + "\n");
	sb.append("Column info: " + getColumnInfo());

	return sb.toString();
    }
}
