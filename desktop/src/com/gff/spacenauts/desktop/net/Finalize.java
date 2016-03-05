package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Java Callable to handle a FINALIZING handshake to a SGMP server.
 * It will return the server answer.
 * 
 * @author Alessio
 *
 */
public class Finalize implements Callable<String> {

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Object cleanupLock;
	private boolean straySocket = false;
	
	public Finalize(Socket socket) {
		this.socket = socket;
		cleanupLock = new Object();
	}
	
	@Override
	public String call() throws Exception {
		String ans = null;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
		
			writer.println("READY");
			ans = reader.readLine();
			
			if (!ans.equals("GAME_READY")) socket.close();
			
			else {
				synchronized(cleanupLock) {
					if (straySocket) {
						writer.println("CLOSE");
						socket.close();
					} else {
						straySocket = true;
					}
				}
			}
		} catch (Exception e) {
			if (socket != null) socket.close();
			throw e;
		}
		
		return ans;
	}
	
	/**
	 * Tries to alert this runnable to close the socket in any case. 
	 * 
	 * @return Whether a stray socket remains (the task was already completed)
	 */
	public boolean end () {
		synchronized (cleanupLock) {
			boolean result = straySocket;
			straySocket = true;
			return result;
		}
	}
	
	public Socket getSocket() {
		return socket;
	}
}
