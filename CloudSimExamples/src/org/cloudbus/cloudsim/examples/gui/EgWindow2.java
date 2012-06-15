package org.cloudbus.cloudsim.examples.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.LayoutManager2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.axis.utils.IOUtils;


public class EgWindow2 extends JFrame implements ActionListener {
	private JLabel statusLab;
	private JLabel[] labels;
	private JTextField[] texts;
	private JButton[] buttons;
	private JTextArea statusArea;
	private int running = 0;
	private Thread thread;
	public EgWindow2(){
		setTitle("my example");
		setSize(800,600);
		
		
		JPanel p1 = new JPanel();
		this.getContentPane().add(p1);
		LayoutManager2 vLayout = new BorderLayout();
		p1.setLayout(vLayout);
		
		
		
		
		Container myContentFrame= new JPanel();
		
		
		
		GridLayout layout = new GridLayout(10,4);
		myContentFrame.setLayout(layout);
		myContentFrame.setSize(500, 380);
		labels = new JLabel[20];
		texts = new JTextField[10];
		buttons = new JButton[10];
		for (int i=0;i<10;i++){
			labels[i] = new JLabel("");
			myContentFrame.add(labels[i]);
			
			texts[i] = new JTextField();
			myContentFrame.add(texts[i]);
			
			labels[i+10] = new JLabel("");
			myContentFrame.add(labels[i+10]);
			
			buttons[i] = new JButton("btn"+i);
			buttons[i].setVisible(false);
			myContentFrame.add(buttons[i]);
		}
		
		
		presetParams();
		
		buttons[0].setText("reset parameters");
		buttons[0].setVisible(true);
		buttons[0].addActionListener(this);
		
		buttons[2].setText("save parameters");
		buttons[2].setVisible(true);		
		buttons[2].addActionListener(this);
		
		buttons[3].setText("load parameters");
		buttons[3].setVisible(true);		
		buttons[3].addActionListener(this);
		
		buttons[5].setText("run");
		buttons[5].setVisible(true);		
		buttons[5].addActionListener(this);
		
		buttons[7].setText("stop");
		buttons[7].setVisible(true);		
		buttons[7].addActionListener(this);
		
		JPanel myStatusFrame= new JPanel();
		myStatusFrame.setMinimumSize(new Dimension(600, 180));
		myStatusFrame.setVisible(true);
		statusArea = new JTextArea("");
		statusArea.setEditable(false);
		statusArea.setAutoscrolls(true);
		statusArea.setMinimumSize(new Dimension(600, 180));
		//statusArea.setSize(600,180);
		statusLab = new JLabel("");
		statusLab.setBackground(Color.gray);
		statusLab.setSize(100,100);
		
		myStatusFrame.setLayout(new BorderLayout());
		myStatusFrame.add(statusArea,BorderLayout.NORTH);
		myStatusFrame.add(statusLab, BorderLayout.SOUTH );
		
		p1.add(myStatusFrame,BorderLayout.SOUTH);
		p1.add(myContentFrame,BorderLayout.NORTH);
		
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
	}

	private void presetParams() {
		labels[0].setText("VM numbers(vNum)");
		texts[0].setText("100,200,300,400,500");
		labels[1].setText("Capacity Index(CI)");
		texts[1].setText("5");
		//labels[11].setText("PM numbers(pNum)");
		//texts[2].setText("20,40,60,80,100");
		//texts[2].setEditable(false);
		
		labels[2].setText("run times");
		texts[2].setText("10");
		
		labels[3].setText("Roulette Crossover Rate");
		texts[3].setText("0.5");
		labels[13].setText("0.5 represents 50%");
		
		labels[4].setText("Mutation Rate");
		texts[4].setText("0.1");
		labels[14].setText("0.1 represents 10%");
		
		labels[5].setText("Results dir");
		texts[5].setText("c:\\tmp");
		
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JButton btn = null;
		if (arg0.getSource() instanceof JButton)
			btn = (JButton)arg0.getSource();
		if (btn == null){ 
			if( arg0.getID()==9999){				
				setStatus(arg0.getActionCommand());
			}
			
			return;
		}
		
		if (btn.getText().equals("stop")){
			if (thread!=null && thread.isAlive()){
				thread.stop();
				setStatus("task stopped");
			}
		}				
		else if (btn.getText().equals("reset parameters"))
			this.presetParams();
		else if (btn.getText().equals("save parameters"))
		{ 
			String saveFilePath = chooseFile(btn.getText());
			setStatus(saveFilePath);
			File f = new File(saveFilePath);
			FileOutputStream os=null;
			try {
				os = new FileOutputStream(f);
			} catch (FileNotFoundException e) {				
				e.printStackTrace();
			}
			try {
				if (os!=null)
				os.write(getParamStr().getBytes());
				os.close();
				setStatus("saved parameters to "+saveFilePath);
			} catch (IOException e) {				
				e.printStackTrace();
			}
			
			
			
		}
		else if (btn.getText().equals("load parameters"))
		{
			String loadFilePath = chooseFile(btn.getText());
			setStatus("loading parameters from "+loadFilePath);
			File f = new File(loadFilePath);
			FileInputStream in=null;
			try {
				in = new FileInputStream(f);
			} catch (FileNotFoundException e) {				
				e.printStackTrace();
			}
			try {
				byte[] bytes = new byte[1024];
				int len =0;
				if (in!=null){
					len  = in.read(bytes);
					in.close();
				}
				String s = "";
				for(int i=0;i<len;i++)
					s += (char)bytes[i];
				setParamStr(s);
			} catch (IOException e) {				
				e.printStackTrace();
			}
			
			setStatus("loaded parameters from "+loadFilePath);
		}
		else if (btn.getText().equals("run"))
		{
			if (running!=0){				
				return;
			}
			setStatus("running, please check the logs");
			final EgWindow2 frame = this;
			thread = new Thread(new Runnable() {

				@Override
				public void run() {
					String args = getParamStr();
					try {
						CallDCEnergy.call(args,frame);
					} catch (Exception e) {						
						setStatus(e.getMessage());
					}
					running = 0;
					setStatus("process finished, please check the logs");
				}
			});
			thread.start();	
		}
	}
	
	private String getParamStr(){
		String result = "";
		for(int i=0;i<10;i++){
			result+=texts[i].getText()+"|";
		}
		return result;
	}
	
	private void setParamStr(String s){
		StringTokenizer tokenizer = new StringTokenizer(s,"|");
		String[] params = new String[tokenizer.countTokens()];
		int j=0;
		while(tokenizer.hasMoreElements()){
			params[j++] = tokenizer.nextToken();
		}
		
		if (params.length<=texts.length-5 ) return;
		for(int i=0;i<10;i++){
			if (i<params.length){
				texts[i].setText(params[i]);
			}
		}
	}
	
	private String chooseFile(String title){
		final JFileChooser fc = new JFileChooser(); 
		fc.setDialogTitle(title);
		fc.setApproveButtonText(title);		
		fc.setMultiSelectionEnabled(false);
		fc.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				
				return "txt file";
			}
			
			@Override
			public boolean accept(File arg0) {
				if (arg0.getName().contains(".txt"))
					return true;
				return false;
			}
		});
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return fc.getSelectedFile().getAbsolutePath();
		return "";
	}
	
	public void setStatus(String s){
		statusArea.setText(s);
	}
}
