package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;

public class VmAllocationPolicySimplePacking extends VmAllocationPolicySimple    {

	private double utilizationThreshold;
	
	public VmAllocationPolicySimplePacking(List<? extends Host> list, double utilizationThreshold ) {
		super(list);	
		setUtilizationThreshold(utilizationThreshold);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		PowerHost allocatedHost = findHostForVm(vm);
		if (allocatedHost != null && allocatedHost.vmCreate(vm)) { //if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), allocatedHost);
			if (!Log.isDisabled()) {
				Log.printLine(String.format("%.2f: VM #" + vm.getId() + " has been allocated to the host #" + allocatedHost.getId() + "\n", CloudSim.clock()));
			}
			return true;
		}
		return false;
	}
	
	protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
		List<Double> allocatedMipsForVm = null;
		PowerHost allocatedHost = (PowerHost) vm.getHost();

		if (allocatedHost != null) {
			allocatedMipsForVm = vm.getHost().getAllocatedMipsForVm(vm);
		}

		if (!host.allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			return Double.MAX_VALUE;
		}

		double maxUtilization = host.getMaxUtilizationAmongVmsPes(vm);

		host.deallocatePesForVm(vm);

		if (allocatedHost != null && allocatedMipsForVm != null) {
			vm.getHost().allocatePesForVm(vm, allocatedMipsForVm);
		}

		return maxUtilization;
	}
	private List<PowerHost> sortHostList(){
		List<PowerHost> result = new ArrayList<PowerHost>(getHostList().size());
		Map<Integer, PowerHost> sortedMap = new HashMap<Integer, PowerHost>();
		
		for (PowerHost host : this.<PowerHost>getHostList()) {
			double maxMips = 0;
			PowerHost maxMipsHost = null;
			for (PowerHost hostInner : this.<PowerHost>getHostList()){
				if (!sortedMap.containsKey( hostInner.getId())
						&& maxMips<host.getMaxAvailableMips()){
					maxMips = hostInner.getMaxAvailableMips();
					maxMipsHost = hostInner;
				}
				if (maxMipsHost!=null){
					sortedMap.put(maxMipsHost.getId(), maxMipsHost);
					result.add(maxMipsHost);
				}
			}
		}
		return result;
	}
	
	protected PowerHost findHostForVm(Vm vm) {
		PowerHost allocatedHost = null;
		for (PowerHost host : sortHostList()) {
			if (host.isSuitableForVm(vm)) {
				double maxUtilization = getMaxUtilizationAfterAllocation(host, vm);
				//if ((!vm.isRecentlyCreated() && maxUtilization > getUtilizationThreshold()) || (vm.isRecentlyCreated() && maxUtilization > 1.0)) {
				if ( maxUtilization <= getUtilizationThreshold() ) {
				allocatedHost = host;				
				}
			}
		}
		return allocatedHost;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		if (getVmTable().containsKey(vm.getUid())) {
			PowerHost host = (PowerHost) getVmTable().remove(vm.getUid());
			if (host != null) {
				host.vmDestroy(vm);
			}
		}
	}

	private double getUtilizationThreshold() {
		return utilizationThreshold;
	}	
	
	
	private void setUtilizationThreshold(double utilizationThreshold) {
		this.utilizationThreshold = utilizationThreshold;		
	}
	
	@Override
	public String getPolicyDesc() {		
		return "nm-sp";
	}

}
