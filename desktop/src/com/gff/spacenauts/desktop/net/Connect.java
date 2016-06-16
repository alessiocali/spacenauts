package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

import com.gff.spacenauts.Globals;

/**
 * Java Callable to handle a CONNECT (Guest to Host) interaction with a SGPP server.
 * It will return the server's answer.
 * 
 * @author Alessio
 *
 */
public class Connect implements Callable<String> {
	
	private static final int SO_TIMEOUT = 10000;

	private String answer;
	private String cookie;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Object cleanupLock;
	private boolean straySocket = false;
	public Connect(String cookie) {
		this.cookie = cookie;
		cleanupLock = new Object();
	}

	@Override
	public String call () throws Exception {
		try {
			socket = new Socket(Globals.serverAddress, Globals.MULTIPLAYER_PORT);
			socket.setSoTimeout(SO_TIMEOUT);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);

			writer.println("CONNECT " + cookie);
			answer = reader.readLine();

			if (answer == null) answer = "UNKNWON";

			if (!answer.startsWith("OK")) socket.close();	//Dispose of socket if answer was not expected
			else {
				synchronized (cleanupLock) {
					if (straySocket) {
						//The request was aborted from above, close
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
		return answer;
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
