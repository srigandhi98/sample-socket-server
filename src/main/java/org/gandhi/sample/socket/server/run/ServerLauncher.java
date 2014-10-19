package org.gandhi.sample.socket.server.run;

import org.gandhi.sample.socket.server.Server;

public class ServerLauncher {

	public static void main(String args[]) throws Exception{
		Server s = new Server(8080);
		s.start();
	  //Thread.sleep(10000);
	  //s.stopServer();
	}
}