package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

public class Bidder {

	protected PowerHost host;
	public Bidder(PowerHost host){
		this.host = host;
	}
	
	public int bidPrice(SaleItem saleItem) {
		
		//if ( CloudSim.clock() - host.getLastMigrationTime() < 100)
		//s	return 0;
		Vm vm = saleItem.getRealItem();
		int income = (int)vm.getMips();

		if ( host.isSuitableForVm(vm)){
			double oldPower = host.getPower();
			if ( host.getVmList().size()==0 )
				oldPower = 0;
			
			double newPower = getPowerAfterAllocation(host,vm);
			int cost = (int)( newPower - oldPower);
			return income - cost;
		}

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


	public Host getHost() {		
		return host;
	}
}
