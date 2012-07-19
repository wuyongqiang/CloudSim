package org.cloudbus.cloudsim.examples.test;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cloudbus.cloudsim.examples.power.DoubleThresholdTrading;
import org.junit.Assert;
import org.junit.Test;

public class TestSimTrading {
	static public void main(String args[]) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		for(int i=0;i<args.length;i++){
			System.out.println(args[i]);
		}
		if (args.length<4) return;
		String testName = args[0];
		int testTimes = Integer.parseInt(args[1]);
		int testNumberMin = Integer.parseInt(args[2]);
		int testNumberMax = Integer.parseInt(args[3]);
		
		TestSimTrading trading = new TestSimTrading();
		trading.testTimes = testTimes;
		trading.testNumberMin = testNumberMin;
		trading.testNumberMax = testNumberMax;
		Method m = TestSimTrading.class.getMethod(testName);
		m.invoke(trading);		
	}
	
	public int testTimes = 10;
	public int testNumberMin = 1;
	public int testNumberMax = 5;
	
	@Test
	public void testSimWithGroupsDT() throws IOException{
		for (int k=0;k<5;k++){
			DoubleThresholdTrading dtTrading = null;
			boolean generateNewNetCfg = true;
			for (int i=0;i<10;i++){
				dtTrading = new DoubleThresholdTrading();
				dtTrading.useTrading = false;
				dtTrading.groupNum = 10*(k+1);
				dtTrading.dtGroupNum = dtTrading.groupNum;
				dtTrading.isNetworkAware = false;
				dtTrading.isTradeWithinGrps = false;
				
				dtTrading.hostPerGroup = 10;
				dtTrading.workHourLoad = true;
				dtTrading.roughIndex = 3;
				dtTrading.generateNewNetCfg = generateNewNetCfg;
				dtTrading.startSim();
				generateNewNetCfg = false;				
			}
		}
	}
	
	@Test
	public void testSimWithOneGroupDT() throws IOException{
		for (int k=testNumberMin-1;k<testNumberMax;k++){
			DoubleThresholdTrading dtTrading = null;
			boolean generateNewNetCfg = true;
			for (int i=0;i<testTimes;i++){
				dtTrading = new DoubleThresholdTrading();
				dtTrading.useTrading = false;
				dtTrading.groupNum = 10*(k+1);
				dtTrading.dtGroupNum = 1;
				dtTrading.isNetworkAware = false;
				dtTrading.isTradeWithinGrps = false;
				
				dtTrading.hostPerGroup = 10;
				dtTrading.workHourLoad = true;
				dtTrading.roughIndex = 3;
				dtTrading.generateNewNetCfg = generateNewNetCfg;
				dtTrading.startSim();
				generateNewNetCfg = false;
				
			}
		}
	}
	
	@Test
	public void testSimWithGroupsTrading() throws IOException{
		for (int k=0;k<5;k++){
			DoubleThresholdTrading dtTrading = null;
			boolean generateNewNetCfg = true;
			for (int i=0;i<10;i++){
				dtTrading = new DoubleThresholdTrading();
				dtTrading.useTrading = true;
				dtTrading.groupNum = 10*(k+1);
				dtTrading.dtGroupNum = 1;
				dtTrading.isNetworkAware = false;
				dtTrading.isTradeWithinGrps = true;
				
				dtTrading.hostPerGroup = 10;
				dtTrading.workHourLoad = true;
				dtTrading.roughIndex = 3;
				dtTrading.generateNewNetCfg = generateNewNetCfg;
				dtTrading.startSim();
				generateNewNetCfg = false;
			}
		}
	}
	
	@Test
	public void testSimAccossGroupsTrading() throws IOException{
		for (int k=1;k<5;k++){
			DoubleThresholdTrading dtTrading = null;
			boolean generateNewNetCfg = true;
			for (int i=0;i<5;i++){
				dtTrading = new DoubleThresholdTrading();
				dtTrading.useTrading = true;
				dtTrading.groupNum = 10*(k+1);
				dtTrading.dtGroupNum = 1;
				dtTrading.isNetworkAware = false;
				dtTrading.isTradeWithinGrps = false;
				
				dtTrading.hostPerGroup = 10;
				dtTrading.workHourLoad = true;
				dtTrading.roughIndex = 3;
				dtTrading.generateNewNetCfg = generateNewNetCfg;
				dtTrading.startSim();
				generateNewNetCfg = false;
			}
		}
	}
	
	@Test
	public void testSimAccossGroupsTradingNetwork() throws IOException{
		for (int k=3;k<5;k++){
			DoubleThresholdTrading dtTrading = null;
			boolean generateNewNetCfg = true;
			for (int i=0;i<5;i++){
				dtTrading = new DoubleThresholdTrading();
				dtTrading.useTrading = true;
				dtTrading.groupNum = 10*(k+1);
				dtTrading.dtGroupNum = 1;
				dtTrading.isNetworkAware = true;
				dtTrading.isTradeWithinGrps = false;
				
				dtTrading.hostPerGroup = 10;
				dtTrading.workHourLoad = true;
				dtTrading.roughIndex = 3;
				dtTrading.generateNewNetCfg = generateNewNetCfg;
				dtTrading.startSim();
				generateNewNetCfg = false;
				
			}
		}
	}
}
