package cloud;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrey on 1/8/15.
 */
public class VmAllocationPolicyFirstFit extends VmAllocationPolicy {


    /** The vm table. */
    private Map<String, Host> vmTable;

    /**
     * Creates the new VmAllocationPolicySimple object.
     *
     * @param list the list
     * @pre $none
     * @post $none
     */
    public VmAllocationPolicyFirstFit(List<? extends Host> list) {
        super(list);
        setVmTable(new HashMap<String, Host>());
    }

    /**
     * Sets the vm table.
     *
     * @param vmTable the vm table
     */
    protected void setVmTable(Map<String, Host> vmTable) {
        this.vmTable = vmTable;
    }
    /**
     * Gets the vm table.
     *
     * @return the vm table
     */
    public Map<String, Host> getVmTable() {
        return vmTable;
    }
    private void printLogMsg(String msg) {
        Log.print("FF_Allocator: " + msg + "\n");
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        int idx = 0;
        for (Host host : getHostList()) {
            idx++;
            if(host.isSuitableForVm(vm)) {
                boolean result = host.vmCreate(vm);
                if(result) {
                    printLogMsg("Vm:"+vm.getId()+ "Ram: "+vm.getRam() +" Allocated on " + host.getId());
                    getVmTable().put(vm.getUid(), host);
                    return true;
                } else {
                    printLogMsg("Vm creation failed on " + idx + " Lets try another host");
                    continue;
                }
            }
        }
        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        printLogMsg("Allocate specified host for vm");
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = getVmTable().remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
        }
    }

    @Override
    public Host getHost(Vm vm){
        return getVmTable().get(vm.getUid());
    }

    @Override
    public Host getHost(int vmId, int userId) {
        return getVmTable().get(Vm.getUid(userId, vmId));
    }
}

