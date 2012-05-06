package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;

import org.cloudbus.cloudsim.Log;

import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;

import org.cloudbus.cloudsim.Vm;

import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;

import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerPe;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySingleThreshold;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * environmental setup
 *1) 100 heterogeneous physical nodes. Each node is modeled to have one CPU core with performance
 *   equivalent to 1000, 2000, 3000 MIPS, 8Gb Ram, and 1TB of stroage
 *2) linear power consumption model. 175W with 0% CPU utilization, and 250W with 100% CPU 
 *   utilization
 *3) VM. Each VM requires one CPU core with 250, 500, 750 or 1000MIPS, 128 MB of RAM and 1GB of
 *   storage. 290 heterogeneous VMs that fills the full capacity of the simulated data center. 
 *4) Application. Each VM runs an application  with variable workload, with the utilization of 
 *   CPU according to a uniformly distributed random variable.
 *5) Simulation Time. The application runs for 150,000 MIPS that equals to 10 minutes on 250 MIPS 
 *   CPU with 100% utilization
 *6) Initial allocation. In the beginning, the VMs are allocated according to the requested characteristics 
 *   assuming 100% utilization.
 */
public class BuyyaSingleThreshhold {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {
		
		Log.printLine("Starting CloudSimExample1...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = true; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			PowerDatacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm>();

			createVMs(vmlist, brokerId);
						
			// submit vm list to the broker
			broker.submitVmList(vmlist);

			// Fifth step: Create one Cloudlet
			cloudletList = new ArrayList<Cloudlet>();

			createCloudlets(cloudletList, vmlist, brokerId);			

			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			//Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);

			// Print the debt of each user to each datacenter
			datacenter0.printDebts();
			
			datacenter0.getHostList().get(0);

			Log.printLine("CloudSimExample1 finished!");
			
			 int totalTotalRequested = 0;
			    int totalTotalAllocated = 0;
			    ArrayList<Double> sla = new ArrayList<Double>();
			    int numberOfAllocations = 0;
				for (Entry<String, List<List<Double>>> entry : datacenter0.getUnderAllocatedMips().entrySet()) {
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
				//Log.printLine(String.format("Total simulation time: %.2f sec", lastClock));
				Log.printLine(String.format("Energy consumption: %.2f kWh", datacenter0.getPower() / (3600 * 1000)));
				Log.printLine(String.format("Number of VM migrations: %d", datacenter0.getMigrationCount()));
				Log.printLine(String.format("Number of SLA violations: %d", sla.size()));
				Log.printLine(String.format("SLA violation percentage: %.2f%%", (double) sla.size() * 100 / numberOfAllocations));
				Log.printLine(String.format("Average SLA violation: %.2f%%", averageSla));
				Log.printLine();

			Log.printLine("SingleThreshold finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	


	private static void createCloudlets(List<Cloudlet> cloudletList,
			List<Vm> vmlist, int brokerId) {
		int id = 0;
		java.util.Iterator<Vm> itvm = vmlist.iterator();
		while(itvm.hasNext()){
			Vm vm = itvm.next(); 
			
			// Cloudlet properties		
			int pesNumber = 1;
			long length =   (long) (10*1*vm.getMips());
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelUniform();
	
			Cloudlet cloudlet = new Cloudlet(id++, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(vm.getId());
	
			// add the cloudlet to the list
			cloudletList.add(cloudlet);		
		}
			
	}

	private static void createVMs(List<Vm> vmlist,int brokerId) {		
		int vmid = 0;
		for (int i=0;i<1;i++){ //10
			createOneVM(vmlist, brokerId, vmid++, 250);
		}
			
		for (int i=0;i<1;i++){ //10
			createOneVM(vmlist, brokerId, vmid++, 250*2);
		}
		
		for (int i=0;i<1;i++){ //10
			createOneVM(vmlist, brokerId, vmid++, 250*3);
		}
	}
	
	private static void createOneVM(List<Vm> vmlist, int brokerId, int vmid, int mips) {
		// VM description		
		long size = 1000; // image size (MB)
		int ram = 125; // vm memory (MB)
		long bw = 10;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		// create VM
		Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm,
				new CloudletSchedulerDynamicWorkload(mips, pesNumber,ram));

		// add the VM to the vmList
		vmlist.add(vm);		
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static PowerDatacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		ArrayList<PowerHost> hostList = new ArrayList<PowerHost>();;

		creatHostList(hostList); // This is our machine

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		PowerDatacenter datacenter = null;
		try {
			double utilizationThreshold = 0.9;
			datacenter = new PowerDatacenter(name, 
					characteristics, 
					new PowerVmAllocationPolicySingleThreshold(hostList, utilizationThreshold), storageList, 5.0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	private static void creatHostList(ArrayList<PowerHost> hostList) {
		
		//1. totally 100 hosts, 60 with 1000 mips, 30 with 2000 mips, 10 with 3000mips
		int hostId = 0;
		
		for (int i=0;i<1;i++){ //6
			creaeOneHost(hostList, hostId++, 1000, 8000, 1000000);			
		}
		
		for (int i=0;i<1;i++){ //3
			creaeOneHost(hostList, hostId++, 2000, 8000, 1000000);			
		}
		
		for (int i=0;i<1;i++){ //1
			creaeOneHost(hostList, hostId++, 3000, 8000, 1000000);			
		}
	}

	private static void creaeOneHost(ArrayList<PowerHost> hostList, int hostId, int mips, int ram, int storage) {
		// 0. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		double maxPower = 250; // 250W
		double staticPowerPercent = 0.7; // 70%

		int bw = 100000;

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		// 3. Create PEs and add these into an object of PowerPeList.
		List<PowerPe> peList = new ArrayList<PowerPe>();
		peList.add(new PowerPe(0, new PeProvisionerSimple(mips), new PowerModelLinear(maxPower, staticPowerPercent))); // need to store PowerPe id and MIPS Rating

		// 4. Create PowerHost with its id and list of PEs and add them to the list of machines
		
		hostList.add(
			new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerTimeShared(peList)
			)
		); // This is our machine
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

}
