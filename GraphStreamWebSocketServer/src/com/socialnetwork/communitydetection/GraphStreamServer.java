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
	
	GraphStreamServer( int port ) throws UnknownHostException {
		
		super ( new InetSocketAddress( port ) );
		
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
		
		LouvainAlgorithm la = new LouvainAlgorithm(conn);
		
		la.SimpleAlgorithm();
		
		
	}
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		int port = 8887; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		GraphStreamServer s = new GraphStreamServer( port );
		s.start();
		System.out.println( "GraphStreamServer started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			if( in.equals( "exit" ) ) {
				s.stop();
				break;
			} else if( in.equals( "restart" ) ) {
				s.stop();
				s.start();
				break;
			}
		}
	}

}
