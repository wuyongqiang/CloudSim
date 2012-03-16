package grant.wu.jannealer.example;

import net.sourceforge.jannealer.AnnealingScheme;
import net.sourceforge.jannealer.ObjectiveFunction;
import net.sourceforge.jannealer.test.Util;

public class Example1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AnnealingScheme scheme = new AnnealingScheme();
		
		scheme.setSolution(new double[]{1,2,1,0,2});
		scheme.setFunction(new ObjectiveFunction()
		{
			private final static int NDIM = 5;
			// 600MIPS, 700 MIPS, 800 MIPS , 900 MIPS, 1100 MIPS 
			// 4 PMs , 1000MIPS, 2000MIPS, 2000MIPS, 3000 MIPS
			
			public int getNdim()
			{
				return NDIM;
			}
			public double distance(double[] vertex)
			{
				double sumd = 0;
				double sumr = 0;
				double[] wid = { 1, 1.8, 1.8, 2.4 };
				for (int jj = 0; jj < NDIM; jj++)
				{
					double q = vertex[jj] * wid[jj];
					double r =
						(double) (q >= 0 ? (int) (q + 0.5) : - (int) (0.5 - q));
					sumr += q * q;
					sumd += (q - r) * (q - r);
				}
				return Integer.MAX_VALUE;
			}
		});
		
		
		scheme.anneal();
		
		Util.printSolution(scheme);

	}

}
