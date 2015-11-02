package com.socialnetwork.communitydetection;


import org.graphstream.stream.Sink;
import org.java_websocket.WebSocket;
import org.json.*;


public class GraphStreamSenderSink implements Sink {
	
	private WebSocket socketConnection;
	
	
	GraphStreamSenderSink( WebSocket conn ) {
		socketConnection = conn;
	}
	
	@Override
	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId, String attribute, Object value) {

		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject().put("operation","edgeAttributeAdded").
							put("edgeId", edgeId).
								put(attribute,value);
	
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		// send jsonObj to websocket endpoint
		doSend(jsonObj);
	}
	
	@Override
	public void edgeAttributeChanged(String sourceId, long timeId, String edgeId, String attribute, Object oldValue, Object newValue) {

			JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject().put("operation","edgeAttributeChanged").
							put("edgeId",edgeId).
								put(attribute,newValue);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// send jsonObj to websocket endpoint
			doSend(jsonObj);
		}

	@Override
	public void edgeAttributeRemoved(String sourceId, long timeId, String edgeId, String attribute) {

		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject().put("operation","edgeAttributeRemoved").
						put("edgeId",edgeId).
							put(attribute,JSONObject.NULL);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// send jsonObj to websocket endpoint
		doSend(jsonObj);
	}

	@Override
	public void graphAttributeAdded(String sourceId, long timeId, String attribute, Object value) {
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation", "graphAttributeAdded").
		    			put(attribute,value);
		} catch (JSONException e) {
		    e.printStackTrace();
		}

		// send jsonObj to websocket endpoint
		doSend(jsonObj);
	}

	@Override
	public void graphAttributeChanged(String sourceId, long timeId, String attribute, Object oldValue, Object newValue) {
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation","graphAttributeChanged").
		    			put(attribute,newValue);
		} catch (JSONException e) {
		    e.printStackTrace();
		}

		// send jsonObj to websocket endpoint
		doSend(jsonObj);
		
	}

	@Override
	public void graphAttributeRemoved(String sourceId, long timeId, String attribute) {
		
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation","graphAttributeRemoved").
		    			put(attribute,JSONObject.NULL);
		} catch (JSONException e) {
		    e.printStackTrace();
		}

		// send jsonObj to websocket endpoint
		doSend(jsonObj);
		
	}

	@Override
	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {
		// make an JSON object
		JSONObject jsonObj = null;
		try {
		    if (attribute.equalsIgnoreCase("xyz")) {
			jsonObj = new JSONObject().put("operation","NodeAttributeAdded").
						put("x",((Double) ((Object[]) value)[0])).
						put("y", ((Double) ((Object[]) value)[1])).
						put("z", ((Double) ((Object[]) value)[2]));
					
		    } else {
			jsonObj =
				new JSONObject().put("operation", "NodeAttributeAdded").
					put("nodeId",nodeId).
					put(attribute,value);
		    }
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		
		// send jsonObj to websocket endpoint
		doSend(jsonObj);
		
	}

	@Override
	public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue, Object newValue) {
		// make an JSON object
				JSONObject jsonObj = null;
				try {
				    if (attribute.equalsIgnoreCase("xyz")) {
					jsonObj = new JSONObject().put("operation","NodeAttributeChanged").
								put("x",((Double) ((Object[]) newValue)[0])).
								put("y", ((Double) ((Object[]) newValue)[1])).
								put("z", ((Double) ((Object[]) newValue)[2]));
							
				    } else {
					jsonObj =
						new JSONObject().put("operation", "NodeAttributeChanged").
							put("nodeId",nodeId).
							put(attribute,newValue);
				    }
				} catch (JSONException e) {
				    e.printStackTrace();
				}
				
				// send jsonObj to websocket endpoint
				doSend(jsonObj);
		
	}

	@Override
	public void nodeAttributeRemoved(String sourceId, long timeId, String nodeId, String attribute) {
		JSONObject jsonObj = null;
		try {
		    jsonObj = 
			    new JSONObject().put("operation","nodeAttributeRemoved").
			    put(attribute,JSONObject.NULL);
		} catch (JSONException e) {
		    e.printStackTrace();
		}

		// send jsonObj to websocket endpoint
		doSend(jsonObj);

	}

	@Override
	public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {
		// make an JSON object
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation", "edgeAdded").
		    			put("edgeId",edgeId).
		    			put("fromNodeId",fromNodeId).
		    			put("toNodeId", toNodeId).
		    			put("isDirected",directed);

		} catch (JSONException e) {
		    e.printStackTrace();
		}

		// send jsonObj to websocket endpoint
		doSend(jsonObj);
	}

	@Override
	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation", "edgeRemoved").
		    			put("edgeId",edgeId);
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		// send jsonObj to websocket endpoint
		doSend(jsonObj);	
	}

	@Override
	public void graphCleared(String sourceId, long timeId) {
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation","graphCleared");
		} catch (JSONException e) {
		    e.printStackTrace();
		}

		// send jsonObj to websocket endpoint
		doSend(jsonObj);
	}

	@Override
	public void nodeAdded(String sourceId, long timeId, String nodeId) {

		// make an JSON object
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation", "nodeAdded").
		    			put("nodeId",nodeId);
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		
		// send jsonObj to websocket endpoint
		doSend(jsonObj);
		
	}

	@Override
	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		// make an JSON object
		JSONObject jsonObj = null;
		try {
		    jsonObj = new JSONObject().put("operation", "nodeRemoved").
		    			put("nodeId",nodeId);
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		
		// send jsonObj to websocket endpoint
		doSend(jsonObj);
		
	}

	@Override
	public void stepBegins(String sourceId, long timeId, double step) {

		
	}
    private void doSend(JSONObject obj) {
    	socketConnection.send(obj.toString());
    }
}
