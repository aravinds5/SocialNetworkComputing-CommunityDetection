package com.socialnetwork.communitydetection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.Sink;

public class Network implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private GraphStreamSenderSink senderSink;
	private Graph graph;
	
	public Network( GraphStreamSenderSink sink ){
		senderSink = sink;
	}
	
	// Construct a graph from reading a file from filesystem
	public void ConstructGraph( String file ) throws IOException{
		graph = new MultiGraph("communities");
		graph.addSink(senderSink);
		
		BufferedReader bufferedReader;
        Integer j,nLines;
        String[] splittedLine;

        bufferedReader = new BufferedReader(new FileReader(file));

        nLines = 0;
        while (bufferedReader.readLine() != null)
            nLines++;

        bufferedReader.close();

        bufferedReader = new BufferedReader(new FileReader(file));
        String line = bufferedReader.readLine();
        j =0;
        while ( line !=null) {
        	
            splittedLine = line.split("\t");
            
            if( graph.getNode(splittedLine[0]) == null )
            {
            	graph.addNode(splittedLine[0]);
            }
            if( graph.getNode(splittedLine[1]) == null )
            {
            	graph.addNode(splittedLine[1]);
            }
            
           Double edgeWeight = (splittedLine.length > 2) ? Double.parseDouble(splittedLine[2]) : 1.0;
           
           graph.addEdge(j.toString( ), splittedLine[0], splittedLine[1],true);
           graph.getEdge(j).addAttribute("weight", edgeWeight);
           
           line = bufferedReader.readLine();
           j++;
        }
        bufferedReader.close();
	}
	
	// Get the graph
	public Graph getGraph( ) {	
		return this.graph;
	}
	
	// Get Neighbor list of a node
	public Iterator<Node> getNeighborList( final String nodeId ) {
		return graph.getNode(nodeId).getNeighborNodeIterator( );
	}
	
	//Get number of neighbors for a given node
	public int getNumNeighbors( String nodeId ) {
		return graph.getNode(nodeId).getDegree();
	}
	
	// Get the weight of the edge between node1Id and node2Id
	public double getEdgeWeight( String node1Id , String node2Id ) {
		return graph.getNode(node1Id).getEdgeBetween(node2Id).getAttribute("weight");
	}
	
	// Get total number of nodes in graph
	public int getNumNodes( ) {	
		return graph.getNodeCount();
	}
	
	// Get total number of edges in graph
	public int getNumEdges( ) {
		return graph.getEdgeCount();
	}
}
