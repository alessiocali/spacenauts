package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

import com.gff.spacenauts.Globals;

/**
 * Checks back the matchmaking status with the server performing a STATUS SGPP request.
 * 
 * @author Alessio
 *
 */
public class StatusCheck implements Callable<String> {

	private Socket socket;
	private String cookie;
	private BufferedReader reader;
	private PrintWriter writer;
	private Agent agent;
	private Object cleanupLock = new Object();
	private boolean straySocket = false;

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
	 * Initializes StatusCheck operation for Guest. The socket must be handed.
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

			if (agent == Agent.GUEST) {
				if (!ans.equals("MATCHED") && !ans.equals("WAITING")) 
					socket.close();	//Close socket for unexpected response (GUEST)
				else
					conclude();
			}
			
			else if (agent == Agent.HOST) {
				if (!ans.equals("MATCHED"))
					socket.close(); //Close socket when not matched (HOST)
				else
					conclude();
			}
			
		} catch (Exception e) {
			if (socket != null) socket.close();
			e.printStackTrace();
			throw e;
		}

		return ans;
	}
	
	private void conclude () throws IOException {
		synchronized (cleanupLock) {
			if (straySocket) {
				writer.println("CLOSE");
				socket.close();
			} else {
				straySocket = true;
			}
		}
	}

	public Socket getSocket() {
		return socket;
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
}
