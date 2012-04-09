package org.cloudbus.cloudsim.power.migration;

public interface MigrationScheduleInt {
	public void scheduleMigration();
	public int[] getAssignment();
	public void initScheduler(double[] pCPU, double[] pVM, int[] vAssignOld, int oldPMInUse, int newPMInUse, double targetUtilization, String[] vmNames);
	public void initScheduler(MigrationProblem problem);
	public MigrationProblem getSolution();
}
