package cnuphys.bCNU.shell;

import java.util.EventListener;

public interface IProcessListener extends EventListener {

	public void processStart(ProcessRecord processRecord);

	public void processEnd(ProcessRecord processRecord);

	public void processFail(ProcessRecord processRecord);

}
