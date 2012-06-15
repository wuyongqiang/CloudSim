/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licensing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package examples.maolin.dcenergy;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.cloudbus.cloudsim.network.FatTreeTopologicalNode;
import org.cloudbus.cloudsim.network.TopologicalGraph;
import org.cloudbus.cloudsim.network.TopologicalNode;
import org.cloudbus.cloudsim.power.NetworkCostCalculator;
import org.jgap.*;


/**
 * Sample fitness function for the CoinsEnergy example. Adapted from
 * examples.MinimizingMakeChangeFitnessFunction
 *
 * @author Klaus Meffert
 * @since 2.4
 */
public class DcEnergyFitnessFunction
    extends FitnessFunction {
  //private  final double normalEnergyBound = 1000*1000*100;	
  //private  final double breachEnergy = normalEnergyBound * 100000;
		
	
/** String containing the CVS revision. Read out via reflection!*/
  private final  String CVS_REVISION = "$Revision: 1.5 $";

private  int vNum;

private  double[] vCPU;

private  double[] vMEM;


private  int[] vAssign;
private  int pNum;

private  double[] pCPU;

private  double[] pMEM;

private  double[] ePM;

private  double idleEnergyRatio;

private  double[] pUtilization;

private  double pLeastTheoryEnergy;  

private  double pLargestPMEnergy;  

private  boolean inited = false;

private  NetworkCostCalculator networkCalc;
private  FatTreeTopologicalNode root;

private  int traffic[];

private  double networkWeight = 1;
private  double energyWeight = 1;

  private  void generateProblem(int scale,int capacityIndexPM) throws Exception{
	  
	  if (inited) return;
		
		vNum = scale;
		vNum = vNum <=0? 1: vNum;
		vCPU = new double[vNum];
		vMEM = new double[vNum];
		vAssign = new int[vNum];
		vNum = scale;
		pNum = scale *2 / 2 / capacityIndexPM;
		pNum = pNum <=0 ? 1:pNum;
		pCPU = new double[pNum];
		pMEM = new double[pNum];
		ePM= new double[pNum];
		
		
		
		
		Random r = new Random();
		r.setSeed(2000);
		Random rMem = new Random();
		rMem.setSeed(3000);
		
		double totalRequirement = 0;
		for (int i=0;i<vNum;i++){
			double randomRequirement = Math.abs( r.nextInt()% 20 ) * 100 ;
			vCPU[i] = randomRequirement;
			if (randomRequirement<0.01) randomRequirement= 50; // minimum cpu to keep it alive
			vCPU[i] = randomRequirement;
			
			totalRequirement += vCPU[i];
			
			randomRequirement = Math.abs( rMem.nextInt()% 20 ) * 100 ;
			if (randomRequirement<0.01) randomRequirement= 200;
			vMEM[i] = randomRequirement;
			
		}
		
		sortVM();
		
		int capacity[] = { 1000, 1200, 1500, 1800, 2000, 2300, 2400, 2500, 2700, 3000};
		//int capacity[] = {2000, 2000, 2000,2000,2000,2000, 2000, 2000,2000,2000};
						
		
		for (int i=0;i<pNum;i++){			
			pCPU[i] = capacity[i%10] * capacityIndexPM;		
			pMEM[i] = pCPU[i];//3000 * capacityIndexPM;
						
		}
		
		
		sortPM();
		double maxPMCapacity = 0;
		for (int i=0;i<pNum;i++){
			
			
			double energyTimes = 1;
			if (pCPU[i]/1000 < 100){
				//10 times capacity, 6 times of energy
				//100 times capacity, 20 times of energy
				energyTimes =( 1 - Math.log10( pCPU[i]/1000)*0.4);
				//Random r3 = new Random(3);
				//energyTimes = 0.5 + 0.5 * r3.nextInt(10)/10;
			}else{
				println("wrong capacity: " + pCPU[i]);
				throw new Exception();
			}
				
			ePM[i]= (pCPU[i]/10) * energyTimes; // 1000-> 100*(1-0), 3000-> 300 * (1-1/3)
						
			if (pCPU[i]>maxPMCapacity){
				maxPMCapacity = pCPU[i];
				pLargestPMEnergy = ePM[i];
			}
		}
		
		idleEnergyRatio = 0.7;		
		
		pLeastTheoryEnergy = (totalRequirement/maxPMCapacity) * (1- idleEnergyRatio ) * pLargestPMEnergy + idleEnergyRatio * pLargestPMEnergy;
		
		 
		
		//printProblem();
		println("gnerate problem done");
		inited = true;
		
		generateNetworkConfig(pNum,vNum);
		
		printProblem();
	}
  
  private  void generateNetworkConfig(int pmNumber, int vmNumber){
	  int childrenNumber = 5;
	  for(int i=2;i<10;i++){
		  childrenNumber = i;
		  if (Math.pow(i, 3)  > pmNumber)
			  break;
	  }
	  println("children number of network node is = " + childrenNumber);
	  TopologicalGraph graph = FatTreeTopologicalNode.generateTree(pmNumber, childrenNumber);
		
		root = FatTreeTopologicalNode.orgnizeGraphToTree(graph);
		networkCalc = new NetworkCostCalculator();
		networkCalc.setNetworkRootNode(root);
		
		traffic = new int[vmNumber * vmNumber];
		
		resetArray(traffic);	
		//networkPairs(vmNumber, traffic);
		networkRandomGrp(vmNumber, traffic);
		networkCalc.setVmTraffic(traffic,vmNumber);
  }
  
   private void networkPairs(int vmNumber, int traffic[] ){
	  int vmHalfNumber = vmNumber / 2;
		for (int i=0;i<vmHalfNumber; i++){
			traffic[i * vmNumber + (vmHalfNumber + i)] = 10;		
		}
  }
  
   private void networkRandom(int vmNumber, int traffic[] ){
	  	Random r = new Random(123456L);
		for (int i=0;i<vmNumber; i++){
			for (int j=0;j<vmNumber; j++)
				if (i!=j)
					traffic[i * vmNumber + j] = r.nextInt(10);		
		}
  }
  
   private void networkRandomGrp(int vmNumber, int traffic[] ){
	  	Random r = new Random(123456L);
	  	int grpNum = vmNumber / 4;
	  	int vmGrp[] = new int[vmNumber];
	  	for (int i=0;i<vmNumber; i++){
	  		vmGrp[i]=r.nextInt(grpNum);
	  	}
	  	for (int i=0;i<grpNum;i++){
	  		String s = "Group"+i + " :";
	  		for (int j=0;j<vmNumber;j++){
	  			if (vmGrp[j]==i){
	  				s += j+",";
	  			}
	  		}
	  		println(s);
	  	}
		for (int i=0;i<vmNumber; i++){
			for (int j=0;j<vmNumber; j++){
				if (i!=j && vmGrp[i] == vmGrp[j]) // in a grp
					traffic[i * vmNumber + j] = r.nextInt(10);
			}
		}
  }
  
   private void resetArray(int a[]){
		for (int i=0;i<a.length;i++){
			a[i] = 0;
		}
	}
  
  
  public  int[] firstFit() {
	  
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
		
		
		println("ffd energy:" + getTotalWeightStr(vAssign));
		
		return vAssign;
	}
  
  


private  void sortVM() {
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
	
	private  void sortPM() {
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



  private  void printProblem() {
	String s = "VM:";
	double totalRequirement = 0;
	  for(int i=0; i< vNum;i++){
		s += "("+vCPU[i]+","+vMEM[i]+"),";
		totalRequirement += vCPU[i];
	}
	  
	  println(s);
	  println(String.format("total VM %d requirement %.2f", vNum, totalRequirement));
	  s = "PM:";
	  totalRequirement = 0;
	  for(int i=0; i< pNum;i++){
		s += "("+pCPU[i]+","+pMEM[i]+"),";;
		totalRequirement += pCPU[i];
	}
	  println(s);
	  println(String.format("total PM %d capacity %.2f",pNum, totalRequirement));
	  
	  //network traffic info	  
//	  s = "network traffic info\n";
//	  for (int i=0;i<vNum; i++){
//			for (int j=0;j<vNum; j++){
//				s += String.format("%4d", traffic[i * vNum + j]);
//			}
//			s += "\n";
//		}
	  println(s);
}





public DcEnergyFitnessFunction(int scale,int capacityIndexPM) throws Exception {
	 
		  generateProblem(scale, capacityIndexPM);
	
  }

  /**
   * Determine the fitness of the given Chromosome instance. The higher the
   * return value, the more fit the instance. This method should always
   * return the same fitness value for two equivalent Chromosome instances.
   *
   * @param a_subject the Chromosome instance to evaluate
   *
   * @return positive double reflecting the fitness rating of the given
   * Chromosome
   * @since 2.0 (until 1.1: return type int)
   * @author Neil Rotstan, Klaus Meffert, John Serri
   */
  public double evaluate(IChromosome a_subject) {
    // The fitness value measures both how close the value is to the
    // target amount supplied by the user and the total number of coins
    // represented by the solution. We do this in two steps: first,
    // we consider only the represented amount of change vs. the target
    // amount of change and return higher fitness values for amounts
    // closer to the target, and lower fitness values for amounts further
    // away from the target. Then we go to step 2, which returns a higher
    // fitness value for solutions representing fewer total coins, and
    // lower fitness values for solutions representing more total coins.
    // ------------------------------------------------------------------
 

    double fitness = 0;

    // Step 1: Determine total sum of energies (interpreted as weights here)
    // of coins. If higher than the given maximum value, the solution is not
    // accepted in any way, i.e. the fitness value is then set to the worst
    // value.
    double totalEnergy = getTotalWeight(a_subject);
   /* if (totalEnergy > breachEnergy) {
      if (a_subject.getConfiguration().getFitnessEvaluator().isFitter(2, 1)) {
        return 1.0d;
      }
      else {
    	  fitness = breachEnergy*(1/totalEnergy);
      }
    }*/

    fitness = pLeastTheoryEnergy/totalEnergy * 100;

    // Make sure fitness value is always positive.
    // -------------------------------------------
    return Math.max(1.0d, fitness);
  }






  /**
   * Retrieves the number of coins represented by the given potential
   * solution at the given gene position.
   *
   * @param a_potentialSolution the potential solution to evaluate
   * @param a_position the gene position to evaluate
   * @return the number of coins represented by the potential solution at the
   * given gene position
   *
   * @author Neil Rotstan
   * @since 1.0
   */
   public int getPmNumberAtGene(IChromosome a_potentialSolution,
                                           int a_position) {
    Integer num =
        (Integer) a_potentialSolution.getGene(a_position).getAllele();
    return num.intValue();
  }



  /**
   * Returns the total weight of all coins.
   *
   * @param a_potentialSolution the potential solution to evaluate
   * @return total weight of all coins
   *
   * @author Klaus Meffert
   * @since 2.4
   */
   public double getTotalWeight(IChromosome a_potentialSolution) {
	  int[] tmpAssign = new int[vNum];
	  
	  double totalWeight = 0.0d;
    int numberOfGenes = a_potentialSolution.size();
    for (int i = 0; i < numberOfGenes; i++) {
      int pmNumber = getPmNumberAtGene(a_potentialSolution,i);
      tmpAssign[i] = pmNumber;
      
    }
    totalWeight = stateEnergy(tmpAssign)*energyWeight + networkCalc.getTotalNetworkCost(tmpAssign) * networkWeight;
    return totalWeight;
  }
  
  
  private  double stateEnergy(int[] assignment){
		double energy = 0;
		double[] uPM = new double[pNum];
		double[] usedMEM = new double[pNum];
		for (int i=0;i<assignment.length;i++)
		{
			int iPM = assignment[i];
			if ( iPM >= pNum || iPM <0 ){
				println("illegal assignment " + assignment.toString());
				return Double.MAX_VALUE;
			}
			
			uPM[iPM] += vCPU[i] / pCPU[iPM];
			usedMEM[iPM] += vMEM[i];
		}
		
		for (int i = 0;i< pNum; i++){
			double energyPM = 0;
			
			if (uPM[i]>1 || pMEM[i]<usedMEM[i]){
				double uMem = usedMEM[i] /pMEM[i];
				double uE = uPM[i]>uMem?uPM[i]:uMem;
				energyPM = pLargestPMEnergy * uE *2;
			}
			else{			
				if (uPM[i] > 0.001)
					energyPM = uPM[i] * (1- idleEnergyRatio ) * ePM[i] + idleEnergyRatio * ePM[i];
			}
			energy += energyPM;
			
			saveUtilization(uPM);
		}
		
		return energy ;
	}
  
  
	private  void saveUtilization(double[] uPM) {
		if (pUtilization==null){
			pUtilization = new double[pNum];
		}
		
		for (int i=0;i<uPM.length;i++){
			pUtilization[i] = uPM[i];
		}
	}
  
  private  void println(String s){
	  PrintUtil.print(s);
	}

  /**
   *
   * @param a_maxFitness the maximum fitness value allowed
   * @param a_weight the coins weight of the current solution
   * @return the penalty computed
   * @author Klaus Meffert
   * @since 2.4
   */
  protected double computeWeightPenalty(double a_maxFitness, double a_weight) {
    if (a_weight <= 0) {
      // we know the solution cannot have less than one coin
      return 0;
    }
    else {
      // The more weight the more penalty, but not more than the maximum
      // fitness value possible. Let's avoid linear behavior and use
      // exponential penalty calculation instead
      return (Math.min(a_maxFitness, a_weight * a_weight));
    }
  }

public  String printResult(IChromosome bestSolutionSoFar) {
		String s = " assignment";
		
		int tmpAssign[]= new int[vNum];
		double tmpuPM[] = new double[pNum];
		double tmpusedMEM[] = new double [pNum];
		                        
		int numberOfGenes = bestSolutionSoFar.size();
		
		assert(numberOfGenes == vNum);
		
		clearAllNetworkNodeAppData();
		
		for (int i = 0; i < numberOfGenes; i++) {
			int pmNumber = getPmNumberAtGene(bestSolutionSoFar, i);
			tmpAssign[i] = pmNumber;
			
			int iPM = pmNumber;
			tmpuPM[iPM] += vCPU[i] / pCPU[iPM];
			tmpusedMEM[iPM] += vMEM[i] / pMEM[iPM];
		}
		
		
		
		for (int i=0; i< pNum; i++){
			//s = s + pCPU[i]+ String.format("[%%%.2f]", tmpuPM[i]*100) + "," ;
			addToNetworkNode(String.format("cpu%%%.2f,mem%%%.2f", tmpuPM[i]*100,tmpusedMEM[i]*100),i);
		}	
		
		s = s + "\n";
		
		for (int i=0; i< vNum; i++){
			//s = s + tmpAssign[i] + "," ;
			addToNetworkNode(i,tmpAssign[i]);
		}	
		FatTreeTopologicalNode.clearTreeNode2StrBuilder();
		FatTreeTopologicalNode.printTreeNode2(root);
		s = s + FatTreeTopologicalNode.getTreeNode2StrBuilder();
		return s;
}

private  void clearAllNetworkNodeAppData() {
	if (root != null) {
		Iterator<TopologicalNode> it = root.getGraph().getNodeIterator();
		while(it.hasNext()){
			FatTreeTopologicalNode node = (FatTreeTopologicalNode) it.next();
			node.getAppData().clear();
		}
	}

}

	@SuppressWarnings("unchecked")
	private  void addToNetworkNode(Object appData, int iPM) {
		if (root != null) {
			FatTreeTopologicalNode node = FatTreeTopologicalNode.getNodeByIdInGraph(
					root.getGraph(), iPM);
			node.getAppData().add(appData);
		}

	}

	 public String getTotalWeightStr(IChromosome a_potentialSolution) {
		 
		    int numberOfGenes = a_potentialSolution.size();
		    int[] tmpAssign = new int[numberOfGenes];
			for (int i = 0; i < numberOfGenes; i++) {
		      int pmNumber = getPmNumberAtGene(a_potentialSolution,i);
		      tmpAssign[i] = pmNumber;		      
		    }
			return getTotalWeightStr(tmpAssign);
	}	
	
	 private String getTotalWeightStr(int[] tmpAssign) {
		  double totalEnergy = stateEnergy(tmpAssign);
			double totalNetworkCost = networkCalc.getTotalNetworkCost(tmpAssign) * networkWeight;
			double totalWeight =totalEnergy + totalNetworkCost;
		    return String.format("%.2f",  totalWeight) + " energy:" + String.format("%.2f",totalEnergy)+" network:"+String.format("%.2f",totalNetworkCost);
		}
}
