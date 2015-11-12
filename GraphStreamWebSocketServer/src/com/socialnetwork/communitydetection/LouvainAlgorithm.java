package com.socialnetwork.communitydetection;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.algorithm.measure.NormalizedMutualInformation;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.java_websocket.WebSocket;
import org.json.*;
import org.graphstream.algorithm.Toolkit;

/*
 * Credits: https://github.com/csdashes/GraphStreamCommunityDetection
 * 
 */



public class LouvainAlgorithm {
	
	private GraphStreamSenderSink sink;
	
	private String inputFile;
	
	private Network inputNetwork;
	
	private Network finalNetwork;
	
	private Modularity modularity;
	
	private double maxModularity, newModularity, initialModularity, deltaQ, globalMaxQ, globalNewQ;
	
	private String oldCommunity,bestCommunity;
	
	private Iterator<Node> neighbors;
	
    private Random color;
    private int r, g, b;
    
    private SpriteManager sm;
    private Sprite communitiesCount,
            modularityCount,
            nmiCount;
	
	private int step;
	
	private NormalizedMutualInformation nminfo;
	
	private List<Map<String, HyperCommunity>> communitiesPerPhase;
	
	
	public LouvainAlgorithm( WebSocket conn, String file) {
		
		sink = new GraphStreamSenderSink( conn );
		inputFile = file;
		
		inputNetwork = new Network(sink);
		try {
			inputNetwork.ConstructGraph(inputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		globalMaxQ = -0.5;
	}
	
	public void runAlgorithm( ) {
		
		initialize( );
		globalNewQ = optimizeModularity(inputNetwork);
        
		System.out.println("Modularity : " + globalNewQ);
		
		finalNetwork = inputNetwork;
       
        while (globalNewQ > globalMaxQ) {
        	globalMaxQ = globalNewQ;
        	inputNetwork = foldingCommunities(inputNetwork);
        	globalNewQ = optimizeModularity(inputNetwork);   
        }
       
        printFinalGraph( finalNetwork ); 
	}
	
	public void initialize( ) {
		
		
		Graph g = inputNetwork.getGraph();
		
	    for (Node node : g) {
	    	Set<String> communityNodes = new HashSet<String>();
	        communityNodes.add(node.getId());
	        node.addAttribute("trueCommunityNodes", communityNodes);
	    }
	    
	    communitiesPerPhase = new ArrayList<Map<String, HyperCommunity>>();
	    
	    nminfo = new NormalizedMutualInformation("community","groundTruth");
	    nminfo.init(g);
	    
	    g.display();
	}
	
	public double optimizeModularity( Network network  ) {
		
		
		Graph g = network.getGraph();
		
		modularity = new Modularity("community", "weight");
        modularity.init(g);
        
        Map<String, HyperCommunity> communities = new HashMap<String, HyperCommunity>();

        for (Node node : g) {
        	
            //node.addAttribute("ui.label", node.getId()); 
            HyperCommunity community = new HyperCommunity();

            node.addAttribute("community", community.getAttribute());
            communities.put(community.getAttribute(), community);
        }

        communitiesPerPhase.add(communities);
        int i = 0;
        do {
        	i++;
            initialModularity = modularity.getMeasure();
            for (Node node : g) {
                maxModularity = -0.5;
                newModularity = -0.5;
                oldCommunity = node.getAttribute("community");
                bestCommunity = oldCommunity;

                // For every neighbour node of the node, test if putting it to it's
                // community, will increase the modularity.
                neighbors = node.getNeighborNodeIterator();
                
                
                while (neighbors.hasNext()) {

                    Node neighbour = neighbors.next();

                    // Put the node in the neighbour's community.
                    node.changeAttribute("community", neighbour.getAttribute("community").toString());

                    // Calculate new modularity
                    newModularity = modularity.getMeasure();
                    
                    System.out.println("Node ID :" + node.getId() + " Merged With Neighbor : " + neighbour.getId());
                    System.out.println("New Modularity : " + newModularity);
                    
                    if (newModularity > maxModularity) {
                        maxModularity = newModularity;
                        bestCommunity = neighbour.getAttribute("community").toString();
                    }
                }
                
                System.out.println("Best community found : " + bestCommunity);
                
                if (node.getAttribute("community") != bestCommunity) {
                    node.changeAttribute("community", bestCommunity);
                }       
            }
            deltaQ = modularity.getMeasure() - initialModularity;
            
            System.out.println("Iteration number : " + i );
            System.out.println("DeltaQ  : " + deltaQ );
            
        } while (deltaQ > 0);
        
        //System.out.println("NMI: " + nminfo.getMeasure());

       //System.out.println("Modularity measure : " + modularity.getMeasure( ) );
        return modularity.getMeasure();
	}
	
	 public Network foldingCommunities(Network network) {

	        // Group nodes by community and count the edge types. Knowing the number
	        // of each edge type (inner and outer) is necessary in order to create
	        // the folded graph.
		 	
		 	Graph g = network.getGraph();
		 	
	        Map<String, HyperCommunity> communities = communitiesPerPhase.get(communitiesPerPhase.size() - 1);
	        ListMultimap<String, Node> multimap = ArrayListMultimap.create();
	        HyperCommunity community;
	        for (Node node : g) {
	            multimap.put((String) node.getAttribute("community"), node);
	            community = communities.get(node.getAttribute("community")); // Get the community object 
	            // from the node's attribute.
	            community.increaseNodesCount();  // increase the count of the community's nodes by 1.
	            neighbors = node.getNeighborNodeIterator();
	            while (neighbors.hasNext()) {
	                // If the neighbour and the node have the same community attribute then increase
	                // the inner edges of the community, otherwise increase the outer edges of the 
	                // community to the neighbour's community.
	                Node neighbour = neighbors.next();
	                double edgeWeightBetweenThem = (Double)node.getEdgeBetween(neighbour).getAttribute("weight");
	                String neighbourCommunity = neighbour.getAttribute("community");
	                if (neighbourCommunity.equals(node.getAttribute("community"))) {
	                    community.increaseInnerEdgesWeightCount(edgeWeightBetweenThem);
	                    community.addNodesSet((HashSet<String>) node.getAttribute("trueCommunityNodes"));
	                } else {
	                    community.increaseEdgeWeightToCommunity(neighbourCommunity, edgeWeightBetweenThem);
	                }
	            }
	        }


	        // Remove from the map the communities with 0 nodes.
	        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
	            Entry<String, HyperCommunity> entry = it.next();
	            if (communities.get(entry.getKey()).getNodesCount() == 0) {
	                it.remove();
	            }
	        }

	        // Finilize the inner edges weight count (divide it by 2)
	        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
	            Entry<String, HyperCommunity> entry = it.next();
	            communities.get(entry.getKey()).finilizeInnerEdgesWeightCount();
	        }
	        
	        g.clear();
	        // Creation of the folded graph.
	        g = new SingleGraph("communitiesPhase2");

	        g.display();
	        String edgeIdentifierWayOne,
	                edgeIdentifierWayTwo,
	                edgeIdentifierSelfie;
	        Entry<String, HyperCommunity> communityEntry;
	        Entry<String, Double> edgeWeightToCommunity;
	        List<String> edgeIdentifiers = new ArrayList<String>(); // Keep a list of edge ids so we
	                                                                // don't add the same edge twice.
	        Edge edge;
	        double innerEdgesWeight;

	        // For every community
	        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
	            communityEntry = it.next();
	            // and for every community that the above community is connected to,
	            // create the two nodes and the between them edge, with a weight equal
	            // to the total weight of the outer edges between these two communities.
	            Map<String, Double> outerEdgesWeights = communityEntry.getValue().getEdgeWeightToCommunity();
	            for (Iterator<Entry<String, Double>> outerEdgesWeightIt = outerEdgesWeights.entrySet().iterator(); outerEdgesWeightIt.hasNext();) {
	                edgeWeightToCommunity = outerEdgesWeightIt.next();
	                edgeIdentifierWayOne = communityEntry.getKey() + ":" + edgeWeightToCommunity.getKey();
	                edgeIdentifierWayTwo = edgeWeightToCommunity.getKey() + ":" + communityEntry.getKey();
	                if (!edgeIdentifiers.contains(edgeIdentifierWayOne) && !edgeIdentifiers.contains(edgeIdentifierWayTwo)) {
	                    if (g.getNode(communityEntry.getKey()) == null) {
	                        g.addNode(communityEntry.getKey()).addAttribute("trueCommunityNodes", communityEntry.getValue().getCommunityNodes());
	                    }
	                    if (g.getNode(edgeWeightToCommunity.getKey()) == null) {
	                        g.addNode(edgeWeightToCommunity.getKey()).addAttribute("trueCommunityNodes", communities.get(edgeWeightToCommunity.getKey()).getCommunityNodes());
	                    }
	                    edge = g.addEdge(edgeIdentifierWayOne,
	                            communityEntry.getKey(),
	                            edgeWeightToCommunity.getKey());
	                    edge.addAttribute("weight", Double.parseDouble(String.valueOf(edgeWeightToCommunity.getValue())));
	                    edge.addAttribute("ui.label", edgeWeightToCommunity.getValue());

	                    edgeIdentifiers.add(edgeIdentifierWayOne);
	                    edgeIdentifiers.add(edgeIdentifierWayTwo);
	                }
	            }
	            
	            // Add a self-edge to every node, with a weight that represents the total 
	            // sum of the inner edges weights of the community.
	            innerEdgesWeight = communityEntry.getValue().getInnerEdgesWeightCount();
	            edgeIdentifierSelfie = communityEntry.getKey() + ":" + communityEntry.getKey();
	            edge = g.addEdge(edgeIdentifierSelfie,
	                    communityEntry.getKey(),
	                    communityEntry.getKey());
	            edge.addAttribute("weight", Double.parseDouble(String.valueOf(innerEdgesWeight)));
	        	
	        	}
	        
	        	//g.display();
	        	
	        return network;
	    }
	
	 public void printFinalGraph(Network finalNetwork2) {
		 
		 
		 Graph finalGraph = finalNetwork2.getGraph();
		 // Cleaning up the communities of the original graph
	        // TO-DO: could be done by a function.
	        for (Node node : finalGraph) {
	            node.addAttribute("community", "");
	            node.addAttribute("ui.style", "size: 20px;");
	        }

	        // Creating the communities count on the display screen.
	        sm = new SpriteManager(finalGraph);
	        communitiesCount = sm.addSprite("CC");
	        communitiesCount.setPosition(Units.PX, 20, 20, 0);
	        communitiesCount.setAttribute("ui.label",
	                String.format("Communities: %d", this.inputNetwork.getGraph().getNodeCount()));
	        communitiesCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");

	        finalGraph.display(true); // display the graph on the screen.
	        modularity.init(finalGraph);

	        // Creating the modularity count on the display screen.
	        modularityCount = sm.addSprite("MC");
	        modularityCount.setPosition(Units.PX, 20, 60, 0);
	        modularityCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");
	        
	        nmiCount = sm.addSprite("NMIC");
	        nmiCount.setPosition(Units.PX, 20, 100, 0);
	        nmiCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");

	        // Color every node of a community with the same random color.
	        color = new Random();
	        for (Node community : this.inputNetwork.getGraph()) {
	            r = color.nextInt(255);
	            g = color.nextInt(255);
	            b = color.nextInt(255);
	            Set<String> communityNodes = community.getAttribute("trueCommunityNodes");
	            for (Iterator<String> node = communityNodes.iterator(); node.hasNext();) {
	                Node n = finalGraph.getNode(node.next());
	                n.addAttribute("community", community.getId());
	                n.addAttribute("ui.style", "fill-color: rgb(" + r + "," + g + "," + b + "); size: 20px;");
	                modularityCount.setAttribute("ui.label",
	                        String.format("Modularity: %f", modularity.getMeasure()));
	                nmiCount.setAttribute("ui.label",
	                        String.format("NMI: %f", nminfo.getMeasure()));
	                sleep();
	            }
	        }

	        // If an edge connects nodes that belong to different communities, color
	        // it gray.
	        for (Iterator<? extends Edge> it = finalGraph.getEachEdge().iterator(); it.hasNext();) {
	            Edge edge = it.next();
	            if (!edge.getNode0().getAttribute("community").equals(edge.getNode1().getAttribute("community"))) {
	                edge.addAttribute("ui.style", "fill-color: rgb(236,236,236);");
	            }
	        }
	    }
	protected void sleep() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
    }
}
