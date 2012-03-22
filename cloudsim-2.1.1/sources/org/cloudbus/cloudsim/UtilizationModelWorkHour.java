package org.cloudbus.cloudsim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class UtilizationModelWorkHour extends UtilizationModelStochastic {

	//private Map<String, Double> history;
	
	public UtilizationModelWorkHour(int roughIndex){
		super(roughIndex);		
		//history = new HashMap<String, Double>();
	}
	
	@Override
	public double getUtilization(double time) {
		
		if (time==0){
			return 1;
		}

		double utilization = geWorkHourWorkLoad(time);
		utilization = addRandomVariation(utilization);
		history.put(String.format("%.0f", time), utilization);
				
		return utilization;
	}
	
	private double geWorkHourWorkLoad(double time) {
		int passSecond = (int)time;
		passSecond = passSecond % 3600;
		double utilization = 0;
		//3600 seconds a cycle
		double peakUtilization = 0.60;
		double nightUtilization = 0.20;
		
		double rampUpTime = 10 * 60;
		double peakBeginTime = rampUpTime;
		double peakEndtime = peakBeginTime + 30 * 60;
		double rampDownBeginTime = peakEndtime;
		double rampDownEndTime = rampDownBeginTime + 10 * 60;
		double nightBeginTime = rampDownEndTime;
		
		if(passSecond<rampUpTime){
			utilization = (double)passSecond/rampUpTime * peakUtilization;
		}else if (passSecond>=peakBeginTime && passSecond<peakEndtime){
			utilization = peakUtilization;
		}else if (passSecond>=rampDownBeginTime && passSecond<rampDownEndTime){
			utilization = (peakUtilization-nightUtilization) * (rampDownEndTime - (double)passSecond)/(rampDownEndTime - rampDownBeginTime) + nightUtilization;
		}
		else{
			utilization = nightUtilization;
		}
		return utilization;
	}

	private double addRandomVariation(double baseUtilization){		
		UUID id = UUID.randomUUID();
		id.hashCode();
		Random r = new Random(id.hashCode());
		double rn = ((double)Math.abs(r.nextInt())%100)/100;
		double randomUtilization = ( rn -0.5)/1.5*(roughIndex/5.0);		
		double utilization = baseUtilization + randomUtilization;
		if (utilization<0){
			utilization = 0.05;
		}
		else if(utilization>1){
			utilization = 1;
		}
		
		return utilization;
	}
	
	@Override
	public void saveHistory(String filename) throws Exception {
			String LINE_SEPARATOR = System.getProperty("line.separator");
		    Writer out = new OutputStreamWriter(new FileOutputStream(filename), "utf8");
		    try {
		    	for (int i=0;i<3700;i++){
		    		String key = String.format("%d", i);
		    		if (history.containsKey(key)){
		    			String message = String.format("%d,%d,%.2f",Log.getLogSimId(), i,history.get(key)) + LINE_SEPARATOR;
			    		out.write(message);
		    		}		    		
		    	}
		    }
		    finally {
		      out.close();
		    }		
	}

	/**
	 * Load history.
	 *
	 * @param filename the filename
	 *
	 * @throws Exception the exception
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void loadHistory(String filename) throws Exception {
		
	}
}
