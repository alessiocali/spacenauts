package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

import com.gff.spacenauts.Globals;

public class StatusCheck implements Callable<String> {

	private Socket socket;
	private String cookie;
	private BufferedReader reader;
	private PrintWriter writer;
	private Agent agent;

	/**
	 * Initializes StatusCheck operation for Host. A new connection will be made.
	 * 
	 * @param cookie
	 */
	public StatusCheck(String cookie) {
		this.cookie = cookie;
		agent = Agent.HOST;
	}

	/**
	 * Initializes StatusCheck operation for Guest. The socket must be handled.
	 * 
	 * @param socket
	 */
	public StatusCheck(Socket socket) {
		this.socket = socket;
		agent = Agent.GUEST;
	}

	@Override
	public String call() throws Exception {
		String ans = null;
		try {
			if (agent == Agent.HOST)
				socket = new Socket(Globals.serverAddress, Globals.MULTIPLAYER_PORT);

			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			
			String query = agent == Agent.HOST ? "STATUS " + cookie : "STATUS";
			writer.println(query);
			ans = reader.readLine();

			if (ans == null) ans = "UNKNOWN";

			if (!ans.equals("MATCHED") && !ans.equals("WAITING") && agent == Agent.GUEST) 
				socket.close();	//Close socket for unexpected response (GUEST)
			
			else if (!ans.equals("MATCHED") && agent == Agent.HOST) 
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
