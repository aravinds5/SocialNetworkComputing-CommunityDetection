package python;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.python.core.PyException;

import python.Network;

public class Client {
	static Network network;
	static Graph graph;
	public Client()
	{
		
	}
	public static void main(String[] args) throws PyException, IOException {			

		String[] argss = new String[] {"C:\\Python27\\python.exe","D:\\pydemo.py","D:\\large.txt"};
		
		readInputFile(argss[2], 1);
		createGraph(network);
        Process process = null;
		try {
			process = Runtime.getRuntime().exec(argss);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line;
        int i=-1;
        try {		
            while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                    
                    if (line.equals("Community: ")){
                    	i++;                
                    }else
                    {
                    	Node node=graph.getNode(line);
                    	if (node!=null){
                    		node.addAttribute("ui.label", i+"");
                    	}
                    }
                    
            }
        } catch (IOException e) {
                e.printStackTrace();
        }
        System.out.println("Num of Nodes:"+network.nNodes);
        System.out.println("Num of Edges:"+network.nEdges); 
        System.out.println("Num of Communities:"+(i+1)); 
	} 
	private static void createGraph(Network network) throws IOException {
    	graph = new SingleGraph("Tutorial 1");
		
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
	}
	 private static void createGraph(String inputFileName) throws IOException {
	    	BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFileName));

			int i, j, nEdges, nLines, nNodes;
	        String[] splittedLine;

	        bufferedReader = new BufferedReader(new FileReader(inputFileName));

	        nLines = 0;
	        while (bufferedReader.readLine() != null)
	            nLines++;

	        bufferedReader.close();

	        bufferedReader = new BufferedReader(new FileReader(inputFileName));

	        i = -1;
	        int[][] edges=new int[2][nLines];
	        for (j = 0; j < nLines; j++)
	        {
	            splittedLine = bufferedReader.readLine().split("\t");
	            int n1 = Integer.parseInt(splittedLine[0]);
	            int n2 = Integer.parseInt(splittedLine[1]);
	            edges[0][j]=n1;
	            edges[1][j]=n2;
	        }
	        bufferedReader.close();
	        network.edges=edges;
	        System.out.println("Network saved");
		}
	private static Network readInputFile(String fileName, int modularityFunction) throws IOException{ 
		 BufferedReader bufferedReader;
		 double[] edgeWeight1, edgeWeight2, nodeWeight;
		 int i, j, nEdges, nLines, nNodes;
		 int[] firstNeighborIndex, neighbor, nNeighbors, node1, node2;
		 String[] splittedLine;
		
		 bufferedReader = new BufferedReader(new FileReader(fileName));
		
		 nLines = 0;
		 while (bufferedReader.readLine() != null)
		     nLines++;
		
		 bufferedReader.close();
		
		 bufferedReader = new BufferedReader(new FileReader(fileName));
		
		 node1 = new int[nLines];
		 node2 = new int[nLines];
		 edgeWeight1 = new double[nLines];
		 i = -1;
		 for (j = 0; j < nLines; j++)
		 {
		     splittedLine = bufferedReader.readLine().split("\t");
		     node1[j] = Integer.parseInt(splittedLine[0]);
		     if (node1[j] > i)
		         i = node1[j];
		     node2[j] = Integer.parseInt(splittedLine[1]);
		     if (node2[j] > i)
		         i = node2[j];
		     edgeWeight1[j] = (splittedLine.length > 2) ? Double.parseDouble(splittedLine[2]) : 1;
		 }
		 nNodes = i + 1;
		
		 bufferedReader.close();
		
		 nNeighbors = new int[nNodes];
		 for (i = 0; i < nLines; i++)
		     if (node1[i] < node2[i])
		     {
		         nNeighbors[node1[i]]++;
		         nNeighbors[node2[i]]++;
		     }
		
		 firstNeighborIndex = new int[nNodes + 1];
		 nEdges = 0;
		 for (i = 0; i < nNodes; i++)
		 {
		     firstNeighborIndex[i] = nEdges;
		     nEdges += nNeighbors[i];
		 }
		 firstNeighborIndex[nNodes] = nEdges;
		
		 neighbor = new int[nEdges];
		 edgeWeight2 = new double[nEdges];
		 Arrays.fill(nNeighbors, 0);
		 for (i = 0; i < nLines; i++)
		     if (node1[i] < node2[i])
		     {
		         j = firstNeighborIndex[node1[i]] + nNeighbors[node1[i]];
		         neighbor[j] = node2[i];
		         edgeWeight2[j] = edgeWeight1[i];
		         nNeighbors[node1[i]]++;
		         j = firstNeighborIndex[node2[i]] + nNeighbors[node2[i]];
		         neighbor[j] = node1[i];
		         edgeWeight2[j] = edgeWeight1[i];
		         nNeighbors[node2[i]]++;
		     }
		
		 if (modularityFunction == 1)
		     network = new Network(nNodes, firstNeighborIndex, neighbor, edgeWeight2);
		 else
		 {
		     nodeWeight = new double[nNodes];
		     Arrays.fill(nodeWeight, 1);
		     network = new Network(nNodes, nodeWeight, firstNeighborIndex, neighbor, edgeWeight2);
		 }
		 createGraph(fileName);
		 return network;
	}
}
