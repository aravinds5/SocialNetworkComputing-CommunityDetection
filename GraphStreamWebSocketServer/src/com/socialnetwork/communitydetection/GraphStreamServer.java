package com.socialnetwork.communitydetection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class GraphStreamServer extends WebSocketServer {
	
	private String inputFile;
	
	GraphStreamServer( int port, String file ) throws UnknownHostException {
		
		super ( new InetSocketAddress( port ) );
		inputFile = file;
		
	}
	
	GraphStreamServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		
		System.out.println( "Opened connection");
		LouvainAlgorithm la = new LouvainAlgorithm(conn,inputFile);
		la.SimpleAlgorithm();
		
	}
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		int port = 8887; // 843 flash policy port
		GraphStreamServer s = new GraphStreamServer( port, args[0] );
		s.start();
		System.out.println( "GraphStreamServer started on port: " + s.getPort() );
		}
	}
