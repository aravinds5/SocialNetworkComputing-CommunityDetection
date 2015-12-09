package com.socialnetwork.communitydetection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.algorithm.measure.NormalizedMutualInformation;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.java_websocket.WebSocket;

public class GirvanNewmanAlgorithm {
	
	private GraphStreamSenderSink sink;
	
	private Graph inputGraph;
	
	private String inputFile;
	
	public GirvanNewmanAlgorithm( WebSocket conn, String file) {
		
		sink = new GraphStreamSenderSink( conn );
		inputFile = file;
		
		try {
			inputGraph = Network.ConstructGraph(inputFile, sink);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int[] data = {0,0};
		inputGraph.addAttribute("EdgeRemoved", data );
		inputGraph.addAttribute("MaximumBetweeness", 0.0);
	}
	
	public void runAlgorithm( int numCommunities ) {
		
		BetweennessCentrality bcb;
		
		int edgeCount = inputGraph.getEdgeCount();
		
		while ( inputGraph.getEdgeCount() > 0) {
			
			bcb = new BetweennessCentrality();
	        bcb.setWeightAttributeName("weight");
	        bcb.setWeighted();
	        bcb.init(inputGraph);
	        bcb.computeEdgeCentrality(true);
	        bcb.compute();
			
	        double maxBetweeness = Double.MIN_VALUE;
	        List<Edge> maxBetweenessEdges = new ArrayList<Edge>();
	     
	        for (Iterator<? extends Edge> it = inputGraph.getEachEdge().iterator(); it.hasNext();) {
	            Edge edge = it.next();
	            
	           double bc = bcb.centrality(edge);
	           
	           if( bc == maxBetweeness)
	           {
	        	   maxBetweeness = bc;
	        	   maxBetweenessEdges.add(edge);  
	           }
	           else if  ( bc > maxBetweeness )
	           {	
	        	   maxBetweeness = bc;
	        	   maxBetweenessEdges.clear();
	        	   maxBetweenessEdges.add(edge);
	           }
	        }
	        System.out.println("Max Betweeness : " + maxBetweeness);
	        inputGraph.changeAttribute("MaximumBetweeness", maxBetweeness);
	        sleep(2000);
	        removeEdges( maxBetweenessEdges );
		}
	}
	
	protected void removeEdges( List<Edge> edgesToBeRemoved ) {
		
		for (Iterator<? extends Edge> it = edgesToBeRemoved.iterator(); it.hasNext();) {
            Edge edge = it.next();
            
            int Node0 = Integer.parseInt(edge.getNode0().getId());
            int Node1 = Integer.parseInt(edge.getNode1().getId());
            int[] data = {Node0,Node1};
            inputGraph.removeEdge(edge);
            
            inputGraph.changeAttribute("EdgeRemoved", data );
		}
	}
	
	protected void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
