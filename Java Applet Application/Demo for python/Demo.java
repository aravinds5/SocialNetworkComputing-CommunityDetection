
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


public class Demo{
	public Demo()
	{
		
	}
	
	public static void main(String[] args) throws PyException {
		
		String[] argss = new String[] {"C:\\Python27\\python.exe","D:\\demo.py","D:\\dataset.txt"};
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
        try {
                while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        
	} 
}
