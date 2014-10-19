package org.gandhi.sample.socket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import org.gandhi.sample.socket.server.dto.ConnectionInfo;

//Currently just printing the basic error Info to the console for all the exceptions as proper logging is not implemented
//Once we pick up logging  - LOG the complete exception in the ERROR level
public class Server extends Thread {

	private final int PORT;

	private boolean keepServerOn;
	
	private ConcurrentHashMap<ConnectionInfo, Thread> currentRequests;

	public ConcurrentHashMap<ConnectionInfo, Thread> getCurrentRequests() {
		return currentRequests;
	}

	public void setCurrentRequests(ConcurrentHashMap<ConnectionInfo, Thread> currentRequests) {
		this.currentRequests = currentRequests;
	}

	public Server(int port) {
		this.PORT = port;
	}

	public int getPORT() {
		return PORT;
	}
	
	public boolean isKeepServerOn() {
		return keepServerOn;
	}

	public synchronized void stopServer() {
		if(keepServerOn){
			keepServerOn = false;
		} else{
			System.err.println("Server is currently off & not running");
			System.exit(-1);
		}	
	}

	public void startServer() {
		keepServerOn = true;
		currentRequests = new ConcurrentHashMap<ConnectionInfo, Thread>();
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORT);
			serverSocket.setSoTimeout(1000);
			while (keepServerOn) {
				System.out.println(".");
				synchronized (this) {
					Socket clientSocket = null;
					try {
						clientSocket = serverSocket.accept();
					} catch (IOException ioe) {
						//IO Exception while trying to accept a client - May be a time our error waiting for a client - Ignore and move on to waiting for next Client
					}
					if (clientSocket != null) {
						new ClientHandler(clientSocket, this).start();
					}
				}
			}
		} catch (IOException ioe) {
			System.err.println("IOException occured on the server while starting/running - " + ioe.getLocalizedMessage());
		} finally {
			closeServerSocketQuietly(serverSocket);
			keepServerOn = false;
			currentRequests = null;
		}
	}

	private void closeServerSocketQuietly(ServerSocket s) {
		try {
			if (s != null && !s.isClosed())
				s.close();
		} catch (IOException ioe) {
			System.err.println("IOException occured while closing the server socket - " + ioe.getLocalizedMessage());
		}
	}

	@Override
	public void run() {
		startServer();
	}
}