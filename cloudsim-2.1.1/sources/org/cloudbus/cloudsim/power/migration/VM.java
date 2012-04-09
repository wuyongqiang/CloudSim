package org.cloudbus.cloudsim.power.migration;

import java.util.List;


public class VM{
	Integer number;
	String name;
	double requestedMips;
	double requestedMem;
	
	public VM(int vmNumber,String name, double requestedMips){
		this.number = vmNumber;
		this.name = name;
		this.requestedMips = requestedMips;
	}
	
	public VM clone(){
		VM newVm = new VM(number,name,requestedMips);	
		newVm.setMem(this.requestedMem);
		return newVm;
	}
	
	public double getMips(){
		return requestedMips;
	}
	
	public double getMem(){
		return requestedMem;
	}
	
	public void setMem(double v){
		this.requestedMem = v;
	}
	
	public String getName(){
		return name;
	}
	
	public String getVmInfo(){
		String  reslt = String.format("%3d\t%s\t%.2f\t%.2f", this.number,this.name, this.requestedMips, this.requestedMem);
		return reslt;
	}
}

