package org.cloudbus.cloudsim.power;

public interface Seller {
	public boolean accept(int price);
	public SaleItem provisionSaleItem();
	public SaleItem getSaleItem();
}
