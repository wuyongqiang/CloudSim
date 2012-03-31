package org.cloudbus.cloudsim.power.migration;

public interface MigrationSchedule {
	public void scheduleMigration();
	public int[] getAssignment();
	public void initScheduler(double[] pCPU, double[] pVM, int[] vAssignOld, int oldPMInUse, int newPMInUse, double targetUtilization, String[] vmNames);
}
