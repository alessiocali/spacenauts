package com.gff.spacenauts.android.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.gff.spacenauts.Globals;

public class ConnectionThread extends Thread {

	public enum ConnectionThreadState {
		RUNNING, SUCCESS, FAIL
	}

	private static final String FAIL_IO_ERROR = "IO Exception: ";
	private static final String FAIL_INVALID_RESPONSE = "Friend sent wrong response. Response was: ";
	private static final String FAIL_TIMEOUT = "Connection timeout";

	private static final String MSG_READY = "READY";
	private static final int SO_TIMEOUT = 10000;

	private Socket socket;
	private ServerSocket serverSocket;
	private InetAddress serverAddress;
	private InThread inThread;
	private OutThread outThread;
	private ConnectionThreadState state = ConnectionThreadState.RUNNING;
	private String failureReason;

	public ConnectionThread(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	public ConnectionThread() {
		this(null);
	}

	@Override
	public void run () {
		try {
			if (serverAddress == null) {
				serverSocket = new ServerSocket(Globals.MULTIPLAYER_PORT);
				serverSocket.setSoTimeout(SO_TIMEOUT);
				socket = serverSocket.accept();
				serverSocket.close();
			} else {
				socket = new Socket(serverAddress, Globals.MULTIPLAYER_PORT);
			}

			socket.setSoTimeout(SO_TIMEOUT);

			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

			writer.println(MSG_READY);

			String ans = reader.readLine();

			if (ans.equals(MSG_READY)) {
				inThread = new InThread(socket);
				outThread = new OutThread(socket);
				synchronized (state) {
					state = ConnectionThreadState.SUCCESS;
				}
				return;
			} else {
				synchronized (state) {
					state = ConnectionThreadState.FAIL;
					failureReason = FAIL_INVALID_RESPONSE;
					return;
				}
			}
		} catch (SocketTimeoutException sto) {
			synchronized (state) {
				state = ConnectionThreadState.FAIL;
				failureReason = FAIL_TIMEOUT;
				return;
			}
		} catch (IOException ioe) {
			synchronized (state) {
				state = ConnectionThreadState.FAIL;
				failureReason = FAIL_IO_ERROR;
			}
			ioe.printStackTrace();
			return;
		} finally {
			if (state != ConnectionThreadState.SUCCESS && socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public InThread getInput () {
		return inThread;
	}

	public OutThread getOutput () {
		return outThread;
	}

	public ConnectionThreadState getConnectionState () {
		synchronized (state) {
			return state;
		}
	}

	public String getFailureReason () {
		synchronized (state) {
			return failureReason;
		}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		try {
			if (!socket.isClosed()) socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (!serverSocket.isClosed()) serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}