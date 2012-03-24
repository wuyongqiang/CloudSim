package org.cloudbus.cloudsim.power;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class SimulationAnneal {
	
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

	public SimulationAnneal(double[] pCPU, double[] pVM, int[] vAssignOld, int oldPMInUse, int newPMInUse, double targetUtilization) {
		this.pCPU = pCPU;
		this.vCPU = pVM;
		this.pmInUse = newPMInUse;
		this.pmInUseOld = oldPMInUse;
		this.vAssignOld = vAssignOld;
		
		//pNum = pCPU.length;
		pNum = pmInUse  ;
		if (pNum>pCPU.length) pNum = pCPU.length;
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
		for (int i=0;i<vNum;i++){
			vAssign[i] = vAssignOld[i];
		}
		
		initPMPower();
		
		String  s = "pmInUseOld=" + pmInUseOld + " pmInUse=" + pmInUse + " initial vm assignment:";
		for (int i=0; i< vNum; i++){
			s += String.format("%2d,", vAssignOld[i]) ;
		}
		System.out.println(s);
	}
	
	private void initPMPower() {
		for (int i=0;i<pNum;i++){
			ePM[i] = pCPU[i]/10;
		}		
	}

	private void print(String s){
		Date now = new Date();		
		
		s = now.toString() +": "+s;
		//System.out.println(s);
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyMMdd-hhmmss");
		
		String st = fmt.format(beginTime);
		String fName = "simulatonAnneal.txt";
		writeText(fName,s);
	}
	
	private void createFileIfNotExist(String filePath) throws IOException {
		File f;
		f = new File(filePath);
		if (!f.exists()){
			f.createNewFile();
		}
	}
	
	private void createFolderIfNotExist(String filePath) throws IOException {
		File f;
		f = new File(filePath);
		if (!f.exists()){
			f.mkdir();
		}
	}
	
	private void writeText(String fFileName,String message) {
		String folder = "C:\\users\\n7682905\\" + resultFolder+"\\";
	
		try {
			createFolderIfNotExist(folder);
			
			fFileName = folder +"\\" +  fFileName;
			
			createFileIfNotExist(fFileName);

			Writer out = new OutputStreamWriter(
					new FileOutputStream(fFileName, true), "utf8");
			try {
				out.append(message+"\r\n");
			} finally {
				out.close();
			}
		} catch (Exception e) {
		}
	}

	public void anneal() {
		if (pNum<=1) return;
		int scale = vCPU.length;
		iTeration = scale;
			beginTime = new Date();
			print("annealing method 1: random assignment");
			
			int temperature = initialTemperature;
			totalIteration = 0;
			recentBest = Double.MAX_VALUE;
			
			double dE = dievationEnergy();
			firstFit();

			while( temperature > 0){
				int staleMateCount = 0;
				dE = dievationEnergy();
				for (int iT = 0; iT< iTeration * scale ; iT++){
					if (getTicksFromStart()> 30) break;
					totalIteration++;
					fluctuate();			
					double curEnergy = stateEnergy(vAssign);
					
					if (curEnergy/migrationCost <= vNum*0.2) break;
					if (hasLowerEnergy(curEnergy )){
						recentBest = curEnergy;
						saveRecentBestAssign();
						staleMateCount = 0;
						
						if (recentBest < sofarBest){
							sofarBest = recentBest;
							saveResult();
						}
					}
					else{
						revertFluctuate();
						staleMateCount ++;
					}
					
					if (staleMateCount >= scale*scale){		
						//double tmpcost = sofarBest;
						//initAssign();
						//sofarBest = tmpcost;
						fluctuate();
						
						curEnergy = stateEnergy(vAssign);
						
						if ( curEnergy - recentBest > dE)
							revertFluctuate();
						else{
							recentBest = curEnergy;
							saveRecentBestAssign();
							staleMateCount = 0;
						}
					}
				}			
				
				temperature -= coldingRate;
				if ( ((int)temperature) %10 == 0) print("temperature="+temperature);
			}		
		}

	private void saveRecentBestAssign() {
		if (vRecentAssignBest==null){
			vRecentAssignBest = new int [vNum];
		}
		for (int i=0; i< vNum; i++){
			vRecentAssignBest[i] = vAssign[i] ;
		}
		
	}

	private double dievationEnergy() {
		//double largestPMEnergy = 0;
		largestPMEnergy = 0;
		for (int i = 0;i< pNum; i++){			
			largestPMEnergy +=  ePM[i];			
		}
		//double energy = largestPMEnergy * temperature / initialTemperature + 0.1 ;
		//double energy = largestPMEnergy * temperature / (initialTemperature * pNum)  ;
		double energy = migrationCost * vNum * temperature / (initialTemperature) ;
		return energy;
	}

	private boolean hasLowerEnergy(double curEnergy) {
		//if(curEnergy < Double.MAX_VALUE)
		//	print("curEnergy "+curEnergy );
		return ( curEnergy < recentBest );
	}

	private void revertFluctuate() {	
			for (int i=0; i< vNum; i++){
				vAssign[i] = vAssignOldTmp[i];
			}		
		}


	private double stateEnergy(int[] vAssign2) {
		// this energy comes from the how many migrations are needed
		// the more migration, the more energy
		// migration as constraint or a component of the energy?
		// the number of PMs in use cannot be changed
		// if the machine number increase, then the energy soars
		double energyCost = getEnergyCost(vAssign2);
		migrations = getMigrationCost(vAssign2);
		
		if (energyCost < Double.MAX_VALUE)
			energyCost = migrations *  migrationCost ;
		return energyCost;
	}

	private int getMigrationCost(int[] vAssign2) {
		int migrations = 0;
		for (int i=0;i<this.vNum;i++){
			if (vAssign2[i]!=this.vAssignOld[i])
				migrations ++;
		}
		return migrations;
	}

	private double getEnergyCost(int[] assignment) {
		double energy = 0;
		double[] uPM = new double[pNum];
		double[] usedMEM = new double[pNum];
		for (int i=0;i<assignment.length;i++)
		{
			int iPM = assignment[i];
			if ( iPM >= pNum || iPM <0 ){
				print("illegal assignment " + assignment.toString());
				return Double.MAX_VALUE;
			}
			
			uPM[iPM] += vCPU[i] / pCPU[iPM];
			//usedMEM[iPM] += vMEM[i]/ pMEM[iPM];
		}
		
		for (int i = 0;i< pNum; i++){
			if (uPM[i]>targetUtilization + 0.1
					//|| usedMEM[i]>1 
					){
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

	private void saveUtilization(double[] uPM, double[] mPM) {
		
			if (pUtilization==null){
				pUtilization = new double[pNum];
			}
			for (int i=0;i<uPM.length;i++){
				pUtilization[i] = uPM[i];
			}
			
			if (pUtilizationMem==null){
				pUtilizationMem = new double[pNum];
			}
			
			for (int i=0;i<mPM.length;i++){
				pUtilizationMem[i] = mPM[i];
			}
		}

	public int[] getAssignment() {
		if (migrations>0)
			print("migrations = " + migrations);
		String  s = "pmInUseOld=" + pmInUseOld + " pmInUse=" + pmInUse + " final vm assignment:";
		for (int i=0; i< vNum; i++){
			s += String.format("%2d,", vAssignBest[i]) ;
		}
		System.out.println(s);
		return vAssignBest;
	}
	
	private double getTicksFromStart() {
		Date now = new Date();
		double tick = 0;
		if (beginTime!=null)
			tick= now.getTime() - beginTime.getTime();
		else
			beginTime = now;
		tick = tick/1000;
		return tick;
	}
	
	private void saveBestAssign() {
		print("save a best assign " + this.totalIteration);
		
		if (vAssignBest==null){
			vAssignBest = new int [vNum];
		}
		
		String s = "";
		if (vRecentAssignBest!=null){
			for (int i=0; i< vNum; i++){
				vAssignBest[i] = vRecentAssignBest[i] ;
				s +=vAssignBest[i] + ","; 
			}
		}
		
		print(s);
	}
	
	private Random getRandom(){
		long tick = (new Date()).getTime();
		Random result = new Random();
		result.setSeed(tick);
		return result;
	}
	
	private void fluctuate() {
		
		Random random = getRandom();
		if (random.nextInt() % 2 ==0){
			swap();
			return;
		}
		
		
		int vNum = vAssign.length;
		for (int i=0; i< vNum; i++){
			vAssignOldTmp[i] = vAssign[i];
		}
		int rnd = Math.abs(random.nextInt()) % 3;
		for (int k=0; k<rnd; k++){
			//pick up the vm
			int vm = Math.abs( random.nextInt() ) % vNum;

			int pm = Math.abs(random.nextInt()) % pNum;
			vAssign[vm] = pm;
		}
	}
	
	private void swap() {
		
		for (int i=0; i< vNum; i++){
			vAssignOldTmp[i] = vAssign[i];
		}
		
		int oldPm1;
		int oldPm2;
		
		if (vNum<=1 || pNum<=1) return; //no need to swap
		
		int loopCount = 1;
		do{
		Random random = getRandom();
		//pick up the vm
		int vm1 = Math.abs( random.nextInt() ) % vNum;
		//pick up the pm
		int vm2 = Math.abs( random.nextInt() ) % vNum;
		
		if (vm1<0 || vm2 <0 || vm1 >= vNum || vm2 >= vNum) return; //weird, it could happen
		
		oldPm1 = vAssignOldTmp[vm1];
		oldPm2 = vAssignOldTmp[vm2];
		
				
		vAssign[vm1] = oldPm2;
		vAssign[vm2] = oldPm1;
		loopCount++;
		}while(oldPm1==oldPm2 && loopCount < vNum*2);
	}
	
	private void firstFit() {
		
		for (int i=0; i< vNum; i++){
			vAssignOldTmp[i] = vAssign[i];
		}
		
		double[] pLeftCPU = new double[pNum];
		//double[] pLeftMEM = new double[pNum];
		for (int i=0;i<pNum;i++){
			pLeftCPU[i] = pCPU[i] * targetUtilization;
			//pLeftMEM[i] = pMEM[i];
		}
		for (int i=0;i<vNum;i++){
			for (int j = 0;j<pNum;j++){
				if (pLeftCPU[j]>vCPU[i] 
						//&& pLeftMEM[j]>vMEM[i] 
								){
					vAssign[i] = j;
					pLeftCPU[j] -= vCPU[i];
					//pLeftMEM[j] -= vMEM[i];
					break;
				}
			}			
		}		
		
		sofarBest =  stateEnergy(vAssignOldTmp);
		recentBest = stateEnergy(vAssign);
		
		if ( recentBest < sofarBest)
			sofarBest = recentBest;
		else
			revertFluctuate();
		
		saveRecentBestAssign();
		
		fftCost = sofarBest;
		saveResult();
		
		//sofarBest= Double.MAX_VALUE;
	}
	
	
	private void saveResult() {
		
		double tick = getTicksFromStart();
		String strResult = "";
		for (int i=0; i< vNum; i++){
			strResult = strResult + vAssign[i] + "," ;
		}	

		strResult = (String.format("%.1f", tick)+"\t"+totalIteration +"\t " + String.format("%.2f", sofarBest)
				+"\t" +String.format("%.2f%%", (fftCost-sofarBest)*100/fftCost) +" \nassignment " + strResult);
		
		
		saveBestAssign();
		
		strResult += "\n ";
		if (pUtilization != null) {
			for (int i = 0; i < pNum; i++) {
				strResult = strResult + pCPU[i] + "-"
						+ String.format("%.2f%%|%.2f%%,", pUtilization[i]*100,pUtilizationMem[i]*100);
			}
		}
		
		
			for (int i = 0; i < vNum; i++) {
				strResult += "\n";
				strResult = strResult + String.format("%.0f",vCPU[i]) + ",";
						
			}
			
			for (int i = 0; i < pNum; i++) {
				strResult += "\n";
				strResult = strResult + String.format("%.2f",pCPU[i]) + ",";
						
			}
		
		
		
		print(strResult);
		/*
		resultAssign.add("");
		results.add(0);
		*/		
	}

}
