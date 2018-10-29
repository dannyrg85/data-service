package DanielLangley;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DataServiceServer {

	private int numberOfClients;
	private FileWriter fileWriter;
	private byte[] bytes;
    private int duplicates;
    private int unique;
    private int totalUnique;
    private int totalDuplicates;
    private boolean openServer;
    private ServerSocket serverSocket;
    private Semaphore semaphore;
    
    /**
     * Data Services Code Challenge
     * DataServicesServer.java
     * 
     * @author Daniel Langley, <dannyrg85@gmail.com>
     * @version 1.0 10/29/2018
     * 
     */
    
    
    /*
     * This private method will be called by the ServerThread class when openServer is set to false.
     * 
     */
    private void closeAll() throws IOException
    {
    	while(true)
    	{
    		if(numberOfClients == 0)
    		{
    			fileWriter.close();
    			serverSocket.close();
    			System.out.println("Closing down now");
    			System.out.println("The final amount of unique numbers is: "+ (totalUnique+unique));
    			System.out.println("The final amount of duplicate numbers is "+ (totalDuplicates+duplicates));
    			System.exit(0);
    		}
    	}		
    }
    
    /*
     * This public method is used to start the server. 
     *  
     */
	public void StartServer() throws IOException
	{
		numberOfClients = 0;
		semaphore = new Semaphore(1);
		//bytes is used to represent each number 0-999,999,999 with a bit for each
		bytes = new byte[125000000];
		//when openServer is set to false the threads will stop running and closeAll() will be called
		openServer = true;
		
		
		//creates numbers.log locally, will be re-written each time server is ran
		String absolutePath = new File("src/main/resources/numbers.log").getAbsolutePath();
		fileWriter = new FileWriter(absolutePath);
		
		//this socket will be passed to ServerThread when assigned a client
        Socket socket = null;

        try 
        {
            serverSocket = new ServerSocket(4000);
        } 
        catch (IOException e) 
        {
            System.out.println("Could not open server on port 4000");
            e.printStackTrace();
        }
        
        //starts a TimingThread to begin monitoring
        new TimingThread().start();
        
        //continuously wait for a client to connect
        while (true) 
        {
        	
	            try 
	            {   
	            	socket = serverSocket.accept();
	            	while(numberOfClients>4)
	            	{
	            		//holds socket until there is room for another client
	            	}
	            } 
	            catch (IOException e) 
	            {
	                System.out.println("The server is closed...");
	                System.exit(0);
	            }
	            
	            //creates a new ServerThread passing in socket as parameter
	            new ServerThread(socket).start();	
        }
	}
	
	/*
	 * This private class will be used for creating a new ServerThread when StartServer has a new
	 * 	client to connect. 
	 */
	
	private class ServerThread extends Thread
	{
		private Socket socket;
		
		public ServerThread(Socket clientSocket) 
		{
			numberOfClients++;
			System.out.println("New client connected... Total clients: "+numberOfClients);
		    this.socket = clientSocket;
		}
	    public void run() 
	    {
	    	
	        InputStream inputStream = null;
	        BufferedReader reader = null;
	        
	        //this will be the number we get from inputStream
	        int number = 0;
	        try 
	        {
	            inputStream = socket.getInputStream();
	            reader = new BufferedReader(new InputStreamReader(inputStream));  
	        } catch (IOException e) 
	        {
	        	System.out.println("There was an error getting input stream.");
	        	e.printStackTrace();
	        	numberOfClients--;
	            return;
	        }
	        
	        //continuously reads lines from inputStream until openServer is set to close
	        while (openServer) {
	            try 
	            {
	            	String inputFromClient = reader.readLine();
	            	
	            	if(inputFromClient != null)
	            	{
	            		/*
	            		 * Series of if, else and try that will result in 1 of 3 things
	            		 * 1. Nine decimal number is read
	            		 * 2. Terminate is read
	            		 * 3. Anything else is read
	            		 */
	            		if(inputFromClient.equalsIgnoreCase("terminate"))
		            	{
	            			openServer = false;
		            	}
		            	else
		            	{
		            		if(inputFromClient.length() == 9)
		            		{
		            			
		            			try {
		            				number = Integer.parseInt(inputFromClient);
		            			} catch(NumberFormatException e){
		            				numberOfClients--;
		            				return;
		            			}
		            		}
		            		else
		            		{
		            			numberOfClients--;
		            			return;
		            		}
		            	}
	            		
	            		//acquire the semaphore to ensure there will be no data loss or duplicates
	            		try {
							semaphore.acquire();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	            		
	            		
	            		/*
	            		 * This is how I ensure there is no duplicates in list
	            		 * Each number represents a bit
	            		 * number/8 is byte in bytes[] array
	            		 * number%8 is bit in that array
	            		 */
	            		
	            		int posByte = (number/8); 
	            	    int posBit = (number%8);
	            	    
	            	    //this grabs bit value of current number
	            	    int valInt = bytes[posByte]>>(8-(posBit+1)) & 0x0001;
	            	    
	            	    /**
	            	     * If the bit is 1 we have already written that number
	            	     * Alternatively, if it is 0 we will write the number and flip the bit to 1
	            	     */
	            		if(valInt == 1)
	            		{
	            			duplicates++;
	            		}
	            		else
	            		{
	            			
	            			fileWriter.write(""+number+'\n');
	            			bytes[posByte] = (byte) (bytes[posByte] | (1 << 8-(posBit+1)));
	            			unique++;
	            		}
	            		
	            		//Releases the semaphore so the next thread can check bytes[] array and use fileWriter
	            		semaphore.release();
	            	}
	                
	            } catch (IOException e) 
	            {
	                e.printStackTrace();
	                numberOfClients--;
	                return;
	            }
	        }
	        
	        
	        //We reach this segment of code when openServer is set to false which only 
	        //	happens when we are ready to terminate	        
	        numberOfClients--;
	        
	        try {
				closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	
	
	/**
	 * This private class is called from the StartServer method to start the monitoring process.
	 */
	private class TimingThread extends Thread
	{
		public TimingThread()
		{
			unique = 0;
			duplicates = 0;
			totalUnique = 0;
			totalDuplicates = 0;
		}
		public void run()
		{
			while(openServer)
			{
				//acquire semaphore to ensure accurate numbers are outputed
				try {
					semaphore.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				totalUnique += unique;
				totalDuplicates += duplicates;
				
				System.out.println("New Unique: "+unique);
				System.out.println("New Duplicates: "+duplicates);
				System.out.println("Total Unique: " +totalUnique);
				System.out.println("Total Duplicates: "+ totalDuplicates);
				System.out.println("Number of clients: " +numberOfClients);
				
				unique=0;
				duplicates = 0;
				
				semaphore.release();
				
				//sleep for 10 seconds to repeat the process when active again
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			}
		}
	}
}
