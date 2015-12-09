package com.socialnetwork.communitydetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class GraphStreamServer extends WebSocketServer {
	
	private String inputFile;
	
	
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
