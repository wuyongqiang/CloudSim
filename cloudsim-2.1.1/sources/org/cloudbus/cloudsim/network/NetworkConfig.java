/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licensing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.cloudbus.cloudsim.network;

import java.util.Iterator;
import java.util.Random;

import org.cloudbus.cloudsim.network.FatTreeTopologicalNode;
import org.cloudbus.cloudsim.network.TopologicalGraph;
import org.cloudbus.cloudsim.network.TopologicalNode;
import org.cloudbus.cloudsim.power.NetworkCostCalculator;

/**
 * Sample fitness function for the CoinsEnergy example. Adapted from
 * examples.MinimizingMakeChangeFitnessFunction
 * 
 * @author Klaus Meffert
 * @since 2.4
 */
public class NetworkConfig {
	// private final double normalEnergyBound = 1000*1000*100;
	// private final double breachEnergy = normalEnergyBound * 100000;

	/** String containing the CVS revision. Read out via reflection! */
	private final String CVS_REVISION = "$Revision: 1.5 $";

	private int vNum;
	private int pNum;

	private double pLeastTheoryEnergy;

	private NetworkCostCalculator networkCalc;

	private FatTreeTopologicalNode root;

	private int traffic[];

	private double networkWeight = 1;

	private void generateProblem(int pNum, int vNum) throws Exception {
		this.pNum = pNum;
		this.vNum = vNum;

		generateNetworkConfig(pNum, vNum);
		printProblem();
	}

	private void generateNetworkConfig(int pmNumber, int vmNumber) {
		int childrenNumber = 5;
		for (int i = 2; i < 10; i++) {
			childrenNumber = i;
			if (Math.pow(i, 3) > pmNumber)
				break;
		}
		println("children number of network node is = " + childrenNumber);
		TopologicalGraph graph = FatTreeTopologicalNode.generateTree(pmNumber,
				childrenNumber);

		root = FatTreeTopologicalNode.orgnizeGraphToTree(graph);
		networkCalc = new NetworkCostCalculator();
		networkCalc.setNetworkRootNode(root);

		traffic = new int[vmNumber * vmNumber];

		resetArray(traffic);
		// networkPairs(vmNumber, traffic);
		networkRandomGrp(vmNumber, traffic);
		networkCalc.setVmTraffic(traffic, vmNumber);
	}

	private void networkPairs(int vmNumber, int traffic[]) {
		int vmHalfNumber = vmNumber / 2;
		for (int i = 0; i < vmHalfNumber; i++) {
			traffic[i * vmNumber + (vmHalfNumber + i)] = 10;
		}
	}

	private void networkRandom(int vmNumber, int traffic[]) {
		Random r = new Random(123456L);
		for (int i = 0; i < vmNumber; i++) {
			for (int j = 0; j < vmNumber; j++)
				if (i != j)
					traffic[i * vmNumber + j] = r.nextInt(10);
		}
	}

	private void networkRandomGrp(int vmNumber, int traffic[]) {
		Random r = new Random(123456L);
		int grpNum = vmNumber / 4;
		int vmGrp[] = new int[vmNumber];
		for (int i = 0; i < vmNumber; i++) {
			vmGrp[i] = r.nextInt(grpNum);
		}
		for (int i = 0; i < grpNum; i++) {
			String s = "Group" + i + " :";
			for (int j = 0; j < vmNumber; j++) {
				if (vmGrp[j] == i) {
					s += j + ",";
				}
			}
			println(s);
		}
		for (int i = 0; i < vmNumber; i++) {
			for (int j = 0; j < vmNumber; j++) {
				if (i != j && vmGrp[i] == vmGrp[j]) // in a grp
					traffic[i * vmNumber + j] = r.nextInt(10);
			}
		}
	}

	private void resetArray(int a[]) {
		for (int i = 0; i < a.length; i++) {
			a[i] = 0;
		}
	}

	private void printProblem() {
		String s = "";
		// network traffic info
		s = "network traffic info\n";
		for (int i = 0; i < vNum; i++) {
			for (int j = 0; j < vNum; j++) {
				s += String.format("%4d", traffic[i * vNum + j]);
			}
			s += "\n";
		}
		println(s);
	}

	public NetworkConfig(int pmNumber, int vmNumber) throws Exception {
		generateProblem(pmNumber, vmNumber);
	}

	/**
	 * Returns the total weight of all coins.
	 * 
	 * @param a_potentialSolution
	 *            the potential solution to evaluate
	 * @return total weight of all coins
	 * 
	 * @author Klaus Meffert
	 * @since 2.4
	 */
	public double getTotalWeight(int[] tmpAssign) {

		double totalWeight = networkCalc.getTotalNetworkCost(tmpAssign)
				* networkWeight;
		return totalWeight;
	}

	private void println(String s) {
		System.out.println(s);
	}

	public String printResult(int tmpAssign[], int tmpuPM[], int tmpusedMEM[]) {
		String s = " assignment";

		for (int i = 0; i < pNum; i++) {
			addToNetworkNode(String.format("cpu%%%.2f,mem%%%.2f",
					tmpuPM[i] * 100, tmpusedMEM[i] * 100), i);
		}

		s = s + "\n";

		for (int i = 0; i < vNum; i++) {
			addToNetworkNode(i, tmpAssign[i]);
		}
		FatTreeTopologicalNode.clearTreeNode2StrBuilder();
		FatTreeTopologicalNode.printTreeNode2(root);
		s = s + FatTreeTopologicalNode.getTreeNode2StrBuilder();
		return s;
	}

	private void clearAllNetworkNodeAppData() {
		if (root != null) {
			Iterator<TopologicalNode> it = root.getGraph().getNodeIterator();
			while (it.hasNext()) {
				FatTreeTopologicalNode node = (FatTreeTopologicalNode) it
						.next();
				node.getAppData().clear();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void addToNetworkNode(Object appData, int iPM) {
		if (root != null) {
			FatTreeTopologicalNode node = FatTreeTopologicalNode
					.getNodeByIdInGraph(root.getGraph(), iPM);
			node.getAppData().add(appData);
		}

	}

	private String getTotalWeightStr(int[] tmpAssign) {
		double totalNetworkCost = networkCalc.getTotalNetworkCost(tmpAssign)
				* networkWeight;
		double totalWeight = totalNetworkCost;
		return String.format("network:"
				+ String.format("%.2f", totalNetworkCost));
	}
}
