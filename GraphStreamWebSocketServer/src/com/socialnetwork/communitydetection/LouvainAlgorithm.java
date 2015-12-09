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
 * Adopted From: https://github.com/csdashes/GraphStreamCommunityDetection
 * 
 */
public class LouvainAlgorithm {
	
	private static WebSocket my_conn;
	
	private GraphStreamSenderSink sink;
	
	private String inputFile;
	
	private Graph gp;
	
	private double totalEdgeWeight;
	
	private Graph finalNetwork;
	
	private Modularity modularity;
	
	private double maxModularity, newModularity, initialModularity, deltaQ, globalMaxQ, globalNewQ;
	
	private String oldCommunity,bestCommunity;
	
	private Iterator<Node> neighbors;
	
    private Random color;
    private int r, gr, b;
    
	private int step;
	
	private NormalizedMutualInformation nminfo;
	
	private List<Map<String, HyperCommunity>> communitiesPerPhase;
	
	
	public LouvainAlgorithm( WebSocket conn, String file) {
		
		my_conn = conn;
		sink = new GraphStreamSenderSink( conn );
		inputFile = file;
		
		try {
			gp = Network.ConstructGraph(file,sink);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		globalMaxQ = -0.5;
		totalEdgeWeight = getTotalEdgeWeight( gp );
		System.out.println("Total Edge weight : " + totalEdgeWeight);
	}
	
	public Map<String,HyperCommunity> getFinalCommunites()
	{
	  return communitiesPerPhase.get(communitiesPerPhase.size()-2);
	}
	
	public Graph getFinalGraph()
	{
		return this.finalNetwork;
	}
	
	private static void sendAlgEvent(WebSocket conn,String attribute,Object newValue )
	{
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation","graphAttributeChanged").
		    			put(attribute,newValue);
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		conn.send(jsonObj.toString());	
	}
	
	public void runAlgorithm( ) {
		initialize( );
		globalNewQ = optimizeModularity(this.gp);
		
		finalNetwork = this.gp;

        while (globalNewQ > globalMaxQ) {
        	globalMaxQ = globalNewQ;
        	this.gp = foldingCommunities(this.gp);
        	globalNewQ = optimizeModularity(this.gp);
        }
        printFinalGraph( this.gp ); 
	}

	public void initialize( ) {
		
		this.gp.addAttribute("AlgorithmEvent", "");
		this.gp.addAttribute("NumCum", 0);
		this.gp.addAttribute("delQ", 0.0 );
		
	    for (Node node : this.gp) {
	    	Set<String> communityNodes = new HashSet<String>();
	        communityNodes.add(node.getId());
	        node.addAttribute("trueCommunityNodes", communityNodes);
	    }
	    
	    communitiesPerPhase = new ArrayList<Map<String, HyperCommunity>>();
	    
	    nminfo = new NormalizedMutualInformation("community","groundTruth");
	    nminfo.init(this.gp);
	}
	
	public double optimizeModularity( Graph g  ) {
		
		
		modularity = new Modularity("community", "weight");
       // modularity.init(g);
        
        Map<String, HyperCommunity> communities = new HashMap<String, HyperCommunity>();

        for (Node node : g) {
        	
            HyperCommunity community = new HyperCommunity();

            node.addAttribute("community", community.getAttribute());
            communities.put(community.getAttribute(), community);
        }
        
        System.out.println("Communty ids assigned");
        communitiesPerPhase.add(communities);
        int i = 0;
        do {
        	i++;
             //initialModularity = modularity.getMeasure();
        	initialModularity = calculateModularity ( g );
        	sendAlgEvent(my_conn,"globalNewQ", globalNewQ);
            System.out.println("initialModularity = " + initialModularity );
            for (Node node : g) {
               maxModularity = Double.MIN_VALUE;
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
                 
                    
                    newModularity = calculateDeltaQ( node,neighbour);
                        
                    if (newModularity > maxModularity) {
                        maxModularity = newModularity;
                        bestCommunity = neighbour.getAttribute("community").toString();
                    }
                }
                  
                if (node.getAttribute("community") != bestCommunity) {
                    node.changeAttribute("community", bestCommunity);
                }
                
            }  
             deltaQ = calculateModularity( g ) - initialModularity; 
             //this.gp.changeAttribute("delQ", deltaQ);
             sendAlgEvent(my_conn,"delQ", deltaQ);
             
            System.out.println("deltaQ : " + deltaQ);
        } while (deltaQ > 0);
        
        return calculateModularity( g );
	}
	
	public double getTotalEdgeWeight( Node n ) {
		
		double edgeWeight = 0.0;
		
		Collection<Edge> ed = n.getEdgeSet();
		
		
		for( Edge e : ed )
		{
			edgeWeight = edgeWeight + (double)e.getAttribute("weight");
		}
		
		return edgeWeight;
	}
	
	public double getTotalEdgeWeight( Graph g ) {
		return g.getEdgeCount();
	}
	
	
	public double calculateDeltaQ(Node i, Node j) {
		
		double sigmaIn;
		Edge ed = i.getEdgeBetween(i);
		if(ed !=null)
		sigmaIn =(double)ed.getAttribute("weight");
		else
			sigmaIn =0;
		
		double sigmaTot = getTotalEdgeWeight(j) - sigmaIn;
		double ki = getTotalEdgeWeight(i) - sigmaIn;
		double kiIn = (double)i.getEdgeBetween(j).getAttribute("weight");
		
		double a = (sigmaIn + 2*kiIn)/(2*totalEdgeWeight);
		double b = (sigmaTot + ki)*(sigmaTot+ki)/(4*totalEdgeWeight*totalEdgeWeight);
		double c = (sigmaIn)/(2*totalEdgeWeight);
		double d = (sigmaTot*sigmaTot)/(4*totalEdgeWeight*totalEdgeWeight);
		double e = (ki*ki)/(4*totalEdgeWeight*totalEdgeWeight);
		
		double deltaQ = ((a-b))- ((c-d-e)); 
		return deltaQ;
	}
	
	
	
	
	public double calculateModularity( Graph g ) {
		double modularity = 0.0;
		double count = 0;
		for( Node nodei : g )
		{
			double weighti = getTotalEdgeWeight( nodei );
			
			Iterator<Node> neigh = nodei.getNeighborNodeIterator();
			while (neigh.hasNext())
			{
				Node nodej = neigh.next();
				if( !nodei.getId().equals(nodej.getId()) && nodei.getAttribute("community").toString().equals(nodej.getAttribute("community").toString())){
					double aij = 0.0;
					Edge ij = nodei.getEdgeToward(nodej);
					if( ij != null )
					{
						aij = aij + (double)ij.getAttribute("weight"); 
					}
					modularity += (aij - (weighti*getTotalEdgeWeight(nodej)/(totalEdgeWeight))) ;
				}
			}
		}
		modularity = (modularity) / (2*totalEdgeWeight);
		
		return modularity;
	}
	
	 public Graph foldingCommunities(Graph  g) {

	        // Group nodes by community and count the edge types. Knowing the number
	        // of each edge type (inner and outer) is necessary in order to create
	        // the folded graph.
		 	
	        Map<String, HyperCommunity> communities = communitiesPerPhase.get(communitiesPerPhase.size() - 1);
	        ListMultimap<String, Node> multimap = ArrayListMultimap.create();
	        HyperCommunity community = new HyperCommunity();
	        for (Node node : g) {
	            multimap.put((String) node.getAttribute("community"), node);
	            community = communities.get(node.getAttribute("community"));
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
	        
	        
	       // this.gp.changeAttribute("AlgorithmEvent", "graphCleared");
	        
	       //sendAlgEvent(my_conn,"AlgorithmEvent", "graphCleared");
	        
	        // Creation of the folded graph.
	        g =  new SingleGraph("communitiesPhase2");
	        
	       //  g.display();
	       // g.addSink(sink);
	        System.out.println("Gonna Create New graph");
	       
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
	        
	        System.out.println("Community Folding Complete");
	        
	        return g;
	 }
	 
	 public void printFinalGraph(Graph graph) {
		 
		 // Cleaning up the communities of the original graph
	        // TO-DO: could be done by a function.
	        for (Node node : finalNetwork) {
	            node.addAttribute("communityId", "");
	            node.addAttribute("ui.style", "size: 20px;");
	        }

	        modularity.init(finalNetwork);

	        // Color every node of a community with the same random color.
	        color = new Random();
	        for (Node community : graph) {
	            r = color.nextInt(255);
	            gr = color.nextInt(255);
	            b = color.nextInt(255);
	            Set<String> communityNodes = community.getAttribute("trueCommunityNodes");
	            for (Iterator<String> node = communityNodes.iterator(); node.hasNext();) {
	                Node n = finalNetwork.getNode(node.next());
	                n.addAttribute("ui.label", community.getId());
	                n.addAttribute("finalColor", new int[]{r,gr,b});
	            }
	        }

	        // If an edge connects nodes that belong to different communities, color
	        // it gray.
	        for (Iterator<? extends Edge> it = finalNetwork.getEachEdge().iterator(); it.hasNext();) {
	            Edge edge = it.next();
	            if (!edge.getNode0().getAttribute("community").equals(edge.getNode1().getAttribute("community"))) {
	                edge.addAttribute("ui.color", "fill-color: rgb(236,236,236);");
	            }
	        }
	       
	        sendAlgEvent(my_conn,"NumCum", communitiesPerPhase.get(communitiesPerPhase.size()-1).size());
	      
	        this.gp.changeAttribute("AlgorithmEvent", "algorithmEnded");
	        
	        System.out.println("Algorithm Ended");
	    }
	protected void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
        }
    }
}
