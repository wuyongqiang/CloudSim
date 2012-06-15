package examples.maolin.dcenergy.only50;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.jfree.io.FileUtilities;
import org.junit.Assert;
import org.junit.Test;

public class ProcessData {
	
	private static final String UTF_8 = "utf-8";
	Map<Integer,ArrayList<Double>> generationMap = new TreeMap<Integer,ArrayList<Double>>();
	void processOneFile(File f) throws IOException{
		processOneFile(f.getAbsolutePath());
	}
	void processOneFile(String s) throws IOException{
		String[] lines = readOneFile(s);
		for (String line : lines){
			if (!line.contains("time:") || !line.contains("fitness value:") )
				continue;
			try{
				int timePos = line.indexOf("time:");
				line = line.substring(timePos);
				StringBuilder sb = new StringBuilder();
				char lastc = ' ';
				for(int i=0;i<line.length();i++){
					char c = line.charAt(i);
					if ( (c>='0' && c<='9') || c=='.' || (c==' '&& lastc != ' ') ){
						sb.append(c);
						lastc = c;
					}					
				}
				String[] figures = sb.toString().split(" ");
				if (figures.length>=6){					
					ArrayList<Double> list = new ArrayList<Double>();
					Integer generation = Integer.parseInt(figures[1]);
					Double time =  Double.parseDouble(figures[0]);
					Double fitness  = Double.parseDouble(figures[2]);
					Double energy = Double.parseDouble(figures[3]);
					Double server = Double.parseDouble(figures[4]);
					Double network = Double.parseDouble(figures[5]);
					
					list.add(time);
					list.add(fitness);
					list.add(energy);
					list.add(server);
					list.add(network);
					list.add(1.0);
					if ( generationMap.get(generation)==null ){											
						generationMap.put(generation, list);
					}
					else{						
						ArrayList<Double> listOld = generationMap.get(generation);
						double count = listOld.get(5);
						for(int i=0;i<5;i++)
							listOld.set(i, (list.get(i) + count*listOld.get(i) )/ (count + 1));
						listOld.set(5, count+1);
					}
					
				}
			}catch(Exception e){
				
			}
		}
	}
	//time:0.0 generation:1 fitness value: 20.6725 total energy:6417.00 energy:5064.00 network:1353.00
	int processOneDir(String dir) throws IOException{
		
		File directory = new File(dir);
		String[] extensions = {"txt"};
		Collection files = FileUtils.listFiles(directory, extensions , false);
		Iterator it = files.iterator();
		int i=0;
		while (it.hasNext()){
			File f = (File) it.next();
			processOneFile(f);
			i++;
		}
		return i;
	}
	
	String[] readOneFile(String s) throws IOException{	
		File f = new File(s);
		String fContent = FileUtils.readFileToString(f, UTF_8);
		return fContent.split("\r\n");
	}
	
	void saveToFile(String s) throws IOException{	
		Iterator<Integer> it = generationMap.keySet().iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()){
			Integer gen = it.next();
		
			ArrayList<Double> list = generationMap.get(gen);
			String line = String.format("%d\t", gen.intValue());
			for (Double d : list){
				line += String.format("%.4f\t", d.doubleValue());
			}
			sb.append(line+"\r\n");
		}
		
		File file = new File(s);
		FileUtils.writeStringToFile(file, sb.toString(), UTF_8);
	}
	@Test
	public void testReadOneFile() throws IOException{
		String path = "C:\\Users\\n7682905\\results\\test-bigger-efficient\\";
		String s = "examples.maolin.dcenergy.only50.PrintUtil50-0.txt";
		
		processOneFile(path+s);
		
		saveToFile(path+"tmp.mydat");
		
		Assert.assertTrue( generationMap.keySet().size()>0);
	}
	
	@Test
	public void testProcessOneDir() throws IOException{
		//String path = "C:\\Users\\n7682905\\results\\test-bigger-efficient\\";
		//String path = "C:\\Users\\n7682905\\results\\test-homo\\";		
		//String path = "C:\\Users\\n7682905\\results\\test-random-energy\\";
		
		//String path = "C:\\Users\\n7682905\\results\\test100\\";
		//String path = "C:\\Users\\n7682905\\results\\test500\\";
		//String path = "C:\\Users\\n7682905\\results\\test-smallgroups\\";
		//String path = "C:\\Users\\n7682905\\results\\test-largergroup\\";
		//String path = "C:\\Users\\n7682905\\results\\test-nonetwork\\";
		//String path = "C:\\Users\\n7682905\\results\\test-noenergy\\";
		String path = "C:\\Users\\n7682905\\results\\test-tmp\\";
		int fn = processOneDir(path);
		
		saveToFile(path+"tmp.mydat");
		
		Assert.assertEquals(10, fn);
	}
}
