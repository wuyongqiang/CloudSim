package org.cloudbus.cloudsim.power.migration;


public class MigrationSchedulerFFD extends MigrationScheduler {

	@Override
	public void scheduleMigration() {
		if (!init){
			print("the problem has not been initialized");
			return;
		}
		
		for (int i = 1;i<10;i++){
			if (migratePartially(i*10)){
				bestSolution = workProblem;	
				printConsole("workable solution find at attempt " + i);
				printWorkProblem();
				return;
			}
			else{
				printConsole("failed attempt " + i);
			}
		}

	}

	private boolean migratePartially(int migrationPencent) {
		pickupToMigrateVMs(migrationPencent);
		printWorkProblem();
		return assignByFFD();
	}

	private boolean assignByFFD() {
		int iVm = 0;
		int iLoop = 0;
		while( workProblem.getNonAssignedVmCount() > 0){
			if (iLoop++ > 10000) 
				throw new RuntimeException("too many loops in assignByFFD");
			iVm = iVm % workProblem.getNonAssignedVmCount();
			VM vm = workProblem.getNonAssignedVm(iVm);
			for (int j=0;j<workProblem.getPmCount();j++){
				PM pm = workProblem.getPM(j);
				if (pm.canAccept(vm)){
					workProblem.removeNonAssignedVm(vm);
					pm.addVm(vm);
					break;
				}
			}
			iVm++;
		}
		return workProblem.getNonAssignedVmCount()==0;
	}



	private void pickupToMigrateVMs(int migrationPencent) {
		workProblem = originalProblem.clone();
		workProblem.pickInfeasibleVms();
		int vmCount = workProblem.getTotalVmCount();
		int toPick = (int)(vmCount * migrationPencent /100.0);
		toPick = toPick<workProblem.getTotalVmCount()?toPick:workProblem.getTotalVmCount();
		int toPickFromPm = toPick - workProblem.getNonAssignedVmCount();
		
		int pmNumber = 0;
		int pickedNum = 0;
		int loopCount = 0;
		while (pickedNum<toPickFromPm){
			if (loopCount ++ > 10000) 
				throw new RuntimeException("too many loops in pickupToMigrateVMs");
			
			VM vm = workProblem.getPM(pmNumber).getSmallestVm();
			if (vm != null) {
				workProblem.getPM(pmNumber).removeVm(vm);
				workProblem.addNonAssignedVm(vm);

				pickedNum++;
			}
			pmNumber++;
			pmNumber = pmNumber % workProblem.getPmCount();			
		}
	}


}
