package grant.wu.jannealer.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.sun.jmx.remote.internal.ArrayQueue;

import net.sourceforge.jannealer.AnnealingScheme;
import net.sourceforge.jannealer.ObjectiveFunction;
import net.sourceforge.jannealer.test.Util;

public class SimuAnnealTest {
	
	static transient int vNum = 15;
	static transient int pNum = 9;
	
	static transient double vCPU[]=null;
	static transient double vMEM[]= null;
	static transient double pCPU[]=null;
	static transient double pMEM[]= null;
	static transient double ePM[]= null;
	static transient double idleEnergyRatio = 0.7;
	
	
	static double initialTemperature = 1000;
	static double temperature = 1000;
	static double coldingRate = 5;
	
	static double recentBest = Double.MAX_VALUE - 1;
	static double sofarBest = Double.MAX_VALUE - 1;
	static double fftCost = Double.MAX_VALUE - 1;
	static int iTeration = 10000;
	
	static int vAssign[]=null;
	
	static int vAssignOld[]=null;
	
	static int vAssignBest[]=null;
	
	static int  vRecentAssignBest[] = null;
	
	static double pUtilization[] = null;
	
	static double pUtilizationMem[] = null;
	
	static Random random = new java.util.Random();
	
	static int totalIteration = 0;
	
	static ArrayQueue<String> resultAssign = new ArrayQueue<String>(10);
	static ArrayQueue<Integer> results = new ArrayQueue<Integer>(10);
	
	static Date beginTime = null;
	
	private static void initAssign(){
		if (vAssign==null) vAssign= new int[vNum];
		if (vAssignOld==null) vAssignOld = new int[vNum];		
		
		//print("best Fit");
		//bestFit();
		print("first Fit");
		firstFit();
	}	
	
	private static void initAssignRandom(){
		if (vAssign==null) vAssign= new int[vNum];
		if (vAssignOld==null) vAssignOld = new int[vNum];		
		
		print("radom assignment initially");
		randomFit();
		
	}	
	
	
	private static void sortVM() {
		for (int i=0;i<vNum;i++){
			double prevMax = vCPU[i];
			for (int j = i+1;j<vNum;j++){
				if(vCPU[j] > prevMax){					
					vCPU[i] = vCPU[j];
					vCPU[j] = prevMax;
					prevMax = vCPU[i]; 
					
					//swap the mem usage attribute
					double tmp = vMEM[i];
					vMEM[i] = vMEM[j];
					vMEM[j] = tmp;
				}
			}			
		}			
	}
	
	private static void sortPM() {
		for (int i=0;i<pNum;i++){
			double prevMax = pCPU[i];
			for (int j = i+1;j<pNum;j++){
				if(pCPU[j] > prevMax){					
					pCPU[i] = pCPU[j];
					pCPU[j] = prevMax;
					prevMax = pCPU[i]; 
					
					//swap the mem usage attribute
					double tmp = pMEM[i];
					pMEM[i] = pMEM[j];
					pMEM[j] = tmp;
				}
			}			
		}			
	}
	
	private static Random getRandom(){
		long tick = (new Date()).getTime();
		Random result = new Random();
		result.setSeed(tick);
		return result;
	}
	
	private static void randomFit() {
		double[] pLeftCPU = new double[pNum];
		double[] pLeftMEM = new double[pNum];
		for (int i=0;i<pNum;i++){
			pLeftCPU[i] = pCPU[i];
			pLeftMEM[i] = pMEM[i];
		}
		for (int i=0;i<vNum;i++){
			
			int selectedPM = -1;
			
			Random r = getRandom();	
			while(selectedPM == -1){
				int j = Math.abs( r.nextInt()) % pNum;
				
				if (pLeftCPU[j]>vCPU[i] && pLeftMEM[j]>vMEM[i] ){					
						selectedPM = j;
				}
			}	
			vAssign[i] = selectedPM;
			pLeftCPU[selectedPM] -= vCPU[i];
			pLeftMEM[selectedPM] -= vMEM[i];			
		}		
		
		sofarBest = stateEnergy(vAssign);
		fftCost = sofarBest;
		saveResult();
		
		sofarBest= Double.MAX_VALUE;
	}

	
	private static void bestFit() {
		double[] pLeftCPU = new double[pNum];
		double[] pLeftMEM = new double[pNum];
		for (int i=0;i<pNum;i++){
			pLeftCPU[i] = pCPU[i];
			pLeftMEM[i] = pMEM[i];
		}
		for (int i=0;i<vNum;i++){
			double lowestIncreaseEnergy = Double.MAX_VALUE;
			int selectedPM = 0;
			for (int j = 0;j<pNum;j++){
				
				
				if (pLeftCPU[j]>vCPU[i] && pLeftMEM[j]>vMEM[i] ){
					
					double oldEnergy = getEngergy(j,pCPU[j]-pLeftCPU[j]);
					double increaseEnergy = getEngergy(j,pCPU[j]-pLeftCPU[j] + vCPU[i]) -oldEnergy;
					
					if (increaseEnergy<lowestIncreaseEnergy){
						lowestIncreaseEnergy = increaseEnergy;
						selectedPM = j;
					}
					
				}
			}	
			vAssign[i] = selectedPM;
			pLeftCPU[selectedPM] -= vCPU[i];
			pLeftMEM[selectedPM] -= vMEM[i];			
		}		
		
		sofarBest = stateEnergy(vAssign);
		fftCost = sofarBest;
		saveResult();
		
		sofarBest= Double.MAX_VALUE;
	}

	private static double getEngergy(int pmNo, double cpuUsage) {
		double u = cpuUsage / pCPU[pmNo];
		double energy = 0;
		if (u > 0.00001)
			energy = u * (1- idleEnergyRatio ) * ePM[pmNo] + idleEnergyRatio * ePM[pmNo];
		
		return energy;
	}

	private static void firstFit() {
		double[] pLeftCPU = new double[pNum];
		double[] pLeftMEM = new double[pNum];
		for (int i=0;i<pNum;i++){
			pLeftCPU[i] = pCPU[i];
			pLeftMEM[i] = pMEM[i];
		}
		for (int i=0;i<vNum;i++){
			for (int j = 0;j<pNum;j++){
				if (pLeftCPU[j]>vCPU[i] && pLeftMEM[j]>vMEM[i] ){
					vAssign[i] = j;
					pLeftCPU[j] -= vCPU[i];
					pLeftMEM[j] -= vMEM[i];
					break;
				}
			}			
		}		
		
		sofarBest = stateEnergy(vAssign);
		fftCost = sofarBest;
		saveResult();
		
		sofarBest= Double.MAX_VALUE;
	}



	private static void anneal(int scale) {
		print("annealing method 1: random assigment with group annealing");
		initAssign();
		while( temperature > 0){
			int staleMateCount = 0;
			for (int iT = 0; iT< iTeration ; iT++){
				totalIteration++;
				//fluctuate();
				sediment();
				//double curEnergy = stateEnergy(vAssign);
				double curEnergy = stateEnergyGroup();
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
					
					//fluctuate();
					sediment();
					//curEnergy = stateEnergy(vAssign);
					curEnergy = stateEnergyGroup();
					if ( curEnergy - recentBest > dievationEnergy())
						revertFluctuate();
					else{
						recentBest = curEnergy;
						saveRecentBestAssign();
						staleMateCount = 0;
					}
				}
			}			
			
			temperature -= coldingRate;
		}		
	}
	
	private static void anneal_old(int scale, boolean initFFD) {
		print("annealing method 1: random assignment");
		
		temperature = initialTemperature;
		totalIteration = 0;
		
		if (initFFD){
			initAssign();
		}else{
			initAssignRandom();
		}
		
		
		while( temperature > 0){
			int staleMateCount = 0;
			for (int iT = 0; iT< iTeration * scale ; iT++){
				if (getTicksFromStart()> 200) break;
				totalIteration++;
				fluctuate();			
				double curEnergy = stateEnergy(vAssign);
				
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
					
					if ( curEnergy - recentBest > dievationEnergy())
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
	
	private static void saveRecentBestAssign() {
		if (vRecentAssignBest==null){
			vRecentAssignBest = new int [vNum];
		}
		for (int i=0; i< vNum; i++){
			vRecentAssignBest[i] = vAssign[i] ;
		}
		
	}

	private static void saveResult() {
				
		double tick = getTicksFromStart();
		strResult = "";
		for (int i=0; i< vNum; i++){
			strResult = strResult + vAssign[i] + "," ;
		}	

		strResult = (String.format("%.1f", tick)+"\t"+totalIteration +"\t " + String.format("%.2f", sofarBest)
				+"\t" +String.format("%.2f%%", (fftCost-sofarBest)*100/fftCost) +" assignment " + strResult);
		
		
		saveBestAssign();
		
		strResult += " ";
		if (pUtilization != null) {
			for (int i = 0; i < pNum; i++) {
				strResult = strResult + pCPU[i] + "-"
						+ String.format("%.2f%%|%.2f%%,", pUtilization[i]*100,pUtilizationMem[i]*100);
			}
		}
		
		print(strResult);
		/*
		resultAssign.add("");
		results.add(0);
		*/		
	}

	private static double getTicksFromStart() {
		Date now = new Date();
		double tick = 0;
		if (beginTime!=null)
			tick= now.getTime() - beginTime.getTime();
		tick = tick/1000;
		return tick;
	}
	
	private static void saveBestAssign() {
		if (vAssignBest==null){
			vAssignBest = new int [vNum];
		}
		if (vRecentAssignBest!=null){
			for (int i=0; i< vNum; i++){
				vAssignBest[i] = vRecentAssignBest[i] ;
			}
		}
	}


	private static void revertFluctuate() {
		for (int i=0; i< vNum; i++){
			vAssign[i] = vAssignOld[i];
		}		
	}


	
	private static boolean initSediment = false;
	private static int sedimentGroupNum = 4;
	private static List<ArrayList<Integer>> sedimentGroups = null;
	private static void initSediment(){
		sedimentGroups = new ArrayList<ArrayList<Integer>>(sedimentGroupNum);
		int curVM = 0;
		int groupSize = vNum / sedimentGroupNum;
		for (int i=0;i<sedimentGroupNum-1;i++){			
			ArrayList<Integer> group = new ArrayList<Integer>();
			sedimentGroups.add(group);
			for(int j= 0; j< groupSize;j++){				
				group.add(curVM++);
			}
		}
		
		// the last group
		ArrayList<Integer> group = new ArrayList<Integer>();
		sedimentGroups.add(group);
		for(; curVM< vNum;curVM++){
			group.add(curVM);
		}	
		initSediment = true;
	}
	
	private static int curGroupNum;
	private static int lastGroupNum = -1;
	private static void sediment(){
		//the temperature only allow a certain 10% range of VMs to move around
		//the VM sized above the range will be taken away at the moment
		//the VM sized blow the range will stay their current positions
		
		//step0: init
		if (!initSediment){
			initSediment();
		}
		
		
		
		//step1: calculate the current range to deal with
		long groupNum = sedimentGroupNum -1 - Math.round( temperature*100 / initialTemperature)/10;
		
		if (groupNum<0)
			groupNum = 0;
		else if (groupNum>=sedimentGroupNum)
			groupNum = sedimentGroupNum-1;
		curGroupNum = (int) groupNum; 
		
		if (lastGroupNum!=-1 && lastGroupNum!=curGroupNum){
			getLastRoundBestAssign();
			generateInitialGroupAssign(curGroupNum);			
		}
		else{		
			fluctuateSediment(curGroupNum);
		}
		
		lastGroupNum = curGroupNum;
	}

	private static void generateInitialGroupAssign(int grpNum) {
		double[] pLeftCPU = new double[pNum];
		double[] pLeftMEM = new double[pNum];
		for (int i=0;i<pNum;i++){
			pLeftCPU[i] = pCPU[i];
			pLeftMEM[i] = pMEM[i];
		}
		
		//computeLeftCPU
		for (int i=0;i<grpNum;i++){
			
			ArrayList<Integer> curGroup = sedimentGroups.get(i);								
			
			for (int j=0;j<curGroup.size();j++)
			{
				int iVM = curGroup.get(j);
				int iPM = vAssign[iVM];								
				pLeftCPU[iPM] -= vCPU[iVM] ;
				pLeftMEM[iPM] -= vMEM[iVM] ;
			}						
		}
		
		//generate initial assignment for this group
		ArrayList<Integer> curGroup = sedimentGroups.get(grpNum);								
		
		for (int j=0;j<curGroup.size();j++)
		{
			int iVM = curGroup.get(j);
			for (int k = 0; k< pNum; k++){
												
				if ( pLeftCPU[k] >= vCPU[iVM] && pLeftMEM[k] >= vMEM[iVM])
				{
					 vAssign[iVM] = k;	
					 pLeftCPU[k] -= vCPU[iVM] ;	
				}
			}
		}			
	}

	private static void getLastRoundBestAssign() {
		if (vAssignBest!=null){
			for (int i=0; i< vNum; i++){
				vAssign[i] = vAssignBest[i] ;
			}
		}
		else{
			print("vAssignBest is null");
		}		
	}

private static void fluctuateSediment(int grpNum) {
		
	ArrayList<Integer> curGroup = sedimentGroups.get((int) grpNum);
	
		int rnd = Math.abs( random.nextInt());
		/*
		if ( rnd% 2 ==0){
			swapSediment(grpNum);
			return;
		}*/
		
		for (int i=0; i< vNum; i++){
			vAssignOld[i] = vAssign[i];
		}
		
		int vmNumMax = curGroup.size()<3?curGroup.size():3; // we don't want to change many vm assignment at a time
		int vmNum = rnd % vmNumMax;
		if (vmNum==0) vmNum=1;
		
		
		
		//pickVMFromGroups(grpNum, vNum);
		
		pickVMFromCurGroup(grpNum, vNum);
	}

private static void pickVMFromGroups(int grpNum, int vmNum) {
	ArrayList<Integer> curGroup = sedimentGroups.get((int) grpNum);
	
	for (int k=0;k<vmNum;k++){
		// pick up the group first, current group with the most probability
		int sqareNum = (grpNum+1)*(grpNum+1);
		int rnd = Math.abs( random.nextInt()) % sqareNum;
		int selGrp = grpNum;
		for(int i = 0; i<= grpNum ; i++){
			if (rnd<=i){
				selGrp = i;
				break;
			}
		}
		
		ArrayList<Integer> selGroup = sedimentGroups.get(selGrp);
		int grpInx = Math.abs( random.nextInt()) % selGroup.size();
		if (grpInx >= 0 && grpInx < selGroup.size()) {
				int vm = selGroup.get(grpInx);

				int pm = Math.abs(random.nextInt()) % pNum;
				vAssign[vm] = pm;
		}
	}
}

private static void pickVMFromCurGroup(int grpNum, int vmNo) {
	
	ArrayList<Integer> curGroup = sedimentGroups.get((int) grpNum);
	for (int k=0;k<vmNo;k++){
		// pick up the vm
			int grpInx = Math.abs(random.nextInt()) % curGroup.size();
		// pick up the pm
			if (grpInx >= 0 && grpInx < curGroup.size()) {
				int vm = curGroup.get(grpInx);

				int pm = Math.abs(random.nextInt()) % pNum;
				vAssign[vm] = pm;
			}
	}
}
	
	private static void swapSediment(int grpNum) {
		
		for (int i=0; i< vNum; i++){
			vAssignOld[i] = vAssign[i];
		}
		
		ArrayList<Integer> curGroup = sedimentGroups.get((int) grpNum);
		
		int oldPm1;
		int oldPm2;
		
		do{

			//pick up the vm
			int grpInx = Math.abs( random.nextInt() ) % curGroup.size();
			//pick up the pm
			
			int vm1 = curGroup.get(grpInx);		
			
			grpInx = Math.abs( random.nextInt() ) % curGroup.size();
		
			int vm2 = curGroup.get(grpInx);
		
			oldPm1 = vAssignOld[vm1];
			oldPm2 = vAssignOld[vm2];
		
			vAssign[vm1] = oldPm2;
			vAssign[vm2] = oldPm1;
		
		}while(oldPm1==oldPm2);		
	}
	
	private static double stateEnergyGroup(){
		
		int grpNum = curGroupNum;
		double energy = 0;
		for (int i=grpNum+1;i<sedimentGroupNum;i++){
			energy += 1000000;
		}
		double[] uPM = new double[pNum];
		double[] usedMEM = new double[pNum];
		for (int i=0;i<=grpNum;i++){
				
			ArrayList<Integer> curGroup = sedimentGroups.get(i);								
			
			for (int j=0;j<curGroup.size();j++)
			{
				int iVM = curGroup.get(j);
				int iPM = vAssign[iVM];
				if ( iPM >= pNum || iPM <0 ){
					print("illegal assignment " + vAssign.toString());
					return Double.MAX_VALUE;
				}				
				uPM[iPM] += vCPU[iVM] / pCPU[iPM];			
				usedMEM[iPM] += vMEM[iVM]/ pMEM[iPM];	
			}						
		}
		for (int k = 0;k< pNum; k++){
			if (uPM[k]>1 || usedMEM[k] > 1){
				return Double.MAX_VALUE;
			}
			double energyPM = 0;
			if (uPM[k] > 0.001)
				energyPM = uPM[k] * (1- idleEnergyRatio ) * ePM[k] + idleEnergyRatio * ePM[k];
			energy += energyPM;
			
			saveUtilization(uPM,usedMEM);
		}
		return energy;
	}
	
	
	
	private static void saveUtilization(double[] uPM, double[] mPM) {
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

	private static void fluctuate_old() {
		
		if (random.nextInt() % 2 ==0){
			swap();
			return;
		}
		
		for (int i=0; i< vNum; i++){
			vAssignOld[i] = vAssign[i];
		}
		//pick up the vm
		int vm = Math.abs( random.nextInt() ) % vNum;
		//pick up the pm
		
		int oldPm = vAssignOld[vm];
		int distance = (int)(pNum * 2 * (temperature / initialTemperature)) + 2;
		distance =  random.nextInt()  % distance;		
		int pm = Math.abs( (oldPm + distance )%pNum );		
		vAssign[vm] = pm;
	}
	
	
	private static void fluctuate() {
		
		int rnd = Math.abs( random.nextInt()); 
		if (rnd % 2 ==0){
			swap();
			return;
		}
		
		for (int i=0; i< vNum; i++){
			vAssignOld[i] = vAssign[i];
		}
		
		rnd = rnd % 3;
		for (int k=0; k<rnd; k++){
			//pick up the vm
			int vm = Math.abs( random.nextInt() ) % vNum;

			int pm = Math.abs(random.nextInt()) % pNum;
			vAssign[vm] = pm;
		}
	}
	
	private static void swap() {
		
		for (int i=0; i< vNum; i++){
			vAssignOld[i] = vAssign[i];
		}
		
		int oldPm1;
		int oldPm2;
		
		do{
		//pick up the vm
		int vm1 = Math.abs( random.nextInt() ) % vNum;
		//pick up the pm
		int vm2 = Math.abs( random.nextInt() ) % vNum;
		
		if (vm1<0 || vm2 <0 || vm1 >= vNum || vm2 >= vNum) return; //weird, it could happen
		
		oldPm1 = vAssignOld[vm1];
		oldPm2 = vAssignOld[vm2];
		
				
		vAssign[vm1] = oldPm2;
		vAssign[vm2] = oldPm1;
		}while(oldPm1==oldPm2);
	}
	
	private static double dievationEnergy(){
		double largestPMEnergy = 0;
		for (int i = 0;i< pNum; i++){			
			largestPMEnergy +=  ePM[i];			
		}
		double energy = largestPMEnergy * temperature / initialTemperature + 0.1 ;
		return energy;
	}
	
	private static void print(String s){
		Date now = new Date();		
		
		s = now.toString() +": "+s;
		System.out.println(s);
		
		String fName = "simulatonAnneal-"+problemScale+"-"+problemCapacityIndex+".txt";
		writeText(fName,s);
	}
	
	private static String resultFolder = "";
	
	private static void writeText(String fFileName,String message) {
		String folder = "C:\\users\\n7682905\\" + resultFolder;
	
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

	private static void createFileIfNotExist(String filePath) throws IOException {
		File f;
		f = new File(filePath);
		if (!f.exists()){
			f.createNewFile();
		}
	}
	
	private static void createFolderIfNotExist(String filePath) throws IOException {
		File f;
		f = new File(filePath);
		if (!f.exists()){
			f.mkdir();
		}
	}
	
	private static double stateEnergy(int[] assignment){
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
	
	private static boolean hasLowerEnergy(double curEnergy) {
		return ( curEnergy < recentBest );
	}
	
	private static void generateProblem(int scale,int capacityIndexPM, boolean mem) throws Exception{
		
		vNum = scale;
		vNum = vNum <=0? 1: vNum;
		vCPU = new double[vNum];
		vMEM = new double[vNum];
		vNum = scale;
		pNum = scale *2 / 2 / capacityIndexPM;
		pNum = pNum <=0 ? 1:pNum;
		pCPU = new double[pNum];
		pMEM = new double[pNum];
		ePM= new double[pNum];
		
		
		Random r = getRandom();
		r.setSeed(2000);
		Random rMem = getRandom();
		rMem.setSeed(3000);
		for (int i=0;i<vNum;i++){
			double randomRequirement = Math.abs( r.nextInt()% 20 ) * 100 ;
			if (randomRequirement<0.01) randomRequirement= 50; // minimum cpu to keep it alive
			vCPU[i] = randomRequirement;
			
			randomRequirement = Math.abs( rMem.nextInt()% 20 ) * 100 ;
			vMEM[i] = randomRequirement ;
		}
		
		sortVM();
		
		int capacity[] = { 1000, 1200, 1500, 1800, 2000, 2300, 2400, 2500, 2700, 3000};
		//int capacity[] = { 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000};
		for (int i=0;i<pNum;i++){			
			pCPU[i] = capacity[i%10] * capacityIndexPM;		
			if (mem)
				pMEM[i] = pCPU[i]; //;
			else
				pMEM[i] = 30000 * capacityIndexPM; 
		}
		
		sortPM();
		
		/*for (int i=0;i<pNum;i++){
			ePM[i]= (pCPU[i]/10) * ( 1 - 1000/pCPU[i]); // 1000-> 100, 3000-> 300 * (1-1/3)
		}*/
		for (int i=0;i<pNum;i++){
			double energyTimes = 1;
			if (pCPU[i]/1000 < 100){
				//10 times capacity, 5 times of energy
				//100 times capacity, 20 times of energy
				energyTimes =( 1 - Math.log10( pCPU[i]/1000)*0.4); 
			}else{
				print("wrong capacity: " + pCPU[i]);
				throw new Exception();
			}
			
			ePM[i]= (pCPU[i]/10) * energyTimes; // 1000-> 100*(1-0), 3000-> 300 * (1-1/3)
		} 
		 
		idleEnergyRatio = 0.7;
		printProblem();
		
		vAssign= new int[vNum];
		
		vAssignOld = new int[vNum];
		
		vAssignBest = new int[vNum];
		
		vRecentAssignBest = new int[vNum];
		
		pUtilization = new double[pNum];
		
		pUtilizationMem = new double[pNum];
	}
	
	private static  void printProblem(){
		String s = "";
		double totalReqquirement = 0;
		for (int i=0;i<vNum;i++){
			s += String.format("%.0f,",vCPU[i]);
			totalReqquirement += vCPU[i];
		}
		
		print("VM No " + vNum +" requiremnt " + s);
		print("total requirement " + totalReqquirement);
		
		s = "";
		double totalCapacity = 0;
		for (int i=0;i<pNum;i++){
			s += String.format("%.0f,",pCPU[i]);
			totalCapacity += pCPU[i];
		}
		
		print("PM " + pNum +" capacity " + s);
		print("total capacity " + totalCapacity);				
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	
	static private int problemScale = 10;
	static private int problemCapacityIndex = 1;
	private static String strResult;
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		beginTime = new Date();
		String resultFile = "st_result"+beginTime.getHours()+beginTime.getMinutes() +".txt";
		sedimentGroupNum=10;
		coldingRate = 50;
		int problemScales[] = {20, 50,100 }; //200 20, 50, 50,51,52,53,54,55,56,57,58,59,60
		int problemCapacityIndexes[] = {1, 2, 3, 4, 5, 10};
		boolean initWithFFD = true;
		for (int experimentTime=40;experimentTime<50;experimentTime++){
			resultFolder = "st-mem-" + String.format("%02d", experimentTime);
			boolean mem = false;
			if (experimentTime>=40) mem = true;
			for (int i=0;i<problemScales.length;i++){				
				for (int j=0;j<problemCapacityIndexes.length;j++){
					problemScale = problemScales[i];
					problemCapacityIndex = problemCapacityIndexes[j];
					generateProblem(problemScale,problemCapacityIndex,mem);
					beginTime = new Date();
					print("started "+beginTime.toString());			
					anneal_old(problemScale,initWithFFD);		
					//anneal(problemScale);
					writeText(resultFile, problemScale+"\t"+problemCapacityIndex+"\t"+ strResult);
					print("finished");
				}
			}
		}// for experimentTimes
	}

}
