package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

public class Bidder {

	protected PowerHost host;
	private PowerDatacenter dc;
	protected int bidPrice = 0;
	private boolean networkAware = true;
	public Bidder(PowerHost host){
		this.host = host;
	}
	
	
	public SaleItemPrice bidPrice(SaleItem saleItem){
		List<Vm> vms = saleItem.getRealItems();
		
		SaleItemPrice bidPrice = new SaleItemPrice();
		for (Vm vm : vms){
			int price = 0;
			//whole sale, the owner does not bid
			if (vm.getHost().getId() == this.host.getId() && vms.size() > 1){
				price = 0;				
			}else{
				//does not bid if it is turned off a short time ago
				if ( CloudSim.clock() - host.getLastMigrationTime() < 100 &&
						hostTurnedOff() )
					price = 0;
				else
					price = bidOneVm(vm);
			}
			bidPrice.addPrice(price);
		}		
		return bidPrice;
		
	}
	
	private boolean hostTurnedOff(){
		if (host.getVmList().size()==0)
			return true;
		
		boolean allBeingMigrated = true;
		for (Vm vm : host.getVmList()){
			if (!vm.isInMigration()){
				allBeingMigrated = false;
				break;
			}				
		}
		return allBeingMigrated;		
	}
	
	protected int getSelfBidPrice(Vm vm){
		if (!canHoldTheVm(vm)) return 0;
		int income = 2*(int)vm.getMips();
		double oldPower =  host.getPower();
		double newPower =  getPowerAfterDeAllocation(vm);
		int cost = (int) (oldPower - newPower);
		return income - cost;
	}
	
	private int bidOneVm(Vm vm) {
		
		//if ( CloudSim.clock() - host.getLastMigrationTime() < 100)
		//	return 0;

		
		
		int[] vmAssign = dc.getVmAssign();
		int oldHostId = vmAssign[vm.getId()];
		int oldNetworkCost = 0;
		int incNetworkCost = 0;
		if (isNetworkAware()){
			oldNetworkCost = (int) dc.getNetworkConfig().getTotalWeight(vmAssign);
			vmAssign[vm.getId()] = this.host.getId();
			int newNetworkCost = (int) dc.getNetworkConfig().getTotalWeight(vmAssign);
			vmAssign[vm.getId()] = oldHostId;
			incNetworkCost = newNetworkCost -oldNetworkCost;
		}			
		
		int income =2 * (int)vm.getMips();

		double newPower = 0;
		double oldPower = 0;
		int cost = 0;
		bidPrice = 0;
		//if the vm belongs to the host
		if (vm.getHost().getId() == this.host.getId() ){
			oldPower = host.getPower();
			newPower = getPowerAfterDeAllocation(vm);
			cost = (int) (oldPower - newPower);
			bidPrice = income - cost;
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
			return bidPrice - incNetworkCost;
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
	
	protected int getTotalIncome(){
		int income = 0;
		for(Vm vm : host.getVmList()){
			income += vm.getMips();
		}
		return income*2;
	}


	public Host getHost() {		
		return host;
	}
	
	protected boolean canHoldMoreVm(){
		return host.getMaxUtilization(false) < 0.7 && host.getUtilizationOfMem()<0.7;
	}
	
	protected boolean canHoldTheVm(Vm vm){
		if (vm.getHost() == host){
			if (host.getMaxUtilization(false) < 0.7 && host.getUtilizationOfMem()<0.7) 
				return true;
			else
				return false;
		}
		double avgMips = vm.getAvgCurrentRequestedTotalMips();
		double mem = vm.getCurrentRequestedRam();
		double increaseUtilMem = mem/ host.getRam();
		double increaseUtil = avgMips/ host.getMaxAvailableMips();
		return (host.getMaxUtilization(false) + increaseUtil <= 0.7
				&& host.getUtilizationOfMem() + increaseUtilMem <= 0.7); 
				//|| host.getMaxUtilization(false) > 0.4;
		
	}

	public void setDc(PowerDatacenter dc) {
		this.dc = dc;
	}


	public boolean isNetworkAware() {
		return networkAware;
	}


	public void setNetworkAware(boolean networkAware) {
		this.networkAware = networkAware;
	}
}
