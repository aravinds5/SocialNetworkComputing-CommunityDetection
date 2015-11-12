package com.socialnetwork.communitydetection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
	
	private Network inputNetwork;
	
	private String inputFile;
	
	public GirvanNewmanAlgorithm( WebSocket conn, String file) {
		
		sink = new GraphStreamSenderSink( conn );
		inputFile = file;
		
		inputNetwork = new Network(sink);
		try {
			inputNetwork.ConstructGraph(inputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void initialize( ) {
		
		Graph g = inputNetwork.getGraph();
		//g.display();
	}
	public void runAlgorithm( int numCommunities ) {
		
		initialize( );
		
		Graph g = inputNetwork.getGraph();
		
		BetweennessCentrality bcb;
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		for ( int i=1 ; i <= numCommunities; i++) {
			
			bcb = new BetweennessCentrality();
	        bcb.setWeightAttributeName("weight");
	        bcb.init(g);
	        bcb.computeEdgeCentrality(true);
	        bcb.compute();
			
	        double maxBetweeness = Double.MIN_VALUE;
	        Edge maxBetweenessEdge = null;
	     
	        for (Iterator<? extends Edge> it = g.getEachEdge().iterator(); it.hasNext();) {
	            Edge edge = it.next();
	            
	           double bc = bcb.centrality(edge);
	           
	           if( bc >= maxBetweeness)
	           {
	        	   maxBetweeness = bc;
	        	   maxBetweenessEdge = edge;
	           }       
	        } 
	        g.removeEdge(maxBetweenessEdge);
	        try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
