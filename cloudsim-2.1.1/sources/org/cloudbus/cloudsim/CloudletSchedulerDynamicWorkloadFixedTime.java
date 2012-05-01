package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

public class CloudletSchedulerDynamicWorkloadFixedTime extends
		CloudletSchedulerDynamicWorkload {

	public CloudletSchedulerDynamicWorkloadFixedTime(double mips, int pesNumber, int ram) {
		super(mips, pesNumber,ram);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public double cloudletSubmit(Cloudlet cl, double fileTransferTime) {
		super.cloudletSubmit(cl, fileTransferTime);
		return  cl.getCloudletDuration() + CloudSim.clock();
	}
	
	/**
	 * Updates the processing of cloudlets running under management of this scheduler.
	 *
	 * @param currentTime current simulation time
	 * @param mipsShare array with MIPS share of each Pe available to the scheduler
	 *
	 * @return time predicted completion time of the earliest finishing cloudlet, or 0
	 * if there is no next events
	 *
	 * @pre currentTime >= 0
	 * @post $none
	 */
	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);

		double timeSpan = currentTime - getPreviousTime();
		double nextEvent = Double.MAX_VALUE;
		List<ResCloudlet> cloudletsToFinish = new ArrayList<ResCloudlet>();

		for (ResCloudlet rcl : getCloudletExecList()) {
			rcl.updateCloudletFinishedSoFar((long) (timeSpan * getTotalCurrentAllocatedMipsForCloudlet(rcl, getPreviousTime())));

            if ( rcl.getCloudlet().getCloudletDuration() <= currentTime - rcl.getExecStartTime()  ) { //finished: remove from the list
            	cloudletsToFinish.add(rcl);
                continue;
            } else { //not finish: estimate the finish time
            	double estimatedFinishTime = rcl.getCloudlet().getCloudletDuration() + rcl.getExecStartTime();
				if (estimatedFinishTime - currentTime < 0.1) {
					estimatedFinishTime = currentTime + 0.1;
				}
            	if (estimatedFinishTime < nextEvent) {
            		nextEvent = estimatedFinishTime;            		
            	}          
            }
		}

		for (ResCloudlet rgl : cloudletsToFinish) {
			getCloudletExecList().remove(rgl);
			cloudletFinish(rgl);
		}

		setPreviousTime(currentTime);

		if (getCloudletExecList().isEmpty()) {
			return 0;
		}

		return nextEvent;
	}

}
