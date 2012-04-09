package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.power.migration.MigrationProblem;
import org.cloudbus.cloudsim.power.migration.MigrationScheduleInt;
import org.cloudbus.cloudsim.power.migration.MigrationScheduler;
import org.cloudbus.cloudsim.power.migration.PM;
import org.cloudbus.cloudsim.power.migration.VM;

public class CoEvSimAnneal extends MigrationScheduler {

	private MigrationProblem problem1 = null;
	private MigrationProblem problem2 = null;
	private MigrationProblem solution = null;

	private MigrationScheduleInt task1 = null;
	private MigrationScheduleInt task2 = null;

	private boolean confSimpleCombine = false;
	private int groupNum = 50;
	private int grEvTimeLimit = 2;

	public CoEvSimAnneal(boolean confSimpleCombine,int groupNum,int grEvTimeLimit){
		super();
		this.confSimpleCombine = confSimpleCombine;
		this.groupNum = groupNum;
		this.grEvTimeLimit = grEvTimeLimit;
	}
	@Override
	public void scheduleMigration() {

		devideProblem();

		executeSubTasks();

		combineSolution();
	}
	
	public void setSimpleCombine(boolean v){
		confSimpleCombine = v;
	}

	private void executeSubTasks() {
		if (task1 != null) {
			task1.scheduleMigration();
		}
		if (task2 != null) {
			task2.scheduleMigration();
		}
	}

	private void combineSolution() {
		if (task2 == null) {
			solution = task1.getSolution();
		} else {
			MigrationProblem solution1 = task1.getSolution();
			// System.out.println("solution1:"+solution1.getPmCount());
			MigrationProblem solution2 = task2.getSolution();
			// System.out.println("solutions2:"+solution2.getPmCount());
			simplyCombineSolutions(solution1, solution2);

			if (!confSimpleCombine) {
				// to find the low-utilization PMs and to re-assign

				// move the underutilized PMs to solution2
				List<PM> pmList = solution1.sortPmByUtilization();
				int toPickPM = (int) (pmList.size() * 0.20);
				int pickedPM = 0;
				for (int i = pmList.size() - 1; i >= 0; i--) {
					PM pm = pmList.get(i);
					if (pm.getUtilizationCPU() > 0.00001) {
						solution1.removePM(pm);
						solution2.addPM(pm);
						pickedPM++;
					}
					if (pickedPM == toPickPM)
						break;
				}
				// pick up some empty PMs if any
				toPickPM = (int) (pmList.size() * 0.10);
				pickedPM = 0;
				for (int i = pmList.size() - 1; i >= 0; i--) {
					PM pm = pmList.get(i);
					if (pm.getUtilizationCPU() < 0.00001) {
						solution1.removePM(pm);
						solution2.addPM(pm);
						pickedPM++;
					}
					if (pickedPM == toPickPM)
						break;
				}

				for (int i = solution1.getNonAssignedVmCount() - 1; i >= 0; i--) {
					VM vm = solution1.getNonAssignedVm(i);
					solution1.removeNonAssignedVm(vm);
					solution2.addNonAssignedVm(vm);
				}

				CoEvSimAnneal sa = new CoEvSimAnneal(confSimpleCombine,groupNum,grEvTimeLimit);
				solution2.setName(problem2.getName() + "-combine1");
				System.out.println(solution2.getName());
				sa.initScheduler(solution2);
				sa.scheduleMigration();
				solution2 = sa.getSolution();
				// System.out.println("solutions2-2:"+solution2.getPmCount());
				simplyCombineSolutions(solution1, solution2);
			}
			solution = solution1;
		}
	}

	private void simplyCombineSolutions(MigrationProblem solution1,
			MigrationProblem solution2) {
		for (int i = 0; i < solution2.getPmCount(); i++) {
			PM pm = solution2.getPM(i);
			solution1.addPM(pm);
		}

		for (int i = 0; i < solution2.getNonAssignedVmCount(); i++) {
			VM vm = solution2.getNonAssignedVm(i);
			solution1.addNonAssignedVm(vm);
		}

		solution2.clear();
	}

	private void devideProblem() {
		// double[] pCPU, double[] pVM, int[] vAssignOld,
		// int oldPMInUse, int newPMInUse, double targetUtilization, String[]
		// vmNames
		if (originalProblem.getTotalVmCount() > groupNum) {
			// split them into 2 sub-problems
			problem1 = new MigrationProblem();
			problem1.setName(originalProblem.getName() + "-sub1");
			System.out.println(problem1.getName());
			problem2 = new MigrationProblem();
			problem2.setName(originalProblem.getName() + "-sub2");
			System.out.println(problem2.getName());
			MigrationProblem[] problems = { problem1, problem2 };
			for (int i = 0; i < originalProblem.getPmCount(); i++) {
				PM pm = originalProblem.getPM(i).clone();
				problems[i % 2].addPM(pm);
			}

			for (int i = 0; i < originalProblem.getNonAssignedVmCount(); i++) {
				VM vm = originalProblem.getNonAssignedVm(i).clone();
				problems[i % 2].addNonAssignedVm(vm);
			}
		} else {
			problem1 = originalProblem;
			System.out.println(problem1.getName());
		}

		initSubProblems();
	}

	private void initSubProblems() {
		if (problem2 != null) {
			task2 = new CoEvSimAnneal(confSimpleCombine,groupNum,grEvTimeLimit);
			task2.initScheduler(problem2);

			task1 = new CoEvSimAnneal(confSimpleCombine,groupNum,grEvTimeLimit);
			task1.initScheduler(problem1);
		} else {
			task1 = new EnergySimulationAnneal(grEvTimeLimit);
			task1.initScheduler(problem1);
		}
	}

	@Override
	public MigrationProblem getSolution() {
		return solution;
	}
}
