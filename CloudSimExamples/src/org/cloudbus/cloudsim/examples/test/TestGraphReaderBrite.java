package org.cloudbus.cloudsim.examples.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.List;
import java.util.StringTokenizer;

import org.cloudbus.cloudsim.network.FatTreeGraphReaderBrite;
import org.cloudbus.cloudsim.network.FatTreeTopologicalNode;
import org.cloudbus.cloudsim.network.GraphReaderBrite;
import org.cloudbus.cloudsim.network.TopologicalGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestGraphReaderBrite {
	
	private final String fileName = "c:\\users\\n7682905\\graphBrite.txt";
	private String lineSep = System.getProperty("line.separator");
	/*
	 *  the brite-file is structured as followed:
	 * Node-section:
	 *		NodeID, xpos, ypos, indegree, outdegree, ASid, type(router/AS)
	 *
	 * Edge-section:
	 *		EdgeID, fromNode, toNode, euclideanLength, linkDelay, linkBandwith, AS_from, AS_to, type
	 *
	 */
	@SuppressWarnings("deprecation")
	@Before
	public void writeBrite() throws IOException{
		
		/*               7
		 *           /      \
		 *          5        6
		 *     /       \
		 *    3         4
		 *  / | \     / | \ 
		 * 0  1  2   10 11 12
		 */
		StringBuilder builder = new StringBuilder();
		builder.append("Nodes:"+lineSep);
		builder.append("0 0 0 0"+lineSep);
		builder.append("1 1 1 0"+lineSep);
		builder.append("2 2 2 0"+lineSep);
		builder.append("10 0 0 0"+lineSep);
		builder.append("11 1 1 0"+lineSep);
		builder.append("12 2 2 0"+lineSep);		
		builder.append("3 3 3 1"+lineSep);
		builder.append("4 4 4 1"+lineSep);
		builder.append("5 5 5 2"+lineSep);
		builder.append("6 6 6 2"+lineSep);
		builder.append("7 7 7 3"+lineSep);
		builder.append("Edges:"+lineSep);
		builder.append("01 0 3 0 0 100"+lineSep);
		builder.append("02 1 3 0 0 100"+lineSep);
		builder.append("03 2 3 0 0 100"+lineSep);
		builder.append("10 10 4 0 0 100"+lineSep);
		builder.append("11 11 4 0 0 100"+lineSep);
		builder.append("12 12 4 0 0 100"+lineSep);
		
		builder.append("04 3 5 0 0 100"+lineSep);
		builder.append("05 4 5 0 0 100"+lineSep);
		builder.append("06 5 7 0 0 100"+lineSep);
		builder.append("07 6 7 0 0 100"+lineSep);
		
		File f = new File(fileName);
		if (!f.exists()){
			f.createNewFile();
		}
		StringBufferInputStream in = new StringBufferInputStream(builder.toString());
		FileOutputStream out = new FileOutputStream(f);
		while(true){
			int i = in.read();
			if (i!=-1){
				out.write(i);
			}
			else{
				break;
			}				
		}
		in.close();
		out.close();
	}
	
	private void printTreeStructure(TopologicalGraph graph){
		FatTreeTopologicalNode node0 = FatTreeTopologicalNode.getNodeByIdInGraph(graph,7);
		printTreeNode(node0);
	}
	
	private void printTreeNode(FatTreeTopologicalNode node){
		List<FatTreeTopologicalNode> list = node.getLowerNodes();
		int indent = 4-node.getNodeType().ordinal();
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<indent;i++){
			builder.append("  |");
		}
		builder.append("--"+node.getNodeLabel());
		System.out.println(builder.toString());
		for(int i=0;i<list.size();i++){
			FatTreeTopologicalNode subNode =list.get(i);
			printTreeNode(subNode);
		}
	}
	
	private void printTreeNode2(FatTreeTopologicalNode node){
		List<FatTreeTopologicalNode> list = node.getLowerNodes();
		int indent = 4-node.getNodeType().ordinal();
		StringBuilder builder = new StringBuilder();
		builder.append(getPrefix(node));
		builder.append("--"+node.getNodeLabel());
		System.out.println(builder.toString());
		for(int i=0;i<list.size();i++){
			FatTreeTopologicalNode subNode =list.get(i);
			printTreeNode2(subNode);
		}
	}
	
	private String getPrefix(FatTreeTopologicalNode node){
		String result = "";
		if (node!=null) {
			result = getPrefix(node.getParent());
			if (node.hasNextSibling())
				result +=  "  |";
			else
				result +=  "   ";
		}
		return result;
	}
	
	@Test
	public void testWriteBrite() throws IOException{
		File f = new File(fileName);
		FileInputStream in = new FileInputStream(f);
		StringBuffer buf = new StringBuffer();
		
		while(true){
			int i = in.read();
			if (i!=-1){
				buf.append((char)i);
			}else{
				break;
			}
		}
		String s = new String(buf);
		assertEquals("Nodes", s.substring(0, 5));
	}
	
	@Test
	public void testOrgnizeGraphToTree() throws IOException{
		FatTreeGraphReaderBrite rd = new FatTreeGraphReaderBrite();		
		TopologicalGraph graph =  rd.readGraphFile(fileName);
		FatTreeTopologicalNode root = FatTreeTopologicalNode.orgnizeGraphToTree(graph);
		assertEquals(7,root.getNodeID());
		printTreeNode2(root);
		assertEquals(2,root.getChildren().size());
	}
	
	@Test
	public void testGraphRead() throws IOException {

		FatTreeGraphReaderBrite rd = new FatTreeGraphReaderBrite();
		
		TopologicalGraph graph =  rd.readGraphFile(fileName);
		
		System.out.print(graph.toString());
		
		assertEquals(true, graph.toString().contains("7 | x is: 7 y is: 7"));
		assertEquals(true, graph.toString().contains("from: 6 to: 7"));
		
		assertEquals(6, FatTreeTopologicalNode.getNodesByType(graph, FatTreeTopologicalNode.FatTreeNodeType.PM).size());
		assertEquals(2, FatTreeTopologicalNode.getNodesByType(graph, FatTreeTopologicalNode.FatTreeNodeType.Edge).size());
		assertEquals(2, FatTreeTopologicalNode.getNodesByType(graph, FatTreeTopologicalNode.FatTreeNodeType.Aggregate).size());
		assertEquals(1, FatTreeTopologicalNode.getNodesByType(graph, FatTreeTopologicalNode.FatTreeNodeType.Core).size());
		
		FatTreeTopologicalNode coreNode =FatTreeTopologicalNode.getNodesByType(graph, FatTreeTopologicalNode.FatTreeNodeType.Core).get(0);
		assertEquals(2, coreNode.getLowerNodes().size());
		
		
		FatTreeTopologicalNode node0 = FatTreeTopologicalNode.getNodeByIdInGraph(graph,0);
		FatTreeTopologicalNode node1 = FatTreeTopologicalNode.getNodeByIdInGraph(graph,1);
		FatTreeTopologicalNode node6 = FatTreeTopologicalNode.getNodeByIdInGraph(graph,6);
		FatTreeTopologicalNode node11 = FatTreeTopologicalNode.getNodeByIdInGraph(graph,11);
		assertEquals(3, node0.getNodesFrom(node1).size());
		
		FatTreeTopologicalNode node4 = FatTreeTopologicalNode.getNodeByIdInGraph(graph,4);
		assertEquals(3, node4.getLowerNodes().size());
		
		printNodeList(node0.getNodesFrom(coreNode));
		assertEquals(4, node0.getNodesFrom(coreNode).size());
		printNodeList(node0.getNodesFrom(node6));
		assertEquals(5, node0.getNodesFrom(node6).size());
		printNodeList(node0.getNodesFrom(node11));
		assertEquals(5, node0.getNodesFrom(node11).size());
		
		printTreeStructure(graph);
	}
	
	private void printNodeList(List<FatTreeTopologicalNode> list){
		for (int i= 0;i<list.size();i++){
			System.out.print(list.get(i).getNodeLabel()+ " ");
		}
		System.out.println("");
	}
	
	@After
	public void deleteBrite(){
		File f = new File(fileName);
		if (f.exists()){
			f.delete();
		}
	}
	
	@Test
	public void testTokens(){
		StringTokenizer tokenizer = new StringTokenizer("Nodes: 1 2 3");
		assertEquals(4, tokenizer.countTokens());
		while(tokenizer.hasMoreElements()){
			String token = tokenizer.nextToken();
			System.out.println(token);
		}
	}
	
	@Test
	public void testGenerateTree(){		
		TopologicalGraph graph = FatTreeTopologicalNode.generateTree(200, 10);
		FatTreeTopologicalNode root = FatTreeTopologicalNode.orgnizeGraphToTree(graph);
		assertEquals(1000000,root.getNodeID());
		printTreeNode2(root);
		assertEquals(2,root.getChildren().size());
	}

}
