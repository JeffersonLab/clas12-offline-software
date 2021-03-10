package cnuphys.cnf.export;

public interface IExportFilter {

	public boolean pass(double[] dataArray);
}
