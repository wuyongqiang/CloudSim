package org.cloudbus.cloudsim.power;

import java.util.List;

public interface Seller {	
	public SaleItem provisionSaleItem();
	public SaleItem getSaleItem();
	public boolean accept(SaleItemPrice bidPrice);
}
