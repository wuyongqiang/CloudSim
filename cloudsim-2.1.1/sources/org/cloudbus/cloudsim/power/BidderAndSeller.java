package org.cloudbus.cloudsim.power;

import java.util.Date;
import java.util.Random;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

public class BidderAndSeller extends Bidder {

	public BidderAndSeller(PowerHost host) {
		super(host);	
	}

	public SaleItem provisionSaleItem(){
		//double laspeSinceLastMigration = CloudSim.clock() - host.getLastMigrationTime();
		//if ( laspeSinceLastMigration  < 100 )
		//	return null;
		Vm vm = null;
		SaleItem item = null;
		Boolean inMigration = false;
		for (Vm tmpVm :host.getVmList()){
			if (vm==null || vm.getMips() > tmpVm.getMips()){
				vm = tmpVm;
			}
			
			if (tmpVm.isInMigration()) inMigration = true;
		}
		
		if (vm!=null && !inMigration){
			item = new SaleItem(vm);
			Date d =new Date();
			Random r = new Random(d.getTime());
			item.setPriority(  Math.abs(r.nextInt())%10 );
			if (host.getHistoryAvgUtilization(host.getUtilizationOfCpu(), false) > 0.7){				
				item.setPriority(10);
			}			 
		}
		
		return item;
	}
	
}
