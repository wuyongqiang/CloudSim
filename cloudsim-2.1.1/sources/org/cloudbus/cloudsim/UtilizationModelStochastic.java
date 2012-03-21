/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.cloudbus.cloudsim.distributions.ZipfDistr;

/**
 * The UtilizationModelStochastic class implements a model, according to which
 * a Cloudlet generates random CPU utilization every time frame.
 *
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class UtilizationModelStochastic implements UtilizationModel {

	/** The history. */
	private Map<String, Double> history;
	private long seed;
	private Random rnd;

	/**
	 * Instantiates a new utilization model stochastic.
	 */
	public UtilizationModelStochastic() {
		setHistory(new HashMap<String, Double>());
		zipf = new ZipfDistr(0.5, 10); 
		UUID id = UUID.randomUUID();
		seed = id.hashCode();
		rnd = new Random(seed);
	}
	
	transient private ZipfDistr zipf = null;
	
		

	/* (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time) {
		String key = String.format("%d", (int)time);
		if (getHistory().containsKey(time)) {
			return getHistory().get(time);
		}
		
		if (time==0){
			return 1;
		}
		
		double utilization = rnd.nextGaussian()/4 + 0.5;
		//double utilization = zipf.sample();
		if (utilization< 0.001){
			utilization = 0.05;
		}
		
		if (utilization> 1){
			utilization = 1;
		}
		getHistory().put(String.format("%d", (int)time), utilization);
		return utilization;
	}

	/**
	 * Gets the history.
	 *
	 * @return the history
	 */
	protected Map<String, Double> getHistory() {
		return history;
	}

	/**
	 * Sets the history.
	 *
	 * @param history the history
	 */
	protected void setHistory(Map<String, Double> history) {
		this.history = history;
	}

	/**
	 * Save history.
	 *
	 * @param filename the filename
	 *
	 * @throws Exception the exception
	 */
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
	@SuppressWarnings("unchecked")
	public void loadHistory(String filename) throws Exception {
		throw new Exception("not implemented");
	}

}
