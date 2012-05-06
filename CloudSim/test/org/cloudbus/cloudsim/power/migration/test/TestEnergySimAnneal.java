package org.cloudbus.cloudsim.power.migration.test;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.power.CoEvSimAnneal;
import org.cloudbus.cloudsim.power.EnergySimulationAnneal;
import org.cloudbus.cloudsim.power.migration.MigrationProblem;
import org.cloudbus.cloudsim.power.migration.PM;
import org.cloudbus.cloudsim.power.migration.VM;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestEnergySimAnneal {

	private Random getRandom() {
		long tick = 0;// (new Date()).getTime();
		Random result = new Random();
		result.setSeed(tick);
		return result;
	}

	private double[] vCPU;
	private double[] pCPU;
	private double[] pMEM;
	private double[] vMEM;
	private String[] vmNames;
	private int[] vAssignOld;
	private int oldInUse = 0;
	private int newInUse = 0;
	private double[] ePM;
	private double targetUtilization = 1;
	

	private void sortVM() {
		int vNum = vCPU.length;
		for (int i = 0; i < vNum; i++) {
			double prevMax = vCPU[i];
			for (int j = i + 1; j < vNum; j++) {
				if (vCPU[j] > prevMax) {
					vCPU[i] = vCPU[j];
					vCPU[j] = prevMax;
					prevMax = vCPU[i];

					// swap the mem usage attribute
					double tmp = vMEM[i];
					vMEM[i] = vMEM[j];
					vMEM[j] = tmp;
				}
			}
		}
	}

	private void sortPM() {
		int pNum = pCPU.length;
		for (int i = 0; i < pNum; i++) {
			double prevMax = pCPU[i];
			for (int j = i + 1; j < pNum; j++) {
				if (pCPU[j] > prevMax) {
					pCPU[i] = pCPU[j];
					pCPU[j] = prevMax;
					prevMax = pCPU[i];

					// swap the mem usage attribute
					double tmp = pMEM[i];
					pMEM[i] = pMEM[j];
					pMEM[j] = tmp;
				}
			}
		}
	}

	private void generateProblem(int scale, int capacityIndexPM, boolean mem)
	{

		int vNum = scale;
		vNum = vNum <= 0 ? 1 : vNum;
		vCPU = new double[vNum];
		vMEM = new double[vNum];
		vAssignOld = new int[vNum];
		vmNames = new String[vNum];
		vNum = scale;
		int pNum = scale * 2 / 2 / capacityIndexPM;
		pNum = pNum <= 0 ? 1 : pNum;
		pCPU = new double[pNum];
		pMEM = new double[pNum];
		ePM = new double[pNum];
		newInUse = pNum;

		Random r = getRandom();
		r.setSeed(2000);
		Random rMem = getRandom();
		rMem.setSeed(3000);
		for (int i = 0; i < vNum; i++) {
			double randomRequirement = Math.abs(r.nextInt() % 20) * 100;
			//if (randomRequirement < 0.01)
			//	randomRequirement = 50; // minimum cpu to keep it alive
			vCPU[i] = randomRequirement;

			randomRequirement = Math.abs(rMem.nextInt() % 20) * 100;
			if (randomRequirement < 0.01)
				randomRequirement = 50; // minimum cpu to keep it alive
			vMEM[i] = randomRequirement;
			
			vAssignOld[i] = -1;
			vmNames[i] = "vm"+i;
		}

		sortVM();

		int capacity[] = { 1000, 1200, 1500, 1800, 2000, 2300, 2400, 2500,
				2700, 3000 };
		for (int i = 0; i < pNum; i++) {
			pCPU[i] = capacity[i % 10] * capacityIndexPM;
			if (mem)
				pMEM[i] = pCPU[i]; 
			else
				pMEM[i] = 30000 * capacityIndexPM;
		}
		sortPM();
		
		for (int i = 0; i < pNum; i++) {
			double energyTimes = 1;
			if (pCPU[i] / 1000 < 100) {
				energyTimes = (1 - Math.log10(pCPU[i] / 1000) * 0.4);
			} else {
				throw new RuntimeException("wrong capacity: " + pCPU[i]);
			}

			ePM[i] = (pCPU[i] / 10) * energyTimes; // 1000-> 100*(1-0), 3000->
													// 300 * (1-1/3)
		}
	}
	
	private MigrationProblem getMigrationProblem(){
		MigrationProblem problem = new MigrationProblem();
		for (int i=0;i<pCPU.length;i++){
			PM pm = new PM(i,pCPU[i],ePM[i]);
			pm.setTargetUtilization(targetUtilization);
			
			problem.addPM(pm);
		}
		
		for (int i=0;i<vCPU.length;i++){
			VM vm = new VM(i,"vm"+i,vCPU[i]);
			vm.setMem(vMEM[i]);
			problem.addNonAssignedVm(vm);
		}
		
		return problem;
	}

	@Test
	public void testSolution1() {
		generateProblem(20,3,false);
		EnergySimulationAnneal sa = new EnergySimulationAnneal();
		sa.initScheduler(pCPU, vCPU, vAssignOld, oldInUse, newInUse, targetUtilization, vmNames);
		sa.scheduleMigration();
		//MigrationProblem sol = sa.getSolution();
		//String s = sol.getProblemInfo();
		//System.out.println(s);
		int[] vAssign = sa.getAssignment();
	}
	
	@Test
	public void testSolution2(){
		generateProblem(100, 5, false);
		MigrationProblem problem = getMigrationProblem();
		CoEvSimAnneal sa = new CoEvSimAnneal(true,50,2);
		sa.initScheduler(problem);
		sa.scheduleMigration();
		MigrationProblem sol = sa.getSolution();
		String s = sol.getProblemInfo();
		System.out.println(s);
	}
	
	@Test
	public void testSolutionWithMem() {
		generateProblem(1000,5,true);
		EnergySimulationAnneal sa = new EnergySimulationAnneal();
		sa.setAnnealTime(64);
		sa.initScheduler(pCPU, vCPU, vAssignOld, oldInUse, newInUse, targetUtilization, vmNames);
		sa.initMemory(vMEM);
		sa.scheduleMigration();
		int[] vAssign = sa.getAssignment();
	}
	
	@Test
	public void testSolutionWithMem500() {
		generateProblem(500,10,true);
		EnergySimulationAnneal sa = new EnergySimulationAnneal();
		sa.setAnnealTime(64);
		sa.initScheduler(pCPU, vCPU, vAssignOld, oldInUse, newInUse, targetUtilization, vmNames);
		sa.initMemory(vMEM);
		sa.scheduleMigration();
		int[] vAssign = sa.getAssignment();
	}
	
	private Date begin ;
	@Before
	public void setBeginTime(){
		begin = new Date();
	}
	
	@After
	public void showDuration(){
		Date now = new Date();
		System.out.println("time lapsed:"+(now.getTime() - begin.getTime())/1000);
	}
	
	@Test
	public void testCoEvSolutionWithMem1000(){
		generateProblem(1000, 5, true);
		MigrationProblem problem = getMigrationProblem();
		CoEvSimAnneal sa = new CoEvSimAnneal(false,100,3);
		sa.initScheduler(problem);
		sa.scheduleMigration();
		MigrationProblem sol = sa.getSolution();
		String s = sol.getProblemInfo();
		System.out.println(s);
	}
	
	@Test
	public void testCoEvSolutionWithMem200(){
		generateProblem(200, 10, true);
		MigrationProblem problem = getMigrationProblem();
		CoEvSimAnneal sa = new CoEvSimAnneal(false,100,3);
		sa.initScheduler(problem);
		sa.scheduleMigration();
		MigrationProblem sol = sa.getSolution();
		String s = sol.getProblemInfo();
		System.out.println(s);
	}
	
	@Test
	public void testCoEvSolutionWithMem500(){
		generateProblem(500, 10, true);
		MigrationProblem problem = getMigrationProblem();
		CoEvSimAnneal sa = new CoEvSimAnneal(false,100,3);
		sa.initScheduler(problem);
		sa.scheduleMigration();
		MigrationProblem sol = sa.getSolution();
		String s = sol.getProblemInfo();
		System.out.println(s);
	}
	
	@Test
	public void testRemoveFromList(){
		List<String> list = new ArrayList<String>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		
		Iterator<String> it = list.iterator();
		while(it.hasNext()){
			String cur = it.next();
			//list.remove(cur);
			System.out.println(cur);
		}
		
		for(int i=list.size()-1;i>=0;i--){
			String cur = list.get(i);
			list.remove(cur);
			System.out.println(cur);
		}
		Assert.assertEquals(0, list.size());
	}
}
