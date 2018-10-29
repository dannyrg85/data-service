package DanielLangley;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import org.junit.Test;

public class GoodClient {

	/*
	 * This file simulates a good client who continuously sends numbers to server
	 */
	@Test
	public void test() throws UnknownHostException, IOException {
		
		Socket clientSocket = new Socket("localhost", 4000);
		
		DataOutputStream  outToServer = 
	             new DataOutputStream(clientSocket.getOutputStream()); 
		
		
	             
		Random r = new Random();
		
		
		while(true)
		{
			outToServer.writeBytes(String.format("%09d", r.nextInt(1000000000) ) + '\n');
		}
		
	}
		
}


