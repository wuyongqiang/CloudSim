package org.cloudbus.cloudsim.power;

import java.awt.image.SampleModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.lists.PowerVmList;

public class PowerVmAllocationPolicyTrading extends
		PowerVmAllocationPolicySingleThreshold {

	/** The utilization threshold. */
	private double utilizationLowThreshold = 0.5;

	public PowerVmAllocationPolicyTrading(
			List<? extends PowerHost> list, double utilizationThreshold,
			double utilizationLowThreshold) {
		super(list, utilizationThreshold);
		setUtilizationLowThreshold(utilizationLowThreshold);
	}
	
	public void setUtilizationLowThreshold(double utilizationLowThreshold) {
		this.utilizationLowThreshold = utilizationLowThreshold;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		
		List<Map<String, Object>> migrationMap = new ArrayList<Map<String, Object>>();
		if (vmList.isEmpty()) {
			return migrationMap;
		}
		
		saveAllocation(vmList);
		List<Vm> vmsToRestore = new ArrayList<Vm>();
		vmsToRestore.addAll(vmList);

		List<Map<String, Object>> migrate = trade();
		if (migrate.size()>0)
			migrationMap.addAll(migrate);

		restoreAllocation(vmsToRestore, getHostList());
		return migrationMap;
	}

	private List<Map<String, Object>> trade() {
		
		PowerHost allocatedHost=null;
		
		ArrayList<Map<String, Object>> migList0 = new ArrayList<Map<String,Object>>();
		Market market0 = createMarket(0);
		addMigList(migList0, market0,null);	
		
		boolean bNotFind0 = (migList0.size()==0) && market0.getSelectedSaleItem()!=null;		
		
		ArrayList<Map<String, Object>> migList1 = new ArrayList<Map<String,Object>>();
		Market market1 = createMarket(1);
		addMigList(migList1, market1,null);	
		
		boolean bNotFind1 = (migList1.size()==0) && market1.getSelectedSaleItem()!=null;	
		/*
		if (bNotFind0){
			addMigList(migList1, market1,market0.getSelectedSaleItem());	
		}
		
		if (bNotFind1){
			addMigList(migList0, market0,market1.getSelectedSaleItem());	
		}*/
		
		migList0.addAll(migList1);
		return migList0;
	}

	private void addMigList(ArrayList<Map<String, Object>> migList,
			Market market, SaleItem item) {
		PowerHost allocatedHost;
		boolean bidResult = false;
		if (item==null){
			bidResult = market.bid();
		}else{
			market.setSelectedSaleItem(item);
			bidResult = market.bidWithoutSaleItem();
		}
		if (bidResult) {
			for (int i=0;i<market.getSoldItem().getRealItems().size();i++) {
				Vm vm = market.getSoldItem().getRealItems().get(i);
				allocatedHost = (PowerHost) market.getBuyers().get(i).getHost();
				Map<String, Object> migrate = new HashMap<String, Object>();
				if (vm != null && allocatedHost != null
						&& vm.getHost().getId() != allocatedHost.getId()) {
					migrate.put("vm", vm);
					migrate.put("host", allocatedHost);
					PowerHost oldHost = (PowerHost) vm.getHost();
					if (oldHost != null)
						oldHost.setLastMigrationTime(CloudSim.clock());
					vm.setLastMigrationTime(CloudSim.clock());
					
					migList.add(migrate);
					log(vm.getId()+"from \t" + vm.getHost().getId() + "to \t" + allocatedHost.getId() );
				}
			}
		}
	}
	
	
	private void log(String s){
		System.out.println(CloudSim.clock()+":\t"+s);
	}
	

	private Market createMarket(int marketNo) {
		//int hostNo[][] = {{0,1,2,3,4,5},{}};
		int hostNo[][] = {{0,1,2},{3,4,5}};
		int marketHosts[] = hostNo[marketNo];
		Market market = new Market();
		for (PowerHost host : this.<PowerHost>getHostList()) {
			if (inList(host.getId(),marketHosts)){
				BidderAndSeller bs = new BidderAndSeller(host);
				market.addBidder(bs);
				market.addSaleItem(bs.provisionSaleItem());
			}
		}
		return market;
	}

	private boolean inList(int id, int[] marketHosts) {
		for(int i=0;i<marketHosts.length;i++){
			if (marketHosts[i]==id) return true;
		}
		return false;
	}

	@Override
	public String getPolicyDesc() {
		String rst = String.format("trading%.2f-%.2f", getUtilizationThreshold(),
				utilizationLowThreshold);
		return rst;
	}
}
