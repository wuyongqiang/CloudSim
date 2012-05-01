package org.cloudbus.cloudsim.power;


public class EnergySimulationAnneal extends SimulationAnneal {
	
	public EnergySimulationAnneal(double[] pCPU, double[] pVM, int[] vAssignOld,
			int oldPMInUse, int newPMInUse, double targetUtilization,
			String[] vmNames) {
		super(pCPU, pVM, vAssignOld, oldPMInUse, newPMInUse, targetUtilization, vmNames);
	}
	
	public EnergySimulationAnneal() {
		super();
		migrationCost = 1;
	}	
	
	public EnergySimulationAnneal(int timeLimit) {
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
