package com.socialnetwork.communitydetection.louvainMethod;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.java_websocket.WebSocket;
import org.json.*;

import com.socialnetwork.communitydetection.GraphStreamSenderSink;

public class LouvainAlgorithm {
	
	GraphStreamSenderSink sink;
	
	
	public LouvainAlgorithm( WebSocket conn) {
		
		sink = new GraphStreamSenderSink( conn );
		
	}
	
	public void SimpleAlgorithm( ) {
		
		Graph graph = new SingleGraph("Tutorial 1");
		
		graph.addSink(sink);
		
		graph.addNode("A" );
		graph.addNode("B" );
		graph.addNode("C" );
		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CA", "C", "A");
	}
	

}
