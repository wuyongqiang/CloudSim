/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * The Class HostDynamicWorkload.
 *
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class HostDynamicWorkload extends Host {

	/** The utilization mips. */
	private double utilizationMips;

	/** The under allocated mips. */
	private Map<String, List<List<Double>>> underAllocatedMips;
	
	private Map<String, List<List<Double>>> underAllocatedMem;
	
	private Queue<Double> historyUtilizationQueue;	
	
	private int queueLength = 16;

	private double utilizationMem;

	/**
	 * Instantiates a new host.
	 *
	 * @param id the id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage
	 * @param peList the pe list
	 * @param vmScheduler the VM scheduler
	 */
	public HostDynamicWorkload(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setUtilizationMips(0);
		setUnderAllocatedMips(new HashMap<String, List<List<Double>>>());
		setUnderAllocatedMem(new HashMap<String, List<List<Double>>>());
		setVmsMigratingIn(new ArrayList<Vm>());
		historyUtilizationQueue = new ArrayDeque<Double>();
		
	}

	/* (non-Javadoc)
	 * @see cloudsim.Host#updateVmsProcessing(double)
	 */
	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = super.updateVmsProcessing(currentTime);

		setUtilizationMips(0);

		for (Vm vm : getVmList()) {
			getVmScheduler().deallocatePesForVm(vm);
		}

		for (Vm vm : getVmList()) {
			getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
		}

		for (Vm vm : getVmList()) {
			double totalRequestedMips = vm.getCurrentRequestedTotalMips();

			if (totalRequestedMips == 0) {
				Log.printLine("VM #" + vm.getId() + " has completed its execution and destroyed");
				continue;
			}

			double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

			if (totalAllocatedMips + 0.1 < totalRequestedMips) {
				Log.printLine("Under allocated MIPS for VM #" + vm.getId() + ": requested " + totalRequestedMips + ", allocated " + totalAllocatedMips);
			}

			updateUnderAllocatedMips(vm, totalRequestedMips, totalAllocatedMips, CloudSim.clock());

			Log.formatLine("%.2f: Total allocated MIPS for VM #" + vm.getId() + " (Host #" + vm.getHost().getId() + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)", CloudSim.clock(), totalAllocatedMips, totalRequestedMips, vm.getMips(), totalRequestedMips / vm.getMips() * 100);
			if ( totalAllocatedMips < totalRequestedMips ){ 
				Log.printLineToViolationFile((int)CloudSim.clock(), vm.getId(), vm.getHost().getId(),  totalRequestedMips, totalAllocatedMips);
			}
			if (vm.isInMigration()) {
				Log.printLine("VM #" + vm.getId() + " is in migration");
				totalAllocatedMips /= 0.99; // performance degradation due to migration - 10%
			}

			setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
		}

		updateVmsProcessingMemory(currentTime);
		return smallerTime;
	}
	
	public void updateVmsProcessingMemory(double currentTime) {
		
		utilizationMem = 0;
		getRamProvisioner().deallocateRamForAllVms();
		
		for (Vm vm : getVmList()) {
			double totalRequestedMem = vm.getCurrentRequestedRam();
			getRamProvisioner().allocateRamForVm(vm, vm.getCurrentRequestedRam());
			double totalAllocatedMem = getRamProvisioner().getAllocatedRamForVm(vm);

			if (totalAllocatedMem + 0.1 < totalRequestedMem) {
				Log.printLine("Under allocated mem for VM #" + vm.getId() + ": requested " + totalRequestedMem + ", allocated " + totalAllocatedMem);
			}

			updateUnderAllocatedMem(vm, totalRequestedMem, totalAllocatedMem, CloudSim.clock());

			Log.formatLine("%.2f: Total allocated Mem for VM #" + vm.getId() + " (Host #" + vm.getHost().getId() + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)", CloudSim.clock(), totalAllocatedMem, totalRequestedMem, vm.getMips(), totalRequestedMem / vm.getMips() * 100);
			if ( totalAllocatedMem < totalRequestedMem ){ 
				Log.printLineToMemViolationFile((int)CloudSim.clock(), vm.getId(), vm.getHost().getId(),  totalRequestedMem, totalAllocatedMem);
			}
			if (vm.isInMigration()) {
				Log.printLine("VM #" + vm.getId() + " is in migration");
				totalAllocatedMem /= 0.99; // performance degradation due to migration - 10%
			}

			setUtilizationMem(getUtilizationMem() + totalAllocatedMem);
		}

	}

	private void setUtilizationMem(double mem) {
		utilizationMem += mem;
		
	}

	public double getUtilizationMem() {
		return utilizationMem;
	}

	
	/**
	 * Gets the completed vms.
	 *
	 * @return the completed vms
	 */
	public List<Vm> getCompletedVms() {
		List<Vm> vmsToRemove = new ArrayList<Vm>();
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				continue;
			}
			if (vm.getCurrentRequestedTotalMips() == 0) {
				vmsToRemove.add(vm);
			}
		}
		return vmsToRemove;
	}

	/**
	 * Gets the max utilization among by all PEs.
	 *
	 * @return the utilization
	 */
	@SuppressWarnings("unchecked")
	public double getMaxUtilization(boolean moreRecently) {
		//double currentUtilization =  PeList.getMaxUtilization((List<Pe>) getPeList());
		//return currentUtilization;
		double currentUtilization = getUtilizationOfCpu();
		return getHistoryAvgUtilization(currentUtilization,moreRecently);
	}

	public double getHistoryAvgUtilization(double currentUtilization,boolean moreRecently){
		
		if ( historyUtilizationQueue.isEmpty()){
			while(historyUtilizationQueue.size()< queueLength){
				historyUtilizationQueue.offer(currentUtilization);
			}
			return currentUtilization;
		}
		else{
			historyUtilizationQueue.remove();
			historyUtilizationQueue.offer(currentUtilization);
			Iterator<Double> it = historyUtilizationQueue.iterator();
			double avgUtilization = 0;
			int count = 0;
			int size = historyUtilizationQueue.size();
			if (moreRecently) size = size/2;
			while(it.hasNext()){
				avgUtilization += it.next();
				count ++;
				if (count>=size) break;
			}			
			return avgUtilization / count ++;
		}
	}
	/**
	 * Gets the max utilization among by all PEs
	 * allocated to the VM.
	 *
	 * @param vm the vm
	 *
	 * @return the utilization
	 */
	@SuppressWarnings("unchecked")
	public double getMaxUtilizationAmongVmsPes(Vm vm) {
		return PeList.getMaxUtilizationAmongVmsPes((List<Pe>) getPeList(), vm);
	}

	/**
	 * Gets the utilization of memory.
	 *
	 * @return the utilization of memory
	 */
	public double getUtilizationOfRam() {
		return getRamProvisioner().getUsedRam();
	}

	/**
	 * Gets the utilization of bw.
	 *
	 * @return the utilization of bw
	 */
	public double getUtilizationOfBw() {
		return getBwProvisioner().getUsedBw();
	}

	/**
	 * Update under allocated mips.
	 *
	 * @param vm the vm
	 * @param requested the requested
	 * @param allocated the allocated
	 */
	protected void updateUnderAllocatedMips(Vm vm, double requested, double allocated, double time) {
		List<List<Double>> underAllocatedMipsArray;
		List<Double> underAllocatedMips = new ArrayList<Double>();
		underAllocatedMips.add(requested);
		underAllocatedMips.add(allocated);
		underAllocatedMips.add(time);

		if (getUnderAllocatedMips().containsKey(vm.getUid())) {
			underAllocatedMipsArray = getUnderAllocatedMips().get(vm.getUid());
		} else {
			underAllocatedMipsArray = new ArrayList<List<Double>>();
		}

		underAllocatedMipsArray.add(underAllocatedMips);
		getUnderAllocatedMips().put(vm.getUid(), underAllocatedMipsArray);
	}
	
	protected void updateUnderAllocatedMem(Vm vm, double requested, double allocated, double time) {
		List<List<Double>> underAllocatedMemArray;
		List<Double> underAllocatedMem = new ArrayList<Double>();
		underAllocatedMem.add(requested);
		underAllocatedMem.add(allocated);
		underAllocatedMem.add(time);

		if (getUnderAllocatedMem().containsKey(vm.getUid())) {
			underAllocatedMemArray = getUnderAllocatedMem().get(vm.getUid());
		} else {
			underAllocatedMemArray = new ArrayList<List<Double>>();
		}

		underAllocatedMemArray.add(underAllocatedMem);
		getUnderAllocatedMem().put(vm.getUid(), underAllocatedMemArray);
	}

	/**
	 * Get current utilization of CPU in percents.
	 *
	 * @return current utilization of CPU in percents
	 */
	public double getUtilizationOfCpu() {
		return getUtilizationMips() / getTotalMips();
	}

	/**
	 * Get current utilization of CPU in MIPS.
	 *
	 * @return current utilization of CPU in MIPS
	 */
	public double getUtilizationOfCpuMips() {
		return getUtilizationMips();
	}

	/**
	 * Gets the utilization mips.
	 *
	 * @return the utilization mips
	 */
	protected double getUtilizationMips() {
		return utilizationMips;
	}

	/**
	 * Sets the utilization mips.
	 *
	 * @param utilizationMips the new utilization mips
	 */
	protected void setUtilizationMips(double utilizationMips) {
		this.utilizationMips = utilizationMips;
	}

    /**
     * Gets the under allocated mips.
     *
     * @return the under allocated mips
     */
    public Map<String, List<List<Double>>> getUnderAllocatedMips() {
		return underAllocatedMips;
	}
    
    public Map<String, List<List<Double>>> getUnderAllocatedMem() {
		return underAllocatedMem;
	}

	/**
	 * Sets the under allocated mips.
	 *
	 * @param underAllocatedMips the under allocated mips
	 */
	protected void setUnderAllocatedMips(Map<String, List<List<Double>>> underAllocatedMips) {
		this.underAllocatedMips = underAllocatedMips;
	}
	
	protected void setUnderAllocatedMem(Map<String, List<List<Double>>> underAllocatedMem) {
		this.underAllocatedMem = underAllocatedMem;
	}

}
