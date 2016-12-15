package org.jlab.service.dc;

import java.util.List;
import java.util.StringTokenizer;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.coda.jevio.EvioCompactStructureHandler;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

public class HeaderEngine extends ReconstructionEngine{

	public HeaderEngine() {
		super("HEADER","ziegler","3.0");
	}
	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean processDataEvent(DataEvent event) {
		
		this.load((EvioDataEvent) event);
		EvioDataBank bank =  (EvioDataBank) event.getDictionary().createBank("RUN::config", 1);
		
		
		bank.setInt("Run",0,this.RunNb);
		bank.setInt("Event", 0, this.EventNb);
		bank.setByte("Type", 0, (byte) 0);
		int mode = 1;
		if(this.SolenoidOn || this.TorusOn)
			mode =0;
		bank.setByte("Mode", 0,(byte) mode);
		bank.setFloat("Torus", 0, (float) this.TorusScale);
		bank.setFloat("Solenoid", 0, (float) this.SolenoidScale);
		bank.setFloat("RF", 0, (float) 0.0);
		event.appendBank(bank);
		//bank.show();
		return true;
	}
	/**
	 * From Dave Heddle
	 * @param event

	 */
	static final int GEMCMetaBankTag = 5;
	static final int GEMCMetaBankNum = 1;

	// has this been use to set the fields?
	public boolean resetFields;

	// the raw strings
	//private Vector<String> properties = new Vector<String>();


	private String scaleString(String magnet, String key, String val) {

		String valStr = null;
		magnet = magnet.toUpperCase();

		String s = "SCALE_FIELD";
		if (key != null && key.equalsIgnoreCase(s)) { 
			if (val.toUpperCase().contains(magnet)) {
				int index = val.indexOf(',');
				valStr = val.substring(index + 1);
			}
			else {
				s = "SCALE_FIELD_REPETITION_1";
				if (key != null && key.equalsIgnoreCase(s)) {
					if (val.toUpperCase().contains(magnet)) {
						int index = val.indexOf(',');
						valStr = val.substring(index + 1);
					}
				}
			}
		}
		return valStr;
	}

	

	/**
	 * Find a node with a specific tag and num
	 * 
	 * @param event
	 *            the event
	 * @param tag
	 *            the target tag
	 * @param num
	 *            the target num
	 * @return the node, or null if not found
	 */
	private EvioNode getNode(EvioDataEvent event, int tag, int num) {
		List<EvioNode> nodeList = null;

		if (event != null) {
			EvioCompactStructureHandler handler = event.getStructureHandler();
			
			if (handler != null) {
				try {
					nodeList = handler.getNodes();
				}
				 catch (EvioException e) {
					e.printStackTrace();
				}

				if (nodeList != null) {
					for (EvioNode node : nodeList) {
						int ntag = node.getTag();
						if (ntag > tag) {
							return null;
						} else if (ntag == tag) {
							int nnum = node.getNum();
							if (nnum > num) {
								return null;
							} else if (nnum == num) {
								return node;
							}
						}
					}
				} else {
					return null;
				} 
			} else {
				return null;
			}
				
		}	

		return null;
	}
	private boolean SolenoidOn =false;
	private boolean TorusOn =false;
	private double SolenoidScale;
	private double TorusScale;
	private int RunNb;
	private int EventNb;
	
	public void load(EvioDataEvent event) {
		if (event == null) {
			return;
		}
		
		//this.EventNb = event.getEventBuffer().; System.out.println(this.EventNb);
		
		EvioNode node = this.getNode(event, GEMCMetaBankTag,
				GEMCMetaBankNum);

		if (node != null) {
			
//			System.err.println("Found GEMC metadata bank.");
//			resetFields = true;

			byte bytes[] = node.getStructureBuffer(true).array();

			if (bytes != null) {
				String ss = new String(bytes);

				if (ss != null) {
					String tokens[] = this.tokens(ss, "\0");

					if (tokens != null) {
						for (String s : tokens) {
							s = s.trim();
							if (s.startsWith("option ")) {
								s = s.substring(7);
								int index = s.indexOf(':');
								if (index > 0) {
									String key = s.substring(0, index).trim()
											.toUpperCase();
									String val = s.substring(index + 1).trim();
									 System.err.println("KEY: [" + key
									 + "] VAL: [" + val + "]");
									 if(key !=null && key.equalsIgnoreCase("RUNNO")) {
										 try {
												RunNb = Integer.parseInt(val.trim() );
											} catch (Exception e) {
												e.printStackTrace();
											}
									 }
									 EventNb++;
									 if(key != null && key.equalsIgnoreCase("ACTIVEFIELDS")) { 
											if ((val.toUpperCase()).trim().contains("TORUS") ) {
													System.out.println("TORUS ON");
													TorusOn = true;
													TorusScale =1.0;
											}
											if (val.toUpperCase().contains("SOLENOID") ) {
												System.out.println("SOLENOID ON");
												SolenoidOn = true;
												SolenoidScale =1.0;
										}
									 }
									//properties.addElement(key);
									//properties.addElement(val);
									 if(SolenoidOn && scaleString("solenoid",  key,  val) !=null) {
										 
										 String St = scaleString("solenoid",  key,  val) ;
										 if (St  != null) {
												try {
													SolenoidScale = Double.parseDouble(scaleString("solenoid",  key,  val) );
												} catch (Exception e) {
													e.printStackTrace();
												}
										 }
									 }
									 
									 if(TorusOn && scaleString("torus",  key,  val) !=null){
										 String St = scaleString("torus",  key,  val) ;
										 if (St  != null) {
											try {
												TorusScale = Double.parseDouble(scaleString("torus",  key,  val) );
											} catch (Exception e) {
												e.printStackTrace();
											}
										 }
									 }
									 
								}
							}
						} // for s
					} // tokens != null

				} // ss not null
			} // bytes not null
		} // node not null
		//System.out.println(" TORUS "+TorusScale+" SOLENOID "+SolenoidScale);
	}

	
	private String[] tokens(String str, String delimiter) {

		StringTokenizer t = new StringTokenizer(str, delimiter);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			lines[i] = t.nextToken();
		}

		return lines;
	}


}