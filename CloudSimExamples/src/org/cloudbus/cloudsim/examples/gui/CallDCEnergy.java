package org.cloudbus.cloudsim.examples.gui;

import java.awt.AWTEvent;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jgap.gp.function.Ceil;

import examples.maolin.dcenergy.only50.DcEnergy;
import examples.maolin.dcenergy.only50.PrintUtil;

public class CallDCEnergy {
	
	private static String[] myStrSplit(String s, String sep){
		StringTokenizer tokenizer = new StringTokenizer(s,sep);
		String[] params = new String[tokenizer.countTokens()];
		int j=0;
		while(tokenizer.hasMoreElements()){
			params[j++] = tokenizer.nextToken().trim();
		}
		return params;
	}
	
	private static int[] getScales(String s){
		String[] scaleStr = myStrSplit(s,",");
		int[] scales= new int[scaleStr.length];
		int j=0;
		for(int i=0;i<scales.length;i++){
			try{
				int n = Integer.parseInt(scaleStr[i]);
				if (n>0 && n<10000)
					scales[j++] = n;
			}catch(NumberFormatException e){
				
			}
		}
		
		int[] retScales= new int[j];
		for (int i=0;i<j;i++)
			retScales[i] = scales[i];
		return scales;
	}
	public static void call(String argStr, final EgWindow2 frame) throws Exception {	
		//100,200,300,400,500|5|10|0.5|10|c:\tmp|||sdfasdfasdfa||
		String[] args = myStrSplit(argStr, "|");
		if (args.length<6)
			throw new RuntimeException("invalid args:" + args.toString());
		int cIndex = Integer.parseInt(args[1]);
		int times = Integer.parseInt(args[2]);
		double crossoverRate = Double.parseDouble(args[3]);
		int mutationRate = (int)( 10.0 /Double.parseDouble(args[4]) + 0.5);
		String resultsDir = args[5];
				
		
		for (int j =0; j < times; j++) {
			int scales[] = getScales(args[0]);//{ 100,200,300,400,500};
			for (int i = 0; i < scales.length; i++) {
				final String s = "processing vmNo="+scales[i] + " at loop "+(j+1) +" of "+times;
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						frame.setStatus(s);
						
					}
				});
					
			
				PrintUtil.setLogName(scales[i] + "-" + j);
				DcEnergy dc = new DcEnergy();
				dc.setMutationRate(mutationRate);
				dc.setRouletteCrossoverRate(crossoverRate);
				dc.setLogFolder(resultsDir);
				
				int scale = scales[i];
				int capacityIndex = cIndex;//5;
				dc.println(new Date()
						+ String.format(
								" start\nSyntax: DcEnergy <scale=%d> <capacityIndex=%d>",
								scale, capacityIndex));
				dc.optimizeEnergyNetwork(scale, capacityIndex);

				dc.println(new Date() + "End");
			}
		}
	}
}
