package cnuphys.ced.clasio;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;

public class ClasIoTest {

	public static void main(String arg[]) {
		String testFile = Environment.getInstance().getHomeDirectory()
				+ "/evioData/gmnElectrons.evio";
		System.err.println("testFile: " + testFile);

		ClasIoEventManager eventManager = ClasIoEventManager.getInstance();

		try {
			eventManager.openEvioFile(testFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		boolean done = false;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (!done) {
			System.out.print(">> ");
			String s;
			try {
				s = br.readLine();
				if (s == null) {
					s = "";
				}

				if (s.equalsIgnoreCase("q")) { // q
					done = true;
				}

				else if (s.equalsIgnoreCase("e")) { // evio nodes
					EvioNode nodes[] = EvioNodeSupport.getNodes(eventManager
							.getCurrentEvent());
					if (nodes != null) {
						for (EvioNode node : nodes) {
							EvioNodeSupport.writeNode(System.out, node);
						}
					}
				}

				else if (s.equalsIgnoreCase("m")) { // map
					String allBanks[] = eventManager.getKnownBanks();
					if (allBanks != null) {
						for (String bs : allBanks) {
							System.out.println(bs + " "
									+ eventManager.isBankInCurrentEvent(bs));
						}
					}
				}

				else if (s.equalsIgnoreCase("r")) { // root node
					EvioNode root = EvioNodeSupport.getRootNode(eventManager
							.getCurrentEvent());
					if (root != null) {
						System.out.println("Root node:");
						EvioNodeSupport.writeNode(System.out, root);
					}
				}

				else if (s.equalsIgnoreCase("n")) { // next event
					EvioDataEvent event = eventManager.getNextEvent();
					System.err.println("Event number: "
							+ eventManager.getEventNumber() + "/"
							+ eventManager.getEventCount());

					event.getDictionary().getDescriptorList();
					event.show();
				} else if (s.equalsIgnoreCase("p")) { // next event
					EvioDataEvent event = eventManager.getPreviousEvent();
					System.err.println("Event number: "
							+ eventManager.getEventNumber() + "/"
							+ eventManager.getEventCount());
					event.show();
				} else if (s.equalsIgnoreCase("l")) { // list nodes
				} else if (s.startsWith("goto ")) {
					String tokens[] = FileUtilities.tokens(s);
					int evnum = Integer.parseInt(tokens[1]);
					EvioDataEvent event = eventManager.gotoEvent(evnum);
					System.err.println("Event number: "
							+ eventManager.getEventNumber() + "/"
							+ eventManager.getEventCount());
					event.show();
				} else {
					System.out.println("Command not recognized: " + s);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		eventManager.getDataSource().close();
	}
}
