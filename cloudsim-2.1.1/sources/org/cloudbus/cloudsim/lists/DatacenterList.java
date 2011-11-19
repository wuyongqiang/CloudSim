/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.lists;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;

/**
 * VmList is a collection of operations on lists of VMs.
 *
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class DatacenterList {

	/**
	 * Return a reference to a Datacenter object from its ID.
	 *
	 * @param id ID of required Datacenter
	 * @param vmList the Datacenter list
	 *
	 * @return Datacenter with the given ID, $null if not found
	 *
	 * @pre $none
	 * @post $none
	 */
	public static <T extends Datacenter> T getById(List<T> dcList, int id) {
		for (T dc : dcList) {
			if (dc.getId() == id) {
				return dc;
			}
		}
		return null;
	}


}
