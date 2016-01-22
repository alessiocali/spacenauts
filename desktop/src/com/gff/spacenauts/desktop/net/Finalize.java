package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

public class Finalize implements Callable<String> {

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	
	public Finalize(Socket socket) {
		this.socket = socket;
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
		} catch (Exception e) {
			if (socket != null) socket.close();
			throw e;
		}
		
		return ans;
	}
	
	public Socket getSocket() {
		return socket;
	}
}
