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

/**
 * This thread manages the connection between two players once they've established a channel 
 * through the WifiP2P API. If the ConnectionThread is provided with a serverAddress, it will
 * act as a client, if not it will act as a server. After successfully initiating the sockets
 * the two peers will exchange a simple handshake and then start communicating using a
 * {@link InThread} and an {@link OutThread}. Once the IO Threads are running (or an error occurs)
 * the ConnectionThread returns. Its current status (whether running, exited for success or for failure)
 * can be checked by {@link #getConnectionState()} and {@link #getFailureReason()}.  
 * 
 * @author Alessio
 */
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
	private ConnectionThreadState state = ConnectionThreadState.RUNNING;
	private String failureReason;
	private ServerSocket serverSocket;
	private InetAddress serverAddress;
	private InThread inThread;
	private OutThread outThread;

	public ConnectionThread(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	public ConnectionThread() {
		this(null);
	}

	@Override
	public void run () {
		try {
			//Init sockets
			if (serverAddress == null) {
				serverSocket = new ServerSocket(Globals.MULTIPLAYER_PORT);
				serverSocket.setSoTimeout(SO_TIMEOUT);
				socket = serverSocket.accept();
				serverSocket.close();
			} else {
				socket = new Socket(serverAddress, Globals.MULTIPLAYER_PORT);
			}

			socket.setSoTimeout(SO_TIMEOUT);

			//Init IO Streams
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

			//Handshake
			writer.println(MSG_READY);
			String ans = reader.readLine();

			if (ans.equals(MSG_READY)) {
				//Handshake good. Init IO Threads
				inThread = new InThread(socket);
				outThread = new OutThread(socket);
				synchronized (state) {
					state = ConnectionThreadState.SUCCESS;
				}
				return;
			} else {
				//Handshake invalid. Fail.
				synchronized (state) {
					state = ConnectionThreadState.FAIL;
					failureReason = FAIL_INVALID_RESPONSE + ans;
					return;
				}
			}
		} catch (SocketTimeoutException sto) {
			//Timeout exception
			synchronized (state) {
				state = ConnectionThreadState.FAIL;
				failureReason = FAIL_TIMEOUT;
				return;
			}
		} catch (IOException ioe) {
			//IO exception
			synchronized (state) {
				state = ConnectionThreadState.FAIL;
				failureReason = FAIL_IO_ERROR + ioe.getMessage();
			}
			ioe.printStackTrace();
			return;
		} finally {
			//Close any outstanding resource
			//Do not close socket if connection is successful.
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
	
	/**
	 * Override of Thread.interrupt() to close any outstanding resource.
	 */
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