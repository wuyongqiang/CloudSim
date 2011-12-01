package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.lists.PowerVmList;

public class PowerVmAllocationPolicyDoubleThreshold extends PowerVmAllocationPolicySingleThreshold {
	
	/** The utilization threshold. */
	private double utilizationLowThreshold = 0.5;

	public PowerVmAllocationPolicyDoubleThreshold(
			List<? extends PowerHost> list, double utilizationThreshold,
			double utilizationLowThreshold) {
		super(list, utilizationThreshold);
		setUtilizationLowThreshold(utilizationLowThreshold);
	}
	
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		List<Map<String, Object>> migrationMap = new ArrayList<Map<String, Object>>();
		if (vmList.isEmpty()) {
			return migrationMap;
		}
		saveAllocation(vmList);
		List<Vm> vmsToRestore = new ArrayList<Vm>();
		vmsToRestore.addAll(vmList);

		List<Vm> vmsToMigrate = new ArrayList<Vm>();
		for (Vm vm : vmList) {
			if (vm.isRecentlyCreated() || vm.isInMigration()) {
				continue;
			}
			if ( ((PowerHost) vm.getHost()).getMaxUtilizationAmongVmsPes(vm) > this.getUtilizationThreshold()) {
				vmsToMigrate.add(vm);
				vm.getHost().vmDestroy(vm);			
			}
			else if ( ((PowerHost) vm.getHost()).getMaxUtilizationAmongVmsPes(vm) < this.getUtilizationLowThreshold()) {
				vmsToMigrate.add(vm);
				vm.getHost().vmDestroy(vm);			
			}
		}
		PowerVmList.sortByCpuUtilization(vmsToMigrate);

		for (PowerHost host : this.<PowerHost>getHostList()) {
			host.reallocateMigratingVms();
		}

		for (Vm vm : vmsToMigrate) {
			PowerHost oldHost = (PowerHost) getVmTable().get(vm.getUid());
			PowerHost allocatedHost = findHostForVm(vm);
			if (allocatedHost != null){
				allocatedHost.vmCreate(vm);
				Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());
				if( allocatedHost.getId() != oldHost.getId()) {				
					Map<String, Object> migrate = new HashMap<String, Object>();
					migrate.put("vm", vm);
					migrate.put("host", allocatedHost);
					migrationMap.add(migrate);
				}
			}
		}

		restoreAllocation(vmsToRestore, getHostList());

		return migrationMap;
	}
	
	
	
	public void setUtilizationLowThreshold(double utilizationLowThreshold){
		this.utilizationLowThreshold = utilizationLowThreshold ;
	}
	
	public double getUtilizationLowThreshold(){
		return this.utilizationLowThreshold;
	}
	
	@Override
	public String getPolicyDesc() {
		String rst = String.format("MM%.2f-%.2f", getUtilizationThreshold(),getUtilizationLowThreshold());
		return rst;
	}
}
