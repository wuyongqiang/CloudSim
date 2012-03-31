package org.cloudbus.cloudsim.power.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.lists.VmList;

public class PM{
	Integer number;
    double cpu;
	double ePM;
	private List<VM> assignedVmList;
	private Map<Integer,VM> assignedVmMap;
	
	private double idleEnergyRatio = 0.7;
	private double targetUtilization = 0.8;
	
	public PM(int pmNumber,double cpu, double maxEnergy){
		this();		
		number = pmNumber;
		this.cpu = cpu;
		ePM = maxEnergy;
		
	}
	
	public void setTargetUtilization(double v){
		this.targetUtilization = v;
	}
	
	public PM(){		
		assignedVmList = new ArrayList<VM>();
		assignedVmMap = new HashMap<Integer, VM>();
	}
	
	public double getUtilizationCPU(){
		double utilization;
		double usedCpu = 0;
		for (int i= 0; i<assignedVmList.size();i++){
			usedCpu += assignedVmList.get(i).getMips();
		}
		utilization = usedCpu / this.cpu;
		return utilization;
	}
	
	public double getUtilizationMem(){
		return 0;
	}
	
	public boolean canAccept(VM vm){
		double finalUitlization = getUtilizationCPU();
		finalUitlization += vm.getMips() / this.cpu;		
		return finalUitlization <= this.targetUtilization;
	}
	
	public double getEnergy(){
		return ePM * idleEnergyRatio + (1-idleEnergyRatio)* ePM * getUtilizationCPU();
	}
	
	public int getVmCount(){
		return assignedVmList.size();
	}
	
	public VM getVm(int index){
		return assignedVmList.get(index);
	}
	
	public VM getVmByNumer(Integer vmNumber){
		return assignedVmMap.get(vmNumber);
	}
	
	public boolean hasVM(VM vm){
		return assignedVmMap.containsKey(vm.number);
	}
	
	public void addVm(VM vm){
		if (!assignedVmMap.containsKey(vm.number)){
			assignedVmMap.put(vm.number, vm);
			int i =0;
			while (i<assignedVmList.size()){
				if (assignedVmList.get(i).requestedMips < vm.requestedMips)
					break;
				i++;
			}
			assignedVmList.add(i, vm);
		}
	}
	
	public void removeVm(VM vm){
		assignedVmList.remove(vm);
		assignedVmMap.remove(vm.number);
	}
	
	
	public PM clone(){
		PM newPm = new PM();
		newPm.cpu = cpu;
		newPm.number = number;
		newPm.ePM = ePM;
		
		for (int i=0;i<assignedVmList.size();i++){			
			newPm.addVm(getVm(i).clone());
		}
		return newPm;
	}
	
	public String getPmInfo(){
		String reslt = "";
		
		reslt = String.format("%3d\t%.2f\t%.2f%%", this.number,this.cpu,this.getUtilizationCPU()*100);
		for (VM vm : assignedVmList){
			reslt += "\r\n\t" + vm.getVmInfo() ;
		}
		return reslt;
	}
}