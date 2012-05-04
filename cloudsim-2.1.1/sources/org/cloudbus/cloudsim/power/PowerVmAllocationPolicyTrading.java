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

		Map<String, Object> migrate = trade();
		if (migrate.size()>0)
			migrationMap.add(migrate);

		restoreAllocation(vmsToRestore, getHostList());
		return migrationMap;
	}

	private Map<String, Object> trade() {
		
		PowerHost allocatedHost=null;
		Map<String, Object> migrate = new HashMap<String, Object>();
		
		Market market = createMarket();
		if (market.bid()) {
			for (int i=0;i<market.getSoldItem().getRealItems().size();i++) {
				Vm vm = market.getSoldItem().getRealItems().get(i);
				allocatedHost = (PowerHost) market.getBuyers().get(i).getHost();

				if (vm != null && allocatedHost != null
						&& vm.getHost().getId() != allocatedHost.getId()) {
					migrate.put("vm", vm);
					migrate.put("host", allocatedHost);
					PowerHost oldHost = (PowerHost) vm.getHost();
					if (oldHost != null)
						oldHost.setLastMigrationTime(CloudSim.clock());
					vm.setLastMigrationTime(CloudSim.clock());
				}
			}
		}		
		return migrate;
	}

	

	private Market createMarket() {
		Market market = new Market();
		for (PowerHost host : this.<PowerHost>getHostList()) {
			BidderAndSeller bs = new BidderAndSeller(host);
			market.addBidder(bs);
			market.addSaleItem(bs.provisionSaleItem());			
		}
		return market;
	}

	@Override
	public String getPolicyDesc() {
		String rst = String.format("trading%.2f-%.2f", getUtilizationThreshold(),
				utilizationLowThreshold);
		return rst;
	}
}
