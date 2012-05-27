/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.network.FatTreeTopologicalNode.FatTreeNodeType;

/**
 * Just represents an topological network node
 * retrieves its information from an topological-generated file
 * (eg. topology-generator)
 *
 * @author		Thomas Hohnstein
 * @since		CloudSim Toolkit 1.0
 */
public class FatTreeTopologicalNode extends TopologicalNode {


	private static final int EDGE_START_NUMBER = 1000;

	private static final int AGG_START_NUMBER = 100000;

	private static final int CORE_START_NUMBER = 1000000;

	private FatTreeNodeType nodeType;
	
	private TopologicalGraph graph;
	
	private FatTreeTopologicalNode parent = null;
	
	private List<FatTreeTopologicalNode> children = null;
	
	private List appData;
	
	public enum FatTreeNodeType{
		PM,
		Edge,
		Aggregate,
		Core
	};


	public FatTreeTopologicalNode(int nodeID){
		super(nodeID);
	}

	/**
	 * constructs an new node including world-coordinates
	 */
	public FatTreeTopologicalNode(int nodeID, int x, int y){
		super(nodeID,x,y);
	}

	/**
	 * constructs an new node including world-coordinates and the nodeName
	 */
	public FatTreeTopologicalNode(int nodeID, String nodeName, int x, int y){
		super(nodeID,nodeName,x,y);
	}

	
	public FatTreeNodeType getNodeType(){
		return this.nodeType;
	}

	public void setNodeType(FatTreeNodeType nodeType){
		this.nodeType = nodeType;
	}

	public TopologicalGraph getGraph() {
		return graph;
	}

	public void setGraph(TopologicalGraph graph) {
		this.graph = graph;
	}

	public static FatTreeNodeType FatTreeNodeType(int nodeTypeInt) {
		switch(nodeTypeInt){
		case 0:
				return FatTreeNodeType.PM;
		case 1:
				return FatTreeNodeType.Edge;
		case 2:
				return FatTreeNodeType.Aggregate;
		case 3:
				return FatTreeNodeType.Core;		
		}
		if (nodeTypeInt<0)
			return FatTreeNodeType.PM;
		else
			return FatTreeNodeType.PM.Core;
	}
	
	public static FatTreeNodeType getUpperNodeType(FatTreeNodeType nType) {
		int i = nType.ordinal();
		i = i+1;
		return FatTreeNodeType(i);
	}
	
	public static FatTreeNodeType getLowerNodeType(FatTreeNodeType nType) {
		int i = nType.ordinal();
		i = i-1;
		return FatTreeNodeType(i);
	}
	
	private Iterator<TopologicalLink> getLinkIterator(){
		return graph.getLinkIterator();
	}
	
	
	private Iterator<TopologicalNode> getNodeIterator(){
		return graph.getNodeIterator();
	}
	
	private FatTreeTopologicalNode getNodeById(int id){
		Iterator<TopologicalNode> it = getNodeIterator();
		while(it.hasNext()){
			TopologicalNode node = it.next();
			if ( node.getNodeID() == id){
				return (FatTreeTopologicalNode)node;
			}
		}		
		return null;
	}
	
	static public FatTreeTopologicalNode getNodeByIdInMap(int id){		
		return map.get(id);
	}
	
	static public FatTreeTopologicalNode getNodeByIdInGraph(TopologicalGraph _graph, int id){
		Iterator<TopologicalNode> it = _graph.getNodeIterator();
		while(it.hasNext()){
			TopologicalNode node = it.next();
			if ( node.getNodeID() == id){
				return (FatTreeTopologicalNode)node;
			}
		}		
		return null;
	}
	
	public FatTreeTopologicalNode getUpperNode(){
		Iterator<TopologicalLink> it = getLinkIterator();
		
		while(it.hasNext()){
			TopologicalLink link = it.next();
			if (link.getSrcNodeID()==this.getNodeID()){
				FatTreeTopologicalNode neighbor= getNodeById(link.getDestNodeID());
				if ( neighbor.getNodeType() == getUpperNodeType(this.nodeType) ){
					return neighbor;
				}
				
			}else if(link.getDestNodeID() == this.getNodeID()){
				FatTreeTopologicalNode neighbor= getNodeById(link.getSrcNodeID());
				if ( neighbor.getNodeType() == getUpperNodeType(this.nodeType) ){
					return neighbor;
				}
			}
		}
		return null;
	}
	
	public List<FatTreeTopologicalNode> getLowerNodes(){
		Iterator<TopologicalLink> it = getLinkIterator();
		List<FatTreeTopologicalNode> list = new ArrayList<FatTreeTopologicalNode>();
		while(it.hasNext()){
			TopologicalLink link = it.next();
			if (link.getSrcNodeID()==this.getNodeID()){
				FatTreeTopologicalNode neighbor= getNodeById(link.getDestNodeID());
				if ( neighbor.getNodeType() == getLowerNodeType(this.nodeType) ){
					list.add( neighbor );
				}
				
			}else if(link.getDestNodeID() == this.getNodeID()){
				FatTreeTopologicalNode neighbor= getNodeById(link.getSrcNodeID());
				if ( neighbor.getNodeType() == getLowerNodeType(this.nodeType) ){
					list.add( neighbor );
				}
			}
		}
		return list;
	}
	
	public static List<FatTreeTopologicalNode> getNodesByType(TopologicalGraph graph, FatTreeNodeType type){
		List<FatTreeTopologicalNode> list = new ArrayList<FatTreeTopologicalNode>();
		Iterator<TopologicalNode> it = graph.getNodeIterator();
		while(it.hasNext()){
			FatTreeTopologicalNode node = (FatTreeTopologicalNode) it.next();
			if (node.nodeType == type){
				list.add(node);
			}
		}
		return list;
	}
	
	public List<FatTreeTopologicalNode> getNodesFrom(FatTreeTopologicalNode node){
		List<FatTreeTopologicalNode> list1 = new ArrayList<FatTreeTopologicalNode>();
		List<FatTreeTopologicalNode> list2 = new ArrayList<FatTreeTopologicalNode>();
		FatTreeTopologicalNode node1 = this;
		FatTreeTopologicalNode node2 = node;
		
		list1.add(node1);
		list2.add(node2);
		
		while(node1!=node2){
			if (node1.getUpperNode()!=null){
				node1 = node1.getUpperNode();
				if (!list1.contains(node1))
					list1.add(node1);
			}
			if (node2.getUpperNode()!=null){
				node2 = node2.getUpperNode();
				if (!list2.contains(node2))
					list2.add(node2);
			}
		}
		for (int i= list2.size()-1;i>=0;i--){
			if ( !list1.contains( list2.get(i)) ){
				list1.add(list2.get(i));
			}
		}
		
		return list1;
	}
	
   static HashMap<Integer, FatTreeTopologicalNode> map = new HashMap<Integer, FatTreeTopologicalNode>();
	
	public static FatTreeTopologicalNode orgnizeGraphToTree(TopologicalGraph graph){
		map.clear();
		FatTreeTopologicalNode root = null;		
		Iterator<TopologicalNode> itNode = graph.getNodeIterator();
		while(itNode.hasNext()){
			FatTreeTopologicalNode node = (FatTreeTopologicalNode) itNode.next();
			map.put(node.getNodeID(), node);	
			if (node.getNodeType() == FatTreeTopologicalNode.FatTreeNodeType.Core)
				root = node;
		}
		
		Iterator<TopologicalLink> itLink = graph.getLinkIterator();		
		while(itLink.hasNext()){
			TopologicalLink link = itLink.next();
			FatTreeTopologicalNode node1 =  map.get(link.getDestNodeID());
			FatTreeTopologicalNode node2 =  map.get(link.getSrcNodeID());
			
			if( node1!=null && node2 != null){
				if(node1.nodeType.ordinal() > node2.nodeType.ordinal()){
					node1.addChild(node2);
					node2.setParent(node1);
				}else if(node1.nodeType.ordinal() < node2.nodeType.ordinal()){
					node1.setParent(node2);
					node2.addChild(node1);
				}
			}
			
		}
		return root;
	}
	
	public static TopologicalGraph generateTree(int pmNumber, int portNumber){
		//calculate how many edge-level switches
		int edges = pmNumber / portNumber + (pmNumber % portNumber ==0 ? 0 : 1);
		//calculate how many aggregate-level nodes
		int aggregate = edges / portNumber + (edges % portNumber ==0 ? 0 : 1);
		
		int core = aggregate / portNumber + (aggregate % portNumber ==0 ? 0 : 1);
		
		if (core>1) throw new RuntimeException("core node number should be 1, but is " + core);
		
		TopologicalGraph graph = new TopologicalGraph();
		
		int curPM = 0;
		int curEdges = 0;
		int curAggregate = 0;
		int curCore = 0;
		
		for (int i=0;i<core;i++){
			
			int coreId = CORE_START_NUMBER+curCore;
			FatTreeTopologicalNode coreNode = new FatTreeTopologicalNode(coreId, String.format("%d",coreId), 0, 0);
			coreNode.setNodeType(FatTreeNodeType.Core);
			graph.addNode(coreNode);
			coreNode.setGraph(graph);
			
			for (int j=0;j<portNumber;j++){
				int aggId = AGG_START_NUMBER+curAggregate;
				FatTreeTopologicalNode aggNode = new FatTreeTopologicalNode(aggId, String.format("%d",aggId), 0, 0);
				aggNode.setNodeType(FatTreeNodeType.Aggregate);
				graph.addNode(aggNode);
				aggNode.setGraph(graph);
				graph.addLink(new TopologicalLink(coreId, aggId, 0.0f, 1000.0f));
				
				for (int k=0; k< portNumber;k++){
					int edgeId = EDGE_START_NUMBER+curEdges;
					FatTreeTopologicalNode edgeNode = new FatTreeTopologicalNode(edgeId, String.format("%d",edgeId), 0, 0);
					edgeNode.setNodeType(FatTreeNodeType.Edge);
					graph.addNode(edgeNode);
					edgeNode.setGraph(graph);
					graph.addLink(new TopologicalLink(aggId, edgeId, 0.0f, 1000.0f));
					
					for(int m = 0;m< portNumber;m++){
						int pmId = curPM;
						FatTreeTopologicalNode pmNode = new FatTreeTopologicalNode(pmId, String.format("%d",pmId), 0, 0);
						pmNode.setNodeType(FatTreeNodeType.PM);
						pmNode.setNodeLabel("pm"+pmNode.getNodeID());
						graph.addNode(pmNode);
						pmNode.setGraph(graph);
						graph.addLink(new TopologicalLink(edgeId, pmId, 0.0f, 100.0f));
						if (++curPM>= pmNumber) break;
					}
					
					if (++curEdges>=edges) break;
				}
				if (++curAggregate>=aggregate) break;
			}			
			if (++curCore>=core) break;
		}
		return graph;
	}

	public FatTreeTopologicalNode getParent() {
		return parent;
	}

	public void setParent(FatTreeTopologicalNode parent) {
		this.parent = parent;
	}

	public List<FatTreeTopologicalNode> getChildren() {
		return children;
	}

	public void setChildren(List<FatTreeTopologicalNode> children) {
		this.children = children;
	}
	
	public void addChild(FatTreeTopologicalNode child) {
		if (children==null){
			children = new ArrayList<FatTreeTopologicalNode>();
		}
		
		if (!children.contains(child)){
			children.add(child);
		}
	}
	
	public FatTreeTopologicalNode getLastChild(){
		if (children==null){
			return null;
		}
		int i = children.size() - 1;
		return children.get(i);
	}
	
	public boolean hasNextSibling(){
		if (parent == null)
			return false;
		if (parent.getLastChild() == null)
			return false;
		return parent.getLastChild()!=this;
	}

	public FatTreeTopologicalNode findNodeByLabel(String label) {		
			Iterator<TopologicalNode> it = getNodeIterator();
			while(it.hasNext()){
				TopologicalNode node = it.next();
				if ( node.getNodeLabel().equalsIgnoreCase(label)){
					return (FatTreeTopologicalNode)node;
				}
			}		
			return null;
		}
	
	public void setNodeLabel(String name){
		this.nodeName = name;
	}
	
	
	static private String getPrefix(FatTreeTopologicalNode node){
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
	
	static StringBuilder treeNode2StrBuilder = new StringBuilder();
	
	static public void clearTreeNode2StrBuilder(){
		treeNode2StrBuilder = new StringBuilder();
	}
	
	static public StringBuilder getTreeNode2StrBuilder(){
		return treeNode2StrBuilder;
	}
	
	static public void printTreeNode2(FatTreeTopologicalNode node){
		List<FatTreeTopologicalNode> list = node.getLowerNodes();		
		StringBuilder builder = new StringBuilder();
		builder.append(getPrefix(node));
		builder.append("--"+node.getNodeLabel() + "(" + node.getAppDataStr()+")");
		treeNode2StrBuilder.append(builder.toString()+"\n");
		for(int i=0;i<list.size();i++){
			FatTreeTopologicalNode subNode =list.get(i);
			printTreeNode2(subNode);
		}
	}

	private String getAppDataStr() {
		String str = "";
		if (appData!=null){
			for(Object o:appData)
				str += o +",";
		}
		return str;
	}

	@SuppressWarnings("rawtypes")
	public List getAppData() {
		if(appData==null)
			appData = new ArrayList();
		return appData;
	}


	
}
