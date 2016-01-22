package com.gff.spacenauts.desktop.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

import com.gff.spacenauts.Globals;

public class Register implements Callable<String> {

	private static final String CMD_REGISTER = "REGISTER";
	private static final String ANS_UNKNOWN = "UNKNOWN";
	private static final int TIMEOUT = 10000;

	private String data;
	private BufferedReader reader;
	private PrintWriter writer;
	private Socket socket;

	public Register(String nickname, int hostTimeout, String hostData) {
		data = String.format("%s %s %d %s", CMD_REGISTER, nickname, hostTimeout, hostData);
	}

	@Override
	public String call() throws Exception {
		String ans;
		try {
			socket = new Socket(Globals.serverAddress, Globals.MULTIPLAYER_PORT);
			socket.setSoTimeout(TIMEOUT);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);

			writer.println(data);
			ans = reader.readLine();
		} catch (Exception e) {
			throw e;
		} finally {
			if (socket != null) socket.close();
		}

		if (ans != null)
			return ans;
		else return ANS_UNKNOWN;
	}

}
