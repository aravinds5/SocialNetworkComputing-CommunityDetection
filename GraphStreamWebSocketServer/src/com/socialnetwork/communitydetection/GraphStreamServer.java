package com.socialnetwork.communitydetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GraphStreamServer extends WebSocketServer {
	
	private String inputFile;
	
	List<HyperCommunity> final_community;
	
	Graph final_graph;
	
	private Map<WebSocket,Thread> threadMap;
	
	private static int ConnectionCount = 0;
	
	
	GraphStreamServer( int port, String file ) throws UnknownHostException {
		
		super ( new InetSocketAddress( port ) );
		threadMap = new HashMap<WebSocket,Thread>();
		inputFile = file;
	}
	
	GraphStreamServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println("Connection closed with the reason : " + reason);
		System.out.println("Code : " + code);
		System.out.print("Remote :"+ remote );	
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		System.out.println(" Error occured : " + ex.getMessage() );
	}
	
	
	@Override
	public void onMessage( WebSocket conn, String message ) {
		
		
		System.out.println("Message :"+ message );
		
		
		Thread t = threadMap.get(conn);
		
		if(message.equals("pause"))
		{
			t.suspend();
		}
		else if ( message.equals("resume"))
		{
			t.resume();
		}
		
		if(message.equals("StartLouvain"))
		{	
			Thread t1  = new Thread ( new Runnable() {
				@Override
				public void run() {
					 LouvainAlgorithm la = new LouvainAlgorithm(conn,inputFile);
				     la.runAlgorithm();	
				     
				     final_community = new ArrayList<HyperCommunity>(la.getFinalCommunites().values());
				     
				     final_graph = la.getFinalGraph();
				}	
			});
			
			threadMap.put(conn, t1);
			t1.start(); 
		}
		
		else if(message.equals("StartGirvanNewman"))
		{
			Thread t2  = new Thread ( new Runnable() {
				@Override
				public void run() {
					 GirvanNewmanAlgorithm gn= new GirvanNewmanAlgorithm(conn,inputFile);
				     gn.runAlgorithm(0);	
				}	
			});
			
			threadMap.put(conn, t2);
			t2.start(); 
		}
		else {
			JSONObject gjs = new JSONObject();
			int communityId = Integer.parseInt(message);
			if( communityId <= final_community.size() )
			{	
				Set<String> nodes = final_community.get(communityId).getCommunityNodes();
				
				JSONArray ejs = new JSONArray();
				for ( String a : nodes )
				{
					Node n	= final_graph.getNode(a);
					for ( String b : nodes )
					{
						Node n1 = final_graph.getNode(b);
						if( ! (n.getId().equals(n1.getId())))
						{
							if(n.getEdgeBetween(n1) != null)
							{
								JSONObject edge = new JSONObject();
								try {
									edge.put("edge1", n1);
									edge.put("edge2", n);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								ejs.put(edge);
							}
						}
					}
				}
				JSONObject nmessage = new JSONObject();
				try {
					gjs.put("edges",ejs);
				    nmessage = new JSONObject();
					nmessage.put("operation", "graphAttributeChanged").put("cluster",gjs);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				conn.send(nmessage.toString());
			}
		}
	}
	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		ConnectionCount++;
		System.out.println("Client Connected : " + ConnectionCount );
	}
	
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		//WebSocketImpl.DEBUG = true;
		
		int port = 8887;
		GraphStreamServer s = new GraphStreamServer( port, args[0] );
		
		s.start();
		System.out.println( "GraphStreamServer started on port: " + s.getPort() );
		
		}
	}
