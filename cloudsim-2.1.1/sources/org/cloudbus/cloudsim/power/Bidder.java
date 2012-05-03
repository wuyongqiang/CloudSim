package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

public class Bidder {

	protected PowerHost host;
	protected int bidPrice = 0;
	public Bidder(PowerHost host){
		this.host = host;
	}
	
	public int bidPrice(SaleItem saleItem) {
		
		//if ( CloudSim.clock() - host.getLastMigrationTime() < 100)
		//	return 0;

		Vm vm = saleItem.getRealItem();
		
		
		int income = (int)vm.getMips();

		double newPower = 0;
		double oldPower = 0;
		int cost = 0;
		bidPrice = 0;
		//if the vm belongs to the host
		if (vm.getHost().getId() == this.host.getId() ){
			oldPower = host.getPower();
			newPower = getPowerAfterDeAllocation(vm);
			cost = (int)(oldPower - newPower);
			bidPrice = income -cost;
		}
		else if ( host.isSuitableForVm(vm) ){
			oldPower = host.getPower();
			if ( host.getVmList().size()==0 )
				oldPower = 0;
			
			newPower = getPowerAfterAllocation(host,vm);
			cost = (int)( newPower - oldPower);
			bidPrice = income - cost;
		}

		if (canHoldMoreVm() && canHoldTheVm(vm))
			return bidPrice;
		else
			return 0;		
	}
	
	protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
		List<Double> allocatedMipsForVm = null;
		PowerHost allocatedHost = (PowerHost) vm.getHost();

		if (allocatedHost != null) {
			allocatedMipsForVm = allocatedHost.getAllocatedMipsForVm(vm);
		}

		if (!host.allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			return -1;
		}

		double power = host.getPower();

		host.deallocatePesForVm(vm);

		if (allocatedHost != null && allocatedMipsForVm != null) {
			vm.getHost().allocatePesForVm(vm, allocatedMipsForVm);
		}

		return power;
	}
	
	protected double getPowerAfterDeAllocation( Vm vm) {
		List<Double> allocatedMipsForVm = null;
		PowerHost allocatedHost = (PowerHost) vm.getHost();

		if (allocatedHost != null) {
			allocatedMipsForVm = allocatedHost.getAllocatedMipsForVm(vm);
		}

		vm.getHost().deallocatePesForVm(vm);
		
		double power = host.getPower();		

		if (allocatedHost != null && allocatedMipsForVm != null) {
			vm.getHost().allocatePesForVm(vm, allocatedMipsForVm);
		}

		return power;
	}


	public Host getHost() {		
		return host;
	}
	
	private boolean canHoldMoreVm(){
		return host.getMaxUtilization(false) < 0.7;
	}
	
	protected boolean canHoldTheVm(Vm vm){
		if (vm.getHost() == host) return true;
		double avgMips = vm.getAvgCurrentRequestedTotalMips();
		double increaseUtil = avgMips/ host.getMaxAvailableMips();
		return (host.getMaxUtilization(false) + increaseUtil) < 0.7 ||
				host.getMaxUtilization(false) > 0.4;
		
	}

}
