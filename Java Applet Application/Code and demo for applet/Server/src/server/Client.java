package server;

import java.applet.Applet;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.graphstream.algorithm.community.DecentralizedCommunityAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
/*
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
*/
import org.python.core.PyException;


public class Client extends Applet implements Runnable{
	Socket client=null;
    BufferedReader in=null;
    DataOutputStream out=null;
    ObjectOutputStream obout;
	ObjectInputStream obin;
    Thread curThread=null;
    static String server="localhost";//"192.168.0.14";
    String str=null;
	private Graphics graphics;
	static int numofedges=0;
	static int numofnodes=0;
	static int numofc=0;
    public void init()
	{
		
	}
    public void start(){
		action();
    }
	public Client()
	{
		
	}
	public void action()
	{
		try{
			//System.out.println("Socket TRY");
			client=new Socket(server,506);
			System.out.println("Socket"+client);
			in=new BufferedReader(new InputStreamReader(client.getInputStream()));
			out=new DataOutputStream(client.getOutputStream());
			out.writeBytes("0\n");
			curThread=new Thread(this);
			curThread.start();
		}catch(Exception ce){
			System.out.println("Server not started!");
		}
	}
	public void paint(Graphics g){  
	    g.drawString("Please wait!",40,60); 
	    graphics=g;
	}  
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			System.out.println("READING!");
			//str=in.readLine();	
			//System.out.println(str);
			
			obin=new ObjectInputStream(client.getInputStream());
			Network net=(Network)obin.readObject();
			createGraph(net);
			showInfo();
			System.out.println("Finished!");
		}catch(IOException | ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	public void showInfo(){
		repaint();
		//update(graphics);
	}
	public void update(Graphics g){  
	    g.drawString("Fishished!",40,80); 
	    g.drawString("Number of nodes:"+numofnodes,40,100);
	    g.drawString("Number of edges:"+numofedges,40,120);
	    g.drawString("Number of communities:"+(numofc+1),40,140);
	}  
	private static void createGraph(Network network) throws IOException {
    	Graph graph = new SingleGraph("Tutorial 1");
		
		graph.display();
		
		for(int i=0;i<network.getNNodes();i++)
		{
			graph.addNode(i+"");
		}
		
		int[][] edge=network.edges;

		int[] edges=edge[0];
		for (int k=0;k<edges.length;k++)
		{
			int n1=edge[0][k];
			int n2=edge[1][k];
			if (graph.getEdge(n1+"+"+n2)==null && graph.getEdge(n2+"+"+n1)==null){
            	graph.addEdge(n1+"+"+n2, n1+"", n2+"");
            }        
		}
		for(int i=0;i<network.getNNodes();i++)
    	{
	    	Node node=graph.getNode(i+"");
	    	int color=network.nodecolor[i];
	    	//int r=color%3*100;
	    	//int g=(color%3)*20;
	    	//int b=color*80;
	    	//node.addAttribute("ui.style", "fill-color: rgb("+r+","+g+","+b+");");	    	
	    	node.addAttribute("ui.label", color+"");
	    	if (color>numofc)
	    	{
	    		numofc=color;
	    	}
	    }
		numofedges=network.getNEdges();
		numofnodes=network.getNNodes();
	}
	
}
