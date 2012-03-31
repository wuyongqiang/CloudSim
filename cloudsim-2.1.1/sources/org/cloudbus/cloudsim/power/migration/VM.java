package org.cloudbus.cloudsim.power.migration;

import java.util.List;


public class VM{
	Integer number;
	String name;
	double requestedMips;
	
	public VM(int vmNumber,String name, double requestedMips){
		this.number = vmNumber;
		this.name = name;
		this.requestedMips = requestedMips;
	}
	
	public VM clone(){
		VM newVm = new VM(number,name,requestedMips);		
		return newVm;
	}
	
	public double getMips(){
		return requestedMips;
	}
	
	public String getName(){
		return name;
	}
	
	public String getVmInfo(){
		String  reslt = String.format("%3d\t%s\t%.2f", this.number,this.name, this.requestedMips);
		return reslt;
	}
}

