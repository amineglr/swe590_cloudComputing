package cloud;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class DataCenterScenerio {

	private static List<Cloudlet> cloudletList;

	
	private static List<Vm> vmlist;
	
	private static List<Vm> createVM(int userId, int vms) {

		
		LinkedList<Vm> list = new LinkedList<Vm>();
		long size = 10000;
		int ram = 128;
		int mips = 1000;
		long bw = 1000;
		int pesNumber = 1; 
		String vmm = "Xen"; 

		
		Vm[] vm = new Vm[vms];
		

		for(int i=0;i<vms;i++){
			vm[i] = new Vm(i, userId, mips + (i*200), pesNumber, ram, bw , size , vmm, new CloudletSchedulerTimeShared());
			list.add(vm[i]);
			vm[i].getHost();
		}

		return list;
	}


	private static List<Cloudlet> createCloudlet(int userId, int cloudlets){
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		
		long length = 1000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

	public static void main(String[] args) {
		Log.printLine("Starting Simulation...");

		try {
		
			int num_user = 1;   
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; 

			
			CloudSim.init(num_user, calendar, trace_flag);

			
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

		
			vmlist = createVM(brokerId,5); 
			cloudletList = createCloudlet(brokerId,40); 

			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			
			CloudSim.startSimulation();

			
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList);

			Log.printLine("Simulation finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}
	private static Datacenter createDatacenter(String name){

		List<Host> hostList = new ArrayList<Host>();

		
		List<Pe> peList1 = new ArrayList<Pe>();
	
		int mips = 2600;
		
		peList1.add(new Pe(1,new PeProvisionerSimple(mips))); 
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));
		int hostId=0;
		int ram = 512; 
		long storage = 1000000; 
		int bw = 10000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); 

			
		hostId++;
			  
		hostList.add( new Host( 
				hostId, 
				new RamProvisionerSimple(ram), 
				new BwProvisionerSimple(bw), 
				storage, 
				peList2, 
				new VmSchedulerTimeShared(peList2)
			  ) ); 
		
		String arch = "x86";     
		String os = "Linux";          
		String vmm = "Xen";
		double time_zone = 10.0;         
		double cost = 3.0;              
		double costPerMem = 0.05;		
		double costPerStorage = 0.1;	
		double costPerBw = 0.1;			
		LinkedList<Storage> storageList = new LinkedList<Storage>();	

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		Datacenter datacenter = null;
		try {
			
			//First fit
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyFirstFit(hostList), storageList,0);
			
			//RoundRobin 
			//datacenter = new Datacenter(name, characteristics, new RoundRobinVmAllocationPolicy(hostList), storageList,0);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	private static DatacenterBroker createBroker(){

		
		DatacenterBroker broker = null;
		
		try {
			broker = new DatacenterBroker("Broker");
		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
		
		String indent = "    ";
		
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent + "CPU Time" + indent + "Start Time" + indent + "Finish Time" + indent  +"Waiting Time" + indent + "Turn Around Time");

        double totalCompletionTime=0;
        double totalCost=0;
		DecimalFormat dft = new DecimalFormat("###.##");
		
		
		
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");
				
				double completionTime= cloudlet.getActualCPUTime()+ cloudlet.getWaitingTime();
                double cost= cloudlet.getCostPerSec()* cloudlet.getActualCPUTime() ;
                double turnaroundTime = cloudlet.getFinishTime() - cloudlet.getSubmissionTime();
               
                
	     
				Log.printLine( 
						indent + indent + indent + cloudlet.getResourceId() + 
						indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
						indent + indent + indent + dft.format(cloudlet.getExecStartTime())+ 
						indent + indent + indent + dft.format(cloudlet.getFinishTime())+
						indent + indent + indent + dft.format(cloudlet.getWaitingTime()) +
						indent + indent + indent + dft.format(turnaroundTime));
			    
				totalCompletionTime += completionTime;
                totalCost += cost;
                
			}
		}
	      
	        Log.printLine("Total Completion Time: " + totalCompletionTime );
	        Log.printLine("Avg Completion Time: "+ (totalCompletionTime/size));
	        Log.printLine("Total Cost : " + totalCost);
	        Log.printLine("Avg cost: "+ (totalCost/size));
	        
	}

}
