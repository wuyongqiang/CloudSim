/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.network.NetworkConfig;

/**
 * PowerDatacenter is a class that enables simulation of power-aware data centers.
 *
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class PowerDatacenter extends Datacenter {

	/** The power. */
	private double power;

	/** The disable migrations. */
	private boolean disableMigrations;

	/** The cloudlet submited. */
	private double cloudletSubmitted;

	/** The migration count. */
	private int migrationCount;

	private int turnOnTimes;
	
	private int turnOffTimes;
	
	private List<String> powerOnOffRecord;

	private double networkCost;
	
	private NetworkConfig networkConfig;
	/**
	 * Instantiates a new datacenter.
	 *
	 * @param name the name
	 * @param characteristics the res config
	 * @param schedulingInterval the scheduling interval
	 * @param utilizationBound the utilization bound
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 *
	 * @throws Exception the exception
	 */
	public PowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
		powerOnOffRecord = new ArrayList<String>();
	}

	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events
	 * and updating cloudlets inside them must be called from the outside.
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void updateCloudletProcessing() {
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();
		double timeframePower = 0.0;
		double timeframeNetworkCost = 0.0;			
		
		int[] vmAssign = getVmAssign();
		double[] vmWorkloads = getVmWorkloads();
		timeframeNetworkCost = networkConfig.getTotalWeight(vmAssign,vmWorkloads);

		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			double timeDiff = currentTime - getLastProcessTime();
			double minTime = Double.MAX_VALUE;

			Log.printLine("\n");
			
			timeframeNetworkCost = timeframeNetworkCost * timeDiff;
			setNetworkCost(getNetworkCost() + timeframeNetworkCost);
			for (PowerHost host : this.<PowerHost>getHostList()) {

				Log.formatLine("%.2f: Host #%d", CloudSim.clock(), host.getId());

				double hostPower = 0.0;

				if (host.getUtilizationOfCpu() > 0) {
					try {
						hostPower = host.getPower() * timeDiff;
						timeframePower += hostPower;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if (host.getUtilizationOfCpu() > 0 && host.getLastUtilization() <= 0){
					setTurnOnTimes(getTurnOnTimes()+1);		
					String s = String.format("%d,%d,%s",(int)CloudSim.clock(), host.getId(),"on");
					getPowerOnOffRecords().add(s);
					Log.printLineToHostOnOffFile(s);
				}
				
				if (host.getUtilizationOfCpu() <= 0 && host.getLastUtilization() > 0){
					setTurnOffTimes(getTurnOffTimes()+1);
					String s = String.format("%d,%d,%s",(int)CloudSim.clock(), host.getId(),"off");
					getPowerOnOffRecords().add(s);
					Log.printLineToHostOnOffFile(s);
				}
				
				host.setLastUtilization(host.getUtilizationOfCpu());
				
				Log.formatLine("%.2f: Host #%d utilization is %.2f%%", CloudSim.clock(), host.getId(), host.getUtilizationOfCpu() * 100);
				Log.formatLine("%.2f: Host #%d energy is %.2f W*sec", CloudSim.clock(), host.getId(), hostPower);
				
				Log.printLineToDetailFile(String.format("%d,%d,%d,%.2f,%.2f,%.2f", Log.getLogSimId(), (int)CloudSim.clock(), host.getId(), host.getUtilizationOfCpu() * 100,hostPower, host.getUtilizationOfMem() * 100));
			}

			Log.formatLine("\n%.2f: Consumed energy is %.2f W*sec\n", CloudSim.clock(), timeframePower);

			Log.printLine("\n\n--------------------------------------------------------------\n\n");

			for (PowerHost host : this.<PowerHost>getHostList()) {
				Log.formatLine("\n%.2f: Host #%d", CloudSim.clock(), host.getId());

				double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
				if (time < minTime) {
					minTime = time;
				}

				Log.formatLine("%.2f: Host #%d utilization is %.2f%%", CloudSim.clock(), host.getId(), host.getUtilizationOfCpu() * 100);
			}

			setPower(getPower() + timeframePower);			
			/** Remove completed VMs **/
			for (PowerHost host : this.<PowerHost>getHostList()) {
				for (Vm vm : host.getCompletedVms()) {
					getVmAllocationPolicy().deallocateHostForVm(vm);
					getVmList().remove(vm);
					Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
				}
			}

			Log.printLine();

			if (!isDisableMigrations()) {
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(getVmList());

				for (Map<String, Object> migrate : migrationMap) {
					Vm vm = (Vm) migrate.get("vm");
					PowerHost targetHost = (PowerHost) migrate.get("host");
					PowerHost oldHost = (PowerHost) vm.getHost();

					targetHost.addMigratingInVm(vm);

					if (oldHost == null) {
						Log.formatLine("%.2f: Migration of VM #%d to Host #%d is started", CloudSim.clock(), vm.getId(), targetHost.getId());
					} else {
						Log.formatLine("%.2f: Migration of VM #%d from Host #%d to Host #%d is started", CloudSim.clock(), vm.getId(), oldHost.getId(), targetHost.getId());
					}

					incrementMigrationCount();

					vm.setInMigration(true);

					/** VM migration delay = RAM / bandwidth + C    (C = 10 sec) **/
					// vm.getRam() / ((double) vm.getBw() / 8) + 0
					send(getId(),vm.getEstimatedMigrationDuration(), CloudSimTags.VM_MIGRATE, migrate);
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}

			setLastProcessTime(currentTime);
		}
	}
	
	private double[] _vmWorkloads = null;
	private double[] getVmWorkloads() {
		if (_vmWorkloads==null)
			_vmWorkloads = new double[getMaxVmId()+10];
		
		for(int i=0;i<_vmWorkloads.length;i++){
			_vmWorkloads[i] = 0;
		}
					
		for (Vm vm : getVmList()) {			
			_vmWorkloads[vm.getId()] = vm.getAvgCurrentRequestedTotalMips()/vm.getMips();			
		}
		
		return _vmWorkloads;
	}

	int[] _vmAssign = null; 
	public int[] getVmAssign() {
		if (_vmAssign==null)
			_vmAssign = new int[getMaxVmId()+10];
		
		for(int i=0;i<_vmAssign.length;i++){
			_vmAssign[i] = -1;
		}
				
		for (Vm vm : getVmList()) {
			if (vm.getHost()!=null)
				_vmAssign[vm.getId()] = vm.getHost().getId();			
		}
		return _vmAssign;
	}
	
	private int getMaxVmId(){
		int idMax = 0;
		for (Vm vm : getVmList()){
			if (idMax<vm.getId()){
				idMax = vm.getId();
			}
		}
		return idMax;
	}

	/* (non-Javadoc)
	 * @see cloudsim.Datacenter#processCloudletSubmit(cloudsim.core.SimEvent, boolean)
	 */
	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}

	/**
	 * Gets the power.
	 *
	 * @return the power
	 */
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power.
	 *
	 * @param power the new power
	 */
	protected void setPower(double power) {
		this.power = power;
	}
	
	/**
	 * Gets the power.
	 *
	 * @return the power
	 */
	public double getNetworkCost() {
		return networkCost;
	}

	/**
	 * Sets the power.
	 *
	 * @param power the new power
	 */
	protected void setNetworkCost(double power) {
		this.networkCost = power;
	}

	/**
	 * Checks if PowerDatacenter is in migration.
	 *
	 * @return true, if PowerDatacenter is in migration
	 */
	protected boolean isInMigration() {
		boolean result = false;
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Gets the under allocated mips.
	 *
	 * @return the under allocated mips
	 */
	public Map<String, List<List<Double>>> getUnderAllocatedMips() {
		Map<String, List<List<Double>>> underAllocatedMips = new HashMap<String, List<List<Double>>>();
		for (PowerHost host : this.<PowerHost>getHostList()) {
			for (Entry<String, List<List<Double>>> entry : host.getUnderAllocatedMips().entrySet()) {
				if (!underAllocatedMips.containsKey(entry.getKey())) {
					underAllocatedMips.put(entry.getKey(), new ArrayList<List<Double>>());
				}
				underAllocatedMips.get(entry.getKey()).addAll(entry.getValue());

			}
		}
		return underAllocatedMips;
	}
	
	public Map<String, List<List<Double>>> getUnderAllocatedMem() {
		Map<String, List<List<Double>>> underAllocatedMem = new HashMap<String, List<List<Double>>>();
		for (PowerHost host : this.<PowerHost>getHostList()) {
			for (Entry<String, List<List<Double>>> entry : host.getUnderAllocatedMem().entrySet()) {
				if (!underAllocatedMem.containsKey(entry.getKey())) {
					underAllocatedMem.put(entry.getKey(), new ArrayList<List<Double>>());
				}
				underAllocatedMem.get(entry.getKey()).addAll(entry.getValue());

			}
		}
		return underAllocatedMem;
	}

	/**
	 * Checks if is disable migrations.
	 *
	 * @return true, if is disable migrations
	 */
	public boolean isDisableMigrations() {
		return disableMigrations;
	}

	/**
	 * Sets the disable migrations.
	 *
	 * @param disableMigrations the new disable migrations
	 */
	public void setDisableMigrations(boolean disableMigrations) {
		this.disableMigrations = disableMigrations;
	}

	/**
	 * Checks if is cloudlet submited.
	 *
	 * @return true, if is cloudlet submited
	 */
	protected double getCloudletSubmitted() {
		return cloudletSubmitted;
	}

	/**
	 * Sets the cloudlet submited.
	 *
	 * @param cloudletSubmitted the new cloudlet submited
	 */
	protected void setCloudletSubmitted(double cloudletSubmitted) {
		this.cloudletSubmitted = cloudletSubmitted;
	}

	/**
	 * Gets the migration count.
	 *
	 * @return the migration count
	 */
	public int getMigrationCount() {
		return migrationCount;
	}

	/**
	 * Sets the migration count.
	 *
	 * @param migrationCount the new migration count
	 */
	protected void setMigrationCount(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	/**
	 * Increment migration count.
	 */
	protected void incrementMigrationCount() {
		setMigrationCount(getMigrationCount() + 1);
	}

	public void setTurnOnTimes(int turnOnOffTimes) {
		this.turnOnTimes = turnOnOffTimes;
	}

	public int getTurnOnTimes() {
		return turnOnTimes;
	}

	public void setTurnOffTimes(int turnOffTimes) {
		this.turnOffTimes = turnOffTimes;
	}

	public int getTurnOffTimes() {
		return turnOffTimes;
	}

	public List<String> getPowerOnOffRecords(){
		return this.powerOnOffRecord;
	}

	public NetworkConfig getNetworkConfig() {
		return networkConfig;
	}

	public void setNetworkConfig(NetworkConfig networkConfig) {
		this.networkConfig = networkConfig;
	}

}
