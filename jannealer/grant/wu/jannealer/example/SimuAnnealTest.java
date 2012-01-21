package grant.wu.jannealer.example;

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
	
	static transient double vCPU[]={600,700,800,900,1100,600,700,800,900,1100,1200,1300,1400,1500,1600};
	static transient double pCPU[]={1000, 2000, 2500, 3000, 3000, 3000, 3000,2500,2500};
	static transient double ePM[]={100, 180, 200, 230.00, 230.01, 230.02, 230.03,230.01,230.02};
	static transient double idleEnergyRatio = 0.7;
	
	
	static double initialTemperature = 1000;
	static double temperature = 1000;
	static final double coldingRate = 5;
	
	static double recentBest = Double.MAX_VALUE - 1;
	static double sofarBest = Double.MAX_VALUE - 1;
	static double fftCost = Double.MAX_VALUE - 1;
	static int iTeration = 10000;
	
	static int vAssign[]=null;
	
	static int vAssignOld[]=null;
	
	static int vAssignBest[]=null;
	
	static int  vRecentAssignBest[] = null;
	
	static double pUtilization[] = null;
	
	static Random random = new java.util.Random();
	
	static int totalIteration = 0;
	
	static ArrayQueue<String> resultAssign = new ArrayQueue<String>(10);
	static ArrayQueue<Integer> results = new ArrayQueue<Integer>(10);
	
	
	private static void initAssign(){
		vAssign= new int[vNum];
		vAssignOld = new int[vNum];		
		firstFit();
	}	
	
	private static void sortVM() {
		for (int i=0;i<vNum;i++){
			double prevMax = vCPU[i];
			for (int j = i+1;j<vNum;j++){
				if(vCPU[j] > prevMax){					
					vCPU[i] = vCPU[j];
					vCPU[j] = prevMax;
					prevMax = vCPU[i]; 
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
				}
			}			
		}			
	}

	private static void firstFit() {
		double[] pLeftCPU = new double[pNum];
		for (int i=0;i<pNum;i++){
			pLeftCPU[i] = pCPU[i];
		}
		for (int i=0;i<vNum;i++){
			for (int j = 0;j<pNum;j++){
				if (pLeftCPU[j]>vCPU[i]){
					vAssign[i] = j;
					pLeftCPU[j] -= vCPU[i];
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
	
	private static void anneal_old(int scale) {
		initAssign();
		while( temperature > 0){
			int staleMateCount = 0;
			for (int iT = 0; iT< iTeration ; iT++){
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
		String s = " ";
		for (int i=0; i< vNum; i++){
			s = s + vAssign[i] + "," ;
		}	

		print("iteration:" + totalIteration +"\tenergy " + String.format("%.2f", sofarBest)
				+"save " +String.format("%.2f%%", (fftCost-sofarBest)*100/fftCost) +" assinment " + s);
		
		
		saveBestAssign();
		
		s = "";
		if (pUtilization != null) {
			for (int i = 0; i < pNum; i++) {
				s = s + pCPU[i] + "-"
						+ String.format("%.2f%%,", pUtilization[i]*100);
			}
		}
		print(s);
		/*
		resultAssign.add("");
		results.add(0);
		*/		
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
		}
		lastGroupNum = curGroupNum;
		fluctuateSediment(curGroupNum);
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
		
		rnd = rnd % curGroup.size();
		for (int k=0;k<rnd;k++){
			// pick up the vm
			int grpInx = Math.abs(random.nextInt()) % curGroup.size();
			// pick up the pm

			int vm = curGroup.get(grpInx);

			int pm = Math.abs(random.nextInt()) % pNum;
			vAssign[vm] = pm;
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
			energy += 100000;
		}
		double[] uPM = new double[pNum];
		
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
			}						
		}
		for (int k = 0;k< pNum; k++){
			if (uPM[k]>1){
				return Double.MAX_VALUE;
			}
			double energyPM = 0;
			if (uPM[k] > 0.001)
				energyPM = uPM[k] * (1- idleEnergyRatio ) * ePM[k] + idleEnergyRatio * ePM[k];
			energy += energyPM;
			
			saveUtilization(uPM);
		}
		return energy;
	}
	
	
	
	private static void saveUtilization(double[] uPM) {
		if (pUtilization==null){
			pUtilization = new double[pNum];
		}
		
		for (int i=0;i<uPM.length;i++){
			pUtilization[i] = uPM[i];
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
		Date dt = new Date();		
		System.out.println(dt.toString() +": "+s);
	}
	
	private static double stateEnergy(int[] assignment){
		double energy = 0;
		double[] uPM = new double[pNum];
		for (int i=0;i<assignment.length;i++)
		{
			int iPM = assignment[i];
			if ( iPM >= pNum || iPM <0 ){
				print("illegal assignment " + assignment.toString());
				return Double.MAX_VALUE;
			}
			
			uPM[iPM] += vCPU[i] / pCPU[iPM];			
		}
		
		for (int i = 0;i< pNum; i++){
			if (uPM[i]>1){
				return Double.MAX_VALUE;
			}
			double energyPM = 0;
			if (uPM[i] > 0.001)
				energyPM = uPM[i] * (1- idleEnergyRatio ) * ePM[i] + idleEnergyRatio * ePM[i];
			energy += energyPM;
			
			saveUtilization(uPM);
		}
		
		return energy;
	}
	
	private static boolean hasLowerEnergy(double curEnergy) {
		return ( curEnergy < recentBest );
	}
	
	private static void generateProblem(int scale){
		int capacityIndexPM = 4;
		vNum = scale;
		vNum = vNum <=0? 1: vNum;
		vCPU = new double[vNum];
		vNum = scale;
		pNum = scale *2 / 2 / capacityIndexPM;
		pNum = pNum <=0 ? 1:pNum;
		pCPU = new double[pNum];		
		ePM= new double[pNum];
		
		
		Random r = new Random();
		r.setSeed(2000);
		for (int i=0;i<vNum;i++){
			double randomRequirement = Math.abs( r.nextInt()% 20 ) * 100 ;
			vCPU[i] = randomRequirement;
		}
		
		sortVM();
		
		int capacity[] = { 1000, 1200, 1500, 1800, 2000, 2300, 2400, 2500, 2700, 3000};
		for (int i=0;i<pNum;i++){			
			pCPU[i] = capacity[i%10] * capacityIndexPM;			
		}
		
		sortPM();
		
		for (int i=0;i<pNum;i++){
			ePM[i]= (pCPU[i]/10) * ( 1 - 1000/pCPU[i]); // 1000-> 100, 3000-> 300 * (1-1/3)
		}
		 
		 
		idleEnergyRatio = 0.7;
		printProblem();
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
	 */
	public static void main(String[] args) {
		
		generateProblem(24);
		print("started");
		anneal_old(100);
		//anneal(20);
		print("finished");
		

	}

}
