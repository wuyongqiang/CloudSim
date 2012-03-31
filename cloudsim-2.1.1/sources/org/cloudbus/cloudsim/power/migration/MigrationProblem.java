package org.cloudbus.cloudsim.power.migration;

import java.util.ArrayList;
import java.util.List;

public class MigrationProblem {
	
	List<VM> nonAssingedVmList;
	List<PM> pmList;
	
	public MigrationProblem(){
		nonAssingedVmList = new ArrayList<VM>();
		pmList = new ArrayList<PM>();
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
	
	public void addPM(PM pm){
		this.pmList.add(pm);
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
	
	private int getVmCount(){
		int count =0;
		for(PM pm : pmList){
			count += pm.getVmCount();
		}
		return count;
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
		int[] vmAssign = new int[getVmCount()+nonAssingedVmList.size()];
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
}
