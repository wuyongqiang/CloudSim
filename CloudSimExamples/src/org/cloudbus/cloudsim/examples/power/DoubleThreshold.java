package org.cloudbus.cloudsim.examples.power;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.UtilizationModelWorkHour;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerPe;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyDoubleThreshold;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySingleThreshold;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class DoubleThreshold extends SingleThreshold {

	private static double utilizationLowThreshold = 0.4;

	public static void main(String[] args) throws IOException {

		Log.setOutputFile("C:\\Users\\n7682905\\sim.txt");
		Log.printLine("Starting SingleThreshold example...");
		
		if ( args.length >=2){
			utilizationThreshold = Double.parseDouble(args[0]);
			utilizationLowThreshold = Double.parseDouble(args[1]);
		}

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities. We can't run this example without
			// initializing CloudSim first. We will get run-time exception
			// error.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace GridSim events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			PowerDatacenter datacenter = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmList = createVms(brokerId);

			// submit vm list to the broker
			broker.submitVmList(vmList);

			// Fifth step: Create one cloudlet
			cloudletList = createCloudletList(brokerId);

			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
			double lastClock = CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			printCloudletList(newList);

		    int totalTotalRequested = 0;
		    int totalTotalAllocated = 0;
		    ArrayList<Double> sla = new ArrayList<Double>();
		    int numberOfAllocations = 0;
			for (Entry<String, List<List<Double>>> entry : datacenter.getUnderAllocatedMips().entrySet()) {
			    List<List<Double>> underAllocatedMips = entry.getValue();
			    double totalRequested = 0;
			    double totalAllocated = 0;
			    for (List<Double> mips : underAllocatedMips) {
			    	if (mips.get(0) != 0) {
			    		numberOfAllocations++;
			    		totalRequested += mips.get(0);
			    		totalAllocated += mips.get(1);
			    		double _sla = (mips.get(0) - mips.get(1)) / mips.get(0) * 100;
			    		if (_sla > 0) {
			    			sla.add(_sla);
			    		}
			    	}
				}
			    totalTotalRequested += totalRequested;
			    totalTotalAllocated += totalAllocated;
			}

			double averageSla = 0;
			if (sla.size() > 0) {
			    double totalSla = 0;
			    for (Double _sla : sla) {
			    	totalSla += _sla;
				}
			    averageSla = totalSla / sla.size();
			}

			Log.printLine();
			Log.printLine(String.format("Total simulation time: %.2f sec", lastClock));
			Log.printLine(String.format("Energy consumption: %.4f kWh", datacenter.getPower() / (3600 * 1000)));
			Log.printLine(String.format("Number of VM migrations: %d", datacenter.getMigrationCount()));
			Log.printLine(String.format("Number of SLA violations: %d", sla.size()));
			Log.printLine(String.format("SLA violation percentage: %.2f%%", (double) sla.size() * 100 / numberOfAllocations));
			Log.printLine(String.format("Average SLA violation: %.2f%%", averageSla));
			Log.printLine(String.format("Turn On times: %d", datacenter.getTurnOnTimes()));
			Log.printLine(String.format("Turn Off times: %d", datacenter.getTurnOffTimes()));
			Log.printLine();
			
			Log.printLineToInfoFile(datacenter.getVmAllocationPolicy().getPolicyDesc(),simLength, 
					datacenter.getMigrationCount(),
					(double) sla.size() * 100 / numberOfAllocations,
					averageSla,
					datacenter.getPower() / (3600 * 1000));
			utilizationModelStochastic.saveHistory("c:\\users\\n7682905\\simWorkload.txt");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		Log.printLine("DoubleThreshold finished!");
		System.out.println("DoubleThreshold finished!");
	}
	
	protected static PowerDatacenter createDatacenter(String name) throws Exception {
		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create an object of HostList2 to store
		// our machine
		List<PowerHost> hostList = new ArrayList<PowerHost>();

		double maxPower = 250; // 250W
		double staticPowerPercent = 0.7; // 70%

		int[] mips = { 1000, 2000, 3000 };
		int ram = 10000; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 100000;

		for (int i = 0; i < hostsNumber; i++) {
			// 2. A Machine contains one or more PEs or CPUs/Cores.
			// In this example, it will have only one core.
			// 3. Create PEs and add these into an object of PowerPeList.
			List<PowerPe> peList = new ArrayList<PowerPe>();
			peList.add(new PowerPe(0, new PeProvisionerSimple(mips[i % mips.length]), new PowerModelLinear(maxPower+ 100 *  (i % mips.length), staticPowerPercent))); // need to store PowerPe id and MIPS Rating

			// 4. Create PowerHost with its id and list of PEs and add them to the list of machines
			hostList.add(
				new PowerHost(
					i,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList)
				)
			); // This is our machine
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a Grid resource: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/PowerPe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		PowerDatacenter powerDatacenter = null;
		try {
			powerDatacenter = new PowerDatacenter(
					name,
					characteristics,
					new PowerVmAllocationPolicyDoubleThreshold(hostList, utilizationThreshold,utilizationLowThreshold),
					new LinkedList<Storage>(),
					5.0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return powerDatacenter;
	}
}
