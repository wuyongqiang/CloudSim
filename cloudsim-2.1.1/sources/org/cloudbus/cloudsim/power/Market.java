package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;

public class Market {
	
	private List<Bidder> bidders = new ArrayList<Bidder>();
	private List<SaleItem> items = new ArrayList<SaleItem>();
	
	private SaleItem soldItem = null;
	private Bidder buyer = null;
	
	public boolean bid(){
		Boolean soldSuccess = false;
		SaleItem saleItem = selectSaleItem(); 
		if (saleItem!=null){
			int price = 0;
			for (Bidder bidder : bidders){
				int curBidPrice = bidder.bidPrice(saleItem);
				if (price < curBidPrice){				
					price = curBidPrice;
					buyer = bidder;
				}
			}
			if (saleItem.getOwner()==null || saleItem.getOwner().accept(price))
				soldSuccess = (price > 0);
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

	public Bidder getBuyer() {	
		return buyer;
	}
	
	
	
}
