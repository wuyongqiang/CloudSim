package org.cloudbus.cloudsim.power;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

public class Market {
	
	private List<Bidder> bidders = new ArrayList<Bidder>();
	private List<SaleItem> items = new ArrayList<SaleItem>();
	
	private SaleItem soldItem = null;
	private List<Bidder> buyers = new ArrayList<Bidder>();
	
	private SaleItem saleItem = null;
	private Map<Host,Vm> restoreMap = new HashMap<Host, Vm>();
	
	private TreeMap<Double,Host> bidderAndPrice = new TreeMap<Double, Host>();
	
	public TreeMap<Double,Host> getBidderMap(){
		return bidderAndPrice;
	}
	
	public int bid(){
		saleItem = selectSaleItem(); 
		return bidWithoutSaleItem();
	}
	
	public int bidWithoutSaleItem(){
		Boolean soldSuccess = false;
		int totalPrice = 0;
		restoreMap.clear();
		bidderAndPrice.clear();
		if (saleItem!=null){
			
			int reservedPrice = saleItem.getOwner().reservedPrice();
			//int vmCount = saleItem.getRealItems().size();
			//ArrayList<Integer> priceList = new ArrayList<Integer>(vmCount);
			SaleItemPrice bidPrice = new SaleItemPrice();
			for (int i=0;i<saleItem.getRealItems().size();i++){
				bidPrice.addPrice(0);
				buyers.add(null);
			}
	
			for(int i=0;i< saleItem.getRealItems().size();i++){
				
				
				buyers.set(i, null);
				bidPrice.getPriceList().set(i, 0);
			
				//for one VM that every bidder can bid
				for (Bidder bidder : bidders){				
					SaleItemPrice curBidPrice = bidder.bidPrice(saleItem);
					Integer tmpPrice = curBidPrice.getPriceList().get(i);
					if (curBidPrice.getPriceList().get(i)>0 && curBidPrice.getPrice() > reservedPrice) {												
						Random r = new Random(bidder.getHost().getId()); //use the random to guarantee there is no same price in the map
						bidderAndPrice.put(tmpPrice+r.nextDouble()/1000, bidder.getHost());
					}					
				}	
				
			}					
		}
		
		selectHighestOffers();
		return bidderAndPrice.size();
	}
	
	private void selectHighestOffers() {
		
	}

	public int bidWithoutSaleItemWholeSale(){
		Boolean soldSuccess = false;
		int totalPrice = 0;
		restoreMap.clear();
		
		if (saleItem!=null){
			
			//int vmCount = saleItem.getRealItems().size();
			//ArrayList<Integer> priceList = new ArrayList<Integer>(vmCount);
			SaleItemPrice bidPrice = new SaleItemPrice();
			for (int i=0;i<saleItem.getRealItems().size();i++){
				bidPrice.addPrice(0);
				buyers.add(null);
			}
				
			
			
				
			for(int i=0;i< saleItem.getRealItems().size();i++){
				
				
				buyers.set(i, null);
				bidPrice.getPriceList().set(i, 0);
			
				//for one VM that every bidder can bid
				for (Bidder bidder : bidders){				
					SaleItemPrice curBidPrice = bidder.bidPrice(saleItem);
					
					if (bidPrice.getPriceList().get(i) < curBidPrice.getPriceList()
							.get(i)) {
						Integer tmpPrice = curBidPrice.getPriceList().get(i);
						bidPrice.getPriceList().set(i, tmpPrice);
						buyers.set(i, bidder);
					}					
				}	
				if (buyers.get(i)==null) break; // no one bid the current vm
				if (saleItem.getRealItems().size()>1 ){
									
						Vm vm = saleItem.getRealItems().get(i);
						Host host = buyers.get(i).getHost();
						List<Double> allocatedMipsForVm =  vm.getHost().getAllocatedMipsForVm(vm);
						host.allocatePesForVm(vm, allocatedMipsForVm);
						restoreMap.put(host,vm);
				}
			}
			if (saleItem.getOwner()==null || saleItem.getOwner().accept(bidPrice)){
				soldSuccess = bidPrice.isValid();
				totalPrice = bidPrice.totalPrice();
			}
			soldItem = soldSuccess ? saleItem : null;
		}
		restoreHosts();
		return totalPrice;
	}

	
	private void restoreHosts(){
		Host host = null;
		Vm vm = null;
		
		Iterator<Host> it = restoreMap.keySet().iterator();
		while(it.hasNext()){
			host = it.next();
			vm = restoreMap.get(host);			
			host.deallocatePesForVm(vm);
		}		
	}
	
	private SaleItem selectSaleItem() {
		int priority = -1;
		SaleItem selectedItem = null;
		for (SaleItem item : items){
			if ( priority < item.getPriority()){
				priority = item.getPriority();
				selectedItem = item;
			}
		}
		return selectedItem;
	}

	public SaleItem getSoldItem(){
		return soldItem;
	}
	
	public void addSaleItem(SaleItem item){
		if( item != null)
			items.add(item);
	}
	
	public void setSelectedSaleItem(SaleItem item){
		saleItem = item;
	}
	
	public SaleItem getSelectedSaleItem(){
		return saleItem ;
	}
	
	public void addBidder(Bidder bidder){
		if (bidder != null)
			bidders.add(bidder);
	}

	public List<Bidder> getBuyers() {	
		return buyers;
	}

	

}
