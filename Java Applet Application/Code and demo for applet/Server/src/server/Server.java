package server;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class Server implements Runnable {
	Thread srvThread=null;
	ServerSocket server=null;
	ThreadSocket clients[]=null;
	public Server()
	{
		start();
	}
	public void start(){
		try{
			server=new ServerSocket(506);
			clients=new ThreadSocket[20];				
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
		srvThread=new Thread(this);
		srvThread.start();
	}
	public void stop(){
		if(srvThread==null){
			//11111
			return;
		}
		try{
			srvThread=null;
			for(int i=0;i<clients.length;i++)
			{if(clients[i]!=null){
				clients[i].close();
				clients[i]=null;
			}
		}
			clients=null;
			server.close();
			server=null;
			//11111
		}catch(IOException e){
			e.printStackTrace();
			}
		}
	public static void main(String[] args) {
		Server LS=new Server();

	}
	@Override
	public void run() {
		while(true){//(cur==srvThread){
			try{
				Socket ts=server.accept();		
				System.out.println("New client accepted!");
				int i;
				for(i=0;i<clients.length;i++){
					if(clients[i]==null){
						clients[i]=new ThreadSocket(this,ts);
						clients[i].start();
						break;
					}
				}
				if(i>=clients.length){
					ts.close();
					ts=null;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

public class ThreadSocket extends Thread
{
	public Server server=null;
	public Socket s=null;
	String name=null;
	public DataOutputStream out=null;
	public BufferedReader in=null;
	public ObjectOutputStream obout=null;
	public ObjectInputStream obin =null; 

	public ThreadSocket(Server server,Socket s)
	{
		this.server=server;
		this.s=s;
	}
	public void sendmsg(String str)
	{
		try{
    		out.writeBytes(str+"\n");
    		System.out.println("SENDMSG"+str);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
	public void run()
	{
		System.out.println("New thread running~~~");
		while(s!=null)
			try{
				in=new BufferedReader(new InputStreamReader(s.getInputStream()));
				out=new DataOutputStream(s.getOutputStream());
				String str=null;
				ThreadSocket ts=this;
				str=in.readLine();	
				boolean f=false;
				//switch(Integer.parseInt(str)){
				//	case 0:
						System.out.println("Connected!");				
						//out.writeBytes("100\n");											
						obout = new ObjectOutputStream(s.getOutputStream());
						obout.writeObject(getNetwork());
						System.out.println("Network sent!");
						obout.flush();  		          
						obout.close();  
						
					    close();
				//	break;
				//}
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	public Network getNetwork() throws IOException
	{
		ModularityOptimizer.main(null);
		return ModularityOptimizer.getNetwork();
	}
	public void close()
	{
		try{
			in.close();
			s.close();
			server=null;
			s=null;	
		}catch(IOException e){}
	}
	}
}