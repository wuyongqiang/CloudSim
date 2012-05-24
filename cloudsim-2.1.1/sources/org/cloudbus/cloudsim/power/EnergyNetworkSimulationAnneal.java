package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.network.FatTreeTopologicalNode;


public class EnergyNetworkSimulationAnneal extends SimulationAnneal {
	
	private FatTreeTopologicalNode networkRootNode =null;
	private int[] trafficMap;

	public EnergyNetworkSimulationAnneal(double[] pCPU, double[] pVM, int[] vAssignOld,
			int oldPMInUse, int newPMInUse, double targetUtilization,
			String[] vmNames) {
		super(pCPU, pVM, vAssignOld, oldPMInUse, newPMInUse, targetUtilization, vmNames);
	}
	
	public void setNetworkRootNode(FatTreeTopologicalNode node){
		this.networkRootNode = node;
	}
	
	public void setVmTraffic(int[] trafficMap){
		this.trafficMap = trafficMap;		
	}
	
	private int networkCost(int vm1, int vm2,int[] assignment){	
		int pm1 = assignment[vm1];
		int pm2 = assignment[vm2];
		
		int traffic = getTaffic(vm1,vm2);
		int unitCost = 0;
		if (traffic>0) unitCost = networkUnitCost(pm1,pm2);
		return unitCost * traffic;
	}
	
	private int getTaffic(int vm1, int vm2) {					
			int pos = vm1 * vNum + vm2;
			return trafficMap[pos];		
	}
	
	public int getTotalNetworkCost(int[] assignment){
		int total = 0;
		
		for(int i=0;i<vNum;i++){			
			for(int j=0;j<vNum;j++){
				if (i==j) continue;
				total += networkCost(i,j,assignment);
			}
		}		
		
		return total;
	}

	private int networkUnitCost(int pmNumber1, int pmNumber2){
		String pm1 = "pm"+pmNumber1;
		String pm2 = "pm"+pmNumber2;
		if (pm1.equals(pm2)) return 0;
		FatTreeTopologicalNode node1 = networkRootNode.findNodeByLabel(pm1);
		FatTreeTopologicalNode node2 = networkRootNode.findNodeByLabel(pm2);
		List<FatTreeTopologicalNode> list = node1.getNodesFrom(node2);
		if (list.size()==1)
			return 0;
		else if (list.size()==3)
			return 1;
		else if (list.size()==5)
			return 2;
		else if (list.size()==7)
			return 4;
		else{
			throw new RuntimeException("impossible network link " + list.size());
		}
	}
	
	public EnergyNetworkSimulationAnneal() {
		super();
		migrationCost = 1;
	}	
	
	public EnergyNetworkSimulationAnneal(int timeLimit) {
		super();
		this.annealTimeLimit = timeLimit;
		migrationCost = 1;
	}
	
	public void setAnnealTime(int v){
		this.annealTimeLimit = v;
	}

	@Override
	protected double dievationEnergy() {
		double largestPMEnergy = 0;
		for (int i = 0;i< pNum; i++){			
			largestPMEnergy +=  ePM[i];			
		}
		double energy = largestPMEnergy * temperature / initialTemperature + 0.1 ;
		return energy;
	}
	
	@Override
	protected double stateEnergy(int[] assignment) {
		double energy = 0;
		double[] uPM = new double[pNum];
		double[] usedMEM = new double[pNum];
		for (int i=0;i<assignment.length;i++)
		{
			int iPM = assignment[i];
			if ( iPM >= pNum || iPM <0 ){
				print("illegal assignment "+vmNames[i] +" to " + iPM);
				return Double.MAX_VALUE;
			}
			
			uPM[iPM] += vCPU[i] / pCPU[iPM];
			usedMEM[iPM] += vMEM[i]/ pMEM[iPM];
		}
		
		for (int i = 0;i< pNum; i++){
			if (uPM[i]>1 || usedMEM[i]>1 ){
				return Double.MAX_VALUE;
			}
			double energyPM = 0;
			if (uPM[i] > 0.001)
				energyPM = uPM[i] * (1- idleEnergyRatio ) * ePM[i] + idleEnergyRatio * ePM[i];
			energy += energyPM;
			
			saveUtilization(uPM,usedMEM);
		}
		
		return energy;
	}

}
