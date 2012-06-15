package examples.maolin.dcenergy.only50;

import org.cloudbus.cloudsim.util.LogPrint;

public class PrintUtil {
	private static LogPrint log;

	public static void print(String message){
		if (log==null){
			log = new LogPrint(PrintUtil.class.getName()+".txt");
		}
		
		log.print(message,LogPrint.PrintMode.PrintLog);
	}
	
	public static void setLogName(String name){		
		if (log==null)	
			log = new LogPrint(PrintUtil.class.getName()+name+".txt");
		log.setLogFile(name+".txt");
	}
	
	public static void setLogFolder(String path){	
		if (log==null)
			 log = new LogPrint(PrintUtil.class.getName()+".txt");
		
		log.setLogFolder(path);
	}
}
