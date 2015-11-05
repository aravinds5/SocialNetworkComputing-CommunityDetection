package com.socialnetwork.communitydetection;
import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.java_websocket.WebSocket;
import org.json.*;

public class LouvainAlgorithm {
	
	private GraphStreamSenderSink sink;
	private String inputFile;
	
	public LouvainAlgorithm( WebSocket conn, String file) {
		
		sink = new GraphStreamSenderSink( conn );
		inputFile = file;
	}
	
	public void SimpleAlgorithm( ) {
		try {	
			Network network = new Network(sink);
			network.ConstructGraph(inputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
