package org.cloudbus.cloudsim.power.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.util.LogPrint;

public class MigrationSchedulerFFD implements MigrationSchedule {
	

	private List<VM> vms;
	private List<PM> pms;
	private static final int migrationCost = 100000;
	private double[] pCPU;
	private double[] vCPU;
	private int[] vAssign;
	private int pmInUse;
	private int pmInUseOld;
	private String resultFolder="cloudSimSA";
	private int initialTemperature = 200;
	private int totalIteration;
	private int coldingRate = 1;
	private int iTeration = 50000;
	private Date beginTime;
	private int[] vAssignBest;
	private int[] vAssignOldTmp;
	private int[] vAssignOld;
	private int vNum, pNum;
	private int[] vRecentAssignBest;
	private int temperature;
	private double sofarBest;
	private double fftCost;
	private double[] pUtilization;
	private double[] pUtilizationMem;
	private double idleEnergyRatio = 0.7;
	private double[] ePM;
	private double targetUtilization = 0.8;
	private double recentBest;
	private int migrations;
	private int largestPMEnergy;
	private  String[] vmNames;
	
	private boolean init = false;

	public MigrationSchedulerFFD(){
		;
	}
	
	private void realInit(double[] pCPU, double[] pVM, int[] vAssignOld, int oldPMInUse, int newPMInUse, double targetUtilization) {
		this.pCPU = pCPU;
		this.vCPU = pVM;
		this.pmInUse = newPMInUse;
		this.pmInUseOld = oldPMInUse;
		this.vAssignOld = vAssignOld;
		
		pNum = pmInUse >pCPU.length ? pCPU.length : pmInUse; 		
		vNum = pVM.length;
		this.targetUtilization = targetUtilization;
		
		initVectors();
		
		initVMs();
 
		initPMPower();
		
		printProblem();
		
		beginTime = new Date();
		init = true;
	}

	private void initVMs() {
		vms = new ArrayList<VM>();
		for (int i= 0;i<vNum;i++){
			VM vm = new VM(i,vmNames[i],vCPU[i]);			
			vms.add(vm);
		}
	}
		

	private void initVectors() {
		this.vAssign = new int[vNum];
		this.vAssignBest = new int[vNum];
		this.vAssignOldTmp = new int[vNum];
		this.vRecentAssignBest = new int[vNum];
		this.pUtilization = new double[pNum];
		this.pUtilizationMem = new double[pNum];
		this.ePM = new double[pNum];
		
		for (int i=0;i<vNum;i++){
			vAssign[i] = vAssignOld[i];
		}
		
		pms = new ArrayList<PM>();
		for (int i=0;i<pNum;i++){
			PM pm = new PM();
			pm.number = i;
			pms.add(pm);
		}
	}
	
	private void printProblem() {
		String  s = "pmInUseOld=" + pmInUseOld + " pmInUse=" + pmInUse + " initial vm assignment:";
		for (int i=0; i< vNum; i++){
			s += String.format("%2d,", vAssignOld[i]) ;
		}
		printConsole(s);
	}
	
	public void initScheduler(double[] pCPU, double[] pVM, int[] vAssignOld, int oldPMInUse, int newPMInUse, double targetUtilization, String[] vmNames) {
		realInit(pCPU,pVM, vAssignOld,oldPMInUse, newPMInUse,targetUtilization);
		this.vmNames = vmNames;
	}
	
	private void initPMPower() {
		for (int i=0;i<pNum;i++){
			ePM[i] = pCPU[i]/10;
		}		
	}
	
	
	LogPrint logPrint = new LogPrint(this.getClass().getName());
	void print(String message){
		logPrint.print(message,LogPrint.PrintMode.PrintLog);
	}
	
	void printConsole(String message){
		logPrint.print(message,LogPrint.PrintMode.PrintOnly);
	}

	

	@Override
	public int[] getAssignment() {
		
		showFinalAssignment();
		showMigrationRoute();
		return vAssignBest;
	}

	private void showFinalAssignment() {
		String s = "pmInUseOld=" + pmInUseOld + " pmInUse=" + pmInUse
				+ " final vm assignment:";
		for (int i = 0; i < vNum; i++) {
			s += String.format("%2d,", vAssignBest[i]);
		}
		print(s);
	}
	
	
	private void showMigrationRoute(){
		String s = "";
		int migrations = 0;
		
		for (int i=0;i<vNum;i++){
			if (vAssignOld[i]!=vAssignBest[i]){
				if (vmNames!=null && i<vmNames.length && vmNames[i]!=null)
					s += vmNames[i];
				else
					s += "vm["+i+"]" ;
				s += " migrate from " + vAssignOld[i] +" to "+ vAssignBest[i] + "\n";
				migrations++;
			}
		}	
		s += migrations + "\n";
		print(s);
	}
	
	@Override
	public void scheduleMigration() {
		if (!init){
			print("the problem has not been initialized");
			return;
		}
		
		for (int i = 1;i<10;i++){
			if (migratePartially(i*10)) return;
		}

	}

	private boolean migratePartially(int migrationPencent) {
		Map<Integer,Integer> toMigrateVMs = pickupToMigrateVMs(migrationPencent);
		Map<Integer,Integer> nonMigrateVMs = pickupNonMigrateVMs(migrationPencent);
		return false;
	}

	private Map<Integer, Integer> pickupToMigrateVMs(int migrationPencent) {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<Integer, Integer> pickupNonMigrateVMs(int migrationPencent) {
		int migrationVmNumber = (int)(vNum*migrationPencent/100.0);
		Map<Integer,Integer> resultVMs = new HashMap<Integer, Integer>();
		
		for (int i=0; i<migrationVmNumber;i++){
			
		}
		return resultVMs;
	}


}
