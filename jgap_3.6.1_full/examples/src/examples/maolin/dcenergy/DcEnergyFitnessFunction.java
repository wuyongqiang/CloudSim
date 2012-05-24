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
import java.util.Random;

import org.jgap.*;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

/**
 * Sample fitness function for the CoinsEnergy example. Adapted from
 * examples.MinimizingMakeChangeFitnessFunction
 *
 * @author Klaus Meffert
 * @since 2.4
 */
public class DcEnergyFitnessFunction
    extends FitnessFunction {
  //private static final double normalEnergyBound = 1000*1000*100;	
  //private static final double breachEnergy = normalEnergyBound * 100000;
		
	
/** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.5 $";

private static int vNum;

private static double[] vCPU;

private static double[] vMEM;


private static int[] vAssign;
private static int pNum;

private static double[] pCPU;

private static double[] pMEM;

private static double[] ePM;

private static double idleEnergyRatio;

private static double[] pUtilization;

private static double pLeastTheoryEnergy;  

private static double pLargestPMEnergy;  

private static boolean inited = false;

  private static void generateProblem(int scale,int capacityIndexPM) throws Exception{
	  
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
			
			totalRequirement += vCPU[i];
			
			randomRequirement = Math.abs( rMem.nextInt()% 20 ) * 100 ;
			vMEM[i] = randomRequirement;
		}
		
		sortVM();
		
		int capacity[] = { 1000, 1200, 1500, 1800, 2000, 2300, 2400, 2500, 2700, 3000};
		
						
		
		for (int i=0;i<pNum;i++){			
			pCPU[i] = capacity[i%10] * capacityIndexPM;		
			pMEM[i] = 3000 * capacityIndexPM;
						
		}
		
		
		sortPM();
		double maxPMCapacity = 0;
		for (int i=0;i<pNum;i++){
			
			
			double energyTimes = 1;
			if (pCPU[i]/1000 < 100){
				//10 times capacity, 6 times of energy
				//100 times capacity, 20 times of energy
				energyTimes =( 1 - Math.log10( pCPU[i]/1000)*0.4); 
			}else{
				print("wrong capacity: " + pCPU[i]);
				throw new Exception();
			}
				
			ePM[i]= (pCPU[i]/10) * energyTimes; // 1000-> 100*(1-0), 3000-> 300 * (1-1/3)
						
			if (pCPU[i]>maxPMCapacity){
				maxPMCapacity = pCPU[i];
				pLargestPMEnergy = ePM[i];
			}
		}
		
		idleEnergyRatio = 0.7;
		//pLeastTheoryEnergy = totalRequirement /10 * (1 - 1000/maxPMCapacity) /2;
		
		pLeastTheoryEnergy = (totalRequirement/maxPMCapacity) * (1- idleEnergyRatio ) * pLargestPMEnergy + idleEnergyRatio * pLargestPMEnergy;
		
		 
		
		//printProblem();
		print("gnerate problem done");
		inited = true;
		
		printProblem();
	}
  
  
  public static int[] firstFit() {
	  
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
		
		
		System.out.println("ffd energy:" + stateEnergy(vAssign));
		
		return vAssign;
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



  private static void printProblem() {
	String s = "VM:";
	double totalRequirement = 0;
	  for(int i=0; i< vNum;i++){
		s += vCPU[i]+",";
		totalRequirement += vCPU[i];
	}
	  
	  print(s);
	  print(String.format("total VM %d requirement %.2f", vNum, totalRequirement));
	  s = "PM:";
	  totalRequirement = 0;
	  for(int i=0; i< pNum;i++){
		s += pCPU[i]+",";
		totalRequirement += pCPU[i];
	}
	  print(s);
	  print(String.format("total PM %d capacity %.2f",pNum, totalRequirement));
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
    totalWeight = stateEnergy(tmpAssign);
    return totalWeight;
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
  
  private static void print(String s){
		Date dt = new Date();		
		System.out.println(dt.toString() +": "+s);
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
		for (int i = 0; i < numberOfGenes; i++) {
			int pmNumber = getPmNumberAtGene(bestSolutionSoFar, i);
			tmpAssign[i] = pmNumber;
			
			int iPM = pmNumber;
			tmpuPM[iPM] += vCPU[i] / pCPU[iPM];
			tmpusedMEM[iPM] += vMEM[i];
		}
		
		for (int i=0; i< vNum; i++){
			s = s + tmpAssign[i] + "," ;
		}	
		
		s = s + "\n";
		
		for (int i=0; i< pNum; i++){
			s = s + pCPU[i]+ String.format("[%%%.2f]", tmpuPM[i]*100) + "," ;
		}	
		
		

		return s;
}

}
