package com.socialnetwork.communitydetection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.text.View;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.Sink;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class Network implements Serializable {
	
	// Construct a graph from reading a file from filesystem
	public static Graph ConstructGraph( String file, Sink sink ) throws IOException{
		Graph graph = new SingleGraph("communities");
		graph.addSink(sink);
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
           
           graph.addEdge(j.toString( ), splittedLine[0], splittedLine[1]).addAttribute("weight", edgeWeight);
           line = bufferedReader.readLine();
           j++;
        }
        bufferedReader.close();
        System.out.println("Graph read from disk");
        return graph;
	}
}
