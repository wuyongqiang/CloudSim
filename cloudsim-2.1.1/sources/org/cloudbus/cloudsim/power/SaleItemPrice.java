package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

public class SaleItemPrice {
		
	private List<Integer> priceList = new ArrayList<Integer>();
	
	public SaleItemPrice(){
		
	}
	
	public boolean isValid(){
		if (priceList.size()==0)
			return false;
		
		for(int i=0;i<priceList.size();i++){
			if (priceList.get(i)==null || priceList.get(i).intValue()==0)
				return false;
		}
		
		return true;
	}
	
	public int totalPrice(){
		int tValue = 0;
		for(int i=0;i<priceList.size();i++){
			if (priceList.get(i)!=null && priceList.get(i).intValue()!=0)
				tValue += priceList.get(i).intValue();
		}
		return tValue;
	}

	public int getPrice() {
		if (priceList.size()>0)
			return priceList.get(0);
		else
			return 0;
	}
	
	public void addPrice(int price) {
		priceList.add(price);
	}

	public List<Integer> getPriceList() {
		return priceList;
	}

}
