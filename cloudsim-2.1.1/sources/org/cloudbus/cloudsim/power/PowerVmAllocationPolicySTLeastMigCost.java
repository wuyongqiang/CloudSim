package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.lists.PowerHostList;
import org.cloudbus.cloudsim.power.lists.PowerVmList;
import org.cloudbus.cloudsim.power.migration.MigrationSchedulerFFD;

public class PowerVmAllocationPolicySTLeastMigCost extends
		PowerVmAllocationPolicySingleThreshold {

	public PowerVmAllocationPolicySTLeastMigCost(
			List<? extends PowerHost> list, double utilizationThreshold) {
		super(list, utilizationThreshold);
		_lastReshuffleTime = 0;
		setReshuffleInterval(100);
	}
	
	private int _reshuffleInterval = 60;
	private long _lastReshuffleTime = 0;
	private boolean _normalFFD = false;
	private boolean _incrementalFFD = false;
	private boolean _usingSA = false;
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		List<Map<String, Object>> migrationMap = new ArrayList<Map<String, Object>>();
		if (vmList.isEmpty()) {
			return migrationMap;
		}
		
		if ( _lastReshuffleTime!=0 )
		{
			if (CloudSim.clock() - _lastReshuffleTime < _reshuffleInterval)
			return migrationMap;
		}
		{
			if (CloudSim.clock() - _lastReshuffleTime < 30)
				return migrationMap;
		}
		_lastReshuffleTime = (long) CloudSim.clock();
		saveAllocation(vmList);
		
		List<Vm> vmsToRestore = new ArrayList<Vm>();
		vmsToRestore.addAll(vmList);

		Map<Integer,Integer> inMigrationHosts = new HashMap<Integer, Integer>();
		List<Vm> vmsToMigrate = new ArrayList<Vm>();
		for (Vm vm : vmList) {
			
			int inMigrationVmsOnTheHost = 0;
			if (inMigrationHosts.get(vm.getHost().getId())!=null)
				inMigrationVmsOnTheHost = inMigrationHosts.get(vm.getHost().getId()).intValue();
			
			if ( vm.isInMigration()){
				inMigrationHosts.put(vm.getHost().getId(),inMigrationVmsOnTheHost+1);
				continue;
			}			
			
			if (_incrementalFFD){
				//don't know what to do with this
			}
			
			/*if ( inMigrationVmsOnTheHost >= 2 ){				
				continue;
			}*/
			
			if (vm.isRecentlyCreated() ) {				
				continue;
			}
			vmsToMigrate.add(vm);
			inMigrationHosts.put(vm.getHost().getId(), inMigrationVmsOnTheHost + 1);
			vm.getHost().vmDestroy(vm);			
		}
		
		int oldPMInUse = getOldHostInUse(vmsToMigrate).size();
		/*
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
		}*/
		
		//1) do the FFD to find the how many PMs needed
		
		PowerVmList.sortByCpuUtilization(vmsToMigrate);
		for (Vm vm : vmsToMigrate) {
			PowerHost oldHost = (PowerHost) getVmTable().get(vm.getUid());
			PowerHost allocatedHost = findHostForVm(vm);
			if (allocatedHost != null){
				allocatedHost.vmCreate(vm);
				//System.out.println("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());
				Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());
				if( allocatedHost.getId() != oldHost.getId()) {				
					Map<String, Object> migrate = new HashMap<String, Object>();
					migrate.put("vm", vm);
					migrate.put("host", allocatedHost);
					migrationMap.add(migrate);
				}
			}else
			{
				System.err.println("VM #" + vm.getId() + " cannot find a host");
			}
		}
		int newPMInUse = getHostInUse(vmsToMigrate).size();
		//2) compare the new PM number with the old one
		
		// revert the assignment
		for (Vm vm : vmsToMigrate) {
			if (vm.getHost()!=null)
			vm.getHost().vmDestroy(vm);
		}
		if (!_normalFFD)
			migrationMap = findCostLeastMigration(vmsToMigrate,oldPMInUse, newPMInUse);
		
		System.out.println("migration="+	migrationMap.size());
		restoreAllocation(vmsToRestore, getHostList());

		return migrationMap;
	}
	private List<Map<String, Object>> findCostLeastMigration(
			List<Vm> vmsToMigrate, int oldPMInUse, int newPMInUse) {
		List<Map<String, Object>> migrationMap = new ArrayList<Map<String, Object>>();
		
		double[] pCPU = new double[getHostList().size()];
		double[] pVM = new double[vmsToMigrate.size()];
		int[] oldVmAssign = new int[vmsToMigrate.size()];
		String[] vmNames = new String[vmsToMigrate.size()];
		
		for (int i=0;i<pCPU.length;i++){
			pCPU[i] = getHostList().get(i).getAvailableMips();
		}
		for (int i=0;i<pVM.length;i++){
			pVM[i] = vmsToMigrate.get(i).getAvgCurrentRequestedTotalMips();
			vmNames[i] = vmsToMigrate.get(i).getUid();
		}
		for (int i=0;i<oldVmAssign.length;i++){
			oldVmAssign[i] = -1;
			Host host = getOldHost( vmsToMigrate.get(i));
			if (host!=null){
				oldVmAssign[i]=getHostList().indexOf(host);
			}else
			{
				System.err.println("old host not found");
			}
		}
		
		int[] vmAssign = null;
		if (_usingSA){
			SimulationAnneal anneal = new SimulationAnneal(pCPU, pVM, oldVmAssign, oldPMInUse, newPMInUse, getUtilizationThreshold(),vmNames);
			anneal.anneal();
			vmAssign = anneal.getAssignment();
		}else{		
			MigrationSchedulerFFD scheduler = new MigrationSchedulerFFD();
			scheduler.initScheduler(pCPU, pVM, oldVmAssign, oldPMInUse, newPMInUse, getUtilizationThreshold(),vmNames);
			scheduler.scheduleMigration();
			vmAssign = scheduler.getAssignment();
		}
		
		for (int i=0;i<vmAssign.length;i++){
			if( vmAssign[i] != oldVmAssign[i]) {				
				Map<String, Object> migrate = new HashMap<String, Object>();
				migrate.put("vm", vmsToMigrate.get(i));
				migrate.put("host", getHostList().get(vmAssign[i]));
				migrationMap.add(migrate);
			}
		}
		
		return migrationMap;
	}
	
	
	private Host getOldHost(Vm vm) {
		for (Map<String, Object> map : getSavedAllocation()) {
			Vm vmTmp = (Vm) map.get("vm"); 
			if ( vmTmp.getId() == vm.getId()){
			PowerHost host = (PowerHost) map.get("host");			
			return host;
			}
		}
		return null;
	}
	public int getReshuffleInterval() {
		return _reshuffleInterval;
	}
	public void setReshuffleInterval(int _reshuffleInterval) {
		this._reshuffleInterval = _reshuffleInterval;
	}

	protected Map<Integer,Host> getOldHostInUse(List<? extends Vm> vmList) {
		
		Map<Integer,Host> inUseHosts = new HashMap<Integer, Host>();
		
		for (Vm vm : vmList) {	
			Host oldHost = getOldHost(vm);
			 inUseHosts.put(oldHost.getId(), 
			 oldHost);			
		}
		
		return inUseHosts;
	}
	
protected Map<Integer,Host> getHostInUse(List<? extends Vm> vmList) {
		
		Map<Integer,Host> inUseHosts = new HashMap<Integer, Host>();
		
		for (Vm vm : vmList) {	
			if (vm.getHost()!=null)
			 inUseHosts.put(vm.getHost().getId(), 
					 vm.getHost());			
		}
		
		return inUseHosts;
	}
	
	/**
	 * Determines a host to allocate for the VM.
	 *
	 * @param vm the vm
	 *
	 * @return the host
	 */
	@Override
	public PowerHost findHostForVm(Vm vm) {
		//double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;
		PowerHostList.sortByCpu(getHostList());
		for (PowerHost host : this.<PowerHost>getHostList()) {
			if (host.isSuitableForVm(vm)) {
				double maxUtilization = getMaxUtilizationAfterAllocation(host, vm);
				//if ((!vm.isRecentlyCreated() && maxUtilization > getUtilizationThreshold()) || (vm.isRecentlyCreated() && maxUtilization > 1.0)) {
				if ( maxUtilization > getUtilizationThreshold() ) {
					continue;
				}
				else
				{
					allocatedHost = host;
					break;//FFD gets the first fit
				}
			}
		}

		return allocatedHost;
	}
	
	public String getPolicyDesc() {
		String rst = String.format("SA%.2f Interval%d", getUtilizationThreshold(),_reshuffleInterval);
		return rst;
	}

}
