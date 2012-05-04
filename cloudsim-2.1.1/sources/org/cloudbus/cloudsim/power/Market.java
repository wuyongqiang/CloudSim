package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;

public class Market {
	
	private List<Bidder> bidders = new ArrayList<Bidder>();
	private List<SaleItem> items = new ArrayList<SaleItem>();
	
	private SaleItem soldItem = null;
	private List<Bidder> buyers = new ArrayList<Bidder>();
	
	public boolean bid(){
		Boolean soldSuccess = false;
		SaleItem saleItem = selectSaleItem(); 
		if (saleItem!=null){
			
			//int vmCount = saleItem.getRealItems().size();
			//ArrayList<Integer> priceList = new ArrayList<Integer>(vmCount);
			SaleItemPrice bidPrice = new SaleItemPrice();
			for (int i=0;i<saleItem.getRealItems().size();i++){
				bidPrice.addPrice(0);
				buyers.add(null);
			}
				
			
			for (Bidder bidder : bidders){				
				SaleItemPrice curBidPrice = bidder.bidPrice(saleItem);
				
				for(int i=0;i< curBidPrice.getPriceList().size();i++){
					
					if (bidPrice.getPriceList().get(i) < curBidPrice.getPriceList().get(i)){				
						Integer tmpPrice = curBidPrice.getPriceList().get(i);
						bidPrice.getPriceList().set(i, tmpPrice);						
						buyers.set(i, bidder);
					}
				}
			}
			if (saleItem.getOwner()==null || saleItem.getOwner().accept(bidPrice))
				soldSuccess = bidPrice.isValid();
			soldItem = soldSuccess ? saleItem : null;
		}
		return soldSuccess;
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
		
	
	public void addBidder(Bidder bidder){
		if (bidder != null)
			bidders.add(bidder);
	}

	public List<Bidder> getBuyers() {	
		return buyers;
	}
	
	
	
}
