package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

import com.gff.spacenauts.Globals;

public class Connect implements Callable<String> {

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
			socket.setSoTimeout(10000);
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
