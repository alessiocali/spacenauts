package com.gff.spacenauts.desktop.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.gff.spacenauts.Globals;

public class Close implements Runnable {

	private String cookie;
	private PrintWriter writer;
	private Socket socket;
	
	public Close(String cookie) {
		this.cookie = cookie;
	}
	
	@Override
	public void run() {
		try {
			socket = new Socket(Globals.serverAddress, Globals.MULTIPLAYER_PORT);
			writer = new PrintWriter(socket.getOutputStream(), true);
			writer.println("CLOSE " + cookie);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
