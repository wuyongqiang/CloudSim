/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;

/**
 * The Log class used for performing loggin of the simulation process.
 * It provides the ability to substitute the output stream by any
 * OutputStream subclass.
 *
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class Log {

	/** The Constant LINE_SEPARATOR. */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/** The output. */
	private static OutputStream output;
	
	private static OutputStream outputDetail;
	
	private static OutputStream outputInfo;
	
	private static OutputStream outputVm;

	/** The disable output flag. */
	private static boolean disabled;
	
	private static String logFilePath;
	
	private static String infoFilePath;
	
	private static String vmFilePath;
	
	private static int logSimId;
	
	private static String logSimIdFilePath;

	

	final private static String fEncoding = "utf8";

	/**
	 * Prints the message.
	 *
	 * @param message the message
	 */
	public static void print(String message) {
		if (!isDisabled()) {
			try {
				getOutput().write(message.getBytes());
				if (getOutput() != System.out){
					System.out.write(message.getBytes());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prints the message passed as a non-String object.
	 *
	 * @param message the message
	 */
	public static void print(Object message) {
		if (!isDisabled()) {
			print(String.valueOf(message));
		}
	}

	public static int getLogSimId() {		
		return logSimId;
	}
	/**
	 * Prints the line.
	 *
	 * @param message the message
	 */
	public static void printLine(String message) {
		if (!isDisabled()) {
			print(message + LINE_SEPARATOR);
		}
	}
	
	public static void printLineToDetailFile(String message) {
		if (!isDisabled()) {
			String messageLs = message + LINE_SEPARATOR;			
			try {
				outputDetail.write(messageLs.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void printLineToInfoFile(String simdesc,int length, int migration,double violation,double avgviolation,double energy) {
		if (!isDisabled()) {
			String message = String.format("%d,%s,%d,%d,%.2f,%.2f,%.2f", logSimId,simdesc,length,migration,violation,avgviolation,energy) + LINE_SEPARATOR;			
			try {
				outputInfo.write(message.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//simid, time, vm, host, utilization
	public static void printLineToVmFile(int time,int vm,int host,double utilization, double vmmips, double hostmips, double usedmips) {
		if (!isDisabled()) {
			String message = String.format("%d,%d,%d,%d,%.2f,%.2f,%.2f,%.2f", logSimId, time, vm, host, utilization, vmmips, hostmips, usedmips) + LINE_SEPARATOR;			
			try {
				outputVm.write(message.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * Prints the empty line.
	 */
	public static void printLine() {
		if (!isDisabled()) {
			print(LINE_SEPARATOR);
		}
	}

	/**
	 * Prints the line passed as a non-String object.
	 *
	 * @param message the message
	 */
	public static void printLine(Object message) {
		if (!isDisabled()) {
			printLine(String.valueOf(message));
		}
	}

	/**
	 * Prints a string formated as in String.format().
	 *
	 * @param format the format
	 * @param args the args
	 */
	public static void format(String format, Object... args ) {
		if (!isDisabled()) {
			print(String.format(format, args));
		}
	}

	/**
	 * Prints a line formated as in String.format().
	 *
	 * @param format the format
	 * @param args the args
	 */
	public static void formatLine(String format, Object... args ) {
		if (!isDisabled()) {
			printLine(String.format(format, args));
		}
	}

	/**
	 * Sets the output.
	 *
	 * @param _output the new output
	 */
	public static void setOutput(OutputStream _output) {
		output = _output;
	}
	
	/**
	 * Sets the output file.
	 *
	 * @param _output the new output
	 * @throws IOException 
	 */
	public static void setOutputFile(String filePath) throws IOException {
		// create log output stream
		createFileIfNotExist(filePath);
		output = new FileOutputStream(filePath);
		
		// create log detail output stream for importing to database
		logFilePath = addFileSuffix(filePath,"Detail");		
		createFileIfNotExist(logFilePath);
		outputDetail = new FileOutputStream(logFilePath);
		
		// create info  output stream for importing to database
		infoFilePath = addFileSuffix(filePath,"Info");		
		createFileIfNotExist(infoFilePath);
		outputInfo = new FileOutputStream(infoFilePath);
		
		// create vm  output stream for importing to database
		vmFilePath = addFileSuffix(filePath,"vm");		
		createFileIfNotExist(vmFilePath);
		outputVm = new FileOutputStream(vmFilePath);
		
		//form the filePath for keeping the simulation id		
		logSimIdFilePath = addFileSuffix(filePath,"Id");
		createFileIfNotExist(logSimIdFilePath);
		
		String idText = readText(logSimIdFilePath).replaceAll(LINE_SEPARATOR, "");
		if (idText==null || idText.length()==0 || idText.equals(LINE_SEPARATOR)){
			logSimId = 1;
			writeText(logSimIdFilePath,String.format("%d", logSimId+1));
		}
		else{
			logSimId = Integer.parseInt(idText);
			writeText(logSimIdFilePath,String.format("%d", logSimId+1));
		}
		
	}

	private static void createFileIfNotExist(String filePath) throws IOException {
		File f;
		f = new File(filePath);
		if (!f.exists()){
			f.createNewFile();
		}
	}

	private static String addFileSuffix(String filePath, String suffix) {
		String suffixFilePath;
		int idx = filePath.lastIndexOf(".");		
		if (idx<0){
			suffixFilePath = filePath+suffix;
		}else{
			suffixFilePath = filePath.substring(0, idx)+suffix+filePath.substring(idx);
		}
		return suffixFilePath;
	}

	/**
	 * Gets the output.
	 *
	 * @return the output
	 */
	public static OutputStream getOutput() {
		if (output == null) {
			setOutput(System.out);
		}
		return output;
	}

	/**
	 * Sets the disable output flag.
	 *
	 * @param _disabled the new disabled
	 */
	public static void setDisabled(boolean _disabled) {
		disabled = _disabled;
	}

	/**
	 * Checks if the output is disabled.
	 *
	 * @return true, if is disable
	 */
	public static boolean isDisabled() {
		return disabled;
	}

	/**
	 * Disables the output.
	 */
	public static void disable() {
		setDisabled(true);
	}

	/**
	 * Enables the output.
	 */
	public static void enable() {
		setDisabled(false);
	}
	
	 /** Write fixed content to the given file. */
	private static void writeText(String fFileName,String message) throws IOException  {
	    Writer out = new OutputStreamWriter(new FileOutputStream(fFileName), fEncoding);
	    try {
	      out.write(message);
	    }
	    finally {
	      out.close();
	    }
	  }
	  
	  /** Read the contents of the given file. */
	private static String readText(String fFileName) throws IOException {	  
		StringBuilder text = new StringBuilder();
	    String NL = LINE_SEPARATOR;
	    Scanner scanner = new Scanner(new FileInputStream(fFileName), fEncoding);
	    try {
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine() + NL);
	      }
	    }
	    finally{
	      scanner.close();
	    }	    
	    return text.toString();
	  }


}
