package org.cloudbus.cloudsim.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogPrint {
	
	public enum PrintMode {
		PrintLog,
		PrintOnly,
		LogOnly;
	}
	
	private String fileName;
	private String resultFolder = "results";
	
	public LogPrint(String fileName){
		this.fileName = fileName;
	}
	
	public void print(String s, PrintMode mode){
		
		Date now = new Date();		
		
		s = now.toString() +": "+s;
		
		if (mode==PrintMode.PrintOnly){
			System.out.println(s);			
		}else
		if (mode==PrintMode.LogOnly){
			writeText(fileName,s);			
		}
		else{
			System.out.println(s);
			writeText(fileName,s);
		}
	}
	
	private void createFileIfNotExist(String filePath) throws IOException {
		File f;
		f = new File(filePath);
		if (!f.exists()){
			f.createNewFile();
		}
	}
	
	private void createFolderIfNotExist(String filePath) throws IOException {
		File f;
		f = new File(filePath);
		if (!f.exists()){
			f.mkdir();
		}
	}
	
	private void writeText(String fFileName,String message) {
		String folder = "C:\\users\\n7682905\\" + resultFolder+"\\";
	
		try {
			createFolderIfNotExist(folder);
			
			fFileName = folder +"\\" +  fFileName;
			
			createFileIfNotExist(fFileName);

			Writer out = new OutputStreamWriter(
					new FileOutputStream(fFileName, true), "utf8");
			try {
				out.append(message+"\r\n");
			} finally {
				out.close();
			}
		} catch (Exception e) {
		}
	}

}
