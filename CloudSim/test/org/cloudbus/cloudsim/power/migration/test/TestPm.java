package org.cloudbus.cloudsim.power.migration.test;
import junit.framework.Assert;

import  org.cloudbus.cloudsim.power.migration.PM;
import  org.cloudbus.cloudsim.power.migration.VM;
import org.junit.Test;
public class TestPm {
	@Test
	public void test(){
		PM pm = new PM(3, 100, 100);
		
		VM vm1 = new VM(1, "vm1", 400);
		pm.addVm(vm1);
		
		VM vm2 = new VM(2, "vm2", 500);
		pm.addVm(vm2);
		
		VM vm3 = new VM(3, "vm3", 200);
		pm.addVm(vm3);
		
		Assert.assertEquals(3,pm.getVmCount());
		Assert.assertEquals("200", String.format("%.0f", pm.getVm(2).getMips()));
		Assert.assertEquals("vm2", pm.getVm(0).getName());
		Assert.assertEquals("vm3", pm.getVmByNumer(3).getName());
		Assert.assertTrue("contain vm3 expected", pm.getPmInfo().indexOf("vm3")>0);
		System.out.println(pm.getPmInfo());
	}
}
