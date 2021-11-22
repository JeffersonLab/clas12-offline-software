package org.jlab.detector.calib.utils;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rcdb.RCDB;
import org.rcdb.Condition;
import org.rcdb.ConditionType;

/**
 *
 * @author baltzell
 */
public class RCDBProvider {
    public static Logger LOGGER = Logger.getLogger(RCDBProvider.class.getName());

    public static final String DEFAULTADDRESS = "mysql://rcdb@clasdb.jlab.org/rcdb";

    private org.rcdb.JDBCProvider provider;

    private int debugMode = 2;

    public RCDBProvider(){
        String address = DEFAULTADDRESS;
        String envAddress = this.getEnvironment();
        if (envAddress!=null) address = envAddress;
        this.initialize(address);
    }

    public RCDBProvider(String address){
        this.initialize(address);
    }

    private String getEnvironment(){
        String envRCDB   = System.getenv("RCDB_DATABASE");
        String envCLAS12 = System.getenv("CLAS12DIR");
        String connection = System.getenv("RCDB_CONNECTION");
        if(connection!=null) return connection;
        String propCLAS12 = System.getProperty("CLAS12DIR");
        String propRCDB   = System.getProperty("RCDB_DATABASE");
        String clas12;
        String rcdb;
        if (envRCDB!=null && envCLAS12!=null){
            clas12 = envCLAS12;
            rcdb = envRCDB;
        }
        else if (propRCDB!=null && propCLAS12!=null){
            clas12 = propCLAS12;
            rcdb = propRCDB;
        }
        else return null;
        StringBuilder str = new StringBuilder();
        str.append("sqlite:///");
        if(clas12.charAt(0)!='/') str.append("/");
        str.append(clas12);
        if(rcdb.charAt(0)!='/' && clas12.charAt(clas12.length()-1)!='/'){
            str.append("/");
        }
        str.append(rcdb);
        return str.toString();
    }

    private void initialize(String address){
        provider = RCDB.createProvider(address);
        try {
            LOGGER.log(Level.INFO,"[RCDB] --->  open connection with : " + address);
            provider.connect();
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE,"",e);
        }

        if(provider.isConnected()==true){
            LOGGER.log(Level.INFO,"[RCDB] --->  database connection  : success");
        } else {
            LOGGER.log(Level.SEVERE,"[RCDB] --->  database connection  : failed");
        }

    }

    public void disconnect(){
        if (provider.isConnected()) {
            LOGGER.log(Level.INFO,"[RCDB] --->  database disconnect  : success");
            provider.close();
        }
    }

    public RCDBConstants getConstants(int run) {
        RCDBConstants data = new RCDBConstants();
        if (provider.isConnected()) {
            HashMap<String, ConditionType> cndTypes = provider.getConditionTypeByNames();
            for (String name : cndTypes.keySet()) {
                Condition cnd=provider.getCondition(run,name);
                if (cnd==null) continue;
                switch (cndTypes.get(name).getValueType().toString()) {
                    case "String":
                        data.add(name,cnd.toString());
                        break;
                    case "Long":
                        data.add(name,cnd.toLong());
                        break;
                    case "Double":
                        data.add(name,cnd.toDouble());
                        break;
                    case "Time":
                        data.add(name,cnd.toTime());
                        //System.out.print(cnd.toTime().getHours()+" ");
                        //System.out.print(cnd.toTime().getMinutes()+" ");
                        //System.out.print(cnd.toTime().getSeconds()+" ");
                        //System.out.print(cnd.toTime().getTime()+" ");
                        //System.out.print(cnd.toTime().toLocaleString()+" ");
                        //System.out.println(cnd.toTime().toString());
                        break;
                    default:
                        break;
                }
            }
        }
        return data;
    }

    public static void main(String[] args){
        RCDBProvider a=new RCDBProvider();
        RCDBConstants data=a.getConstants(4014);
        data.show();
        a.disconnect();
    }

}
