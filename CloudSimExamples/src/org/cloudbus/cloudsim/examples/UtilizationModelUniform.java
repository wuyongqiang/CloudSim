package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.distributions.UniformDistr;

class UtilizationModelUniform implements UtilizationModel {

	transient private UniformDistr ud = null;
	
	public UtilizationModelUniform() {
		ud = new UniformDistr(0, 1); 
	}
	
	/* (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time) {
		return ud.sample() ;
	}

}