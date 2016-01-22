package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

import com.gff.spacenauts.Globals;

public class StatusCheck implements Callable<String> {

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private String cookie;

	/**
	 * Initializes StatusCheck operation for Host. A new connection will be made.
	 * 
	 */
	public StatusCheck(String cookie) {
		this.cookie = cookie;
	}

	/**
	 * Initializes StatusCheck operation for Guest. IO handlers for ongoing connection must be given
	 * 
	 * @param reader
	 * @param writer
	 */
	public StatusCheck(Socket socket) {
		this.socket = socket;
	}

	@Override
	public String call() throws Exception {
		String ans = null;
		try {
			if (socket == null)
				socket = new Socket(Globals.serverAddress, Globals.MULTIPLAYER_PORT);

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			
			String query = cookie == null ? "STATUS" : "STATUS " + cookie;
			writer.println(query);
			ans = reader.readLine();

			if (ans == null) ans = "UNKNOWN";

			if (!ans.equals("MATCHED") && !ans.equals("WAITING") && cookie == null) 
				socket.close();	//Close socket for unexpected response (GUEST)
			else if (!ans.equals("MATCHED") && cookie != null) 
				socket.close(); //Close socket when not matched (HOST)
		} catch (Exception e) {
			if (socket != null) socket.close();
			e.printStackTrace();
			throw e;
		}

		return ans;
	}

	public Socket getSocket() {
		return socket;
	}
}
