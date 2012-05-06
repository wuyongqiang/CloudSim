package org.cloudbus.cloudsim.examples.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.cloudbus.cloudsim.examples.power.SingleThreshold;
import org.cloudbus.cloudsim.power.SimulationAnneal;
import org.junit.Test;

public class TestSingleThreshold {
	
	final private String batPath = "c:\\users\\n7682905\\reports\\test.bat";
	final private String batRsltPath = "c:\\users\\n7682905\\reports\\test.txt";
	
	final private String importBatPath = "c:\\users\\n7682905\\reports\\import.bat";
	
	
	@Test
	public void testExcBat() throws IOException, InterruptedException {
		
		Process proc = Runtime.getRuntime().exec("cmd /c "+batPath);
		proc.waitFor(); 
		File f = new File(batRsltPath);
		
		assertEquals("file should exit", f.exists(), true);
	}
	@Test
	public void testExcImports() throws IOException, InterruptedException {
		
		Process proc = Runtime.getRuntime().exec("cmd /c "+importBatPath);
		proc.waitFor(); 		
	}
	
	@Test
	public void testSingleThreshold() throws IOException, InterruptedException {
		String[] args = new String[1];
		SingleThreshold.main(args);
		//testExcImports();		
	}
	

}
