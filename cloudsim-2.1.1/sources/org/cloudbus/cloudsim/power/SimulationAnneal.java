package org.cloudbus.cloudsim.power;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.power.migration.MigrationProblem;
import org.cloudbus.cloudsim.power.migration.MigrationScheduleInt;
import org.cloudbus.cloudsim.power.migration.PM;
import org.cloudbus.cloudsim.power.migration.VM;
import org.cloudbus.cloudsim.util.LogPrint;

public class SimulationAnneal implements MigrationScheduleInt {

	protected static int migrationCost = 100000;
	protected double[] pCPU;
	protected double[] vCPU;
	protected double[] pMEM;
	protected double[] vMEM;
	protected int[] vAssign;
	protected int pmInUse;
	protected int pmInUseOld;
	protected String resultFolder = "cloudSimSA";
	protected int initialTemperature = 200;
	protected int totalIteration;
	protected int coldingRate = 1;
	protected int iTeration = 10000;
	protected Date beginTime;
	protected int[] vAssignBest;
	protected int[] vAssignOldTmp;
	protected int[] vAssignOld;
	protected int vNum, pNum;
	protected int[] vRecentAssignBest;
	protected int temperature;
	protected double sofarBest;
	protected double fftCost;
	protected double[] pUtilization;
	protected double[] pUtilizationMem;
	protected double idleEnergyRatio = 0.7;
	protected double[] ePM;
	protected double targetUtilization = 0.8;
	protected double recentBest;
	protected int migrations;
	protected int largestPMEnergy;
	protected String[] vmNames;

	protected boolean init = false;
	Random random = new java.util.Random();

	protected MigrationProblem originalProblem;
	protected int annealTimeLimit = 200;

	public SimulationAnneal(double[] pCPU, double[] pVM, int[] vAssignOld,
			int oldPMInUse, int newPMInUse, double targetUtilization) {
		initScheduler(pCPU, pVM, vAssignOld, oldPMInUse, newPMInUse,
				targetUtilization, vmNames);
	}

	public SimulationAnneal(double[] pCPU, double[] pVM, int[] vAssignOld,
			int oldPMInUse, int newPMInUse, double targetUtilization,
			String[] vmNames) {
		this(pCPU, pVM, vAssignOld, oldPMInUse, newPMInUse, targetUtilization);
		this.vmNames = vmNames;
	}

	public SimulationAnneal() {
		init = false;
	}

	protected void initPMPower() {
		for (int i = 0; i < pNum; i++) {
			double energyTimes = 1;
			if (pCPU[i] / 1000 < 100) {
				energyTimes = (1 - Math.log10(pCPU[i] / 1000) * 0.4);
			} else {
				throw new RuntimeException("wrong capacity: " + pCPU[i]);
			}

			ePM[i] = (pCPU[i] / 10) * energyTimes; // 1000-> 100*(1-0), 3000->
													// 300 * (1-1/3)
		}
	}

	LogPrint logPrint = new LogPrint(this.getClass().getName());
	

	void print(String message) {
		logPrint.print(message, LogPrint.PrintMode.LogOnly);
	}

	public void anneal() {
		if (pNum <= 1)
			return;
		int scale = vCPU.length;		
		beginTime = new Date();
		print("annealing method 1: random assignment");

		temperature = initialTemperature;
		totalIteration = 0;
		recentBest = Double.MAX_VALUE;

		double dE = dievationEnergy();
		firstFit();

		while (temperature > 0) {
			int staleMateCount = 0;
			dE = dievationEnergy();
			//iTeration = scale;
			//for (int iT = 0; iT < iTeration * scale; iT++) {
			for (int iT = 0; iT < iTeration * scale ; iT++) {
				if (getTicksFromStart() > annealTimeLimit)
					return;
				totalIteration++;
				fluctuate();
				double curEnergy = stateEnergy(vAssign);

				if (curEnergy / migrationCost <= vNum * 0.2)
					break;
				if (hasLowerEnergy(curEnergy)) {
					recentBest = curEnergy;
					saveRecentBestAssign();
					staleMateCount = 0;

					if (recentBest < sofarBest) {
						sofarBest = recentBest;
						saveResult();
					}
				} else {
					revertFluctuate();
					staleMateCount++;
				}

				if (staleMateCount >= scale * scale) {
					// double tmpcost = sofarBest;
					// initAssign();
					// sofarBest = tmpcost;
					fluctuate();

					curEnergy = stateEnergy(vAssign);

					if (curEnergy - recentBest > dE)
						revertFluctuate();
					else {
						recentBest = curEnergy;
						saveRecentBestAssign();
						staleMateCount = 0;
					}
				}
			}

			temperature -= coldingRate;
			if (((int) temperature) % 10 == 0)
				print("temperature=" + temperature);
		}
	}

	protected void saveRecentBestAssign() {
		if (vRecentAssignBest == null) {
			vRecentAssignBest = new int[vNum];
		}
		for (int i = 0; i < vNum; i++) {
			vRecentAssignBest[i] = vAssign[i];
		}

	}

	protected double dievationEnergy() {
		// double largestPMEnergy = 0;
		largestPMEnergy = 0;
		for (int i = 0; i < pNum; i++) {
			largestPMEnergy += ePM[i];
		}
		// double energy = largestPMEnergy * temperature / initialTemperature +
		// 0.1 ;
		// double energy = largestPMEnergy * temperature / (initialTemperature *
		// pNum) ;
		double energy = migrationCost * vNum * temperature
				/ (initialTemperature);
		return energy;
	}

	protected boolean hasLowerEnergy(double curEnergy) {
		// if(curEnergy < Double.MAX_VALUE)
		// print("curEnergy "+curEnergy );
		return (curEnergy < recentBest);
	}

	protected void revertFluctuate() {
		for (int i = 0; i < vNum; i++) {
			vAssign[i] = vAssignOldTmp[i];
		}
	}

	protected double stateEnergy(int[] vAssign2) {
		// this energy comes from the how many migrations are needed
		// the more migration, the more energy
		// migration as constraint or a component of the energy?
		// the number of PMs in use cannot be changed
		// if the machine number increase, then the energy soars
		double energyCost = getEnergyCost(vAssign2);
		migrations = getMigrationCost(vAssign2);

		if (energyCost < Double.MAX_VALUE)
			energyCost = migrations * migrationCost;
		return energyCost;
	}

	protected int getMigrationCost(int[] vAssign2) {
		int migrations = 0;
		for (int i = 0; i < this.vNum; i++) {
			if (vAssign2[i] != this.vAssignOld[i])
				migrations++;
		}
		return migrations;
	}

	protected double getEnergyCost(int[] assignment) {
		double energy = 0;
		double[] uPM = new double[pNum];
		double[] usedMEM = new double[pNum];
		for (int i = 0; i < assignment.length; i++) {
			int iPM = assignment[i];
			if (iPM >= pNum || iPM < 0) {
				print("illegal assignment ");
				return Double.MAX_VALUE;
			}

			uPM[iPM] += vCPU[i] / pCPU[iPM];
			usedMEM[iPM] += vMEM[i]/ pMEM[iPM];
		}

		for (int i = 0; i < pNum; i++) {
			if (uPM[i] > targetUtilization + 0.1
			 || usedMEM[i]>1
			) {
				return Double.MAX_VALUE;
			}
			double energyPM = 0;
			if (uPM[i] > 0.001)
				energyPM = uPM[i] * (1 - idleEnergyRatio) * ePM[i]
						+ idleEnergyRatio * ePM[i];
			energy += energyPM;

			saveUtilization(uPM, usedMEM);
		}

		return energy;
	}

	protected void saveUtilization(double[] uPM, double[] mPM) {

		if (pUtilization == null) {
			pUtilization = new double[pNum];
		}
		for (int i = 0; i < uPM.length; i++) {
			pUtilization[i] = uPM[i];
		}

		if (pUtilizationMem == null) {
			pUtilizationMem = new double[pNum];
		}

		for (int i = 0; i < mPM.length; i++) {
			pUtilizationMem[i] = mPM[i];
		}
	}

	public int[] getAssignment() {
		if (migrations > 0)
			print("migrations = " + migrations);
		String s = "pmInUseOld=" + pmInUseOld + " pmInUse=" + pmInUse
				+ " final vm assignment:";
		for (int i = 0; i < vNum; i++) {
			s += String.format("%2d,", vAssignBest[i]);
		}
		System.out.println(s);
		print(s);
		showMigrationRoute();
		return vAssignBest;
	}

	protected void showMigrationRoute() {
		String s = "";
		for (int i = 0; i < vNum; i++) {
			if (vAssignOld[i] != vAssignBest[i]) {
				if (vmNames != null && i < vmNames.length && vmNames[i] != null)
					s += vmNames[i];
				else
					s += "vm[" + i + "]";
				s += " migrate from " + vAssignOld[i] + " to " + vAssignBest[i]
						+ "\n";
			}
		}
		print(s);
	}

	protected double getTicksFromStart() {
		Date now = new Date();
		double tick = 0;
		if (beginTime != null)
			tick = now.getTime() - beginTime.getTime();
		else
			beginTime = now;
		tick = tick / 1000;
		return tick;
	}

	protected void saveBestAssign() {
		print("save a best assign " + this.totalIteration);

		if (vAssignBest == null) {
			vAssignBest = new int[vNum];
		}

		String s = "";
		if (vRecentAssignBest != null) {
			for (int i = 0; i < vNum; i++) {
				vAssignBest[i] = vRecentAssignBest[i];
				s += vAssignBest[i] + ",";
			}
		}

		print(s);
	}

	protected Random getRandom() {
		long tick = (new Date()).getTime();
		Random result = new Random();
		result.setSeed(tick);
		return result;
	}
	
	protected void fluctuate() {

		//Random random = getRandom();
		if (random.nextInt() % 2 == 0) {
			swap();
			return;
		}

		int vNum = vAssign.length;
		for (int i = 0; i < vNum; i++) {
			vAssignOldTmp[i] = vAssign[i];
		}
		int rnd = Math.abs(random.nextInt()) % 3;
		for (int k = 0; k < rnd; k++) {
			// pick up the vm
			int vm = Math.abs(random.nextInt()) % vNum;

			int pm = Math.abs(random.nextInt()) % pNum;
			vAssign[vm] = pm;
		}
	}

	protected void swap() {

		for (int i = 0; i < vNum; i++) {
			vAssignOldTmp[i] = vAssign[i];
		}

		int oldPm1;
		int oldPm2;

		if (vNum <= 1 || pNum <= 1)
			return; // no need to swap

		int loopCount = 1;
		do {
			Random random = getRandom();
			// pick up the vm
			int vm1 = Math.abs(random.nextInt()) % vNum;
			// pick up the pm
			int vm2 = Math.abs(random.nextInt()) % vNum;
			
			
			if (vm1 < 0 || vm2 < 0 || vm1 >= vNum || vm2 >= vNum){
				oldPm1 = oldPm2 = 0;
				continue; // weird, it could happen
			}
				
			oldPm1 = vAssignOldTmp[vm1];
			oldPm2 = vAssignOldTmp[vm2];

			vAssign[vm1] = oldPm2;
			vAssign[vm2] = oldPm1;
			loopCount++;
		} while (oldPm1 == oldPm2 && loopCount < vNum * 100);	
	}

	protected void firstFit() {

		for (int i = 0; i < vNum; i++) {
			vAssignOldTmp[i] = vAssign[i];
		}

		double[] pLeftCPU = new double[pNum];
		double[] pLeftMEM = new double[pNum];
		for (int i = 0; i < pNum; i++) {
			pLeftCPU[i] = pCPU[i] * targetUtilization;
			pLeftMEM[i] = pMEM[i];
		}
		for (int i = 0; i < vNum; i++) {
			for (int j = 0; j < pNum; j++) {
				if (pLeftCPU[j] > vCPU[i]
				&& pLeftMEM[j]>vMEM[i]
				) {
					vAssign[i] = j;
					pLeftCPU[j] -= vCPU[i];
					pLeftMEM[j] -= vMEM[i];
					break;
				}
			}
		}

		sofarBest = stateEnergy(vAssignOldTmp);
		recentBest = stateEnergy(vAssign);

		if (recentBest < sofarBest)
			sofarBest = recentBest;
		else
			revertFluctuate();

		saveRecentBestAssign();

		fftCost = sofarBest;
		saveResult();

		//sofarBest= Double.MAX_VALUE;
	}

	protected void saveResult() {

		double tick = getTicksFromStart();
		String strResult = "";
		for (int i = 0; i < vNum; i++) {
			strResult = strResult + vAssign[i] + ",";
		}

		strResult = (String.format("%.1f", tick)
				+ "\t"
				+ totalIteration
				+ "\t "
				+ String.format("%.2f", sofarBest)
				+ "\t"
				+ String.format("%.2f%%", (fftCost - sofarBest) * 100 / fftCost)
				+ " \nassignment " + strResult);

		saveBestAssign();

		strResult += "\n ";
		if (pUtilization != null) {
			for (int i = 0; i < pNum; i++) {
				strResult = strResult
						+ pCPU[i]
						+ "-"
						+ String.format("%.2f%%|%.2f%%,",
								pUtilization[i] * 100, pUtilizationMem[i] * 100);
			}
		}

		for (int i = 0; i < vNum; i++) {			
			strResult = strResult + String.format("%.0f", vCPU[i]) + ",";
		}
		strResult += "\n";

		for (int i = 0; i < pNum; i++) {			
			strResult = strResult + String.format("%.2f", pCPU[i]) + ",";

		}
		strResult += "\n";
		print(strResult);
	}

	@Override
	public void scheduleMigration() {
		if (!init) {
			throw new RuntimeException("Simuliation Anealing not initialized!");
		}
		anneal();
	}

	@Override
	public void initScheduler(double[] pCPU, double[] pVM, int[] vAssignOld,
			int oldPMInUse, int newPMInUse, double targetUtilization,
			String[] vmNames) {

		this.pCPU = pCPU;
		this.vCPU = pVM;
		this.pmInUse = newPMInUse;
		this.pmInUseOld = oldPMInUse;
		this.vAssignOld = vAssignOld;

		// pNum = pCPU.length;
		pNum = pmInUse;
		if (pNum > pCPU.length)
			pNum = pCPU.length;
		vNum = pVM.length;
		this.vAssign = new int[vNum];
		this.vAssignBest = new int[vNum];
		this.vAssignOldTmp = new int[vNum];
		this.vRecentAssignBest = new int[vNum];
		this.pUtilization = new double[pNum];
		this.pUtilizationMem = new double[pNum];
		this.ePM = new double[pNum];

		this.targetUtilization = targetUtilization;
		beginTime = new Date();
		for (int i = 0; i < vNum; i++) {
			vAssign[i] = vAssignOld[i];
		}

		initPMPower();
		initMemory();

		String s = "pmInUseOld=" + pmInUseOld + " pmInUse=" + pmInUse
				+ " initial vm assignment:";
		for (int i = 0; i < vNum; i++) {
			s += String.format("%2d,", vAssignOld[i]);
		}
		print(s);

		this.vmNames = vmNames;
		init = true;
	}
	

	protected void initMemory() {
		pMEM = new double[pNum];
		vMEM = new double[vNum];
		
		for (int i = 0; i < pNum; i++) {
			pMEM[i] = pCPU[i];
		}
		
		for (int i = 0; i < vNum; i++) {
			vMEM[i] = 0;
		}
	}
	
	public void initMemory(double[] paramMem) {
		if (paramMem.length!=vNum) 
			throw new RuntimeException("memory usage array is not correct, expected "+vNum+" actual "+ paramMem.length);
		for (int i = 0; i < vNum; i++) {
			vMEM[i] = paramMem[i];
		}
	}

	@Override
	public void initScheduler(MigrationProblem problem) {
		originalProblem = problem;
		pNum = problem.getPmCount();
		vNum = problem.getTotalVmCount();
		double[] pCPU = new double[pNum];
		double[] pVM = new double[vNum];
		double[] mVM = new double[vNum];
		int[] vAssignOld = new int[vNum];
		
		int oldPMInUse = 0;
		int newPMInUse = pNum;
		double targetUtilization = problem.getPM(0).getTargetUtilization();
		String[] vmNames = new String[vNum];
		
		Map<String,Integer> vmPm = new HashMap<String, Integer>();
		int iVM = 0;
		for (int i = 0; i < pNum; i++) {
			PM pm = problem.getPM(i);
			pCPU[i] = pm.getCPU();
			if (pm.getUtilizationCPU() >= 0.0000001)
				oldPMInUse++;
			for (int j = pm.getVmCount()-1; j >=0 ; j--) {
				VM vm = pm.getVm(j);
				vmPm.put(vm.getName(), i);
				pm.removeVm(vm);
				problem.addNonAssignedVm(vm);
			}
		}

		for (int i = 0; i < problem.getNonAssignedVmCount(); i++) {
			VM vm = problem.getNonAssignedVm(iVM);
			pVM[iVM] = vm.getMips();
			mVM[iVM] = vm.getMem();
			if (vmPm.get(vm.getName())==null)
				vAssignOld[iVM] = -1;
			else
				vAssignOld[iVM] = vmPm.get(vm.getName()).intValue();
			vmNames[iVM] = vm.getName();
			iVM++;
		}
		
		initScheduler(pCPU, pVM, vAssignOld, oldPMInUse, newPMInUse,
				targetUtilization, vmNames);
		initMemory(mVM);
	}

	@Override
	public MigrationProblem getSolution() {
		if (originalProblem == null)
			throw new RuntimeException(
					"original problem is not passed as an instance of MigrationProblem");

		MigrationProblem solution = originalProblem.clone();
		solution.moveVmsToUnAssignedList();

		for (int i = 0; i < vNum; i++) {
			VM vm = solution.getVmByName(vmNames[i]);
			int iPM = vAssignBest[i];
			solution.getPM(iPM).addVm(vm);
			solution.removeNonAssignedVm(vm);
		}

		return solution;
	}

}
