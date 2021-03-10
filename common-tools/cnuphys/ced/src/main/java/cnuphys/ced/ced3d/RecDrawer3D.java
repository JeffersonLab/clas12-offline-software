package cnuphys.ced.ced3d;



import java.awt.Color;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

import com.jogamp.opengl.GLAutoDrawable;

import bCNU3D.Support3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.event.data.RECCalorimeter;
import cnuphys.ced.frame.CedColors;
import cnuphys.lund.LundId;
import item3D.Item3D;

public class RecDrawer3D extends Item3D {
	
	// the event manager
	ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();
	
	//the current event
	private DataEvent _currentEvent;

	private static final float POINTSIZE = 5f;
	private CedPanel3D _cedPanel3D;
	

	
	public RecDrawer3D(CedPanel3D panel3D) {
		super(panel3D);
		_cedPanel3D = panel3D;
	}

	@Override
	public void draw(GLAutoDrawable drawable) {
		
		_currentEvent = _eventManager.getCurrentEvent();
		if (_currentEvent == null) {
			return;
		}
		
		if (_panel3D instanceof ForwardPanel3D) { // forward detectors
			
			//show any data from REC::Calorimiter?
			if (((ForwardPanel3D) _panel3D).showRecCal()) {
				showEconCalorimeter(drawable);
			}
		}
	}
	
	
	//show data from REC::Calorimeter
	private void showEconCalorimeter(GLAutoDrawable drawable) {
		
		RECCalorimeter recCal = RECCalorimeter.getInstance();
		if (recCal.isEmpty()) {
			return;
		}

		for (int i = 0; i < recCal.count; i++) {

			float radius = recCal.getRadius(recCal.energy[i]);
			LundId lid = recCal.getLundId(i);

			if ((recCal.layer[i] <= 3) && _cedPanel3D.showPCAL()) {
				Support3D.drawPoint(drawable, recCal.x[i], recCal.y[i], recCal.z[i], Color.black, POINTSIZE, true);

				if (radius > 0) {
					Color color = (lid == null) ? CedColors.RECEcalFill : lid.getStyle().getTransparentFillColor();
					Support3D.solidSphere(drawable, recCal.x[i], recCal.y[i], recCal.z[i], radius, 40, 40, color);
				}
			} else if ((recCal.layer[i] > 3) && _cedPanel3D.showECAL()) {
				Support3D.drawPoint(drawable, recCal.x[i], recCal.y[i], recCal.z[i], Color.black, POINTSIZE, true);
				if (radius > 0) {
					Color color = (lid == null) ? CedColors.RECEcalFill : lid.getStyle().getTransparentFillColor();
					Support3D.solidSphere(drawable, recCal.x[i], recCal.y[i], recCal.z[i], radius, 40, 40, color);
				}
			}
		} // end for

	}

}
