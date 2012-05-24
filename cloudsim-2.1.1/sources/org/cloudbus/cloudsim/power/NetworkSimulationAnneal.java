package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.network.FatTreeTopologicalNode;


public class NetworkSimulationAnneal extends SimulationAnneal {
	
	private NetworkCostCalculator networkCalculator = new NetworkCostCalculator();

	public NetworkSimulationAnneal(double[] pCPU, double[] pVM, int[] vAssignOld,
			int oldPMInUse, int newPMInUse, double targetUtilization,
			String[] vmNames) {
		super(pCPU, pVM, vAssignOld, oldPMInUse, newPMInUse, targetUtilization, vmNames);
		migrationCost = 1;
		migrationRate = 0.0;
	}
	
	public void setNetworkRootNode(FatTreeTopologicalNode node){
		networkCalculator.setNetworkRootNode(node);
	}
	
	public void setVmTraffic(int[] trafficMap, int vNum){
		networkCalculator.setVmTraffic(trafficMap, vNum);		
	}
		
	public int getTotalNetworkCost(int[] assignment){
		
		return networkCalculator.getTotalNetworkCost(assignment);
	}
	
	public NetworkSimulationAnneal() {
		super();
		migrationCost = 1;
	}	
	
	public NetworkSimulationAnneal(int timeLimit) {
		super();
		this.annealTimeLimit = timeLimit;
		migrationCost = 1;
	}
	
	public void setAnnealTime(int v){
		this.annealTimeLimit = v;
	}

	@Override
	protected double dievationEnergy() {
		double largestPMEnergy = networkCalculator.getUpperBound();
		double energy = largestPMEnergy * temperature / initialTemperature + 0.1 ;
		return energy;
	}
	
	@Override
	protected double stateEnergy(int[] assignment) {
		double energy = getTotalNetworkCost(assignment);
		
		return energy;
	}

}
