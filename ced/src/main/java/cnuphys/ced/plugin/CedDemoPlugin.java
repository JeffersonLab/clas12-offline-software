package cnuphys.ced.plugin;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D.Double;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.plugin.PluginProperties;
import cnuphys.bCNU.plugin.shapes.PluginShape;
import cnuphys.ced.event.data.DC;

public class CedDemoPlugin extends CedPlugin {

	// cache my wires
	private PluginShape shapes[][];

	@Override
	public void initializePluginWorld(Double world) {
		world.x = 120;
		world.y = 0;

		// note I can make width or height negative!
		world.width = -128;
		world.height = 8;
	}

	@Override
	public void customizePlugin() {
		// THIS IS OPTIONAL!!!
		addProperty(PluginProperties.WIDTH, 900);
		addProperty(PluginProperties.HEIGHT, 300);
		addProperty(PluginProperties.BACKGROUND, "light blue");
		addProperty(PluginProperties.VVPANEL, 2);
		addProperty(PluginProperties.VVLOCATION, Plugin.BOTTOMLEFT);
	}

	@Override
	public String getPluginTitle() {
		return "Demo Plugin, sector 2, DC Superlayer 1";
	}

	@Override
	public void mouseOverShape(PluginShape shape) {
		updateStatus((shape == null) ? null : shape.getInfoString());
	}

	@Override
	public void shapeClick(PluginShape shape, int clickCount,
			Point pixelPoint) {
		updateStatus((shape == null) ? null
				: "click count " + clickCount + " on " + shape.getInfoString());
	}

	@Override
	public void shapePopupTrigger(PluginShape shape, Point pixelPoint) {
		updateStatus((shape == null) ? null
				: "popup trigger on " + shape.getInfoString());
	}

	@Override
	public void addInitialShapes() {
		// waste a little to make the code simpler
		shapes = new PluginShape[7][113];

		for (int layer = 1; layer <= 6; layer++) {
			for (int wire = 1; wire <= 112; wire++) {
				shapes[layer][wire] = addRectangle(
						"Layer: " + layer + " Wire: " + wire, wire, layer, 1, 1,
						PluginProperties.FILLCOLOR, Color.lightGray,
						PluginProperties.SECTOR, 2, PluginProperties.SUPERLAYER,
						1, PluginProperties.LAYER, layer,
						PluginProperties.COMPONENT, wire);
			}
		}
	}

	@Override
	public void processClasIoEvent(EvioDataEvent event,
			boolean isAccumulating) {

		// reset all the fill colors and info strings
		for (int layer = 1; layer <= 6; layer++) {
			for (int wire = 1; wire <= 112; wire++) {
				shapes[layer][wire].setFillColor(Color.lightGray);
				shapes[layer][wire]
						.setInfoString("Layer: " + layer + " Wire: " + wire);
			}
		}
		
		int hitCount = DC.hitCount();
		
		if (hitCount > 0) {
			int sector[] = DC.sector();
			int superlayer[] = DC.superlayer();
			int layer[] = DC.layer();
			int wire[] = DC.wire();
			int tdc[] = DC.tdc();
			
			for (int hit = 0; hit < hitCount; hit++) {
				int sect = sector[hit]; // 1 based
				int supl = superlayer[hit]; // 1 based
				if ((sect == 2) && (supl == 1)) {
					int lay = layer[hit]; // 1 based
					int w = wire[hit]; // 1 based
					shapes[lay][w].setFillColor(Color.red);
					
					if (tdc != null) {
						shapes[lay][w].setInfoString("Layer: " + lay + " Wire: "
								+ w + "  TDC: " + tdc[hit]);
					}
				}

			}

		}
		
		// loop over hits

		refresh();
	}

}
