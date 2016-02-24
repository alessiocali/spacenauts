package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

import com.gff.spacenauts.Globals;

/**
 * Java Callable to handle a CONNECT (Guest to Host) interaction with a SGMP server.
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

	public Connect(String cookie) {
		this.cookie = cookie;
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
		} catch (Exception e) {
			if (socket != null) socket.close();
			throw e;
		}
		return answer;
	}

	public Socket getSocket() {
		return socket;
	}
}
