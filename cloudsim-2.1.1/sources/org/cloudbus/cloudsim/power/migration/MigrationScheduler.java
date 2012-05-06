package org.cloudbus.cloudsim.power.migration;


import org.cloudbus.cloudsim.util.LogPrint;

public abstract class MigrationScheduler implements MigrationScheduleInt {
	
	protected MigrationProblem originalProblem;
	
	protected MigrationProblem workProblem;
	
	protected MigrationProblem bestSolution;
	
	protected boolean init = false;

	public MigrationScheduler(){
		originalProblem = new MigrationProblem();
	}
	
	protected void printProblem() {
		printConsole("intial problem info ");
	 	printConsole(originalProblem.getProblemInfo());
	}
	
	public void initScheduler(double[] pCPU, double[] pVM, int[] vAssignOld, int oldPMInUse, int newPMInUse, double targetUtilization, String[] vmNames) {
		
		if (newPMInUse>pVM.length) throw new RuntimeException("newPMInUse>pVM.length");
		
		originalProblem.clear();
		
		for(int i=0;i<pCPU.length;i++){
			
			PM pm = new PM(i,pCPU[i],pCPU[i]/100);
			pm.setTargetUtilization(targetUtilization);
			
			originalProblem.addPM(pm);
		}
		
		for (int i= 0;i<pVM.length;i++){
			String name = "vm"+i;
			if (vmNames!=null && vmNames[i]!=null) name = vmNames[i];
			VM vm = new VM(i,name,pVM[i]);
			if (vAssignOld[i] < newPMInUse)
				originalProblem.getPM(vAssignOld[i]).addVm(vm);
			else 
				originalProblem.addNonAssignedVm(vm);
		}		
		
		init = true;
		printProblem();
	}
	
	public void initScheduler(MigrationProblem problem){
		this.originalProblem = problem;
		init = true;
	}

	
	LogPrint logPrint = new LogPrint(this.getClass().getName());
	void print(String message){
		logPrint.print(message,LogPrint.PrintMode.PrintLog);
	}
	
	void printConsole(String message){
		logPrint.print(message,LogPrint.PrintMode.PrintOnly);
	}

	

	@Override
	public int[] getAssignment() {
		
		if (bestSolution!=null)
			return bestSolution.getAssignment();
		else
			return originalProblem.getAssignment();
	}	
	
	@Override
	abstract public void scheduleMigration();

	protected void printWorkProblem() {
		printConsole(workProblem.getProblemInfo());
		printConsole("migrations = "+workProblem.getMigrationCount(originalProblem));
	}

	@Override
	public MigrationProblem getSolution() {
		if (bestSolution!=null)
			return bestSolution;
		else
			return originalProblem;
	}

}
