package org.cloudbus.cloudsim.examples.test;

import static org.junit.Assert.*;

import org.cloudbus.cloudsim.power.SimulationAnneal;
import org.cloudbus.cloudsim.power.migration.MigrationSchedulerFFD;
import org.junit.Test;

public class TestShedulerFFD {
	
	double[] pCPU = {5,5,5,5,3,2};
	double[] pVM = {1,1,1,1,1,2,2,2,2,2};	
	int[] vAssignOld = {0,0,0,0,1,1,2,2,3,3};
	int oldPMInUse = 4;
	int newPMInUse = 4;
	double targetUtilization = 0.81;
	
	MigrationSchedulerFFD scheduler= new MigrationSchedulerFFD();
	
	@Test
	public void testNoMigration() {
		
		
		
		scheduler.initScheduler(pCPU, pVM, vAssignOld, oldPMInUse, newPMInUse,
				targetUtilization,null) ;
		scheduler.scheduleMigration();
		int[] result = scheduler.getAssignment();
		assertTrue(result.length==vAssignOld.length);
		assertEquals(0, compareList(result,vAssignOld));
	}
	
	private int compareList(int[] a, int[] b){
		int aLen = a.length;
		int bLen = b.length;
		int diff = 0;
		
		for (int i=0;i<aLen;i++){
			if(a[i]!=b[i]) diff++;
		}
		return diff;
	}
	
	@Test
	public void test2Migrations() {
		//{1,1,1,1,1,2,2,2,2,2};
		int vAssignOldHere[] = {0,0,2,2,2,2,1,1,3,0};
		//expected  {0,0,0,2,2,2,1,1,3,3}
		
		scheduler.initScheduler(pCPU, pVM, vAssignOldHere, oldPMInUse, newPMInUse,
				targetUtilization,null) ;
		scheduler.scheduleMigration();
		int[] result = scheduler.getAssignment();
		assertTrue(result.length==vAssignOld.length);
		int migrations = compareList(result,vAssignOldHere);
		System.out.println("migrations="+migrations);
		assertEquals(true, migrations<=2);
	}
	
	@Test
	public void test3Migrations() {
		int vAssignOldHere[] = {0,0,0,0,2,1,2,2,0,0};
		
		scheduler.initScheduler(pCPU, pVM, vAssignOldHere, oldPMInUse, newPMInUse,
				targetUtilization,null) ;
		scheduler.scheduleMigration();
		int[] result = scheduler.getAssignment();
		assertTrue(result.length==vAssignOld.length);
		assertEquals(3, compareList(result,vAssignOldHere));
	}
	
	
	@Test
	public void test1Migration() {
		int vAssignOldHere[] = {0,0,0,0,2,1,2,2,3,3};
		
		scheduler.initScheduler(pCPU, pVM, vAssignOldHere, oldPMInUse, newPMInUse,
				targetUtilization,null) ;
		scheduler.scheduleMigration();
		int[] result = scheduler.getAssignment();
		assertTrue(result.length==vAssignOld.length);
		assertEquals(1, compareList(result,vAssignOldHere));
	}
	
	

}
