package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

public class SaleItem {

	private Vm vm;
	private int value;
	private int priority;
	
	private Seller owner;
	
	public SaleItem(Vm vm, Seller owner){
		this.vm = vm;
	}
	
	public Vm getRealItem(){
		return vm;
	}

	public int getValue() {
		return (int)vm.getMips();
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Seller getOwner() {
		return owner;
	}

	public void setOwner(Seller owner) {
		this.owner = owner;
	}

}
