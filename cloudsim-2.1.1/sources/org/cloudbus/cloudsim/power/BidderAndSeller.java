package org.cloudbus.cloudsim.power;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

public class BidderAndSeller extends Bidder implements Seller {

	public BidderAndSeller(PowerHost host) {
		super(host);	
	}
	
	private SaleItem saleItem = null;

	public SaleItem provisionSaleItem(){
		int priority = (int) (CloudSim.clock() - host.getLastMigrationTime());
		
		Vm vm = null;
		SaleItem item = null;
		Boolean inMigration = false;
		for (Vm tmpVm :host.getVmList()){
			if (vm==null || vm.getMips() > tmpVm.getMips()){
				vm = tmpVm;
			}
			
			if (tmpVm.isInMigration()) inMigration = true;
		}
		
		
		if (vm != null && !inMigration) {
			if (CloudSim.clock()<vm.getRecommendMigrationInterval() 
					|| vm.getRecommendMigrationInterval() + vm.getLastMigrationTime() < CloudSim.clock()){
				item = new SaleItem(vm, this);
				item.setOwner(this);
				// Date d =new Date();
				// Random r = new Random(d.getTime());
				item.setPriority(priority); // Math.abs(r.nextInt())%10 );
				if (host.getMaxUtilization(false) > 0.8)
					item.setPriority(20000);
				else if (host.getMaxUtilization(false) > 0.75)
					item.setPriority(15000);
				else if (host.getMaxUtilization(false) > 0.7)
					item.setPriority(10000);
				else if (!canHoldMoreVm())
					item.setPriority(9999);
				else if (host.getMaxUtilization(false) < 0.02) {
					item.setPriority(6999);
				}else if (host.getMaxUtilization(false) < 0.4) {
					if (host.getVmsMigratingIn().size()==0){
					item.setPriority(5999);					
					item.getRealItems().clear();
					item.getRealItems().addAll(host.getVmList());
					}
					else{
						item = null;
					}
				}
				else if (host.getMaxUtilization(false) < 0.4) {
					item.setPriority(4999);
				} 
				else {
					//item = null;
				}
			}
		}
		
		saleItem = item;
		return item;
	}


	/*@Override
	public SaleItem getSaleItem() {		
		return saleItem;
	}*/

	@Override
	public boolean accept(SaleItemPrice price) {
		int selfBidPrice = 0;
		if (price.getPriceList().size()>1){
			selfBidPrice = getTotalIncome() - (int)host.getPower();
		}else
			selfBidPrice =  getSelfBidPrice(saleItem.getRealItem());
		
		int diff =  price.totalPrice() - selfBidPrice;
		boolean accepted = false;
		if (selfBidPrice>0 && diff* 1.0 / selfBidPrice > 0.00){
			System.out.println("bidPrice " + price.totalPrice() +" selfBidPrice=" + selfBidPrice );
			accepted = true;
		}
		if (!canHoldTheVm(saleItem.getRealItem())){
			System.out.println("cannot hold the vm " + String.format("%.2f%%",  host.getMaxUtilization(false)*100 ) +  String.format(" mem=%.2f%%",  host.getUtilizationOfMem()*100,false) );
			accepted = true;
		}
		return accepted;
	}


	@Override
	public SaleItem getSaleItem() {
		return saleItem;
	}
	
}
