package examples.dcenergy;

import java.util.List;
import java.util.Random;

import org.jgap.BaseGeneticOperator;
import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.IntegerGene;

import examples.dcenergy.DcEnergy.EnergyGeneConstraintChecker;

public class UniformCrossover extends BaseGeneticOperator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4610069753895473057L;

	public UniformCrossover(Configuration a_configuration)
			throws InvalidConfigurationException {
		super(a_configuration);
		
	}
	
	public void operateMutation(Population a_population, List a_candidateChromosomes) {
		
		int chromosomeSize = a_candidateChromosomes.size();
		
		if (chromosomeSize==0){
			System.out.println("a_candidateChromosomes Number==0");
			return;
		}
		
		int mutationNumber = chromosomeSize/10;
		if (mutationNumber==0 && chromosomeSize >0) {
			mutationNumber = 1;			
		}
		for (int i=0 ;i< mutationNumber;i++){
			Chromosome chromosome;
			
			//pick the chromosome to mutate
			Random r = new Random();
			int r1 = Math.abs( r.nextInt())%chromosomeSize;
			
			chromosome = (Chromosome) a_candidateChromosomes.get(r1);

			Gene[] genes = chromosome.getGenes();	
			
			MutateGenes(genes);
			
			//chromosome.setFitnessValueDirectly(-1);			
			//chromosome.getFitnessValue();
		}
		
	}

	private void MutateGenes(Gene[] genes) {
		int size = genes.length / 5;
		
		if (size==0 && genes.length>0) size=1; 
		Random r = new Random();
		
		 IntegerGene gene = (IntegerGene) genes[0];
		 int bound = gene.getUpperBounds();
		 
		for (int i=0;i<size;i++){
			int r1 = Math.abs( r.nextInt())%genes.length;
			
			int r2 = Math.abs( r.nextInt())%bound;
			
			if (r1%2==0) //give larger VMs more opportunity
				r2= (int) Math.round( r2 /2.0);
			genes[r1].setAllele(new Integer(r2));
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void operate(Population a_population, List a_candidateChromosomes) {
		Sort(a_candidateChromosomes);
		
		int deceasedNumber = a_candidateChromosomes.size()*4/5;
		if (deceasedNumber==0) {
			System.out.println("deceasedNumber==0");
			return;
		}
		for (int i=deceasedNumber;i<a_candidateChromosomes.size();i++){
			Chromosome chromosome;
				
			Random r = new Random();
			int r1 = Math.abs( r.nextInt())%deceasedNumber;
			int r2 = 0;
			do {
				r2 =Math.abs( r.nextInt())%deceasedNumber;
			}while(r1==r2);
			int parentA = r1;
			int parentB = r2;
			chromosome = createNewChrome(a_candidateChromosomes, parentA,
					parentB);

			a_candidateChromosomes.set(i, chromosome);	
			
			//chromosome.setFitnessValueDirectly(-1);
			//chromosome.getFitnessValue();
		}
		
		operateMutation(a_population, a_candidateChromosomes);
	}

	private Chromosome createNewChrome(List a_candidateChromosomes, int father,
			int mother) {
		Chromosome chromosomeA = (Chromosome) a_candidateChromosomes.get(father);
		
		Chromosome chromosomeB = (Chromosome) a_candidateChromosomes.get(mother);
		
		Configuration conf = chromosomeA.getConfiguration();
		
		Gene[] sampleGenes = new Gene[chromosomeA.getGenes().length];
		
		for (int i=0; i< chromosomeA.getGenes().length; i++){
	    	
			IntegerGene geneA = (IntegerGene) chromosomeA.getGenes()[i];
			
			IntegerGene geneB = (IntegerGene) chromosomeB.getGenes()[i];
			
			Random r = new Random();
			int r1 = Math.abs( r.nextInt())%2;
			
				IntegerGene geneNew = (IntegerGene) geneA.newGene();
			if (r1 == 0) {
				geneNew.setAllele(geneB.getAllele());
			} else {
				geneNew.setAllele(geneA.getAllele());
			}
			
			sampleGenes[i] = geneNew;
	               
	    }
		Chromosome sampleChromosome = null;
	    try {
	    	sampleChromosome = new Chromosome(conf, sampleGenes);
		} catch (InvalidConfigurationException e) {
			System.out.println("ouch, errors occur during creatation of new Chromosome");
			e.printStackTrace();
		}
		return sampleChromosome;
	}

	private void Sort(List a_candidateChromosomes) {
		for (int i=0;i<a_candidateChromosomes.size();i++){
			Chromosome chromosomeA = (Chromosome) a_candidateChromosomes.get(i);
			for (int j=i+1;j<a_candidateChromosomes.size();j++){
				Chromosome chromosomeB = (Chromosome) a_candidateChromosomes.get(j); 
				if (chromosomeA.getFitnessValue() < chromosomeB.getFitnessValue())
					swapInList(a_candidateChromosomes,i,j);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void swapInList(List a_candidateChromosomes, int i, int j) {
		Object tmp = a_candidateChromosomes.get(i);
		
		a_candidateChromosomes.set(i,a_candidateChromosomes.get(j));
		a_candidateChromosomes.set(j, tmp);
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
