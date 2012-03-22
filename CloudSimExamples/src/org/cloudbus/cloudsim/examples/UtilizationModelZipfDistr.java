package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.distributions.ZipfDistr;

public class UtilizationModelZipfDistr implements UtilizationModel {

	transient private ZipfDistr zipf = null;
	
	public UtilizationModelZipfDistr() {
		zipf = new ZipfDistr(0.5, 100); 
	}
	
	/* (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time) {
		return zipf.sample() ;
	}
	
	@Override
	public double getAvgUtilization(double time) {
		return zipf.sample() ;
	}

}