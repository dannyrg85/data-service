package DanielLangley;

import java.io.IOException;

import org.junit.Test;


public class DataServiceServerStart {

	/*
	 * This file starts the Server
	 */
	@Test
	public void test() throws IOException {
		DataServiceServer server = new DataServiceServer();
		
		server.StartServer();
	}

}
