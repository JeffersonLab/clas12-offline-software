package org.jlab.io.utils;

import java.io.Console;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.logging.DefaultLogger;

/**
 *
 * @author gavalian
 */
public class DataSourceDump {
    
    public static String waitForEnter() {
        String line = "";
        Console c = System.console();
        if (c != null) {
            // printf-like arguments
            //c.format(message, args);
            c.format("\nChoose (n=next,p=previous, q=quit), Type Bank Name or id : ");
            line = c.readLine();
        }
        return line;
    }
    public static void main(String[] args){
                
        DefaultLogger.debug();

        if(args.length==0){
            System.out.println("\n\n\t Usage : eviodump [filename]");
            System.exit(0);
        }
        
        String inputFile = args[0];
        
        DataSource  reader = null;
        
        if(inputFile.contains("hipo")==true){
            reader = new HipoDataSource();
        } else {
            reader = new EvioSource();
        }
        
        reader.open(inputFile);

        String command = "";
        DataEvent event = null;
        int icounter = 0;
        
        event = reader.getNextEvent();
        System.out.println(" HAS EVENTS = " + reader.hasEvent());
        while(reader.hasEvent()==true){
            
            
            if(command.length()<1){
                System.out.println("\n");
                System.out.println("*********************** EVENT # " + icounter 
                        + "  ***********************");
                event.show();
            }
            
            command = DataSourceDump.waitForEnter();
            
            if(command.compareTo("n")==0){
                event = reader.getNextEvent();
                icounter++;
                command = "";
                continue;
            }
            
            if(command.compareTo("p")==0){
                if(icounter>0){
                    event = reader.getPreviousEvent();
                    icounter--;
                }
                command = "";
                continue;
            }
            
            if(command.length()>=1){
                int order = -1;
                if(command.matches("-?\\d+(\\.\\d+)?")){
                    try {
                        order = Integer.parseInt(command);
                        if(event instanceof HipoDataEvent){
                            HipoDataEvent he = (HipoDataEvent) event;
                            he.showBankByOrder(order); 
                            continue;
                        }
                    } catch(Exception e) {
                        System.out.println(" [warning] --> unrecognized input [" + command + "]");
                    }                
                } else {
                    if(event.hasBank(command)==true){
                        DataBank bank = event.getBank(command);
                        bank.show();
                    } else {
                        System.out.println("[DataDump] warning ---> bank not found : " + command);
                    }
                }
            }
            if(command.compareTo("q")==0){
                reader.close();
                System.exit(0);
            }
        }         
    }   

}
