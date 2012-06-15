package org.cloudbus.cloudsim.power;

import java.awt.image.SampleModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.lists.PowerVmList;

public class PowerVmAllocationPolicyTrading extends
		PowerVmAllocationPolicySingleThreshold {

	/** The utilization threshold. */
	private double utilizationLowThreshold = 0.5;
	private PowerDatacenter dc;

	public PowerVmAllocationPolicyTrading(
			List<? extends PowerHost> list, double utilizationThreshold,
			double utilizationLowThreshold) {
		super(list, utilizationThreshold);
		setUtilizationLowThreshold(utilizationLowThreshold);
		
	}
	
	public void setPowerDatacenter(PowerDatacenter dc){
		this.dc = dc;
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

		List<Map<String, Object>> migrate = tradeInGrps();
		if (migrate.size()>0)
			migrationMap.addAll(migrate);

		restoreAllocation(vmsToRestore, getHostList());
		return migrationMap;
	}

	private List<Map<String, Object>> tradeIn2Grps() {
		
		PowerHost allocatedHost=null;
		
		ArrayList<Map<String, Object>> migList0 = new ArrayList<Map<String,Object>>();
		Market market0 = createMarket(2,0);
		addMigList(migList0, market0,null);	
		
		boolean bNotFind0 = (migList0.size()==0) && market0.getSelectedSaleItem()!=null;		
		
		ArrayList<Map<String, Object>> migList1 = new ArrayList<Map<String,Object>>();
		Market market1 = createMarket(2,1);
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
	
private List<Map<String, Object>> tradeInGrps() {
	
		int groupNum = 4;
		Market[] markets = new Market[groupNum];		
		List<Map<String, Object>> migListAll =  new ArrayList<Map<String,Object>>();
		
		List<TreeMap<Integer,ArrayList<Map<String, Object>>>> bidList 
			= new ArrayList<TreeMap<Integer,ArrayList<Map<String, Object>>>>(); 
		
		List<SaleItem> saleItems = new ArrayList<SaleItem>();
		
		// first round to check whether there is need to migrate within the group
		for(int i=0;i<groupNum;i++){
			ArrayList<Map<String, Object>> migList = new ArrayList<Map<String,Object>>();
			Market market = createMarket(groupNum,i);
			markets[i] = market;
			int price = addMigList(migList, market,null);
			
			saleItems.add(market.getSelectedSaleItem());
			TreeMap<Integer, ArrayList<Map<String, Object>>> e = new TreeMap<Integer,ArrayList<Map<String, Object>>>();
			e.put(price*1000+i, migList);
			bidList.add(e);
		}
							
		// second round to check whether it is possible to find hosts in other groups to migrate to
		for(int i=0;i<groupNum;i++){
			SaleItem item = saleItems.get(i);
			if (item==null) continue;
			for(int j=0;j<groupNum;j++){
				if (i==j) continue; //no need to check its own group again
				ArrayList<Map<String, Object>> migList = new ArrayList<Map<String,Object>>();
				Market market = markets[j];
				
				int price = addMigList(migList, market,item);								
				Map<Integer, ArrayList<Map<String, Object>>> e = bidList.get(i);
				if (price>0)
					e.put(price*1000 + j, migList);				
			}
		}
		
		//pick the best group to migrate to
		List<Integer> picked = new ArrayList<Integer>();
		
		for(int i=0;i<groupNum;i++){
			SaleItem item = saleItems.get(i);
			if (item==null) continue;
			TreeMap<Integer, ArrayList<Map<String, Object>>> e = bidList.get(i);
			Iterator<Integer> it = e.descendingKeySet().iterator();
			while(it.hasNext()){
				int priceAndGrp = it.next();
				int grp = priceAndGrp % 1000;
				if (!picked.contains(grp)){
					picked.add(grp);
					migListAll.addAll(e.get(priceAndGrp));
					break;
				}
			}
		}
		
		//migListAll.addAll(migList);
		return migListAll;
	}

private List<Map<String, Object>> trade() {
		ArrayList<Map<String, Object>> migList0 = new ArrayList<Map<String,Object>>();
		Market market0 = createMarket();
		addMigList(migList0, market0,null);	
		
		return migList0;
	}

	private int addMigList(ArrayList<Map<String, Object>> migList,
			Market market, SaleItem item) {
		PowerHost allocatedHost;
		int bidResult = 0;
		if (item==null){
			bidResult = market.bid();
		}else{
			market.setSelectedSaleItem(item);
			bidResult = market.bidWithoutSaleItem();
		}
		if (bidResult>0) {
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
		return bidResult;
	}
	
	private int quote(ArrayList<Map<String, Object>> migList,
			Market market, SaleItem item) {
		PowerHost allocatedHost;
		int bidResult = 0;
		if (item==null){
			bidResult = market.bid();
		}else{
			market.setSelectedSaleItem(item);
			bidResult = market.bidWithoutSaleItem();
		}
		if (bidResult>0) {
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
		return bidResult;
	}
	
	
	private void log(String s){
		System.out.println(CloudSim.clock()+":\t"+s);
	}
	

	private Market createMarket(int groups, int marketNo) {
		// 9 / 2 = 4, two groups 4 , 5
		// 13/4 = 3, 3 groups, 3,3,3,4
		int hostNum =  this.<PowerHost>getHostList().size();
		int numPerGroup = hostNum / groups;
		int numTheGroup = numPerGroup;
		if(marketNo == groups-1){ // the last group		
			numTheGroup = hostNum - (groups-1)*numPerGroup;
		}
		int hostNo[] = new int[numPerGroup];
		for (int i= numPerGroup * marketNo ; i< numPerGroup * marketNo+numTheGroup;i++){
			int inx = i - numPerGroup * marketNo; 
			hostNo[inx] = i;
		}
		int marketHosts[] = hostNo;
		Market market = new Market();
		for (PowerHost host : this.<PowerHost>getHostList()) {
			if (inList(host.getId(),marketHosts)){
				BidderAndSeller bs = new BidderAndSeller(host);
				bs.setDc(dc);
				market.addBidder(bs);
				market.addSaleItem(bs.provisionSaleItem());
			}
		}
		return market;
	}
	
	private Market createMarket() {
		Market market = new Market();
		for (PowerHost host : this.<PowerHost>getHostList()) {			
			BidderAndSeller bs = new BidderAndSeller(host);
			bs.setDc(dc);
			market.addBidder(bs);
			market.addSaleItem(bs.provisionSaleItem());		
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
