package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Vm;

public class SaleItem {

	private Vm vm;
	private List<Vm> vms;
	private int value;
	private int priority;
	
	private Seller owner;	
	
	public SaleItem(Vm vm, Seller owner){
		this.vm = vm;
	}
	
	public SaleItem(List<Vm> vms, Seller owner){
		this.vms = vms;
	}
	
	public Vm getRealItem(){
		if (vm!=null)
			return vm;
		if (vms!=null && vms.size()>0)
			return vms.get(0);
		else
			return null;
	}

	public List<Vm> getRealItems(){
		if (vms!=null)
			return vms;
		vms = new ArrayList<Vm>();
		vms.add(vm);
		return vms;
	}
	
	public int getValue() {
		if (vm!=null)
			return (int)vm.getMips();
		else{
			int v = 0;
			for(Vm tmpVm : vms){
				v += (int)tmpVm.getMips();
			}
			return v;
		}
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
