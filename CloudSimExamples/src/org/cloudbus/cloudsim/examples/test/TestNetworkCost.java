package org.cloudbus.cloudsim.examples.test;

import static org.junit.Assert.*;

import org.cloudbus.cloudsim.network.FatTreeTopologicalNode;
import org.cloudbus.cloudsim.network.TopologicalGraph;
import org.cloudbus.cloudsim.power.EnergyNetworkSimulationAnneal;
import org.cloudbus.cloudsim.power.NetworkSimulationAnneal;
import org.cloudbus.cloudsim.power.SimulationAnneal;
import org.junit.Test;

public class TestNetworkCost {
	
	
	double[] pCPU = {5,5,5,5,3,2};
	double[] pVM = {1,1,1,1,1,2,2,2,2,2};
	int[] vAssignOld = {0,0,0,0,1,1,2,2,3,3};
	int oldPMInUse = 5;
	int newPMInUse = 5;
	double targetUtilization = 0.81;
	
	private void resetArray(int a[]){
		for (int i=0;i<a.length;i++){
			a[i] = 0;
		}
	}
	/*
	 * 
	 *                             core
	 *                       /            \
	 *                     a0              a1 
	 *              /             \         \ 
	 *             e0              e1       e2 
	 *          /     \         /     \      |
	 *        pm0      pm1     pm2     pm3  pm4
	 *  /   |  |   \   |     / |  \    |     |
	 * vm0 vm1 vm2 vm3 vm5 v4 vm6 vm7 vm8   vm9
	 */

	@Test
	public void test() {
		int vAssignOldHere[] = {0,0,0,0,2,1,2,2,3,4};
		
		String[] vmNames = {"vm0","vm1","vm2","vm3","vm4","vm5","vm6","vm7","vm8","vm9"};
		
		int traffic[] = new int[100];
		
		int vmCount = 10;
		
		/*
		traffic[0 * vmCount + 5] = 10; //vm0 ~ vm5, 10
		traffic[1 * vmCount + 6] = 20; //vm1 ~ vm6, 20
		traffic[2 * vmCount + 7] = 30; //vm2 ~ vm7, 30
		traffic[3 * vmCount + 8] = 40; //vm3 ~ vm8, 40
		traffic[4 * vmCount + 9] = 50; //vm4 ~ vm9, 50
		*/
		
		EnergyNetworkSimulationAnneal sa= new EnergyNetworkSimulationAnneal(pCPU, pVM, vAssignOldHere, oldPMInUse, newPMInUse,
				targetUtilization, vmNames) ;
		TopologicalGraph graph = FatTreeTopologicalNode.generateTree(5, 2);
		
		FatTreeTopologicalNode root = FatTreeTopologicalNode.orgnizeGraphToTree(graph);
		
		sa.setNetworkRootNode(root);
		
		
		resetArray(traffic);			
		sa.setVmTraffic(traffic,vmCount);
		int cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(0, cost);
		
		resetArray(traffic);	
		traffic[2 * vmCount + 3] = 10; //vm2 ~ vm3, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(0, cost);
		
		resetArray(traffic);	
		traffic[0 * vmCount + 1] = 10; //vm0 ~ vm1, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(0, cost);
		
		resetArray(traffic);	
		traffic[0 * vmCount + 4] = 10; //vm0 ~ vm4, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(20, cost);
		
		resetArray(traffic);	
		traffic[1 * vmCount + 5] = 10; //vm1 ~ vm5, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(10, cost);
		
		resetArray(traffic);	
		traffic[5 * vmCount + 3] = 10; //vm5 ~ vm3, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(10, cost);
		
		resetArray(traffic);	
		traffic[6 * vmCount + 8] = 10; //vm6 ~ vm8, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(10, cost);
		
		resetArray(traffic);	
		traffic[5 * vmCount + 8] = 10; //vm5 ~ vm8, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(20, cost);
		
		resetArray(traffic);	
		traffic[2 * vmCount + 9] = 10; //vm2 ~ vm9, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(40, cost);
		
		
		resetArray(traffic);	
		traffic[5 * vmCount + 8] = 10; //vm5 ~ vm8, 10		
		traffic[2 * vmCount + 9] = 10; //vm2 ~ vm9, 10
		sa.setVmTraffic(traffic,vmCount);
		cost = sa.getTotalNetworkCost(vAssignOldHere);
		assertEquals(60, cost);
		
	}
	
	@Test
	public void testNetworkSimulationAnneal(){
		int vAssignOldHere[] = {0,0,0,0,2,1,2,2,3,4};
		
		String[] vmNames = {"vm0","vm1","vm2","vm3","vm4","vm5","vm6","vm7","vm8","vm9"};
		
		int traffic[] = new int[100];
		
		int vmCount = 10;		
		
		NetworkSimulationAnneal sa= new NetworkSimulationAnneal(pCPU, pVM, vAssignOldHere, oldPMInUse, newPMInUse,
				targetUtilization, vmNames) ;
		TopologicalGraph graph = FatTreeTopologicalNode.generateTree(5, 2);
		
		FatTreeTopologicalNode root = FatTreeTopologicalNode.orgnizeGraphToTree(graph);
		
		sa.setNetworkRootNode(root);
		
		resetArray(traffic);	
		traffic[5 * vmCount + 8] = 10; //vm5 ~ vm8, 10		
		traffic[2 * vmCount + 9] = 10; //vm2 ~ vm9, 10
		sa.setVmTraffic(traffic,vmCount);
		
		sa.anneal();		
		int[] result = sa.getAssignment();
		FatTreeTopologicalNode nodeforvm5 = root.getNodeByIdInMap(result[5]);
		FatTreeTopologicalNode nodeforvm8 = root.getNodeByIdInMap(result[8]);
		System.out.println("nodeforvm5="+nodeforvm5.getNodeLabel());
		System.out.println("nodeforvm8="+nodeforvm8.getNodeLabel());
		assertEquals(1, nodeforvm5.getNodesFrom(nodeforvm8).size());
		
		FatTreeTopologicalNode nodeforvm2 = root.getNodeByIdInMap(result[2]);
		FatTreeTopologicalNode nodeforvm9 = root.getNodeByIdInMap(result[9]);
		System.out.println("nodeforvm2="+nodeforvm2.getNodeLabel());
		System.out.println("nodeforvm9="+nodeforvm9.getNodeLabel());
		assertEquals(1, nodeforvm2.getNodesFrom(nodeforvm9).size());
	}

}
