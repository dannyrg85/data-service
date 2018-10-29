package DanielLangley;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import org.junit.Test;

public class TerminatingClient {
	
	/*
	 * This simulates a client that will send 500,000 numbers to the Server then send over a 'terminate'
	 */
	@Test
	public void test() throws UnknownHostException, IOException {
		
		Socket clientSocket = new Socket("localhost", 4000);
		
		DataOutputStream  outToServer = 
	             new DataOutputStream(clientSocket.getOutputStream()); 
		
		
		Random r = new Random();
		
		int i = 0;
		
		while(i<500000)
		{
			i++;
			outToServer.writeBytes(String.format("%09d", r.nextInt(1000000000)) + '\n');
		}
		
		outToServer.writeBytes("terminate");
		
		
	}

}
