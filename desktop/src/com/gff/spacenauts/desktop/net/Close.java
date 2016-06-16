package com.gff.spacenauts.desktop.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.gff.spacenauts.Globals;

/**
 * Java Runnable to send a CLOSE command to the SGPP server.
 * It acts differently Guest side from Host side.
 * 
 * @author Alessio
 *
 */
public class Close implements Runnable {

	private Agent agent;
	private String cookie;
	private Socket socket;
	private PrintWriter writer;
	
	/**
	 * Close for a host.
	 * 
	 * @param cookie the host's private cookie.
	 */
	public Close(String cookie) {
		this.cookie = cookie;
		agent = Agent.HOST;
	}
	
	/**
	 * Close for a guest.
	 * 
	 * @param socket the guest's current connection.
	 */
	public Close(Socket socket) {
		this.socket = socket;
		agent = Agent.GUEST;
	}
	
	@Override
	public void run() {
		try {
			if (agent == Agent.HOST)
				socket = new Socket(Globals.serverAddress, Globals.MULTIPLAYER_PORT);
			
			writer = new PrintWriter(socket.getOutputStream(), true);
			
			if (agent == Agent.HOST)
				writer.println("CLOSE " + cookie);
			else
				writer.println("CLOSE");
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
