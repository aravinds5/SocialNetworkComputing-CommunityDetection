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
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.Sink;

public class Network implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private GraphStreamSenderSink senderSink;
	private Graph graph;
	
	/*
	 * Generates a random graph of 100 nodes with number of edges between 
	 * 99 to 10000 edges
	 */
	public Network( GraphStreamSenderSink sink ){
		senderSink = sink;
	}
	
	
	public void ConstructGraph( String file ) throws IOException{
		graph = new SingleGraph("firstGraph");
		
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
            
           Double edgeWeight = (splittedLine.length > 2) ? Double.parseDouble(splittedLine[2]) : 1;
           
           graph.addEdge(j.toString( ), splittedLine[0], splittedLine[1]);
           graph.getEdge(j).addAttribute("weight", edgeWeight);
           line = bufferedReader.readLine();
           j++;
        }
        bufferedReader.close();
        
        System.out.println(" Number of nodes :" + graph.getNodeCount() );
        System.out.println(" Number of edges :" + graph.getEdgeCount() );
	}
	
	
	public Iterator<Node> getNeighborList( final String nodeId ) {
		return graph.getNode(nodeId).getNeighborNodeIterator( );
	}
	
	
	public int getNumNodes( ) {
		
		return graph.getNodeCount();
	}
	
	public int getNumEdges( ) {
		
		return graph.getEdgeCount();
	}
	

}
