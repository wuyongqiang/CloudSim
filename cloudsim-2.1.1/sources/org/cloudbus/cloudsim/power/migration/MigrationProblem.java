package org.cloudbus.cloudsim.power.migration;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.theories.internal.Assignments;

public class MigrationProblem {
	
	List<VM> nonAssingedVmList;
	List<PM> pmList;
	String name;
	
	public MigrationProblem(){
		nonAssingedVmList = new ArrayList<VM>();
		pmList = new ArrayList<PM>();
		name = "problem";
	}
	
	public String getName()	{
		return name;
	}
	
	public void setName(String v){
		name = v;
	}
	
	public VM getNonAssignedVm(int index){
		return nonAssingedVmList.get(index);
	}
	
	public int getNonAssignedVmCount(){
		return nonAssingedVmList.size();
	}
	
	public void addNonAssignedVm(VM vm) {

		int i = 0;
		while (i < nonAssingedVmList.size()) {
			if (nonAssingedVmList.get(i).requestedMips < vm.requestedMips)
				break;
			i++;
		}
		nonAssingedVmList.add(i, vm);
	}
	
	public void removeNonAssignedVm(VM vm) {
		nonAssingedVmList.remove(vm);
	}
	
	public void addPM(PM pm){
		int i = 0;
		while (i < pmList.size()) {
			if (pmList.get(i).number > pm.number)
				break;
			i++;
		}
		pmList.add(i, pm);		
	}
	
	public void removePM(PM pm){
		this.pmList.remove(pm);
	}
	
	public int getPmCount(){
		return this.pmList.size();
	}
	
	public PM getPM(int index){
		return this.pmList.get(index);
	}
	
	public double getTotalEnergy(){
		double tEnergy = 0;
		for(PM pm : pmList){
			tEnergy += pm.getEnergy();
		}
		return tEnergy;
	}
	
	public MigrationProblem clone(){
		MigrationProblem problem = new MigrationProblem();
		for (int i= 0; i<nonAssingedVmList.size();i++){
			VM vm = this.nonAssingedVmList.get(i).clone();
			problem.nonAssingedVmList.add(vm);
		}
		
		for (int i= 0; i<pmList.size();i++){
			PM pm = this.pmList.get(i).clone();
			problem.pmList.add(pm);
		}
		return problem;
	}
	
	public void copy(MigrationProblem problem){
		nonAssingedVmList.clear();
		pmList.clear();
		for (int i= 0; i<problem.nonAssingedVmList.size();i++){
			VM vm = problem.nonAssingedVmList.get(i).clone();
			this.nonAssingedVmList.add(vm);
		}
		
		for (int i= 0; i<problem.pmList.size();i++){
			PM pm = problem.pmList.get(i).clone();
			this.pmList.add(pm);
		}
	}
	
	public int getAssignedVmCount(){
		int count =0;
		for(PM pm : pmList){
			count += pm.getVmCount();
		}
		return count;
	}
	
	public int getTotalVmCount(){
		return getAssignedVmCount() + getNonAssignedVmCount();
	}
	
	public VM getVmByName(String name){		
		for (int i=0;i<nonAssingedVmList.size();i++){
			VM vm = nonAssingedVmList.get(i);
			if (vm.getName().equals(name)) return vm;
		}
		
		for(int i=0;i<pmList.size();i++){
			PM pm = pmList.get(i);
			for(int j=0;j<pm.getVmCount();j++){
				VM vm = pm.getVm(j);
				if (vm.getName().equals(name)) return vm;
			}			
		}
		
		return null;
	}
	
	public int getMigrationCount(MigrationProblem problem){
		int migrations = 0;
		int[] myAssignment = getAssignment();
		int[] oldAssignment = problem.getAssignment();
		
		for (int i=0;i<myAssignment.length;i++){
			if (myAssignment[i]!=oldAssignment[i])
				migrations++;
		}
		
		return migrations;
	}
	
	public int[] getAssignment(){
		int[] vmAssign = new int[getAssignedVmCount()+nonAssingedVmList.size()];
		for (int i=0;i<nonAssingedVmList.size();i++){
			VM vm = nonAssingedVmList.get(i);
			vmAssign[vm.number.intValue()] = -1;
		}
		
		for(int i=0;i<pmList.size();i++){
			PM pm = pmList.get(i);
			for(int j=0;j<pm.getVmCount();j++){
				VM vm = pm.getVm(j);
				vmAssign[vm.number.intValue()] = pm.number;
			}			
		}
		return vmAssign;
	}
	
	public String getProblemInfo(){
		String reslt = "PM list";
		double totalEnergy = 0;
		for (int i = 0;i < pmList.size();i++){
			reslt += "\r\n" + pmList.get(i).getPmInfo();
			totalEnergy += pmList.get(i).getEnergy();
		}
		
		reslt += "\r\n" + "nonAssingedVmList:";
		for (int i = 0;i < nonAssingedVmList.size();i++){
			
			reslt += "\r\n" + nonAssingedVmList.get(i).getVmInfo();
		}
		reslt += "\r\n" + "totalEnergy=" + String.format("%.2f", totalEnergy);
		return reslt;
	}

	public void clear() {
		nonAssingedVmList.clear();
		pmList.clear();		
	}
	
	public void pickInfeasibleVms(){
		for(int i=0;i<pmList.size();i++){
			PM pm = pmList.get(i);
		
			while(pm.getUtilizationCPU()>pm.getTargetUtilization()){			
				VM vm = pm.getSmallestVm();
				pm.removeVm(vm);
				nonAssingedVmList.add(vm);
			}
		}
	}
	
	public void moveVmsToUnAssignedList(){
		for (int i=0;i<getPmCount();i++){
			PM pm = getPM(i);
			for (int j=0;j<pm.getVmCount();j++){
				VM vm = getPM(i).getVm(j);
				addNonAssignedVm(vm);
			}
		}
	}
	
	public List<PM> sortPmByUtilization(){
		List<PM> sortedList = new ArrayList<PM>();
		for (int k = 0; k<pmList.size();k++){
			PM pm = pmList.get(k);
			int i = 0;
			while (i < sortedList.size()) {
				if (sortedList.get(i).getUtilizationCPU() < pm.getUtilizationCPU())
					break;
				i++;
			}
			sortedList.add(i, pm);
		}
		return sortedList;
	}
}
