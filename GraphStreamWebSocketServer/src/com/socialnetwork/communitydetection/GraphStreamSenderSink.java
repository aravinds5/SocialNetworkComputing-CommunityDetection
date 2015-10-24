package com.socialnetwork.communitydetection;


import org.graphstream.stream.Sink;


public class GraphStreamSenderSink implements Sink {
	
	private GraphStreamServer serversocket;
	
	
	GraphStreamSenderSink( GraphStreamServer graphStreamServer ) {
		serversocket = graphStreamServer;
	}
	
	@Override
	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId, String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edgeAttributeChanged(String sourceId, long timeId, String edgeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edgeAttributeRemoved(String sourceId, long timeId, String edgeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void graphAttributeAdded(String sourceId, long timeId, String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void graphAttributeChanged(String sourceId, long timeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void graphAttributeRemoved(String sourceId, long timeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeAttributeRemoved(String sourceId, long timeId, String nodeId, String attribute) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void graphCleared(String sourceId, long timeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stepBegins(String sourceId, long timeId, double step) {
		// TODO Auto-generated method stub
		
	}

}
