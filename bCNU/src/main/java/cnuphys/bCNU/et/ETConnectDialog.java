package cnuphys.bCNU.et;

import java.awt.Component;

import javax.swing.BorderFactory;

import org.jlab.coda.et.EtConstants;

import cnuphys.bCNU.component.LabeledTextField;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.SimpleDialog;
import cnuphys.bCNU.graphics.component.IpPortPanel;
import cnuphys.bCNU.util.Environment;

public class ETConnectDialog extends SimpleDialog {

    // button labels
    private static final String CONNECT = "Connect";
    private static final String CANCEL = "Cancel";

    // for the ET name
    LabeledTextField _etName;

    // reason dialog closed
    private int _answer;

    // the panel with ip address and port
    private IpPortPanel _ippanel;

    public ETConnectDialog() {
	super("Connect to an ET System", true, CONNECT, CANCEL);
	pack();
	DialogUtilities.centerDialog(this);
    }

    @Override
    protected Component createNorthComponent() {
	_etName = new LabeledTextField("ET Name (a /tmp file) ",
		ETSupport._defaultName.length() + 8);
	_etName.setText(ETSupport._defaultName);
	_etName.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
	return _etName;
    }

    /**
     * Get the name of the et system we are connecting to
     * 
     * @return the name of the et system we are connecting to
     */
    public String getETName() {
	return _etName.getText();
    }

    /**
     * Override to create the component that goes in the center. Usually this is
     * the "main" component.
     * 
     * @return the component that is placed in the center
     */
    @Override
    protected Component createCenterComponent() {
	_ippanel = new IpPortPanel(Environment.getInstance().getHostAddress(),
		EtConstants.serverPort);
	_ippanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
	return _ippanel;
    }

    /**
     * Get the entered IP address
     * 
     * @return the IP address
     */
    public String getIpAddress() {
	return _ippanel.getIpAddress();
    }

    /**
     * Get the port number
     * 
     * @return the port number
     */
    public int getPort() {
	return _ippanel.getPort();
    }

    /**
     * A button was hit. The default behavior is to shutdown the dialog.
     * 
     * @param command
     *            the label on the button that was hit.
     */
    @Override
    protected void handleCommand(String command) {
	if (CONNECT.equals(command)) {
	    _answer = DialogUtilities.YES_RESPONSE;
	} else if (CANCEL.equals(command)) {
	    _answer = DialogUtilities.CANCEL_RESPONSE;
	}

	setVisible(false);
    }

    /**
     * Get what closed the dialog, either DialogUtilities.YES_RESPONSE or
     * DialogUtilities.CANCEL_RESPONSE.
     * 
     * @return what closed the dialog.
     */
    public int getAnswer() {
	return _answer;
    }

}