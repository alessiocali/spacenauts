package com.gff.spacenauts.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gff.spacenauts.Logger;
import com.gff.spacenauts.Logger.LogLevel;

/**
 * A thread that receives messages from another player through network. 
 * Message retrieval is synchronized and thread-safe.
 * 
 * @author Alessio Cali'
 *
 */
public class InThread extends Thread {
	
	public enum InThreadStatus {
		RUNNING, CLOSED, FAIL
	}
	
	private final static String TAG = "InThread";
	
	private final static String MSG_CONNECTION_LOST = "CONNECTION LOST";
	private final static String MSG_CLOSE = "CLOSE";

	private static final int QUEUE_SIZE = 1000;
	private Socket socket;
	private BufferedReader reader;
	private Array<String> messageQueue;
	private InThreadStatus status = InThreadStatus.RUNNING;

	public InThread (Socket socket) {
		if (socket == null) throw new GdxRuntimeException(new IllegalArgumentException("Socket can't be null"));
		this.socket = socket;
		messageQueue = new Array<String>(QUEUE_SIZE);
	}

	@Override
	public void run() {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msg;

			while (status == InThreadStatus.RUNNING) {
				msg = reader.readLine();
				
				if (msg == null) break;
				
				if (msg.equals(MSG_CONNECTION_LOST)) {
					Logger.log(LogLevel.WARNING, TAG, "Connection with guest was lost");
					status = InThreadStatus.CLOSED;
				} else if (msg.equals(MSG_CLOSE)) {
					Logger.log(LogLevel.UPDATE, TAG, "Buddy closed connection");
					status = InThreadStatus.CLOSED;
				}
				synchronized(messageQueue) {
					while (messageQueue.size == QUEUE_SIZE) {
						Logger.log(LogLevel.WARNING, TAG, "Full buffer!");
						messageQueue.wait();
					}

					messageQueue.add(msg);
				}
			}

		} catch (SocketException se) {
			Logger.log(LogLevel.WARNING, TAG, "Socket exception: " + se.getMessage());
			status = InThreadStatus.FAIL;
			return;
		} catch (SocketTimeoutException to) {
			Logger.log(LogLevel.WARNING, TAG, "Socket timeout");
			status = InThreadStatus.FAIL;
			return;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			status = InThreadStatus.FAIL;
			return;
		} catch (InterruptedException ine) {
			Logger.log(LogLevel.WARNING, TAG, "Interrupted");
			status = InThreadStatus.CLOSED;
			return;
		}
	}

	/**
	 * Pops one message from the queue. Access is thread safe.
	 * 
	 * @return the first message in queue.
	 */
	public String getMessage() {
		synchronized (messageQueue) {
			if (messageQueue.size == 0) {
				messageQueue.notifyAll();
				return null;
			} else {
				String msg = messageQueue.first();
				messageQueue.removeIndex(0);
				messageQueue.notifyAll();
				return msg;
			}
		}
	}

	/**
	 * Stops thread. Warning: socket must be disposed appropriately.
	 */
	public void close () {
		if (status == InThreadStatus.RUNNING) status = InThreadStatus.CLOSED;
	}

	public InThreadStatus getStatus () {
		return status;
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		if (socket != null) {
			try {
				if (!socket.isClosed()) socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
