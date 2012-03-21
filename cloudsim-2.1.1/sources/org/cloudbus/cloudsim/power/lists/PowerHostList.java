package org.cloudbus.cloudsim.power.lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.HostList;


public class PowerHostList extends HostList {

	 
	 

	 	/**
	 	 * Sort by cpu utilization.
	 	 *
	 	 * @param vmList the vm list
	 	 */
	 	public static <T extends Host> void sortByCpu(List<T> hostList) {
	     	Collections.sort(hostList, new Comparator<T>() {
	            @Override
	 		public int compare(T a, T b) throws ClassCastException {
	                Double aCPU = (double) a.getTotalMips();
	                Double bCPU = (double) b.getTotalMips();
	                return bCPU.compareTo(aCPU);
	            }
	 		});
	 	}

}
